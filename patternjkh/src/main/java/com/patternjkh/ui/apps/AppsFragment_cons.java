package com.patternjkh.ui.apps;

import android.annotation.SuppressLint;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Switch;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Application;
import com.patternjkh.utils.StringUtils;
import com.patternjkh.utils.Utility;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class AppsFragment_cons extends Fragment {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";

    private String login, pass, id_account, name_owner, isCons, hex;

    private ListView work_list;
    private Switch chk_read;
    private AppCompatButton btnAddApp;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler;
    private ArrayList<Application> applications = new ArrayList<>();
    private WorkAdapter work_adapter;
    private DB db;
    private SharedPreferences sPref;
    private Server server = new Server(getActivity());

    public static AppsFragment_cons newInstance(String personalAccounts) {
        AppsFragment_cons fragment = new AppsFragment_cons();
        Bundle args = new Bundle();
        args.putString(PERSONAL_ACCOUNTS, personalAccounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        getParametersFromPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.apps_fragment_cons, container, false);
        initViews(view);

        hex = sPref.getString("hex_color", "23b6ed");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }
        
        work_adapter = new WorkAdapter(getActivity(), applications, name_owner, "1", id_account, login, pass);
        db = new DB(getContext());

        // Кнопка - Добавить заявку
        btnAddApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddApplicationActivity.class);
                intent.putExtra("login", login);
                intent.putExtra("pass", pass);
                intent.putExtra("isCons", "1");
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 4) {
                    applications.clear();
                    filldata_work();
                    work_adapter = new WorkAdapter(getActivity(), applications, name_owner, "1", id_account);
                    work_list.setAdapter(work_adapter);
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
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
                        Check_new_comm();
                        handler.sendEmptyMessage(4);
                    }
                }).start();
            }
        });

        return view;
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

    void filldata_work() {
        Cursor cursor = db.getDataOwner(DB.TABLE_APPLICATIONS, "", "");
        if (cursor.moveToFirst()) {
            do {
                String number = cursor.getString(cursor.getColumnIndex(DB.COL_NUMBER));
                String text = cursor.getString(cursor.getColumnIndex(DB.COL_TEXT));
                String tema = cursor.getString(cursor.getColumnIndex(DB.COL_TEMA));
                String date = cursor.getString(cursor.getColumnIndex(DB.COL_DATE));
                String address = cursor.getString(cursor.getColumnIndex(DB.COL_ADRESS));
                String flat = cursor.getString(cursor.getColumnIndex(DB.COL_FLAT));
                if (!flat.contains("кв")) {
                    flat = flat.replaceFirst("^0+(?!$)", "");
                    flat = "кв. " + flat;
                }
                String phone = cursor.getString(cursor.getColumnIndex(DB.COL_PHONE));
                String owner = cursor.getString(cursor.getColumnIndex(DB.COL_OWNER));
                String type_app = cursor.getString(cursor.getColumnIndex(DB.COL_TYPE));
                int isClosed = cursor.getInt(cursor.getColumnIndex(DB.COL_CLOSE));
                int isAnswered = cursor.getInt(cursor.getColumnIndex(DB.COL_IS_ANSWERED));
                int isRead = cursor.getInt(cursor.getColumnIndex(DB.COL_IS_READ));
                int isReadCons = cursor.getInt(cursor.getColumnIndex(DB.COL_IS_READ_CONS));

                if (chk_read.isChecked()) {
                    applications.add(new Application(number, text, login, isClosed, isRead, isAnswered, "", "",
                            tema, date, address + ", " + flat, phone, false, type_app, isReadCons));
                } else {
                    if (isAnswered == 0 && isClosed == 0) {
                        applications.add(new Application(number, text, login, isClosed, isRead, 0, "", "",
                                tema, date, address + ", " + flat, phone, false, type_app, isReadCons));
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        sortArray();
    }

    @Override
    public void onResume() {
        db.open();
        chk_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applications.clear();
                filldata_work();
                work_list.setAdapter(work_adapter);
            }
        });
        applications.clear();
        filldata_work();
        work_list.setAdapter(work_adapter);

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(updateAppReceiver, new IntentFilter("update_app"));

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(updateAppReceiver);
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

    private BroadcastReceiver updateAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Check_new_comm();
                    handler.sendEmptyMessage(4);
                }
            }).start();

        }
    };

    void Check_new_comm() {

        String line = "xxx";

        // получим ВСЕ заявки
        try {
            line = server.get_apps(login, pass, "All", isCons);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            db.del_table(db.TABLE_APPLICATIONS);
            db.del_table(db.TABLE_COMMENTS);


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
        String str_adress = "", str_flat = "", str_phone = "";
        int id_com = 0, id_app = 0;

        Parser_Get_Apps() {}

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (localName.toLowerCase().equals("row")) {
                str_name = atts.getValue("text").toString();
                try {
                    str_id = atts.getValue("id").toString();
                } catch (Exception e) {
                    str_id = atts.getValue("ID").toString();
                }
                str_owner = login; //atts.getValue("name").toString();
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
                    str_isRead = atts.getValue("IsReaded");
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
                    str_client = atts.getValue("CusName").toString();
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

                str_tema = atts.getValue("name").toString();
                str_app_date = atts.getValue("added").toString();
                if (str_app_date.length() > 10) {
                    str_app_date = str_app_date.substring(0, 10);
                }

                str_adress = atts.getValue("HouseAddress").toString();
                str_flat = atts.getValue("FlatNumber").toString();

                str_phone = atts.getValue("PhoneNum").toString();

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
                String isHidden = atts.getValue("isHidden");

                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, id_account, isHidden);

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

                    URL url = new URL(ComponentsInitializer.SITE_ADRR + server.DOWNLOAD_FilE + "id=" + fileID + "&tmode=1");
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

    private void getParametersFromPrefs() {
        login = sPref.getString("login_push", "");
        pass = sPref.getString("pass_push", "");
        id_account = sPref.getString("id_account_push", "");
        isCons = sPref.getString("isCons_push", "");
        name_owner = sPref.getString("_fio_", "");
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        btnAddApp.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
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
        chk_read.getThumbDrawable().setTintList(buttonStates);
        chk_read.getTrackDrawable().setTintList(buttonStates);
    }

    private void initViews(View view) {
        btnAddApp = view.findViewById(R.id.save_app);
        swipeRefreshLayout = view.findViewById(R.id.swipe_apps);
        work_list = view.findViewById(R.id.work_list);
        chk_read = view.findViewById(R.id.chk_close);
    }
}