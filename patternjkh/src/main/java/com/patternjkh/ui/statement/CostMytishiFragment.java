package com.patternjkh.ui.statement;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.data.Saldo;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.MoneyUtils;
import com.patternjkh.utils.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.tinkoff.acquiring.sdk.Money;

public class CostMytishiFragment extends Fragment implements OnCostServiceClickListener, OnServiceSumChanged {

    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";
    private static final String APP_SETTINGS = "global_settings";

    private String login = "", pass = "", mail = "", mPersonalAccounts, hex, toExtractPays;
    private double service;
    private String[] mPersonalAccountsAndAll;
    private int currSpinnerPos, selectionSpinner;
    private boolean isAccountsHasDebts;

    private Button btnPay, btnHistory, btnNoInternetRefresh;
    private Spinner spinnerPersonalAccounts;
    private RecyclerView rvServices;
    private View mainLayout;
    private TextView tvItog, tvService, tvTotalSum;
    private ScrollView layoutMain;
    private LinearLayout layoutNoInternet, layoutTech;
    private ProgressDialog dialog;

    private DB db;
    private volatile ArrayList<Saldo> saldos = new ArrayList<>();
    private ArrayList<Integer> saldosCheckedServices = new ArrayList<>();
    private ArrayList<String> receiptItems = new ArrayList<>();
    private SharedPreferences sPref;
    private CostServiceAdapterMytishi adapter;

