package com.patternjkh.ui.counters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Counter;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.StringUtils;

import java.util.ArrayList;


public class CountersFragment_no_history extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private String login, pass, mail, start_day, end_day, can_count;
    private int i_num_month, i_year, max_i_num_month, max_i_year, min_i_num_month = 0, min_i_year = 0, can_edit_count = 0;

    private TextView month_head, text_head, action_left, action_rigth;
    private ListView count_list;

    private CountersAdapter_no_history count_adapter;
    private ArrayList<Counter> counters = new ArrayList<Counter>();
    private Cursor cursor;
    private DB db;
    private Server server = new Server(getActivity());
    private SharedPreferences sPref;
    private Activity teckActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.counters_fragment_no_history, container, false);

        initViews(v);
        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        teckActivity = getActivity();
        db = new DB(teckActivity);
        db.open();

        getParametersFromPrefs();

        if (can_count.equals("0")) {
            can_edit_count = 0;
        } else {
            can_edit_count = 1;
        }

        text_head.setText("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");

        cursor = db.getDataFromTableByOrder(db.TABLE_COUNTERS, db.COL_YEAR + ", " + db.COL_NUM_MONTH);
        if (cursor.moveToFirst()) {
            do {
                int c_num_m = cursor.getColumnIndex(db.COL_NUM_MONTH);
                int c_year = cursor.getColumnIndex(db.COL_YEAR);

                String name_month = DateUtils.getMonthNameByNumber(cursor.getInt(c_num_m));
                String name_btn_month = name_month + " " + String.valueOf(cursor.getInt(c_year));

                // Новый дизайн
                month_head.setText(name_btn_month);

                i_num_month = cursor.getInt(c_num_m);
                i_year = cursor.getInt(c_year);

                // установим максимальные значения по месяцам
                max_i_num_month = i_num_month;
                max_i_year = i_year;

                // установим минимальные значения по месяцам
                if (min_i_year == 0) {
                    min_i_year = i_year;
                    if (min_i_num_month == 0) {
                        min_i_num_month = i_num_month;
                    } else if (min_i_num_month > i_num_month) {
                        min_i_num_month = i_num_month;
                    }
                } else if (min_i_year > i_year) {
                    min_i_year = i_year;
                    if (min_i_num_month == 0) {
                        min_i_num_month = i_num_month;
                    } else if (min_i_num_month > i_num_month) {
                        min_i_num_month = i_num_month;
                    }
                } else if (min_i_year == i_year) {
                    if (min_i_num_month == 0) {
                        min_i_num_month = i_num_month;
                    } else if (min_i_num_month > i_num_month) {
                        min_i_num_month = i_num_month;
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        filldata(i_num_month, i_year);

        // вывод списка
        count_adapter = new CountersAdapter_no_history(teckActivity, counters);
        count_list.setAdapter(count_adapter);

        // Новое отправка показаний на сайт - в отдельном окне
        count_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (can_edit_count == 0) {

                } else {
                    if (position != 0) {
                        final Counter counter = (Counter) parent.getItemAtPosition(position);

                        LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.add_counts_old, null);


                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(layout);
                        TextView txt_number = layout.findViewById(R.id.txt_number);
                        txt_number.setText(counter.name + ", " + counter.ed_izm);
                        final EditText diff_count = layout.findViewById(R.id.text_Count);
                        builder.setPositiveButton(R.string.btn_tech_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String line = "";
                                        try {
                                            line = server.addCountToServer(login, pass, counter.uniq_num, diff_count.getText().toString());
                                            if (line.equals("0")) {
                                                Snackbar.make(getView(), "Переданы не все параметры", Snackbar.LENGTH_LONG).show();
                                            } else if (line.equals("1")) {
                                                Snackbar.make(getView(), "Не пройдена авторизация", Snackbar.LENGTH_LONG).show();
                                            } else if (line.equals("2")) {
                                                Snackbar.make(getView(), "Не найден прибор у пользователя", Snackbar.LENGTH_LONG).show();
                                            } else if (line.equals("3")) {
                                                Snackbar.make(getView(), "Передача показаний возможна только с 15 по 25 числа", Snackbar.LENGTH_LONG).show();
                                            } else if (line.equals("5")) {

                                                float diff_text = StringUtils.convertStringToFloat(diff_count.getText().toString());
                                                counter.value = Float.toString(diff_text);
                                                count_adapter.notifyDataSetChanged();

                                                // показания переданы - запишем данные в БД
                                                db.addCount(login, counter.num_month, counter.year, counter.name, counter.ed_izm, counter.uniq_num, counter.prev, counter.value, counter.diff, counter.getTypeId(), counter.ident, counter.serialNumber, "1");

                                                Snackbar.make(getView(), "Показания переданы", Snackbar.LENGTH_LONG).show();

                                            } else {
                                                Snackbar.make(getView(), "Не удалось передать показания", Snackbar.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });

                        builder.setNegativeButton(R.string.btn_tech_no,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                            dialog.show();
                        }
                    }
                }
            }
        });

        // Новый дизайн - кнопки внизу
        action_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                do {
                    i_num_month = i_num_month - 1;
                    if (i_num_month == 0) {
                        i_num_month = 12;
                        i_year = i_year - 1;
                    }
                } while (((i_num_month < min_i_num_month) & (i_year == min_i_year) |
                        (db.chechk_month_in_DB(i_num_month, i_year))
                ));
                check_table();
                check_border();
            }
        });

        action_rigth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                do {
                    i_num_month = i_num_month + 1;
                    if (i_num_month == 13) {
                        i_num_month = 1;
                        i_year = i_year + 1;
                    }
                } while (((i_num_month > max_i_num_month) & (i_year == max_i_year) |
                        (db.chechk_month_in_DB(i_num_month, i_year))
                ));
                check_table();
                check_border();
            }
        });

        check_border();

        return v;

    }

    void filldata(int num_month, int year) {
        cursor = db.getDataFromTable(db.TABLE_COUNTERS);
        if (cursor.moveToFirst()) {
            do {
                int c_num_m = cursor.getColumnIndex(db.COL_NUM_MONTH);
                int c_year = cursor.getColumnIndex(db.COL_YEAR);
                int c_name = cursor.getColumnIndex(db.COL_COUNT);
                int c_ed_izm = cursor.getColumnIndex(db.COL_COUNT_ED_IZM);
                int c_uniq_num = cursor.getColumnIndex(db.COL_UNIQ_NUM);
                int c_prev = cursor.getColumnIndex(db.COL_PREV_VALUE);
                int c_value = cursor.getColumnIndex(db.COL_VALUE);
                int c_diff = cursor.getColumnIndex(db.COL_DIFF);
                int c_type_id = cursor.getColumnIndex(db.COL_TYPE_ID);
                String ident = cursor.getString(cursor.getColumnIndex(DB.COL_IDENT));
                String serial = cursor.getString(cursor.getColumnIndex(DB.COL_SERIAL));
                String isSent = cursor.getString(cursor.getColumnIndex(DB.COL_IS_SENT));

                if ((cursor.getInt(c_num_m) == num_month) & (cursor.getInt(c_year) == year)) {

                    counters.add(new Counter(cursor.getInt(c_num_m),
                            cursor.getInt(c_year),
                            cursor.getString(c_name),
                            cursor.getString(c_ed_izm),
                            cursor.getString(c_uniq_num),
                            cursor.getString(c_prev),
                            cursor.getString(c_value),
                            cursor.getString(c_diff),
                            cursor.getInt(c_type_id),
                            ident,
                            serial,
                            isSent));
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    void check_border() { // проверка на достижение границ по месяцам в БД
        if ((i_num_month == min_i_num_month) & (i_year == min_i_year)) {

            action_left.setBackgroundColor(getResources().getColor(R.color.grey));
            action_left.setClickable(false);
            text_head.setText("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");

            can_edit_count = 0;
        } else {

            action_left.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
            action_left.setClickable(true);
            text_head.setText("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");

            can_edit_count = 0;
        }
        if ((i_num_month == max_i_num_month) & (i_year == max_i_year)) {
            action_rigth.setBackgroundColor(getResources().getColor(R.color.grey));
            action_rigth.setClickable(false);

            if (can_count.equals("0")) {
                can_edit_count = 0;
                text_head.setText("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");
            } else {
                can_edit_count = 1;
                text_head.setText("Для внесения показаний нажмите на счетчик");
            }
        } else {

            action_rigth.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
            action_rigth.setClickable(true);

            can_edit_count = 0;
            text_head.setText("Возможность передавать показания доступна с " + start_day + " по " + end_day + " число текущего месяца!");
        }
    }

    void check_table() { // обновление отображения данных в таблице
        String name_month = DateUtils.getMonthNameByNumber(i_num_month);
        String name_btn_month = name_month + " " + String.valueOf(i_year);

        // Новый дизайн
        month_head.setText(name_btn_month);

        counters.clear();
        filldata(i_num_month, i_year);
        count_list.setAdapter(count_adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String rezult = data.getStringExtra("rezult");
        if (rezult.equals("ok_count")) {
            check_table();
            check_border();
        }
    }

    private void getParametersFromPrefs() {
        login = sPref.getString("login_push", "");
        pass = sPref.getString("pass_push", "");
        mail = sPref.getString("mail_pref", "");
        start_day = sPref.getString("start_day", "");
        end_day = sPref.getString("end_day", "");
        can_count = sPref.getString("can_count", "");
    }

    private void initViews(View v) {
        month_head = v.findViewById(R.id.month_head);
        text_head = v.findViewById(R.id.text_head);
        count_list = v.findViewById(R.id.counters_list);
        action_left = v.findViewById(R.id.action_left);
        action_rigth = v.findViewById(R.id.action_rigth);
    }
}
