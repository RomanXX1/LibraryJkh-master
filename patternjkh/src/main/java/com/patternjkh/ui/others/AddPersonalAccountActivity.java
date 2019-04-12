package com.patternjkh.ui.others;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.parsers.DebtsParser;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.patternjkh.utils.ToastUtils.showToast;

public class AddPersonalAccountActivity extends BaseMainActionsActivity implements OnAddPersonalAccountFragmentInteractionListener{

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";
    private static final String PHONE = "PHONE";
    private static final String PASSWORD = "PASSWORD";

    private String isCons = "0", id_account, tokenFirebase, mPhone, mPassword, hex;

    private Button btnNoInternetRefresh;
    private LinearLayout layoutNoInternet, layoutMain;
    private ProgressDialog dialog;

    private Server server = new Server(this);
    private DB db = new DB(this);
    private FragmentManager mFragmentManager;
    private SharedPreferences sPref;

    public static Intent newIntent(Context context, String login, String pass, String isCons,String accountId, String token) {
        Intent intent = new Intent(context, AddPersonalAccountActivity.class);
        intent.putExtra("login", login);
        intent.putExtra("pass", pass);
        intent.putExtra("isCons", isCons);
        intent.putExtra("id_account", accountId);
        intent.putExtra("token", token);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_account);
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        getParametersFromPrefs();

        initViews();
        initToolbar();
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));

        if (ConnectionUtils.hasConnection(AddPersonalAccountActivity.this)) {
            showData(savedInstanceState);
            hideNoInternet();
        } else {
            showNoInternet(savedInstanceState);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void showData(Bundle savedInstanceState) {
        Fragment mAddPersonalAccountFragment;
        if (ComponentsInitializer.SITE_ADRR.contains("muprcmytishi")) {
            mAddPersonalAccountFragment = AddPersonalAccountFragmentMytishi.newInstance(mPhone);
        } else {
            mAddPersonalAccountFragment = AddPersonalAccountFragment.newInstance(mPhone);
        }

        mFragmentManager = getSupportFragmentManager();

        replaceFragment(mAddPersonalAccountFragment);

        // Перестраховка если вдруг произойдет сбой и телефон и/или пароль потеряются
        if (savedInstanceState != null){
            if (TextUtils.isEmpty(mPhone)){
                mPhone = savedInstanceState.getString(PHONE);
            }
            if (TextUtils.isEmpty(mPassword)){
                mPassword = savedInstanceState.getString(PASSWORD);
            }
        }
    }

    private void showNoInternet(final Bundle savedInstanceState) {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(AddPersonalAccountActivity.this)) {
                    showData(savedInstanceState);
                    hideNoInternet();
                } else {
                    showNoInternet(savedInstanceState);
                }
            }
        });
    }

    private void hideNoInternet() {
        layoutNoInternet.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
    }

    // Перестраховка если вдруг произойдет сбой и телефон и/или пароль потеряются
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PHONE, mPhone);
        outState.putString(PASSWORD, mPassword);
        super.onSaveInstanceState(outState);
    }

    // Перестраховка если вдруг произойдет сбой и телефон и/или пароль потеряются
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPhone = savedInstanceState.getString(PHONE);
        mPassword = savedInstanceState.getString(PASSWORD);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in); // Bottom Fragment Animation
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public void onAddPersonalAccountFragmentInteraction() {

        db.open();
        checkLoginAndPassword();

        if (!ConnectionUtils.hasConnection(this)) {
            DialogCreator.showInternetErrorDialog(this, hex);
            return;
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Синхронизация данных...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                String line_reg;
                if (isCons.equals("0")) {
                    line_reg = server.regId(id_account, tokenFirebase);
                } else {
                    line_reg = server.regIdCons(id_account, tokenFirebase);
                }

                if (isCons.equals("0")) { // Пользователь НЕ консультант
                    fillCountersData();

                    if (line_reg.equals("1")) {
                        // Получим данные, которые подготовлены для этого пользователя
                        filldata_id();
                    } else {
                        // Получим данные по ВСЕМ заявкам
                        fillAppsFromServer();
                    }

                } else {
                    fillAppsFromServer();
                }

                // получаем уведомления
                String lineNews = server.get_data_news(mPhone);
                if (!lineNews.equals("xxx")) {
                    parse_json_news(lineNews);
                }

                // получаем опросы
                Cursor cursor = db.getDataFromTable(DB.TABLE_GROUP_QUEST);
                String lineQuestions = server.get_need_questions(String.valueOf(cursor.getCount()));
                cursor.close();
                if (lineQuestions.equals("1")) {
                    lineQuestions = server.get_data_questions_answers(mPhone);
                    if (!lineQuestions.equals("xxx")) {
                        parse_json_questions_answers(lineQuestions);
                    }
                }

                // получаем привязанные лицевые счета по номеру телефона
                String linePersonalAccounts = server.getAccountIdents(mPhone);

                String personalAccounts = parseJsonPersonalAccounts(linePersonalAccounts);

                if (isCons.equals("0")) {

                    DebtsParser.getJsonDebts(server, sPref, personalAccounts);

                    // TODO - добавим в базу данные ОСВ (потом удалить)
                    add_data_Bills_Services();

                    setSettingPersonalAccountsForPref(personalAccounts);
                    setResult(RESULT_OK);
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.dismiss();
                    }
                } else {
                    setSettingPersonalAccountsForPref(personalAccounts);
                    setResult(RESULT_OK);
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.dismiss();
                    }
                }
                finish();
            }
        }).start();

    }

    public void checkLoginAndPassword() {

        // Проверка номера телефона
        if (TextUtils.isEmpty(mPhone)) {
            // Не должно сюда дойти т.к. onSaveInstanceState
            showToastHere(getString(R.string.error_failed_add_new_personal_account));
            finish();
        }

        // Проверка пароля
        if (TextUtils.isEmpty(mPassword)) {
            // Не должно сюда дойти т.к. onSaveInstanceState
            showToastHere(getString(R.string.error_failed_add_new_personal_account));
            finish();
        }
    }

    // Получим данные по показаниям счетчиков
    void fillCountersData() {
        String line = "xxx";

        // Получить ВСЕ показания приборов
        try {
            line = server.getCountersMytishi(mPhone, mPassword);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

//            db.del_table(DB.TABLE_COUNTERS);
            db.del_table(DB.TABLE_COUNTERS_MYTISHI);

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                Parser_Get_Counters_Mytishi xpp = new Parser_Get_Counters_Mytishi();
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SAXParser sp = factory.newSAXParser();
                XMLReader reader = sp.getXMLReader();
                reader.setContentHandler(xpp);
                reader.parse(is);

            } catch (Exception e) {
                Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
            }
        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
    }

