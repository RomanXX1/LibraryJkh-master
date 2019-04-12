package com.patternjkh.ui.others;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.AppStyleManager;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.registration.RegistrationNewActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.PhoneMaskWatcher;

import static com.patternjkh.utils.ToastUtils.showToast;

public class PasswordRecoveryActivity extends AppCompatActivity {

    private String hex;

    private Button btnOk, btnCancel, btnNoInternetRefresh;
    private LinearLayout layoutNoInternet;
    private ConstraintLayout layoutMain;
    private TextView tvSupport;
    private EditText etLogin;
    private ImageView ivSupport;
    private ProgressDialog dialog;

    private AppStyleManager appStyleManager;
    private Server server = new Server(this);
    private Handler fog_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);
        initViews();

        SharedPreferences sPref = getSharedPreferences("global_settings", Context.MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        fog_handler = new Handler() {
            public void handleMessage(Message message) {
                Context context = getApplicationContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etLogin.getWindowToken(), 0);
                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }

                if (message.what == 1) {
                    showToastHere("Не указан номер телефона");
                    etLogin.setError("Не указан номер телефона");
                } else if (message.what == 2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PasswordRecoveryActivity.this);
                    builder.setTitle("Аккаунт не обнаружен");
                    builder.setMessage("Пожалуйста, пройдите процедуру регистрации");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(PasswordRecoveryActivity.this, RegistrationNewActivity.class);
                            intent.putExtra("from_cold_launch", false);
                            startActivity(intent);
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
                } else if (message.what == 11) {
                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }

                    DialogCreator.showErrorCustomDialog(PasswordRecoveryActivity.this, error, hex);
                } else if (message.what == 6) {
                    showToast(PasswordRecoveryActivity.this, "Пароль выслан на указанный при регистрации e-mail");
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                } else if (message.what == 20) {
                    showToast(PasswordRecoveryActivity.this, "Пароль выслан на указанный номер телефона");
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                }
            }
        };

        initListeners();
        if (ConnectionUtils.hasConnection(PasswordRecoveryActivity.this)) {
            hideNoInternet();
        } else {
            showNoInternet();
        }
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(PasswordRecoveryActivity.this)) {
                    hideNoInternet();
                } else {
                    showNoInternet();
                }
            }
        });

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void hideNoInternet() {
        layoutNoInternet.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
    }

    private void initListeners() {

        etLogin.addTextChangedListener(new PhoneMaskWatcher("##-###-###-####"));

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(PasswordRecoveryActivity.this)) {
                    if (!check_fog()) {
                        // запустим восстановление пароля
                        etLogin.setError(null);

                        dialog = new ProgressDialog(PasswordRecoveryActivity.this);
                        dialog.setMessage("Обработка запроса...");
                        dialog.setIndeterminate(true);
                        dialog.setCancelable(false);
                        if (!isFinishing() && !isDestroyed()) {
                            dialog.show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                            }
                        }

                        if (!etLogin.getText().toString().startsWith("79") && !etLogin.getText().toString().startsWith("89")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String line = server.forget_pass(etLogin.getText().toString());
                                    if (line.equals("1")) {
                                        fog_handler.sendEmptyMessage(1);
                                    } else if (line.equals("2")) {
                                        fog_handler.sendEmptyMessage(2);
                                    }  else if (line.contains("@")) {
                                        // Восстановление пароля прошло успешно
                                        fog_handler.sendEmptyMessage(6);
                                    } else if (line.startsWith("79")) {
                                        fog_handler.sendEmptyMessage(20);
                                    } else {
                                        Message msg = fog_handler.obtainMessage(11, 0, 0, line);
                                        fog_handler.sendMessage(msg);
                                    }
                                }
                            }).start();
                        } else {
                            showToastHere("Номер телефона необходимо ввести в формате: +7-ХХХ-ХХХ-ХХХХ");
                            if (!isFinishing() && !isDestroyed()) {
                                dialog.dismiss();
                            }
                        }
                    }
                } else {
                    DialogCreator.showInternetErrorDialog(PasswordRecoveryActivity.this, hex);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etLogin.getWindowToken(), 0);
                }
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });

        tvSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordRecoveryActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    // проверка заполнения при восстановлении пароля
    private boolean check_fog() {
        boolean isError = false;

        etLogin.setError(null);

        if (TextUtils.isEmpty(etLogin.getText().toString())) {
            etLogin.setError("Не указан лиц. счет");
            isError = true;
        }

        return isError;
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(PasswordRecoveryActivity.this, title);
        }
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        appStyleManager = AppStyleManager.getInstance(this, hex);
        etLogin.setCompoundDrawablesWithIntrinsicBounds(appStyleManager.changeDrawableColor(R.drawable.ic_login_account), null, null, null);
        btnCancel.setTextColor(Color.parseColor("#" + hex));
        etLogin.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        etLogin.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        btnOk.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        tvSupport.setTextColor(Color.parseColor("#" + hex));
        ivSupport.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_help));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews() {
        btnOk = findViewById(R.id.btn_fog);
        btnCancel = findViewById(R.id.btn_cancel_fog);
        tvSupport = findViewById(R.id.label_write_to_us);
        ivSupport = findViewById(R.id.image_support);
        etLogin = findViewById(R.id.fog_login);
        layoutMain = findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        setToolbar();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Восстановление пароля");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etLogin.getWindowToken(), 0);
                }
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }
}
