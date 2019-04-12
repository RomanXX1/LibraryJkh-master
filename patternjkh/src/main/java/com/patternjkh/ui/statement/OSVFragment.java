package com.patternjkh.ui.statement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Saldo;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.Downloader;
import com.patternjkh.utils.FileUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.PermissionChecker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static com.patternjkh.utils.ToastUtils.showToast;

public class OSVFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";
    static final int REQUEST_STORAGE_FOR_LOAD = 11;
    static final int REQUEST_STORAGE_FOR_LOAD_FIRST = 12;

    private String hex, mPersonalAccounts, currentLs="", currentBillLink="", pdfBillFilename;
    private int i_num_month, i_year, max_i_num_month, max_i_year, min_i_num_month = 0, min_i_year = 0, currentMonth = 0, currentYear, currPos;
    private String[] mPersonalAccountsAndAll;

    private ListView saldo_list;
    private Button btn_cost, btnNoInternetRefresh;
    private Spinner spinnerPersonalAccounts;
    private TextView tvMonthTitle, tvPreviousMonth, tvNextMonth, tvEmpty, tvAddLs;
    //    private ImageView ivBillPdf;
    private LinearLayout layoutNoInternet, layoutTech;
    private ConstraintLayout layoutMain;
    private ProgressDialog dialog, dialogBill;

    private ArrayList<Saldo> saldos = new ArrayList<>();
    private SaldoAdapter saldo_adapter;
    private DB db;
    private SharedPreferences sPref;
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        db = new DB(getActivity());
        db.open();

        getParametersFromPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.osv_fragment, container, false);
        initViews(view);
        setTechColors(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

//        if (!Server.SITE_ADRR.contains("muprcmytishi")) {
//            ivBillPdf.setVisibility(View.GONE);
//        }

        initPersonalAccountsAndAll();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.choice_type, R.id.text_name_type, mPersonalAccountsAndAll);
        spinnerPersonalAccounts.setAdapter(spinnerAdapter);

        initCursorFromTableSaldoOrderByYearAndMonthAndInitMonths();

        if (savedInstanceState != null) {
            spinnerPersonalAccounts.setSelection(savedInstanceState.getInt("curr_spinner_pos"));
            i_num_month = savedInstanceState.getInt("curr_month");
            i_year = savedInstanceState.getInt("curr_year");
            Log.d("myLog", i_num_month + " " + i_year);
        }

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 3) {
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        if (dialogBill != null)
                            dialogBill.dismiss();
                    }

                    if (!PermissionChecker.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        PermissionChecker.requestPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_STORAGE_FOR_LOAD);
                    } else {
                        openPdfBill();
                    }
                } else if (msg.what == 11) {
                    showToastHere("Нет приложения для открытия данного файла");
                }
            }
        };

        return view;
    }

    @Override
    public void onResume() {

        if (ComponentsInitializer.SITE_ADRR.contains("komfortnew")) {
            btn_cost.setVisibility(View.GONE);
        }

        if (ConnectionUtils.hasConnection(getActivity())) {
            hideNoInternet();
            getData();
        } else {
            showNoInternet();
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curr_spinner_pos", currPos);
        outState.putInt("curr_month", currentMonth);
        outState.putInt("curr_year", currentYear);
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
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Загрузка ведомости...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            dialog.show();
            ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        layoutMain.setVisibility(View.GONE);
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
        if (mPersonalAccountsAndAll.length <= 1) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvAddLs.setVisibility(View.VISIBLE);
            layoutTech.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            tvAddLs.setVisibility(View.GONE);
            layoutTech.setVisibility(View.VISIBLE);
            layoutMain.setVisibility(View.VISIBLE);

            filldata(mPersonalAccountsAndAll[currPos], i_num_month, i_year);
            currentMonth = i_num_month;
            currentYear = i_year;

            if (mPersonalAccountsAndAll.length == 2) {
                currentLs = mPersonalAccountsAndAll[1];
            } else {
                currentLs = mPersonalAccountsAndAll[currPos];
            }

            getBillLinkByMonth();

            sortServices();
            calculateAndShowTotal();

            // вывод списка
            saldo_adapter = new SaldoAdapter(getActivity(), saldos);
            saldo_list.setAdapter(saldo_adapter);

            initClickListeners();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvPreviousMonth.setTextColor(Color.parseColor("#" + hex));
                tvNextMonth.setTextColor(Color.parseColor("#" + hex));
            }

            check_border();
        }
    }

    private void initClickListeners() {
        tvPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                do {
                    i_num_month = i_num_month - 1;
                    if (i_num_month == 0) {
                        i_num_month = 12;
                        i_year = i_year - 1;
                    }
                    currentMonth = i_num_month;
                    currentYear = i_year;
                } while (((i_num_month < min_i_num_month) & (i_year == min_i_year) |
                        (db.chechk_month_in_DB(i_num_month, i_year))
                ));

                getBillLinkByMonth();

                check_table();
                check_border();
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
                    currentMonth = i_num_month;
                    currentYear = i_year;
                } while (((i_num_month > max_i_num_month) & (i_year == max_i_year) |
                        (db.chechk_month_in_DB(i_num_month, i_year))
                ));

                getBillLinkByMonth();

                check_table();
                check_border();
            }
        });

        // Кнопочка - Оплатить
        btn_cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ComponentsInitializer.SITE_ADRR.equals("http://uk-gkh.org/newjkh/")) {
                    Intent intent = new Intent(getActivity(), CostNoPayActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                } else {
                    Intent intent = new Intent(getActivity(), CostActivity.class);
                    intent.putExtra("ls_to_choose", String.valueOf(spinnerPersonalAccounts.getSelectedItemPosition()));
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                }
            }
        });

        spinnerPersonalAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                currPos = position;
                if (mPersonalAccountsAndAll.length == 2) {
                    currentLs = mPersonalAccountsAndAll[1];
                } else {
                    currentLs = mPersonalAccountsAndAll[currPos];
                }
                updateStatementByPersonalAccountsSpinnerPosition();

                getBillLinkByMonth();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        ivBillPdf.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (currentLs.equals("Все")) {
//                    showToastHere("Выберите лс для загрузки квитанции");
//                } else {
//                    if (!PermissionChecker.isPermissionGranted(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        PermissionChecker.requestPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_STORAGE_FOR_LOAD_FIRST);
//                    } else {
//                        createDirectory();
//                    }
//                }
//            }
//        });
    }

    private void createDirectory() {
        dialogBill = new ProgressDialog(getActivity());
        dialogBill.setMessage("Загрузка файла квитанции...");
        dialogBill.setIndeterminate(true);
        dialogBill.setCancelable(false);
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            dialogBill.show();
            ProgressBar progressbar = dialogBill.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        pdfBillFilename = "Bill_" + String.valueOf(Math.abs(new Random().nextInt())) + ".pdf";
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File dir = new File(extStorageDirectory + "/Mytischi");
        if(!dir.exists())
        {
            dir.mkdir();
        }

        File folder = new File(extStorageDirectory + "/Mytischi");

        if (!folder.exists()) {
            folder.mkdir();
        }

        final File file = new File(folder, pdfBillFilename);
        try {
            file.createNewFile();
        } catch (IOException e1) {
            Logger.errorLog(OSVFragment.this.getClass(), e1.getMessage());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Downloader.DownloadFile(currentBillLink, file);

                handler.sendEmptyMessage(3);
            }
        }).start();
    }

    private void openPdfBill() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String firstPath = Environment.getExternalStorageDirectory() + "/Mytischi/" + pdfBillFilename;
        Uri fileURI = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".my.package.name.provider", new java.io.File(firstPath));
        String fullPath = FileUtils.getFileExtension(Environment.getExternalStorageDirectory() + "/Mytischi/" + pdfBillFilename);

        intent.setDataAndType(fileURI, myMime.getMimeTypeFromExtension(fullPath));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            handler.sendEmptyMessage(11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_FOR_LOAD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openPdfBill();
                }
                return;
            case REQUEST_STORAGE_FOR_LOAD_FIRST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createDirectory();
                }
        }

    }

    private void initPersonalAccountsAndAll(){
        if (TextUtils.isEmpty(mPersonalAccounts)){
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

    private void initCursorFromTableSaldoOrderByYearAndMonthAndInitMonths(){
        min_i_num_month = 0;
        min_i_year = 0;

        Cursor cursor = db.getDataFromTableByOrder(DB.TABLE_SALDO, DB.COL_YEAR + ", " + DB.COL_NUM_MONTH);
        if (cursor.moveToFirst()) {
            do {
                int year = cursor.getInt(cursor.getColumnIndex(DB.COL_YEAR));
                int numMonth = cursor.getInt(cursor.getColumnIndex(DB.COL_NUM_MONTH));

                String name_month = DateUtils.getMonthNameByNumber(numMonth);
                String name_btn_month = name_month + " " + String.valueOf(year);

                tvMonthTitle.setText(name_btn_month.toUpperCase());
                setPrevAndNextMonthName(numMonth);

                i_num_month = numMonth;
                i_year = year;
                Log.d("myLog", "480: " + i_year);

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
    }

    void filldata(String ls, int num_month, int year) {
        saldos.clear();
        Cursor cursor = db.getDataFromTable(DB.TABLE_SALDO);
        if (cursor.moveToFirst()) {
            do {
                int month = cursor.getInt(cursor.getColumnIndex(DB.COL_NUM_MONTH));
                int statementYear = cursor.getInt(cursor.getColumnIndex(DB.COL_YEAR));
                String lsToShow = cursor.getString(cursor.getColumnIndex(DB.COL_LS));
                String name = cursor.getString(cursor.getColumnIndex(DB.COL_USLUGA));
                String start = cursor.getString(cursor.getColumnIndex(DB.COL_START));
                String plus = cursor.getString(cursor.getColumnIndex(DB.COL_PLUS));
                String minus = cursor.getString(cursor.getColumnIndex(DB.COL_MINUS));
                String end = cursor.getString(cursor.getColumnIndex(DB.COL_END));
                String id  = cursor.getString(cursor.getColumnIndex(DB.COL_ID_USLUGA));

                if ((month == num_month) & (statementYear == year)) {
                    if (ComponentsInitializer.SITE_ADRR.contains("mytis")) {
                        if (name.equals("Услуги ЖКУ")) {
                            if (ls.equals(lsToShow)) {
                                saldos.add(new Saldo(month, statementYear, name, start, plus, minus, end, id));
                            }
                        }
                    } else {
                        if (lsToShow.equals(mPersonalAccountsAndAll[currPos])) {
                            saldos.add(new Saldo(month, statementYear, name, start, plus, minus, end, id));
                        }
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void getBillLinkByMonth() {
//        if (Server.SITE_ADRR.contains("muprcmytishi")) {
//            Cursor cursor = db.getDataFromTable(DB.TABLE_BILLS);
//            if (cursor.moveToFirst()) {
//                do {
//                    int year = cursor.getInt(cursor.getColumnIndex(DB.COL_YEAR));
//                    int month = cursor.getInt(cursor.getColumnIndex(DB.COL_NUM_MONTH));
//                    String link = cursor.getString(cursor.getColumnIndex(DB.COL_LINK));
//                    String ident = cursor.getString(cursor.getColumnIndex(DB.COL_IDENT));
//
//                    if (ident.equals(currentLs)) {
//                        if (year == currentYear && month == currentMonth) {
//                            currentBillLink = link;
//                        }
//                    }
//                } while (cursor.moveToNext());
//            }
//
//            if (currentBillLink.equals("http://uk-gkh.org/muprcmytishi/Files/FileStream.ashx?id=")) {
//                ivBillPdf.setVisibility(View.GONE);
//            } else {
//                ivBillPdf.setVisibility(View.VISIBLE);
//            }
//        }
//
//        Log.d("myLog", currentLs + " " + currentMonth + " " + currentYear);
//        Log.d("myLog", currentBillLink);
    }

    void check_table() { // обновление отображения данных в таблице

        String name_month = DateUtils.getMonthNameByNumber(i_num_month);
        String name_btn_month = name_month + " " + String.valueOf(i_year);

        // Новый дизайн
        tvMonthTitle.setText(name_btn_month.toUpperCase());
        setPrevAndNextMonthName(i_num_month);

        filldata(mPersonalAccountsAndAll[currPos], i_num_month, i_year);
        sortServices();
        calculateAndShowTotal();
        saldo_list.setAdapter(saldo_adapter);
    }

    void check_table(int currentMonth, int currentYear) {

        String name_month = DateUtils.getMonthNameByNumber(currentMonth);
        String name_btn_month = name_month + " " + String.valueOf(i_year);

        // Новый дизайн
        tvMonthTitle.setText(name_btn_month.toUpperCase());
        setPrevAndNextMonthName(currentMonth);

        filldata(mPersonalAccountsAndAll[currPos], currentMonth, currentYear);
        sortServices();
        calculateAndShowTotal();
        saldo_list.setAdapter(saldo_adapter);
    }

    void check_border() { // проверка на достижение границ по месяцам в БД
        if ((i_num_month == min_i_num_month)&(i_year == min_i_year)) {
            tvPreviousMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPreviousMonth.setVisibility(View.INVISIBLE);
            tvPreviousMonth.setClickable(false);
        } else {
            tvPreviousMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPreviousMonth.setVisibility(View.VISIBLE);
            tvPreviousMonth.setClickable(true);
        }
        if ((i_num_month == max_i_num_month)&(i_year == max_i_year)) {
            tvNextMonth.setVisibility(View.INVISIBLE);
            tvNextMonth.setClickable(false);
        } else {
            tvNextMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvNextMonth.setVisibility(View.VISIBLE);
            tvNextMonth.setClickable(true);
        }
    }

    void check_border(int currentMonth, int currentYear) { // проверка на достижение границ по месяцам в БД
        if ((currentMonth == min_i_num_month)&(currentYear == min_i_year)) {
            tvPreviousMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPreviousMonth.setVisibility(View.INVISIBLE);
            tvPreviousMonth.setClickable(false);
        } else {
            tvPreviousMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPreviousMonth.setVisibility(View.VISIBLE);
            tvPreviousMonth.setClickable(true);
        }
        if ((currentMonth == max_i_num_month)&(currentYear == max_i_year)) {
            tvNextMonth.setVisibility(View.INVISIBLE);
            tvNextMonth.setClickable(false);
        } else {
            tvNextMonth.setPaintFlags(tvPreviousMonth.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvNextMonth.setVisibility(View.VISIBLE);
            tvNextMonth.setClickable(true);
        }
    }

    private void setPrevAndNextMonthName(int month) {
        String prevMonth;
        String nextMonth;
        if (month == 1) {
            prevMonth = DateUtils.getMonthNameByNumber(12);
            nextMonth = DateUtils.getMonthNameByNumber(month + 1);
        } else if (month == 12) {
            prevMonth = DateUtils.getMonthNameByNumber(month - 1);
            nextMonth = DateUtils.getMonthNameByNumber(1);
        } else {
            prevMonth = DateUtils.getMonthNameByNumber(month - 1);
            nextMonth = DateUtils.getMonthNameByNumber(month + 1);
        }
        tvPreviousMonth.setText("< " + prevMonth.toUpperCase());
        tvNextMonth.setText(nextMonth.toUpperCase() + " >");
    }

    private void updateStatementByPersonalAccountsSpinnerPosition(){

//        initCursorFromTableSaldoOrderByYearAndMonthAndInitMonths();
        check_table(currentMonth, currentYear);
        check_border(currentMonth, currentYear);

        i_num_month = currentMonth;
        i_year = currentYear;
        Log.d("myLog", "672: " + i_year);
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

    private void calculateAndShowTotal() {
        BigDecimal totalStart = new BigDecimal("0.0");
        BigDecimal totalPlus = new BigDecimal("0.0");
        BigDecimal totalMinus = new BigDecimal("0.0");
        BigDecimal totalEnd = new BigDecimal("0.0");

        for (Saldo saldo : saldos) {
            if (!TextUtils.isEmpty(saldo.start)) {
                totalStart = totalStart.add(new BigDecimal(saldo.start));
            }
            if (!TextUtils.isEmpty(saldo.plus)) {
                totalPlus = totalPlus.add(new BigDecimal(saldo.plus));
            }
            if (!TextUtils.isEmpty(saldo.minus)) {
                totalMinus = totalMinus.add(new BigDecimal(saldo.minus));
            }
            if (!TextUtils.isEmpty(saldo.end)) {
                totalEnd = totalEnd.add(new BigDecimal(saldo.end));
            }
        }

        if (!ComponentsInitializer.SITE_ADRR.contains("mytish")) {
            if (saldos.size() == 0) {
                saldos.add(new Saldo(0,
                        0,
                        "По всем услугам",
                        totalStart.toString(),
                        totalPlus.toString(),
                        totalMinus.toString(),
                        totalEnd.toString(),
                        "0"));
            } else {
                saldos.add(new Saldo(saldos.get(0).num_month,
                        saldos.get(0).year,
                        "По всем услугам",
                        totalStart.toString(),
                        totalPlus.toString(),
                        totalMinus.toString(),
                        totalEnd.toString(),
                        "0"));
            }
        }

    }

    private void sortServices() {
        Collections.sort(saldos, new Comparator<Saldo>() {
            @Override
            public int compare(Saldo o1, Saldo o2) {
                return o1.usluga.compareTo(o2.usluga);
            }
        });
    }

    private void getParametersFromPrefs() {
        mPersonalAccounts = sPref.getString("personalAccounts_pref", "");
    }

    private void showToastHere(String title) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            showToast(getActivity(), title);
        }
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
        btn_cost.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        tvAddLs.setTextColor(Color.parseColor("#" + hex));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews(View v) {
        spinnerPersonalAccounts = v.findViewById(R.id.spinner_personal_account);
        btn_cost = v.findViewById(R.id.btn_cost);
        saldo_list = v.findViewById(R.id.counters_list);
        tvMonthTitle = v.findViewById(R.id.month_head);
        tvPreviousMonth = v.findViewById(R.id.action_left);
        tvNextMonth = v.findViewById(R.id.action_rigth);
        layoutMain = v.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = v.findViewById(R.id.layout_no_internet);
        layoutTech = v.findViewById(R.id.layout_tech);
        btnNoInternetRefresh = v.findViewById(R.id.btn_no_internet_refresh);
        tvEmpty = v.findViewById(R.id.tv_empty);
        tvAddLs = v.findViewById(R.id.tv_add_ls);
//        ivBillPdf = v.findViewById(R.id.iv_osv_pdf);
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