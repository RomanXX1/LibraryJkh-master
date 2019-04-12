package com.patternjkh.ui.polls;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.utils.ConnectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.patternjkh.utils.ToastUtils.showToast;

public class QuestionActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    boolean isAnswered, cancel;
    private String login, id, answer_str = "";
    private int min_number = 1, teck_number = 0, max_number = 0;

    private TextView tvPrev, tvNext, tvQuestion;
    private RadioGroup radioGroupQuestions;
    private Button btnFinishPoll;
    private ProgressDialog dialog;
    private AlertDialog.Builder quitDialog;

    private DB db;
    private Cursor cursor, cursor2, cursor3, cursor_variant;
    private Server server = new Server(this);
    private Handler handler;
    private SharedPreferences sPref;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        login = sPref.getString("login_pref", "");

        // добавим на экран перелистываемый view-pager
        LayoutInflater inflater = LayoutInflater.from(this);
        final List<View> pages = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");
        isAnswered = extras.getBoolean("isAnswered");

        db = new DB(this);
        db.open();

        int numb = 1;
        cursor = db.getDataByPole(db.TABLE_QUEST, db.COL_ID_GROUP, id, db.COL_ID);
        if (cursor.moveToFirst()) {
            do {
                String questionsNumber = String.valueOf(cursor.getCount());
                String currentQuestion = String.valueOf(numb);
                String questionId = cursor.getString(cursor.getColumnIndex(db.COL_ID));
                String questionName = cursor.getString(cursor.getColumnIndex(db.COL_NAME));

                View page = inflater.inflate(R.layout.fragment_question, null);
                TextView tvHead = page.findViewById(R.id.txt_obj);
                tvHead.setText("Вопрос " + currentQuestion + " из " + questionsNumber);
                tvQuestion = page.findViewById(R.id.txt_question);
                tvQuestion.setText(questionName);
                radioGroupQuestions = page.findViewById(R.id.choice_question);

                // Варианты ответов
                Cursor cursor_answers = db.getDataByPole(db.TABLE_ANSWERS, db.COL_ID_GROUP, questionId, db.COL_ID);
                if (cursor_answers.moveToFirst()) {
                    do {
                        String answer = cursor_answers.getString(cursor_answers.getColumnIndex(db.COL_NAME));
                        String isAnswerChecked = cursor_answers.getString(cursor_answers.getColumnIndex(db.COL_IS_ANSWERED));

                        RadioButton newRadioButton = new RadioButton(page.getContext());
                        newRadioButton.setText(answer);
                        radioGroupQuestions.addView(newRadioButton);
                        if (isAnswerChecked.equals("true")) {
                            newRadioButton.setChecked(true);
                        }
                        // Если группа вопросов отвечена - не редактировать
                        if (isAnswered) {
                            newRadioButton.setEnabled(false);
                        }
                    } while (cursor_answers.moveToNext());
                }
                cursor_answers.close();

                // Установим слушатель на переключатели
                radioGroupQuestions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                        int checkedIndex = 0;
                        int childCount = group.getChildCount();
                        for (int i = 0; i < childCount; i++) {
                            RadioButton r_btn = (RadioButton) group.getChildAt(i);
                            if (r_btn.getId() == checkedId) {
                                checkedIndex = i;
                            }
                        }
                        // TODO - решение в лоб, потом подумать
                        // Счетчик для перебора вопросов
                        int j = 0;
                        String id_question = "0";
                        if (cursor2 != null && !cursor2.isClosed()) {
                            cursor2.close();
                        }
                        db.open();
                        cursor2 = db.getDataByPole(db.TABLE_QUEST, db.COL_ID_GROUP, id, db.COL_ID);
                        if (cursor2.moveToFirst()) {
                            do {
                                if (j == (teck_number)) {
                                    id_question = cursor2.getString(cursor2.getColumnIndex(db.COL_ID));
                                    int questionId = cursor2.getInt(cursor2.getColumnIndex(db.COL_ID));
                                    String answer = cursor2.getString(cursor2.getColumnIndex(db.COL_NAME));
                                    int groupAnswersId = cursor2.getInt(cursor2.getColumnIndex(db.COL_ID_GROUP));
                                    db.add_question(questionId, answer, groupAnswersId, "true");
                                    // Посчитаем количество отвеченных в целом по вопросу
                                    check_col_answered(id);
                                }
                                j = j + 1;
                            } while (cursor2.moveToNext());
                        }
                        cursor2.close();
                        // Счетчик для перебора вариантов ответов
                        int i = 0;
                        cursor_variant = db.getDataByPole(db.TABLE_ANSWERS, db.COL_ID_GROUP, id_question, db.COL_ID);
                        if (cursor_variant.moveToFirst()) {
                            do {
                                int answerId = cursor_variant.getInt(cursor_variant.getColumnIndex(db.COL_ID));
                                String answerName = cursor_variant.getString(cursor_variant.getColumnIndex(db.COL_NAME));
                                int groupId = cursor_variant.getInt(cursor_variant.getColumnIndex(db.COL_ID_GROUP));
                                if (i == checkedIndex) {
                                    db.add_answer(answerId, answerName, groupId, "true");
                                } else {
                                    db.add_answer(answerId, answerName, groupId, "false");
                                }
                                i = i + 1;
                            } while (cursor_variant.moveToNext());
                        }
                        cursor_variant.close();

                        set_border();

                    }
                });

                pages.add(page);
                numb = numb + 1;
                max_number = max_number + 1;
            } while (cursor.moveToNext());
        }
        cursor.close();

        setContentView(R.layout.activity_question);

        PagerAdapter pagerAdapter = new PagerAdapter(pages);
        final ViewPager viewPager = findViewById(R.id.fragment);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                teck_number = viewPager.getCurrentItem();
                set_border();
            }
        });

        setToolbar();

        tvPrev = findViewById(R.id.action_left);
        tvPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teck_number = teck_number - 1;
                viewPager.setCurrentItem(teck_number);
                set_border();
            }
        });

        tvNext = findViewById(R.id.action_rigth);
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teck_number = teck_number + 1;
                viewPager.setCurrentItem(teck_number);
                set_border();
            }
        });

        String hex = sPref.getString("hex_color", "23b6ed");

        btnFinishPoll = findViewById(R.id.button2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnFinishPoll.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        }
        btnFinishPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Завершить опрос
                boolean first_answer = true;
                cursor = db.getDataByPole(db.TABLE_QUEST, db.COL_ID_GROUP, id, db.COL_ID);
                if (cursor.moveToFirst()) {
                    do {
                        int id_question = cursor.getInt(cursor.getColumnIndex(db.COL_ID));
                        cursor2 = db.getDataByPole(db.TABLE_ANSWERS, db.COL_ID_GROUP, String.valueOf(id_question), db.COL_ID);
                        if (cursor2.moveToFirst()) {
                            do {
                                if (cursor2.getString(cursor2.getColumnIndex(db.COL_IS_ANSWERED)).equals("true")) {
                                    if (first_answer) {
                                        answer_str = answer_str + String.valueOf(cursor.getInt(cursor.getColumnIndex(db.COL_ID))) + "-" +
                                                String.valueOf(cursor2.getInt(cursor2.getColumnIndex(db.COL_ID)));
                                        first_answer = false;
                                    } else {
                                        answer_str = answer_str + ";" + String.valueOf(cursor.getInt(cursor.getColumnIndex(db.COL_ID))) + "-" +
                                                String.valueOf(cursor2.getInt(cursor2.getColumnIndex(db.COL_ID)));
                                    }
                                }
                            } while (cursor2.moveToNext());
                        }
                        cursor2.close();

                    } while (cursor.moveToNext());
                }
                cursor.close();

                if (!answer_str.equals("")) {
                    // Это ответ на вопрос - передадим данные на сервер
                    dialog = new ProgressDialog(QuestionActivity.this);
                    dialog.setMessage("Отправка данных...");
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(false);
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.show();
                    }

                    if (!ConnectionUtils.hasConnection(QuestionActivity.this)) {
                        // сообщение об ошибке
                        cancel = false;
                        quitDialog = new AlertDialog.Builder(QuestionActivity.this);
                        quitDialog.setTitle(R.string.not_connection_no_continue);
                        quitDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        quitDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        if (!isFinishing() && !isDestroyed()) {
                            quitDialog.show();
                        }
                    }

                    if (cancel) {
                        if (!isFinishing() && !isDestroyed()) {
                            if (dialog != null)
                                dialog.dismiss();
                        }
                        return;
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String line = server.send_data_answers(login, answer_str, id);
                            if (line.equals("ok")) {
                                handler.sendEmptyMessage(1);
                            } else {
                                handler.sendEmptyMessage(2);
                            }
                        }
                    }).start();
                }
            }
        });

        handler = new Handler() {
            public void handleMessage(Message message) {

                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }
                if (message.what == 1) {
                    // Удалось, запишем данные в БД
                    db.update_end_group_questions(Integer.valueOf(id), "true");

                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);

                } else if (message.what == 2) {
                    showToastHere("Не удалось. Попробуйте позже.");
                }

            }
        };

        set_border();

        updateWidget();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void setToolbar() {
        String simpleName = sPref.getString("simple_poll", "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle(simpleName);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    void set_border() {
        if ((teck_number + 1) == min_number) {
            tvPrev.setBackgroundColor(getResources().getColor(R.color.grey));
            tvPrev.setClickable(false);

            btnFinishPoll.setClickable(false);
            btnFinishPoll.setEnabled(false);
            btnFinishPoll.setVisibility(View.INVISIBLE);

        } else {
            tvPrev.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
            tvPrev.setClickable(true);

            btnFinishPoll.setClickable(false);
            btnFinishPoll.setEnabled(false);
            btnFinishPoll.setVisibility(View.INVISIBLE);

        }
        if ((teck_number + 1) == max_number) {
            tvNext.setBackgroundColor(getResources().getColor(R.color.grey));
            tvNext.setClickable(false);

            if (check_answers()) {
                btnFinishPoll.setClickable(true);
                btnFinishPoll.setEnabled(true);
                btnFinishPoll.setVisibility(View.VISIBLE);
            } else {
                btnFinishPoll.setClickable(false);
                btnFinishPoll.setEnabled(false);
                btnFinishPoll.setVisibility(View.INVISIBLE);
            }

        } else {
            tvNext.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
            tvNext.setClickable(true);

            btnFinishPoll.setClickable(false);
            btnFinishPoll.setEnabled(false);
            btnFinishPoll.setVisibility(View.INVISIBLE);

        }
    }

    boolean check_answers() {
        boolean rezult = false;

        cursor3 = db.getDataByPole(db.TABLE_QUEST, db.COL_ID_GROUP, id, db.COL_ID);
        int i = 0;
        int j = 0;
        if (cursor3.moveToFirst()) {
            do {
                String isQuestionAnswered = cursor3.getString(cursor3.getColumnIndex(db.COL_IS_ANSWERED));
                if (isQuestionAnswered.equals("true")) {
                    i = i + 1;
                }
                j = j + 1;
            } while (cursor3.moveToNext());
        }
        cursor3.close();
        if (i == j) {
            rezult = true;
        }
        if (isAnswered) {
            rezult = false;
        }
        return rezult;
    }

    void check_col_answered(String id) {
        cursor3 = db.getDataByPole(db.TABLE_QUEST, db.COL_ID_GROUP, id, db.COL_ID);
        int i = 0;
        if (cursor3.moveToFirst()) {
            do {
                String isQuestionAnswered = cursor3.getString(cursor3.getColumnIndex(db.COL_IS_ANSWERED));
                if (isQuestionAnswered.equals("true")) {
                    i = i + 1;
                }
            } while (cursor3.moveToNext());
        }
        cursor3.close();

        if (i != 0) {
            db.update_answer_group_questions(Integer.valueOf(id), i);
        }

    }

    // Обновим счетчики виджета, т.к. опрос прочитан
    private void updateWidget() {
        db.open();
//        new GroupQuestionsCounterAsyncTask().execute();
    }

    class GroupQuestionsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return db.getCountGroupQuestionsNotAnswered();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

//            SharedPreferences.Editor ed = sPref.edit();
//            ed.putString("count_polls", String.valueOf(result));
//            ed.apply();
//            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), NewAppWidget.class));
//            NewAppWidget myWidget = new NewAppWidget();
//            myWidget.onUpdate(QuestionActivity.this, AppWidgetManager.getInstance(QuestionActivity.this),ids);
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(QuestionActivity.this, title);
        }
    }
}
