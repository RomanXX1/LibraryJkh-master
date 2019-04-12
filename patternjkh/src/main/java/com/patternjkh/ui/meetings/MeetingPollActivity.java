package com.patternjkh.ui.meetings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import java.util.ArrayList;

import static com.patternjkh.utils.ToastUtils.showToast;

public class MeetingPollActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String hex = "", login, pwd, currentAnswer = "", currentQuestionId = "";
    private int meetingIdFromIntent, currentQuestion, questionsNumber;

    private TextView tvQuestionNumber, tvQuestionTitle;
    private Button btnVoiceFor, btnVoiceAgainst, btnVoiceAbstained, btnPrevious, btnNext, btnComplete;

    private SharedPreferences sPref;
    private DB db = new DB(this);
    private Server server = new Server(this);
    private Handler handler;
    private ArrayList<String> answers = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_poll);
        getParamsFromPrefs();
        db.open();

        initViews();
        setToolbar();
        setTechColors();

        setColors();

        meetingIdFromIntent = getIntent().getIntExtra("meeting_id", 0);
        questionsNumber = getIntent().getIntExtra("questions_number", 0);

        setButtonDisabled(btnComplete);
        setButtonDisabled(btnPrevious);
        if (questionsNumber == 1) {
            btnNext.setVisibility(View.GONE);
            btnPrevious.setVisibility(View.GONE);
        }

        getDataFromDb(true);
        checkIfPollIsCompleted();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!isFinishing() && !isDestroyed()) {
                    if (progressDialog != null)
                        progressDialog.dismiss();
                }

                if (msg.what == 0) {
                    answers.set(currentQuestion, currentAnswer);
                    db.setMeetingPollAnswer(meetingIdFromIntent, StringUtils.convertStringToInteger(currentQuestionId), currentAnswer);

                    openNextQuestion();

                    checkIfPollIsCompleted();
                } else if (msg.what == 1) {

                    String error = "-";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj).replaceAll("error:", "");
                    }
                    Logger.errorLog(MeetingPollActivity.this.getClass(), error);
                    showToastHere("Ошибка передачи данных" + error);

                    setFillingToButton(btnVoiceAgainst, "#" + hex);
                    setFillingToButton(btnVoiceAbstained, "#" + hex);
                    setFillingToButton(btnVoiceFor, "#" + hex);
                    currentAnswer = "";
                } else if (msg.what == 2) {
                    String error = "-";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    DialogCreator.showErrorCustomDialog(MeetingPollActivity.this, error, hex);
                } else if (msg.what == 100) {
                    boolean upd = db.setPollCompleted(meetingIdFromIntent);
                    Logger.plainLog(String.valueOf(upd));

                    AlertDialog.Builder builder = new AlertDialog.Builder(MeetingPollActivity.this);
                    builder.setCancelable(false);
                    builder.setMessage("Ваш голос передан!");
                    builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    if (!isFinishing() && !isDestroyed()) {
                        dialog.show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
                        }
                    }
                } else if (msg.what == 101) {
                    String error = "-";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    Logger.errorLog(MeetingPollActivity.this.getClass(), error);
                    showToastHere("Ошибка передачи данных" + error);
                } else if (msg.what == 200) {
                    progressDialog = new ProgressDialog(MeetingPollActivity.this);
                    progressDialog.setMessage("Отправка смс-кода...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ProgressBar progressbar = progressDialog.findViewById(android.R.id.progress);
                        progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                } else if (msg.what == 300) {
                    showToastHere("Код не отправлен");
                } else if (msg.what == 301) {
                    String error = "-";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    DialogCreator.showErrorCustomDialog(MeetingPollActivity.this, error, hex);
                } else if (msg.what == 302) {
                    showCompletePollSmsDialog();
                }
            }
        };

        initClickListeners();
    }

    private void checkIfPollIsCompleted() {
        boolean isPollCompleted = true;
        for (String item : answers) {
            if (!item.equals("0") && !item.equals("1") && !item.equals("2")) {
                isPollCompleted = false;
            }
        }

        if (isPollCompleted) {
            setButtonEnabled(btnComplete, "#32cd32");
        }
    }

    private void initClickListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentAnswer.equals("")) {
                    sendAnswerToServer();
                } else {
                    openNextQuestion();
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPreviousQuestion();

                currentAnswer = "";
            }
        });

        btnVoiceFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentAnswer("0");
            }
        });

        btnVoiceAgainst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentAnswer("1");
            }
        });

        btnVoiceAbstained.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentAnswer("2");
            }
        });

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Если текущий вопрос - последний или единственный
                if (questionsNumber == 1 || (currentQuestion + 1) == questionsNumber) {
                    sendLastAnswerToServerAndShowSmsDialog();
                } else {
                    sendCheckCode();
                }
            }
        });
    }

    private void sendLastAnswerToServerAndShowSmsDialog() {
        if (!currentAnswer.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line = server.saveAnswerMeeting(login, pwd, currentQuestionId, currentAnswer);
                    if (line.equals("ok")) {
                        db.setMeetingPollAnswer(meetingIdFromIntent, StringUtils.convertStringToInteger(currentQuestionId), currentAnswer);
                        sendCheckCode();
                    } else if (line.contains("error:")) {
                        Message msg = handler.obtainMessage(1, 0, 0, line);
                        handler.sendMessage(msg);
                    } else {
                        Message msg = handler.obtainMessage(2, 0, 0, line);
                        handler.sendMessage(msg);
                    }
                }
            }).start();
        } else {
            sendCheckCode();
        }
    }

    private void sendCheckCode() {
        handler.sendEmptyMessage(200);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.sendCheckCode(login);

                if (line.equals("ok") || line.contains("ok")) {
                    handler.sendEmptyMessage(302);
                } else if (line.contains("не отправ")) {
                    handler.sendEmptyMessage(300);
                } else if (line.contains("error")) {
                    Message msg = handler.obtainMessage(101, 0, 0, line);
                    handler.sendMessage(msg);
                } else {
                    Message msg = handler.obtainMessage(301, 0, 0, line);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void showCompletePollSmsDialog() {

        if (!isFinishing() && !isDestroyed()) {
            if (progressDialog != null)
                progressDialog.dismiss();
        }
        LayoutInflater layoutinflater = LayoutInflater.from(MeetingPollActivity.this);
        View view = layoutinflater.inflate(R.layout.dialog_confirm_sms_code, null);
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(MeetingPollActivity.this);
        errorDialog.setView(view);

        Button btnCancel = view.findViewById(R.id.btn_dialog_sms_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_dialog_sms_confirm);
        final EditText etSms = view.findViewById(R.id.et_dialog_sms);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnCancel.setTextColor(Color.parseColor("#" + hex));
            btnConfirm.setTextColor(Color.parseColor("#" + hex));
        }
        final AlertDialog dialog = errorDialog.create();
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    dialog.dismiss();
                }
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = etSms.getText().toString();
                if (!code.equals("")) {
                    String line = "xxx";
                    line = server.validateCheckCode(login, code);
                    if (line.equals("ok")) {
                        sendCompletePollToServer();
                    } else if (line.contains("error")) {
                        showToastHere(line.replace("error:", ""));
                    } else {
                        DialogCreator.showErrorCustomDialog(MeetingPollActivity.this, line, hex);
                    }
                } else {
                    showToastHere("Введите код");
                }
            }
        });
    }

    private void sendCompletePollToServer() {
        progressDialog = new ProgressDialog(MeetingPollActivity.this);
        progressDialog.setMessage("Синхронизация данных...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar = progressDialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = server.completePollMeeting(login, pwd, String.valueOf(meetingIdFromIntent));
                if (line.equals("ok")) {
                    handler.sendEmptyMessage(100);
                } else if (line.contains("error:")) {
                    Message msg = handler.obtainMessage(101, 0, 0, line);
                    handler.sendMessage(msg);
                } else {
                    Message msg = handler.obtainMessage(2, 0, 0, line);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void setCurrentAnswer(String answer) {
        currentAnswer = answer;
        setAnswerButtonEnabled(answer);
        if (questionsNumber == 1) {
            setButtonEnabled(btnComplete, "#32cd32");
        } else {
            if ((currentQuestion + 1) == questionsNumber) {
                setButtonEnabled(btnComplete, "#32cd32");
            }
        }
    }

    private void sendAnswerToServer() {
        showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.plainLog(currentQuestionId + " " + currentAnswer);
                String line = server.saveAnswerMeeting(login, pwd, currentQuestionId, currentAnswer);
                handleSavingResult(line);
            }
        }).start();
    }

    private void openNextQuestion() {
        currentQuestion++;

        getDataFromDb(false);

        if (currentQuestion + 1 == questionsNumber) {
            setButtonDisabled(btnNext);
        } else {
            setButtonEnabled(btnNext, "#" + hex);
        }
        setButtonEnabled(btnPrevious, "#" + hex);
    }

    private void openPreviousQuestion() {
        currentQuestion--;

        getDataFromDb(false);

        if (currentQuestion == 0) {
            setButtonDisabled(btnPrevious);
        } else {
            setButtonEnabled(btnPrevious, "#" + hex);
        }
        setButtonEnabled(btnNext, "#" + hex);
    }

    private void showProgressBar() {
        progressDialog = new ProgressDialog(MeetingPollActivity.this);
        progressDialog.setMessage("Отправка ответов...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar = progressDialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void handleSavingResult(String line) {
        if (line.equals("ok")) {
            handler.sendEmptyMessage(0);
        } else if (line.contains("error:")) {
            Message msg = handler.obtainMessage(1, 0, 0, line);
            handler.sendMessage(msg);
        } else {
            Message msg = handler.obtainMessage(2, 0, 0, line);
            handler.sendMessage(msg);
        }
    }

    private void getDataFromDb(boolean firstCall) {
        Cursor cursor = db.getDataFromTable(DB.TABLE_MEETING_QUESTIONS);
        cursor.moveToFirst();
        if (cursor.moveToFirst()){
            do {
                int meetingId = cursor.getInt(cursor.getColumnIndex(DB.COL_ID_MEETING));
                if (meetingId == meetingIdFromIntent) {
                    String questionToShow = String.valueOf(currentQuestion + 1);
                    String question = cursor.getString(cursor.getColumnIndex(DB.COL_TEXT));
                    String questionNumber = cursor.getString(cursor.getColumnIndex(DB.COL_NUMBER));
                    String answer = cursor.getString(cursor.getColumnIndex(DB.COL_ANSWER));
                    if (firstCall) {
                        answers.add(answer);
                    }
                    if (questionToShow.equals(questionNumber)) {
                        currentQuestionId = String.valueOf(cursor.getInt(cursor.getColumnIndex(DB.COL_ID_MEETING_QUEST)));
                        tvQuestionTitle.setText(question);
                        tvQuestionNumber.setText("Вопрос " + questionNumber + " из " + questionsNumber + ":");
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        setAnswerButtonEnabled(answers.get(currentQuestion));
    }

    private void setAnswerButtonEnabled(String answer) {
        switch (answer) {
            case "0":
                setFillingToButton(btnVoiceFor, "#FFFFFF");
                setFillingToButton(btnVoiceAbstained, "#" + hex);
                setFillingToButton(btnVoiceAgainst, "#" + hex);
                break;

            case "1":
                setFillingToButton(btnVoiceAgainst, "#FFFFFF");
                setFillingToButton(btnVoiceAbstained, "#" + hex);
                setFillingToButton(btnVoiceFor, "#" + hex);
                break;

            case "2":
                setFillingToButton(btnVoiceAbstained, "#FFFFFF");
                setFillingToButton(btnVoiceAgainst, "#" + hex);
                setFillingToButton(btnVoiceFor, "#" + hex);
                break;

            default:
                setFillingToButton(btnVoiceAgainst, "#" + hex);
                setFillingToButton(btnVoiceAbstained, "#" + hex);
                setFillingToButton(btnVoiceFor, "#" + hex);
                break;
        }
    }

    private void setButtonEnabled(Button btn, String color) {
        btn.setEnabled(true);
        btn.setClickable(true);
        btn.setFocusable(true);
        btn.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor(color)}));
    }

    private void setButtonDisabled(Button btn) {
        btn.setEnabled(false);
        btn.setClickable(false);
        btn.setFocusable(false);
        btn.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#dedede")}));
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
                Intent intent = new Intent(MeetingPollActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
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

    private void setColors() {
        btnPrevious.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        btnNext.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        setBorderToButton(btnVoiceAbstained);
        setBorderToButton(btnVoiceFor);
        setBorderToButton(btnVoiceAgainst);
        setFillingToButton(btnVoiceAbstained, "#" + hex);
        setFillingToButton(btnVoiceFor, "#" + hex);
        setFillingToButton(btnVoiceAgainst, "#" + hex);
    }

    private void setBorderToButton(View view) {
        StateListDrawable drawable = (StateListDrawable) view.getBackground();
        DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
        if (dcs != null) {
            Drawable[] drawableItems = dcs.getChildren();
            GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0];
            gradientDrawableChecked.setStroke(2, Color.parseColor("#" + hex));
        }
    }

    private void setFillingToButton(Button btn, String color) {
        StateListDrawable drawable = (StateListDrawable) btn.getBackground();
        DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState)drawable.getConstantState();
        if (dcs != null) {
            Drawable[] drawableItems = dcs.getChildren();
            GradientDrawable gradientDrawableChecked = (GradientDrawable)drawableItems[0];
            gradientDrawableChecked.setColor(Color.parseColor(color));
        }

        if (color.equals("#FFFFFF")) {
            btn.setTextColor(Color.parseColor("#" + hex));
        } else {
            btn.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(MeetingPollActivity.this, title);
        }
    }

    private void initViews() {
        tvQuestionNumber = findViewById(R.id.tv_meeting_poll_question_number);
        tvQuestionTitle = findViewById(R.id.tv_meeting_poll_question_title);
        btnComplete = findViewById(R.id.btn_meeting_poll_complete);
        btnNext = findViewById(R.id.btn_meeting_poll_next);
        btnPrevious = findViewById(R.id.btn_meeting_poll_previous);
        btnVoiceAbstained = findViewById(R.id.btn_meeting_poll_voice_abstained);
        btnVoiceFor = findViewById(R.id.btn_meeting_poll_voice_for);
        btnVoiceAgainst = findViewById(R.id.btn_meeting_poll_voice_against);
    }
}
