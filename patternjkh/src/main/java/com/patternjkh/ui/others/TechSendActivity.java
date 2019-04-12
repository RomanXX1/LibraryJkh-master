package com.patternjkh.ui.others;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.PhoneMaskWatcher;
import com.patternjkh.utils.PhoneUtils;
import com.patternjkh.utils.StringUtils;

import static com.patternjkh.utils.ToastUtils.showToast;

public class TechSendActivity extends AppCompatActivity {

    private static final String GENERAL_ERROR = "Ошибка. Проверьте стабильность соединения Интернет";
    private static final String APP_SETTINGS = "global_settings";

    private String hex = "23b6ed", personalAccs, errorStr = "", inputtedLogin;

    private Button btn_send, btnNoInternetRefresh;
    private LinearLayout layoutNoInternet, layoutMain;
    private EditText edLogin, edMail, edText;
    private TextView tvVersion;
    private ProgressDialog dialog;

    private Server server = new Server(this);
    private Handler tech_handler;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_send);
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        personalAccs = sPref.getString("personalAccounts_pref", "");
        initComponents();
        setToolbar();

        if (getIntent() != null) {
            errorStr = getIntent().getStringExtra("error_str");
        }

        tvVersion.setText("ver. " + getString(R.string.appVersion));

        if (ConnectionUtils.hasConnection(this)) {
            hideNoInternet();
        } else {
            showNoInternet();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }
        initListeners();

        tech_handler = new Handler() {
            public void handleMessage(Message message) {
                Context context = getApplicationContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edLogin.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(edMail.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(edText.getWindowToken(), 0);

                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }

                if (message.what == 1) {
                    showToastHere("Не переданы все параметры");
                } else if (message.what == 2) {
                    showToastHere("Неверно указан лицевой счет");
                } else if (message.what == 3) {
                    edText.setError(null);

                    showSuccessDialog();

                } else if (message.what == 4) {
                    showToastHere(GENERAL_ERROR);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TechSendActivity.this);
        builder.setTitle("Ваше сообщение отправлено");
        builder.setMessage("Специалист свяжется с Вами в ближайшее время");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
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

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(TechSendActivity.this)) {
                    hideNoInternet();
                } else {
                    showNoInternet();
                }
            }
        });
    }

    private void hideNoInternet() {
        layoutNoInternet.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
    }

    void initListeners() {

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(TechSendActivity.this)) {
                    sendMessageToTechSupport();
                } else {
                    DialogCreator.showInternetErrorDialog(TechSendActivity.this, hex);
                }
            }
        });

        edLogin.addTextChangedListener(new PhoneMaskWatcher("##-###-###-####"));
    }

    private void sendMessageToTechSupport() {

        inputtedLogin = edLogin.getText().toString().replaceAll("-", "");

        boolean returns = false;
        if (TextUtils.isEmpty(inputtedLogin)) {
            edLogin.setError("Не указан номер телефона");
            returns = true;
        } else {
            if (inputtedLogin.startsWith("+7") || inputtedLogin.startsWith("8")) {
                if (!PhoneUtils.checkValidPhone(inputtedLogin)) {
                    edLogin.setError("Номер телефона необходимо ввести в формате: +7-ХХХ-ХХХ-ХХХХ");
                    returns = true;
                }
            }
        }
        if (TextUtils.isEmpty(edMail.getText().toString())) {
            edMail.setError("Не указан электронный адрес для ответа");
            returns = true;
        }
        if (!StringUtils.checkIsEmailValid(edMail.getText().toString())) {
            edMail.setError("Неверный электронный адрес для ответа");
            returns = true;
        }
        if (TextUtils.isEmpty(edText.getText().toString())) {
            edText.setError("Укажите текст обращения");
            returns = true;
        }
        if (!returns) {

            edLogin.setError(null);
            edMail.setError(null);
            edText.setError(null);

            // Отправка письма с обращением через скрипт
            dialog = new ProgressDialog(TechSendActivity.this);
            dialog.setMessage("Отправка обращения...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String errorText = edText.getText().toString();
                    if (errorStr != null) {
                        errorText += "; Дополнительно: " + errorStr;
                    }
                    String line = server.send_mail(inputtedLogin, edMail.getText().toString(), errorText, personalAccs);
                    if (line.equals("1")) {
                        tech_handler.sendEmptyMessage(1);
                    } else if (line.equals("2")) {
                        tech_handler.sendEmptyMessage(2);
                    } else if (line.contains("0")) {
                        // Отправка оповещения прошла успешно
                        tech_handler.sendEmptyMessage(3);
                    } else {
                        // Неизвестная ошибка
                        tech_handler.sendEmptyMessage(4);
                    }
                }
            }).start();
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Обращение в тех.поддержку");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(edLogin.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(edMail.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(edText.getWindowToken(), 0);
                }
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(TechSendActivity.this, title);
        }
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        hex = sPref.getString("hex_color", "23b6ed");
        btn_send.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        edLogin.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        edMail.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        edText.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initComponents() {
        edLogin = findViewById(R.id.edLogin);
        String phoneFromPref = sPref.getString("login_pref", "");
        if (phoneFromPref != null) {
            if (PhoneUtils.checkValidPhone(phoneFromPref.replaceAll("-", ""))) {
                edLogin.setText(PhoneUtils.formatPhoneToRightFormat(phoneFromPref));
            } else {
                edLogin.setText("+7");
            }
        } else {
            edLogin.setText("+7");
        }

        edMail = findViewById(R.id.edMail);
        edMail.setText(sPref.getString("mail_pref", ""));
        edText = findViewById(R.id.edText);
        btn_send = findViewById(R.id.btn_send);
        layoutMain = findViewById(R.id.LinearLayout);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        tvVersion = findViewById(R.id.tv_app_version);
    }
}
