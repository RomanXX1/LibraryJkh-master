package com.patternjkh.ui.additionalServices;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.R;

public class AdditionalServicesActivity extends BaseMainActionsActivity {

    private static final String APP_SETTINGS = "global_settings";
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_services);

        initToolbar();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String title = sPref.getString("menu_adds", "");

        setTitle(title);

        initAndShowFragment(AdditionalServicesFragment.class);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
