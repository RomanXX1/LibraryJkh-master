package com.patternjkh.ui.apps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Photo;
import com.patternjkh.enums.FileType;
import com.patternjkh.listeners.IRecyclerViewItemListener;
import com.patternjkh.listeners.RecyclerItemTouchListener;
import com.patternjkh.ui.others.ChoiceLsActivity;
import com.patternjkh.ui.others.PhotoAdapter;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.FileUtils;
import com.patternjkh.utils.ImageUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AddApplicationActivity extends BasePhotoActivity {

    private static final String APP_SETTINGS = "global_settings";

    private final int RESULT_CHOICE = 0;
    private final int REQUEST_CODE_MAKE_PHOTO = 1;
    private final int REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY = 2;
    private final int REQUEST_CODE_TAKE_FILE = 3;

    private static final int PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE = 10;
    private static final int PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE = 11;
    private static final int PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE = 12;

    private static final int PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE = 21;
    private static final int PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE = 22;

    private static final int PERMISSION_TAKE_FILE_REQUEST_WRITE_STORAGE = 31;
    private static final int PERMISSION_TAKE_FILE_REQUEST_READ_STORAGE = 32;

    private static final String USER_NOT_FOUND = "-2";

    private static final String PRIORITY_APP_MEDIUM = "2";
    private static final int NAME_APP_LIMIT = 30;

    private String login, pass, cons, line = "", adress = "", flat = "", telefone = "", mNameApp, mPersonalAccounts, hex, idAccount;
    private int mPhotoPosition;

    private ProgressDialog dialog;
    private View mainScroll, consView;
    private Context context = this;
    private Spinner type_app, mPersonalAccountsSpinner;
    private TextView mPersonalAccountsTextView;
    private EditText text_app, LS;
    private View mChoicePersonalAccountView;
    private RecyclerView mFilesRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private Button btn_save_app;

    private List<Photo> mPhotos = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private Server server = new Server(this);
    private MenuItem item_add;
    private DB db = new DB(this);
    private Handler handler;
    private List<File> mPhotosFiles = new ArrayList<>();
    private SharedPreferences sPref;
    private ArrayList<String> types = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_application);
        initViews();
        getAppTypesFromServer();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        getParametersFromPrefs();

        Logger.plainLog("acc: " + idAccount);

        setToolbar();

        db.open();
        if (!TextUtils.isEmpty(mPersonalAccounts)) {
            String[] personalAccountsSeparate = mPersonalAccounts.split(",");
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.choice_type, R.id.text_name_type, personalAccountsSeparate);
            mPersonalAccountsSpinner.setAdapter(spinnerAdapter);
        }

        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this, R.layout.choice_type, R.id.text_name_type, types);
        type_app.setAdapter(spinner_adapter);

        LS.setClickable(false);
        LS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddApplicationActivity.this, ChoiceLsActivity.class);
                startActivityForResult(intent, RESULT_CHOICE);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        if (cons.equals("1")) {
            consView.setVisibility(View.VISIBLE);
            mPersonalAccountsTextView.setVisibility(View.GONE);
            mPersonalAccountsSpinner.setVisibility(View.GONE);
        } else {
            consView.setVisibility(View.GONE);
            if (TextUtils.isEmpty(mPersonalAccounts)) {
                mPersonalAccountsTextView.setVisibility(View.GONE);
                mPersonalAccountsSpinner.setVisibility(View.GONE);
            } else {
                mPersonalAccountsTextView.setVisibility(View.VISIBLE);
                mPersonalAccountsSpinner.setVisibility(View.VISIBLE);
            }
        }

        mChoicePersonalAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddApplicationActivity.this, ChoiceLsActivity.class);
                startActivityForResult(intent, RESULT_CHOICE);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        // Фотографии
        mGridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, GridLayoutManager.VERTICAL, false);
        mFilesRecyclerView.setLayoutManager(mGridLayoutManager);
        mPhotoAdapter = new PhotoAdapter();
        mFilesRecyclerView.setAdapter(mPhotoAdapter);
        mPhotos.add(new Photo());
        mPhotoAdapter.setPhotos(mPhotos);

        registerForContextMenu(mFilesRecyclerView);
        mFilesRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(this, mFilesRecyclerView, new IRecyclerViewItemListener() {
            @Override
            public void onItemClick(View view, int adapterPosition) {
                mPhotoPosition = adapterPosition;
                view.showContextMenu();
            }

            @Override
            public void onLongClick(View view, int adapterPosition) {
                mPhotoPosition = adapterPosition;
            }
        }));

        // Кнопка внизу - Отправить заявку
        dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.send_app_ad);
        btn_save_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckForPrefilled(text_app.getText().toString(), LS.getText().toString()) == false) {
                    Snackbar.make(mainScroll, "Заполнены не все параметры", Snackbar.LENGTH_SHORT)
                            .setAction("Ok", null).show();
                } else {

                    if (text_app.getText().toString().length() > NAME_APP_LIMIT) {
                        mNameApp = text_app.getText().toString().substring(0, NAME_APP_LIMIT);
                    } else {
                        mNameApp = text_app.getText().toString();
                    }

                    btn_save_app.setVisibility(View.GONE);
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
                            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                    }

                    Context context = getApplicationContext();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(text_app.getWindowToken(), 0);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            save_app();
                        }
                    }).start();
                }
            }
        });

        handler = new Handler() {
            public void handleMessage(Message message) {
                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }
                if (message.what == 1) {
                    Toast.makeText(AddApplicationActivity.this, "Переданы не все параметры", Toast.LENGTH_SHORT).show();
                } else if (message.what == 11) {
                    Toast.makeText(AddApplicationActivity.this, "Не удалось создать обращение", Toast.LENGTH_SHORT).show();
                } else if (message.what == 2) {
                    Toast.makeText(AddApplicationActivity.this, "Переданы некорректные параметры", Toast.LENGTH_SHORT).show();
                } else if (message.what == 3) {
                    Toast.makeText(AddApplicationActivity.this, "Переданы некорректные параметры", Toast.LENGTH_LONG).show();
                } else if (message.what == 5) {

                    if (!line.equals(USER_NOT_FOUND)) {
                        sendPhotosToServerAndSaveInDb();
                    }

                    String line_comm = "";
                    // Подтянем комментарий к этой заявке - нужен новый скрипт
                    if (TextUtils.isEmpty(mPersonalAccounts)) {
                        line_comm = server.get_comm_by_app(line, login, pass);
                    } else {
                        line_comm = server.get_comm_by_app(line, String.valueOf(mPersonalAccountsSpinner.getSelectedItem()), pass);
                    }

                    if (!(line_comm.equals("xxx"))) {
                        if (line_comm.equals("0")) {

                        } else if (line_comm.equals("1")) {

                        } else if (line_comm.equals("2")) {

                        } else {
                            line_comm = line_comm.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                            try {
                                BufferedReader br = new BufferedReader(new StringReader(line_comm));
                                InputSource is = new InputSource(br);
                                Parser_Get_Comm_By_App xpp = new Parser_Get_Comm_By_App(db);
                                SAXParserFactory factory = SAXParserFactory.newInstance();

                                SAXParser sp = factory.newSAXParser();
                                XMLReader reader = sp.getXMLReader();
                                reader.setContentHandler(xpp);
                                reader.parse(is);

                            } catch (Exception e) {
                            }
                        }
                    }
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.dismiss();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(AddApplicationActivity.this);
                    builder.setTitle("Обращение создано");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra("created", "yes");
                            setResult(101, intent);
                            finish();
                            overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                        }
                    });

                    builder.create();
                    if (!isFinishing() && !isDestroyed()) {
                        builder.show();
                    }
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle(R.string.add_app);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(text_app.getWindowToken(), 0);
                Intent intent = new Intent();
                intent.putExtra("created", "no");
                setResult(101, intent);
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void getAppTypesFromServer() {

        String line = "xxx";

        try {
            line = server.get_apps_type();
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?><Table Name=\"Support_RequestTypes\">", "<Types>");
            line = line.replace("</Table>", "</Types>");

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                Parser_Apps_Type xpp = new Parser_Apps_Type();
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

    public class Parser_Apps_Type extends DefaultHandler {

        String id = "";
        String type = "";

        Parser_Apps_Type() {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.toLowerCase().equals("row")) {
                id = attributes.getValue("id");
                type = attributes.getValue("name");
                types.add(type);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_MAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    processReceivedFile(mFileTakenFromCamera, true);
                }
                break;
            case REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();

                    String filePath = FileUtils.getFilePathFromUri(this, uri);

                    File sourceFile = new File(filePath);

                    processReceivedFile(sourceFile, true);
                }
                break;
            case REQUEST_CODE_TAKE_FILE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();

                    String filePath = FileUtils.getFilePathFromUri(this, uri);

                    File sourceFile = new File(filePath);

                    ContentResolver cR = context.getContentResolver();
                    String mimeType = cR.getType(uri);

                    boolean isImage;
                    isImage = mimeType.contains("image");

                    processReceivedFile(sourceFile, isImage);

                }
                break;
            case RESULT_CHOICE:
                if (data == null) {
                    return;
                }
                String ls = data.getStringExtra("ls");
                LS.setText(ls);
                adress = data.getStringExtra("adress");
                flat = data.getStringExtra("flat");
                telefone = data.getStringExtra("telefone");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_save, menu);

        // Определим кнопку меню для создания заявки
        item_add = menu.findItem(R.id.item_save);

        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.make_photo) {
            if (!isPermissionGranted(Manifest.permission.CAMERA)) {
                requestPermission(Manifest.permission.CAMERA, PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE);
            } else {
                if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE);
                } else {
                    if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE);
                    } else {
                        startMakePhotoActivity();
                    }
                }
            }

        } else if (i == R.id.take_photo_from_gallery) {
            if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE);
            } else {
                if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE);
                } else {
                    startGalleryActivity();
                }
            }

        } else if (i == R.id.take_file) {
            if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_TAKE_FILE_REQUEST_READ_STORAGE);
            } else {
                if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_TAKE_FILE_REQUEST_WRITE_STORAGE);
                } else {
                    startTakeFileActivity();
                }
            }

        } else if (i == R.id.delete_photo) {
            Photo currentPhoto = mPhotos.get(mPhotoPosition);
            String photoPath = currentPhoto.getFoto_path();
            if (photoPath != null) {
                File file = new File(photoPath);
                mPhotosFiles.remove(file);
                mPhotos.remove(currentPhoto);
                mPhotoAdapter.setPhotos(mPhotos);
            }

        }
        return super.onContextItemSelected(item);
    }

    private void save_app() {

        if (!login.equals("")) {

            Utility.apps_number = Utility.apps_number + 1;

            try {
                if (cons.equals("1")) {
                    line = new Add_App_Cons().execute(LS.getText().toString(), mNameApp, text_app.getText().toString(), String.valueOf(type_app.getSelectedItemPosition() + 1), PRIORITY_APP_MEDIUM, telefone).get();
                } else {
                    if (TextUtils.isEmpty(mPersonalAccounts)) {
                        line = new Add_App_SIC().execute(login, pass, mNameApp, text_app.getText().toString(), String.valueOf(type_app.getSelectedItemPosition() + 1), PRIORITY_APP_MEDIUM, login).get();
                    } else {
                        line = new Add_App_SIC().execute(String.valueOf(mPersonalAccountsSpinner.getSelectedItem()), pass, mNameApp, text_app.getText().toString(), String.valueOf(type_app.getSelectedItemPosition() + 1), PRIORITY_APP_MEDIUM, login).get();
                    }
                }
                if (line.equals("xxx")) {
                    handler.sendEmptyMessage(11);
                } else if (line.equals("1")) {
                    handler.sendEmptyMessage(1);
                } else if (line.equals("2")) {
                    handler.sendEmptyMessage(2);
                } else if (line.equals("3")) {
                    handler.sendEmptyMessage(3);
                } else {
                    handler.sendEmptyMessage(5);
                }

            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(11);
            }
        } else {
            handler.sendEmptyMessage(11);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.item_save) {
            item_add.setVisible(false);
            item_add.setEnabled(false);

            save_app();


            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public boolean CheckForPrefilled(String str2, String str3) {
        boolean rezult = true;
        if (str2.equals("")) {
            rezult = false;
        }
        if (cons.equals("1")) {
            if (str3.equals("")) {
                rezult = false;
            }
        }
        return rezult;
    }

    class Add_App_Cons extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = ComponentsInitializer.SITE_ADRR + server.ADD_APP +
                        "ident=" + params[0] +
                        "&name=" + params[1] +
                        "&text=" + params[2] +
                        "&type=" + params[3] +
                        "&priority=" + params[4] +
                        "&phonenum=" + params[5];
                Logger.plainLog(zapros);
                zapros = zapros.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");

            } catch (Exception e) {
                line = "xxx";
                e.printStackTrace();
            }

            if (!line.equals("xxx") || !line.equals("1") || !line.equals("2")) {
                String telefone = params[5];
                // Запишем новую заявку в БД
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                String str_date = dateFormat.format(new Date());
                db.addApp(line, params[2], login, 0, 0, 0, "", "", params[1], str_date, adress, flat, telefone, String.valueOf(type_app.getSelectedItemPosition() + 1), 0);
            } else {
                handler.sendEmptyMessage(11);
            }

            return line;
        }
    }

    class Add_App_SIC extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = ComponentsInitializer.SITE_ADRR + server.ADD_APP +
                        "ident=" + params[0].replaceAll("\\+", "") +
                        "&name=" + params[2] +
                        "&text=" + params[3] +
                        "&type=" + params[4] +
                        "&priority=" + params[5] +
                        "&phonenum=" + params[6];

                Logger.plainLog(zapros);
                zapros = zapros.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");

            } catch (Exception e) {
                line = "xxx";
                e.printStackTrace();
            }

            if (!line.equals("xxx") || !line.equals("1") || !line.equals("2")) {
                // Запишем новую заявку в БД
                String str_date = DateUtils.getDate();
                db.addApp(line, params[3], login, 0, 1, 1, "", "", params[2], str_date, "", "", "", "", 0);
            } else {
                handler.sendEmptyMessage(11);
            }

            return line;
        }
    }

    class AddApplicationFilesInDb extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String photoId = params[0];
            String applicationNumber = params[1];
            String photoPath = params[2];
            byte[] bArray = null;
            String name;
            if (FileUtils.getFileExtension(photoPath).equals(IMAGE_EXTENSION)) {
                Bitmap photo = ImageUtils.getSizedBitmap(photoPath, ImageUtils.BITMAP_SIZE_WIDTH, ImageUtils.BITMAP_SIZE_HEIGHT);
                bArray = ImageUtils.convertBitmapToByte(photo);
                name = "Фото по обращению №";
            } else {
                name = "Файл по обращению №";
            }
            db.addFoto(photoId, applicationNumber, bArray, photoPath, name + applicationNumber);
            return null;
        }
    }

    public class Parser_Get_Comm_By_App extends DefaultHandler {

        int id_com = 0;
        int id_app = 0;
        String str_text = "";
        String str_date = "";
        String str_id_author = "";
        String str_author = "";
        String str_id_account = "";
        String isHidden = "";

        Parser_Get_Comm_By_App(DB db) {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
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
                str_id_account = atts.getValue("id_account");
                isHidden = atts.getValue("isHidden");

                db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, str_id_account, isHidden);
            }
        }
    }

    private void processReceivedFile(File file, boolean isImageFile) {
        Photo currentPhoto = mPhotos.get(mPhotoPosition);
        String photoPath = currentPhoto.getFoto_path();
        if (photoPath != null) {
            File f = new File(photoPath);
            mPhotosFiles.remove(f);
        }
        processMakeSpecificOperationsForFile(file, currentPhoto, isImageFile);

        currentPhoto.setFoto_path(file.getAbsolutePath());
        if (photoPath == null) {
            mPhotos.add(new Photo());
        }
        mPhotosFiles.add(file);
        mPhotoAdapter.setPhotos(mPhotos);
    }

    private void processMakeSpecificOperationsForFile(File file, Photo currentPhoto, boolean isImageFile) {
        if (isImageFile) {
            Bitmap photo = ImageUtils.getSizedBitmap(file.getAbsolutePath(), ImageUtils.BITMAP_SIZE_WIDTH, ImageUtils.BITMAP_SIZE_HEIGHT);
            byte[] bArray = ImageUtils.convertBitmapToByte(photo);
            currentPhoto.setSmall_image(bArray);
            currentPhoto.setFileType(null);
        } else {
            String fileExtension = FileUtils.getFileExtension(file);
            FileType fileType = FileUtils.getFileType(fileExtension);
            currentPhoto.setSmall_image(null);
            currentPhoto.setFileType(fileType);
        }
    }

    private void sendPhotosToServerAndSaveInDb() {
        int fileNumber = 1;
        for (File file : mPhotosFiles) {

            String filePath;
            // Если файл в папке FILE_PATH_NAME_CHILD (папка сохранения вложений), то его и отправляем
            // иначе копируем в папку FILE_PATH_NAME_CHILD и его уже отправляем
            if (file.getAbsolutePath().contains(FILE_PATH_NAME_CHILD)) {
                filePath = file.getAbsolutePath();
            } else {
                String currentFilePath = file.getAbsolutePath();

                String fileExtensionForMimeType = FileUtils.getFileExtension(currentFilePath);
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionForMimeType);

                File destinationFile =null;
                if (mimeType.contains("image")) {
                    try {
                        destinationFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String fileNameWithExtension = FileUtils.getFileNameByPath(currentFilePath);

                    String fileExtension = "";
                    String fileNameWithOutExtension = "";
                    if (fileNameWithExtension.contains(".")) {
                        fileExtension = FileUtils.getFileExtension(fileNameWithExtension);
                        fileNameWithOutExtension = FileUtils.getFileNameWithOutExtension(fileNameWithExtension);
                    }

                    destinationFile = createFile(fileNameWithOutExtension, fileExtension);
                }

                try {
                    FileUtils.copyFile(file, destinationFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                filePath = destinationFile.getAbsolutePath();
            }

            String fileExtension = FileUtils.getFileExtension(filePath);
            String result;
            // отправим файл на сервер - оставим только login, чтобы при создании заявки файл тоже уходил
            if (cons.equals("1")) {
                result = server.FileUpload_Cons(line, filePath, "File " + fileNumber++ + " (app " + line + ")." + fileExtension, idAccount);
            } else {
                result = server.FileUpload(line, filePath, "File " + fileNumber++ + " (app " + line + ")." + fileExtension, login);
            }

            if (!result.equals("xxx")) {
                // добавим в БД
                new AddApplicationFilesInDb().execute(result, line, filePath);

            }
        }
    }

    // Выдача разрешений
    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            // для фото
            case PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE);
                    } else if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE);
                    } else {
                        startMakePhotoActivity();
                    }
                }
                return;
            case PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.CAMERA)) {
                        requestPermission(Manifest.permission.CAMERA, PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE);
                    } else if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE);
                    } else {
                        startMakePhotoActivity();
                    }
                }
                return;
            case PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.CAMERA)) {
                        requestPermission(Manifest.permission.CAMERA, PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE);
                    } else if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE);
                    } else {
                        startMakePhotoActivity();
                    }
                }
                return;

            // для выбора из галереи
            case PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE);
                    } else {
                        startGalleryActivity();
                    }
                }
                return;
            case PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE);
                    } else {
                        startGalleryActivity();
                    }
                }
                return;

            // для выбора файла
            case PERMISSION_TAKE_FILE_REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_TAKE_FILE_REQUEST_WRITE_STORAGE);
                    } else {
                        startTakeFileActivity();
                    }
                }
                return;
            case PERMISSION_TAKE_FILE_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_TAKE_FILE_REQUEST_READ_STORAGE);
                    } else {
                        startTakeFileActivity();
                    }
                }
                return;
        }
    }

    private void startMakePhotoActivity(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri photoFileUri = null;
            try {
                photoFileUri = generateFileUri(this);
            } catch (IOException ex) {
                Toast.makeText(this, "Не удалось получить доступ к памяти на устройстве", Toast.LENGTH_LONG).show();
            }
            if (photoFileUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                startActivityForResult(takePictureIntent, REQUEST_CODE_MAKE_PHOTO);
            }
        }
    }

    private void startGalleryActivity(){
        Intent galleryPickerIntent = new Intent(Intent.ACTION_PICK);
        galleryPickerIntent.setType("image/*");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(galleryPickerIntent, REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY);
    }

    private void startTakeFileActivity(){
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(fileIntent, REQUEST_CODE_TAKE_FILE);
    }

    private void getParametersFromPrefs() {
        login = sPref.getString("login_push", "");
        pass = sPref.getString("pass_push", "");
        cons = sPref.getString("isCons_push", "");
        idAccount = sPref.getString("id_account_push", "");
        mPersonalAccounts = sPref.getString("personalAccounts_pref", "");
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        btn_save_app.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        text_app.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        type_app.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        mPersonalAccountsSpinner.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
    }

    private void initViews() {
        mPersonalAccountsTextView = findViewById(R.id.label_personal_account);
        mPersonalAccountsSpinner = findViewById(R.id.spinner_personal_account);
        type_app = findViewById(R.id.type_app);
        mainScroll = findViewById(R.id.mainScroll);
        consView = findViewById(R.id.add_app_cons);
        LS = findViewById(R.id.LS);
        btn_save_app = findViewById(R.id.save_app);
        text_app = findViewById(R.id.text_app);
        mChoicePersonalAccountView = findViewById(R.id.layout_choice_personal_account_click_view);
        mFilesRecyclerView = findViewById(R.id.recyclerView_photos);
    }
}
