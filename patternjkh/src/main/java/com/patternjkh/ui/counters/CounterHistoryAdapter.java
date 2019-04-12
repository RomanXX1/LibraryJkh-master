package com.patternjkh.ui.counters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.CounterHistoryItem;

import java.util.List;

public class CounterHistoryAdapter extends RecyclerView.Adapter<CounterHistoryAdapter.CounterHistoryViewHolder> {

    private List<CounterHistoryItem> items;

    public CounterHistoryAdapter(List<CounterHistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CounterHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_counter, parent, false);
        return new CounterHistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CounterHistoryViewHolder holder, int position) {
        CounterHistoryItem item = items.get(position);

        holder.tvPeriod.setText(item.getPeriod());
        holder.tvValue.setText(item.getValue());
        if (item.getSendError().equals("1")) {
            holder.ivNotSent.setVisibility(View.VISIBLE);
        } else {
            holder.ivNotSent.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class CounterHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPeriod, tvValue;
        private ImageView ivNotSent;

        public CounterHistoryViewHolder(View itemView) {
            super(itemView);

            tvPeriod = itemView.findViewById(R.id.tv_counter_history_date);
            tvValue = itemView.findViewById(R.id.tv_counter_history_value);
            ivNotSent = itemView.findViewById(R.id.iv_counter_history_error);
        }
    }
}