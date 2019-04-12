package com.patternjkh.ui.statement;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Saldo;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CostFragment extends Fragment implements OnCostServiceClickListener {

    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";
    private static final String APP_SETTINGS = "global_settings";

    private String login = "", pass = "", mail = "", mPersonalAccounts, hex, fio = "", str_date, toExtractPays = "";
    private double service;
    private int currSpinnerPosition, selectionSpinner;
    private String[] mPersonalAccountsAndAll;
    private boolean isAccountsHasDebts;

    private EditText etTotalSum;
    private Spinner spinnerPersonalAccounts;
    private LinearLayout layout;
    private RecyclerView rvServices;
    private View layoutMain;
    private TextView tvItog, tvService;
    private Button btnPay, btnHistory;

    private DB db;
    private volatile ArrayList<Saldo> saldos = new ArrayList<>();
    private ArrayList<Integer> saldosCheckedServices = new ArrayList<>();
    private ArrayList<String> receiptItems = new ArrayList<>();
    private SharedPreferences sPref;
    private CostServicesAdapter adapter;

    public static CostFragment newInstance(String personalAccounts) {
        CostFragment fragment = new CostFragment();
        Bundle args = new Bundle();
        args.putString(PERSONAL_ACCOUNTS, personalAccounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = new DB(context);
        db.open();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPersonalAccounts = getArguments().getString(PERSONAL_ACCOUNTS);
        }

        // Если ДомЖилСервис - покажем задолженность
        if (ComponentsInitializer.SITE_ADRR.contains("dgservicnew")) {
            new SumDebtAsyncTask().execute();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cost_fragment, container, false);
        initViews(view);

        getParametersFromPrefs();

        if (getActivity().getIntent() != null) {
            if (!getActivity().getIntent().getStringExtra("ls_to_choose").equals("main")) {
                selectionSpinner = StringUtils.convertStringToInteger(getActivity().getIntent().getStringExtra("ls_to_choose"));
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        initPersonalAccountsAndAll();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.choice_type, R.id.text_name_type, mPersonalAccountsAndAll);
        spinnerPersonalAccounts.setAdapter(spinnerAdapter);
        spinnerPersonalAccounts.setSelection(selectionSpinner);
        if (isAccountsHasDebts) {
            spinnerPersonalAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currSpinnerPosition = position;
                    getFromDb();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            changeEdittext();
        }

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPersonalAccountsAndAll.length > 2 && spinnerPersonalAccounts.getSelectedItemPosition() == 0) {
                    showChooseLsDialog();
                } else {
                    boolean isEmailValid = StringUtils.checkIsEmailValid(mail);
                    if (!isEmailValid) {
                        showEnterEmailDialog();
                    } else {
                        startCostActivity();
                    }
                }
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HistoryOsvOldActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                getActivity().finish();
            }
        });

        etTotalSum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateAndShowTotal();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showChooseLsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Для совершения оплаты укажите лицевой счет");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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

    private void showEnterEmailDialog() {
        LayoutInflater layoutinflater = LayoutInflater.from(getActivity());
        View promptUserView = layoutinflater.inflate(R.layout.dialog_enter_email, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(promptUserView);

        final EditText newEmail = promptUserView.findViewById(R.id.et_dialog_email);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newEmail.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        }

        alertDialogBuilder.setTitle("Для перехода к оплате введите Ваш e-mail:");

        alertDialogBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!newEmail.getText().toString().equals("")) {
                    if (StringUtils.checkIsEmailValid(newEmail.getText().toString())) {
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putString("mail_pref", newEmail.getText().toString());
                        ed.commit();

                        startCostActivity();
                    } else {
                        Snackbar.make(layoutMain, "Неверный email", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(layoutMain, "Введите email", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            alertDialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#" + hex));
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
            }
        }
    }

    private void startCostActivity() {
        boolean toShareSum = false;
        if (StringUtils.convertStringToDouble(etTotalSum.getText().toString()) != calculateSumOfServicesMinus().doubleValue()) {
            toShareSum = true;
        }

        final double startSum = calculateSumOfServicesMinus().doubleValue();

        HashMap<Double, Saldo> newSaldos = new HashMap<>();
        for (int i = 0; i < saldos.size(); i++) {
            if (saldosCheckedServices.contains(i)) {
                Saldo saldo = saldos.get(i);
                if (!saldo.end.equals("0.00")) {
                    double coef = StringUtils.convertStringToDouble(saldo.end) / startSum;
                    newSaldos.put(coef, saldo);
                }
            }
        }

        if (toShareSum) {
            int size = newSaldos.size();
            int i = 0;
            final double finalCurrSum = StringUtils.convertStringToDouble(etTotalSum.getText().toString());
            double currSum = finalCurrSum;
            ArrayList<String> names = new ArrayList<>();
            for (Saldo saldo : saldos) {
                names.add(saldo.usluga);
            }
            for(Map.Entry<Double, Saldo> entry : newSaldos.entrySet()) {
                double coef = entry.getKey();
                Saldo saldo = entry.getValue();
                if (i + 1 != size) {
                    long addedSum = (long)(new BigDecimal(coef*finalCurrSum).setScale(2, RoundingMode.HALF_UP).doubleValue() * 100);
                    if (addedSum != 0 && saldosCheckedServices.contains(names.indexOf(saldo.usluga))) {
                        receiptItems.add(saldo.usluga + ";" + addedSum + ";" + "1.00" + ";" + addedSum + ";" + "none" + ";" + saldo.id);
                    }

                    currSum -= addedSum;
                } else {
                    long addedSum = (long)(new BigDecimal(currSum).setScale(2, RoundingMode.HALF_UP).doubleValue() * 100);
                    if (addedSum != 0 && saldosCheckedServices.contains(names.indexOf(saldo.usluga))) {
                        receiptItems.add(saldo.usluga + ";" + addedSum + ";" + "1.00" + ";" + addedSum + ";" + "none" + ";" + saldo.id);
                    }
                }
                i++;
            }
        } else {
            ArrayList<String> names = new ArrayList<>();
            for (Saldo saldo : saldos) {
                names.add(saldo.usluga);
            }
            for(Map.Entry<Double, Saldo> entry : newSaldos.entrySet()) {
                Saldo saldo = entry.getValue();
                long addedSum = (long)(new BigDecimal(saldo.end).setScale(2, RoundingMode.HALF_UP).doubleValue() * 100);
                if (addedSum != 0 && saldosCheckedServices.contains(names.indexOf(saldo.usluga))) {
                    receiptItems.add(saldo.usluga + ";" + addedSum + ";" + "1.00" + ";" + addedSum + ";" + "none" + ";" + saldo.id);
                }
            }
        }
        long serviceInCops = (long)(new BigDecimal(tvService.getText().toString()).setScale(2, RoundingMode.HALF_UP).doubleValue() * 100);
        receiptItems.add("Сервисный сбор" + ";" + serviceInCops + ";" + "1.00" + ";" + serviceInCops + ";" + "none" + ";" + "serv");

        Intent intent = new Intent(getActivity(), PayServiceActivity.class);
        intent.putExtra("login", login);
        intent.putExtra("pass", pass);
        String sum_oplata = "0";
        sum_oplata = tvItog.getText().toString().replaceAll(",", ".");
        Log.d("myLog", sum_oplata);
        if (StringUtils.convertStringToDouble(sum_oplata) > 0) {
            intent.putExtra("sum", sum_oplata);
            intent.putStringArrayListExtra("items", receiptItems);
            startActivity(intent);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
        } else {
            Snackbar.make(layoutMain, "Введите сумму оплаты", Snackbar.LENGTH_LONG).show();
        }
    }

    private void initPersonalAccountsAndAll() {
        if (TextUtils.isEmpty(mPersonalAccounts)) {
            mPersonalAccountsAndAll = new String[1];
            mPersonalAccountsAndAll[0] = "Все";
        } else {
            String[] personalAccountsSeparate = mPersonalAccounts.split(",");

            mPersonalAccountsAndAll = new String[personalAccountsSeparate.length + 1];
            mPersonalAccountsAndAll[0] = "Все";

            for (int i = 0; i < personalAccountsSeparate.length; i++) {
                mPersonalAccountsAndAll[i + 1] = personalAccountsSeparate[i];
            }
        }
    }

    private void getFromDb() {
        int lastPayMonth = DateUtils.getCurrentMonth();
        int lastPayYear = DateUtils.getCurrentYear();
        String lastPayId = "";

        int numberMonthLast = -1;
        int yearLast = -1;
        saldos.clear();

        Cursor saldoCursor = db.getDataFromTable(DB.TABLE_SALDO, DB.COL_YEAR + ", " + DB.COL_NUM_MONTH);
        if (saldoCursor.moveToLast()) {
            do {

                int i_num_month = saldoCursor.getInt(saldoCursor.getColumnIndex(DB.COL_NUM_MONTH));
                int i_year = saldoCursor.getInt(saldoCursor.getColumnIndex(DB.COL_YEAR));

                // берем только последние данные за месяц и год
                if ((numberMonthLast == -1 && yearLast == -1) || (numberMonthLast == i_num_month && yearLast == i_year)) {
                    numberMonthLast = i_num_month;
                    yearLast = i_year;

                    String ls = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_LS));
                    String name = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_USLUGA));
                    String start = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_START));
                    String plus = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_PLUS));
                    String minus = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_MINUS));
                    String end = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_END));
                    String id  = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_ID));

                    if (mPersonalAccountsAndAll[currSpinnerPosition].equals(ls)) {
                        saldos.add(new Saldo(i_num_month, i_year, name, start, plus, minus, end, id));
                    }
                    lastPayMonth = i_num_month;
                    lastPayYear = i_year;
                    lastPayId = id;
                    layout.setVisibility(View.GONE);
                } else {
                    break;
                }
            } while (saldoCursor.moveToPrevious());
        }
        saldoCursor.close();

        saldosCheckedServices.clear();
        for (int i = 0; i < saldos.size(); i++) {
            saldosCheckedServices.add(i);
        }

        if (toExtractPays.equals("0")) {
            BigDecimal totalEnd = calculateSumOfServicesMinus();

            saldos.clear();
            saldos.add(new Saldo(lastPayMonth, lastPayYear, "Услуги ЖКХ", "", "", "", String.valueOf(totalEnd), lastPayId));
        }

        rvServices.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CostServicesAdapter(CostFragment.this, saldos, hex);
        rvServices.setAdapter(adapter);

        changeEdittext();
    }

    private void changeEdittext() {
        BigDecimal totalEnd = calculateSumOfServicesMinus();

        etTotalSum.setText(String.valueOf(totalEnd));
        calculateAndShowTotal();
    }

    private BigDecimal calculateSumOfServicesMinus() {
        BigDecimal totalEnd = new BigDecimal("0.0");
        for (int i = 0; i < saldos.size(); i++) {
            if (saldosCheckedServices.contains(i)) {
                if (!TextUtils.isEmpty(saldos.get(i).end)) {
                    totalEnd = totalEnd.add(new BigDecimal(saldos.get(i).end));
                }
            }
        }
        totalEnd.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return totalEnd;
    }

    private void calculateAndShowTotal() {

        String total = etTotalSum.getText().toString();
        if (total.equals("")) {
            total = "0";
        }
        service = StringUtils.convertStringToFloat(String.valueOf(StringUtils.convertStringToFloat(total) / 0.992 - StringUtils.convertStringToFloat(total)));
        service = new BigDecimal(service).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        tvService.setText(new BigDecimal(service).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
        tvItog.setText(new BigDecimal((StringUtils.convertStringToFloat(total) / 0.992)).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
    }

    @Override
    public void onServiceClicked(int position, boolean checked) {
        if (!checked) {
            if (saldosCheckedServices.contains(position))
                saldosCheckedServices.remove(Integer.valueOf(position));
        } else {
            if (!saldosCheckedServices.contains(position))
                saldosCheckedServices.add(position);
        }

        changeEdittext();
    }

    private void getParametersFromPrefs() {
        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        login = sPref.getString("login_pref", "");
        pass = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");
        mail = sPref.getString("mail_pref", "");
        fio = sPref.getString("_fio_", "");
        toExtractPays = sPref.getString("to_extract_pays", "0");
        isAccountsHasDebts = sPref.getBoolean("is_acc_has_debts", true);
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        btnPay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        etTotalSum.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnHistory.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
    }

    // Для ДомЖилСервис - расчет задолженности - вывод сообщения
    class SumDebtAsyncTask extends AsyncTask<Void, Void, Double> {

        @Override
        protected Double doInBackground(Void... params) {
            SharedPreferences sPrefs = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
            str_date = sPrefs.getString("date_debt", "");
            return Double.valueOf(sPrefs.getString("sum_debt", "0"));
        }

        @Override
        protected void onPostExecute(Double result) {
            super.onPostExecute(result);

            if (result > 0) {

                if (result < 10000) {

                    LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.sum_debt_10000, null);
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    builder.setView(layout);

                    TextView txt_fio = layout.findViewById(R.id.txt_fio);
                    txt_fio.setText(fio);

                    TextView txt_date = layout.findViewById(R.id.txt_date);
                    txt_date.setText(str_date);

                    TextView txt_sum = layout.findViewById(R.id.txt_sum);
                    txt_sum.setText(" " + String.valueOf(result));

                    builder.setPositiveButton(R.string.go_on,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            });

                    android.support.v7.app.AlertDialog dialog = builder.create();
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        dialog.show();
                    }

                } else {

                    LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.sum_debt_over_10000, null);
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                    builder.setView(layout);

                    TextView txt_fio = layout.findViewById(R.id.txt_fio);
                    txt_fio.setText(fio);

                    TextView txt_date = layout.findViewById(R.id.txt_date);
                    txt_date.setText(str_date);

                    TextView txt_sum = layout.findViewById(R.id.txt_sum);
                    txt_sum.setText(" - за жилищно-коммунальные услуги: " + String.valueOf(result) + " руб.");

                    builder.setPositiveButton(R.string.btn_next,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View layout = inflater.inflate(R.layout.sum_debt_over_10000_2, null);
                                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                                    builder.setView(layout);

                                    builder.setPositiveButton(R.string.go_on,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });

                                    android.support.v7.app.AlertDialog dialog_2 = builder.create();
                                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                                        dialog_2.show();
                                    }

                                }
                            });

                    android.support.v7.app.AlertDialog dialog = builder.create();
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        dialog.show();
                    }
                }
            }
        }
    }

    private void initViews(View view) {
        tvItog = view.findViewById(R.id.tv_cost_mytishi_itog);
        tvService = view.findViewById(R.id.tv_cost_mytishi_service);
        rvServices = view.findViewById(R.id.recyclerView);
        btnPay = view.findViewById(R.id.btn_cost);
        spinnerPersonalAccounts = view.findViewById(R.id.spinner_personal_account);
        etTotalSum = view.findViewById(R.id.txt_total_sum);
        btnHistory = view.findViewById(R.id.btn_osv_history);
        layoutMain = view.findViewById(R.id.layout_root);
        layout = view.findViewById(R.id.linearLayout3);
    }
}
