package com.patternjkh.ui.others;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.enums.TimeOfDay;
import com.patternjkh.utils.TimeUtils;

public class HelloFragment extends Fragment {

    private static final String FIO = "FIO";

    private String mFio;

    public HelloFragment() {
    }

    public static HelloFragment newInstance(String param1) {
        HelloFragment fragment = new HelloFragment();
        Bundle args = new Bundle();
        args.putString(FIO, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFio = getArguments().getString(FIO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hello, container, false);

        TextView mFIOTextView = v.findViewById(R.id.txt_fio);
        TextView mHelloTextView = v.findViewById(R.id.txt_hello);

        mFIOTextView.setText(mFio.concat(","));

        TimeOfDay timeOfDay = TimeUtils.identifyTimeOfDay();
        mHelloTextView.setText(getHello(timeOfDay));

        return v;
    }

    private String getHello(TimeOfDay timeOfDay) {

        String hello = null;

        switch (timeOfDay) {
            case MORNING:
                hello = getString(R.string.good_morning);
                break;
            case AFTERNOON:
                hello = getString(R.string.good_afternoon);
                break;
            case EVENING:
                hello = getString(R.string.good_evening);
                break;
            case NIGHT:
                hello = getString(R.string.good_night);
                break;
        }
        return hello;
    }
}
