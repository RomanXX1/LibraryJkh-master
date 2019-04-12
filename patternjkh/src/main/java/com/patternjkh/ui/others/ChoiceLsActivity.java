package com.patternjkh.ui.others;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Flat;
import com.patternjkh.data.House;
import com.patternjkh.data.LS;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.patternjkh.utils.ToastUtils.showToast;

public class ChoiceLsActivity extends AppCompatActivity {

    private Spinner ch_house, ch_flat, ch_ls;
    private boolean getFlats = false;

    private EditText etPhone;
    private Button btn_choice;
    private ProgressDialog dialog;

    private ArrayList<House> houses = new ArrayList<>();
    private ArrayList<Flat> flats = new ArrayList<>();
    private ArrayList<LS> lsl = new ArrayList<>();
    private DB db = new DB(this);
    private Server server = new Server(this);
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_ls);

        db.open();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle(R.string.choice_ls);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });

        ch_house = findViewById(R.id.ch_house);
        ch_flat = findViewById(R.id.ch_flat);
        ch_ls = findViewById(R.id.ch_ls);
        etPhone = findViewById(R.id.ed_Telefone);

        handler = new Handler() {
            public void handleMessage (Message message){
                if (!isFinishing() && !isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }
                if (message.what == 1) {
                    showToastHere("Не удалось. Попробуйте позже");
                } else if (message.what == 2) {
                    ident_spinners();
                } else if (message.what == 3) {
                    ident_spinners_house(ch_house.getSelectedItemPosition());
                }
            }
        };

        // Заполним три колллекции структур для отборов при выборе
        ident_spinners();

        // Установим обработчики событий для полей выбора
        ident_listeners();

        // Кнопка - Выбор
        btn_choice = findViewById(R.id.btn_choice);
        btn_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lsl.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("ls", lsl.get(ch_ls.getSelectedItemPosition()).getName());
                    intent.putExtra("adress", houses.get(ch_house.getSelectedItemPosition()).getName());
                    intent.putExtra("id_house", houses.get(ch_house.getSelectedItemPosition()).getFias());
                    intent.putExtra("flat", flats.get(ch_flat.getSelectedItemPosition()).getName());
                    intent.putExtra("telefone", etPhone.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                } else if (flats.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("ls", "");
                    intent.putExtra("adress", houses.get(ch_house.getSelectedItemPosition()).getName());
                    intent.putExtra("id_house", houses.get(ch_house.getSelectedItemPosition()).getFias());
                    intent.putExtra("flat", flats.get(ch_flat.getSelectedItemPosition()).getName());
                    intent.putExtra("telefone", etPhone.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                } else if (houses.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("ls", "");
                    intent.putExtra("adress", houses.get(ch_house.getSelectedItemPosition()).getName());
                    intent.putExtra("id_house", houses.get(ch_house.getSelectedItemPosition()).getFias());
                    intent.putExtra("flat", "");
                    intent.putExtra("telefone", etPhone.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                } else {
                    finish();
                    overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                }
            }
        });
    }

    void ident_listeners() {
        ch_house.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getFlats = false;
                ident_spinners_house(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ch_flat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ident_spinners_flat(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    void ident_spinners() {

        Cursor cursor = db.getDataFromTable(db.TABLE_HOUSES);

        if (cursor.getCount() == 0) {
            // Запустим крутилку
            dialog = new ProgressDialog(ChoiceLsActivity.this);
            dialog.setMessage("Актуализация домов...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Получим список домов, спросим нужный дом, подтянем информацию по дому
                    String line = "";
                    Boolean rezult = true;
                    try {
                        line = server.getHouses();
                    } catch (Exception e) {
                        rezult = false;
                    }
                    if (rezult) {
                        db.del_table(db.TABLE_HOUSES);
                        db.del_table(db.TABLE_FLATS);
                        db.del_table(db.TABLE_LS);
                        if (line.equals("{\"Houses\":[]}")) {
                            handler.sendEmptyMessage(4);
                        } else {
                            parse_json_houses(line);
                            handler.sendEmptyMessage(2);
                        }
                    }
                }

            }).start();
        } else {
            boolean first_house = true;
            if (cursor.moveToFirst()) {
                do {
                    houses.add(new House(cursor.getString(cursor.getColumnIndex(db.COL_NAME)),
                            cursor.getString(cursor.getColumnIndex(db.COL_FIAS))));
                    if (first_house) {
                        first_house = false;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            String[] houses_Array = new String[houses.size()];
            for (int i = 0; i < houses.size(); i++) {
                houses_Array[i] = houses.get(i).getName();
            }
            ArrayAdapter<String> house_adapter = new ArrayAdapter<String>(this, R.layout.choice_type, R.id.text_name_type, houses_Array);
            ch_house.setAdapter(house_adapter);
        }
    }

    // изменен дом - обновим коллекции: помещения, лиц. счета
    void ident_spinners_house(int j) {

        final String house_name = houses.get(j).getFias();
        flats.clear();
        lsl.clear();

        Cursor cursor_flat = db.getDataFromTable(db.TABLE_FLATS, db.COL_NAME);

        if (getFlats == false) {
            // Запустим крутилку
            dialog = new ProgressDialog(ChoiceLsActivity.this);
            dialog.setMessage("Актуализация квартир, лиц.счетов...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Получим список домов, спросим нужный дом, подтянем информацию по дому
                    String line = "";
                    Boolean rezult = true;
                    try {
                        line = server.getFlatsLS(house_name);
                    } catch (Exception e) {
                        rezult = false;
                    }
                    if (rezult) {
                        db.del_table(db.TABLE_FLATS);
                        db.del_table(db.TABLE_LS);
                        parse_json_flats(line, house_name);
                        getFlats = true;
                        handler.sendEmptyMessage(3);
                    }
                }

            }).start();
        } else {
            boolean first_flat = true;
            String flat_name = "";
            if (cursor_flat.moveToFirst()) {
                do {
                    if (house_name.equals(cursor_flat.getString(cursor_flat.getColumnIndex(db.COL_ID_HOUSE)))) {
                        flats.add(new Flat(cursor_flat.getString(cursor_flat.getColumnIndex(db.COL_NAME)),
                                cursor_flat.getString(cursor_flat.getColumnIndex(db.COL_ID_FLAT)),
                                cursor_flat.getString(cursor_flat.getColumnIndex(db.COL_NAME_SORT))));
                        if (first_flat) {
                            flat_name = cursor_flat.getString(cursor_flat.getColumnIndex(db.COL_ID_FLAT));
                            first_flat = false;
                        }
                    }
                } while (cursor_flat.moveToNext());
            }
            cursor_flat.close();
            String [] flat_Array = new String[flats.size()];
            for (int i = 0; i < flats.size(); i++) {
                flat_Array[i] = flats.get(i).getName();
            }
            ArrayAdapter<String> flat_adapter = new ArrayAdapter<String>(this, R.layout.choice_type, R.id.text_name_type, flat_Array);
            ch_flat.setAdapter(flat_adapter);

            Cursor cursor_ls = db.getDataFromTable(db.TABLE_LS);
            if (cursor_ls.moveToFirst()) {
                do {
                    if (flat_name.equals(cursor_ls.getString(cursor_ls.getColumnIndex(db.COL_ID_FLAT)))) {
                        lsl.add(new LS(cursor_ls.getString(cursor_ls.getColumnIndex(db.COL_NAME)),
                                cursor_ls.getString(cursor_ls.getColumnIndex(db.COL_ID_FLAT))));
                    }
                } while (cursor_ls.moveToNext());
            }
            cursor_ls.close();
            String [] lsl_Array = new String[lsl.size()];
            for (int i = 0; i < lsl.size(); i++) {
                lsl_Array[i] = lsl.get(i).getName();
            }
//        ArrayAdapter<String> lsl_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lsl_Array);
            ArrayAdapter<String> lsl_adapter = new ArrayAdapter<String>(this, R.layout.choice_type, R.id.text_name_type, lsl_Array);
            ch_ls.setAdapter(lsl_adapter);
        }
    }

    // изменена квартира - обновим коллекцию лиц. счетов
    void ident_spinners_flat(int j) {

        String flat_name = flats.get(j).getId();
        lsl.clear();

        Cursor cursor_ls = db.getDataFromTable(db.TABLE_LS);
        if (cursor_ls.moveToFirst()) {
            do {
                if (flat_name.equals(cursor_ls.getString(cursor_ls.getColumnIndex(db.COL_ID_FLAT)))) {
                    lsl.add(new LS(cursor_ls.getString(cursor_ls.getColumnIndex(db.COL_NAME)),
                            cursor_ls.getString(cursor_ls.getColumnIndex(db.COL_ID_FLAT))));
                }
            } while (cursor_ls.moveToNext());
        }
        cursor_ls.close();
        String [] lsl_Array = new String[lsl.size()];
        for (int i = 0; i < lsl.size(); i++) {
            lsl_Array[i] = lsl.get(i).getName();
        }
//        ArrayAdapter<String> lsl_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lsl_Array);
        ArrayAdapter<String> lsl_adapter = new ArrayAdapter<String>(this, R.layout.choice_type, R.id.text_name_type, lsl_Array);
        ch_ls.setAdapter(lsl_adapter);
    }


    // Изначальное заполнения БД для заполнения Спиннеров
    // распарсим json приходящий с домами, квартирами, лиц. счетами
    void parse_json_houses(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_houses = json.getJSONArray("Houses");
            for (int i = 0; i < json_houses.length(); i++) {
                // Запишем дом
                JSONObject json_house = json_houses.getJSONObject(i);
                String house_id = json_house.getString("ID");
                String house_name = json_house.getString("Address");
                db.addHouse(house_name, house_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void parse_json_flats(String line, String house_id) {
        try {
            JSONObject json = new JSONObject(line);
            // Получим квартиры
            try {
                JSONArray json_flats = json.getJSONArray("Premises");
                for (int j = 0; j < json_flats.length(); j++) {
                    JSONObject json_flat = json_flats.getJSONObject(j);
                    String flat_id = json_flat.getString("ID");
                    String flat_name = json_flat.getString("Number");
                    String flat_name_sort = json_flat.getString("Number");
                    if (flat_name_sort.length() == 1) {
                        flat_name_sort = "00" + flat_name_sort;
                    } else if (flat_name_sort.length() == 2) {
                        flat_name_sort = "0" + flat_name_sort;
                    }
                    flat_id = flat_id + "_" + house_id;
                    db.addFlat(flat_name + " кв.", house_id, flat_id, flat_name_sort);

                    try {
                        // Получим лиц. счета
                        JSONObject json_ls = json_flat.getJSONObject("Account");
                        String ls_number = json_ls.getString("Ident");
                        String ls_fio = json_ls.getString("FIO");
                        db.addLS(ls_number, flat_id, ls_fio);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToastHere(String title) {
        if (!isFinishing() && !isDestroyed()) {
            showToast(ChoiceLsActivity.this, title);
        }
    }
}
