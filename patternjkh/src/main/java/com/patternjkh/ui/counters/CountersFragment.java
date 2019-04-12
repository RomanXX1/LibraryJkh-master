package com.patternjkh.ui.counters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Counter;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.DigitUtils;
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

import static android.content.Context.MODE_PRIVATE;

public class CountersFragment extends Fragment implements OnAddCounterClickListener {

    private int i_num_month, i_year, max_i_num_month, max_i_year, min_i_num_month = 0, min_i_year = 0, spinnerPosition = 0;
    private String start_day, end_day, login, pass, strPersonalAccs, hex, currentMonth, currentDate, canGiveCounts = "0";
    private String[] personalAccounts;

    private RecyclerView count_list;
    private Spinner spinnerPersonalAcc;
    private Button btnNoInternetRefresh;
    private LinearLayout layoutNoInternet, layoutTech, layoutMain;
    private TextView tvCounterDates, tvPreviousMonth, tvNextMonth, tvMonthTitle, tvEmpty, tvAddLs;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CountersAdapter count_adapter;
    private Cursor cursor;
    private DB db;
    private Server server = new Server(getActivity());
    private SharedPreferences sPref;
    private Handler handler;
    private ArrayList<Counter> counters = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences("global_settings", MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        db = new DB(getActivity());
        db.open();

        getParametersFromPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.counters_fragment, container, false);
        initViews(v);
        setTechColors(v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        int year = DateUtils.getCurrentYear();
        int month = DateUtils.getCurrentMonth();
        currentMonth = DateUtils.getMonthNameByNumber(month);
        currentDate = currentMonth + " " + year;
        tvMonthTitle.setText(currentDate);

        initPersonalAccountsAndAll();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.choice_type, R.id.text_name_type, personalAccounts);
        spinnerPersonalAcc.setAdapter(spinnerAdapter);

