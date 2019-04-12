package com.patternjkh.ui.meetings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.data.MeetingResult;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.DateUtils;

import java.util.ArrayList;

public class MeetingResultActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String hex = "", login, pwd;
    private int meetingIdFromIntent;

    private RecyclerView rvMeetingResults;

    private SharedPreferences sPref;
    private ArrayList<MeetingResult> results = new ArrayList<>();
    private MeetingResultsAdapter adapter;
    private DB db = new DB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_result);

        db.open();

        getParamsFromPrefs();
        initViews();
        setTechColors();
        setToolbar();

        meetingIdFromIntent = getIntent().getIntExtra("meeting_id", 0);

        adapter = new MeetingResultsAdapter(this, results);
        LinearLayoutManager layoutManagerCatalog = new LinearLayoutManager(this);
        rvMeetingResults.setLayoutManager(layoutManagerCatalog);
        rvMeetingResults.setAdapter(adapter);

        getDataFromDb();
    }

    private void getDataFromDb() {
        results.clear();

        Cursor cursor = db.getDataFromTable(DB.TABLE_MEETING_RESULTS);
        cursor.moveToFirst();
        if (cursor.moveToFirst()){
            do {
                int meetingId = cursor.getInt(cursor.getColumnIndex(DB.COL_ID_MEETING));
                if (meetingId == meetingIdFromIntent) {
                    String question = cursor.getString(cursor.getColumnIndex(DB.COL_TITLE));
                    String allDecision = cursor.getString(cursor.getColumnIndex(DB.COL_ALL_DESICION));
                    String userVoice = cursor.getString(cursor.getColumnIndex(DB.COL_USER_VOICE));
                    String participants = cursor.getString(cursor.getColumnIndex(DB.COL_PARTICIPANTS));
                    String voicesFor = cursor.getString(cursor.getColumnIndex(DB.COL_VOICES_FOR));
                    String voicesAgainst = cursor.getString(cursor.getColumnIndex(DB.COL_VOICES_AGAINST));
                    String voicesAbstained = cursor.getString(cursor.getColumnIndex(DB.COL_VOICES_ABSTAINED));
                    String voicesForPercent = cursor.getString(cursor.getColumnIndex(DB.COL_VOICES_FOR_PERCENT));
                    String voicesAgainstPercent = cursor.getString(cursor.getColumnIndex(DB.COL_VOICES_AGAINST_PERCENT));
                    String voicesAbstainedPercent = cursor.getString(cursor.getColumnIndex(DB.COL_VOICES_ABSTAINED_PERCENT));

                    results.add(new MeetingResult(question, allDecision, userVoice, participants, voicesFor, voicesAgainst,
                            voicesAbstained, voicesForPercent, voicesAgainstPercent, voicesAbstainedPercent));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.notifyDataSetChanged();
    }

    private void getParamsFromPrefs() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        login = sPref.getString("login_pref", "");
        pwd = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");
    }

    private void setTechColors() {
        TextView tvTech = findViewById(R.id.tv_tech);
        CardView cvDisp = findViewById(R.id.card_view_img_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
        }

        LinearLayout layout = findViewById(R.id.layout_tech);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingResultActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Результаты голосования");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void initViews() {
        rvMeetingResults = findViewById(R.id.rv_meetings_result);
    }
}
