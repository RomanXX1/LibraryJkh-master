package com.patternjkh.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.ui.others.TechSendActivity;

public class DialogCreator {

    public static void showErrorCustomDialog(final Activity activity, final String errorText, String hex) {
        final String error = "Возможно на устройстве отсутствует интернет или сервер временно не доступен" +
                "\n\nОтвет сервера: " + errorText;

        LayoutInflater layoutinflater = LayoutInflater.from(activity);
        View view = layoutinflater.inflate(R.layout.dialog_custom_error, null);
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(activity);
        errorDialog.setView(view);
        errorDialog.setTitle("Сервер временно не отвечает");
        Button btnOk = view.findViewById(R.id.btn_error_dialog_ok);
        Button btnTech = view.findViewById(R.id.btn_error_dialog_tech);
        TextView tvError = view.findViewById(R.id.tv_error_dialog_text);
        tvError.setText(error);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnOk.setTextColor(Color.parseColor("#" + hex));
            btnTech.setTextColor(Color.parseColor("#" + hex));
        }
        final AlertDialog dialog = errorDialog.create();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            dialog.show();
        }
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    dialog.dismiss();
                }
            }
        });
        btnTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    Intent intent = new Intent(activity, TechSendActivity.class);
                    intent.putExtra("error_str", errorText);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                    dialog.dismiss();
                }
            }
        });
    }

    public static void showInternetErrorDialog(final Activity activity, String hex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Ошибка");
        builder.setMessage("Проверьте стабильность Интернет-соединения");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            dialog.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#" + hex));
            }
        }
    }

    public static void showAddCounterErrorDialog(final Activity activity, final String errorText, String hex) {
        LayoutInflater layoutinflater = LayoutInflater.from(activity);
        View view = layoutinflater.inflate(R.layout.dialog_custom_add_counter_error, null);
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(activity);
        errorDialog.setView(view);
        Button btnOk = view.findViewById(R.id.btn_count_error_dialog_ok);
        Button btnTech = view.findViewById(R.id.btn_count_error_dialog_tech);
        TextView tvError = view.findViewById(R.id.tv_count_error_dialog_text);
        tvError.setText(errorText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnOk.setTextColor(Color.parseColor("#" + hex));
            btnTech.setTextColor(Color.parseColor("#" + hex));
        }
        final AlertDialog dialog = errorDialog.create();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            dialog.show();
        }
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
//                    dialog.dismiss();
//                }
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    activity.finish();
                    activity.overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
                }
            }
        });
        btnTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    Intent intent = new Intent(activity, TechSendActivity.class);
                    intent.putExtra("error_str", errorText);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                    dialog.dismiss();
                }
            }
        });
    }
}
