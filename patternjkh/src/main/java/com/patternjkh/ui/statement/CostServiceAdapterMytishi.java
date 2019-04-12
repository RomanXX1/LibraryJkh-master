package com.patternjkh.ui.statement;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Saldo;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CostServiceAdapterMytishi extends RecyclerView.Adapter<CostServiceAdapterMytishi.CostServicesViewHolder> {

    private List<Saldo> items;
    private OnCostServiceClickListener onCostServiceClickListener;
    private OnServiceSumChanged onServiceSumChanged;
    private String hex;
    private HashMap<String, String> stringSums = new HashMap<>();

    public CostServiceAdapterMytishi(OnCostServiceClickListener onCostServiceClickListener, OnServiceSumChanged onServiceSumChanged, List<Saldo> items, String hex) {
        this.items = items;
        this.onCostServiceClickListener = onCostServiceClickListener;
        this.onServiceSumChanged = onServiceSumChanged;
        this.hex = hex;

        for (Saldo saldo: items) {
            String saldoId = saldo.id;
            if (saldoId.equals("") || saldoId.equals("-")) {
                saldoId = "1234";
            }
            stringSums.put(saldo.usluga + "-" + saldoId, saldo.end);
        }
    }

    @NonNull
    @Override
    public CostServicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cost_service_mytishi, parent, false);
        return new CostServicesViewHolder(itemView, onCostServiceClickListener, onServiceSumChanged);
    }

    @Override
    public void onBindViewHolder(final CostServicesViewHolder holder, final int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.etSum.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex)));
        }
        final Saldo item = items.get(position);
        holder.etSum.setText(item.end);
        holder.tvName.setText(item.usluga);
        if (item.usluga.equals("Сервисный сбор")) {
            holder.checkBox.setVisibility(View.GONE);
        } else if (item.usluga.equals("Страховка") || item.usluga.toLowerCase().equals("тех.обсл. внутрикварт. газ.об.")) {
            holder.etSum.setVisibility(View.GONE);
            holder.tvSum.setVisibility(View.VISIBLE);
            holder.tvSum.setText(item.end);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColor(holder);
        }

        holder.etSum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern patternHasNumbers = Pattern.compile("\\d+");
                Matcher matcher = patternHasNumbers.matcher(holder.etSum.getText().toString());
                if (holder.etSum.getText().toString().equals("") || !matcher.find()) {
                    holder.etSum.setText("0.00");
                }
                stringSums.put(item.usluga + "-" + item.id, holder.etSum.getText().toString());
                holder.onServiceSumChanged.onServiceChanged();
            }
        });
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

    public String getValue(String usluga) {
        return stringSums.get(usluga);
    }

    public HashMap<String, String> getAllValues() {
        return stringSums;
    }

    public class CostServicesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName, tvSum;
        private EditText etSum;
        private CheckBox checkBox;
        private OnCostServiceClickListener onCostServiceClickListener;
        private OnServiceSumChanged onServiceSumChanged;

        public CostServicesViewHolder(View itemView, OnCostServiceClickListener onCostServiceClickListener, OnServiceSumChanged onServiceSumChanged) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_item_cost_service_name);
            tvSum = itemView.findViewById(R.id.tv_item_cost_service_sum);
            etSum = itemView.findViewById(R.id.et_item_cost_service_mytishi_sum);
            checkBox = itemView.findViewById(R.id.checkbox_item_cost_check);
            this.onCostServiceClickListener = onCostServiceClickListener;
            this.onServiceSumChanged = onServiceSumChanged;
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