        initClickListeners();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getCountersFromServer();
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    hideNoInternet();
                    getData();
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                            swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        };

        return v;
    }

    @Override
    public void onResume() {
        if (ConnectionUtils.hasConnection(getActivity())) {
            hideNoInternet();
            getData();
        } else {
            showNoInternet();
        }

        super.onResume();
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        layoutTech.setVisibility(View.GONE);
        tvAddLs.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(getActivity())) {
                    hideNoInternet();
                    getData();
                } else {
                    showNoInternet();
                }
            }
        });
    }

    private void hideNoInternet() {
        layoutNoInternet.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
        layoutTech.setVisibility(View.GONE);
    }

    private void getData() {

        if (personalAccounts.length <= 1) {
            layoutMain.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvAddLs.setVisibility(View.VISIBLE);
            layoutTech.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            tvAddLs.setVisibility(View.GONE);
            layoutTech.setVisibility(View.VISIBLE);
            layoutMain.setVisibility(View.VISIBLE);

            cursor = db.getDataFromTableByOrder(DB.TABLE_COUNTERS, DB.COL_YEAR + ", " + DB.COL_NUM_MONTH);
            if (cursor.moveToFirst()) {
                do {
                    int c_num_m = cursor.getColumnIndex(DB.COL_NUM_MONTH);
                    int c_year = cursor.getColumnIndex(DB.COL_YEAR);

                    int numMonth = cursor.getInt(c_num_m);

                    String name_month = DateUtils.getMonthNameByNumber(numMonth);
                    String name_btn_month = name_month + " " + String.valueOf(cursor.getInt(c_year));

                    tvMonthTitle.setText(name_btn_month.toUpperCase());
                    setPrevAndNextMonthName(numMonth);

                    i_num_month = cursor.getInt(c_num_m);
                    i_year = cursor.getInt(c_year);

                    // установим максимальные значения по месяцам
                    max_i_num_month = i_num_month;
                    max_i_year = i_year;

                    // установим минимальные значения по месяцам
                    if (min_i_year == 0) {
                        min_i_year = i_year;
                        if (min_i_num_month == 0) {
                            min_i_num_month = i_num_month;
                        } else if (min_i_num_month > i_num_month) {
                            min_i_num_month = i_num_month;
                        }
                    } else if (min_i_year > i_year) {
                        min_i_year = i_year;
                        if (min_i_num_month == 0) {
                            min_i_num_month = i_num_month;
                        } else if (min_i_num_month > i_num_month) {
                            min_i_num_month = i_num_month;
                        }
                    } else if (min_i_year == i_year) {
                        if (min_i_num_month == 0) {
                            min_i_num_month = i_num_month;
                        } else if (min_i_num_month > i_num_month) {
                            min_i_num_month = i_num_month;
                        }
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();

            filldata(i_num_month, i_year);

            showIsUserCanGiveCounts();

            count_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            count_adapter = new CountersAdapter(counters, this, hex);
            count_list.setAdapter(count_adapter);

            check_border();
        }
    }

    private void initClickListeners() {

        spinnerPersonalAcc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                spinnerPosition = position;
                filldata(i_num_month, i_year);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tvPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                do {
                    i_num_month = i_num_month - 1;
                    if (i_num_month == 0) {
                        i_num_month = 12;
                        i_year = i_year - 1;
                    }
                } while (((i_num_month < min_i_num_month) & (i_year == min_i_year) |
                        (db.chechk_month_in_DB(i_num_month, i_year))
                ));
                check_table();
                check_border();
                if (!currentDate.toLowerCase().equals(tvMonthTitle.getText().toString().toLowerCase())) {
                    tvCounterDates.setText("За " + tvMonthTitle.getText().toString().toLowerCase() + " передать показания уже нельзя, перейдите в " + currentMonth.toLowerCase() + " для передачи текущих показаний");
                } else {
                    showIsUserCanGiveCounts();
                }
            }
        });

        tvNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                do {
                    i_num_month = i_num_month + 1;
                    if (i_num_month == 13) {
                        i_num_month = 1;
                        i_year = i_year + 1;
                    }
                } while (((i_num_month > max_i_num_month) & (i_year == max_i_year) |
                        (db.chechk_month_in_DB(i_num_month, i_year))
                ));
                check_table();
                check_border();
                if (!currentDate.toLowerCase().equals(tvMonthTitle.getText().toString().toLowerCase())) {
                    tvCounterDates.setText("За " + tvMonthTitle.getText().toString().toLowerCase() + " передать показания уже нельзя, перейдите в " + currentMonth.toLowerCase() + " для передачи текущих показаний");
                } else {
                    showIsUserCanGiveCounts();
                }
            }
        });
    }

    private void showIsUserCanGiveCounts() {
        if (start_day.equals("0") || end_day.equals("0") || start_day.equals("") || end_day.equals("")) {
            tvCounterDates.setText("Возможность передавать показания доступна в текущем месяце!");
        } else {
            tvCounterDates.setText("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");
        }
    }

    private void initPersonalAccountsAndAll(){
        if (TextUtils.isEmpty(strPersonalAccs)){
            personalAccounts = new String[1];
            personalAccounts[0] = "Все";
        } else {
            String[] personalAccountsSeparate = strPersonalAccs.split(",");

            personalAccounts = new String[personalAccountsSeparate.length + 1];
            personalAccounts[0] = "Все";

            for (int i = 0; i < personalAccountsSeparate.length; i++) {
                personalAccounts[i + 1] = personalAccountsSeparate[i];
            }
        }
    }

    void check_border() { // проверка на достижение границ по месяцам в БД
        if ((i_num_month == min_i_num_month) & (i_year == min_i_year)) {
            tvPreviousMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPreviousMonth.setVisibility(View.INVISIBLE);
            tvPreviousMonth.setClickable(false);
        } else {
            tvPreviousMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPreviousMonth.setVisibility(View.VISIBLE);
            tvPreviousMonth.setClickable(true);

        }
        if ((i_num_month == max_i_num_month) & (i_year == max_i_year)) {
            tvNextMonth.setVisibility(View.INVISIBLE);
            tvNextMonth.setClickable(false);
        } else {
            tvNextMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvNextMonth.setVisibility(View.VISIBLE);
            tvNextMonth.setClickable(true);
        }
    }

    private void setPrevAndNextMonthName(int month){
        String prevMonth;
        String nextMonth;
        if (month == 1) {
            prevMonth = DateUtils.getMonthNameByNumber(12);
            nextMonth = DateUtils.getMonthNameByNumber(month+1);
        } else if (month == 12) {
            prevMonth= DateUtils.getMonthNameByNumber(month-1);
            nextMonth = DateUtils.getMonthNameByNumber(1);
        } else {
            prevMonth= DateUtils.getMonthNameByNumber(month-1);
            nextMonth = DateUtils.getMonthNameByNumber(month+1);
        }
        tvPreviousMonth.setText("&lt;" + prevMonth.toUpperCase());
        tvNextMonth.setText(nextMonth.toUpperCase() + "&gt;");
    }

    void filldata(int num_month, int year) {
        counters.clear();
        cursor = db.getDataFromTable(DB.TABLE_COUNTERS);
        if (cursor.moveToFirst()) {
            do {
                int monthNumber = cursor.getInt(cursor.getColumnIndex(DB.COL_NUM_MONTH));
                int counterYear = cursor.getInt(cursor.getColumnIndex(DB.COL_YEAR));
                String counterName = cursor.getString(cursor.getColumnIndex(DB.COL_COUNT));
                String counterEdIzm = cursor.getString(cursor.getColumnIndex(DB.COL_COUNT_ED_IZM));
                String counterUniqIzm = cursor.getString(cursor.getColumnIndex(DB.COL_UNIQ_NUM));
                String counterPrev = cursor.getString(cursor.getColumnIndex(DB.COL_PREV_VALUE));
                String counterValue = cursor.getString(cursor.getColumnIndex(DB.COL_VALUE));
                String counterDiff = cursor.getString(cursor.getColumnIndex(DB.COL_DIFF));
                int typeId = cursor.getInt(cursor.getColumnIndex(DB.COL_TYPE_ID));
                String ident = cursor.getString(cursor.getColumnIndex(DB.COL_IDENT));
                String serial = cursor.getString(cursor.getColumnIndex(DB.COL_SERIAL));
                String isSent = cursor.getString(cursor.getColumnIndex(DB.COL_IS_SENT));

                if ((monthNumber == num_month) & (counterYear == year)) {

                    Counter counter = new Counter(monthNumber, counterYear, counterName, counterEdIzm,
                            counterUniqIzm, counterPrev, counterValue, counterDiff, typeId, ident, serial, isSent);
                    counter.setCounterNameExtended(getCounterNameExtended(typeId));

                    if (spinnerPosition == 0) {
                        counters.add(counter);
                    } else {
                        if (ident.equals(personalAccounts[spinnerPosition])) {
                            counters.add(counter);
                        }
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        count_list.setAdapter(count_adapter);
    }

    private void getCountersFromServer() {
        String line = "xxx";

        // Получить ВСЕ показания приборов
        try {
            line = server.get_counters(login, pass);

            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            db.del_table(db.TABLE_COUNTERS);

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                Parser_Get_Counters xpp = new Parser_Get_Counters();
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

    public class Parser_Get_Counters extends DefaultHandler { // Получение показаний приборов

        String login_to_DB = login, str_num_month, str_year = "", isSent = "";
        int num_month, year = 0, typeId;

        // реквзиты показаний из xml
        String str_count, str_ed_izm, str_uniq_num, str_prev_value, str_value, str_diff = "", ident = "", serial = "";

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (localName.toLowerCase().equals("period")) {
                str_num_month = atts.getValue("NumMonth").toString();
                str_year = atts.getValue("Year").toString();
                num_month = Integer.valueOf(str_num_month);
                year = Integer.valueOf(str_year);
            } else if (localName.toLowerCase().equals("metervalue")) {
                str_count = atts.getValue("Name");
                str_ed_izm = atts.getValue("Units");
                str_uniq_num = atts.getValue("MeterUniqueNum");
                str_prev_value = atts.getValue("PreviousValue");
                str_value = atts.getValue("Value");
                str_diff = atts.getValue("Difference");
                typeId = Integer.valueOf(atts.getValue("MeterTypeID"));
                ident = atts.getValue("Ident");
                serial = atts.getValue("FactoryNumber");
                isSent = atts.getValue("IsSended");

                db.addCount(login_to_DB, num_month, year, str_count, str_ed_izm, str_uniq_num, str_prev_value, str_value, str_diff, typeId, ident, serial, isSent);
            }
        }
    }

    void check_table() { // обновление отображения данных в таблице
        String name_month = DateUtils.getMonthNameByNumber(i_num_month);
        String name_btn_month = name_month + " " + String.valueOf(i_year);

        tvMonthTitle.setText(name_btn_month.toUpperCase());
        setPrevAndNextMonthName(i_num_month);

        filldata(i_num_month, i_year);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String rezult = data.getStringExtra("rezult");
        if (rezult.equals("ok_count")) {
            check_table();
            check_border();
        }
    }

    private String getCounterNameExtended(int counterTypeId) {
        String name = "";
        switch (counterTypeId) {
            case 1:
                name = "Холодная вода";
                break;
            case 2:
                name = "Горячая вода";
                break;
        }
        return name;
    }

    private void getParametersFromPrefs() {
        login = sPref.getString("login_push", "");
        pass = sPref.getString("pass_push", "");
        start_day = sPref.getString("start_day", "");
        end_day = sPref.getString("end_day", "");
        strPersonalAccs = sPref.getString("personalAccounts_pref", "");
        canGiveCounts = sPref.getString("can_count", "0");
    }

    private void setTechColors(View view) {
        TextView tvTech = view.findViewById(R.id.tv_tech);
        CardView cvDisp = view.findViewById(R.id.card_view_img_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
        }

        layoutTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TechSendActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void setScreenColorsToPrimary() {
        String hex = sPref.getString("hex_color", "23b6ed");
        tvPreviousMonth.setTextColor(Color.parseColor("#" + hex));
        tvNextMonth.setTextColor(Color.parseColor("#" + hex));
        tvAddLs.setTextColor(Color.parseColor("#" + hex));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews(View v) {
        tvPreviousMonth = v.findViewById(R.id.action_left);
        tvNextMonth = v.findViewById(R.id.action_rigth);
        tvMonthTitle = v.findViewById(R.id.month_head);
        count_list = v.findViewById(R.id.counters_list);
        tvCounterDates = v.findViewById(R.id.text_head);
        spinnerPersonalAcc = v.findViewById(R.id.spinner_accounts);
        layoutMain = v.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = v.findViewById(R.id.layout_no_internet);
        layoutTech = v.findViewById(R.id.layout_tech);
        btnNoInternetRefresh = v.findViewById(R.id.btn_no_internet_refresh);
        swipeRefreshLayout = v.findViewById(R.id.swipe_counters);
        tvEmpty = v.findViewById(R.id.tv_empty);
        tvAddLs = v.findViewById(R.id.tv_add_ls);
        tvAddLs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPersonalAccountActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                getActivity().finish();
            }
        });
    }

    @Override
    public void addCounter(int position) {
        if (canGiveCounts.equals("1")) {
            final Counter counter = counters.get(position);

            LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.add_counts_old, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(layout);
            TextView txt_number = layout.findViewById(R.id.txt_number);
            txt_number.setText(counter.name + ", " + counter.ed_izm);
            final EditText diff_count = layout.findViewById(R.id.text_Count);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                diff_count.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
            }
            builder.setPositiveButton(R.string.btn_tech_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {

                            String line = "";
                            try {
                                line = server.addCountToServer(login, pass, counter.uniq_num, diff_count.getText().toString());
                                if (line.equals("0")) {
                                    Snackbar.make(getView(), "Переданы не все параметры", Snackbar.LENGTH_LONG).show();
                                } else if (line.equals("1")) {
                                    Snackbar.make(getView(), "Не пройдена авторизация", Snackbar.LENGTH_LONG).show();
                                } else if (line.equals("2")) {
                                    Snackbar.make(getView(), "Не найден прибор у пользователя", Snackbar.LENGTH_LONG).show();
                                } else if (line.equals("3")) {
                                    Snackbar.make(getView(), "Передача показаний возможна только с 15 по 25 числа", Snackbar.LENGTH_LONG).show();
                                } else if (line.equals("5")) {

                                    double diff_text = StringUtils.convertStringToFloat(diff_count.getText().toString());
                                    double prev_text = StringUtils.convertStringToFloat(counter.prev.replaceAll(",", "."));
                                    double value_txt = diff_text - prev_text;
                                    counter.value = Double.toString(DigitUtils.round(diff_text, 2));
                                    counter.diff = Double.toString(DigitUtils.round(value_txt, 2));
                                    counter.isSent = "1";

                                    count_adapter = new CountersAdapter(counters, CountersFragment.this, hex);
                                    count_list.setAdapter(count_adapter);

                                    // показания переданы - запишем данные в БД
                                    db.addCount(login, counter.num_month, counter.year, counter.name, counter.ed_izm, counter.uniq_num, counter.prev, counter.value, counter.diff, counter.getTypeId(), counter.ident, counter.serialNumber, "1");

                                    Snackbar.make(getView(), "Показания переданы", Snackbar.LENGTH_LONG).show();
                                } else {
                                    DialogCreator.showErrorCustomDialog(getActivity(), line, hex);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });

            builder.setNegativeButton(R.string.btn_tech_no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            AlertDialog dialog = builder.create();
            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#" + hex));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
                }
            }
        } else {
            showCantGiveCountsDialog();
        }
    }

    private void showCantGiveCountsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Ошибка");
        builder.setMessage("Показания можно передавать с " + start_day + " по " + end_day + " число текущего месяца. Сегодня эта возможность отсутствует.");
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
            }
        }
    }
}