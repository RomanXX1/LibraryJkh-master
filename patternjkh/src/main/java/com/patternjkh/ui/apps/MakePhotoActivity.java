package com.patternjkh.ui.apps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Photo;
import com.patternjkh.enums.FileType;
import com.patternjkh.utils.FileUtils;
import com.patternjkh.utils.ImageUtils;
import com.patternjkh.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MakePhotoActivity extends BasePhotoActivity {

    private final int REQUEST_CODE_MAKE_PHOTO = 1;
    private final int REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY = 2;

    private static final int PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE = 10;
    private static final int PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE = 11;
    private static final int PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE = 12;

    private static final int PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE = 21;
    private static final int PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE = 22;

    private static final String APP_SETTINGS = "global_settings";

    private String appNumber, login = "", idAccount = "", line = "";

    private ListView lvPhotos;
    private ProgressDialog dialog;
    private TextView tvDataEmpty;

    private ArrayList<Photo> mPhotos = new ArrayList<>();
    private Photo_Adapter photo_adapter;
    private Server server = new Server(this);
    private DB db = new DB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_photo);
        initViews();

        db.open();
        setToolbar();
        getParamsFromIntentAndPrefs();

        photo_adapter = new Photo_Adapter(this, mPhotos);
        registerForContextMenu(lvPhotos);

        fillDataFromDb();
        lvPhotos.setAdapter(photo_adapter);
    }

    private void getParamsFromIntentAndPrefs() {
        SharedPreferences sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        login = sPref.getString("login_pref", "");
        idAccount = sPref.getString("id_account_push", "");
        appNumber = getIntent().getStringExtra("number");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.item_photo) {
            openContextMenu(lvPhotos);

        } else {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.id.make_photo, Menu.NONE, R.string.action_make_photo);
        menu.add(Menu.NONE, R.id.take_photo_from_gallery, Menu.NONE, R.string.action_take_photo_from_gallery);
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

        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_MAKE_PHOTO:
                    processReceivedFile(mFileTakenFromCamera);
                    break;

                case REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY:
                    Uri uri = data.getData();
                    String photoPath = FileUtils.getFilePathFromUri(this, uri);

                    try {
                        File file = createImageFile();
                        FileUtils.copyFile(new File(photoPath), file);
                        processReceivedFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void processReceivedFile(final File file) {

        dialog = new ProgressDialog(this);
        dialog.setMessage("Отправка на сервер...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed()) {

                    String line = sendPhotoToServer(file.getAbsolutePath());

                    Logger.plainLog(line);
                    if (!line.equals("xxx")) {
                        try {
                            new AddApplicationFilesInDb().execute(line, appNumber, file.getAbsolutePath()).get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    fillDataFromDbAndRefreshList();
                }
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 10000);
    }

    class AddApplicationFilesInDb extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String photoId = params[0];
            String applicationNumber = params[1];
            String photoPath = params[2];
            Bitmap photo = ImageUtils.getSizedBitmap(photoPath, ImageUtils.BITMAP_SIZE_WIDTH, ImageUtils.BITMAP_SIZE_HEIGHT);
            byte[] bitmapByteArray = ImageUtils.convertBitmapToByte(photo);
            db.addFoto(photoId, applicationNumber, bitmapByteArray, photoPath, "Фото по обращению №" + applicationNumber);
            return null;
        }
    }

    private String sendPhotoToServer(String file_path) {
        Logger.plainLog(file_path);

        line = server.FileUpload_Cons(appNumber, file_path, "File_"+ System.currentTimeMillis() +"_app_" + appNumber + ".jpg", idAccount);
        return line;
    }

    private void fillDataFromDbAndRefreshList() {
        fillDataFromDb();
        photo_adapter.notifyDataSetChanged();
        if (!isFinishing() && !isDestroyed()) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void fillDataFromDb() {
        mPhotos.clear();
        Cursor cursor = db.getDataByPole(db.TABLE_FOTOS, db.COL_NUMBER, appNumber, "");
        if (cursor.moveToFirst()) {
            do {
                try {
                    String id = String.valueOf(cursor.getInt(cursor.getColumnIndex(db.COL_ID)));
                    byte[] photoSmall = cursor.getBlob(cursor.getColumnIndex(db.COL_FOTO_SMALL));
                    String filePath = cursor.getString(cursor.getColumnIndex(db.COL_FOTO_PATH));
                    String name = cursor.getString(cursor.getColumnIndex(db.COL_NAME));
                    FileType fileType = null;

                    if (photoSmall == null) {
                        String fileExtension;
                        if (filePath != null && !filePath.equals("")) {
                            fileExtension = FileUtils.getFileExtension(filePath);
                        } else {
                            fileExtension = FileUtils.getFileExtension(name);
                        }
                        fileType = FileUtils.getFileType(fileExtension);
                    }

                    String date = cursor.getString(cursor.getColumnIndex(db.COL_DATE));
                    Photo photo = new Photo(id, appNumber, photoSmall, filePath, name, date);
                    photo.setFileType(fileType);
                    mPhotos.add(photo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (mPhotos.size() == 0) {
            tvDataEmpty.setVisibility(View.VISIBLE);
        } else {
            tvDataEmpty.setVisibility(View.GONE);
        }
    }

    // Запрос разрешений
    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleRequestPermissionResult(requestCode);
        }
    }

    private void handleRequestPermissionResult(int requestCode) {
        switch (requestCode) {
            // для фото
            case PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE:
                if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE);
                } else if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE);
                } else {
                    startMakePhotoActivity();
                }
                break;
            case PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE:
                if (!isPermissionGranted(Manifest.permission.CAMERA)) {
                    requestPermission(Manifest.permission.CAMERA, PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE);
                } else if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE);
                } else {
                    startMakePhotoActivity();
                }
                break;
            case PERMISSION_MAKE_PHOTO_REQUEST_READ_STORAGE:
                if (!isPermissionGranted(Manifest.permission.CAMERA)) {
                    requestPermission(Manifest.permission.CAMERA, PERMISSION_MAKE_PHOTO_REQUEST_IMAGE_CAPTURE);
                } else if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_MAKE_PHOTO_REQUEST_WRITE_STORAGE);
                } else {
                    startMakePhotoActivity();
                }
                break;

            // для выбора из галереи
            case PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE:
                if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE);
                } else {
                    startGalleryActivity();
                }
                break;
            case PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_WRITE_STORAGE:
                if (!isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_TAKE_PHOTO_FROM_GALLERY_REQUEST_READ_STORAGE);
                } else {
                    startGalleryActivity();
                }
                break;
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
                startActivityByIntent(takePictureIntent, REQUEST_CODE_MAKE_PHOTO);
            }
        }
    }

    private void startGalleryActivity(){
        Intent galleryPickerIntent = new Intent(Intent.ACTION_PICK);
        galleryPickerIntent.setType("image/*");
        startActivityByIntent(galleryPickerIntent, REQUEST_CODE_TAKE_PHOTO_FROM_GALLERY);
    }

    private void startActivityByIntent(Intent intent, int requestCode) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(intent, requestCode);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Файлы по обращению №" + appNumber);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void initViews() {
        lvPhotos = findViewById(R.id.list_fotos);
        tvDataEmpty = findViewById(R.id.tv_files_empty);
    }
}