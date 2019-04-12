package com.patternjkh.ui.additionalServices;

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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static android.content.Context.MODE_PRIVATE;

public class AdditionalServicesFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private String login, pwd, hex;

    private RecyclerView lvServices;
    private Button btnNoInternetRefresh;
    private LinearLayout layoutNoInternet;
    private ConstraintLayout layoutMain;
    private TextView tvEmpty;
    private ProgressDialog dialog;

    private ArrayList<AdditionalService> services = new ArrayList<>();
    private ArrayList<AdditionalGroup> groups = new ArrayList<>();
    private SharedPreferences sPref;
    private Server server = new Server(getActivity());
    private Handler handler;
    private AdditionalsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        login = sPref.getString("login_pref", "");
        pwd = sPref.getString("pass_pref", "");
        hex = sPref.getString("hex_color", "23b6ed");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    showContent();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_additional_services, container, false);
        initViews(view);

        if (!ConnectionUtils.hasConnection(getActivity())) {
            showNoInternet();
        } else {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Загрузка услуг...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ProgressBar progressbar = dialog.findViewById(android.R.id.progress);
                    progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
            getDataFromServer();
            hideNoInternet();
        }
        adapter = new AdditionalsAdapter(getActivity(), services, groups, hex);

        setTechColors(view);

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

    private void showContent() {
        if (services.size() > 0) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            lvServices.setLayoutManager(layoutManager);
            lvServices.setAdapter(adapter);
            lvServices.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                }
            });
            lvServices.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        } else {
            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                if (dialog != null)
                    dialog.dismiss();
            }
            lvServices.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void getDataFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                parseData();
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void parseData() {

        services.clear();
        groups.clear();

        String line = "xxx";

        try {
            line = server.getAdditionals(login, pwd);
            line = line.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");

            try {
                BufferedReader br = new BufferedReader(new StringReader(line));
                InputSource is = new InputSource(br);
                Parser_Additionals xpp = new Parser_Additionals();
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SAXParser sp = factory.newSAXParser();
                XMLReader reader = sp.getXMLReader();
                reader.setContentHandler(xpp);
                reader.parse(is);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public class Parser_Additionals extends DefaultHandler {

        String groupName = "", id = "", name = "", address = "", descr = "", logo = "", phone = "";

        Parser_Additionals() {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.toLowerCase().equals("group")) {
                groupName = attributes.getValue("name").toString();
                groups.add(new AdditionalGroup(groupName));
            } else if (localName.toLowerCase().equals("additionalservice")) {
                id = attributes.getValue("id").toString();
                name = attributes.getValue("name").toString();
                address = attributes.getValue("address").toString();
                descr = attributes.getValue("description").toString();
                logo = attributes.getValue("logo").toString();
                phone = attributes.getValue("phone").toString();
                services.add(new AdditionalService(id, name, address, descr, logo, phone, groupName));
            }
        }
    }

    private void initViews(View view) {
        lvServices = view.findViewById(R.id.lv_services);
        tvEmpty = view.findViewById(R.id.tv_adds_empty);
        layoutMain = view.findViewById(R.id.main_layout_with_internet);
        layoutNoInternet = view.findViewById(R.id.layout_no_internet);
        btnNoInternetRefresh = view.findViewById(R.id.btn_no_internet_refresh);
    }
}
