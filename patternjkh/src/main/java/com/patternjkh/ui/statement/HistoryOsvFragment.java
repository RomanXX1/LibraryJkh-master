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
import com.patternjkh.data.OsvHistoryItem;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class HistoryOsvFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private String hex = "23b6ed";

    private RecyclerView rvHistoryOsv;
    private LinearLayout layoutTitle;

    private ArrayList<OsvHistoryItem> items = new ArrayList<>();
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
        View view = inflater.inflate(R.layout.fragment_history_osv, container, false);
        initViews(view);
        setColors();

        rvHistoryOsv.setLayoutManager(new LinearLayoutManager(getActivity()));
        final OsvHistoryAdapter adapter = new OsvHistoryAdapter(items);
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
        Cursor cursor = db.getDataFromTable(DB.TABLE_HISTORY_OSV);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(db.COL_DATE));
                String period = cursor.getString(cursor.getColumnIndex(db.COL_PERIOD));
                String paySum = cursor.getString(cursor.getColumnIndex(db.COL_PAY_SUM));

                items.add(new OsvHistoryItem(date, period, paySum));
            } while (cursor.moveToNext());
        }
        cursor.close();

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
        layoutTitle = v.findViewById(R.id.layout_osv_history);
        rvHistoryOsv = v.findViewById(R.id.rv_history_osv);
    }
}
