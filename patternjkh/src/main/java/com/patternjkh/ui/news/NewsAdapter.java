package com.patternjkh.ui.news;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.New;

import java.util.ArrayList;


public class NewsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<New> news;
    private DB db;
    private String login, hex;
    private AppStyleManager appStyleManager;

    public NewsAdapter(Context context, ArrayList<New> news, DB db, String login, String hex, AppStyleManager appStyleManager) {
        this.context = context;
        this.news = news;
        this.db = db;
        this.login = login;
        this.hex = hex;
        this.appStyleManager = appStyleManager;
    }

    @Override
    public int getCount() {
        return news.size();
    }

    public Object getItem(int position) {
        return news.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_list, null, true);

        TextView tvName = view.findViewById(R.id.txtName);
        TextView tvDate = view.findViewById(R.id.txtDate);
        FrameLayout icon = view.findViewById(R.id.imgReaded);

        final New itemNew = getNews(position);

        if (!itemNew.isReaded) {
            tvName.setTextColor(Color.parseColor("#" + hex));
            icon.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        } else {
            tvName.setTextColor(context.getResources().getColor(R.color.green));
            icon.setBackground(context.getDrawable(R.drawable.ic_circle_not_read));
        }
        tvName.setText(itemNew.name);
        tvDate.setText(itemNew.getDate());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                New itemNew = getNews(position);

                // Установим новость прочитанной
                Server server = new Server(v.getContext());
                String line = server.set_new_readed(String.valueOf(itemNew.id), login);
                db.update_end_news(Integer.valueOf(itemNew.id), "true");

                Intent intent = new Intent(context, NewActivity.class);
                Activity activity = (Activity) context;
                intent.putExtra("Name_News", itemNew.name);
                intent.putExtra("Text_News", itemNew.text);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        return view;
    }

    private New getNews(int position) {
        return ((New) getItem(position));
    }
}
