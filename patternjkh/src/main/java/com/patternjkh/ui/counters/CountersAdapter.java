package com.patternjkh.ui.counters;

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
import com.patternjkh.data.Counter;
import com.patternjkh.utils.DigitUtils;
import com.patternjkh.utils.StringUtils;

import java.util.ArrayList;

public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.CountersViewHolder> {

    private ArrayList<Counter> counters;
    private OnAddCounterClickListener listener;
    private String hex;

    public CountersAdapter(ArrayList<Counter> counters, OnAddCounterClickListener listener, String hex) {
        this.counters = counters;
        this.listener = listener;
        this.hex = hex;
    }

    @NonNull
    @Override
    public CountersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.counters_list, parent, false);
        return new CountersViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CountersViewHolder holder, int position) {
        Counter counter = counters.get(position);

        holder.tvAdd.setTextColor(Color.parseColor("#" + hex));
        holder.tvChange.setTextColor(Color.parseColor("#" + hex));

        String name_counter = "";

        if (!counter.getCounterNameExtended().equals("")) {
            name_counter = counter.getCounterNameExtended();
        } else {
            name_counter = counter.name;
        }
        if (!counter.ed_izm.equals("")) {
            name_counter = name_counter + ", " + counter.ed_izm;
        }
        holder.tvNameExtended.setText(name_counter);

        holder.tvIdent.setText("Л/с: " + counter.ident);

        if (counter.isSent.equals("0") || counter.isSent.equals("")) {
            holder.tvIsSent.setText("Показания не переданы");
            holder.tvChange.setVisibility(View.GONE);
            holder.tvAdd.setVisibility(View.VISIBLE);
            holder.layoutDiff.setVisibility(View.GONE);
            holder.layoutCurrent.setVisibility(View.GONE);
        } else {
            holder.tvIsSent.setText("Показания переданы");
            holder.tvAdd.setVisibility(View.GONE);
            holder.tvChange.setVisibility(View.VISIBLE);
        }

        holder.tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addCounter(holder.getAdapterPosition());
            }
        });
        holder.tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addCounter(holder.getAdapterPosition());
            }
        });

        if (counter.name.toLowerCase().contains("эл")) {
            holder.ivIcon.setBackgroundResource(R.drawable.counter_lamp_back);
            holder.ivIcon.setImageResource(R.drawable.lamp);
        } else if (counter.name.toLowerCase().contains("хвс") || counter.name.toLowerCase().contains("холодн") || counter.name.toLowerCase().contains("хвc")) {
            holder.ivIcon.setBackgroundResource(R.drawable.counter_water_blue_back);
            holder.ivIcon.setImageResource(R.drawable.water);
        } else if (counter.name.toLowerCase().contains("гвс") || counter.name.toLowerCase().contains("горяч") || counter.name.toLowerCase().contains("гвc")) {
            holder.ivIcon.setBackgroundResource(R.drawable.counter_water_red_back);
            holder.ivIcon.setImageResource(R.drawable.water);
        } else {
            holder.ivIcon.setVisibility(View.INVISIBLE);
        }

        holder.layoutTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.layoutCounters.isShown()) {
                    holder.layoutCounters.setVisibility(View.GONE);
                    holder.ivArrow.setImageResource(R.drawable.ic_down);
                } else {
                    holder.layoutCounters.setVisibility(View.VISIBLE);
                    holder.ivArrow.setImageResource(R.drawable.ic_up);
                }
            }
        });

        holder.tvNameAndNumber.setText(counter.serialNumber);
        holder.tvPrev.setText(DigitUtils.roundDigit(StringUtils.fixIncorrectDoubleValuesFromString(counter.prev)));
        holder.tvValue.setText(DigitUtils.roundDigit(StringUtils.fixIncorrectDoubleValuesFromString(counter.value)));
        holder.tvDiff.setText(DigitUtils.roundDigit(StringUtils.fixIncorrectDoubleValuesFromString(counter.diff)));
    }

    @Override
    public int getItemCount() {
        if (counters == null) {
            return 0;
        }
        return counters.size();
    }

    public class CountersViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNameExtended, tvNameAndNumber, tvPrev, tvValue, tvDiff, tvIdent, tvIsSent, tvAdd, tvChange;
        private ImageView ivIcon, ivArrow;
        private LinearLayout layoutCounters, layoutTitle, layoutDiff, layoutCurrent;

        public CountersViewHolder(View v) {
            super(v);
            tvNameExtended = v.findViewById(R.id.pole1);
            tvNameAndNumber = v.findViewById(R.id.txt_counter_name_and_number);
            tvPrev = v.findViewById(R.id.pole2);
            tvValue = v.findViewById(R.id.pole3);
            tvDiff = v.findViewById(R.id.pole4);
            ivIcon = v.findViewById(R.id.iv_title_icon);
            ivArrow = v.findViewById(R.id.iv_arrow);
            layoutCounters = v.findViewById(R.id.layout_counters);
            layoutTitle = v.findViewById(R.id.layout_title);
            tvIdent = v.findViewById(R.id.tv_counter_ls);
            tvIsSent = v.findViewById(R.id.tv_counter_is_sent);
            tvAdd = v.findViewById(R.id.tv_counter_add);
            tvChange = v.findViewById(R.id.tv_counter_change);
            layoutDiff = v.findViewById(R.id.layout_counter_diff);
            layoutCurrent = v.findViewById(R.id.layout_counter_current);
        }
    }
}