package com.patternjkh.ui.webcams;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Webcam;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WebcamsFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";

    private String hex;

    private TextView tvEmpty;
    private ListView lvWebcams;
    private Button btnNoInternetRefresh;
    private LinearLayout layoutNoInternet;
    private ConstraintLayout layoutMain;
    private ProgressDialog dialog;

    private Handler handler;
    private ArrayList<Webcam> webcams = new ArrayList<>();
    private WebcamAdapter webcamAdapter;
    private SharedPreferences sPref;
    private Server server = new Server(getActivity());
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, getActivity().MODE_PRIVATE);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (dialog != null)
                        dialog.dismiss();
                }
                if (msg.what == 0) {
                    showContent();
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.webcams_fragment, container, false);
        hex = sPref.getString("hex_color", "23b6ed");

        initViews(view);
        setTechColors(view);

        if (!ConnectionUtils.hasConnection(getActivity())) {
            showNoInternet();
        } else {
            getDataFromServer();
            hideNoInternet();
        }

        webcamAdapter = new WebcamAdapter(getActivity(), webcams, hex);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!ConnectionUtils.hasConnection(getActivity())) {
                    showNoInternet();
                } else {
                    getDataFromServer();
                    hideNoInternet();
                }
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
                    getDataFromServer();
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

    private void getDataFromServer() {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Загрузка вебкамер...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar= dialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        webcams.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = server.getWebcams(sPref.getString(PERSONAL_ACCOUNTS_PREF, "").replaceAll(",", ";"));
                parse_json_webcams(line);
            }
        }).start();
    }

    private void parse_json_webcams(String line) {
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_webcams = json.getJSONArray("data");
            for (int i = 0; i < json_webcams.length(); i++) {
                // Запишем дом
                JSONObject json_house = json_webcams.getJSONObject(i);
                String address = json_house.getString("Address");
                String url = json_house.getString("Url");
                webcams.add(new Webcam(address, url));
            }

            Collections.sort(webcams, new Comparator<Webcam>() {
                @Override
                public int compare(Webcam o1, Webcam o2) {
                    return o1.getAddress().compareTo(o2.getAddress());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        handler.sendEmptyMessage(0);
    }

    private void showContent() {
        if (webcams.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvWebcams.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvWebcams.setVisibility(View.VISIBLE);
            lvWebcams.setAdapter(webcamAdapter);
        }
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
        tvEmpty = view.findViewById(R.id.tv_webcams_empty);
        lvWebcams = view.findViewById(R.id.lv_webcams);
        swipeRefreshLayout = view.findViewById(R.id.swipe_questions);
        layoutMain = view.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = view.findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = view.findViewById(R.id.btn_no_internet_refresh);
    }
}