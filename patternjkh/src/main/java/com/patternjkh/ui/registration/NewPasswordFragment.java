package com.patternjkh.ui.registration;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.StringUtils;

import static com.patternjkh.utils.ToastUtils.showToast;

public class NewPasswordFragment extends Fragment {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PHONE = "phone";

    private String mPhone, hex;

    private EditText etPassword, etPasswordRepeat;
    private Button mSaveButton;
    private ProgressDialog mDialog;
    private TextView tvNewPass, tvNewPassRepeat;
    private ImageView ivPasswordEye, ivPasswordEyeRepeat;

    private OnNewPasswordFragmentInteractionListener mListener;
    private SharedPreferences sPref;
    private Server server = new Server(getActivity());
    private Handler handler;
    private AppStyleManager appStyleManager;

    public interface OnNewPasswordFragmentInteractionListener {
        void onNewPasswordFragmentInteraction(String password);
    }

    public NewPasswordFragment() {
    }

    public static NewPasswordFragment newInstance(String phone) {
        NewPasswordFragment fragment = new NewPasswordFragment();
        Bundle args = new Bundle();
        args.putString(PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewPasswordFragmentInteractionListener) {
            mListener = (OnNewPasswordFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewPasswordFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhone = getArguments().getString(PHONE);
        }

        sPref = getActivity().getSharedPreferences(APP_SETTINGS, getActivity().MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        appStyleManager = AppStyleManager.getInstance(getActivity(), hex);

        handler = new Handler() {
            public void handleMessage(Message message) {

                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
                if (message.what == 1) {
                    if (message.obj != null && mListener != null) {
                        String password = String.valueOf(message.obj);
                        mListener.onNewPasswordFragmentInteraction(password);
                    }
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
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_password, container, false);
        initViews(v);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvNewPass.setTextColor(Color.parseColor("#" + hex));
            tvNewPassRepeat.setTextColor(Color.parseColor("#" + hex));
            etPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
            etPasswordRepeat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));

            mSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        }

        initListeners();

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
        ivPasswordEye.setOnClickListener(new View.OnClickListener() {
            private boolean isPasswordVisible;

            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(getActivity().getDrawable(R.drawable.ic_password_eye));
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_password_eye));
                }
                etPassword.setSelection(etPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        ivPasswordEyeRepeat.setOnClickListener(new View.OnClickListener() {
            private boolean isPasswordVisible;

            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    etPasswordRepeat.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPasswordEyeRepeat.setImageDrawable(getActivity().getDrawable(R.drawable.ic_password_eye_repeat));
                } else {
                    etPasswordRepeat.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPasswordEyeRepeat.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_password_eye_repeat));
                }
                etPasswordRepeat.setSelection(etPasswordRepeat.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    setBtnEnabled(false);
                } else {
                    setBtnEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionUtils.hasConnection(getActivity())) {
                    String password = etPassword.getText().toString();
                    String passwordRepeated = etPasswordRepeat.getText().toString();

                    if (password.equals(passwordRepeated)) {
                        confirmPassword();
                    } else {
                        showToastHere("Пароли не совпадают");
                    }
                } else {
                    DialogCreator.showInternetErrorDialog(getActivity(), hex);
                }
            }
        });
    }

    private void confirmPassword() {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Установка нового пароля...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
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

        if (!etPassword.getText().toString().contains(" ")) {
            setAccountPassword(etPassword.getText().toString());
        } else {
            showToastHere("Пароль не должен содержать \"пробел\"");
            if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        }
    }

    private void setAccountPassword(final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.setAccountPassword(mPhone, password);

                if (line.equals("ok")) {
                    Message msg = handler.obtainMessage(1, 0, 0, password);
                    handler.sendMessage(msg);
                } else if (line.contains("error")) {
                    Message msg = handler.obtainMessage(3, 0, 0, line);
                    handler.sendMessage(msg);
                } else {
                    Message msg = handler.obtainMessage(404, 0, 0, line);
                    handler.sendMessage(msg);
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
                mSaveButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#" + hex), PorterDuff.Mode.SRC_IN));
            } else {
                mSaveButton.getBackground().setColorFilter(new PorterDuffColorFilter(getActivity().getResources().getColor(R.color.ligth_grey), PorterDuff.Mode.SRC_IN));
            }
        }
        mSaveButton.setClickable(enabled);
        mSaveButton.setFocusable(enabled);
        mSaveButton.setEnabled(enabled);
    }

    private void initViews(View v) {
        ivPasswordEye = v.findViewById(R.id.iv_password_eye);
        etPassword = v.findViewById(R.id.et_create_password);
        tvNewPass = v.findViewById(R.id.tv_new_pass);

        etPasswordRepeat = v.findViewById(R.id.et_create_password_repeat);
        ivPasswordEyeRepeat = v.findViewById(R.id.iv_password_eye_repeat);
        tvNewPassRepeat = v.findViewById(R.id.tv_new_pass_repeat);

        mSaveButton = v.findViewById(R.id.btn_save);
    }
}
