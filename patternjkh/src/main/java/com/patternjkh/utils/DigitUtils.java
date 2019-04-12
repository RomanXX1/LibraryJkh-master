package com.patternjkh.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DigitUtils {

    public static String roundDigit(String digit) {
        return String.valueOf(Math.round(Double.parseDouble(digit.replace(",", "."))));
    }

    public static double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp) / pow;
    }

    public static String getDecimalValueFromDoubleString(String value) {
        if (value != null && !value.equals("")) {
            BigDecimal bd = new BigDecimal(value.replaceAll(",", "."));
            BigInteger decimal = bd.remainder(BigDecimal.ONE).movePointRight(bd.scale()).abs().toBigInteger();
            return decimal.toString();
        } else {
            return "0";
        }
    }

    public static String getIntegerValueFromDoubleString(String value) {
        if (value != null && !value.equals("")) {
            BigDecimal bd = new BigDecimal(value.replaceAll(",", "."));
            BigInteger decimal = bd.toBigInteger();
            return decimal.toString();
        } else {
            return "0";
        }
    }
}