//    public class Parser_Get_Counters extends DefaultHandler { // Получение показаний приборов
//
//        String login_to_DB = mPhone, str_num_month, str_year = "";
//        String str_count, str_ed_izm, str_uniq_num, str_prev_value, str_value, str_diff = "", ident = "", serial = "", isSent;
//        int num_month, year = 0, typeId;
//
//        @SuppressLint("DefaultLocale")
//        @Override
//        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
//            if (localName.toLowerCase().equals("period")) {
//                str_num_month = atts.getValue("NumMonth");
//                str_year = atts.getValue("Year");
//                num_month = Integer.valueOf(str_num_month);
//                year = Integer.valueOf(str_year);
//            } else if (localName.toLowerCase().equals("metervalue")) {
//                str_count = atts.getValue("Name");
//                str_ed_izm = atts.getValue("Units");
//                str_uniq_num = atts.getValue("MeterUniqueNum");
//                str_prev_value = atts.getValue("PreviousValue");
//                str_value = atts.getValue("Value");
//                str_diff = atts.getValue("Difference");
//                typeId = Integer.valueOf(atts.getValue("MeterTypeID"));
//                ident = atts.getValue("Ident");
//                serial = atts.getValue("FactoryNumber");
//                isSent = atts.getValue("IsSended");
//
//                db.addCount(login_to_DB, num_month, year, str_count, str_ed_izm, str_uniq_num, str_prev_value, str_value, str_diff, typeId, ident, serial, isSent);
//            }
//        }
//    }

    public class Parser_Get_Counters_Mytishi extends DefaultHandler {

        private String login = mPhone, ident="", units="", name="", uniqueNum="", factoryNum="";
        private String periodDate="", value="", isSent="", sendError="";
        private int typeId;

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (localName.toLowerCase().equals("meter")) {
                ident = atts.getValue("Ident");
                units = atts.getValue("Units");
                name = atts.getValue("Name");
                uniqueNum = atts.getValue("MeterUniqueNum");
                typeId = Integer.parseInt(atts.getValue("MeterTypeID"));
                factoryNum = atts.getValue("FactoryNumber");

            } else if (localName.toLowerCase().equals("metervalue")) {
                periodDate = atts.getValue("PeriodDate");
                value = atts.getValue("Value");
                isSent = atts.getValue("IsSended");
                sendError = atts.getValue("SendError");

                db.addCountMytishi(login, ident, units, name, uniqueNum, typeId, factoryNum, periodDate, value, isSent, sendError);
            }
        }
    }

    // Получим данные для конкретного пользователя
    void filldata_id() {

        clearDbTables();

        if (db.getDataFromTable(DB.TABLE_APPLICATIONS).getCount() == 0) {
            fillAppsFromServer();
        } else {

            String line = "";
            Boolean rezult = true;
            try {
                line = server.get_apps_id(mPhone, tokenFirebase);
            } catch (Exception e) {
                rezult = false;
            }
            if (rezult) {
                parse_json(line);
            }
        }
    }

    void fillAppsFromServer() {

        String line = "xxx";

        // получим ВСЕ заявки
        try {
            line = server.get_apps(mPhone, mPassword, "All", isCons);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            clearDbTables();

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                Parser_Get_Apps xpp = new Parser_Get_Apps();
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SAXParser sp = factory.newSAXParser();
                XMLReader reader = sp.getXMLReader();
                reader.setContentHandler(xpp);
                reader.parse(is);

            } catch (Exception e) {
                Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
            }

        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }

    }

    public class Parser_Get_Apps extends DefaultHandler { // Получение заявок

        int id_com = 0, id_app = 0;
        String str_id = "", str_name = "", str_owner = "", str_closeApp = "", str_isRead, str_isReadCons;
        String str_isAnswered, str_client = "", str_customer_id = "", str_tema = "", str_app_date = "";
        String str_text = "", str_date = "", str_id_author = "", str_author = "", str_id_account = "", str_adress = "", str_flat = "", str_phone = "";

        Parser_Get_Apps() {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            if (localName.toLowerCase().equals("row")) {
                str_name = atts.getValue("text");
                try {
                    str_id = atts.getValue("id");
                } catch (Exception e) {
                    str_id = atts.getValue("ID");
                }
                str_owner = mPhone; //atts.getValue("name").toString();
                int closeApp = 0;
                try {
                    str_closeApp = atts.getValue("isActive");
                    closeApp = StringUtils.convertStringToInteger(str_closeApp);
                } catch (Exception e) {
                    closeApp = 0;
                }
                if (closeApp == 1) {
                    closeApp = 0;

                } else if (closeApp == 0) {
                    closeApp = 1;
                }
                int isRead = 0;
                try {
                    str_isRead = atts.getValue("IsReadedByClient");
                    isRead = StringUtils.convertStringToInteger(str_isRead);
                } catch (Exception e) {
                    isRead = 0;
                }

                int isReadCons = 0;
                try {
                    str_isReadCons = atts.getValue("IsReaded");
                    isReadCons = StringUtils.convertStringToInteger(str_isReadCons);
                } catch (Exception e) {
                    isReadCons = 0;
                }
                try {
                    str_client = atts.getValue("CusName");
                } catch (Exception e) {
                    str_client = "";
                }
                int isAnswered = 1;
                try {
                    str_isAnswered = atts.getValue("IsAnswered");
                    isAnswered = StringUtils.convertStringToInteger(str_isAnswered);
                } catch (Exception e) {
                    isAnswered = 1;
                }

                str_tema = atts.getValue("name");
                str_app_date = atts.getValue("added");
                if (str_app_date.length() > 10) {
                    str_app_date = str_app_date.substring(0, 10);
                }

                str_adress = atts.getValue("HouseAddress");
                str_flat = atts.getValue("FlatNumber");

                str_phone = atts.getValue("PhoneNum");

                String type_app = atts.getValue("id_type");

                db.addApp(str_id, str_name, str_owner, closeApp, isRead, isAnswered, str_client, str_customer_id, str_tema, str_app_date, str_adress, str_flat, str_phone, type_app, isReadCons);

            } else if (localName.toLowerCase().equals("comm")) {
                try {
                    id_com = Integer.valueOf(atts.getValue("id"));
                } catch (Exception e) {
                    id_com = Integer.valueOf(atts.getValue("ID"));
                }
                String isHidden = atts.getValue("isHidden");
                id_app = Integer.valueOf(atts.getValue("id_request"));
                str_text = atts.getValue("text");
                str_date = atts.getValue("added");
                str_id_author = atts.getValue("id_Author");
                str_author = atts.getValue("Name");
                str_id_account = atts.getValue("id_account");

                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, id_account, isHidden);

            } else if (localName.toLowerCase().equals("file")) {

                String fileID = atts.getValue("FileID");
                String number = atts.getValue("RequestID");
                String file_name = atts.getValue("FileName");
                String date_time = atts.getValue("DateTime");

                // скачаем иконку приложения
                byte[] data = new byte[1024];
                byte[] bArray = null;
                try {

                    URL url = new URL(ComponentsInitializer.SITE_ADRR + Server.DOWNLOAD_FilE + "id=" + fileID + "&tmode=1");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream input = new BufferedInputStream(url.openStream());
                    File sdDir = android.os.Environment.getExternalStorageDirectory();
                    OutputStream output = new FileOutputStream(sdDir + "/" + file_name);

                    int count = 0;
                    while ((count = input.read(data)) > 0) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    Bitmap photo = BitmapFactory.decodeFile(sdDir + "/" + file_name);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bArray = bos.toByteArray();

                } catch (Exception e) {
                    Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
                }

                db.addFotoWithDate(fileID, String.valueOf(id_app), bArray, "", file_name, date_time);
            }
        }
    }

    // распарсим json приходящий с токенами
    void parse_json(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            String[] str_apps = new String[json_data.length()];
            String[] str_reqs = new String[json_data.length()];
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_app = json_data.getJSONObject(i);
                String id_app = json_app.getString("id_Request");
                String id_tel = json_app.getString("Token");
                // Добавить (обновить заявку в БД), добавить, обновить комментарии в БД
                Boolean rezult_add = add_app(id_app);
                // Удалить заявку по токену на сервере
                if (rezult_add) {
                    str_apps[i] = id_tel;
                    str_reqs[i] = id_app;
                }
            }
            String json_rezult = "[";
            for (int i = 0; i < json_data.length(); i++) {
                json_rezult = json_rezult + "{\"id_Account\":" + id_account + ",\"id_Request\":" + str_reqs[i] + ",\"id_Device\":\"" + tokenFirebase + "\",\"Token\":\"" + str_apps[i] + "\"}";
                if (i != (json_data.length() - 1)) {
                    json_rezult = json_rezult + ",";
                }
            }
            json_rezult = json_rezult + "]";
            // Отправим полученый json на сервер
            String answer = server.del_read_app(json_rezult);
        } catch (JSONException e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
    }

    // Добавить (обновить заявку в БД) по id
    Boolean add_app(String id_app) {
        Boolean rezult = true;
        try {
            String line = new Get_Comms_by_id().execute(id_app).get();

            if (!line.equals("xxx")) {
                try {
                    BufferedReader br = new BufferedReader(new StringReader(line));
                    InputSource is = new InputSource(br);
                    Parser_Get_Comm xpp = new Parser_Get_Comm();
                    SAXParserFactory factory = SAXParserFactory.newInstance();

                    SAXParser sp = factory.newSAXParser();
                    XMLReader reader = sp.getXMLReader();
                    reader.setContentHandler(xpp);
                    reader.parse(is);
                } catch (Exception e) {
                    Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
                    rezult = false;
                }
            }

        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
            rezult = false;
        }
        return rezult;
    }

    class Get_Comms_by_id extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = ComponentsInitializer.SITE_ADRR + server.COMM_BY_ID + "id=" + params[0];
                zapros = zapros.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    public class Parser_Get_Comm extends DefaultHandler {

        int id_com = 0, id_app = 0;
        String str_text = "", str_date = "", str_id_author = "", str_author = "", str_tema = "", str_app_date = "";
        String id, adress, flat, phone;

        Parser_Get_Comm() {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

            db.open();

            if (localName.toLowerCase().equals("comm")) {

                try {
                    id_com = Integer.valueOf(atts.getValue("id"));
                } catch (Exception e) {
                    id_com = Integer.valueOf(atts.getValue("ID"));
                }
                id_app = Integer.valueOf(atts.getValue("id_request"));
                str_text = atts.getValue("text");
                str_date = atts.getValue("added");
                str_id_author = atts.getValue("id_Author");
                str_author = atts.getValue("Name");

                str_tema = atts.getValue("ReqName");
                str_app_date = atts.getValue("ReqAdded");

                adress = atts.getValue("HouseAddress");
                flat = atts.getValue("FlatNumber");

                phone = atts.getValue("PhoneNum");

                String isHidden = atts.getValue("isHidden");

                if (db.getColApp(atts.getValue("id_request"))) {
                    // TODO - обратить внимание, здесь заявка создается из комментария - нету данных об адресе и номере квартиры
                    db.addApp(String.valueOf(id_app), str_text, "", 0, 0, 0, str_author, str_id_author, str_tema, str_app_date, adress, flat, phone, "", 0);
                    // Подтянем файлы - фотографии по заявке
                    getFiles_About_App(String.valueOf(id_app));
                }

                // Запишем новый комментарий в БД
                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, str_id_author, isHidden);

            }
        }
    }

    // Подтянем файлы - фотографии для заявки
    void getFiles_About_App(String str_id) {
        String line = server.get_foto_by_app(str_id, mPhone);
        parse_json_files(line);
    }

    // распарсим json, приходящий со списком файлов
    void parse_json_files(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_file = json_data.getJSONObject(i);
                String file_number = json_file.getString("RequestID");
                String file_name = json_file.getString("FileName");
                String file_date = json_file.getString("DateTime");
                String file_id   = json_file.getString("FileID");

                // скачаем иконку приложения
                byte[] data = new byte[1024];
                byte[] bArray = null;
                try {
                    URL url = new URL(ComponentsInitializer.SITE_ADRR + Server.DOWNLOAD_FilE + "id=" + file_id + "&tmode=1");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream input = new BufferedInputStream(url.openStream());
                    File sdDir = android.os.Environment.getExternalStorageDirectory();
                    OutputStream output = new FileOutputStream(sdDir + "/" + file_name);

                    int count = 0;
                    while ((count = input.read(data)) > 0) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    Bitmap photo = BitmapFactory.decodeFile(sdDir + "/" + file_name);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bArray = bos.toByteArray();

                } catch (Exception e) {
                    Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
                }

                db.addFotoWithDate(file_id, file_number, bArray, "", file_name, file_date);
            }
        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
    }

    private void clearDbTables() {
        db.del_table(DB.TABLE_APPLICATIONS);
        db.del_table(DB.TABLE_COMMENTS);
    }

    void parse_json_news(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_news = json_data.getJSONObject(i);

                String name_news = json_news.getString("Header");
                String date_news = json_news.getString("Created");
                String id_news = json_news.getString("ID");
                String text_news = json_news.getString("Text");

                db.add_news(Integer.valueOf(id_news), name_news, text_news, date_news, "false");
            }
        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
    }

    void parse_json_questions_answers(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_group = json_data.getJSONObject(i);
                String name_group = json_group.getString("Name");
                String id_group = json_group.getString("ID");
                String isRead = json_group.getString("IsReaded");
                int col_questions = 0;
                int col_answered = 0;
                String isAnswered = "false";

                JSONArray json_questions = json_group.getJSONArray("Questions");
                for (int j = 0; j < json_questions.length(); j++) {
                    JSONObject json_question = json_questions.getJSONObject(j);
                    String name_question = json_question.getString("Question");
                    String id_question = json_question.getString("ID");
                    String isAnswered_question = json_question.getString("IsCompleteByUser");
                    col_questions = col_questions + 1;
                    if (isAnswered_question.equals("true")) {
                        col_answered = col_answered + 1;
                    }
                    db.add_question(Integer.valueOf(id_question), name_question, Integer.valueOf(id_group), isAnswered_question);

                    JSONArray json_answers = json_question.getJSONArray("Answers");
                    for (int k = 0; k < json_answers.length(); k++) {
                        JSONObject json_answer = json_answers.getJSONObject(k);
                        String name_answer = json_answer.getString("Text");
                        String id_answer = json_answer.getString("ID");
                        String isUserAnswer = json_answer.getString("IsUserAnswer");
                        db.add_answer(Integer.valueOf(id_answer), name_answer, Integer.valueOf(id_question), isUserAnswer);
                    }
                }
                if (col_questions == col_answered) {
                    isAnswered = "true";
                }
                db.add_group_questions(Integer.valueOf(id_group), name_group, isAnswered, col_questions, col_answered, isRead);
            }
        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
    }

    private String parseJsonPersonalAccounts(String line) {
        StringBuffer personalAccounts = new StringBuffer();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                personalAccounts.append(json_data.get(i));
                if (i != json_data.length()-1) {
                    personalAccounts.append(",");
                }
            }
        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
        return personalAccounts.toString();
    }

    // Заполним данные для ОСВ (начисления, оплаты по услугам)
    void add_data_Bills_Services() {
        // реальные данные для реального приложения
        String line = "";
        Boolean rezult = true;
        try {
            line = server.getBillsServices(mPhone, mPassword);
        } catch (Exception e) {
            rezult = false;
        }
        if (rezult) {
            db.del_table(DB.TABLE_SALDO);
            parse_json_bills(line);
        }
    }

    void parse_json_bills(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_bills = json.getJSONArray("data");
            for (int i = 0; i < json_bills.length(); i++) {
                // Запишем данные
                JSONObject json_bill = json_bills.getJSONObject(i);
                String ls = json_bill.getString("Ident");
                String bill_month = json_bill.getString("Month");
                String bill_year = json_bill.getString("Year");
                String bill_service = json_bill.getString("Service");
                String bill_acc = json_bill.getString("Accured");
                String bill_debt = json_bill.getString("Debt");
                String bill_pay = json_bill.getString("Payed");
                String bill_total = json_bill.getString("Total");
                String bill_id = json_bill.getString("ServiceTypeId");

                db.addSaldo(ls, bill_service, Integer.valueOf(bill_month), Integer.valueOf(bill_year), bill_debt, bill_acc, bill_pay, bill_total, bill_id);
            }
        } catch (Exception e) {
            Logger.errorLog(AddPersonalAccountActivity.this.getClass(), e.getMessage());
        }
    }

    private void getParametersFromPrefs() {
        mPhone = sPref.getString("login_push", "");
        mPassword = sPref.getString("pass_push", "");
        id_account = sPref.getString("id_account_push", "");
        isCons = sPref.getString("isCons_push", "");
        tokenFirebase = sPref.getString("token_firebase", "");
        hex = sPref.getString("hex_color", "23b6ed");
    }

    void setSettingPersonalAccountsForPref(String personalAccounts) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(PERSONAL_ACCOUNTS_PREF, personalAccounts);
        ed.commit();
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(AddPersonalAccountActivity.this, title);
        }
    }

    private void initViews() {
        layoutMain = findViewById(R.id.LinearLayout1);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        setTitle(R.string.title_add_personal_account);
    }
}