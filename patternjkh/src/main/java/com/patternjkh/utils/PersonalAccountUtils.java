package com.patternjkh.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalAccountUtils {

    public static boolean checkValidPersonalAccount(String personalAccount){
        boolean valid;

        Pattern pattern = Pattern.compile("^(\\d){5}[\\-](\\d){3}[\\-](\\d){2}");
        Matcher matcher = pattern.matcher(personalAccount);
        valid = matcher.matches();
        return valid;
    }
}
