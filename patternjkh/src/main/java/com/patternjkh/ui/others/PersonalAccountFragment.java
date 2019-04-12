package com.patternjkh.ui.others;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static com.patternjkh.utils.ToastUtils.showToast;

public class PersonalAccountFragment extends Fragment implements OnLsDeleteListener {

    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";
    public static final String APP_SETTINGS = "global_settings";

    private String mPersonalAccounts, phone, ls, hex;

    private TextView mAddPersonalAccountTextView;
    private RecyclerView rvLs;

    private OnPersonalAccountFragmentInteractionListener mListener;
    private PersonalAccountsMainAdapter accountsAdapter;
    private ArrayList<String> personalAccounts = new ArrayList<>();
    private Server server = new Server(getActivity());
    private SharedPreferences sPref;

    public interface OnPersonalAccountFragmentInteractionListener {
        void onPersonalAccountFragmentInteraction();
    }

    public PersonalAccountFragment() {
    }

    public static PersonalAccountFragment newInstance(String personalAccount) {
        PersonalAccountFragment fragment = new PersonalAccountFragment();
        Bundle args = new Bundle();
        args.putString(PERSONAL_ACCOUNTS, personalAccount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPersonalAccountFragmentInteractionListener) {
            mListener = (OnPersonalAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPersonalAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPersonalAccounts = getArguments().getString(PERSONAL_ACCOUNTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_personal_account, container, false);
        initViews(v);

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        phone = sPref.getString("login_push", "");
        hex = sPref.getString("hex_color", "23b6ed");

        personalAccounts.addAll(Arrays.asList(mPersonalAccounts.split("\n")));

        accountsAdapter = new PersonalAccountsMainAdapter(personalAccounts);
        rvLs.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLs.setAdapter(accountsAdapter);

        SharedPreferences sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        String hex = sPref.getString("hex_color", "23b6ed");

        AppStyleManager appStyleManager = AppStyleManager.getInstance(getActivity(), hex);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAddPersonalAccountTextView.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        }

        initListeners();

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLsDeleteClicked(String ls) {
        this.ls = ls;

        if (ConnectionUtils.hasConnection(getActivity())) {
            showLsDeleteDialog();
        } else {
            DialogCreator.showInternetErrorDialog(getActivity(), hex);
        }
    }

    private void showLsDeleteDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Отвязать лицевой счет " + ls.replaceAll("\r", "") + " от аккаунта?");
        builder.setPositiveButton("Да",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Server server = new Server(getActivity());
                        String line = server.deleteAccount(phone, ls);
                        if (line.equals("ok")) {
                            mPersonalAccounts = mPersonalAccounts.replace("\r\n" + ls, "");
                            personalAccounts.remove(ls);
                            accountsAdapter.notifyDataSetChanged();
                            updateAccs();
                            accountsAdapter.notifyDataSetChanged();
                            showToastHere("Лс успешно удален");
                        } else {
                            showToastHere("Ошибка удаления аккаунта");
                        }
                    }
                });

        builder.setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#" + hex));
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
            }
        }
    }

    private void updateAccs() {
        String linePersonalAccounts = server.getAccountIdents(sPref.getString("login_push", ""));
        String personalAccounts = parseJsonPersonalAccounts(linePersonalAccounts);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("personalAccounts_pref", personalAccounts);
        ed.commit();

        Log.d("myLog", sPref.getString("personalAccounts_pref", ""));
    }

    private String parseJsonPersonalAccounts(String line) {
        StringBuffer personalAccounts = new StringBuffer();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                personalAccounts.append(json_data.get(i));
                if (i != json_data.length()-1) {
                    personalAccounts.append(",");
                }
            }
        } catch (Exception e) {
        }
        return personalAccounts.toString();
    }

    private void initListeners() {
        mAddPersonalAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPersonalAccountFragmentInteraction();
                }
            }
        });
    }

    private void showToastHere(String title) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            showToast(getActivity(), title);
        }
    }

    private void initViews(View v) {
        mAddPersonalAccountTextView = v.findViewById(R.id.txt_add_personal_account);
        rvLs = v.findViewById(R.id.rv_main_ls);
    }
}
