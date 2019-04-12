package com.patternjkh.utils;

import ru.tinkoff.acquiring.sdk.Money;

public class MoneyUtils {

    public static long convertRublesToCops(String sum) {
        return Money.ofRubles(StringUtils.convertStringToDouble(sum)).getCoins();
    }

    public static long convertRublesToCops(double sum) {
        return Money.ofRubles(sum).getCoins();
    }

    public static String convertCopsToStringRublesFormatted(long sumInCops) {
        String mon = "0.00";
        if (sumInCops > 0) {
            mon = Money.ofCoins(sumInCops).toString().replaceAll(" ", "").replaceAll(",", ".");
            if (!mon.contains(".")) {
                mon += ".00";
            }
        }
        return mon;
    }
}
