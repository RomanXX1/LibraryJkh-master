package com.patternjkh.utils;

import android.util.Log;

public class Logger {

    public static void plainLog(String message) {
        if (message != null && !message.equals("")) {
            Log.d("myLog", message);
        }
    }

    public static void errorLog(Class classRef, String message) {
        if (classRef != null && message != null && !message.equals("")) {
            String className = classRef.getSimpleName();
            String errorLogStr = className + ": " + message;
            Log.e("myLog", errorLogStr);
        }
    }
}
