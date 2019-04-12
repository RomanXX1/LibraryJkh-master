package com.patternjkh.ui.counters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.CounterHistoryItem;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.DigitUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.patternjkh.utils.ToastUtils.showToast;

public class CounterMytishiActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String ident="", units="", name="", uniqueNum="", factoryNum="", canGiveCounts = "0", login, pass, hex, start_day, end_day;
    private int typeId;

    private ProgressDialog dialog;
    private ConstraintLayout layoutMain;
    private LinearLayout layoutNoInternet;
    private Button btnNoInternetRefresh, btnAdd;
    private RecyclerView rvHistoryOsv;
    private TextView tvLs, tvName, tvFactoryNum, tvNotSent;
    private AlertDialog alertDialog;

    private ArrayList<CounterHistoryItem> items = new ArrayList<>();
    private Server server;
    private CounterHistoryAdapter adapter;
    private Handler handler;
    private DB db = new DB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_mytishi);
        initViews();

        db.open();

        getParamsFromIntent();
        getParamsFromSharedPrefs();
        setToolbar();

        tvLs.setText("Л/с: " + ident);
        tvName.setText(name);
        tvFactoryNum.setText(factoryNum);

        initClickListeners();

        LinearLayout layout = findViewById(R.id.layout_osv_history);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout.setBackgroundColor(Color.parseColor("#" + hex));
            btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
            btnAdd.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        }

        rvHistoryOsv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CounterHistoryAdapter(items);
        rvHistoryOsv.setAdapter(adapter);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                closeAddCountDialog();

                if (msg.what == 0) {
                    hideProgressDialog();
                    adapter.notifyDataSetChanged();
                    checkIfFirstCounterIsError();
                } else if (msg.what == 1) {
                    adapter.notifyDataSetChanged();
                    checkIfFirstCounterIsError();
                    showToastHere("Показания переданы");
                } else if (msg.what == 100) {
                    showToastHere("Переданы не все параметры");
                } else if (msg.what == 101) {
                    showToastHere("Не пройдена авторизация");
                } else if (msg.what == 102) {
                    showToastHere("Не найден прибор у пользователя");
                } else if (msg.what == 103) {
                    showToastHere("Передача показаний возможна только с 15 по 25 числа");
                } else if (msg.what == 404) {
                    String error = "";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    DialogCreator.showErrorCustomDialog(CounterMytishiActivity.this, error, hex);
                }
            }
        };

        if (ConnectionUtils.hasConnection(this)) {
            requestDataFromServer();
            hideNoInternet();
        } else {
            showNoInternet();
        }
    }

    private void initClickListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectionUtils.hasConnection(CounterMytishiActivity.this)) {
                    addCount();
                } else {
                    DialogCreator.showInternetErrorDialog(CounterMytishiActivity.this, hex);
                }
            }
        });
    }

    private void addCount() {
        if (canGiveCounts.equals("1")) {
            LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.add_counts, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(CounterMytishiActivity.this);
            builder.setView(layout);
            TextView txt_number = layout.findViewById(R.id.txt_number);
            txt_number.setText(name + ", " + units);

            final EditText diff_count = layout.findViewById(R.id.text_Count);
            final Button btnAdd = layout.findViewById(R.id.btn_add_count_dialog);
            Button btnCancel = layout.findViewById(R.id.btn_add_count_dialog_cancel);
            final ProgressBar pb = layout.findViewById(R.id.pb_add_count_dialog);
            pb.setVisibility(View.INVISIBLE);
            btnAdd.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                diff_count.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
                btnAdd.setTextColor(Color.parseColor("#" + hex));
                btnCancel.setTextColor(Color.parseColor("#" + hex));
                pb.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pb.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.INVISIBLE);

                    final String counterValue = diff_count.getText().toString();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            int year = DateUtils.getCurrentYear();
                            int month = DateUtils.getCurrentMonth();
                            int day = DateUtils.getCurrentDay();

                            String monthStr = "";
                            if (month < 10) {
                                monthStr = "0" + month;
                            } else {
                                monthStr += month;
                            }

                            String dayStr = "";
                            if (day < 10) {
                                dayStr = "0" + day;
                            } else {
                                dayStr += day;
                            }

                            String date = dayStr + "." + monthStr + "." + year;

                            String valueDouble = Double.toString(DigitUtils.round(StringUtils.convertStringToFloat(counterValue), 2)).replace(".", ",");
                            if (valueDouble.endsWith(",0")) {
                                valueDouble = valueDouble.replace(",0", ",00");
                            }

                            String line = "";
                            try {
                                line = server.addCounterValueMytishi(login, pass, uniqueNum, counterValue);
                                if (line.equals("0")) {
                                    handler.sendEmptyMessage(100);
                                } else if (line.equals("1")) {
                                    handler.sendEmptyMessage(101);
                                } else if (line.equals("2")) {
                                    handler.sendEmptyMessage(102);
                                } else if (line.equals("3")) {
                                    handler.sendEmptyMessage(103);
                                } else if (line.equals("5")) {

                                    if (items.get(0).getPeriod().equals(date)) {
                                        items.set(0, new CounterHistoryItem(date, valueDouble, "1", "0"));
                                    } else {
                                        items.add(0, new CounterHistoryItem(date, valueDouble, "1", "0"));
                                    }

                                    boolean upd = db.updateCountMytishi(date, valueDouble, uniqueNum, "0");

                                    if (!upd) {
                                        db.addCountMytishi(login, ident, units, name, uniqueNum, typeId, factoryNum, date, valueDouble, "1", "0");
                                    }

                                    handler.sendEmptyMessage(1);
                                } else {
                                    Message msg = handler.obtainMessage(404, 0, 0, line);
                                    handler.sendMessage(msg);
                                }
                            } catch (Exception e) {
                                Logger.errorLog(this.getClass(), e.getMessage());
                            }
                        }
                    }).start();
                }
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeAddCountDialog();
                }
            });

            alertDialog = builder.create();
            if (!isFinishing() && !isDestroyed()) {
                alertDialog.show();
            }
        } else {
            showCantGiveCountsDialog();
        }
    }

    private void showCantGiveCountsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ошибка");
        builder.setMessage("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
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

    private void requestDataFromServer() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getHistoryFromServer();
            }
        }).start();
    }

    private void getHistoryFromServer() {
        server = new Server(this);
        String line = server.getCounterValuesMytishi(login, pass, getIntent().getStringExtra("counter_id"));
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
            Logger.errorLog(this.getClass(), e.getMessage());
        }

        handler.sendEmptyMessage(0);
    }

    public class Parser_Get_Counters_Mytishi extends DefaultHandler {

        private String periodDate, value, isSent, sendError;
        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (localName.toLowerCase().equals("metervalue")) {
                periodDate = atts.getValue("PeriodDate");
                value = atts.getValue("Value");
                isSent = atts.getValue("IsSended");
                sendError = atts.getValue("SendError");
                items.add(new CounterHistoryItem(periodDate, value, isSent, sendError));
            }
        }
    }

    private void checkIfFirstCounterIsError() {
        if (items != null && items.size() > 0) {
            if (items.get(0).getSendError().equals("1")) {
                btnAdd.setText("Передать еще раз");
                tvNotSent.setVisibility(View.VISIBLE);
            } else {
                btnAdd.setText("Передать показания");
                tvNotSent.setVisibility(View.GONE);
            }
        }
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(CounterMytishiActivity.this)) {
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

    private void getParamsFromIntent() {
        units = getIntent().getStringExtra("counter_units");
        uniqueNum = getIntent().getStringExtra("counter_id");
        factoryNum = getIntent().getStringExtra("counter_factory");
        name = getIntent().getStringExtra("counter_name");
        ident = getIntent().getStringExtra("counter_ls");
        typeId = getIntent().getIntExtra("counter_type", -1);
    }

    private void getParamsFromSharedPrefs() {
        SharedPreferences prefs = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        login = prefs.getString("login_push", "");
        pass = prefs.getString("pass_push", "");
        hex = prefs.getString("hex_color", "23b6ed");
        canGiveCounts = prefs.getString("can_count", "0");
        start_day = prefs.getString("start_day", "");
        end_day = prefs.getString("end_day", "");
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(CounterMytishiActivity.this, title);
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("История показаний");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void showProgressDialog() {
        if (!isFinishing() && !isDestroyed()) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Загрузка истории показаний...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void hideProgressDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

    private void closeAddCountDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
    }

    private void initViews() {
        layoutMain = findViewById(R.id.layout_with_internet);
        layoutNoInternet = findViewById(R.id.layout_no_internet);
        rvHistoryOsv = findViewById(R.id.rv_history_counter);
        btnNoInternetRefresh = findViewById(R.id.btn_no_internet_refresh);
        btnAdd = findViewById(R.id.btn_history_counter_add);
        tvLs = findViewById(R.id.tv_history_counter_ls);
        tvName = findViewById(R.id.tv_history_counter_name);
        tvFactoryNum = findViewById(R.id.tv_history_counter_factory);
        tvNotSent = findViewById(R.id.tv_history_counter_not_sent);
    }
}