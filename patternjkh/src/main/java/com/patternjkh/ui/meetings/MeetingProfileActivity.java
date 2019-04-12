package com.patternjkh.ui.meetings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.MeetingQuestion;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class MeetingProfileActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String hex = "", login, pwd;
    private int meetingIdFromIntent, questionsNumber = 0;

    private TextView tvAddress, tvMeetingType, tvMeetingForm, tvDate, tvDaysRemained, tvAgenda;
    private AppCompatButton btnGoToPoll;
    private LinearLayout layoutResults, layoutAgenda;
    private RecyclerView rvMeetingQuestions;

    private SharedPreferences sPref;
    private DB db = new DB(this);
    private MeetingQuestionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_profile);

        getParamsFromPrefs();

        initViews();
        setTechColors();
        setToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColors();
        }

        meetingIdFromIntent = getIntent().getIntExtra("meeting_id", 0);
        questionsNumber = getIntent().getIntExtra("questions_number", 0);

        db.open();

        btnGoToPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetingProfileActivity.this, MeetingPollActivity.class);
                intent.putExtra("meeting_id", meetingIdFromIntent);
                intent.putExtra("questions_number", questionsNumber);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        layoutResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetingProfileActivity.this, MeetingResultActivity.class);
                intent.putExtra("meeting_id", meetingIdFromIntent);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        layoutAgenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rvMeetingQuestions.isShown()) {
                    rvMeetingQuestions.setVisibility(View.GONE);
                    tvAgenda.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_down), null);
                } else {
                    rvMeetingQuestions.setVisibility(View.VISIBLE);
                    tvAgenda.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_up), null);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getInfoFromDb();
    }

    private void getInfoFromDb() {
        Cursor cursor = db.getDataFromTable(DB.TABLE_MEETINGS);
        cursor.moveToFirst();
        if (cursor.moveToFirst()){
            do {
                int meetingId = cursor.getInt(cursor.getColumnIndex(DB.COL_ID_MEETING));
                if (meetingId == meetingIdFromIntent) {
                    String address = cursor.getString(cursor.getColumnIndex(DB.COL_ADRESS));
                    String meetingForm = cursor.getString(cursor.getColumnIndex(DB.COL_FORM));
                    String date = cursor.getString(cursor.getColumnIndex(DB.COL_DATE_END));
                    String type = cursor.getString(cursor.getColumnIndex(DB.COL_TYPE));
                    String isComplete = cursor.getString(cursor.getColumnIndex(DB.COL_IS_COMPLETE));
                    boolean isPollCompleted = StringUtils.convertStringToBoolean(isComplete);
                    if (isPollCompleted) {
                        btnGoToPoll.setVisibility(View.GONE);
                    }
                    long daysRemained = DateUtils.getNumberOfDaysFromTodayToDate(DateUtils.convertStringToDate(date, "dd.MM.yyyy HH:mm:ss"));
                    tvAddress.setText(address);
                    if (type.equals("")) {
                        type = "-";
                    }
                    tvMeetingType.setText(type);
                    tvMeetingForm.setText(meetingForm);
                    tvDate.setText(DateUtils.parseDateToStringWithHoursAndYearLiteral(date));
                    tvDaysRemained.setText("До конца голосования " + daysRemained + " дней");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayList<MeetingQuestion> meetingQuestions = new ArrayList<>();
        Cursor cursor1 = db.getDataFromTable(DB.TABLE_MEETING_QUESTIONS);
        cursor1.moveToFirst();
        if (cursor1.moveToFirst()){
            do {
                int meetingId = cursor1.getInt(cursor1.getColumnIndex(DB.COL_ID_MEETING));
                if (meetingId == meetingIdFromIntent) {
                    String question = cursor1.getString(cursor1.getColumnIndex(DB.COL_TEXT));
                    meetingQuestions.add(new MeetingQuestion(question));
                }
            } while (cursor1.moveToNext());
        }
        cursor1.close();

        adapter = new MeetingQuestionsAdapter(meetingQuestions);
        LinearLayoutManager layoutManagerCatalog = new LinearLayoutManager(this);
        rvMeetingQuestions.setLayoutManager(layoutManagerCatalog);
        rvMeetingQuestions.setAdapter(adapter);
    }

    private void getParamsFromPrefs() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        login = sPref.getString("login_pref", "");
        pwd = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Голосование");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
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
                Intent intent = new Intent(MeetingProfileActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void setColors() {
        btnGoToPoll.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
    }

    private void initViews() {
        tvAddress = findViewById(R.id.tv_meeting_profile_address);
        tvMeetingType = findViewById(R.id.tv_meeting_profile_meeting_type);
        tvMeetingForm = findViewById(R.id.tv_meeting_profile_form);
        tvDate = findViewById(R.id.tv_meeting_profile_date);
        tvDaysRemained = findViewById(R.id.tv_meeting_profile_remained_days);
        btnGoToPoll = findViewById(R.id.btn_meeting_profile_go_to_poll);
        layoutAgenda = findViewById(R.id.layout_meeting_profile_agenda);
        layoutResults = findViewById(R.id.layout_meeting_profile_results);
        rvMeetingQuestions = findViewById(R.id.rv_meeting_questions);
        tvAgenda = findViewById(R.id.tv_meeting_profile_agenda);
    }
}
