package com.patternjkh.ui.statement;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.OsvHistoryItem;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.patternjkh.utils.ToastUtils.showToast;

public class HistoryOsvActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String login, pass, hex;

    private ProgressDialog dialog;
    private LinearLayout layoutNoInternet, layoutMain;
    private Button btnNoInternetRefresh;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    
    private Server server;
    private Handler handler;
    private DB db = new DB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_osv);
        initViews();
        getParamsFromSharedPrefs();
        setToolbar();

        setColors();

        server = new Server(this);
        db.open();

        if (ConnectionUtils.hasConnection(this)) {
            getDataFromServer();
            hideNoInternet();
        } else {
            showNoInternet();
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                hideDialog();
                if (msg.what == 0) {
                    setupViewPager(viewPager);
                    tabLayout.setupWithViewPager(viewPager);
                } else if (msg.what == 1) {
                    String error = "-";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    Logger.errorLog(HistoryOsvActivity.this.getClass(), error);
                    showToastHere("Ошибка загрузки данных. Ответ сервера: " + error);
                }
            }
        };
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HistoryOsvFragment(), "Все платежи");
        adapter.addFragment(new MobilePaysFragment(), "Мобильные платежи");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(HistoryOsvActivity.this)) {
                    getDataFromServer();
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

    private void getDataFromServer() {
        showDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getHistoryOsvFromServer();
                getMobilePaysFromServer();
            }
        }).start();
    }

    private void getHistoryOsvFromServer() {
        db.del_table(DB.TABLE_HISTORY_OSV);
        String line = server.getOsvHistory(login, pass);
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_history = json.getJSONArray("data");
            for (int i = 0; i < json_history.length(); i++) {
                JSONObject json_house = json_history.getJSONObject(i);
                String date = json_house.getString("Date");
                String period = json_house.getString("Period");
                Double pay = json_house.getDouble("Sum");
                if (date.length() > 10) {
                    db.addHistoryOsv(date.substring(0, 10), period, formatPay(pay));
                } else {
                    db.addHistoryOsv(date, period, formatPay(pay));
                }
            }

        } catch (Exception e) {
            Message msg = handler.obtainMessage(1, 0, 0, line);
            handler.sendMessage(msg);
            Logger.errorLog(HistoryOsvActivity.this.getClass(), e.getMessage());
        }
    }

    private void getMobilePaysFromServer() {
        db.del_table(DB.TABLE_MOBILE_PAYS);
        String line = server.getMobilePaysHistory(login);
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_history = json.getJSONArray("data");
            for (int i = 0; i < json_history.length(); i++) {
                JSONObject json_house = json_history.getJSONObject(i);
                String date = json_house.getString("Date");
                String status = json_house.getString("Status");
                Double pay = json_house.getDouble("Sum");

                if (status.equals("Обработан")) {
                    status = "Оплачен";
                }

                if (date.length() > 10) {
                    db.addMobilePays(date.substring(0, 10), formatPay(pay), status);
                } else {
                    db.addMobilePays(date, formatPay(pay), status);
                }
            }

            handler.sendEmptyMessage(0);
        } catch (Exception e) {
            Message msg = handler.obtainMessage(1, 0, 0, line);
            handler.sendMessage(msg);
            Logger.errorLog(HistoryOsvActivity.this.getClass(), e.getMessage());
        }
    }

    private String formatPay(Double pay) {
        DecimalFormat decim = new DecimalFormat("0.00");
        return decim.format(pay).replaceAll(",", ".");
    }

    private void getParamsFromSharedPrefs() {
        SharedPreferences prefs = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        login = prefs.getString("login_push", "");
        pass = prefs.getString("pass_push", "");
        hex = prefs.getString("hex_color", "23b6ed");
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("История платежей");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void showDialog() {
        if (!isFinishing() && !isDestroyed()) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Загрузка истории платежей...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void hideDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(HistoryOsvActivity.this, title);
        }
    }

    private void setColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setTabTextColors(Color.parseColor("#c0c0c0"), Color.parseColor("#" + hex));
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#" + hex));
            btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        }
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewpager);
        layoutMain = findViewById(R.id.layout_tabs);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
    }
}