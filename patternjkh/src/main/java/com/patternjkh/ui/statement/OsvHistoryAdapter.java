package com.patternjkh.ui.statement;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.OsvHistoryItem;

import java.util.List;

public class OsvHistoryAdapter extends RecyclerView.Adapter<OsvHistoryAdapter.OsvHistoryViewHolder> {

    private List<OsvHistoryItem> items;

    public OsvHistoryAdapter(List<OsvHistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OsvHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_osv, parent, false);
        return new OsvHistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OsvHistoryViewHolder holder, int position) {
        OsvHistoryItem item = items.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvPeriod.setText(item.getPeriod());
        holder.tvPay.setText(item.getPay());
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class OsvHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvPeriod, tvPay;

        public OsvHistoryViewHolder(View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_osv_history_date);
            tvPeriod = itemView.findViewById(R.id.tv_osv_history_period);
            tvPay = itemView.findViewById(R.id.tv_osv_history_pay);
        }
    }
}