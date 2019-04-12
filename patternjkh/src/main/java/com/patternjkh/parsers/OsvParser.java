package com.patternjkh.parsers;

import com.patternjkh.DB;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class OsvParser {

    public static void parse_json_bills(DB db, String line) {
        db.open();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_bills = json.getJSONArray("data");
            for (int i = 0; i < json_bills.length(); i++) {
                // Запишем данные
                JSONObject json_bill = json_bills.getJSONObject(i);
                String ls = json_bill.getString("Ident");
                String bill_month = json_bill.getString("Month");
                String bill_year = json_bill.getString("Year");
                String bill_service = json_bill.getString("Service");
                String bill_acc = json_bill.getString("Accured");
                String bill_debt = json_bill.getString("Debt");
                String bill_pay = json_bill.getString("Payed");
                String bill_total = json_bill.getString("Total");
                String bill_id = json_bill.getString("ServiceTypeId");

                db.addSaldo(ls, bill_service, Integer.valueOf(bill_month), Integer.valueOf(bill_year), bill_debt, bill_acc, bill_pay, bill_total, bill_id);
            }
        } catch (Exception e) {
            Logger.errorLog(OsvParser.class, e.getMessage());
        }
    }
}
