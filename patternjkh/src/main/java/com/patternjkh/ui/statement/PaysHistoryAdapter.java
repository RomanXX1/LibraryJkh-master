package com.patternjkh.ui.statement;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.PaysHistoryItem;

import java.util.List;

public class PaysHistoryAdapter extends RecyclerView.Adapter<PaysHistoryAdapter.PaysHistoryViewHolder> {

    private List<PaysHistoryItem> items;

    public PaysHistoryAdapter(List<PaysHistoryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PaysHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_pays, parent, false);
        return new PaysHistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PaysHistoryViewHolder holder, int position) {
        PaysHistoryItem item = items.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvPay.setText(item.getPay());
        holder.tvStatus.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class PaysHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvStatus, tvPay;

        public PaysHistoryViewHolder(View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_pays_history_date);
            tvStatus = itemView.findViewById(R.id.tv_pays_history_status);
            tvPay = itemView.findViewById(R.id.tv_pays_history_price);
        }
    }
}