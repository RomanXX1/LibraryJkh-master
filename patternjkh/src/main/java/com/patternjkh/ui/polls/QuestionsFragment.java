package com.patternjkh.ui.polls;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.GroupQuestion;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuestionsFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private Server server = new Server(getActivity());
    private ArrayList<GroupQuestion> polls = new ArrayList<>();
    private ArrayList<GroupQuestion> pollsUnread = new ArrayList<>();
    private GroupQuestionAdapter adapter;
    private ListView lvPolls;
    private Switch chk_answered;
    private Button btnNoInternetRefresh;

    private LinearLayout layoutCheckBox, layoutNoInternet;
    private ConstraintLayout layoutMain;
    private TextView tvDataEmpty;

    private DB db;

    private ProgressDialog dialog;

    private SharedPreferences sPref;
    private String login, hex;

    private Handler handler;

    private SwipeRefreshLayout swipeRefreshLayout;
    private AppStyleManager appStyleManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        login = sPref.getString("login_pref", "");

        db = new DB(getActivity());
        db.open();

        handler = new Handler() {
            public void handleMessage(Message message) {
                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (message.what == 4) {
                    filldata();
                }
            }
        };
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.questions_fragment, container, false);
        initViews(view);

        hex = sPref.getString("hex_color", "23b6ed");

        setTechColors(view);

        if (!ConnectionUtils.hasConnection(getActivity())) {
            showNoInternet();
        } else {
            getQuestionsFromServer();
            hideNoInternet();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorStateList buttonStates = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            Color.parseColor("#" + hex),
                            Color.parseColor("#" + hex),
                            Color.LTGRAY
                    }
            );
            chk_answered.getThumbDrawable().setTintList(buttonStates);
            chk_answered.getTrackDrawable().setTintList(buttonStates);
        }
        appStyleManager = AppStyleManager.getInstance(getActivity(), hex);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!ConnectionUtils.hasConnection(getActivity())) {
                    showNoInternet();
                } else {
                    getQuestionsFromServer();
                    hideNoInternet();
                }
            }
        });

        chk_answered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCheckBox();
            }
        });

        return view;
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(getActivity())) {
                    getQuestionsFromServer();
                    hideNoInternet();
                } else {
                    showNoInternet();
                }
            }
        });
    }

    private void hideNoInternet() {
        layoutNoInternet.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
    }

    private void handleCheckBox() {
        if (chk_answered.isChecked()) {
            adapter = new GroupQuestionAdapter(getActivity(), polls, hex, appStyleManager, login);
            lvPolls.setAdapter(adapter);
            checkNewsArray();
        } else {
            adapter = new GroupQuestionAdapter(getActivity(), pollsUnread, hex, appStyleManager, login);
            lvPolls.setAdapter(adapter);
            checkNewsArray();
        }
    }

    private void checkNewsArray() {
        if (polls.size() == 0 && pollsUnread.size() == 0) {
            layoutCheckBox.setVisibility(View.GONE);
            tvDataEmpty.setText("Данных по опросам нет");
            tvDataEmpty.setVisibility(View.VISIBLE);
        } else if (polls.size() != 0 && pollsUnread.size() == 0) {
            layoutCheckBox.setVisibility(View.VISIBLE);
            if (chk_answered.isChecked()) {
                tvDataEmpty.setVisibility(View.GONE);
            } else {
                tvDataEmpty.setText("Незавершенных опросов нет");
                tvDataEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            layoutCheckBox.setVisibility(View.VISIBLE);
            tvDataEmpty.setVisibility(View.GONE);
            if (chk_answered.isChecked()) {
                adapter = new GroupQuestionAdapter(getActivity(), polls, hex, appStyleManager, login);
            } else {
                adapter = new GroupQuestionAdapter(getActivity(), pollsUnread, hex, appStyleManager, login);
            }
            lvPolls.setAdapter(adapter);
        }
    }

    private void filldata() {
        polls.clear();
        pollsUnread.clear();

        Cursor cursor = db.getDataFromTable(db.TABLE_GROUP_QUEST);
        cursor.moveToFirst();
        if (cursor.moveToFirst()){
            do {
                String name = cursor.getString(cursor.getColumnIndex(db.COL_NAME));
                String id_group = String.valueOf(cursor.getInt(cursor.getColumnIndex(db.COL_ID)));
                String isAnswered_txt = cursor.getString(cursor.getColumnIndex(db.COL_IS_ANSWERED));
                String isRead_txt = cursor.getString(cursor.getColumnIndex(db.COL_IS_READ));
                boolean isRead = StringUtils.convertStringToBoolean(isRead_txt.toLowerCase());
                boolean isAnswered = StringUtils.convertStringToBoolean(isAnswered_txt.toLowerCase());
                int col_questions = cursor.getInt(cursor.getColumnIndex(db.COL_QUESTIONS));
                int col_answered = cursor.getInt(cursor.getColumnIndex(db.COL_ANSWERED));
                if (!isAnswered) {
                    pollsUnread.add(new GroupQuestion(name, id_group, isAnswered, col_questions, col_answered, isRead));
                }
                polls.add(new GroupQuestion(name, id_group, isAnswered, col_questions, col_answered, isRead));
            } while (cursor.moveToNext());
        }
        cursor.close();

        checkNewsArray();
    }

    private void getQuestionsFromServer() {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Загрузка опросов...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (!ConnectionUtils.hasConnection(getContext())) {
            DialogCreator.showInternetErrorDialog(getActivity(), hex);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = db.getDataFromTable(db.TABLE_GROUP_QUEST);
                String line = server.get_need_questions(String.valueOf(cursor.getCount()));
                cursor.close();
                if (line.equals("1")) {
                    line = server.get_data_questions_answers(login);
                    if (!line.equals("xxx")) {
                        parse_json_questions_answers(line);
                    } else {
                        handler.sendEmptyMessage(4);
                    }
                } else {
                    handler.sendEmptyMessage(4);
                }
            }
        }).start();

        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    void parse_json_questions_answers(String line) {
        db.del_table(DB.TABLE_QUEST);
        db.del_table(DB.TABLE_ANSWERS);
        polls.clear();
        pollsUnread.clear();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_group = json_data.getJSONObject(i);
                String name_group = json_group.getString("Name");
                String id_group = json_group.getString("ID");
                String isRead = json_group.getString("IsReaded");
                int col_questions = 0;
                int col_answered = 0;
                String isAnswered = "false";

                JSONArray json_questions = json_group.getJSONArray("Questions");
                for (int j = 0; j < json_questions.length(); j++) {
                    JSONObject json_question = json_questions.getJSONObject(j);
                    String name_question = json_question.getString("Question");
                    String id_question = json_question.getString("ID");
                    String isAnswered_question = json_question.getString("IsCompleteByUser");
                    col_questions = col_questions + 1;
                    if (isAnswered_question.equals("true")) {
                        col_answered = col_answered + 1;
                    }
                    db.add_question(Integer.valueOf(id_question), name_question, Integer.valueOf(id_group), isAnswered_question);

                    JSONArray json_answers = json_question.getJSONArray("Answers");
                    for (int k = 0; k < json_answers.length(); k++) {
                        JSONObject json_answer = json_answers.getJSONObject(k);
                        String name_answer = json_answer.getString("Text");
                        String id_answer = json_answer.getString("ID");
                        String isUserAnswer = json_answer.getString("IsUserAnswer");
                        db.add_answer(Integer.valueOf(id_answer), name_answer, Integer.valueOf(id_question), isUserAnswer);
                    }
                }
                if (col_questions == col_answered) {
                    isAnswered = "true";
                }

                boolean isAnsweredBool = StringUtils.convertStringToBoolean(isAnswered);
                if (!isAnsweredBool) {
                    pollsUnread.add(new GroupQuestion(name_group, id_group, isAnsweredBool, col_questions, col_answered, StringUtils.convertStringToBoolean(isRead)));
                }
                polls.add(new GroupQuestion(name_group, id_group, isAnsweredBool, col_questions, col_answered, StringUtils.convertStringToBoolean(isRead)));
                db.add_group_questions(Integer.valueOf(id_group), name_group, isAnswered, col_questions, col_answered, isRead);
            }
        } catch (Exception e) {}

        handler.sendEmptyMessage(4);
    }

    private void setTechColors(View view) {
        TextView tvTech = view.findViewById(R.id.tv_tech);
        CardView cvDisp = view.findViewById(R.id.card_view_img_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
            btnNoInternetRefresh.setTextColor(Color.parseColor("#" + hex));
        }

        LinearLayout layout = view.findViewById(R.id.layout_tech);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TechSendActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void initViews(View view) {
        chk_answered = view.findViewById(R.id.chk_answered);
        layoutCheckBox = view.findViewById(R.id.layout_questions_checkbox);
        tvDataEmpty = view.findViewById(R.id.tv_poll_empty);
        lvPolls = view.findViewById(R.id.work_list);
        swipeRefreshLayout = view.findViewById(R.id.swipe_questions);
        layoutMain = view.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = view.findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = view.findViewById(R.id.btn_no_internet_refresh);
    }
}