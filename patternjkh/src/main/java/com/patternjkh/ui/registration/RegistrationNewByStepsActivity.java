package com.patternjkh.ui.registration;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.main.MainActivity;
import com.patternjkh.ui.others.AddPersonalAccountFragment;
import com.patternjkh.ui.others.OnAddPersonalAccountFragmentInteractionListener;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
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

public class RegistrationNewByStepsActivity extends AppCompatActivity
        implements PhoneConfirmationFragment.OnPhoneConfirmationFragmentInteractionListener,
        NewPasswordFragment.OnNewPasswordFragmentInteractionListener,
        OnAddPersonalAccountFragmentInteractionListener {

    private static final String PHONE = "PHONE";
    private static final String PASSWORD = "PASSWORD";
    private static final String APP_SETTINGS = "global_settings";
    private SharedPreferences sPref;
    private static final String LOGIN_PREF = "login_pref";
    private static final String PASS_PREF = "pass_pref";
    private static final String MAIL_PREF = "mail_pref";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";

    private String mail_reg = "", isCons = "0", id_account, token, mPhone, mPassword, hex;

    private ProgressDialog dialog;
    private AlertDialog dialogChange;

    private PhoneConfirmationFragment mPhoneConfirmationFragment;
    private NewPasswordFragment mNewPasswordFragment;
    private Server server = new Server(this);
    private Handler handler, handlerChangePass;
    private DB db = new DB(this);
    private FragmentManager mFragmentManager;

    public static Intent newIntent(Context context, String phone) {
        Intent intent = new Intent(context, RegistrationNewByStepsActivity.class);
        intent.putExtra("phone", phone);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_new_by_steps);

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        mPhone = getIntent().getStringExtra("phone");

        initToolbar();

        mFragmentManager = getSupportFragmentManager();

        setTitle(R.string.title_phone_confirmation);

        mPhoneConfirmationFragment = PhoneConfirmationFragment.newInstance(mPhone);

        replaceFragment(mPhoneConfirmationFragment);

        // Перестраховка если вдруг в процессе регистрации произойдет сбой и телефон и/или пароль потеряются
        if (savedInstanceState != null){
            if (TextUtils.isEmpty(mPhone)){
                mPhone = savedInstanceState.getString(PHONE);
            }
            if (TextUtils.isEmpty(mPassword)){
                mPassword = savedInstanceState.getString(PASSWORD);
            }
        }

    }

    // Перестраховка если вдруг в процессе регистрации произойдет сбой и телефон и/или пароль потеряются
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PHONE, mPhone);
        outState.putString(PASSWORD, mPassword);
        super.onSaveInstanceState(outState);
    }

    // Перестраховка если вдруг в процессе регистрации произойдет сбой и телефон и/или пароль потеряются
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPhone = savedInstanceState.getString(PHONE);
        mPassword = savedInstanceState.getString(PASSWORD);
    }

    @Override
    public void onPhoneConfirmationFragmentInteraction() {

        mNewPasswordFragment = NewPasswordFragment.newInstance(mPhone);

        setTitle(R.string.title_new_password);

        replaceFragment(mNewPasswordFragment);
    }

    @Override
    public void onNewPasswordFragmentInteraction(String password) {
        setSettingPasswordForPref(password);

        mPassword = password;

        db.open();

        attemptLoginAndPassword();

        if (!ConnectionUtils.hasConnection(this)) {
            // сообщение об ошибке
            showToastHere(getString(R.string.not_connection_no_continue));
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

        handler = new Handler() {
            public void handleMessage(Message message) {

                hideDialog();

                if (message.what == 1) {
                    showToastHere("Не переданы обязательные параметры");
                } else if (message.what == 2) {
                    showToastHere("Неверный логин или пароль");
                } else if (message.what == 3) {

                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }

                    DialogCreator.showErrorCustomDialog(RegistrationNewByStepsActivity.this, error, hex);
                } else if (message.what == 101) {
                    if (message.obj != null) {
                        String errorMessage = String.valueOf(message.obj);
                        errorMessage = errorMessage.replaceFirst("error: смена пароля: ", "");
                        String[] accsArray = errorMessage.split(";");

                        hideDialog();
                        showChangedPassDialog(accsArray);
                    }
                }

            }
        };

        handlerChangePass = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 101) {
                    if (!isFinishing() && !isDestroyed()) {
                        if (dialogChange != null) {
                            dialogChange.dismiss();
                        }
                    }
                    if (msg.obj != null) {
                        String line = String.valueOf(msg.obj);
                        if (line.equals("ok")) {
                            showToastHere("Успешно изменен пароль");
                            dialog.dismiss();
                        } else {
                            line = line.replaceFirst("error: ", "");
                            showToastHere(line);
                        }
                    }
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.authenticateAccount(mPhone, mPassword);

                if (line.equals("1")) {
                    handler.sendEmptyMessage(1);
                } else if (line.equals("2")) {
                    handler.sendEmptyMessage(2);
                } else if (line.contains("error: смена пароля:")) {
                    Message msg = handler.obtainMessage(101, 0, 0, line);
                    handler.sendMessage(msg);
                } else if (line.equals("xxx") || line.toLowerCase().contains("ошибка") || line.contains("найти данный ресурс") || line.toLowerCase().contains("error")) {
                    Message msg = handler.obtainMessage(3, 0, 0, line);
                    handler.sendMessage(msg);
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
                    str1 = str_answer[0];
                    str2 = str_answer[1];
                    str3 = str_answer[2];
                    mail_reg = str_answer[3];
                    str4 = str_answer[4];
                    str5 = str_answer[5];
                    isCons = str5;
                    str6 = str_answer[6];

                    str7 = str_answer[7];
                    sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("history_count", str7);
                    ed.commit();

                    if (str_answer.length > 9) {
                        address = str_answer[9];
                    }

                    id_account = str4;

                    token = ComponentsInitializer.firebaseToken;
                    String line_reg;
                    if (isCons.equals("1")) {
                        line_reg = server.regIdCons(str4, token);
                    } else {
                        line_reg = server.regId(str4, token);
                    }

                    if (str5.equals("0")) { // Пользователь НЕ консультант
                        // Получим данные по счетчикам
                        filldata_counters();

                        if (line_reg.equals("1")) {
                            // Получим данные, которые подготовлены для этого пользователя
                            filldata_id();
                        } else {
                            // Получим данные по ВСЕМ заявкам
                            filldata();
                        }

                    } else {                // Пользователь консультант
                        filldata_id_cons();
                    }

                    // получаем уведомления
                    String lineNews = server.get_data_news(mPhone);
                    if (!lineNews.equals("xxx")) {
                        parse_json_news(lineNews);
                    }

                    // получаем опросы
                    Cursor cursor = db.getDataFromTable(db.TABLE_GROUP_QUEST);
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

                    set_settings_for_pref(mPhone, mPassword, true, mail_reg, personalAccounts, str1, str2, str3);

                    if (str5.equals("0")) {
                        // TODO - добавим в базу данные ОСВ (потом удалить)
                        add_data_Bills_Services();
                    }

//                    Intent intent = null;
//                    try {
//                        String name = ComponentsInitializer.mainActivity.getName() + ".MainActivity";
//                        intent = new Intent(RegistrationNewByStepsActivity.this, Class.forName(name));
//                        startActivity(intent);
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    Intent intent = new Intent(RegistrationNewByStepsActivity.this, MainActivity.class);
                    startActivity(intent);
                    hideDialog();

                    put_parametres_to_brain(mPhone, mPassword, mail_reg, id_account, token, str5);
                    finish();
                }
            }
        }).start();
    }

    private void showChangedPassDialog(final String[] accsArr) {
        LayoutInflater layoutinflater = LayoutInflater.from(RegistrationNewByStepsActivity.this);
        View view = layoutinflater.inflate(R.layout.dialog_custom_ls_changed, null);
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(RegistrationNewByStepsActivity.this);
        errorDialog.setView(view);
        errorDialog.setTitle("Ошибка входа");

        // Изначальные компоненты
        Button btnCancel = view.findViewById(R.id.btn_changed_ls_dialog_cancel);
        Button btnToChangePass = view.findViewById(R.id.btn_changed_ls_dialog_change_pass);
        Button btnDeleteLs = view.findViewById(R.id.btn_changed_ls_dialog_delete_ls);
        TextView tvAccs = view.findViewById(R.id.tv_changed_ls_dialog_accs);
        final LinearLayout layoutButtons = view.findViewById(R.id.layout_changed_ls_dialog_buttons);

        // Компоненты по нажатию "Сменить пароль"
        final LinearLayout layoutChangePass = view.findViewById(R.id.layout_dialog_change_pass);
        Button btnChangePassBack = view.findViewById(R.id.btn_changed_ls_dialog_change_back);
        final Button btnChangePassOk = view.findViewById(R.id.btn_changed_ls_dialog_change_ok);
        final EditText etNewPass = view.findViewById(R.id.et_dialog_change_pass);
        final ProgressBar progressBar = view.findViewById(R.id.pb_changed_ls_dialog_change_ok);

        tvAccs.setText("Для лицевого счета " + accsArr[0] + " был изменен пароль на сайте");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            btnCancel.setTextColor(Color.parseColor("#" + hex));
            btnToChangePass.setTextColor(Color.parseColor("#" + hex));
            btnDeleteLs.setTextColor(Color.parseColor("#" + hex));
            btnChangePassBack.setTextColor(Color.parseColor("#" + hex));
            btnChangePassOk.setTextColor(Color.parseColor("#" + hex));
            etNewPass.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        }

        dialogChange = errorDialog.create();
        if (!isFinishing() && !isDestroyed()) {
            dialogChange.show();
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogChange.dismiss();
                }
            });

            btnToChangePass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layoutButtons.setVisibility(View.GONE);
                    layoutChangePass.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            });
            btnDeleteLs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteLsDialog(accsArr[0]);
                }
            });
            btnChangePassBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layoutButtons.setVisibility(View.VISIBLE);
                    layoutChangePass.setVisibility(View.GONE);
                }
            });
            btnChangePassOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!etNewPass.getText().toString().equals("")) {
                        btnChangePassOk.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        Context context = getApplicationContext();
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etNewPass.getWindowToken(), 0);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String line = server.changeLsPassMytishi(mPhone, etNewPass.getText().toString(), accsArr[0]);
                                Message msg = handlerChangePass.obtainMessage(101, 0, 0, line);
                                handlerChangePass.sendMessage(msg);
                            }
                        }).start();
                    }
                }
            });
        }
    }

    private void showDeleteLsDialog(final String ls) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Отвязать лицевой счет " + ls + " от аккаунта?");
        builder.setPositiveButton("Да",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String line = server.deleteAccount(mPhone, ls);
                        if (line.equals("ok")) {
                            showToastHere("Лицевой счет " + ls + " отвязан от аккаунта");
                        } else {
                            showToastHere("Ошибка удаления лс");
                        }

                        if (!isFinishing() && !isDestroyed()) {
                            if (dialogChange != null) {
                                dialogChange.dismiss();
                            }
                        }
                    }
                });

        builder.setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#" + hex));
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
            }
        }
    }

    private void hideDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

    // Настройки в хранилище сохранение пароля
    // Перестраховка 2 если вдруг в процессе регистрации произойдет сбой и пароль потеряется
    void setSettingPasswordForPref(String pass) {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(PASS_PREF, pass);
        ed.commit();
    }

    // Настройки из хранилища извлекаем пароля
    // Перестраховка 2 если вдруг в процессе регистрации произойдет сбой и пароль потеряется
    void getSettingPasswordFromPref() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        mPassword = sPref.getString(PASS_PREF, "");
    }

    @Override
    public void onAddPersonalAccountFragmentInteraction() {
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegistrationNewByStepsActivity.this, RegistrationNewActivity.class);
        intent.putExtra("from_cold_launch", false);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in); // Bottom Fragment Animation
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }

    public void attemptLoginAndPassword() {

        // Проверка номера телефона
        if (TextUtils.isEmpty(mPhone)) {
            // Не должно сюда дойти т.к. onSaveInstanceState
            showToastHere(getString(R.string.error_occurred_during_registration_process_Try_to_register_again));
            finish();
        }

        // Проверка пароля
        if (TextUtils.isEmpty(mPassword)) {
            // Не должно сюда дойти т.к. onSaveInstanceState
            getSettingPasswordFromPref();
            if (TextUtils.isEmpty(mPassword)) {
                showToastHere(getString(R.string.error_occurred_during_registration_process_Try_to_register_again));
                finish();
            }
        }
    }

    // Получим данные по показаниям счетчиков
    void filldata_counters() {
        String line = "xxx";

        // Получить ВСЕ показания приборов
        try {
            line = server.getCountersMytishi(mPhone, mPassword);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            db.del_table(db.TABLE_COUNTERS_MYTISHI);

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
            }

        } catch (Exception e) {
        }
    }

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

        db.del_table(db.TABLE_APPLICATIONS);
        db.del_table(db.TABLE_COMMENTS);

        if (db.getDataFromTable(db.TABLE_APPLICATIONS).getCount() == 0) {
            filldata();
        } else {

            String line = "";
            Boolean rezult = true;
            try {
                line = server.get_apps_id(mPhone, token);
            } catch (Exception e) {
                rezult = false;
            }
            if (rezult) {
                parse_json(line);
            }

        }
    }

    // Получим данные по заявкам
    void filldata() {
        // Проверим есть ли новые заявки
        Check_new_comm();
    }

    void Check_new_comm() {

        String line = "xxx";

        // получим ВСЕ заявки
        try {
            line = server.get_apps(mPhone, mPassword, "All", isCons);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            db.del_table(db.TABLE_APPLICATIONS);
            db.del_table(db.TABLE_COMMENTS);

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
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class Parser_Get_Apps extends DefaultHandler { // Получение заявок

        String str_id = "";
        String str_name = "";
        String str_owner = "";
        String str_closeApp = "";
        String str_isRead;
        String str_isReadCons;
        String str_isAnswered;
        String str_client = "";
        String str_customer_id = "";

        String str_tema = "";
        String str_app_date = "";

        int id_com = 0;
        int id_app = 0;
        String str_text = "";
        String str_date = "";
        String str_id_author = "";
        String str_author = "";
        String str_id_account = "";
        String str_adress = "";
        String str_flat = "";
        String str_phone = "";

        Parser_Get_Apps() {

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (localName.toLowerCase().equals("row")) {
                str_name = atts.getValue("text").toString();
                try {
                    str_id = atts.getValue("id").toString();
                } catch (Exception e) {
                    str_id = atts.getValue("ID").toString();
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
                    str_client = atts.getValue("CusName").toString();
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

                str_tema = atts.getValue("name").toString();
                str_app_date = atts.getValue("added").toString();
                if (str_app_date.length() > 10) {
                    str_app_date = str_app_date.substring(0, 10);
                }

                str_adress = atts.getValue("HouseAddress").toString();
                str_flat = atts.getValue("FlatNumber").toString();

                str_phone = atts.getValue("PhoneNum").toString();

                String type_app = atts.getValue("id_type").toString();

                db.addApp(str_id, str_name, str_owner, closeApp, isRead, isAnswered, str_client, str_customer_id, str_tema, str_app_date, str_adress, str_flat, str_phone, type_app, isReadCons);

            } else if (localName.toLowerCase().equals("comm")) {
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
                str_id_account = atts.getValue("id_account");
                String isHidden = atts.getValue("isHidden");

                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, id_account, isHidden);

            } else if (localName.toLowerCase().equals("file")) {

                String fileID = atts.getValue("FileID");
                String number = atts.getValue("RequestID");
                String file_name = atts.getValue("FileName");
                String date_time = atts.getValue("DateTime");

                // скачаем иконку приложения
                byte[] data = new byte[1024];
                byte[] bArray = null;
                File file = null;
                try {

                    URL url = new URL(ComponentsInitializer.SITE_ADRR + server.DOWNLOAD_FilE + "id=" + fileID + "&tmode=1");
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
                    e.printStackTrace();
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
                json_rezult = json_rezult + "{\"id_Account\":" + id_account + ",\"id_Request\":" + str_reqs[i] + ",\"id_Device\":\"" + token + "\",\"Token\":\"" + str_apps[i] + "\"}";
                if (i != (json_data.length() - 1)) {
                    json_rezult = json_rezult + ",";
                }
            }
            json_rezult = json_rezult + "]";
            // Отправим полученый json на сервер
            String answer = server.del_read_app(json_rezult);
        } catch (JSONException e) {
            e.printStackTrace();
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

        int id_com = 0;
        int id_app = 0;
        String str_text = "";
        String str_date = "";
        String str_id_author = "";
        String str_author = "";

        String str_tema = "";
        String str_app_date = "";

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
                File file = null;
                try {

                    URL url = new URL(ComponentsInitializer.SITE_ADRR + server.DOWNLOAD_FilE + "id=" + file_id + "&tmode=1");
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
                    e.printStackTrace();
                }

                db.addFotoWithDate(file_id, file_number, bArray, "", file_name, file_date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Получим данные для консультанта
    void filldata_id_cons() {
        db.del_table(db.TABLE_APPLICATIONS);
        db.del_table(db.TABLE_COMMENTS);
        filldata();
    }

    // Настройки в хранилище
    void set_settings_for_pref(String login, String pass, boolean chk, String mail_reg_str, String personalAccounts, String startDayCount, String endDayCount, String canCount) {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(LOGIN_PREF, login);
        ed.putString(PASS_PREF, pass);
        ed.putString(MAIL_PREF, mail_reg_str);
        ed.putString(PERSONAL_ACCOUNTS_PREF, personalAccounts);
        ed.putString("start_day", startDayCount);
        ed.putString("end_day", endDayCount);
        ed.putString("can_count", canCount);
        ed.commit();
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
        } catch (Exception e) {}
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
            db.del_table(db.TABLE_SALDO);
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
            e.printStackTrace();
        }
    }

    void put_parametres_to_brain(String login, String pass, String mail, String id_account, String token, String isCons) {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("login_push", login);
        ed.putString("pass_push", pass);
        ed.putString("mail_pref", mail);
        ed.putString("id_account_push", id_account);
        ed.putString("isCons_push", isCons);
        ed.commit();
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(RegistrationNewByStepsActivity.this, title);
        }
    }

    private void initToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationNewByStepsActivity.this, RegistrationNewActivity.class);
                intent.putExtra("from_cold_launch", false);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }
}
