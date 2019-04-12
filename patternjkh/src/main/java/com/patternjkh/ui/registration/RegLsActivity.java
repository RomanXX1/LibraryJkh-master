package com.patternjkh.ui.registration;

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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.patternjkh.utils.ToastUtils.showToast;

public class RegLsActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";

    private String hex, ls, phone, pass, email, fio, billskey, linePersonalAccounts = "xxx", errorStr;
    private boolean isPasswordVisible;

    private EditText etLs, etPass, etEmail, etBillskey;
    private Button btnReg, btnCancel, btnNoInternetRefresh;
    private LinearLayout layoutNoInternet;
    private ConstraintLayout layoutMain;
    private ImageView ivPasswordEye;

    private SharedPreferences sPref;
    private Server server = new Server(this);
    private Handler handler;
    private AppStyleManager appStyleManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_ls);
        initViews();
        getParametersFromPrefs();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setViewsColor();
        }

        setTechColors();

        appStyleManager = AppStyleManager.getInstance(this, hex);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(RegLsActivity.this)) {
                    ls = etLs.getText().toString();
                    pass = etPass.getText().toString();
                    email = etEmail.getText().toString();
                    billskey = etBillskey.getText().toString();

                    if (!ls.equals("") && !pass.equals("") && !email.equals("") && !billskey.equals("")) {
                        if (StringUtils.checkIsEmailValid(email)) {
                            sendInfoToServer();
                        } else {
                            showToastHere("Введен некорректный email");
                        }
                    } else {
                        showToastHere("Заполните все поля");
                    }
                } else {
                    DialogCreator.showInternetErrorDialog(RegLsActivity.this, hex);
                }
            }
        });

        ivPasswordEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(getDrawable(R.drawable.ic_password_eye));
                } else {
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_password_eye));
                }
                etPass.setSelection(etPass.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeScreen();
            }
        });

        if (ConnectionUtils.hasConnection(RegLsActivity.this)) {
            hideNoInternet();
        } else {
            showNoInternet();
        }

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (!isFinishing() && !isDestroyed() && progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (msg.what == 0) {
                    getAccountsFromServer();
                } else if (msg.what == 1) {
                    showToastHere(errorStr);
                } else if (msg.what == 2) {
                    String personalAccounts = parseJsonPersonalAccounts(linePersonalAccounts);

                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString(PERSONAL_ACCOUNTS_PREF, personalAccounts);
                    ed.commit();

                    AlertDialog.Builder builder = new AlertDialog.Builder(RegLsActivity.this);
                    builder.setMessage("Лицевой счет успешно привязан!");
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
                } else if (msg.what == 3) {

                    String error = "";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    final String finalError = error;

                    AlertDialog.Builder builder = new AlertDialog.Builder(RegLsActivity.this);
                    builder.setTitle("Ошибка!");
                    builder.setMessage("Не удалось зарегистрировать лицевой счет, напишите в техподдержку\nОтвет сервера: " + error);
                    builder.setPositiveButton("Написать в техподдержку", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RegLsActivity.this, TechSendActivity.class);
                            intent.putExtra("error_str", finalError);
                            startActivity(intent);
                            overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                        }
                    });
                    builder.setNegativeButton("Пропустить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#" + hex));
                        }
                    } else {
                        showToastHere("Не удалось зарегистрировать лицевой счет, напишите в техподдержку");
                    }
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeScreen();
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(RegLsActivity.this)) {
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

    private void sendInfoToServer() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Отправление данных...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar = progressDialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.reg_ls_mytishi(ls, phone, pass, email, fio, billskey);
                if (line.equals("ok") || line.equals("\"ok\"")) {
                    handler.sendEmptyMessage(0);
                } else if (line.toLowerCase().contains("error:")) {
                    errorStr = line.replace("error: ", "");
                    handler.sendEmptyMessage(1);
                } else {
                    Message msg = handler.obtainMessage(3, 0, 0, line);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void getAccountsFromServer() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Синхронизация данных...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar = progressDialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                linePersonalAccounts = server.getAccountIdents(phone);
                if (linePersonalAccounts.equals("xxx")) {
                    Message msg = handler.obtainMessage(3, 0, 0, linePersonalAccounts);
                    handler.sendMessage(msg);
                } else if (linePersonalAccounts.toLowerCase().contains("error:")) {
                    errorStr = linePersonalAccounts.replace("error:", "");
                    handler.sendEmptyMessage(1);
                } else {
                    handler.sendEmptyMessage(2);
                }
            }
        }).start();
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
            Logger.errorLog(RegLsActivity.this.getClass(), e.getMessage());
        }
        return personalAccounts.toString();
    }

    private void setTechColors() {
        TextView tvTech = findViewById(R.id.tv_tech);
        CardView cvDisp = findViewById(R.id.card_view_img_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
        }

        LinearLayout layout = findViewById(R.id.layout_tech);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegLsActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void getParametersFromPrefs() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        phone = sPref.getString("login_pref", "");
        fio = sPref.getString("_fio_", "");
    }

    @SuppressLint("NewApi")
    private void setViewsColor() {
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        etLs.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        etPass.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        etEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        etBillskey.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnReg.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        btnCancel.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
    }

    private void initViews() {
        etLs = findViewById(R.id.et_reg_ls);
        etPass = findViewById(R.id.et_reg_pass);
        etEmail = findViewById(R.id.et_reg_email);
        ivPasswordEye = findViewById(R.id.iv_password_eye);
        etBillskey = findViewById(R.id.et_reg_billkey);
        btnCancel = findViewById(R.id.btn_reg_cancel);
        btnReg = findViewById(R.id.btn_reg_registration);
        layoutMain = findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        setToolbar();
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(RegLsActivity.this, title);
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Регистрация лицевого счета");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeScreen();
            }
        });
    }

    private void closeScreen() {
        Context context = getApplicationContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etLs.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etBillskey.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etPass.getWindowToken(), 0);
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
