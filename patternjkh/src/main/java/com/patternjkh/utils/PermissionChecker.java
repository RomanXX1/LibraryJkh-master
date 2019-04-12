package com.patternjkh.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionChecker {

    public static void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static boolean isPermissionGranted(Activity activity, String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(activity, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
}