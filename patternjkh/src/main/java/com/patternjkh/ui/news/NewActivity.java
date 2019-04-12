package com.patternjkh.ui.news;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;


public class NewActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String name, text;

    private TextView tvHeader, tvBody;

    private SharedPreferences sPref;
    private DB db = new DB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_app);

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String simpleName = sPref.getString("simple_new", "");

        setTitle(simpleName);

        Bundle extras = getIntent().getExtras();
        name = extras.getString("Name_News");
        text = extras.getString("Text_News");

        tvHeader = findViewById(R.id.header);
        tvHeader.setText(name);

        tvBody = findViewById(R.id.textView5);
        tvBody.setText(text);

        db.open();
//        new NewsCounterAsyncTask().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void setTitle(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    class NewsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            return db.getCountNewsNotReaded();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("count_news", String.valueOf(result));
            ed.apply();
            int[] ids = new int[0];
            try {
                ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), Class.forName(ComponentsInitializer.appWidget.getName() + ".NewAppWidget")));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            ComponentsInitializer.appWidgetProvider.onUpdate(NewActivity.this, AppWidgetManager.getInstance(NewActivity.this),ids);
        }
    }
}
