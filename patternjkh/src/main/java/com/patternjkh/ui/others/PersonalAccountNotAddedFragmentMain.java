package com.patternjkh.ui.others;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;

public class PersonalAccountNotAddedFragmentMain extends Fragment {

    private OnPersonalAccountNotAddedFragmentInteractionListener mListener;

    public PersonalAccountNotAddedFragmentMain() {
        // Required empty public constructor
    }

    public static PersonalAccountNotAddedFragmentMain newInstance() {
        PersonalAccountNotAddedFragmentMain fragment = new PersonalAccountNotAddedFragmentMain();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_no_ls_main, container, false);

        TextView mPersonalAccountNotAddedTextView = v.findViewById(R.id.tv_personal_account_not_added);
        mPersonalAccountNotAddedTextView.setLinkTextColor(Color.BLUE);

        ClickableSpan normalLinkClickSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onPersonalAccountNotAddedFragmentInteraction();
                }
            }
        };

        showTextAddAccWithLink(mPersonalAccountNotAddedTextView,
                new String[] { "сюда" },
                new ClickableSpan[] { normalLinkClickSpan });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPersonalAccountNotAddedFragmentInteractionListener) {
            mListener = (OnPersonalAccountNotAddedFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPersonalAccountNotAddedFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnPersonalAccountNotAddedFragmentInteractionListener {
        void onPersonalAccountNotAddedFragmentInteraction();
    }

    public void showTextAddAccWithLink(TextView textView, String[] links, ClickableSpan[] clickableSpans) {
        SpannableString spannableString = new SpannableString(textView.getText());
        for (int i = 0; i < links.length; i++) {
            ClickableSpan clickableSpan = clickableSpans[i];
            String link = links[i];

            int startIndexOfLink = textView.getText().toString().indexOf(link);
            spannableString.setSpan(clickableSpan, startIndexOfLink,
                    startIndexOfLink + link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setHighlightColor(Color.TRANSPARENT);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }
}
