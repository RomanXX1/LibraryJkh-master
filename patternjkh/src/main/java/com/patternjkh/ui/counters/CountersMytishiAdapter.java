package com.patternjkh.ui.counters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.CounterMytishi;
import com.patternjkh.utils.DigitUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CountersMytishiAdapter extends RecyclerView.Adapter<CountersMytishiAdapter.CountersAdapter> {

    private ArrayList<CounterMytishi> counters;
    private OnCounterMytishiClickListener listener;
    private String hex;
    private Context context;

    public CountersMytishiAdapter(Context context, ArrayList<CounterMytishi> counters, OnCounterMytishiClickListener listener, String hex) {
        this.counters = counters;
        this.listener = listener;
        this.hex = hex;
        this.context = context;
    }

    @NonNull
    @Override
    public CountersAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.counters_list_mytishi, parent, false);
        return new CountersAdapter(itemView);
    }

    @Override
    public void onBindViewHolder(final CountersAdapter holder, int position) {
        final CounterMytishi counter = counters.get(position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.btnAdd.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        } else {
            holder.btnAdd.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#23b6ed")}));
        }

        String name_counter = "";

        if (!counter.getNameExtended().equals("")) {
            name_counter = counter.getNameExtended();
        } else {
            name_counter = counter.name;
        }
        if (!counter.getUnits().equals("")) {
            name_counter = name_counter + ", " + counter.getUnits();
        }
        holder.tvNameExtended.setText(name_counter);

        holder.tvIdent.setText("Л/с: " + counter.ident);
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addCount(holder.getAdapterPosition());
            }
        });

        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.selectCounter(holder.getAdapterPosition());
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

        holder.tvNameAndNumber.setText(counter.getFactoryNum());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String[] valuesStr = counter.getValues().split(";");
        ArrayList<String[]> values = new ArrayList<>();
        if (valuesStr.length > 0) {
            for (String item : valuesStr) {
                values.add(new String[]{item.split("---")[0], item.split("---")[1], item.split("---")[2]});
            }

            try {
                Collections.sort(values, Collections.reverseOrder(new Comparator<String[]>() {
                    DateFormat f = new SimpleDateFormat("dd.MM.yyyy");
                    @Override
                    public int compare(final String[] o1, final String[] o2) {
                        try {
                            return f.parse(o1[0]).compareTo(f.parse(o2[0]));
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }));
            } catch (IllegalArgumentException e) {
                Logger.errorLog(CountersMytishiAdapter.this.getClass(), e.getMessage());
            }

            int size = 0;
            if (values.size() < 4) {
                size = values.size();
            } else {
                size = 3;
            }

            holder.layoutMain.removeAllViews();
            for (int i = 0; i < size; i++) {
                String[] value = values.get(i);
                if (i == 0) {
                    if (value[2].equals("1")) {
                        holder.tvNotSent.setVisibility(View.VISIBLE);
                        holder.btnAdd.setText("Передать еще раз");
                    } else {
                        holder.tvNotSent.setVisibility(View.GONE);
                        holder.btnAdd.setText("Передать показания");
                    }
                }
                View view_is_uploaded = inflater.inflate(R.layout.item_counter_mytishi_value, null);
                TextView tvDate = view_is_uploaded.findViewById(R.id.tv_counter_mytishi_date);
                TextView tvValue = view_is_uploaded.findViewById(R.id.tv_counter_mytishi_value);
                ImageView ivNotSent = view_is_uploaded.findViewById(R.id.iv_counter_mytishi_not_sent);
                tvDate.setText(value[0]);
                tvValue.setText(DigitUtils.roundDigit(StringUtils.fixIncorrectDoubleValuesFromString(value[1])));
                if (value[2].equals("1")) {
                    ivNotSent.setVisibility(View.VISIBLE);
                } else {
                    ivNotSent.setVisibility(View.INVISIBLE);
                }
                holder.layoutMain.addView(view_is_uploaded);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (counters == null) {
            return 0;
        }
        return counters.size();
    }

    public class CountersAdapter extends RecyclerView.ViewHolder {

        private TextView tvNameExtended, tvNameAndNumber, tvIdent, btnAdd, tvNotSent;
        private ImageView ivIcon;
        private LinearLayout layoutMain;

        public CountersAdapter(View v) {
            super(v);
            tvNameExtended = v.findViewById(R.id.pole1);
            tvNameAndNumber = v.findViewById(R.id.txt_counter_name_and_number);
            ivIcon = v.findViewById(R.id.iv_title_icon);
            tvIdent = v.findViewById(R.id.tv_counter_ls);
            btnAdd = v.findViewById(R.id.btn_counter_add);
            layoutMain = v.findViewById(R.id.layout_counters);
            tvNotSent = v.findViewById(R.id.tv_counter_not_sent);
        }
    }
}