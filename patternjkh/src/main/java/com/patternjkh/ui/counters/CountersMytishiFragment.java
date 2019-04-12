package com.patternjkh.ui.counters;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.CounterMytishi;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.CounterValueExtractor;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static android.content.Context.MODE_PRIVATE;

public class CountersMytishiFragment extends Fragment implements OnCounterMytishiClickListener {

    private String start_day, end_day, login, pass, strPersonalAccs, hex, currentMonth, currentDate, canGiveCounts = "0";
    private String[] personalAccounts;
    private int spinnerPosition = 0;

    private RecyclerView count_list;
    private Spinner spinnerPersonalAcc;
    private Button btnNoInternetRefresh;
    private LinearLayout layoutNoInternet, layoutTech, layoutMain;
    private TextView tvCounterDates, tvMonthTitle, tvEmpty, tvAddLs;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CountersMytishiAdapter count_adapter;
    private ArrayList<CounterMytishi> counters = new ArrayList<>();
    private Cursor cursor;
    private DB db;
    private Server server = new Server(getActivity());
    private SharedPreferences sPref;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.counters_fragment, container, false);

        sPref = getActivity().getSharedPreferences("global_settings", MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        db = new DB(getActivity());
        db.open();

        getParametersFromPrefs();
        initViews(v);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColors();
        }
        setTechColors(v);

        initPersonalAccountsAndAll();
        initClickListeners();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.choice_type, R.id.text_name_type, personalAccounts);
        spinnerPersonalAcc.setAdapter(spinnerAdapter);

        if (savedInstanceState != null) {
            spinnerPersonalAcc.setSelection(savedInstanceState.getInt("curr_spinner_pos"));
        }

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
                } else if (msg.what == 1) {
                    count_adapter.notifyDataSetChanged();
                    Snackbar.make(getView(), "Показания переданы", Snackbar.LENGTH_LONG).show();
                } else if (msg.what == 100) {
                    Snackbar.make(getView(), "Переданы не все параметры", Snackbar.LENGTH_LONG).show();
                } else if (msg.what == 101) {
                    Snackbar.make(getView(), "Не пройдена авторизация", Snackbar.LENGTH_LONG).show();
                } else if (msg.what == 102) {
                    Snackbar.make(getView(), "Не найден прибор у пользователя", Snackbar.LENGTH_LONG).show();
                } else if (msg.what == 103) {
                    Snackbar.make(getView(), "Передача показаний возможна только с " + start_day + " по " + end_day + " числа", Snackbar.LENGTH_LONG).show();
                } else if (msg.what == 404) {
                    String error = "";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    DialogCreator.showErrorCustomDialog(getActivity(), error, hex);
                }
            }
        };

        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curr_spinner_pos", spinnerPosition);
    }

    @Override
    public void onResume() {

        int year = DateUtils.getCurrentYear();
        int month = DateUtils.getCurrentMonth();
        currentMonth = DateUtils.getMonthNameByNumber(month);

        currentDate = currentMonth + " " + year;
        tvMonthTitle.setText(currentDate);

        if (ConnectionUtils.hasConnection(getActivity())) {
            hideNoInternet();
            getData();
        } else {
            showNoInternet();
        }

        super.onResume();
    }

    private void getCountersFromServer() {
        String line = "xxx";

        // Получить ВСЕ показания приборов
        try {
            line = server.getCountersMytishi(login, pass);
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
                Logger.errorLog(CountersMytishiFragment.this.getClass(), e.getMessage());
            }
        } catch (Exception e) {
            Logger.errorLog(CountersMytishiFragment.this.getClass(), e.getMessage());
        }
    }

    public class Parser_Get_Counters_Mytishi extends DefaultHandler {

        private String ident="", units="", name="", uniqueNum="", factoryNum="", periodDate="", value="", isSent="", sendError="";
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

            filldata();

            showIsUserCanGiveCounts();

            count_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            count_adapter = new CountersMytishiAdapter(getActivity(), counters, this, hex);
            count_list.setAdapter(count_adapter);
        }
    }

    private void initClickListeners() {

        spinnerPersonalAcc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                spinnerPosition = position;
                filldata();
                count_list.setAdapter(count_adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

    void filldata() {

        ArrayList<CounterMytishi> countersFromDb = new ArrayList<>();

        counters.clear();
        cursor = db.getDataFromTable(DB.TABLE_COUNTERS_MYTISHI);
        if (cursor.moveToFirst()) {

            do {
                int typeId = cursor.getInt(cursor.getColumnIndex(DB.COL_TYPE_ID));
                String ident = cursor.getString(cursor.getColumnIndex(DB.COL_IDENT));
                String units = cursor.getString(cursor.getColumnIndex(DB.COL_COUNT_ED_IZM));
                String name = cursor.getString(cursor.getColumnIndex(DB.COL_COUNT));
                String uniqueNum = cursor.getString(cursor.getColumnIndex(DB.COL_UNIQ_NUM));
                String factoryNum = cursor.getString(cursor.getColumnIndex(DB.COL_FACTORY_NUM));
                String periodDate = cursor.getString(cursor.getColumnIndex(DB.COL_DATE));
                String value = cursor.getString(cursor.getColumnIndex(DB.COL_VALUE));
                String isSent = cursor.getString(cursor.getColumnIndex(DB.COL_IS_SENT));
                String sendError = cursor.getString(cursor.getColumnIndex(DB.COL_SEND_ERROR));

                CounterMytishi counter = new CounterMytishi(ident, units, name, uniqueNum, typeId, factoryNum, periodDate, value, isSent, getCounterNameExtended(typeId), sendError);

                if (spinnerPosition == 0) {
                    countersFromDb.add(counter);
                } else {
                    if (ident.equals(personalAccounts[spinnerPosition])) {
                        countersFromDb.add(counter);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.sort(countersFromDb, new Comparator<CounterMytishi>() {
            @Override
            public int compare(CounterMytishi o1, CounterMytishi o2) {
                return o1.getUniqueName().compareTo(o2.getUniqueName());
            }
        });

        String values = "";
        String unique = "";
        for (int i = 0; i < countersFromDb.size(); i++) {
            CounterMytishi item = countersFromDb.get(i);
            CounterMytishi nextItem = null;
            if (i + 1 < countersFromDb.size()) {
                nextItem = countersFromDb.get(i+1);
            }

            if (unique.equals("")) {
                unique = item.getUniqueName();
            }

            if (nextItem != null) {
                if (nextItem.getUniqueName().equals(item.getUniqueName())) {
                    values += item.getPeriodDate() + "---" + item.getValue() + "---" + item.getSendError() + ";";
                } else {
                    values += item.getPeriodDate() + "---" + item.getValue() + "---" + item.getSendError() + ";";
                    counters.add(new CounterMytishi(item.getIdent(), item.getUnits(), item.getName(), item.getUniqueName(), item.getTypeId(), item.getFactoryNum(), "", "", item.getIsSent(), item.getNameExtended(), values, item.getSendError()));
                    values = "";
                }
            } else {
                values += item.getPeriodDate() + "---" + item.getValue() + "---" + item.getSendError();
                counters.add(new CounterMytishi(item.getIdent(), item.getUnits(), item.getName(), item.getUniqueName(), item.getTypeId(), item.getFactoryNum(), "", "", item.getIsSent(), item.getNameExtended(), values, item.getSendError()));
            }
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

    @Override
    public void selectCounter(int position) {
        CounterMytishi counter = counters.get(position);

        String counterName = "";

        if (!counter.getNameExtended().equals("")) {
            counterName = counter.getNameExtended();
        } else {
            counterName = counter.name;
        }
        if (!counter.getUnits().equals("")) {
            counterName = counterName + ", " + counter.getUnits();
        }

        Intent intent = new Intent(getActivity(), CounterMytishiActivity.class);
        intent.putExtra("counter_id", counter.uniqueName);
        intent.putExtra("counter_ls", counter.ident);
        intent.putExtra("counter_name", counterName);
        intent.putExtra("counter_factory", counter.factoryNum);
        intent.putExtra("counter_units", counter.units);
        intent.putExtra("counter_type", counter.typeId);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
    }

    @Override
    public void addCount(int position) {
        if (ConnectionUtils.hasConnection(getActivity())) {
            if (canGiveCounts.equals("1")) {
                final CounterMytishi counter = counters.get(position);

                ArrayList<String[]> threeLastCounterValues = CounterValueExtractor.getArrayFromStringValues(counter.getValues());
                String lastCounterValue = "0,00";
                if (threeLastCounterValues != null && threeLastCounterValues.size() > 0) {
                    lastCounterValue = threeLastCounterValues.get(0)[1];
                }

                Intent intent = new Intent(getActivity(), AddCounterValueActivity.class);
                intent.putExtra("count_name", counter.getName());
                intent.putExtra("count_measure", counter.getUnits());
                intent.putExtra("count_factory_num", counter.getFactoryNum());
                intent.putExtra("count_prev_val", lastCounterValue);
                intent.putExtra("count_ls", counter.getIdent());
                intent.putExtra("count_unique", counter.getUniqueName());
                intent.putExtra("count_type_id", counter.getTypeId());
                intent.putExtra("count_values", counter.getValues());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            } else {
                showCantGiveCountsDialog();
            }
        } else {
            DialogCreator.showInternetErrorDialog(getActivity(), hex);
        }
    }

    private void showCantGiveCountsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Ошибка");
        builder.setMessage("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");
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

    private void setColors() {
        String hex = sPref.getString("hex_color", "23b6ed");
        tvAddLs.setTextColor(Color.parseColor("#" + hex));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews(View v) {
        TextView tvPreviousMonth = v.findViewById(R.id.action_left);
        TextView tvNextMonth = v.findViewById(R.id.action_rigth);
        tvPreviousMonth.setVisibility(View.GONE);
        tvNextMonth.setVisibility(View.GONE);
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
}