    public static CostMytishiFragment newInstance(String personalAccounts) {
        CostMytishiFragment fragment = new CostMytishiFragment();
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
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cost_fragment_mytishi, container, false);
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
        setTechColors(view);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ConnectionUtils.hasConnection(getActivity())) {
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
                } else {
                    DialogCreator.showInternetErrorDialog(getActivity(), hex);
                }
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HistoryOsvActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isAccountsHasDebts) {
            if (ConnectionUtils.hasConnection(getActivity())) {
                hideNoInternet();
                getDataFromDb();
            } else {
                showNoInternet();
            }
        } else {
            initPersonalAccountsAndAll();

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.choice_type, R.id.text_name_type, mPersonalAccountsAndAll);
            spinnerPersonalAccounts.setAdapter(spinnerAdapter);
            spinnerPersonalAccounts.setSelection(selectionSpinner);

            changeEdittext();
        }
    }

    @Override
    public void onServiceChanged() {
        changeEdittext();
        calculateAndShowTotal();
    }

    private void getDataFromDb() {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Загрузка деталей оплаты...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        initPersonalAccountsAndAll();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.choice_type, R.id.text_name_type, mPersonalAccountsAndAll);
        spinnerPersonalAccounts.setAdapter(spinnerAdapter);
        spinnerPersonalAccounts.setSelection(selectionSpinner);
        spinnerPersonalAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currSpinnerPos = position;
                new SaldoAsyncTask().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        layoutTech.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(getActivity())) {
                    getDataFromDb();
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
        layoutTech.setVisibility(View.VISIBLE);
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
                        Snackbar.make(mainLayout, "Неверный email", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(mainLayout, "Введите email", Snackbar.LENGTH_LONG).show();
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
        HashMap<String, String> saldosToSend = adapter.getAllValues();
        ArrayList<String> names = new ArrayList<>();
        for (Saldo saldo : saldos) {
            names.add(saldo.usluga);
        }
        for(Map.Entry<String, String> entry : saldosToSend.entrySet()) {
            String saldoName = entry.getKey();
            String saldoSum = entry.getValue();
            if (!saldoSum.equals("0.00") && saldosCheckedServices.contains(names.indexOf(getStringBeforeChar(saldoName, "-")))) {
                receiptItems.add(saldoName + ";" + Money.ofRubles(StringUtils.convertStringToDouble(saldoSum)).getCoins() + ";" + "1.00" + ";" + Money.ofRubles(StringUtils.convertStringToDouble(saldoSum)).getCoins() + ";" + "none");
            }
        }
        long serviceInCops = Money.ofRubles(StringUtils.convertStringToDouble(tvService.getText().toString())).getCoins();
        receiptItems.add("Сервисный сбор" + ";" + serviceInCops + ";" + "1.00" + ";" + serviceInCops + ";" + "none");

        Intent intent = new Intent(getActivity(), PayServiceActivity.class);
        intent.putExtra("login", login);
        intent.putExtra("pass", pass);
        String ident = mPersonalAccountsAndAll[spinnerPersonalAccounts.getSelectedItemPosition()];
        if (ident.toLowerCase().equals("все")) {
            ident = mPersonalAccountsAndAll[1];
        }
        intent.putExtra("ident", ident);
        String sum_oplata = tvItog.getText().toString().replaceAll(",", ".");
        if (StringUtils.convertStringToDouble(sum_oplata) > 0) {
            intent.putExtra("sum", sum_oplata);
            intent.putStringArrayListExtra("items", receiptItems);
            startActivity(intent);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
        } else {
            Snackbar.make(mainLayout, "Введите сумму оплаты", Snackbar.LENGTH_LONG).show();
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

    class SaldoAsyncTask extends AsyncTask<Void, Void, Void> {

        int lastPayMonth = DateUtils.getCurrentMonth();
        int lastPayYear = DateUtils.getCurrentYear();
        String lastPayId = "";

        @Override
        protected Void doInBackground(Void... params) {

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
                        String id  = saldoCursor.getString(saldoCursor.getColumnIndex(DB.COL_ID_USLUGA));

                        double uslugi = StringUtils.convertStringToFloat(end);
                        if (mPersonalAccountsAndAll[currSpinnerPos].equals(ls)) {
                            saldos.add(new Saldo(i_num_month, i_year, name, start, plus, minus, new BigDecimal(uslugi).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString(), id));
                        }

                        lastPayMonth = i_num_month;
                        lastPayYear = i_year;
                        lastPayId = id;
                    } else {
                        break;
                    }
                } while (saldoCursor.moveToPrevious());
            }
            saldoCursor.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            saldosCheckedServices.clear();

            for (int i = 0; i < saldos.size(); i++) {
                saldosCheckedServices.add(i);
            }

            rvServices.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter = new CostServiceAdapterMytishi(CostMytishiFragment.this, CostMytishiFragment.this, saldos, hex);
            rvServices.setAdapter(adapter);

            if (toExtractPays.equals("0")) {
                BigDecimal totalEnd = calculateSumOfServicesMinus();

                saldos.clear();
                saldos.add(new Saldo(lastPayMonth, lastPayYear, "Услуги ЖКХ", "", "", "", String.valueOf(totalEnd), lastPayId));

                saldosCheckedServices.clear();

                for (int i = 0; i < saldos.size(); i++) {
                    saldosCheckedServices.add(i);
                }

                adapter = new CostServiceAdapterMytishi(CostMytishiFragment.this, CostMytishiFragment.this, saldos, hex);
                rvServices.setAdapter(adapter);
            }

            changeEdittext();

            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }
    }

    private void changeEdittext() {
        BigDecimal totalEnd = calculateSumOfServicesMinus();

        if (totalEnd != null) {
            tvTotalSum.setText(String.valueOf(totalEnd));
        } else {
            tvTotalSum.setText("0.0");
        }

        calculateAndShowTotal();

    }

    private BigDecimal calculateSumOfServicesMinus() {
        BigDecimal totalEnd = new BigDecimal("0.0");
        for (int i = 0; i < saldos.size(); i++) {
            String saldoId = saldos.get(i).id;
            if (saldoId.equals("") || saldoId.equals("-")) {
                saldoId = "1234";
            }
            String serviceSum = adapter.getValue(saldos.get(i).usluga + "-" + saldoId);
            if (saldosCheckedServices.contains(i)) {
                if (!TextUtils.isEmpty(saldos.get(i).end)) {
                    totalEnd = totalEnd.add(new BigDecimal(serviceSum));
                }
            }
        }
        totalEnd.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return totalEnd;
    }

    private void calculateAndShowTotal() {

        String total = tvTotalSum.getText().toString();
        if (total.equals("")) {
            total = "0";
        }

        if (ComponentsInitializer.SITE_ADRR.contains("muprcmytishi")) {
            service = StringUtils.convertStringToFloat(String.valueOf(StringUtils.convertStringToFloat(total) / 0.992 - StringUtils.convertStringToFloat(total)));
            service = new BigDecimal(service).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
            tvService.setText(new BigDecimal(service).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());

            // Считаем итог через копейки - наиболее точно (и протестировано unit-тестами)
            long totalCops = Money.ofRubles(new BigDecimal(service).setScale(2, BigDecimal.ROUND_HALF_EVEN)).getCoins() + Money.ofRubles(StringUtils.convertStringToDouble(total)).getCoins();
            tvItog.setText(MoneyUtils.convertCopsToStringRublesFormatted(totalCops));
        } else if (ComponentsInitializer.SITE_ADRR.contains("klimovsk12")) {
            service = (StringUtils.convertStringToFloat(total) / 100.0f) * 1.5;
            service = new BigDecimal(service).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
            tvService.setText(String.valueOf(service));

            // Считаем итог через копейки - наиболее точно (и протестировано unit-тестами)
            long totalCops = MoneyUtils.convertRublesToCops(service) + MoneyUtils.convertRublesToCops(total);
            tvItog.setText(MoneyUtils.convertCopsToStringRublesFormatted(totalCops));
        }
    }

    @Override
    public void onServiceClicked(int position, boolean checked) {
        if (!checked) {
            if (saldosCheckedServices.contains(position))
                saldosCheckedServices.remove(new Integer(position));
        } else {
            if (!saldosCheckedServices.contains(position))
                saldosCheckedServices.add(position);
        }

        changeEdittext();
    }

    static String getStringBeforeChar(String value, String a) {
        int posA = value.indexOf(a);
        if (posA == -1) {
            return "";
        }
        return value.substring(0, posA);
    }

    private void getParametersFromPrefs() {
        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        login = sPref.getString("login_pref", "");
        pass = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");
        mail = sPref.getString("mail_pref", "");
        toExtractPays = sPref.getString("to_extract_pays", "0");
        isAccountsHasDebts = sPref.getBoolean("is_acc_has_debts", true);
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

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        btnPay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnHistory.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews(View view) {
        tvItog = view.findViewById(R.id.tv_cost_mytishi_itog);
        tvService = view.findViewById(R.id.tv_cost_mytishi_service);
        rvServices = view.findViewById(R.id.recyclerView);
        btnPay = view.findViewById(R.id.btn_cost);
        spinnerPersonalAccounts = view.findViewById(R.id.spinner_personal_account);
        tvTotalSum = view.findViewById(R.id.txt_total_sum);
        layoutTech = view.findViewById(R.id.layout_tech);
        btnHistory = view.findViewById(R.id.btn_osv_history);
        mainLayout = view.findViewById(R.id.layout_root);
        layoutNoInternet = view.findViewById(R.id.layout_no_internet);
        layoutMain = view.findViewById(R.id.layout_with_internet);
        btnNoInternetRefresh = view.findViewById(R.id.btn_no_internet_refresh);
    }
}
