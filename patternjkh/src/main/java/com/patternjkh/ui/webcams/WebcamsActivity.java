package com.patternjkh.ui.webcams;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.R;

public class WebcamsActivity extends BaseMainActionsActivity {

    public static final String APP_SETTINGS = "global_settings";
    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webcams);

        initToolbar();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String title = sPref.getString("menu_webs", "");
        setTitle(title);

        initAndShowFragment(WebcamsFragment.class);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
