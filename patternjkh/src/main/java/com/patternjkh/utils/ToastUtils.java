package com.patternjkh.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    public static void showToast(Context context, String text) {
        if (context != null && text != null && !text.equals("")) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }
}