package com.patternjkh.ui.meetings;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.MeetingResult;
import com.patternjkh.utils.StringUtils;

import java.util.List;

public class MeetingResultsAdapter extends RecyclerView.Adapter<MeetingResultsAdapter.MeetingResultsViewHolder> {

    private List<MeetingResult> items;
    private Context context;

    public MeetingResultsAdapter(Context context, List<MeetingResult> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MeetingResultsViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting_result, parent, false);
        return new MeetingResultsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MeetingResultsViewHolder holder, int position) {
        MeetingResult result = items.get(position);

        holder.tvTitle.setText(result.getQuestion());
        if (!result.getAllDecision().toLowerCase().equals("за")) {
            holder.tvDecision.setTextColor(Color.RED);
        }
        holder.tvDecision.setText(result.getAllDecision());
        holder.tvVoicesFor.setText(result.getVoicesFor() + " (" + result.getVoicesForPercent() + "%)");
        holder.tvVoicesAgainst.setText(result.getVoicesAgainst());
        holder.tvAbstained.setText(result.getVoicesAbstained());
        int voicesNum = StringUtils.convertStringToInteger(result.getNumberOfParticipants());
        holder.tvParticipantsNum.setText(context.getResources().getQuantityString(R.plurals.plurals_voices, voicesNum, voicesNum));

        String userVoiceText = "";
        if (result.getUserVoice().equals("0")) {
            userVoiceText = "Вы проголосовали: За";
        } else if (result.getUserVoice().equals("1")) {
            userVoiceText = "Вы проголосовали: Против";
        } else if (result.getUserVoice().equals("2")) {
            userVoiceText = "Вы проголосовали: Воздержался";
        } else {
            userVoiceText = "Вы не проголосовали";
        }
        holder.tvUserVoice.setText(userVoiceText);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class MeetingResultsViewHolder extends RecyclerView.ViewHolder {
        TextView tvDecision, tvUserVoice, tvTitle, tvParticipantsNum, tvVoicesFor, tvVoicesAgainst, tvAbstained;

        public MeetingResultsViewHolder(View itemView) {
            super(itemView);
            tvDecision = itemView.findViewById(R.id.tv_meeting_result_decision);
            tvUserVoice = itemView.findViewById(R.id.tv_meeting_result_user_voice);
            tvTitle = itemView.findViewById(R.id.tv_meeting_result_question_title);
            tvParticipantsNum = itemView.findViewById(R.id.tv_meeting_result_number_participants);
            tvVoicesFor = itemView.findViewById(R.id.tv_meeting_result_voice_for);
            tvVoicesAgainst = itemView.findViewById(R.id.tv_meeting_result_voice_against);
            tvAbstained = itemView.findViewById(R.id.tv_meeting_result_voice_abstained);
        }
    }
}