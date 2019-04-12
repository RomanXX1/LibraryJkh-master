package com.patternjkh.ui.apps;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.patternjkh.utils.FileUtils;
import com.patternjkh.utils.Logger;

import java.io.File;
import java.io.IOException;

// TODO вынести общую логику из AddApplication и MakePhotoActivity
public class BasePhotoActivity extends AppCompatActivity {

    protected File mPhotosDirectory;
    protected File mFileTakenFromCamera;

    protected final String FILE_PATH_NAME_CHILD = "ION_Folder"; // папка для сохранения вложений
    public final static String IMAGE_EXTENSION = "jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhotosDirectory = FileUtils.createDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FILE_PATH_NAME_CHILD);
    }

    protected Uri generateFileUri(Context context) throws IOException {
        mFileTakenFromCamera = createImageFile();
        return FileUtils.getUriForFile(context, mFileTakenFromCamera);
    }

    protected File createImageFile() throws IOException {
        return createFile("photo", IMAGE_EXTENSION);
    }

    protected File createFile(String name, String extension) {
        String filePath = mPhotosDirectory.getPath() + "/" + name + "_" + System.currentTimeMillis() + "." + extension;
        return new File(filePath);
    }
}
