package com.patternjkh.ui.news;

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
import com.patternjkh.data.New;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsFragment extends Fragment{

    private static final String APP_SETTINGS = "global_settings";

    private String login, hex;

    private ListView lvNews;
    private Switch chk_readed;
    private LinearLayout layoutCheckBox, layoutNoInternet;
    private ConstraintLayout layoutMain;
    private TextView tvDataEmpty;
    private Button btnNoInternetRefresh;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog dialog;

    private DB db;
    private SharedPreferences sPref;
    private Handler handler;
    private NewsAdapter newsAdapter;
    private Server server = new Server(getActivity());
    private ArrayList<New> newsAll = new ArrayList<>();
    private ArrayList<New> newsUnread = new ArrayList<>();
    private AppStyleManager appStyleManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        login = sPref.getString("login_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");

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
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        initViews(view);
        setTechColors(view);

        getNewsFromServer();

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
            chk_readed.getThumbDrawable().setTintList(buttonStates);
            chk_readed.getTrackDrawable().setTintList(buttonStates);
        }

        appStyleManager = AppStyleManager.getInstance(getActivity(), hex);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewsFromServer();
            }
        });

        chk_readed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCheckBox();
            }
        });
        return view;
    }

    private void handleCheckBox() {
        if (chk_readed.isChecked()) {
            newsAdapter = new NewsAdapter(getActivity(), newsAll, db, login, hex, appStyleManager);
            lvNews.setAdapter(newsAdapter);
            checkNewsArray();
        } else {
            newsAdapter = new NewsAdapter(getActivity(), newsUnread, db, login, hex, appStyleManager);
            lvNews.setAdapter(newsAdapter);
            checkNewsArray();
        }
    }

    private void filldata() {
        newsAll.clear();
        newsUnread.clear();

        Cursor cursor = db.getDataFromTable(db.TABLE_NEWS);
        cursor.moveToFirst();
        if (cursor.moveToFirst()){
            do {
                String name = cursor.getString(cursor.getColumnIndex(db.COL_NAME));
                String id   = String.valueOf(cursor.getInt(cursor.getColumnIndex(db.COL_ID)));
                String date = cursor.getString(cursor.getColumnIndex(db.COL_DATE));
                String text = cursor.getString(cursor.getColumnIndex(db.COL_TEXT));
                String isReaded_txt = cursor.getString(cursor.getColumnIndex(db.COL_IS_READ));
                boolean isReaded = StringUtils.convertStringToBoolean(isReaded_txt);
                if (!isReaded) {
                    newsUnread.add(new New(id, date, name, text, isReaded));
                }
                newsAll.add(new New(id, date, name, text, isReaded));
            } while (cursor.moveToNext());
        }
        cursor.close();

        checkNewsArray();
    }

    private void checkNewsArray() {
        if (newsAll.size() == 0 && newsUnread.size() == 0) {
            layoutCheckBox.setVisibility(View.GONE);
            tvDataEmpty.setText("Данных нет");
            tvDataEmpty.setVisibility(View.VISIBLE);
        } else if (newsAll.size() != 0 && newsUnread.size() == 0) {
            layoutCheckBox.setVisibility(View.VISIBLE);
            if (chk_readed.isChecked()) {
                tvDataEmpty.setVisibility(View.GONE);
            } else {
                tvDataEmpty.setText("Непрочитанных нет");
                tvDataEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            layoutCheckBox.setVisibility(View.VISIBLE);
            tvDataEmpty.setVisibility(View.GONE);
            if (chk_readed.isChecked()) {
                newsAdapter = new NewsAdapter(getActivity(), newsAll, db, login, hex, appStyleManager);
            } else {
                newsAdapter = new NewsAdapter(getActivity(), newsUnread, db, login, hex, appStyleManager);
            }
            lvNews.setAdapter(newsAdapter);
        }
    }

    private void getNewsFromServer() {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Синхронизация данных...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (!ConnectionUtils.hasConnection(getActivity())) {
            showNoInternet();
        } else {
            hideNoInternet();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = server.get_data_news(login);
                if (!line.equals("xxx")) {
                    parse_json_news(line);
                } else {
                    handler.sendEmptyMessage(4);
                }
            }
        }).start();

        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showNoInternet() {
        layoutNoInternet.setVisibility(View.VISIBLE);
        layoutMain.setVisibility(View.GONE);
        btnNoInternetRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(getActivity())) {
                    getNewsFromServer();
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

    void parse_json_news(String line) {
        db.del_table(DB.TABLE_NEWS);
        newsUnread.clear();
        newsAll.clear();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_news = json_data.getJSONObject(i);

                String name_news = json_news.getString("Header");
                String date_news = json_news.getString("Created");
                String id_news = json_news.getString("ID");
                String text_news = json_news.getString("Text");
                String isRead = json_news.getString("IsReaded");
                boolean isNewRead = StringUtils.convertStringToBoolean(isRead);
                if (!isNewRead) {
                    newsUnread.add(new New(id_news, name_news, text_news, date_news, isNewRead));
                }
                newsAll.add(new New(id_news, name_news, text_news, date_news, isNewRead));
                db.add_news(Integer.valueOf(id_news), name_news, text_news, date_news, isRead);
            }
        } catch (Exception e) {
            Logger.errorLog(NewsFragment.this.getClass(), e.getMessage());
        }

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
        layoutCheckBox = view.findViewById(R.id.layout_notifications_checkbox);
        tvDataEmpty = view.findViewById(R.id.tv_notifications_empty);
        chk_readed = view.findViewById(R.id.chk_readed);
        lvNews = view.findViewById(R.id.work_list);
        swipeRefreshLayout = view.findViewById(R.id.swipe_news);
        layoutMain = view.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = view.findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = view.findViewById(R.id.btn_no_internet_refresh);
    }
}