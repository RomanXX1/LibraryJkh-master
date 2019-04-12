package com.patternjkh.ui.statement;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.OsvHistoryItem;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.patternjkh.utils.ToastUtils.showToast;

public class HistoryOsvOldActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String login, pass, hex;

    private ProgressDialog dialog;
    private ConstraintLayout layoutMain;
    private LinearLayout layoutNoInternet;
    private Button btnNoInternetRefresh;
    private RecyclerView rvHistoryOsv;

    private ArrayList<OsvHistoryItem> items = new ArrayList<>();
    private Server server;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_osv_old);

        getParamsFromSharedPrefs();
        initViews();
        setToolbar();

        LinearLayout layout = findViewById(R.id.layout_osv_history);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout.setBackgroundColor(Color.parseColor("#" + hex));
            btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        }

        rvHistoryOsv.setLayoutManager(new LinearLayoutManager(this));
        final OsvHistoryAdapter adapter = new OsvHistoryAdapter(items);
        rvHistoryOsv.setAdapter(adapter);

        if (ConnectionUtils.hasConnection(this)) {
            requestDataFromServer();
            hideNoInternet();
        } else {
            showNoInternet();
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hideDialog();

                if (msg.what == 0) {
                    adapter.notifyDataSetChanged();
                } else if (msg.what == 1) {
                    String error = "-";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    showToastHere("Ошибка загрузки данных. Ответ сервера: " + error);
                }
            }
        };
    }

    private void requestDataFromServer() {
        showDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getHistoryFromServer();
            }
        }).start();
    }

    private void getHistoryFromServer() {
        server = new Server(this);
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
                    items.add(new OsvHistoryItem(date.substring(0, 10), period, formatPay(pay)));
                } else {
                    items.add(new OsvHistoryItem(date, period, formatPay(pay)));
                }
            }

            handler.sendEmptyMessage(0);
        } catch (Exception e) {
            Message msg = handler.obtainMessage(1, 0, 0, line);
            handler.sendMessage(msg);
            Logger.errorLog(HistoryOsvOldActivity.this.getClass(), e.getMessage());
        }
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(HistoryOsvOldActivity.this)) {
                    getHistoryFromServer();
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
            showToast(HistoryOsvOldActivity.this, title);
        }
    }

    private void initViews() {
        layoutMain = findViewById(R.id.layout_with_internet);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        rvHistoryOsv = findViewById(R.id.rv_history_osv);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
    }
}
