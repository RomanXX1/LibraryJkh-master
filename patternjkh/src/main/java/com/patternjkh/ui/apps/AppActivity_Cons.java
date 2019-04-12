package com.patternjkh.ui.apps;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Comment;
import com.patternjkh.data.Consultant;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.DigitUtils;
import com.patternjkh.utils.StringUtils;
import com.patternjkh.utils.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AppActivity_Cons extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String number, owner, id_account, date, tema, text, author, id_author, login, pass, adress, phone, name_owner, type_app, isPush;

    private TextView tvTema, date_time, tvAppPhone, tvAppAddress, tvAppType, tvAcceptApp, tvRedirectApp, tvDoneApp, tvCloseApp, tvAppFiles;
    private RecyclerView comment_list_main;
    private View mainView;
    private EditText etComment;
    private ImageView mCommentSendImageView;
    private CheckBox checkBoxHidden;

    private DB db;
    private Server server;
    private CommentsAdapterCons com_adapter_main;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<Consultant> cons = new ArrayList<>();
    private ArrayList<String> dates = new ArrayList<>();
    private ArrayList<Comment> comments = new ArrayList<>();
    private Handler handler;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_cons);

        initViews();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        getDataFromBundle();
        setToolbar();

        Utility.map.remove(StringUtils.convertStringToInteger(number));

        db = new DB(this);
        db.open();

        tvAppType.setText(db.get_name_type(type_app));
        tvAppPhone.setText(phone);
        tvAppAddress.setText(adress);
        tvTema.setText(tema);

        server = new Server(this);

        mCommentSendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentToSend = etComment.getText().toString();
                if (!commentToSend.equals("")) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    updateMap();
                    add_comment_end(number, commentToSend, id_account, owner);

                    etComment.setText("");
                    ArrayList<Comment> comments2 = new ArrayList<>();
                    CommentsFromDB(number, comments2);

                    db.open();
                    db.addApp(number, text, owner, 0, 0, 1, author, id_author, tema, date, adress, "", phone, type_app, 1);

                    // Обновим в общем списке
                    com_adapter_main = new CommentsAdapterCons(comments2);
                    comment_list_main.setAdapter(com_adapter_main);
                    comment_list_main.scrollToPosition(com_adapter_main.getItemCount() - 1);
                }
            }
        });
        // заполним список консультантов
        fill_data_cons(cons, list, id_account);

        CommentsFromDB(number, comments);

        comment_list_main = findViewById(R.id.list_comments);
        com_adapter_main = new CommentsAdapterCons(comments);
        LinearLayoutManager layoutManagerCatalog = new LinearLayoutManager(this);
        comment_list_main.setLayoutManager(layoutManagerCatalog);
        comment_list_main.setAdapter(com_adapter_main);
        comment_list_main.scrollToPosition(com_adapter_main.getItemCount() - 1);

        updateMap();

        tvAcceptApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rezult = get_app();
                if (!rezult.equals("")) {
                    Snackbar.make(mainView, rezult, Snackbar.LENGTH_SHORT)
                            .setAction("Ok", null).show();
                }
            }
        });

        tvRedirectApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_cons = send_app();
            }
        });

        tvDoneApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.close_app_cons, null);

                final AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity_Cons.this);
                builder.setView(layout);
                builder.setTitle("Действительно выполнить заявку?");
                builder.setPositiveButton("Выполнить",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                close_app();

                                String rezult = ok_app();
                                if (!rezult.equals("")) {
                                    Snackbar.make(v, rezult, Snackbar.LENGTH_LONG).show();
                                }

                                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                                String str_date = dateFormat.format(new Date());
                                comments.add(new Comment(Integer.valueOf(number),
                                        "Ваша заявка выполнена. Пожалуйста, закройте заявку и оцените качество ее выполнения. Если у Вас остались еще какие-либо вопросы, напишите нам, мы всегда рады Вам помочь!",
                                        str_date,
                                        owner,
                                        true,
                                        false));
                                com_adapter_main.notifyDataSetChanged();

                                // Удалим заявку из БД
                                db.open();
                                db.del_app_by_id(number);
                            }
                        });

                builder.setNegativeButton(R.string.btn_tech_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                AlertDialog dialog = builder.create();
                if (!isFinishing() && !isDestroyed()) {
                    dialog.show();
                }
            }
        });

        tvCloseApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.close_app_cons, null);

                final AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity_Cons.this);
                builder.setView(layout);
                builder.setTitle("Действительно закрыть заявку?");
                builder.setPositiveButton(R.string.btn_tech_close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                close_app();

                                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                                String str_date = dateFormat.format(new Date());
                                comments.add(new Comment(Integer.valueOf(number),
                                        "Заявка №" + number + " закрыта специалистом " + name_owner,
                                        str_date,
                                        name_owner,
                                        true,
                                        false));
                                com_adapter_main.notifyDataSetChanged();

                                // Удалим заявку из БД
                                db.open();
                                db.del_app_by_id(number);

                            }
                        });

                builder.setNegativeButton(R.string.btn_tech_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                AlertDialog dialog = builder.create();
                if (!isFinishing() && !isDestroyed()) {
                    dialog.show();
                }
            }
        });

        tvAppFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppActivity_Cons.this, MakePhotoActivity.class);
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
        ed.commit();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateAppReceiver);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateAppReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
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

    private void updateMap() {
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

    private String get_app() {
        String rezult = "";
        // Принять заявку
        String line = "xxx";
        line = server.pr_app(id_account, number);
        if (line.equals("1")) {
            rezult = "Заявка принята";
            Get_Comm_by_id(number);
            CommentsFromDB(number, comments);
            com_adapter_main.notifyDataSetChanged();
        } else if (line.equals("3")) {
            rezult = "Заявка уже принята консультантом";
        } else {
            String hex = sPref.getString("hex_color", "23b6ed");
            DialogCreator.showErrorCustomDialog(AppActivity_Cons.this, line, hex);
        }
        return rezult;
    }

    private String send_app() {
        // Перевести заявку
        return show_choice_cons();
    }

    private String ok_app() {
        String rezult = "";
        // Выполнить заявку
        String line = server.per_app(id_account, number);
        if (line.equals("1")) {
            try {
                String line_comm = new Get_Comms_by_id().execute(number).get();
                if (!line_comm.equals("xxx")) {
                    try {
                        BufferedReader br = new BufferedReader(new StringReader(line_comm));
                        InputSource is = new InputSource(br);
                        Parser_Get_Comm xpp = new Parser_Get_Comm(number);
                        SAXParserFactory factory = SAXParserFactory.newInstance();

                        SAXParser sp = factory.newSAXParser();
                        XMLReader reader = sp.getXMLReader();
                        reader.setContentHandler(xpp);
                        reader.parse(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rezult = "Заявка выполнена";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (rezult.equals("")) {
            rezult = "Заявка выполнена";
        }
        return rezult;
    }

    private void close_app() {
        // Закрыть заявку
        String line = server.close_app_cons(id_account, number);
        if (line.equals("1")) {
            // Заявка выполнена
        }
    }

    private String show_choice_cons() {

        final String[] rezult_ch = {""};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(AppActivity_Cons.this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.choice_cons_cons, null);

        final Spinner spinner = layout.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity_Cons.this);
        builder.setView(layout);
        builder.setTitle("Укажите консультанта");
        builder.setPositiveButton(R.string.btn_choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Consultant ch_cons = cons.get(spinner.getSelectedItemPosition());
                        String line = server.ch_app(id_account, number, ch_cons.id);
                        if (line.equals("1")) {

                            rezult_ch[0] = ch_cons.getName();

                            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                            String str_date = dateFormat.format(new Date());
                            comments.add(new Comment(Integer.valueOf(number),
                                    "Заявка переведена консультанту " + ch_cons.getName(),
                                    str_date,
                                    name_owner,
                                    true,
                                    false));
                            com_adapter_main.notifyDataSetChanged();

                            // Удалим заявку из БД
                            db.open();
                            db.del_app_by_id(number);
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
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }

        return rezult_ch[0];
    }

    void fill_data_cons(ArrayList<Consultant> cons, ArrayList<String> list, String id_account) {
        cons.clear();
        String line_json = server.get_cons(id_account);
        try {
            JSONObject json = new JSONObject(line_json);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_cons = json_data.getJSONObject(i);
                String id_cons = json_cons.getString("id");
                String name_cons = json_cons.getString("name");
                cons.add(new Consultant(name_cons, id_cons));

                list.add(name_cons);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        String str_text = "", str_date = "", str_id_author = "", str_author = "", isHidden = "", id;

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
                isHidden = atts.getValue("isHidden");

                boolean hidden = isHidden.equals("1");

                // Если есть сообщение с такой же датой и серверным текстом "передана специалисту",
                // то не добавлять этот коммент в список.
                if (!dates.contains(str_date)) {
                    if (!str_text.contains("передана специалисту")) {
                        com_adapter_main.add(new Comment(id_app, str_text, str_date, str_author, false, hidden));
                        updateMap();
                    }
                }

                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, str_id_author, isHidden);
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
                String isHidden = "false";
                if (checkBoxHidden.isChecked()) {
                    isHidden = "true";
                }
                String zapros = ComponentsInitializer.SITE_ADRR + server.ADD_APP_COMMENT_CONS + "&" + "reqID=" + params[0] + "&text=" + params[1] + "&accID=" + params[2] + "&isHidden=" + isHidden;
                zapros = zapros.replaceAll(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                line = "xxx";
            }

            if (!line.equals("xxx")) {
                // Запишем новый комментарий в БД
                DB db = new DB(AppActivity_Cons.this);
                db.open();
                String isHidden = "0";
                if (checkBoxHidden.isChecked()) {
                    isHidden = "1";
                }
                db.addCom(Integer.valueOf(line), Integer.valueOf(params[0]), params[1], DateUtils.getDate(), params[2], params[3], params[2], isHidden);
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
                int id_id = cursor.getColumnIndex(DB.COL_ID);
                int id_date = cursor.getColumnIndex(DB.COL_DATE);
                int id_text = cursor.getColumnIndex(DB.COL_TEXT);
                int id_id_author = cursor.getColumnIndex(DB.COL_ID_AUTHOR);
                int id_name = cursor.getColumnIndex(DB.COL_AUTHOR);
                int id_isHidden = cursor.getColumnIndex(DB.COL_IS_HIDDEN);

                Boolean isHidden = cursor.getString(id_isHidden).equals("1");

                boolean isAuthor = true;
                isAuthor = cursor.getString(id_id_author).equals(id_account);

                if (its_first) {
                    if (date_time.getText().toString().equals("")) {
                        date_time.setText(cursor.getString(id_date));
                    }
                    its_first = false;
                }

                comments.add(new Comment(Integer.valueOf(cursor.getString(id_id)),
                        cursor.getString(id_text),
                        cursor.getString(id_date),
                        cursor.getString(id_name),
                        isAuthor,
                        isHidden));

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_ring, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.item_ring) {
            if (!phone.equals("")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            } else {
                Snackbar snackbar = Snackbar.make(mainView, "У создавшего заявку не обнаружен номер телефона", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void getDataFromBundle() {
        Bundle extras = getIntent().getExtras();
        number = extras.getString("number");
        owner = extras.getString("owner");
        author = extras.getString("author");
        id_author = extras.getString("id_author");
        tema = extras.getString("tema");
        text = extras.getString("text");
        id_account = extras.getString("id_account");
        login = extras.getString("login");
        pass = extras.getString("pass");
        phone = extras.getString("phone");
        date = extras.getString("date");
        type_app = extras.getString("type_app");
        isPush = extras.getString("isPush");
        if (isPush == null) {
            isPush = "no";
        }
        adress = extras.getString("adress");
        phone = extras.getString("phone");
        name_owner = extras.getString("name_owner");
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.handling_with_number, number));
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

    private void initViews() {
        tvAppType = findViewById(R.id.tv_app_type);
        tvAppAddress = findViewById(R.id.tv_app_address);
        mainView = findViewById(R.id.main_layout);
        tvAppPhone = findViewById(R.id.tv_app_phone);
        date_time = findViewById(R.id.date_time);
        tvTema = findViewById(R.id.tema);
        etComment = findViewById(R.id.txt_comm);
        mCommentSendImageView = findViewById(R.id.img_send_comm);
        tvAcceptApp = findViewById(R.id.txt_ok_com);
        tvRedirectApp = findViewById(R.id.txt_send_com);
        tvDoneApp = findViewById(R.id.txt_done_com);
        tvCloseApp = findViewById(R.id.txt_close_com);
        tvAppFiles = findViewById(R.id.txt_files_com);
        checkBoxHidden = findViewById(R.id.checkbox_app_cons_is_hidden);
    }
}
