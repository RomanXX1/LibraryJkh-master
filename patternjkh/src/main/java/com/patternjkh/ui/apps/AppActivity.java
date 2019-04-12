package com.patternjkh.ui.apps;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Comment;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.StringUtils;
import com.patternjkh.utils.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AppActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private int prevCommNumber;
    private String number, owner, id_account, tema, adress, hex, phone, type_app;

    private TextView txt_tema, date_time, txt_close_comm, txt_files_comm, tvAppAddress, tvAppType, tvAppPhone;
    private RecyclerView comment_list_main;
    private EditText mCommentEditText;
    private ImageView mCommentSendImageView;

    private Handler handler;
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<String> dates = new ArrayList<>();
    private DB db;
    private Server server;
    private CommentsAdapter com_adapter_main;
    private SharedPreferences sPref;
    private AppStyleManager appStyleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        appStyleManager = AppStyleManager.getInstance(this, hex);

        db = new DB(this);
        db.open();

        initViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        Bundle extras = getIntent().getExtras();
        number = extras.getString("number");
        owner = extras.getString("owner");
        tema = extras.getString("tema");
        id_account = extras.getString("id_account");
        phone = extras.getString("phone");
        adress = extras.getString("adress");
        tvAppAddress.setText(adress);
        tvAppPhone.setText(phone);

        // Тип заявки
        type_app = extras.getString("type_app");
        tvAppType.setText(db.get_name_type(type_app));

        setToolbar(extras);

        txt_tema.setText(tema);

        server = new Server(this);

        CommentsFromDB(number, comments);

        com_adapter_main = new CommentsAdapter(comments);

        LinearLayoutManager layoutManagerCatalog = new LinearLayoutManager(this);
        comment_list_main.setLayoutManager(layoutManagerCatalog);
        comment_list_main.setAdapter(com_adapter_main);
        comment_list_main.scrollToPosition(com_adapter_main.getItemCount() - 1);

        updateMap(comments.size());

        mCommentSendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Здесь добавить комментарий
                if (mCommentEditText.getText().toString().equals("")) {
                    // Ничего не будем делать
                } else {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    prevCommNumber++;
                    updateMap(prevCommNumber);

                    add_comment_end(number, mCommentEditText.getText().toString(), id_account, owner);

                    DB db = new DB(AppActivity.this);
                    db.open();
                    db.set_app_is_opened(number);
                    // Обновление комментариев в списке - поискать другой способ
                    mCommentEditText.setText("");
                    ArrayList<Comment> comments2 = new ArrayList<>();
                    CommentsFromDB(number, comments2);

                    // Обновим в общем списке
                    com_adapter_main = new CommentsAdapter(comments2);
                    comment_list_main.setAdapter(com_adapter_main);
                    comment_list_main.scrollToPosition(com_adapter_main.getItemCount() - 1);
                }
            }
        });

        txt_close_comm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close_app();
            }
        });

        txt_files_comm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppActivity.this, MakePhotoActivity.class);
                intent.putExtra("number", number);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {

                    // Обновим отображаемую позицию ListView
                    comment_list_main.scrollToPosition(com_adapter_main.getItemCount() - 1);
                }
            }
        };

        prevCommNumber = Utility.map.get(StringUtils.convertStringToInteger(number));
        Utility.map.remove(StringUtils.convertStringToInteger(number));

        db.open();
