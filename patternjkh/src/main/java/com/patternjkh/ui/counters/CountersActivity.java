package com.patternjkh.ui.counters;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.R;

public class CountersActivity extends BaseMainActionsActivity {

    private static final String APP_SETTINGS = "global_settings";
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counters);

        initToolbar();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String title = sPref.getString("menu_counters", "");
        setTitle(title);

        if (savedInstanceState != null) {
            return;
        } else {
            // Получим признак истории по приборам учета
            SharedPreferences sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
            String historyCount = sPref.getString("history_count", "1");

            Class fragmentClass;

            if (historyCount.equals("1")) {
                fragmentClass = CountersMytishiFragment.class;
            } else {
                fragmentClass = CountersFragment_no_history.class;
            }

            initAndShowFragment(fragmentClass);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
