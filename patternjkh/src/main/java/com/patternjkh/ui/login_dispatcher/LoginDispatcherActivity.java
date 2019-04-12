package com.patternjkh.ui.login_dispatcher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.OnKeyboardVisibilityListener;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.parsers.AppFilesParser;
import com.patternjkh.parsers.AppTypesParser;
import com.patternjkh.parsers.AppsParser;
import com.patternjkh.parsers.DebtsParser;
import com.patternjkh.parsers.NewsParser;
import com.patternjkh.parsers.PollsParser;
import com.patternjkh.ui.main.MainActivity;
import com.patternjkh.ui.main.MainActivityCons;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.KeyboardUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.PhoneUtils;

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

import java.io.BufferedReader;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.patternjkh.utils.ToastUtils.showToast;

public class LoginDispatcherActivity extends AppCompatActivity implements OnKeyboardVisibilityListener {

    private static final String LOGIN_PREF = "login_pref";
    private static final String APP_SETTINGS = "global_settings";
    private final String PASS_PREF = "pass_pref";
    private final String MAIL_PREF = "mail_pref";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";

    private String id_account, token, isCons = "0", mail_reg = "", login, hex = "23b6ed";
    private boolean cancel, needToUpdate = true, isPasswordVisible;

    private TextView tvWriteToUs, tvVersion;
    private ImageView ivSupport, ivPasswordEye;
    private EditText etPassword;
    private View focusView;
    private AutoCompleteTextView etLogin;
    private Button btnEnter, btnCitizen;
    private ProgressDialog dialog;
    private AlertDialog.Builder errorDialog;

    private SharedPreferences sPref;
    private Handler handler;

