package com.patternjkh.ui.others;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.AppStyleManager;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.registration.RegLsActivity;
import com.patternjkh.utils.ConnectionUtils;
import com.patternjkh.utils.DialogCreator;
import com.patternjkh.utils.StringUtils;

import static android.content.Context.MODE_PRIVATE;
import static com.patternjkh.utils.ToastUtils.showToast;

public class AddPersonalAccountFragmentMytishi extends Fragment {

    private static final String APP_SETTINGS = "global_settings";

    private boolean isPasswordVisible;
    private String mPhone, hex;

    private ImageView ivPasswordEye;
    private TextView tvLs, tvPass;
    private EditText etLs, etPass;
    private Button mAddButton, regButton;
    private ProgressDialog mDialog;

    private SharedPreferences sPref;
    private DB db;
    private Server server = new Server(getActivity());
    private Handler mAddIdentToAccountHandler;
    private OnAddPersonalAccountFragmentInteractionListener mListener;
    private AppStyleManager appStyleManager;

    public static AddPersonalAccountFragmentMytishi newInstance(String phone) {
        AddPersonalAccountFragmentMytishi fragment = new AddPersonalAccountFragmentMytishi();
        Bundle args = new Bundle();
        args.putString("phone", phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddPersonalAccountFragmentInteractionListener) {
            mListener = (OnAddPersonalAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAddPersonalAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPhone = getArguments().getString("phone");
        }
        db = new DB(getContext());
        sPref = getContext().getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");

        appStyleManager = AppStyleManager.getInstance(getActivity(), hex);

        mAddIdentToAccountHandler = new Handler() {
            public void handleMessage(Message message) {

                if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
                if (message.what == 1) {
                    String ls = etLs.getText().toString().replaceAll("-","");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setMessage("Лицевой счет " + ls + " привязан к аккаунту " + mPhone);
                    builder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mListener != null) {
                                mListener.onAddPersonalAccountFragmentInteraction();
                            }
                        }
                    });

                    AlertDialog dialog = builder.create();
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        dialog.show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
                        }
                    }
                } else if (message.what == 2) {

                    String error = "";
                    if (message.obj != null) {
                        error = String.valueOf(message.obj);
                    }
                    final String finalError = error;

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Ошибка!");
                    builder.setMessage("Не удалось добавить лицевой счет, напишите в техподдержку\nОтвет сервера: " + error);
                    builder.setPositiveButton("Написать в техподдержку", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), TechSendActivity.class);
                            intent.putExtra("error_str", finalError);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                        }
                    });
                    builder.setNegativeButton("Пропустить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        dialog.show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#" + hex));
                        }
                    }
                } else if (message.what == 3) {
                    if (message.obj != null) {
                        String errorMessage = String.valueOf(message.obj);
                        errorMessage = errorMessage.replaceFirst("error: ", "");
                        showToastHere(StringUtils.firstUpperCase(errorMessage));
                    }
                }
            }
        };

        db.open();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_personal_account_mytishi, container, false);

        initViews(v);
        initListeners();
        setTechColors(v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setButtonEnabled(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addIdentToAccount(final String ls, final String pass) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "xxx";
                line = server.add_ls_mytishi(ls, pass, mPhone);

                if (line.equals("ok") || line.equals("\"ok\"")) {
                    mAddIdentToAccountHandler.sendEmptyMessage(1);
                } else if (line.contains("error")) {
                    Message msg = mAddIdentToAccountHandler.obtainMessage(3, 0, 0, line);
                    mAddIdentToAccountHandler.sendMessage(msg);
                } else {
                    Message msg = mAddIdentToAccountHandler.obtainMessage(2, 0, 0, line);
                    mAddIdentToAccountHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        mAddButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        regButton.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        tvLs.setTextColor(Color.parseColor("#" + hex));
        tvPass.setTextColor(Color.parseColor("#" + hex));
        etLs.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        etPass.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
    }

    private void initListeners() {

        ivPasswordEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(getActivity().getDrawable(R.drawable.ic_password_eye));
                } else {
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPasswordEye.setImageDrawable(appStyleManager.changeDrawableColor(R.drawable.ic_password_eye));
                }
                etPass.setSelection(etPass.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etLs.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etPass.getWindowToken(), 0);

                if (ConnectionUtils.hasConnection(getActivity())) {
                    mDialog = new ProgressDialog(AddPersonalAccountFragmentMytishi.this.getActivity());
                    mDialog.setMessage("Привязка л/сч к аккаунту...");
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ProgressBar progressbar= mDialog.findViewById(android.R.id.progress);
                        progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#" + hex), android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    String personalAccountNumberWithOutHyphen = etLs.getText().toString().replaceAll("-","");
                    String pass = etPass.getText().toString();
                    addIdentToAccount(personalAccountNumberWithOutHyphen, pass);
                } else {
                    DialogCreator.showInternetErrorDialog(getActivity(), hex);
                }
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegLsActivity.class);
                startActivity(intent);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);            }
        });

        etLs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(etPass.getText().toString()) && !TextUtils.isEmpty(etLs.getText().toString())) {
                    setButtonEnabled(true);
                } else {
                    setButtonEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(etPass.getText().toString()) && !TextUtils.isEmpty(etLs.getText().toString())) {
                    setButtonEnabled(true);
                } else {
                    setButtonEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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

    @SuppressLint("NewApi")
    private void setButtonEnabled(boolean isEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isEnabled) {
                mAddButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#" + hex), PorterDuff.Mode.SRC_IN));
            } else {
                mAddButton.getBackground().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.ligth_grey), PorterDuff.Mode.SRC_IN));
            }
        }
        mAddButton.setClickable(isEnabled);
        mAddButton.setFocusable(isEnabled);
        mAddButton.setEnabled(isEnabled);
    }

    private void showToastHere(String title) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            showToast(getActivity(), title);
        }
    }

    private void initViews(View v) {
        mAddButton = v.findViewById(R.id.btn_add);
        regButton = v.findViewById(R.id.btn_reg);
        tvLs = v.findViewById(R.id.tv_ls_title);
        tvPass = v.findViewById(R.id.tv_pass_title);
        etLs = v.findViewById(R.id.et_personal_account_number);
        etPass = v.findViewById(R.id.et_pass);
        ivPasswordEye = v.findViewById(R.id.iv_password_eye);
    }
}
