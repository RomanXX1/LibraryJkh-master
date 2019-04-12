package com.patternjkh.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CounterValueExtractor {

    public static ArrayList<String[]> getArrayFromStringValues(String strValues) {
        String[] valuesStr = strValues.split(";");
        ArrayList<String[]> values = new ArrayList<>();
        if (valuesStr.length > 0) {
            for (String item : valuesStr) {
                values.add(new String[]{item.split("---")[0], item.split("---")[1], item.split("---")[2]});
            }

            try {
                Collections.sort(values, Collections.reverseOrder(new Comparator<String[]>() {
                    DateFormat f = new SimpleDateFormat("dd.MM.yyyy");

                    @Override
                    public int compare(final String[] o1, final String[] o2) {
                        try {
                            return f.parse(o1[0]).compareTo(f.parse(o2[0]));
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }));
            } catch (IllegalArgumentException e) {
                Logger.plainLog("CounterValueExtractor: " + e.getMessage());
            }
        }

        return values;
    }
}
