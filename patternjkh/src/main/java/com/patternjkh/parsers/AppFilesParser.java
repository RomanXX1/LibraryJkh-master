package com.patternjkh.parsers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.Server;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppFilesParser {

    public static void parse_json_files(DB db, String line) {
        db.open();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_file = json_data.getJSONObject(i);
                String file_number = json_file.getString("RequestID");
                String file_name = json_file.getString("FileName");
                String file_date = json_file.getString("DateTime");
                String file_id   = json_file.getString("FileID");

                // скачаем иконку приложения
                byte[] data = new byte[1024];
                byte[] bArray = null;
                try {

                    URL url = new URL(ComponentsInitializer.SITE_ADRR + Server.DOWNLOAD_FilE + "id=" + file_id + "&tmode=1");
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

                db.addFotoWithDate(file_id, file_number, bArray, "", file_name, file_date);
            }
        } catch (Exception e) {
            Logger.errorLog(AppFilesParser.class, e.getMessage());
        }
    }
}
