package com.patternjkh.parsers;

import com.patternjkh.DB;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewsParser {

    public static void parse_json_news(DB db, String line) {
        db.open();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_news = json_data.getJSONObject(i);

                String name_news = json_news.getString("Header");
                String date_news = json_news.getString("Created");
                String id_news = json_news.getString("ID");
                String text_news = json_news.getString("Text");
                String isRead = json_news.getString("IsReaded");

                db.add_news(Integer.valueOf(id_news), name_news, text_news, date_news, isRead);
            }
        } catch (Exception e) {
            Logger.errorLog(NewsParser.class, e.getMessage());
        }
    }
}