//        new ApplicationsCounterAsyncTask().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void setToolbar(Bundle extras) {
        String simpleName = sPref.getString("simple_app", "");
        String isPush = extras.getString("isPush");
        if (isPush == null) {
            isPush = "no";
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(simpleName + " №" + number);
        setSupportActionBar(toolbar);
        if (!isPush.equals("yes")) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateComments();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(updateAppReceiver, new IntentFilter("update_app"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        StringBuilder apps = new StringBuilder();
        for (int i = 0; i < Utility.updatedApps.size(); i++) {
            apps.append(Utility.updatedApps.get(i));
        }
        String updatedApps = new String(apps);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("updated_apps", updatedApps);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateAppReceiver);
    }

    private BroadcastReceiver updateAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Utility.updatedApps.remove(number);

            updateComments();
        }
    };

    private void updateComments() {
        for (int i = 0; i < comments.size(); i++) {
            dates.add(comments.get(i).getOwner());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Get_Comm_by_id(number);
            }
        }).start();
    }

    private void updateMap(int value) {
        Utility.map.put(StringUtils.convertStringToInteger(number), comments.size());

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

    private void close_app() {
        // Закрыть заявку
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_close_app, null);

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AppActivity.this);
        builder.setView(layout);
        builder.setTitle("Закрыть заявку?");
        builder.setPositiveButton(R.string.close_comm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Процедура закрытия заявки

                        EditText txtComment = layout.findViewById(R.id.txtComment);
                        RatingBar rating = layout.findViewById(R.id.rating);

                        String _text = txtComment.getText().toString();
                        String _mark = String.valueOf(rating.getRating());
                        _mark = create_mark(_mark);
                        String _rezult_close = "";
                        close_app_end(number, _text, _mark, _rezult_close);

                        if (_rezult_close.equals("xxx")) {
                            // Сообщение о потере интернета
                        } else {
                            // Удалим заявку из БД
                            DB db = new DB(AppActivity.this);
                            db.open();
                            db.set_app_is_closed(number);
                        }

                        Get_Comm_by_id(number);
                        CommentsFromDB(number, comments);
                        com_adapter_main.notifyDataSetChanged();
                    }

                    private String create_mark(String _mark) {
                        String rezult = "0";
                        switch (_mark) {
                            case "0.5":
                                rezult = "1";
                                break;
                            case "1.0":
                                rezult = "2";
                                break;
                            case "1.5":
                                rezult = "3";
                                break;
                            case "2.0":
                                rezult = "4";
                                break;
                            case "2.5":
                                rezult = "5";
                                break;
                            case "3.0":
                                rezult = "6";
                                break;
                            case "3.5":
                                rezult = "7";
                                break;
                            case "4.0":
                                rezult = "8";
                                break;
                            case "4.5":
                                rezult = "9";
                                break;
                            case "5.0":
                                rezult = "10";
                                break;
                        }
                        return rezult;
                    }

                });

        builder.setNegativeButton(R.string.btn_tech_no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        android.support.v7.app.AlertDialog dialog = builder.create();
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }
    }

    void close_app_end(String _id, String _text, String _mark, String _rezult_close) {

        try {
            String line = new close_app_end_class().execute(_id, _text, _mark).get();
            _rezult_close = line;
        } catch (Exception e) {
            e.printStackTrace();
            _rezult_close = "xxx";
        }

    }

    class close_app_end_class extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros_close = ComponentsInitializer.SITE_ADRR + server.CLOSE_APP + "&" + "reqID=" + params[0] + "&text=" + params[1] + "&mark=" + params[2];
                zapros_close = zapros_close.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros_close);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                line = "xxx";
            }

            return line;
        }

    }

    void CommentsFromDB(String number, ArrayList<Comment> comments) {
        db = new DB(this);
        db.open();
        Cursor cursor = db.getDataByPole(DB.TABLE_COMMENTS, DB.COL_ID_APP, number, "_id");
        comments.clear();
        boolean its_first = true;
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(DB.COL_ID)));
                String text = cursor.getString(cursor.getColumnIndex(DB.COL_TEXT));
                String date = cursor.getString(cursor.getColumnIndex(DB.COL_DATE));
                Boolean isHidden = cursor.getString(cursor.getColumnIndex(DB.COL_IS_HIDDEN)).equals("1");
                String id_account = cursor.getString(cursor.getColumnIndex(DB.COL_ID_ACCOUNT));

                boolean isAuthor = id_account.equals(this.id_account);

                if (its_first) {
                    if (date_time.getText().toString().equals("")) {
                        date_time.setText(date);
                    }
                    its_first = false;
                    if (!isHidden) {
                        comments.add(new Comment(id, text, date, "Я", isAuthor, isHidden));
                    }
                } else {
                    String name_author = cursor.getString(cursor.getColumnIndex(DB.COL_AUTHOR));
                    if (isAuthor) {
                        name_author = "Я";
                    }
                    if (!isHidden) {
                        comments.add(new Comment(id, text, date, name_author, isAuthor, isHidden));
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Получение комментариев по заявке
    void Get_Comm_by_id(String id_number) {
        try {
            String line = new Get_Comms_by_id().execute(id_number).get();

            if (!line.equals("xxx")) {
                try {
                    BufferedReader br = new BufferedReader(new StringReader(line));
                    InputSource is = new InputSource(br);
                    Parser_Get_Comm xpp = new Parser_Get_Comm(id_number);
                    SAXParserFactory factory = SAXParserFactory.newInstance();

                    SAXParser sp = factory.newSAXParser();
                    XMLReader reader = sp.getXMLReader();
                    reader.setContentHandler(xpp);
                    reader.parse(is);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Get_Comms_by_id extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = ComponentsInitializer.SITE_ADRR + server.COMM_BY_ID + "id=" + params[0];
                zapros = zapros.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    public class Parser_Get_Comm extends DefaultHandler {

        int id_com = 0, id_app = 0;
        String str_text = "", str_date = "", str_id_author = "", str_author = "", id_account = "", isHidden = "", id;

        Parser_Get_Comm(String _id) {
            id = _id;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts) {
            db.open();

            if (localName.toLowerCase().equals("comm")) {
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
                if (atts.getValue("id_MobileAccount").equals("")) {
                    id_account = "0";
                } else {
                    id_account = atts.getValue("id_MobileAccount");
                }
                isHidden = atts.getValue("isHidden");
                Boolean hidden = isHidden.equals("1");

                // Если есть сообщение с такой же датой и серверным текстом "передана специалисту",
                // то не добавлять этот коммент в список.
                if (!dates.contains(str_date)) {
                    if (!str_text.contains("передана специалисту") && !hidden) {
                        com_adapter_main.add(new Comment(id_app, str_text, str_date, str_author, false, hidden));
                        prevCommNumber++;
                        updateMap(prevCommNumber);
                    }
                }
                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, id_account, isHidden);
            }

            handler.sendEmptyMessage(0);
        }
    }

    void add_comment_end(String _id, String _text, String _author_id, String _author_name) {
        try {
            String line = new add_comment_end_class().execute(_id, _text, _author_id, _author_name).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class add_comment_end_class extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = ComponentsInitializer.SITE_ADRR + server.ADD_COMM + "&" + "reqID=" + params[0] + "&text=" + params[1];
                zapros = zapros.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                line = "xxx";
            }

            if (!line.equals("xxx")) {
                // Запишем новый комментарий в БД
                DB db = new DB(AppActivity.this);
                db.open();
                db.addCom(Integer.valueOf(line), Integer.valueOf(params[0]), params[1], DateUtils.getDate(), params[2], params[3], params[2], "0");
            }

            return line;
        }
    }

    class ApplicationsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return db.getCountApplications(false);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

//            SharedPreferences.Editor ed = sPref.edit();
//            ed.putString("count_apps", String.valueOf(result));
//            ed.apply();
//            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), NewAppWidget.class));
//            NewAppWidget myWidget = new NewAppWidget();
//            myWidget.onUpdate(ActivityApp.this, AppWidgetManager.getInstance(ActivityApp.this),ids);
        }
    }

    private void setScreenColorsToPrimary() {
        mCommentSendImageView.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_app_arrow_forward));
        txt_close_comm.setCompoundDrawablesWithIntrinsicBounds(null, appStyleManager.changeDrawableColorLighter(R.drawable.ic_app_close), null, null);
        txt_files_comm.setCompoundDrawablesWithIntrinsicBounds(null, appStyleManager.changeDrawableColor(R.drawable.ic_app_file), null, null);
        txt_close_comm.setTextColor(Color.parseColor("#" + hex));
        txt_files_comm.setTextColor(Color.parseColor("#" + hex));
    }

    private void initViews() {
        date_time = findViewById(R.id.date_time);
        txt_tema = findViewById(R.id.tema);
        comment_list_main = findViewById(R.id.list_comments);
        mCommentEditText = findViewById(R.id.txt_comm);
        mCommentSendImageView = findViewById(R.id.img_send_comm);
        txt_close_comm = findViewById(R.id.txt_close_com);
        txt_files_comm = findViewById(R.id.txt_files_com);
        tvAppAddress = findViewById(R.id.tv_app_address);
        tvAppType = findViewById(R.id.tv_app_type);
        tvAppPhone = findViewById(R.id.tv_app_phone);
    }
}
