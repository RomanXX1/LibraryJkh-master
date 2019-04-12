package com.patternjkh.parsers;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.Server;
import com.patternjkh.utils.StringUtils;
import com.patternjkh.utils.Utility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppsParser extends DefaultHandler {

    private String str_id = "", str_name = "", str_owner = "", str_closeApp = "", str_isRead, str_isReadCons;
    private String str_isAnswered, str_client = "", str_customer_id = "", str_tema = "", str_app_date = "";

    private int id_com, id_app, appId, comNum;
    private String str_text = "", str_date = "", str_id_author = "", str_author = "", str_adress = "";
    private String str_flat = "", str_phone = "", acc_num = "", type_app;

    private DB db;

    public AppsParser(@NonNull DB db, @NonNull String login) {
        this.db = db;
        this.db.open();
        str_owner = login;
    }

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

            type_app = atts.getValue("id_type").toString();

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
            if (atts.getValue("id_MobileAccount").equals("")) {
                acc_num = "0";
            } else {
                acc_num = atts.getValue("id_MobileAccount");
            }
            String isHidden = atts.getValue("isHidden");

            if (appId != 0) {
                if (id_app != appId) {
                    Utility.map.put(appId, comNum);
                    comNum = 0;
                }
            }

            appId = id_app;
            comNum++;

            db.addCom(id_com, id_app, str_text, str_date, str_id_author, str_author, acc_num, isHidden);

        } else if (localName.toLowerCase().equals("file")) {

            String fileID = atts.getValue("FileID");
            String file_name = atts.getValue("FileName");
            String date_time = atts.getValue("DateTime");

            // скачаем иконку приложения
            byte[] data = new byte[1024];
            byte[] bArray = null;
            try {

                URL url = new URL(ComponentsInitializer.SITE_ADRR + Server.DOWNLOAD_FilE + "id=" + fileID + "&tmode=1");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream input = new BufferedInputStream(url.openStream());
                File sdDir = android.os.Environment.getExternalStorageDirectory();
                OutputStream output = new FileOutputStream(sdDir + "/" + file_name);

                int count;
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
        if (appId != 0) {
            Utility.map.put(appId, comNum);
        }
    }
}
