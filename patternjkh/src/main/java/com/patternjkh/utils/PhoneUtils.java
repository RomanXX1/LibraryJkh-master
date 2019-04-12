package com.patternjkh.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneUtils {

    public static boolean checkValidPhone(String phone){
        phone = phone.replaceAll("-", "");
        boolean valid;

        Pattern pattern = Pattern.compile("^(\\+7)[\\d]{10}$");
        Matcher matcher = pattern.matcher(phone);
        valid = matcher.matches();

        return valid;
    }

    public static String formatPhoneToRightFormat(String unformatted) {
        String formattedPhone = "";
        if (checkValidPhone(unformatted.replaceFirst("8", "+7"))) {
            unformatted = unformatted.replaceFirst("8", "+7");
        }
        if (PhoneUtils.checkValidPhone(unformatted)) {
            if (unformatted.length() > 8) {
                formattedPhone = unformatted.substring(0, 2) + "-" +
                        unformatted.substring(2, 5) + "-" +
                        unformatted.substring(5, 8) + "-" +
                        unformatted.substring(8);
            } else {
                formattedPhone = unformatted;
            }
        } else {
            formattedPhone = unformatted;
        }

        return formattedPhone;
    }
}
