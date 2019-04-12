package com.patternjkh.ui.statement;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.data.PaysHistoryItem;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class MobilePaysFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private String hex = "23b6ed";

    private RecyclerView rvHistoryOsv;
    private LinearLayout layoutTitle;

    private ArrayList<PaysHistoryItem> items = new ArrayList<>();
    private DB db;
    private SharedPreferences sPref;
    private Handler handler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        getParamsFromPrefs();

        db = new DB(context);
        db.open();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile_pays, container, false);
        initViews(view);
        setColors();

        rvHistoryOsv.setLayoutManager(new LinearLayoutManager(getActivity()));
        final PaysHistoryAdapter adapter = new PaysHistoryAdapter(items);
        rvHistoryOsv.setAdapter(adapter);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    adapter.notifyDataSetChanged();
                }
            }
        };

        return view;
    }

    @Override
    public void onResume() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromDb();
            }
        }).start();

        super.onResume();
    }

    private void getDataFromDb() {
        Cursor cursor = db.getDataFromTable(DB.TABLE_MOBILE_PAYS);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(db.COL_DATE));
                String status = cursor.getString(cursor.getColumnIndex(db.COL_STATUS));
                String paySum = cursor.getString(cursor.getColumnIndex(db.COL_PAY_SUM));

                if (status.equals("Обработан")) {
                    status = "Оплачен";
                }

                items.add(new PaysHistoryItem(date, status, paySum));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (items != null && items.size() > 2) {
            Collections.reverse(items);
        }
        handler.sendEmptyMessage(0);
    }

    private void getParamsFromPrefs() {
        if (getActivity() != null) {
            sPref = getActivity().getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
            hex = sPref.getString("hex_color", "23b6ed");
        }
    }

    private void setColors() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layoutTitle.setBackgroundColor(Color.parseColor("#" + hex));
        }
    }

    private void initViews(View v) {
        layoutTitle = v.findViewById(R.id.layout_pays_history);
        rvHistoryOsv = v.findViewById(R.id.rv_history_pays);
    }
}
