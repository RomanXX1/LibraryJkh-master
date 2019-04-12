package com.patternjkh.ui.news;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.R;

public class NewsActivity extends BaseMainActionsActivity {

    private static final String APP_SETTINGS = "global_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        initToolbar();

        SharedPreferences sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String title = sPref.getString("menu_news", "");
        setTitle(title);

        initAndShowFragment(NewsFragment.class);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
