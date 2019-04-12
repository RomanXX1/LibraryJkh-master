package com.patternjkh;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class AppStyleManager {

    private static AppStyleManager instance = null;
    private Context context;
    private String hex;

    private AppStyleManager(Context context, String hex) {
        this.context = context;
        this.hex = hex;
    }

    public static AppStyleManager getInstance(Context context, String hex) {
        if (instance == null) {
            instance = new AppStyleManager(context, hex);
        }
        return instance;
    }

    public Drawable changeDrawableColor(int drawableId) {
        Drawable drawable = context.getResources().getDrawable(drawableId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hex == null || hex.equals("")) {
                hex = "23b6ed";
            }
            drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#" + hex), PorterDuff.Mode.SRC_IN));
        }
        return drawable;
    }

    public Drawable changeDrawableColorLighter(int drawableId) {
        Drawable drawable = context.getResources().getDrawable(drawableId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hex == null || hex.equals("")) {
                hex = "23b6ed";
            }
            drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#77" + hex), PorterDuff.Mode.SRC_IN));
        }
        return drawable;
    }
}
