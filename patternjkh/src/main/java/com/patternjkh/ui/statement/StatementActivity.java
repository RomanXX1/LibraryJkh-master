package com.patternjkh.ui.statement;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.R;

public class StatementActivity extends BaseMainActionsActivity {

    private static final String APP_SETTINGS = "global_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement);

        initToolbar();

        SharedPreferences sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String simpleName = sPref.getString("menu_osv", "");

        setTitle(simpleName);

        if (savedInstanceState == null) {
            initAndShowFragment(OSVFragment.class);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
