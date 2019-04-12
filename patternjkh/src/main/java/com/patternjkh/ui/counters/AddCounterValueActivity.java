package com.patternjkh.ui.counters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.parsers.CountersParser;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.patternjkh.utils.ToastUtils.showToast;

public class AddCounterValueActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private String counterName, counterMeasure, counterFactoryNum, ls, counterPrevValue, hex, ed_str = "", login, pass, uniqueNum;
    private int teck_n;
    private boolean isBackspaceClicked = false, onDecimalClicked = false;

    private Button btnCancel, btnSend;
    private TextView tvTitle, tvCounterTitle, tvCounterInfo, n_0, n_1, n_2, n_3, n_4, n_1_1, n_1_2, n_1_3, prev_0, prev_1, prev_2, prev_3, prev_4, prev_1_1, prev_1_2, prev_1_3;
    private ImageView ivCounterIcon;
    private EditText etAddCount;
    private ProgressDialog dialog;
    private ScrollView scrollView;

    private SharedPreferences sPref;
    private Handler handler;
    private Server server = new Server(this);
    private DB db = new DB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_counter_value);
        initViews();
        setToolbar();

        getParamsFromIntent();
        getParamsFromPrefs();

        setColors();
        setTechColors();
        setCounterIcon();

        db.open();

        showPrevValue();
        enableAddButton(false);

        int day = DateUtils.getCurrentDay();
        String month = DateUtils.getMonthNameInRightCaseByNumber(DateUtils.getCurrentMonth());
        int year = DateUtils.getCurrentYear();
        String currentDate = day + " " + month + " " + year + "г";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvTitle.setText(Html.fromHtml("Показания на <span style=color:black><b>" + currentDate + "</b></span>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvTitle.setText(Html.fromHtml("Показания на <span style=color:black><b>" + currentDate + "</b></span>"));
        }
        tvCounterTitle.setText(counterName + ", " + counterMeasure);
        tvCounterInfo.setText(counterFactoryNum + ", л/сч " + ls);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hideDialog();
                if (msg.what == 1) {
                    showResultDialog();
                } else if (msg.what == 100) {
                    DialogCreator.showAddCounterErrorDialog(AddCounterValueActivity.this, "Переданы не все параметры", hex);
                } else if (msg.what == 101) {
                    DialogCreator.showAddCounterErrorDialog(AddCounterValueActivity.this, "Не пройдена авторизация", hex);
                } else if (msg.what == 102) {
                    DialogCreator.showAddCounterErrorDialog(AddCounterValueActivity.this, "Не найден прибор у пользователя", hex);
                } else if (msg.what == 103) {
                    DialogCreator.showAddCounterErrorDialog(AddCounterValueActivity.this, "Передача показаний возможна только с 15 по 25 числа", hex);
                } else if (msg.what == 404) {
                    String error = "";
                    if (msg.obj != null) {
                        error = String.valueOf(msg.obj);
                    }
                    DialogCreator.showErrorCustomDialog(AddCounterValueActivity.this, error, hex);
                }
            }
        };

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                constructValueAndSendToServer();
            }
        });

        setNewValue();
    }

    private void showResultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Показания переданы");

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
        } else {
            showToastHere("Показания переданы");
        }
    }

    private void setNewValue() {
        // Поле для невидимого ввода значения
        etAddCount.addTextChangedListener(new TextWatcher() {
            boolean frwrd = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                frwrd = after > count;
                if (String.valueOf(s).endsWith(".") && frwrd) {
                    teck_n = 6;
                }
                if (after < count) {
                    isBackspaceClicked = true;
                } else {
                    isBackspaceClicked = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (String.valueOf(s).endsWith(".") && frwrd) {
                    teck_n = 6;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                ed_str = etAddCount.getText().toString();
                boolean hasPoint;
                boolean withPoint = false;

                if (ed_str.endsWith(".") && frwrd) {
                    ed_str = ed_str.replace(".", "");
                    teck_n = 6;
                    hasPoint = true;
                    withPoint = true;
                } else {
                    if (!onDecimalClicked && !isBackspaceClicked && teck_n == 6) {
                        withPoint = Character.toString(ed_str.charAt(ed_str.length() - 2)).equals(".");
                    }
                    hasPoint = false;
                }
                if (!hasPoint) {
                    handlingTextFilling(true);
                    if (teck_n == 1) {
                        if (ed_str.equals("")) {
                            teck_n = 0;
                        } else {
                            if (!ed_str.substring(0, 1).equals("0")) {
                                enableAddButton(true);
                            }
                        }
                    }

                    if (!isBackspaceClicked) {
                        if (ed_str.substring(ed_str.length() - 1).equals("0") && isCounterBeforePointZeros()) {
                        } else {
                            if (teck_n == 0) {
                                ++teck_n;
                            } else if (teck_n == 1) {
                                if (!ed_str.equals("")) {
                                    n_4.setText(ed_str.substring(ed_str.length() - 1));
                                    teck_n = teck_n + 1;
                                }
                            } else if (teck_n == 2) {
                                n_3.setText(n_4.getText().toString());
                                n_4.setText(ed_str.substring(ed_str.length() - 1));
                                teck_n = teck_n + 1;
                            } else if (teck_n == 3) {
                                n_2.setText(n_3.getText().toString());
                                n_3.setText(n_4.getText().toString());
                                n_4.setText(ed_str.substring(ed_str.length() - 1));
                                teck_n = teck_n + 1;
                            } else if (teck_n == 4) {
                                n_1.setText(n_2.getText().toString());
                                n_2.setText(n_3.getText().toString());
                                n_3.setText(n_4.getText().toString());
                                n_4.setText(ed_str.substring(ed_str.length() - 1));
                                teck_n = teck_n + 1;
                            } else if (teck_n == 5) {
                                n_0.setText(n_1.getText().toString());
                                n_1.setText(n_2.getText().toString());
                                n_2.setText(n_3.getText().toString());
                                n_3.setText(n_4.getText().toString());
                                n_4.setText(ed_str.substring(ed_str.length() - 1));
                                teck_n++;
                            } else if (teck_n == 6) {
                                if (withPoint || onDecimalClicked) {
                                    n_1_1.setText(ed_str.substring(ed_str.length() - 1));
                                    teck_n++;
                                }
                            } else if (teck_n == 7) {
                                n_1_2.setText(ed_str.substring(ed_str.length() - 1));
                                teck_n++;
                            } else if (teck_n == 8) {
                                n_1_3.setText(ed_str.substring(ed_str.length() - 1));
                                InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(etAddCount.getWindowToken(), 0);
                            }
                        }
                    } else {
                        if (teck_n == 6 && isCounterAfterPointZeros()) {
                            teck_n = 6;
                        } else if (teck_n == 6 && isCounterAfterPoint111()) {
                            teck_n = 9;
                        } else if (teck_n == 6 && isCounterAfterPoint110()) {
                            teck_n = 8;
                        } else if (teck_n == 6 && isCounterAfterPoint100()) {
                            teck_n = 7;
                        } else if (teck_n == 0 && !isCounterBeforePointZeros()) {
                            teck_n = 6;
                        }
                        teck_n -= 1;
                        if (teck_n < 6) {
                            n_4.setText(n_3.getText());
                            n_3.setText(n_2.getText());
                            n_2.setText(n_1.getText());
                            n_1.setText(n_0.getText());
                            n_0.setText("0");
                            if (isCounterBeforePointZeros()) {
                                InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(etAddCount.getWindowToken(), 0);
                            }
                        } else {
                            switch (teck_n) {
                                case 8:
                                    n_1_3.setText("0");
                                    break;

                                case 7:
                                    n_1_2.setText("0");
                                    break;

                                case 6:
                                    n_1_1.setText("0");
                                    if (isCounterBeforePointZeros()) {
                                        InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(etAddCount.getWindowToken(), 0);
                                    }
                                    break;
                            }
                        }

                        if (isCounterZeros()) {
                            enableAddButton(false);
                        } else {
                            etAddCount.requestFocus();
                        }
                    }
                }
            }
        });

        n_0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_0.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 1;
                    onDecimalClicked = false;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_0.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = false;
                }
                return true;
            }
        });

        n_1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_1.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 1;
                    onDecimalClicked = false;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_1.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = false;
                }
                return true;
            }
        });

        n_2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_2.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 1;
                    onDecimalClicked = false;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_2.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = false;
                }
                return true;
            }
        });

        n_3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_3.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 1;
                    onDecimalClicked = false;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_3.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = false;
                }
                return true;
            }
        });

        n_4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_4.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 1;
                    onDecimalClicked = false;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_4.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = false;
                }
                return true;
            }
        });

        // после запятой
        n_1_1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_1_1.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 6;
                    onDecimalClicked = true;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_1_1.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = true;
                }
                return true;
            }
        });

        n_1_2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_1_2.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 6;
                    onDecimalClicked = true;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_1_2.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = true;
                }
                return true;
            }
        });

        n_1_3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    scrollButtons();
                    n_1_3.setBackground(getResources().getDrawable(R.drawable.roundrect_dark));
                    teck_n = 6;
                    onDecimalClicked = true;
                } else if (MotionEvent.ACTION_UP == event.getAction()) {
                    scrollButtons();
                    n_1_3.setBackground(getResources().getDrawable(R.drawable.roundrect));
                    etAddCount.requestFocus();
                    InputMethodManager imm = (InputMethodManager) etAddCount.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    onDecimalClicked = true;
                }
                return true;
            }
        });
    }

    private boolean isCounterZeros() {
        return n_0.getText().equals("0") && n_1.getText().equals("0") && n_2.getText().equals("0")
                && n_3.getText().equals("0") && n_4.getText().equals("0") && n_1_1.getText().equals("0")
                && n_1_2.getText().equals("0") && n_1_3.getText().equals("0");
    }

    private boolean isCounterBeforePointZeros() {
        return n_0.getText().equals("0") && n_1.getText().equals("0") && n_2.getText().equals("0")
                && n_3.getText().equals("0") && n_4.getText().equals("0");
    }

    private boolean isCounterAfterPointZeros() {
        return n_1_1.getText().equals("0") && n_1_2.getText().equals("0") && n_1_3.getText().equals("0");
    }

    private boolean isCounterAfterPoint111() {
        return !n_1_1.getText().equals("0") && !n_1_2.getText().equals("0") && !n_1_3.getText().equals("0");
    }

    private boolean isCounterAfterPoint110() {
        return !n_1_1.getText().equals("0") && !n_1_2.getText().equals("0") && n_1_3.getText().equals("0");
    }

    private boolean isCounterAfterPoint100() {
        return !n_1_1.getText().equals("0") && n_1_2.getText().equals("0") && n_1_3.getText().equals("0");
    }

    private void handlingTextFilling(boolean countFilled) {
        if (countFilled) {
            enableAddButton(true);
            n_0.setTextColor(getResources().getColor(R.color.black));
            n_1.setTextColor(getResources().getColor(R.color.black));
            n_2.setTextColor(getResources().getColor(R.color.black));
            n_3.setTextColor(getResources().getColor(R.color.black));
            n_4.setTextColor(getResources().getColor(R.color.black));
            n_1_1.setTextColor(getResources().getColor(R.color.black));
            n_1_2.setTextColor(getResources().getColor(R.color.black));
            n_1_3.setTextColor(getResources().getColor(R.color.black));
        } else {
            enableAddButton(false);
            n_0.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_1.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_2.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_3.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_4.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_1_1.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_1_2.setTextColor(getResources().getColor(R.color.colorTvGrey));
            n_1_3.setTextColor(getResources().getColor(R.color.colorTvGrey));
        }
    }

    private void constructValueAndSendToServer() {
        String value_for_zapros = "";

        // Соберем целое число для передачи на сервер
        String integerValue = "";

        integerValue = integerValue + n_0.getText().toString();
        integerValue = integerValue + n_1.getText().toString();
        integerValue = integerValue + n_2.getText().toString();
        integerValue = integerValue + n_3.getText().toString();
        integerValue = integerValue + n_4.getText().toString();

        // Соберем дробную часть числа для передачи на сервер
        String decimalValue = "";

        decimalValue = decimalValue + n_1_1.getText().toString();
        decimalValue = decimalValue + n_1_2.getText().toString();
        decimalValue = decimalValue + n_1_3.getText().toString();

        value_for_zapros = integerValue + "." + decimalValue;

        sendValueToServer(value_for_zapros);
    }

    private void sendValueToServer(final String value) {
        dialog = new ProgressDialog(AddCounterValueActivity.this);
        dialog.setMessage("Передача показаний...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String strDoubleValue = String.valueOf(StringUtils.convertStringToDouble(value));
                String line = "";
                try {
                    line = server.addCounterValueMytishi(login, pass, uniqueNum, strDoubleValue);
                    if (line.equals("0")) {
                        handler.sendEmptyMessage(100);
                    } else if (line.equals("1")) {
                        handler.sendEmptyMessage(101);
                    } else if (line.equals("2")) {
                        handler.sendEmptyMessage(102);
                    } else if (line.equals("3")) {
                        handler.sendEmptyMessage(103);
                    } else if (line.equals("5")) {

                        getCountersFromServer();

                    } else {
                        Message msg = handler.obtainMessage(404, 0, 0, line);
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Logger.errorLog(AddCounterValueActivity.this.getClass(), e.getMessage());
                }
                Logger.plainLog(strDoubleValue);
            }
        }).start();
    }

    private void getCountersFromServer() {
        String line = "xxx";

        // Получить ВСЕ показания приборов
        try {
            line = server.getCountersMytishi(login, pass);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            if (!line.equals("xxx")) {
                db.del_table(db.TABLE_COUNTERS_MYTISHI);
            }

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                CountersParser xpp = new CountersParser(db, login);
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SAXParser sp = factory.newSAXParser();
                XMLReader reader = sp.getXMLReader();
                reader.setContentHandler(xpp);
                reader.parse(is);
            } catch (Exception e) {
                Logger.errorLog(AddCounterValueActivity.this.getClass(), e.getMessage());
            }
        } catch (Exception e) {
            Logger.errorLog(AddCounterValueActivity.this.getClass(), e.getMessage());
        }

        handler.sendEmptyMessage(1);
    }

    private void showPrevValue() {
        Pair<String[], String[]> prevCards = StringUtils.formatCounterValueToCards(counterPrevValue);
        String[] cardsIntegers = prevCards.first;
        String[] cardsDecimals = prevCards.second;

        prev_0.setText(cardsIntegers[0]);
        prev_1.setText(cardsIntegers[1]);
        prev_2.setText(cardsIntegers[2]);
        prev_3.setText(cardsIntegers[3]);
        prev_4.setText(cardsIntegers[4]);

        prev_1_1.setText(cardsDecimals[0]);
        prev_1_2.setText(cardsDecimals[1]);
        prev_1_3.setText(cardsDecimals[2]);
    }

    private void setCounterIcon() {
        if (counterName.toLowerCase().contains("эл")) {
            ivCounterIcon.setBackgroundResource(R.drawable.counter_lamp_back);
            ivCounterIcon.setImageResource(R.drawable.lamp);
        } else if (counterName.toLowerCase().contains("хвс") || counterName.toLowerCase().contains("холодн") || counterName.toLowerCase().contains("хвc")) {
            ivCounterIcon.setBackgroundResource(R.drawable.counter_water_blue_back);
            ivCounterIcon.setImageResource(R.drawable.water);
        } else if (counterName.toLowerCase().contains("гвс") || counterName.toLowerCase().contains("горяч") || counterName.toLowerCase().contains("гвc")) {
            ivCounterIcon.setBackgroundResource(R.drawable.counter_water_red_back);
            ivCounterIcon.setImageResource(R.drawable.water);
        } else {
            ivCounterIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void enableAddButton(boolean toEnable) {
        if (toEnable) {
            btnSend.setEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btnSend.setTextColor(Color.parseColor("#" + hex));
            } else {
                btnSend.setTextColor(Color.parseColor("#23b6ed"));
            }
        } else {
            btnSend.setEnabled(false);
            btnSend.setTextColor(getResources().getColor(R.color.colorTvGrey));
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etAddCount.getWindowToken(), 0);
        }
    }

    private void hideDialog() {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(AddCounterValueActivity.this, title);
        }
    }

    private void getParamsFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            counterName = extras.getString("count_name");
            counterMeasure = extras.getString("count_measure");
            counterFactoryNum = extras.getString("count_factory_num");
            counterPrevValue = extras.getString("count_prev_val");
            ls = extras.getString("count_ls");
            uniqueNum = extras.getString("count_unique");
        }
    }

    private void getParamsFromPrefs() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        login = sPref.getString("login_pref", "");
        pass = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");
    }

    private void setColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnSend.setTextColor(Color.parseColor("#" + hex));
            btnCancel.setTextColor(Color.parseColor("#" + hex));
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
                Intent intent = new Intent(AddCounterValueActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Передача показаний");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void scrollButtons() {
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },300);
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btn_add_count_cancel);
        btnSend = findViewById(R.id.btn_add_count_send);
        tvCounterInfo = findViewById(R.id.tv_add_count_counter_info);
        tvCounterTitle = findViewById(R.id.tv_add_count_counter_title);
        tvTitle = findViewById(R.id.tv_add_count_title);
        ivCounterIcon = findViewById(R.id.iv_add_count_counter);
        n_0 = findViewById(R.id.n_0);
        n_1 = findViewById(R.id.n_1);
        n_2 = findViewById(R.id.n_2);
        n_3 = findViewById(R.id.n_3);
        n_4 = findViewById(R.id.n_4);
        n_1_1 = findViewById(R.id.n_1_1);
        n_1_2 = findViewById(R.id.n_1_2);
        n_1_3 = findViewById(R.id.n_1_3);
        etAddCount = findViewById(R.id.et_add_count_value);
        prev_0 = findViewById(R.id.prev_0);
        prev_1 = findViewById(R.id.prev_1);
        prev_2 = findViewById(R.id.prev_2);
        prev_3 = findViewById(R.id.prev_3);
        prev_4 = findViewById(R.id.prev_4);
        prev_1_1 = findViewById(R.id.prev_1_1);
        prev_1_2 = findViewById(R.id.prev_1_2);
        prev_1_3 = findViewById(R.id.prev_1_3);
        scrollView = findViewById(R.id.scrollview);
    }
}
