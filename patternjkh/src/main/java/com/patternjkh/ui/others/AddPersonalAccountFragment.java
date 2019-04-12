package com.patternjkh.ui.others;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Flat;
import com.patternjkh.data.HouseNumber;
import com.patternjkh.data.Street;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.patternjkh.utils.ToastUtils.showToast;

public class AddPersonalAccountFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PHONE = "phone";

    private String mPhone, hex, id_house = "", id_flat = "", street_name = "";
    private boolean getFlats, getHouseNumbers;

    private Spinner mStreetSpinner, mFlatNumberSpinner, mHouseNumberSpinner;
    private EditText etLs;
    private Button mAddButton;
    private TextView tvLs, tvStreet, tvHouseNumber, tvFlat;
    private ProgressDialog mDialog;

    private SharedPreferences sPref;
    private List<Flat> mFlats;
    private List<Street> mStreets;
    private List<HouseNumber> mHouseNumbers;
    private ArrayAdapter<Flat> mFlatAdapter;
    private ArrayAdapter<Street> mStreetAdapter;
    private ArrayAdapter<HouseNumber> mHouseNumberAdapter;
    private Server server = new Server(getActivity());
    private Handler handler, mAddIdentToAccountHandler;
    private DB db;
    private OnAddPersonalAccountFragmentInteractionListener mListener;

    public AddPersonalAccountFragment() {
    }

    public static AddPersonalAccountFragment newInstance(String phone) {
        AddPersonalAccountFragment fragment = new AddPersonalAccountFragment();
        Bundle args = new Bundle();
        args.putString(PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPhone = getArguments().getString(PHONE);
        }

        db = new DB(getContext());
        db.open();

        mFlats = new ArrayList<>();
        mStreets = new ArrayList<>();
        mHouseNumbers = new ArrayList<>();
        sPref = getContext().getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        ident_spinners();

        mFlatAdapter = new FlatAdapter(getContext());
        mStreetAdapter = new StreetAdapter(getContext());
        mHouseNumberAdapter = new HouseNumberAdapter(getContext());

        handler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    mFlatNumberSpinner.setSelection(0);
                    mFlatAdapter.notifyDataSetChanged();
                } else if (message.what == 2) {
                    mStreetSpinner.setSelection(0);
                    mStreetAdapter.notifyDataSetChanged();
                } else if (message.what == 3) {
                    mHouseNumberSpinner.setSelection(0);
                    mHouseNumberAdapter.notifyDataSetChanged();
                }
                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog != null)
                        mDialog.dismiss();
                }
            }
        };

        mAddIdentToAccountHandler = new Handler() {
            public void handleMessage(Message message) {

                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
                if (message.what == 1) {
                    String ls = etLs.getText().toString().replaceAll("-","");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setMessage("Лицевой счет " + ls + " привязан к аккаунту " + mPhone);
                    builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mListener != null) {
                                mListener.onAddPersonalAccountFragmentInteraction();
                            }
                        }
                    });

                    AlertDialog dialog = builder.create();
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        dialog.show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
                        }
                    }
                } else if (message.what == 2) {
                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }
                    DialogCreator.showErrorCustomDialog(getActivity(), error, hex);
                }  else if (message.what == 3) {
                    if (message.obj != null) {
                        String errorMessage = String.valueOf(message.obj);
                        errorMessage = errorMessage.replaceFirst("error: ", "");
                        showToastHere(StringUtils.firstUpperCase(errorMessage));
                    }

                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_personal_account, container, false);

        initViews(v);
        initListeners();
        setTechColors(v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setButtonEnabled(false);
        mStreetSpinner.setAdapter(mStreetAdapter);
        mFlatNumberSpinner.setAdapter(mFlatAdapter);
        mHouseNumberSpinner.setAdapter(mHouseNumberAdapter);
    }

    void ident_spinners() {

        showProgress("Актуализация домов...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Получим список домов, спросим нужный дом, подтянем информацию по дому
                String line = "";
                Boolean rezult = true;
                try {
                    line = server.getStreets();
                } catch (Exception e) {
                    rezult = false;
                }
                if (rezult) {
                    db.del_table(DB.TABLE_HOUSES);
                    db.del_table(DB.TABLE_FLATS);
                    db.del_table(DB.TABLE_LS);
                    db.del_table(DB.TABLE_STREETS);
                    parse_json_streets(line);
                    handler.sendEmptyMessage(2);
                }
            }

        }).start();
    }

    void parse_json_streets(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_streets = json.getJSONArray("Streets");
            for (int i = 0; i < json_streets.length(); i++) {
                // Запишем дом
                JSONObject json_house = json_streets.getJSONObject(i);
                String street_name = json_house.getString("Name");
                db.addStreet(street_name);
                mStreets.add(new Street(street_name));
            }

            Collections.sort(mStreets, new Comparator<Street>() {
                @Override
                public int compare(Street o1, Street o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void change_house_numbers(String street) {
        mHouseNumbers.clear();
        mHouseNumbers.add(new HouseNumber("-", "0"));
        get_data_for_house_number(street);
    }

    void get_data_for_house_number(final String street) {
        Cursor cursor = db.getDataFromTable(DB.TABLE_HOUSE_NUMBERS);

        if (!getHouseNumbers) {
            showProgress("Актуализация номеров домов...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line = "";
                    Boolean rezult = true;
                    try {
                        line = server.getHouseNumber(street);
                    } catch (Exception e) {
                        rezult = false;
                    }
                    if (rezult) {
                        db.del_table(DB.TABLE_HOUSE_NUMBERS);
                        parse_json_house_numbers(line);
                        getHouseNumbers = true;
                        handler.sendEmptyMessage(3);
                    }
                }

            }).start();
        } else {
            if (cursor.moveToFirst()) {
                do {
                    if (street_name.equals(cursor.getString(cursor.getColumnIndex(DB.COL_ID_HOUSE)))) {
                        mHouseNumbers.add(new HouseNumber(cursor.getString(cursor.getColumnIndex(DB.COL_NAME)),
                                cursor.getString(cursor.getColumnIndex(DB.COL_ID_HOUSE))));
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            mHouseNumberSpinner.setSelection(0);
            mHouseNumberAdapter.notifyDataSetChanged();
        }
    }

    void parse_json_house_numbers(String line) {
        try {
            JSONObject json = new JSONObject(line);
            // Получим квартиры
            try {
                JSONArray json_house_numbers = json.getJSONArray("Houses");
                for (int j = 0; j < json_house_numbers.length(); j++) {
                    JSONObject json_house_number = json_house_numbers.getJSONObject(j);
                    String house_id = json_house_number.getString("ID");
                    String house_number = json_house_number.getString("Number");
                    db.addHouseNumber("дом " + house_number, house_id);
                    mHouseNumbers.add(new HouseNumber("дом " + house_number, house_id));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Изменение данных в коллекции Города
    void change_flats(String id_group) {
        mFlats.clear();
        mFlats.add(new Flat("-", "0", "-"));
        get_data_for_house(id_group);

    }

    // Получим города для региона
    void get_data_for_house(final String id_house) {
        Cursor cursor_flat = db.getDataFromTable(DB.TABLE_FLATS, DB.COL_NAME);

        if (!getFlats) {
            showProgress("Актуализация квартир...");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Получим список домов, спросим нужный дом, подтянем информацию по дому
                    String line = "";
                    Boolean rezult = true;
                    try {
                        line = server.getFlatsLS(id_house);
                    } catch (Exception e) {
                        rezult = false;
                    }
                    if (rezult) {
                        db.del_table(DB.TABLE_FLATS);
                        db.del_table(DB.TABLE_LS);
                        parse_json_flats(line, id_house);
                        getFlats = true;
                        handler.sendEmptyMessage(1);
                    }
                }

            }).start();
        } else {
            if (cursor_flat.moveToFirst()) {
                do {
                    if (id_house.equals(cursor_flat.getString(cursor_flat.getColumnIndex(DB.COL_ID_HOUSE)))) {
                        mFlats.add(new Flat(cursor_flat.getString(cursor_flat.getColumnIndex(DB.COL_NAME)),
                                cursor_flat.getString(cursor_flat.getColumnIndex(DB.COL_ID_FLAT)),
                                cursor_flat.getString(cursor_flat.getColumnIndex(DB.COL_NAME_SORT))));
                    }
                } while (cursor_flat.moveToNext());
            }
            cursor_flat.close();
            mFlatNumberSpinner.setSelection(0);
            mFlatAdapter.notifyDataSetChanged();
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
                    db.addFlat(flat_name + " кв.", house_id, flat_id, flat_name_sort);
                    mFlats.add(new Flat(flat_name + " кв.", flat_id, flat_name_sort));
                }

                Collections.sort(mFlats, new Comparator<Flat>() {
                    @Override
                    public int compare(Flat o1, Flat o2) {
                        return o1.getName_sort().compareTo(o2.getName_sort());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Обработчики выбора
    private void set_street(int position){
        if (mStreets.size() > 0) {
            Street street = mStreets.get(position);
            street_name = street.getName();
        }
        getFlats = false;
        getHouseNumbers = false;
        if (!street_name.equals("")) {
            change_house_numbers(street_name);
        }
        if (!TextUtils.isEmpty(etLs.getText().toString())) {
            setButtonEnabled(true);
        } else {
            setButtonEnabled(false);
        }
    }

    private void set_house(int position) {
        if (mHouseNumbers.size() > 0) {
            HouseNumber houseNumber = mHouseNumbers.get(position);
            id_house = houseNumber.getId();
        }

        getFlats = false;

        if (!id_house.equals("")) {
            change_flats(id_house);
        }

        if (!TextUtils.isEmpty(etLs.getText().toString())) {
            setButtonEnabled(true);

        } else {
            setButtonEnabled(false);
        }
    }

    private void set_flat(int position) {
        if (mFlats.size() > 0) {
            Flat flat = mFlats.get(position);
            id_flat = flat.getId();
        }

        if (!TextUtils.isEmpty(etLs.getText().toString())) {
            setButtonEnabled(true);

        } else {
            setButtonEnabled(false);
        }
    }

    private void initListeners() {

        mStreetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                set_street(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        mHouseNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                set_house(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mFlatNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                set_flat(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        etLs.addTextChangedListener(new TextWatcher() {
            int len=0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String str = etLs.getText().toString();
                len = str.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mStreetSpinner.getSelectedItem() != null
                        && mFlatNumberSpinner.getSelectedItem() != null
                        && !TextUtils.isEmpty(etLs.getText().toString())
                ) {
                    setButtonEnabled(true);
                } else {
                    setButtonEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etLs.getWindowToken(), 0);

                showProgress("Привязка л/сч к аккаунту...");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
                    progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                String personalAccountNumberWithOutHyphen = etLs.getText().toString().replaceAll("-","");

                addIdentToAccount(mPhone, personalAccountNumberWithOutHyphen, id_house, id_flat);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddPersonalAccountFragmentInteractionListener) {
            mListener = (OnAddPersonalAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAddPersonalAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addIdentToAccount(final String phone, final String personalAccountNumber, final String houseId, final String flatId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.addIdentToAccount(phone, personalAccountNumber, houseId, flatId);

                if (line.equals("ok")) {
                    mAddIdentToAccountHandler.sendEmptyMessage(1);
                } else if (line.contains("error")) {
                    Message msg = mAddIdentToAccountHandler.obtainMessage(3, 0, 0, line);
                    mAddIdentToAccountHandler.sendMessage(msg);
                } else {
                    Message msg = mAddIdentToAccountHandler.obtainMessage(2, 0, 0, line);
                    mAddIdentToAccountHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private class StreetAdapter extends ArrayAdapter<Street> {

        public StreetAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, mStreets);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Street street = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_1, null);
            }
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(street.getName());

            return convertView;
        }

        @Override
        public int getCount() {
            return mStreets.size();
        }

        @Override
        public Street getItem(int position) {
            return mStreets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private class HouseNumberAdapter extends ArrayAdapter<HouseNumber> {

        public HouseNumberAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, mHouseNumbers);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HouseNumber houseNumber = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_1, null);
            }
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(houseNumber.getNumber());

            return convertView;
        }


        @Override
        public int getCount() {
            return mHouseNumbers.size();
        }

        @Override
        public HouseNumber getItem(int position) {
            return mHouseNumbers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    private class FlatAdapter extends ArrayAdapter<Flat> {

        public FlatAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, mFlats);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Flat flat = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_1, null);
            }
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(flat.getName());

            return convertView;
        }


        @Override
        public int getCount() {
            return mFlats.size();
        }

        @Override
        public Flat getItem(int position) {
            return mFlats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    private void setTechColors(View view) {
        TextView tvTech = view.findViewById(R.id.tv_tech);
        CardView cvDisp = view.findViewById(R.id.card_view_img_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
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

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        mAddButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        etLs.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        tvLs.setTextColor(Color.parseColor("#" + hex));
        tvStreet.setTextColor(Color.parseColor("#" + hex));
        tvHouseNumber.setTextColor(Color.parseColor("#" + hex));
        tvFlat.setTextColor(Color.parseColor("#" + hex));
    }

    @SuppressLint("NewApi")
    private void setButtonEnabled(boolean isEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isEnabled) {
                mAddButton.setEnabled(true);
                mAddButton.setClickable(true);
                mAddButton.setFocusable(true);
                mAddButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#" + hex), PorterDuff.Mode.SRC_IN));
            } else {
                mAddButton.setEnabled(false);
                mAddButton.setClickable(false);
                mAddButton.setFocusable(false);
                mAddButton.getBackground().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.ligth_grey), PorterDuff.Mode.SRC_IN));
            }
        }
        mAddButton.setClickable(isEnabled);
        mAddButton.setFocusable(isEnabled);
        mAddButton.setEnabled(isEnabled);
    }

    private void showProgress(String title) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage(title);
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }

    private void showToastHere(String title) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            showToast(getActivity(), title);
        }
    }

    private void initViews(View v) {
        mStreetSpinner = v.findViewById(R.id.spinner_house);
        mFlatNumberSpinner = v.findViewById(R.id.spinner_flat_number);
        mHouseNumberSpinner = v.findViewById(R.id.spinner_house_number);
        etLs = v.findViewById(R.id.et_personal_account_number);
        mAddButton = v.findViewById(R.id.btn_add);
        tvLs = v.findViewById(R.id.tv_ls_title);
        tvStreet = v.findViewById(R.id.tv_add_ls_street);
        tvHouseNumber = v.findViewById(R.id.tv_add_ls_house_number);
        tvFlat = v.findViewById(R.id.tv_add_ls_flat);
    }
}