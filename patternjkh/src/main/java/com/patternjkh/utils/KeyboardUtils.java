package com.patternjkh.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;

import com.patternjkh.OnKeyboardVisibilityListener;

public class KeyboardUtils {

    private static int mAppHeight;
    private static int currentOrientation = -1;

    public static void setKeyboardVisibilityListener(final Activity activity, final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {

        final View contentView = activity.findViewById(android.R.id.content);

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private int mPreviousHeight;

            @Override
            public void onGlobalLayout() {

                int newHeight = contentView.getHeight();

                if (newHeight == mPreviousHeight)

                    return;

                mPreviousHeight = newHeight;

                if (activity.getResources().getConfiguration().orientation != currentOrientation) {

                    currentOrientation = activity.getResources().getConfiguration().orientation;

                    mAppHeight = 0;
                }

                if (newHeight >= mAppHeight) {
                    mAppHeight = newHeight;
                }

                if (newHeight != 0) {

                    if (mAppHeight > newHeight) {
                        onKeyboardVisibilityListener.onKeyboardVisibilityChanged(true);

                    } else {
                        onKeyboardVisibilityListener.onKeyboardVisibilityChanged(false);
                    }
                }
            }
        });
    }
}
