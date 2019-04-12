package com.patternjkh.ui.statement;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Saldo;

import java.util.List;


public class CostServicesAdapter extends RecyclerView.Adapter<CostServicesAdapter.CostServicesViewHolder> {

    private List<Saldo> items;
    private OnCostServiceClickListener onCostServiceClickListener;
    private String hex;

    public CostServicesAdapter(OnCostServiceClickListener onCostServiceClickListener, List<Saldo> items, String hex) {
        this.items = items;
        this.onCostServiceClickListener = onCostServiceClickListener;
        this.hex = hex;
    }

    @NonNull
    @Override
    public CostServicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cost_service, parent, false);
        return new CostServicesViewHolder(itemView, onCostServiceClickListener);
    }

    @Override
    public void onBindViewHolder(final CostServicesViewHolder holder, int position) {
        Saldo item = items.get(position);
        holder.tvSum.setText(item.end);
        holder.tvName.setText(item.usluga);
        if (item.usluga.equals("Сервисный сбор")) {
            holder.checkBox.setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColor(holder);
        }
    }

    @SuppressLint("NewApi")
    private void setColor(CostServicesViewHolder holder) {
        holder.checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class CostServicesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName, tvSum;
        private CheckBox checkBox;
        private OnCostServiceClickListener onCostServiceClickListener;

        public CostServicesViewHolder(View itemView, OnCostServiceClickListener onCostServiceClickListener) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_item_cost_service_name);
            tvSum = itemView.findViewById(R.id.tv_item_cost_service_sum);
            checkBox = itemView.findViewById(R.id.checkbox_item_cost_check);
            this.onCostServiceClickListener = onCostServiceClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (checkBox.isChecked()) {
                checkBox.setChecked(false);
                onCostServiceClickListener.onServiceClicked(getAdapterPosition(), false);
            } else {
                checkBox.setChecked(true);
                onCostServiceClickListener.onServiceClicked(getAdapterPosition(), true);
            }
        }
    }
}