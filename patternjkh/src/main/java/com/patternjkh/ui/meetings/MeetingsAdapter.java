package com.patternjkh.ui.meetings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Meeting;
import com.patternjkh.utils.DateUtils;

import java.util.ArrayList;

public class MeetingsAdapter extends RecyclerView.Adapter<MeetingsAdapter.MeetingsViewHolder> {

    private ArrayList<Meeting> meetings;
    private Context context;

    public MeetingsAdapter(Context context, ArrayList<Meeting> meetings) {
        this.meetings = meetings;
        this.context = context;
    }

    @NonNull
    @Override
    public MeetingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false);
        return new MeetingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MeetingsViewHolder holder, int position) {
        final Meeting meeting = meetings.get(position);
        holder.tvTitle.setText(meeting.getTitle());

        if (meeting.getIsCompleted()) {
            holder.ivInfo.setVisibility(View.VISIBLE);
            holder.tvIsCompleted.setText("Ваш голос учтен");
            holder.tvIsCompleted.setTextColor(Color.parseColor("#a8a8a8"));
        } else {
            holder.ivInfo.setVisibility(View.GONE);
            if (meeting.getIsAnsweredAnyQuestion()) {
                holder.tvIsCompleted.setText("Ваш голос учтен");
                holder.tvIsCompleted.setTextColor(Color.parseColor("#32cd32"));
            } else {
                holder.tvIsCompleted.setText("Вы не голосовали");
                holder.tvIsCompleted.setTextColor(Color.parseColor("#ff0000"));
            }
        }

        String dateStr = "";
        if (meeting.getDateEnd().length() >= 10) {
            dateStr = DateUtils.parseDateToStringWithoutHours(meeting.getDateEnd());
        } else {
            dateStr = meeting.getDateEnd();
        }
        holder.tvDateEnd.setText("Голосование завершится " + dateStr);

        holder.ivInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                Intent intent = new Intent(context, MeetingResultActivity.class);
                intent.putExtra("meeting_id", meeting.getId());
                intent.putExtra("meeting_title", meeting.getTitle());
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        holder.layoutAllItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                Intent intent = new Intent(context, MeetingProfileActivity.class);
                intent.putExtra("meeting_id", meeting.getId());
                intent.putExtra("questions_number", meeting.getQuestionsNumber());
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (meetings == null) {
            return 0;
        }
        return meetings.size();
    }

    public class MeetingsViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvDateEnd, tvIsCompleted;
        private ImageView ivInfo;
        private LinearLayout layoutAllItem;

        public MeetingsViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_meeting_item_title);
            tvDateEnd = itemView.findViewById(R.id.tv_meeting_item_duration);
            tvIsCompleted = itemView.findViewById(R.id.tv_meeting_item_completed);
            ivInfo = itemView.findViewById(R.id.iv_meeting_item_info);
            layoutAllItem = itemView.findViewById(R.id.layout_meeting_item);
        }
    }
}