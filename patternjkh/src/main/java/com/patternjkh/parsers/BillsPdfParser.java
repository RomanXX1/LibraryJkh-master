package com.patternjkh.parsers;

import com.patternjkh.DB;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class BillsPdfParser {

    public static void parseJsonBillsPdf(DB db, String line) {
        db.open();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_bills = json.getJSONArray("data");
            for (int i = 0; i < json_bills.length(); i++) {
                // Запишем данные
                JSONObject json_bill = json_bills.getJSONObject(i);
                String ls = json_bill.getString("Ident");
                int month = json_bill.getInt("Month");
                int year = json_bill.getInt("Year");
                String link = json_bill.getString("Link");

                db.addBill(ls, month, year, link);
            }
        } catch (Exception e) {
            Logger.errorLog(BillsPdfParser.class, e.getMessage());
        }
    }
}
