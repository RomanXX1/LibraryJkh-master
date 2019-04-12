package com.patternjkh.ui.others;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.R;

public class PersonalAccountNotAddedFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private String hex;
    private LinearLayout layoutTech;

    public PersonalAccountNotAddedFragment() {
    }

    public static PersonalAccountNotAddedFragment newInstance() {
        PersonalAccountNotAddedFragment fragment = new PersonalAccountNotAddedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_personal_account_not_added, container, false);


        SharedPreferences sPref = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        setTechColors(v);

        TextView tvAddLs = v.findViewById(R.id.tv_add_ls);
        tvAddLs.setTextColor(Color.parseColor("#" + hex));
        tvAddLs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPersonalAccountActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                getActivity().finish();
            }
        });

        return v;
    }

    private void setTechColors(View view) {
        TextView tvTech = view.findViewById(R.id.tv_tech);
        CardView cvDisp = view.findViewById(R.id.card_view_img_tech);
        layoutTech = view.findViewById(R.id.layout_tech);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTech.setTextColor(Color.parseColor("#" + hex));
            cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
        }
        layoutTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TechSendActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_left_activity_in, R.anim.move_rigth_activity_out);
            }
        });
    }
}
