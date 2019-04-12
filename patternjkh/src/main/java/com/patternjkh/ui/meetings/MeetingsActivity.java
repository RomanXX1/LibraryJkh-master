package com.patternjkh.ui.meetings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Meeting;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MeetingsActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String hex = "", login, pwd;

    private RecyclerView rvMeetings;
    private ProgressDialog dialog;

    private MeetingsAdapter adapter;
    private ArrayList<Meeting> meetings = new ArrayList<>();
    private SharedPreferences sPref;
    private Server server = new Server(this);
    private DB db = new DB(this);
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        getParamsFromPrefs();

        initViews();
        setToolbar();
        setTechColors();

        db.open();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hideDialog();
                if (msg.what == 0) {
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter = new MeetingsAdapter(this, meetings);
        LinearLayoutManager layoutManagerCatalog = new LinearLayoutManager(this);
        rvMeetings.setLayoutManager(layoutManagerCatalog);
        rvMeetings.setAdapter(adapter);

        getDataFromServer();
    }

    private void getDataFromServer() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Синхронизация данных...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = server.getMeetings(login, pwd);
                parseMeetings(line);
            }
        }).start();
    }

    private void parseMeetings(String line) {
        meetings.clear();
        db.del_table(DB.TABLE_MEETINGS);
        db.del_table(DB.TABLE_MEETING_QUESTIONS);
        db.del_table(DB.TABLE_MEETING_ACCOUNTS);
        db.del_table(DB.TABLE_MEETING_RESULTS);

        int meetingId;
        String dateStart="", dateEnd="", dateRealPart="", houseAddress="-", author="-", form="-", comment="-", areaResidental,
                areaNonResidental, isComplete = "false", title="-", type="-";
        boolean isAnyQuestionIsAnswered = false, isCompleteBool = false;
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject jsonMeeting = json_data.getJSONObject(i);

                meetingId = jsonMeeting.getInt("ID");
                dateStart = jsonMeeting.getString("DateStart");
                dateEnd = jsonMeeting.getString("DateEnd");
                dateRealPart = jsonMeeting.getString("DateRealPart");
                houseAddress = jsonMeeting.getString("HouseAddress");
                author = jsonMeeting.getString("Author");
                form = jsonMeeting.getString("Form");
                comment = jsonMeeting.getString("Comment");
                areaResidental = jsonMeeting.getString("AreaResidential");
                areaNonResidental = jsonMeeting.getString("AreaNonresidential");
                isComplete = jsonMeeting.getString("IsComplete");
                isCompleteBool = StringUtils.convertStringToBoolean(isComplete);
                type = jsonMeeting.getString("Type");

                JSONArray jsonQuestions = jsonMeeting.getJSONArray("Questions");
                int questionsNumber = jsonQuestions.length();
                isAnyQuestionIsAnswered = false;
                for (int j = 0; j < jsonQuestions.length(); j++) {
                    JSONObject jsonQuestion = jsonQuestions.getJSONObject(j);
                    int questId = jsonQuestion.getInt("ID");
                    String questNumber = jsonQuestion.getString("Number");
                    String questText = jsonQuestion.getString("Text");
                    String questAnswer = jsonQuestion.getString("Answer");
                    if (j == 0) {
                        title = questText;
                    }

                    if (!questAnswer.equals("null")) {
                        isAnyQuestionIsAnswered = true;
                    }

                    String allDecision="", participants="", voicesFor="", voicesAgainst="", voicesAbstained="", voicesForPercent="", voicesAgainstPercent="", voicesAbstainedPercent="";
                    JSONArray jsonResults = jsonQuestion.getJSONArray("AnswersStats");
                    for (int k = 0; k < jsonResults.length(); k++) {
                        JSONObject jsonResult = jsonResults.getJSONObject(k);

                        String name = jsonResult.getString("Name");
                        String voices = jsonResult.getString("Count");
                        String percentage = jsonResult.getString("Percentage");

                        if (k == 0) {
                            allDecision = name;
                        }

                        switch (name.toLowerCase()) {
                            case "всего":
                                participants = voices;
                                break;
                            case "за":
                                voicesFor = voices;
                                voicesForPercent = percentage;
                                break;
                            case "против":
                                voicesAgainst = voices;
                                voicesAgainstPercent = percentage;
                                break;
                            case "воздержался":
                                voicesAbstained = voices;
                                voicesAbstainedPercent = percentage;
                                break;
                        }
                    }

                    db.addMeetingResult(meetingId, questId, questText, allDecision, questAnswer, participants, voicesFor, voicesAgainst, voicesAbstained, voicesForPercent, voicesAgainstPercent, voicesAbstainedPercent);
                    db.addMeetingQuestion(questId, meetingId, questNumber, questText, questAnswer);
                }

                meetings.add(new Meeting(meetingId, questionsNumber, title, dateEnd, isAnyQuestionIsAnswered, isCompleteBool, type));

                JSONArray jsonAccounts = jsonMeeting.getJSONArray("Accounts");
                for (int j = 0; j < jsonAccounts.length(); j++) {
                    JSONObject jsonAccount = jsonAccounts.getJSONObject(j);
                    String questIdent = jsonAccount.getString("Ident");
                    String questArea = jsonAccount.getString("Area");
                    String questPropertyPercent = jsonAccount.getString("PropertyPercent");
                    db.addMeetingAccount(questIdent, questArea, questPropertyPercent);
                }

                db.addMeeting(meetingId, dateStart, dateEnd, dateRealPart, houseAddress, author, form, comment,
                        areaResidental, areaNonResidental, isComplete, title, type);
            }
        } catch (Exception e) {
            Logger.errorLog(MeetingsActivity.this.getClass(), e.getMessage());
        }

        handler.sendEmptyMessage(0);
    }

    private void getParamsFromPrefs() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        login = sPref.getString("login_pref", "");
        pwd = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");
    }

    private void hideDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null)
                dialog.dismiss();
        }
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
                Intent intent = new Intent(MeetingsActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Голосования собственников");
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
        rvMeetings = findViewById(R.id.rv_meetings);
    }
}
