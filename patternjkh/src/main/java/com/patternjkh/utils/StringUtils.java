package com.patternjkh.utils;

import android.util.Pair;
import android.util.Patterns;

public class StringUtils {

    public static String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static boolean checkIsEmailValid(CharSequence target) {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static int convertStringToInteger(String string) {
        if (string != null && !string.equals("")) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static long convertStringToLong(String string) {
        if (string != null && !string.equals("")) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static float convertStringToFloat(String string) {
        if (string != null && !string.equals("")) {
            string = string.replaceAll(",", ".");
            try {
                return Float.parseFloat(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static double convertStringToDouble(String string) {
        if (string != null && !string.equals("")) {
            string = string.replaceAll(",", ".");
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static boolean convertStringToBoolean(String string) {
        if (string != null && !string.equals("")) {
            return Boolean.parseBoolean(string);
        } else {
            return false;
        }
    }

    public static String fixIncorrectDoubleValuesFromString(String value) {
        if (value.equals("") || value.equals("-")) {
            value = "0,00";
        }
        return value;
    }

    public static Pair<String[], String[]> formatCounterValueToCards(String value) {
        String[] arrayInts = new String[5];
        String[] arrayDecimals = new String[3];

        String integers = formatIntegersToFiveNumbers(DigitUtils.getIntegerValueFromDoubleString(value));
        String decimals = formatDecimalsToThreeNumbers(DigitUtils.getDecimalValueFromDoubleString(value));

        arrayInts[0] = integers.substring(0, 1);
        arrayInts[1] = integers.substring(1, 2);
        arrayInts[2] = integers.substring(2, 3);
        arrayInts[3] = integers.substring(3, 4);
        arrayInts[4] = integers.substring(4, 5);

        arrayDecimals[0] = decimals.substring(0, 1);
        arrayDecimals[1] = decimals.substring(1, 2);
        arrayDecimals[2] = decimals.substring(2, 3);

        return new Pair<>(arrayInts, arrayDecimals);
    }

    public static String formatIntegersToFiveNumbers(String value) {
        String result = "00000";

        if (value != null && !value.equals("")) {
            if (value.length() == 1) {
                result = "0000" + value;
            } else if (value.length() == 2) {
                result = "000" + value;
            } else if (value.length() == 3) {
                result = "00" + value;
            } else if (value.length() == 4) {
                result = "0" + value;
            } else {
                result = value.substring(0, 5);
            }
        }

        return result;
    }

    public static String formatDecimalsToThreeNumbers(String value) {
        String result = "000";
        if (value != null && !value.equals("")) {
            if (value.length() == 1) {
                result = value + "00";
            } else if (value.length() == 2) {
                result = value + "0";
            } else if (value.length() == 3) {
                result = value;
            } else {
                result = value.substring(0, 3);
            }
        }
        return result;
    }
}
