package com.patternjkh.ui.others;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.registration.RegLsActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.patternjkh.utils.ToastUtils.showToast;

public class ProfileActivity extends AppCompatActivity implements OnLsDeleteListener {

    private String phone, email, hex, accsString, ls, errorStr = "", fio;

    private TextView tvLsTitle;
    private EditText etEmail, etFio;
    private RecyclerView rvLs;
    private Button btnAddLs, btnSave, btnNoInternetRefresh;
    private LinearLayout layoutNoInternet, layoutMain;

    private SharedPreferences sPref;
    private PersonalAccountsAdapter adapter;
    private ArrayList<String> personalAccounts = new ArrayList<>();
    private Handler handler;
    private Server server = new Server(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();

        getParametersFromPrefs();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColors();
        }

        initClickListeners();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("mail_pref", etEmail.getText().toString());
                    ed.putString("_fio_", etFio.getText().toString());
                    ed.commit();
                    showToastHere("Данные о профиле успешно изменены");
                } else if (msg.what == 1) {
                    showToastHere(errorStr);
                } else if (msg.what == 3) {
                    showErrorDialog();
                }
            }
        };

        if (ConnectionUtils.hasConnection(ProfileActivity.this)) {
            hideNoInternet();
        } else {
            showNoInternet();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void showErrorDialog() {
        DialogCreator.showErrorCustomDialog(this, errorStr, hex);
    }

    private void getData() {
        getParametersFromPrefs();
        etEmail.setText(email);
        etFio.setText(fio);

        if (!accsString.equals("") && !accsString.equals(",") && !accsString.equals(" ")) {
            personalAccounts.clear();
            personalAccounts.addAll(Arrays.asList(accsString.split(",")));
        }

        if (personalAccounts.size() > 0) {
            tvLsTitle.setText("Привязанные лицевые счета:");
        } else {
            tvLsTitle.setText("Привязанных лицевых счетов не обнаружено");
        }

        adapter = new PersonalAccountsAdapter(personalAccounts, this);
        rvLs.setLayoutManager(new LinearLayoutManager(this));
        rvLs.setAdapter(adapter);
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(ProfileActivity.this)) {
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

    private void initClickListeners() {
        btnAddLs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(ProfileActivity.this, AddPersonalAccountActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(ProfileActivity.this)) {
                    saveProfileInfo();
                } else {
                    DialogCreator.showInternetErrorDialog(ProfileActivity.this, hex);
                }
            }
        });
    }

    private void saveProfileInfo() {
        if (!etEmail.getText().toString().equals("") && StringUtils.checkIsEmailValid(etEmail.getText().toString())) {
            String line = server.saveEmail(phone, etEmail.getText().toString(), etFio.getText().toString());
            if (line.equals("ok")) {
                handler.sendEmptyMessage(0);
            } else if (!line.toLowerCase().contains("error:")) {
                errorStr = line;
                handler.sendEmptyMessage(3);
            } else {
                errorStr = line.replace("error: ", "");
                handler.sendEmptyMessage(1);
            }
        } else {
            showToastHere("Введен некорректный email");
        }
        InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
        }
    }

    @Override
    public void onLsDeleteClicked(String ls) {
        this.ls = ls;
        if (ConnectionUtils.hasConnection(ProfileActivity.this)) {
            showDeleteLsDialog();
        } else {
            DialogCreator.showInternetErrorDialog(ProfileActivity.this, hex);
        }
    }

    private void showDeleteLsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Отвязать лицевой счет " + ls.replaceAll("\r", "") + " от аккаунта?");
        builder.setPositiveButton("Да",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Server server = new Server(ProfileActivity.this);
                        String line = server.deleteAccount(phone, ls);
                        if (line.equals("ok")) {
                            accsString = accsString.replace("\r\n" + ls, "");
                            personalAccounts.remove(ls);
                            adapter.notifyDataSetChanged();
                            updateAccs();
                            adapter.notifyDataSetChanged();
                            showToastHere("Лс успешно удален");
                            tvLsTitle.setText("Привязанных лицевых счетов не обнаружено");
                        } else {
                            showToastHere("Ошибка удаления лс");
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

    private void updateAccs() {
        String linePersonalAccounts = server.getAccountIdents(sPref.getString("login_push", ""));
        String personalAccounts = parseJsonPersonalAccounts(linePersonalAccounts);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("personalAccounts_pref", personalAccounts);
        ed.commit();
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
            Logger.errorLog(ProfileActivity.this.getClass(), e.getMessage());
        }
        return personalAccounts.toString();
    }

    private void getParametersFromPrefs() {
        sPref = getSharedPreferences("global_settings", MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        email = sPref.getString("mail_pref", "");
        phone = sPref.getString("login_push", "");
        fio = sPref.getString("_fio_", "");
        accsString = sPref.getString("personalAccounts_pref", "");
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Профиль");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
                }
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(ProfileActivity.this, title);
        }
    }

    @SuppressLint("NewApi")
    private void setColors() {
        btnAddLs.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        etEmail.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        etFio.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_profile_email);
        etFio = findViewById(R.id.et_profile_fio);
        rvLs = findViewById(R.id.rv_profile_ls);
        btnAddLs = findViewById(R.id.btn_profile_add_ls);
        btnSave = findViewById(R.id.btn_profile_save);
        tvLsTitle = findViewById(R.id.tv_profile_added_ls_title);
        layoutMain = findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        setToolbar();
    }
}
