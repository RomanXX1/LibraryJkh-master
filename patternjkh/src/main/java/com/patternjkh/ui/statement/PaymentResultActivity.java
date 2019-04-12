package com.patternjkh.ui.statement;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.patternjkh.R;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String APP_SETTINGS = "global_settings";

    private boolean paymentSuccess = false;
    private int rateParameter = 0;
    private String hex = "";

    private TextView tvResult;
    private AppCompatButton btnReturn;

    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        tvResult = findViewById(R.id.tv_payment_result);
        btnReturn = findViewById(R.id.btn_payment_return);

        getParametersFromPref();

        paymentSuccess = getIntent().getBooleanExtra("is_payment_success", false);
        if (paymentSuccess) {
            tvResult.setText("Ваш платеж успешно принят!\nОплата обрабатывается 2-3 рабочих дня");

            if (rateParameter == 0) {
                showRateDialog();
            }
        } else {
            String error = "Ваш платеж отклонен. Проверьте вводимые данные и сумму оплаты.";
            String cause = "";
            cause = getIntent().getStringExtra("error_cause");
            cause = cause.replaceAll("ru.tinkoff.acquiring.sdk.AcquiringSdkException: ", "");
            if (!cause.equals("") && !cause.equals(" ")) {
                error += '\n' + "Причина: " + "\"" + cause + "\"";
            }
            tvResult.setText(error);
        }

        setTitle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColor();
        }

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void showRateDialog() {
        LayoutInflater layoutinflater = LayoutInflater.from(PaymentResultActivity.this);
        View view = layoutinflater.inflate(R.layout.dialog_custom_rate, null);
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(PaymentResultActivity.this);
        errorDialog.setView(view);
        errorDialog.setTitle("Оцените приложение");
        Button btnOk = view.findViewById(R.id.btn_dialog_rate_ok);
        Button btnLater = view.findViewById(R.id.btn_dialog_rate_later);
        Button btnNever = view.findViewById(R.id.btn_dialog_rate_never);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnOk.setTextColor(Color.parseColor("#" + hex));
            btnLater.setTextColor(Color.parseColor("#" + hex));
            btnNever.setTextColor(Color.parseColor("#" + hex));
        }
        final AlertDialog dialog = errorDialog.create();
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }
        btnNever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putInt("rate_param", 404);
                    ed.commit();
                    if (dialog != null)
                        dialog.dismiss();
                }
            }
        });

        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing() && !isDestroyed()) {
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putInt("rate_param", 0);
                    ed.commit();
                    if (dialog != null)
                        dialog.dismiss();
                }
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt("rate_param", 1);
                ed.commit();
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    private void setTitle() {
        String title = "";
        if (paymentSuccess) {
            title = "Платеж принят";
        } else {
            title = "Платеж отклонен";
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
            }
        });
    }

    private void getParametersFromPref() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        rateParameter = sPref.getInt("rate_param", 0);
    }

    @SuppressLint("NewApi")
    private void setColor() {
        btnReturn.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
    }
}
