package com.patternjkh.ui.registration;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.StringUtils;

import static com.patternjkh.utils.ToastUtils.showToast;

public class PhoneConfirmationFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PHONE = "phone";

    private String mPhone, hex;

    private EditText mAccessCodeSmsEditText;
    private TextView mSentToPhoneTextView, mSendAccessCodeAgainTextView;
    private Button mNextButton;
    private ProgressDialog mDialog;

    private OnPhoneConfirmationFragmentInteractionListener mListener;
    private SharedPreferences sPref;
    private Server server = new Server(getActivity());
    private Handler mSendCheckCodeHandler, mValidateCheckCodeHandler;

    public interface OnPhoneConfirmationFragmentInteractionListener {
        void onPhoneConfirmationFragmentInteraction();
    }

    public PhoneConfirmationFragment() {
    }

    public static PhoneConfirmationFragment newInstance(String phone) {
        PhoneConfirmationFragment fragment = new PhoneConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPhoneConfirmationFragmentInteractionListener) {
            mListener = (OnPhoneConfirmationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPhoneConfirmationFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhone = getArguments().getString(PHONE);
        }

        mSendCheckCodeHandler = new Handler() {
            public void handleMessage(Message message) {
                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }

                if (message.what == 1) {
                    showToastHere("Проверочный код доступа отправлен");
                } else if (message.what == 2) {
                    showToastHere("Код не отправлен. Проверьте введенный номер");
                    getActivity().finish();
                } else if (message.what == 3) {
                    if (message.obj != null) {
                        String errorMessage = String.valueOf(message.obj);
                        errorMessage = errorMessage.replaceFirst("error: ", "");
                        showToastHere(StringUtils.firstUpperCase(errorMessage));
                    }

                } else if (message.what == 404) {
                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }

                    DialogCreator.showErrorCustomDialog(getActivity(), error, hex);
                }
            }
        };

        mValidateCheckCodeHandler = new Handler() {
            public void handleMessage(Message message) {
                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }

                if (message.what == 1) {
                    if (mListener != null) {
                        mListener.onPhoneConfirmationFragmentInteraction();
                    }
                } else if (message.what == 3) {
                    if (message.obj != null) {
                        setBtnEnabled(false);
                        mAccessCodeSmsEditText.setText("");
                        showToastHere("Введенный код неверен");
                    }

                } else if (message.what == 404) {
                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }

                    DialogCreator.showErrorCustomDialog(getActivity(), error, hex);
                }
            }
        };

        sendCheckCode();
        sPref = getActivity().getSharedPreferences(APP_SETTINGS, getActivity().MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phone_confirmation, container, false);
        initViews(v);

        mSentToPhoneTextView.setText(getString(R.string.sent_to_phone, mPhone));

        initListeners();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSendAccessCodeAgainTextView.setTextColor(Color.parseColor("#" + hex));
            mAccessCodeSmsEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        }
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBtnEnabled(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initListeners() {

        mAccessCodeSmsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(mAccessCodeSmsEditText.getText().toString())) {
                    setBtnEnabled(false);
                } else {
                    setBtnEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSendAccessCodeAgainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Повторная отправка проверочного кода...");

                sendCheckCode();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ConnectionUtils.hasConnection(getActivity())) {
                    showProgressDialog("Проверка кода...");

                    validateCheckCode(mAccessCodeSmsEditText.getText().toString());
                } else {
                    DialogCreator.showInternetErrorDialog(getActivity(), hex);
                }
            }
        });
    }

    private void showProgressDialog(String title) {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(title);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            mDialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
                progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog.isShowing()) {
                        DialogCreator.showInternetErrorDialog(getActivity(), hex);
                    }
                }
            }
        };
        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 40000);
    }

    private void sendCheckCode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.sendCheckCode(mPhone);

                if (line.equals("ok") || line.contains("ok")) {
                    mSendCheckCodeHandler.sendEmptyMessage(1);
                } else if (line.contains("не отправ")) {
                    mSendCheckCodeHandler.sendEmptyMessage(2);
                } else if (line.contains("error")) {
                    mSendCheckCodeHandler.sendEmptyMessage(3);
                    Message msg = mSendCheckCodeHandler.obtainMessage(3, 0, 0, line);
                    mSendCheckCodeHandler.sendMessage(msg);
                } else {
                    Message msg = mSendCheckCodeHandler.obtainMessage(404, 0, 0, line);
                    mSendCheckCodeHandler.sendMessage(msg);
                }
            }
        }).start();
    }


    private void validateCheckCode(final String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.validateCheckCode(mPhone, code);

                if (line.equals("ok")) {
                    mValidateCheckCodeHandler.sendEmptyMessage(1);
                } else if (line.contains("error")) {
                    Message msg = mValidateCheckCodeHandler.obtainMessage(3, 0, 0, line);
                    mValidateCheckCodeHandler.sendMessage(msg);
                } else {
                    Message msg = mValidateCheckCodeHandler.obtainMessage(404, 0, 0, line);
                    mValidateCheckCodeHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void showToastHere(String title) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            showToast(getActivity(), title);
        }
    }

    @SuppressLint("NewApi")
    private void setBtnEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (enabled) {
                mNextButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#" + hex), PorterDuff.Mode.SRC_IN));
            } else {
                mNextButton.getBackground().setColorFilter(new PorterDuffColorFilter(getActivity().getResources().getColor(R.color.ligth_grey), PorterDuff.Mode.SRC_IN));
            }
        }
        mNextButton.setClickable(enabled);
        mNextButton.setFocusable(enabled);
        mNextButton.setEnabled(enabled);
    }

    private void initViews(View v) {
        mAccessCodeSmsEditText = v.findViewById(R.id.et_access_code_sms);
        mSentToPhoneTextView = v.findViewById(R.id.txt_sent_to_phone);
        mSendAccessCodeAgainTextView = v.findViewById(R.id.txt_send_access_code_again);
        mNextButton = v.findViewById(R.id.btn_next);
    }
}
