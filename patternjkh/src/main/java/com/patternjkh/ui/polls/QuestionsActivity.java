package com.patternjkh.ui.polls;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.R;
import com.patternjkh.Server;

public class QuestionsActivity extends BaseMainActionsActivity {

    private static final String APP_SETTINGS = "global_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        initToolbar();

        SharedPreferences sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String title = sPref.getString("menu_polls", "");
        setTitle(title);

        initAndShowFragment(QuestionsFragment.class);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }
}