    private Server server = new Server(this);
    private DB db = new DB(this);
    private AppStyleManager appStyleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_dispatcher);
        initViews();

        db.open();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        String loginFromPref = sPref.getString(LOGIN_PREF, "");
        String passFromPref = sPref.getString(PASS_PREF, "");
        String isFromCons = sPref.getString("is_entered_as_cons", "0");
        if (isFromCons != null && isFromCons.equals("1")) {
            if (loginFromPref != null && !loginFromPref.equals("")) {
                etLogin.setText(loginFromPref);
                etPassword.setText(passFromPref);
            }
        }

        token = ComponentsInitializer.firebaseToken;

        appStyleManager = AppStyleManager.getInstance(this, hex);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        KeyboardUtils.setKeyboardVisibilityListener(this, this);

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.email || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        ivPasswordEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(getDrawable(R.drawable.ic_password_eye));
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_password_eye));
                }
                etPassword.setSelection(etPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        btnCitizen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = getApplicationContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etLogin.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

                if (ConnectionUtils.hasConnection(LoginDispatcherActivity.this)) {
                    if (TextUtils.isEmpty(etLogin.getText().toString())) {
                        showToastHere("Необходимо заполнить " + getString(R.string.reg_phone));
                        etLogin.requestFocus();
                        return;
                    }

                    cancel = false;
                    attemptLogin();

                    dialog = new ProgressDialog(LoginDispatcherActivity.this);
                    dialog.setMessage("Синхронизация данных...");
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(false);
                    dialog.show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                        progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    Runnable progressRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing() && !isDestroyed()) {
                                if (dialog.isShowing()) {
                                    showErrorDialog();
                                }
                            }
                        }
                    };

                    Handler pdCanceller = new Handler();
                    pdCanceller.postDelayed(progressRunnable, 60000);

                    if (cancel) {
                        hideDialog();
                        focusView.requestFocus();
                        return;
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            String line = "xxx";
                            line = server.check_enter(etLogin.getText().toString(), etPassword.getText().toString());
                            if (line.equals("1")) {
                                handler.sendEmptyMessage(1);
                            } else if (line.equals("2")) {
                                handler.sendEmptyMessage(2);
                            } else if (line.equals("xxx") || line.toLowerCase().contains("ошибка") || line.contains("найти данный ресурс") || line.toLowerCase().contains("error")) {
                                handler.sendEmptyMessage(3);
                            } else {

                                // разберем приходящие данные для отображения редактирования счетчиков
                                // добавлено - последний реквизит - id аккаунта
                                String str1 = ""; // дата начала ввода показаний
                                String str2 = ""; // дата окончания ввода показаний
                                String str3 = ""; // возможность ввода показаий (0 или 1)
                                String str4 = ""; // id аккаунта
                                String str5 = ""; // является или нет консультантом
                                String str6 = ""; // наименование пользователя

                                // Добавлено поле для признака истории показаний по приборам
                                // 0 или 1 - нету или есть история
                                String str7 = "";

                                String address = null;

                                String[] str_answer = line.split(";");
                                try {
                                    str1 = str_answer[0];
                                    str2 = str_answer[1];
                                    str3 = str_answer[2];
                                    mail_reg = str_answer[3];
                                    str4 = str_answer[4];
                                    str5 = str_answer[5];
                                    isCons = str5;
                                    str6 = str_answer[6];
                                    str7 = str_answer[7];
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    Logger.errorLog(LoginDispatcherActivity.this.getClass(), e.getMessage());
                                }

                                sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
                                SharedPreferences.Editor ed = sPref.edit();
                                ed.putString("history_count", str7);
                                ed.putString("is_entered_as_cons", "1");

                                if (str_answer.length > 9) {
                                    address = str_answer[9];
                                } else {
                                    address = "";
                                }
                                if (str_answer.length > 10) {
                                    String phone = str_answer[10];
                                    ed.putString("phone", phone);
                                } else {
                                    ed.putString("phone", "");
                                }
                                ed.putString("_fio_", str6);
                                ed.putString("address_param", address);
                                ed.putString("to_extract_pays", str_answer[11]);

                                ed.commit();
                                id_account = str4;

                                String line_reg = server.regIdCons(str4, token);

                                filldata_id_cons();

                                // получаем привязанные лицевые счета по номеру телефона
                                String linePersonalAccounts = server.getAccountIdents(etLogin.getText().toString());
                                String personalAccounts = parseJsonPersonalAccounts(linePersonalAccounts);

                                // Получим типы заявок
                                String line_types = server.get_apps_type();
                                parse_types_apps(line_types);

                                // получаем уведомления
                                String lineNews = server.get_data_news(etLogin.getText().toString());
                                if (!lineNews.equals("xxx")) {
                                    NewsParser.parse_json_news(db, lineNews);
                                }

                                DebtsParser.getJsonDebts(server, sPref, personalAccounts);

                                // получаем опросы
                                Cursor cursor = db.getDataFromTable(DB.TABLE_GROUP_QUEST);
                                String lineQuestions = server.get_need_questions(String.valueOf(cursor.getCount()));
                                cursor.close();
                                if (lineQuestions.equals("1")) {
                                    lineQuestions = server.get_data_questions_answers(etLogin.getText().toString());
                                    if (!lineQuestions.equals("xxx")) {
                                        PollsParser.parse_json_questions_answers(db, lineQuestions);
                                    }
                                }

                                // получаем заявки
                                if (needToUpdate) {
                                    filldata();
                                }

                                set_settings_for_pref(etLogin.getText().toString(), etPassword.getText().toString(), mail_reg,personalAccounts);

                                Intent intent = new Intent(LoginDispatcherActivity.this, MainActivityCons.class);
                                startActivity(intent);
                                handler.sendEmptyMessage(4);
                                finish();
                                put_parametres_to_brain(etLogin.getText().toString(), etPassword.getText().toString(), mail_reg, id_account, token, str5, str1, str2, str3);
                            }
                        }
                    }).start();
                } else {
                    DialogCreator.showInternetErrorDialog(LoginDispatcherActivity.this, hex);
                }
            }
        });

        handler = new Handler() {
            public void handleMessage(Message message) {

                Context context = getApplicationContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etLogin.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

                if (message.what == 1) {
                    hideDialog();
                    showToastHere("Не переданы обязательные параметры");
                } else if (message.what == 2) {
                    hideDialog();
                    showToastHere("Неверный логин или пароль");
                    etLogin.setError(getString(R.string.error_incorrect_password));
                    createLoginDialog();
                } else if (message.what == 3) {
                    showErrorDialog();
                } else if (message.what == 4) {
                    hideDialog();
                } else if (message.what == 5) {
                    hideDialog();
                    Intent intent = new Intent(LoginDispatcherActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                    finish();
                }
            }
        };
    }

    private void createLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginDispatcherActivity.this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            builder.setMessage(Html.fromHtml("<b>" + "Для работы в приложении необходимо зарегистрироваться <br><br>" + "</b>" +
                    "Для регистрации в приложении необходимо указать № телефона и Ваше имя. Номер телефона укажите в формате +7-ххх-ххх-хххх <br>" +
                    "<br>" +
                    "После регистрации Вы сможете привязать Ваши лицевые счета. <br>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            builder.setMessage(Html.fromHtml("<b>" + "Для работы в приложении необходимо зарегистрироваться <br>" + "</b>" +
                    "Для регистрации в приложении необходимо указать № телефона и Ваше имя <br>" +
                    "<br>" +
                    "После регистрации Вы сможете привязать Ваши лицевые счета. <br>"));
        }

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
            }
        }
    }

    void parse_types_apps(String line_types) {
        String line = line_types.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?><Table Name=\"Support_RequestTypes\">", "<Types>");
        line = line.replace("</Table>", "</Types>");

        try {
            BufferedReader br = new BufferedReader(new StringReader(line));
            InputSource is = new InputSource(br);
            AppTypesParser xpp = new AppTypesParser(db);
            SAXParserFactory factory = SAXParserFactory.newInstance();

            SAXParser sp = factory.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(xpp);
            reader.parse(is);

        } catch (Exception e) {
            Logger.errorLog(LoginDispatcherActivity.this.getClass(), e.getMessage());
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
            Logger.errorLog(LoginDispatcherActivity.this.getClass(), e.getMessage());
        }
        return personalAccounts.toString();
    }

    public void attemptLogin() {
        // Сброс ошибок
        etLogin.setError(null);
        etPassword.setError(null);

        // Store values at the time of the login attempt.
        final String email = etLogin.getText().toString();
        final String password = etPassword.getText().toString();

        // Проверка пароля
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_invalid_password));
            focusView = etPassword;
            cancel = true;
        }

        // Проверка лицевого счета
        if (TextUtils.isEmpty(email)) {
            etLogin.setError(getString(R.string.error_field_required));
            focusView = etLogin;
            cancel = true;
        }
    }

    void filldata_id_cons() {
        if (!etLogin.getText().toString().equals(login)) {
            db.del_table(DB.TABLE_APPLICATIONS);
            db.del_table(DB.TABLE_COMMENTS);
        }

        int count = db.getDataFromTable(DB.TABLE_APPLICATIONS).getCount();
        if (count == 0) {
            filldata();

        } else {

            String line = "";
            boolean rezult = true;
            try {
                line = server.get_apps_id(id_account, token);
            } catch (Exception e) {
                rezult = false;
            }
            if (rezult) {
                parse_json(line);
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
                boolean rezult_add = add_app(id_app);
                // Удалить заявку по токену на сервере
                if (rezult_add) {
                    str_apps[i] = id_tel;
                    str_reqs[i] = id_app;
                }
            }
            String json_rezult = "[";
            for (int i = 0; i < json_data.length(); i++) {
                json_rezult = json_rezult + "{\"id_Account\":" + id_account + ",\"id_Request\":" + str_reqs[i] + ",\"id_Device\":\"" + token + "\",\"Token\":\"" + str_apps[i] + "\"}";
                if (i != (json_data.length() - 1)) {
                    json_rezult = json_rezult + ",";
                }
            }
            json_rezult = json_rezult + "]";
            // Отправим полученый json на сервер
            String answer = server.del_read_app(json_rezult);
        } catch (JSONException e) {
            Logger.errorLog(LoginDispatcherActivity.this.getClass(), e.getMessage());
        }

    }

    // Добавить (обновить заявку в БД) по id
    private boolean add_app(String id_app) {
        boolean rezult = true;
        try {
            String line = new Get_Comms_by_id().execute(id_app).get();

            if (!line.equals("xxx")) {
                try {
                    BufferedReader br = new BufferedReader(new StringReader(line));
                    InputSource is = new InputSource(br);
                    Parser_Get_Comm xpp = new Parser_Get_Comm(id_app);
                    SAXParserFactory factory = SAXParserFactory.newInstance();

                    SAXParser sp = factory.newSAXParser();
                    XMLReader reader = sp.getXMLReader();
                    reader.setContentHandler(xpp);
                    reader.parse(is);
                } catch (Exception e) {
                    e.printStackTrace();
                    rezult = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            rezult = false;
        }
        return rezult;
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
        if (keyboardVisible) {
            btnCitizen.setVisibility(View.GONE);
        } else {
            btnCitizen.setVisibility(View.VISIBLE);
        }
    }

    private class Get_Comms_by_id extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = ComponentsInitializer.SITE_ADRR + Server.COMM_BY_ID + "id=" + params[0];
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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public class Parser_Get_Comm extends DefaultHandler {

        int id_com = 0;
        int id_app = 0;
        String str_text = "";
        String str_date = "";
        String str_id_author = "";
        String str_author = "";

        String str_tema = "";
        String str_app_date = "";
        String id_acc = "";

        String id;

        // Добавлено - поля Адрес и Квартира
        String adress, flat;

        // Добавлено - поле Телефон
        String phone;

        Parser_Get_Comm(String _id) {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

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
                if (atts.getValue("id_MobileAccount").equals("")) {
                    id_acc = "0";
                } else {
                    id_acc = atts.getValue("id_MobileAccount");
                }
                String isHidden = atts.getValue("isHidden");

                str_tema = atts.getValue("ReqName");
                str_app_date = atts.getValue("ReqAdded");

                adress = atts.getValue("HouseAddress");
                flat = atts.getValue("FlatNumber");

                phone = atts.getValue("PhoneNum");

                if (db.getColApp(atts.getValue("id_request"))) {
                    // TODO - обратить внимание, здесь заявка создается из комментария - нету данных об адресе и номере квартиры
                    db.addApp(String.valueOf(id_app), str_text, "", 0, 0, 0, str_author, str_id_author, str_tema, str_app_date, adress, flat, phone, "", 0);
                    // Подтянем файлы - фотографии по заявке
                    getFiles_About_App(String.valueOf(id_app));
                }

                // Запишем новый комментарий в БД
                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, id_acc, isHidden);

            }
        }
    }

    // Подтянем файлы - фотографии для заявки
    private void getFiles_About_App(String str_id) {
        String line = server.get_foto_by_app(str_id, etLogin.getText().toString());
        AppFilesParser.parse_json_files(db, line);
    }

    // Получим данные по заявкам
    private void filldata() {
        // Проверим есть ли новые заявки
        Check_new_comm();
        needToUpdate = false;
    }

    private void Check_new_comm() {

        String line = "xxx";

        // получим ВСЕ заявки
        try {
            line = server.get_apps(etLogin.getText().toString(), etPassword.getText().toString(), "All", isCons);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            db.del_table(DB.TABLE_APPLICATIONS);
            db.del_table(DB.TABLE_COMMENTS);

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                AppsParser xpp = new AppsParser(db, etLogin.getText().toString());
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SAXParser sp = factory.newSAXParser();
                XMLReader reader = sp.getXMLReader();
                reader.setContentHandler(xpp);
                reader.parse(is);

            } catch (Exception e) {
                Logger.errorLog(LoginDispatcherActivity.this.getClass(), e.getMessage());
            }

        } catch (Exception e) {
            Logger.errorLog(LoginDispatcherActivity.this.getClass(), e.getMessage());
        }

    }

    private void showErrorDialog() {
        hideDialog();
        LayoutInflater layoutinflater = LayoutInflater.from(LoginDispatcherActivity.this);
        View view = layoutinflater.inflate(R.layout.dialog_custom_error_login_dispatcher, null);
        errorDialog = new AlertDialog.Builder(LoginDispatcherActivity.this);
        errorDialog.setView(view);
        errorDialog.setTitle("Сервер временно не отвечает");
        Button btnOk = view.findViewById(R.id.btn_error_dialog_ok);
        Button btnTech = view.findViewById(R.id.btn_error_dialog_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnOk.setTextColor(Color.parseColor("#" + hex));
            btnTech.setTextColor(Color.parseColor("#" + hex));
        }
        final AlertDialog dialog = errorDialog.create();
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }
            }
        });
        btnTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginDispatcherActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_left_activity_in, R.anim.move_rigth_activity_out);
            }
        });
    }

    private void set_settings_for_pref(String login, String pass, String mail_reg_str, String personalAccounts) {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(LOGIN_PREF, login);
        ed.putString(PASS_PREF, pass);
        ed.putString(MAIL_PREF, mail_reg_str);
        ed.putString(PERSONAL_ACCOUNTS_PREF, personalAccounts);
        ed.commit();
    }

    private void put_parametres_to_brain(String login, String pass, String mail, String id_account, String token, String isCons, String startDayCount, String endDayCount, String canCount) {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("login_push", login);
        ed.putString("pass_push", pass);
        ed.putString("mail_pref", mail);
        ed.putString("id_account_push", id_account);
        ed.putString("isCons_push", isCons);
        ed.putString("start_day", startDayCount);
        ed.putString("end_day", endDayCount);
        ed.putString("can_count", canCount);
        ed.putString("token_firebase", token);
        ed.commit();
    }

    private void hideDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(LoginDispatcherActivity.this, title);
        }
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        ivSupport.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_help));
        etLogin.setCompoundDrawablesWithIntrinsicBounds(appStyleManager.changeDrawableColor(R.drawable.ic_person_cons_login), null, null, null);
        etPassword.setCompoundDrawablesWithIntrinsicBounds(appStyleManager.changeDrawableColor(R.drawable.ic_lock), null, null, null);
        btnEnter.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        etLogin.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        etPassword.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        tvVersion.setTextColor(Color.parseColor("#" + hex));
        tvWriteToUs.setTextColor(Color.parseColor("#" + hex));
        btnCitizen.setTextColor(Color.parseColor("#" + hex));
        setBorderToButton(btnCitizen);
    }

    private void setBorderToButton(View view) {
        StateListDrawable drawable = (StateListDrawable) view.getBackground();
        DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
        if (dcs != null) {
            Drawable[] drawableItems = dcs.getChildren();
            GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0];
            gradientDrawableChecked.setStroke(2, Color.parseColor("#" + hex));
        }
    }

    private void initViews() {
        ivSupport = findViewById(R.id.image_support);
        etPassword = findViewById(R.id.password);
        etLogin = findViewById(R.id.email);
        tvWriteToUs = findViewById(R.id.label_write_to_us);
        btnEnter = findViewById(R.id.email_sign_in_button);
        tvVersion = findViewById(R.id.tv_app_version);
        tvVersion.setText("ver. " + getString(R.string.appVersion));
        ivPasswordEye = findViewById(R.id.iv_password_eye);
        btnCitizen = findViewById(R.id.btn_login_citizen_enter);
    }
}
