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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.AppStyleManager;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.PhoneMaskWatcher;
import com.patternjkh.utils.PhoneUtils;
import com.patternjkh.utils.StringUtils;

import static com.patternjkh.utils.ToastUtils.showToast;

public class RegistrationNewActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String hex, inputtedPhone;
    private boolean isFromColdLaunch;

    private EditText mPhoneEditText, mFioEditText;
    private TextView tvAlreadyRegistered;
    private Button mRegistrationButton, btnNoInternetRefresh, btnToLogin;
    private LinearLayout layoutNoInternet;
    private ConstraintLayout layoutMain;
    private ProgressDialog mDialog;
    private Switch switchAgreement;

    private Handler mRegistrationPhoneHandler;

    private SharedPreferences sPref;
    private Server server = new Server(this);
    private AppStyleManager appStyleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_new);
        initComponents();
        initListeners();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

//        setButtonEnabled(false);

        isFromColdLaunch = getIntent().getBooleanExtra("from_cold_launch", true);

        if (!isFromColdLaunch) {
            btnToLogin.setVisibility(View.GONE);
            tvAlreadyRegistered.setVisibility(View.GONE);
        } else {
            btnToLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = null;
                    try {
                        String name = ComponentsInitializer.loginActivity.getName() + ".LoginActivity";
                        intent = new Intent(RegistrationNewActivity.this, Class.forName(name));
                        intent.putExtra("from_registration", true);
                        if (!mPhoneEditText.getText().toString().equals("")) {
                            intent.putExtra("login_from_reg", mPhoneEditText.getText().toString());
                        }
                        startActivity(intent);
                        overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                        finish();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        setToolbar();

        mRegistrationPhoneHandler = new Handler() {
            public void handleMessage(Message message) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPhoneEditText.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mFioEditText.getWindowToken(), 0);
                if (!isFinishing() && !isDestroyed()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }

                if (message.what == 1) {
                    startRegistrationNewActivity();
                } else if (message.what == 2) {
                    startRegistrationNewActivity();
                } else if (message.what == 3) {
                    if (message.obj!=null){
                        String errorMessage = String.valueOf(message.obj);
                        errorMessage = errorMessage.replaceFirst("error: ","");
                        showToastHere(StringUtils.firstUpperCase(errorMessage));
                    }

                } else if (message.what == 404) {
                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }

                    DialogCreator.showErrorCustomDialog(RegistrationNewActivity.this, error, hex);
                }
            }
        };

        if (ConnectionUtils.hasConnection(this)) {
            hideNoInternet();
        } else {
            showNoInternet();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        closeScreen();
    }

    private void showNoInternet() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mFioEditText.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(mPhoneEditText.getWindowToken(), 0);
        }
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(RegistrationNewActivity.this)) {
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

    private void initListeners() {

        mFioEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mFioEditText.setCursorVisible(true);
                return false;
            }
        });

        mPhoneEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPhoneEditText.setCursorVisible(true);
                return false;
            }
        });

        mPhoneEditText.addTextChangedListener(new PhoneMaskWatcher("##-###-###-####"));

        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputtedPhone = mPhoneEditText.getText().toString().replaceAll("-", "");

                if (TextUtils.isEmpty(inputtedPhone)) {
                    showToastHere("Необходимо заполнить " + getString(R.string.reg_phone));
                    mPhoneEditText.requestFocus();
                    return;
                }

                if (!PhoneUtils.checkValidPhone(inputtedPhone)) {
                    showToastHere("Номер телефона необходимо ввести в формате: +7-ХХХ-ХХХ-ХХХХ");
                    mPhoneEditText.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(inputtedPhone)) {
                    showToastHere("Необходимо заполнить " + getString(R.string.reg_fio));
                    mFioEditText.requestFocus();
                    return;
                }

                if (!switchAgreement.isChecked()) {
                    showToastHere("Необходимо дать согласие на обработку персональных данных");
                    return;
                }

                if (!ConnectionUtils.hasConnection(RegistrationNewActivity.this)) {
                    DialogCreator.showInternetErrorDialog(RegistrationNewActivity.this, hex);
                } else {
                    mDialog = new ProgressDialog(RegistrationNewActivity.this);
                    mDialog.setMessage("Регистрация...");
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
                        progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    SharedPreferences sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("_fio_", mFioEditText.getText().toString());
                    ed.commit();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String line = "xxx";
                            line = server.registrationAccount(inputtedPhone, mFioEditText.getText().toString());

                            if (line.equals("ok:1")) {
                                mRegistrationPhoneHandler.sendEmptyMessage(1);
                            } else if (line.equals("ok:2")) {
                                mRegistrationPhoneHandler.sendEmptyMessage(2);
                            } else if (line.contains("error")) {
                                mRegistrationPhoneHandler.sendEmptyMessage(3);
                                Message msg = mRegistrationPhoneHandler.obtainMessage(3, 0, 0, line);
                                mRegistrationPhoneHandler.sendMessage(msg);
                            } else {
                                Message msg = mRegistrationPhoneHandler.obtainMessage(404, 0, 0, line);
                                mRegistrationPhoneHandler.sendMessage(msg);
                            }
                        }
                    }).start();
                }
            }
        });
    }

    private void closeScreen() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mFioEditText.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(mPhoneEditText.getWindowToken(), 0);
        }
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void startRegistrationNewActivity() {
        Intent intent = RegistrationNewByStepsActivity.newIntent(RegistrationNewActivity.this, inputtedPhone);
        startActivity(intent);
        overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        mPhoneEditText.setCompoundDrawablesWithIntrinsicBounds(appStyleManager.changeDrawableColor(R.drawable.ic_smartphone), null, null, null);
        mFioEditText.setCompoundDrawablesWithIntrinsicBounds(appStyleManager.changeDrawableColor(R.drawable.ic_perm_identity), null, null, null);
        mPhoneEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        mFioEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        btnToLogin.setTextColor(Color.parseColor("#" + hex));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        mRegistrationButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorStateList buttonStates = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            Color.parseColor("#" + hex),
                            Color.parseColor("#" + hex),
                            Color.LTGRAY
                    }
            );
            switchAgreement.getThumbDrawable().setTintList(buttonStates);
            switchAgreement.getTrackDrawable().setTintList(buttonStates);
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(RegistrationNewActivity.this, title);
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Регистрация");
        if (!isFromColdLaunch) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                Context context = getApplicationContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPhoneEditText.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mFioEditText.getWindowToken(), 0);
            }
        });
    }

    private void initComponents() {
        mPhoneEditText = findViewById(R.id.reg_phone);
        mPhoneEditText.setCursorVisible(false);
        mFioEditText = findViewById(R.id.reg_fio);
        mFioEditText.setCursorVisible(false);
        mRegistrationButton = findViewById(R.id.btn_registration);
        layoutMain = findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        tvAlreadyRegistered = findViewById(R.id.tv_registered_already);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        btnToLogin = findViewById(R.id.btn_to_main);
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        appStyleManager = AppStyleManager.getInstance(this, hex);
        switchAgreement = findViewById(R.id.switch_agreement);
    }
}
