package com.patternjkh.ui.webcams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Webcam;

import java.util.ArrayList;

public class WebcamAdapter extends BaseAdapter {

    Context context;
    ArrayList<Webcam> webcams;
    String hex;

    public WebcamAdapter(Context context, ArrayList<Webcam> webcams, String hex) {
        this.context = context;
        this.webcams = webcams;
        this.hex = hex;
    }

    @Override
    public int getCount() {
        return webcams.size();
    }

    @Override
    public Object getItem(int position) {
        return webcams.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Webcam webcam = getWebcam(position);
        View view = null;
        LayoutInflater inflater = LayoutInflater.from(context);

        view = inflater.inflate(R.layout.item_webcams, null, true);

        TextView tvAddress = view.findViewById(R.id.tv_webcam_address);
        tvAddress.setText(webcam.getAddress());

        ImageView ivWebcam = view.findViewById(R.id.iv_webcam);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ivWebcam.setBackgroundColor(Color.parseColor("#" + hex));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Webcam cam = getWebcam(position);

                Activity activity = (Activity) context;
                if (!cam.getUrl().contains("rtsp://")) {
                    Intent intent = new Intent(context, WebviewCamActivity.class);
                    intent.putExtra("url", cam.getUrl());
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(cam.getUrl()));
                    activity.startActivity(i);
                }
            }
        });

        return view;
    }

    Webcam getWebcam(int i) {
        return ((Webcam) getItem(i));
    }
}
