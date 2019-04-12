package com.patternjkh.parsers;

import android.content.SharedPreferences;

import com.patternjkh.DB;
import com.patternjkh.Server;
import com.patternjkh.utils.StringUtils;

import org.json.JSONObject;

public class DebtsParser {

    public static void getJsonDebts(Server server, SharedPreferences sPref, String personalAccounts) {
        String[] personalAccountsSeparate = personalAccounts.split(",");
        String lineDebtsToPrefs = "";
        for (int i = 0; i < personalAccountsSeparate.length; i++) {
            String line = server.get_debt(personalAccountsSeparate[i]);
            String date = "-";
            String sum = "-";
            String fine = "-";
            try {
                JSONObject json = new JSONObject(line);
                JSONObject json_debt = json.getJSONObject("data");
                date = json_debt.getString("Date");
                sum = json_debt.getString("SumAll");
                fine = json_debt.getString("SumFine");

                if (StringUtils.convertStringToDouble(sum) < 0) {
                    sum = "0.0";
                }

                String lineDebtsOneAcc = personalAccountsSeparate[i] + "--" + date + "--" + sum + "--" + fine + ";";
                lineDebtsToPrefs += lineDebtsOneAcc;

            } catch (Exception e) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("debts_date", "");
                ed.putString("debts_sum",  "");
                ed.putString("debts_fine", "");
                ed.commit();
            }
        }

        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("debts_full_line", lineDebtsToPrefs);
        ed.commit();
    }
}
