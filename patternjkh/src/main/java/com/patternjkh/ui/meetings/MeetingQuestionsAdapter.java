package com.patternjkh.ui.meetings;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.MeetingQuestion;

import java.util.List;


public class MeetingQuestionsAdapter extends RecyclerView.Adapter<MeetingQuestionsAdapter.MeetingQuestionsViewHolder> {

    private List<MeetingQuestion> items;

    public MeetingQuestionsAdapter(List<MeetingQuestion> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public MeetingQuestionsViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting_profile_question, parent, false);
        return new MeetingQuestionsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MeetingQuestionsViewHolder holder, int position) {
        MeetingQuestion item = items.get(position);
        holder.tvQuestion.setText(item.getQuestion());
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class MeetingQuestionsViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;

        public MeetingQuestionsViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_meeting_item_question);
        }
    }
}