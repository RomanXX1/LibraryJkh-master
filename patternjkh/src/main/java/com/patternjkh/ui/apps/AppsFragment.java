package com.patternjkh.ui.apps;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Application;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.StringUtils;
import com.patternjkh.utils.Utility;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AppsFragment extends android.support.v4.app.Fragment {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";
    private static final int ID_APP_CREATION = 101;

    private String login, pass, id_account, isCons, hex, mPersonalAccounts;

    private ListView work_list;
    private ProgressDialog dialog;
    private Switch chk_close;
    private Button mSendApplicationButton, btnNoInternetRefresh;
    private TextView tvDataEmpty, tvAddLs, tvNoLs;
    private ConstraintLayout layoutMain;
    private LinearLayout layoutNoInternet;
    private SwipeRefreshLayout swipeRefreshLayout;

    private HashMap<String, Integer> hashMapUpdatedApps = new HashMap<>();
    private Handler handler, handlerSwipeEnd;
    private ArrayList<Application> applications = new ArrayList<>();
    private WorkAdapter work_adapter;
    private SharedPreferences sPref;
    private Cursor cursor;
    private DB db;
    private Server server = new Server(getActivity());

    public static AppsFragment newInstance(String personalAccounts) {
        AppsFragment fragment = new AppsFragment();
        Bundle args = new Bundle();
        args.putString(PERSONAL_ACCOUNTS, personalAccounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPersonalAccounts = getArguments().getString(PERSONAL_ACCOUNTS);
        }

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);

        getParametersFromPrefs();
        parseMapFromPrefs();

        db = new DB(getContext());
        db.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.apps_fragment, container, false);

        hex = sPref.getString("hex_color", "");

        initViews(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        setTechColors(view);
        work_adapter = new WorkAdapter(getActivity(), applications, "Пользователь", "0", id_account);
        work_list.setDividerHeight(0);
        work_list.setAdapter(work_adapter);

        // Кнопка - Добавить за явку
        mSendApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddApplicationActivity.class);
                intent.putExtra("login", login);
                intent.putExtra("pass", pass);
                intent.putExtra("isCons", "0");
                intent.putExtra("personalAccounts", mPersonalAccounts);
                startActivityForResult(intent, ID_APP_CREATION);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        tvDataEmpty.setVisibility(View.GONE);
        work_list.setVisibility(View.VISIBLE);
        chk_close.setVisibility(View.VISIBLE);
        mSendApplicationButton.setVisibility(View.VISIBLE);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 4) {
                    applications.clear();
                    filldata_work();
                    work_list.setAdapter(work_adapter);
                    handlerSwipeEnd.sendEmptyMessage(0);
                } else {
                    applications.clear();
                    filldata_work();
                    work_list.setAdapter(work_adapter);
                    handlerSwipeEnd.sendEmptyMessage(0);
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        dialog.dismiss();
                    }
                }
            }
        };

        handlerSwipeEnd = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } else if (msg.what == 1) {
                    if (ConnectionUtils.hasConnection(getActivity())) {
                        getAppsFromServer();
                        hideNoInternet();
                    } else {
                        showNoInternet();
                    }
                }
            }
        };

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handlerSwipeEnd.sendEmptyMessage(1);
                    }
                }).start();
            }
        });

        chk_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applications.clear();
                filldata_work();
                work_list.setAdapter(work_adapter);
            }
        });

        if (ConnectionUtils.hasConnection(getActivity())) {
            hideNoInternet();
            getAppsFromServer();
        } else {
            showNoInternet();
        }

        return view;
    }

    @Override
    public void onResume() {
        parseMapFromPrefs();

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateAppReceiver, new IntentFilter("update_app"));

        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        StringBuilder map_apps = new StringBuilder();
        for (Integer key : Utility.map.keySet()) {
            map_apps.append(key.toString());
            map_apps.append("=");
            map_apps.append(Utility.map.get(key));
            map_apps.append("&");
        }
        String updatedApps = new String(map_apps);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("map_apps", updatedApps);
        ed.commit();
    }

    private void getAppsFromServer() {
        if (!mPersonalAccounts.equals("")) {
            tvAddLs.setVisibility(View.GONE);
            tvNoLs.setVisibility(View.GONE);
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Загрузка заявок...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Check_new_comm();
                    handler.sendEmptyMessage(101);
                }
            }).start();
        } else {
            tvAddLs.setVisibility(View.VISIBLE);
            tvNoLs.setVisibility(View.VISIBLE);
        }
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(getActivity())) {
                    getAppsFromServer();
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

    void filldata_work() {
        cursor = db.getDataOwner(DB.TABLE_APPLICATIONS, "", "");
        if (cursor.moveToFirst()) {
            do {
                int id_number = cursor.getColumnIndex("number");
                int id_text = cursor.getColumnIndex("text");
                int id_close = cursor.getColumnIndex("close");
                int id_isRead = cursor.getColumnIndex("isRead");
                int id_isReadCons = cursor.getColumnIndex("isReadCons");
                int id_tema = cursor.getColumnIndex(DB.COL_TEMA);
                int id_date = cursor.getColumnIndex(DB.COL_DATE);

                int id_owner = cursor.getColumnIndex(DB.COL_OWNER);
                String owner = cursor.getString(id_owner);

                int id_adress = cursor.getColumnIndex(DB.COL_ADRESS);
                String adress = cursor.getString(id_adress);
                int id_flat = cursor.getColumnIndex(DB.COL_FLAT);
                String flat = cursor.getString(id_flat);
                if (!flat.contains("кв")) {
                    flat = flat.replaceFirst("^0+(?!$)", "");
                    flat = "кв. " + flat;
                }
                int id_phone = cursor.getColumnIndex(DB.COL_PHONE);
                String phone = cursor.getString(id_phone);
                String type_app = cursor.getString(cursor.getColumnIndex(DB.COL_TYPE));

                if (chk_close.isChecked()) {
                    applications.add(new Application(cursor.getString(id_number), cursor.getString(id_text),
                            login,
                            cursor.getInt(id_close), cursor.getInt(id_isRead), 0, "", "",
                            cursor.getString(id_tema), cursor.getString(id_date), adress + ", " + flat, phone, checkIfUpdated(cursor.getString(id_number)), type_app, cursor.getInt(id_isReadCons)));
                } else {
                    if (cursor.getInt(id_close) == 0) {
                        applications.add(new Application(cursor.getString(id_number), cursor.getString(id_text),
                                login,
                                cursor.getInt(id_close), cursor.getInt(id_isRead), 0, "", "",
                                cursor.getString(id_tema), cursor.getString(id_date), adress + ", " + flat, phone, checkIfUpdated(cursor.getString(id_number)), type_app, cursor.getInt(id_isReadCons)));
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        sortArray();
    }

    private void sortArray() {
        // Сортировка по номеру заявки
        Collections.sort(applications, Collections.reverseOrder(new Comparator<Application>() {
            @Override
            public int compare(Application o1, Application o2) {
                Integer num1 = o1.getNumberInt();
                Integer num2 = o2.getNumberInt();
                return num2.compareTo(num1);
            }
        }));
    }

    private boolean checkIfUpdated(String number) {
        boolean isUpdated = false;
        if (number != null && !number.equals("")) {
            int prevNumberComments = 0;
            int currNumberComments = 0;
            if (Utility.map.containsKey(StringUtils.convertStringToInteger(number))) {
                currNumberComments = Utility.map.get(StringUtils.convertStringToInteger(number));
            }
            if (hashMapUpdatedApps.containsKey(number)) {
                prevNumberComments = hashMapUpdatedApps.get(number);
            }
            if (Utility.updatedApps.contains(number)) {
                isUpdated = true;
            } else {
                if (currNumberComments == 0 || prevNumberComments == 0) {
                    isUpdated = false;
                } else {
                    if (currNumberComments > prevNumberComments) {
                        isUpdated = true;
                        Utility.updatedApps.add(number);
                    }
                }
            }
        }

        return isUpdated;
    }

    private void getParametersFromPrefs() {
        login = sPref.getString("login_push", "");
        pass = sPref.getString("pass_push", "");
        id_account = sPref.getString("id_account_push", "");
        isCons = sPref.getString("isCons_push", "");
    }

    private void parseMapFromPrefs() {
        String map = sPref.getString("map_apps", "");
        String[] lines = map.split("&");
        if (!map.equals("")) {
            for (int i = 0; i < lines.length; i++) {
                String[] values = lines[i].split("=");
                hashMapUpdatedApps.put(values[0], StringUtils.convertStringToInteger(values[1]));
            }
        }
    }

    private BroadcastReceiver updateAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Check_new_comm();
                }
            }).start();
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(updateAppReceiver);
    }


    void Check_new_comm() {

        String line = "xxx";

        // получим ВСЕ заявки
        try {
            line = server.get_apps(login, pass, "All", isCons);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            db.del_table(DB.TABLE_APPLICATIONS);
            db.del_table(DB.TABLE_COMMENTS);

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                Parser_Get_Apps xpp = new Parser_Get_Apps();
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SAXParser sp = factory.newSAXParser();
                XMLReader reader = sp.getXMLReader();
                reader.setContentHandler(xpp);
                reader.parse(is);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class Parser_Get_Apps extends DefaultHandler { // Получение заявок

        String str_id = "", str_name = "", str_owner = "", str_closeApp = "", str_isRead, str_isReadCons;
        String str_isAnswered, str_client = "", str_customer_id = "", str_tema = "", str_app_date = "";
        String str_text = "", str_date = "", str_id_author = "", str_author = "", str_id_account = "";
        String str_adress = "", str_flat = "", str_phone = "", id_acc = "";
        int id_com = 0, id_app = 0;

        Parser_Get_Apps() {

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            if (localName.toLowerCase().equals("row")) {
                str_name = atts.getValue("text");
                str_id = atts.getValue("ID");
                str_owner = login;
                int closeApp = 0;
                try {
                    str_closeApp = atts.getValue("isActive");
                    closeApp = StringUtils.convertStringToInteger(str_closeApp);
                } catch (Exception e) {
                    closeApp = 0;
                }
                if (closeApp == 1) {
                    closeApp = 0;

                } else if (closeApp == 0) {
                    closeApp = 1;
                }
                int isRead = 0;
                try {
                    str_isRead = atts.getValue("IsReadedByClient");
                    isRead = StringUtils.convertStringToInteger(str_isRead);
                } catch (Exception e) {
                    isRead = 0;
                }
                int isReadCons = 0;
                try {
                    str_isReadCons = atts.getValue("IsReaded");
                    isReadCons = StringUtils.convertStringToInteger(str_isReadCons);
                } catch (Exception e) {
                    isReadCons = 0;
                }
                try {
                    str_client = atts.getValue("CusName");
                } catch (Exception e) {
                    str_client = "";
                }
                int isAnswered = 1;
                try {
                    str_isAnswered = atts.getValue("IsAnswered");
                    isAnswered = StringUtils.convertStringToInteger(str_isAnswered);
                } catch (Exception e) {
                    isAnswered = 1;
                }

                str_tema = atts.getValue("name");
                str_app_date = atts.getValue("added");
                if (str_app_date.length() > 10) {
                    str_app_date = str_app_date.substring(0, 10);
                }

                str_adress = atts.getValue("HouseAddress");
                str_flat = atts.getValue("FlatNumber");

                str_phone = atts.getValue("PhoneNum");

                String type_app = atts.getValue("id_type").toString();

                db.addApp(str_id, str_name, str_owner, closeApp, isRead, isAnswered, str_client, str_customer_id, str_tema, str_app_date, str_adress, str_flat, str_phone, type_app, isReadCons);

            } else if (localName.toLowerCase().equals("comm")) {
                try {
                    id_com = Integer.valueOf(atts.getValue("id"));
                } catch (Exception e) {
                    id_com = Integer.valueOf(atts.getValue("ID"));
                }
                id_app = Integer.valueOf(atts.getValue("id_request"));
                str_text = atts.getValue("text");
                str_date = atts.getValue("added");
                str_id_author = atts.getValue("id_Author");
                str_author = atts.getValue("Name");
                str_id_account = atts.getValue("id_account");
                if (atts.getValue("id_MobileAccount").equals("")) {
                    id_acc = "0";
                } else {
                    id_acc = atts.getValue("id_MobileAccount");
                }
                String isHidden = atts.getValue("isHidden");

                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, id_acc, isHidden);

            } else if (localName.toLowerCase().equals("file")) {

                String fileID = atts.getValue("FileID");
                String number = atts.getValue("RequestID");
                String file_name = atts.getValue("FileName");
                String date_time = atts.getValue("DateTime");

                // скачаем иконку приложения
                byte[] data = new byte[1024];
                byte[] bArray = null;
                File file = null;
                try {

                    URL url = new URL(ComponentsInitializer.SITE_ADRR + Server.DOWNLOAD_FilE + "id=" + fileID + "&tmode=1");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream input = new BufferedInputStream(url.openStream());
                    File sdDir = android.os.Environment.getExternalStorageDirectory();
                    OutputStream output = new FileOutputStream(sdDir + "/" + file_name);

                    int count = 0;
                    while ((count = input.read(data)) > 0) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    Bitmap photo = BitmapFactory.decodeFile(sdDir + "/" + file_name);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bArray = bos.toByteArray();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                db.addFotoWithDate(fileID, String.valueOf(id_app), bArray, "", file_name, date_time);
            }
        }
    }

    private void setTechColors(View view) {
        TextView tvTech = view.findViewById(R.id.tv_tech);
        CardView cvDisp = view.findViewById(R.id.card_view_img_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
        }

        LinearLayout layout = view.findViewById(R.id.layout_tech);
        layout.setOnClickListener(new View.OnClickListener() {
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
        mSendApplicationButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
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
        chk_close.getThumbDrawable().setTintList(buttonStates);
        chk_close.getTrackDrawable().setTintList(buttonStates);
        tvAddLs.setTextColor(Color.parseColor("#" + hex));
        btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews(View view) {
        work_list = view.findViewById(R.id.work_list);
        chk_close = view.findViewById(R.id.chk_close);
        tvDataEmpty = view.findViewById(R.id.tv_apps_empty);
        tvAddLs = view.findViewById(R.id.tv_add_ls);
        tvNoLs = view.findViewById(R.id.tv_empty);
        mSendApplicationButton = view.findViewById(R.id.save_app);
        swipeRefreshLayout = view.findViewById(R.id.swipe_apps);
        layoutMain = view.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = view.findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = view.findViewById(R.id.btn_no_internet_refresh);
    }
}