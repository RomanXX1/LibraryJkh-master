package com.patternjkh.ui.statement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Saldo;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SaldoAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Saldo> saldos;

    public SaldoAdapter(Context context, ArrayList<Saldo> saldos) {
        this.context = context;
        this.saldos = saldos;
    }

    @Override
    public int getCount() {
        return saldos.size();
    }

    @Override
    public Object getItem(int position) {
        return saldos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Saldo saldo = getSaldo(position);

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.saldos_list, null, true);
            holder = new ViewHolder();
            initViews(holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvUsluga.setText(saldo.usluga);
        holder.tvStart.setText(new BigDecimal(saldo.start).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
        holder.tvPlus.setText(new BigDecimal(saldo.plus).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
        holder.tvEnd.setText(new BigDecimal(saldo.end).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());

        if (saldo.usluga.toLowerCase().contains("вод")) {
            holder.ivTitleIcon.setVisibility(View.VISIBLE);
            holder.ivTitleIcon.setImageResource(R.drawable.water);
            if (saldo.usluga.toLowerCase().contains("горяч")) {
                holder.ivTitleIcon.setBackgroundResource(R.drawable.counter_water_red_back);
            } else {
                holder.ivTitleIcon.setBackgroundResource(R.drawable.counter_water_blue_back);
            }

        } else if (saldo.usluga.toLowerCase().contains("отопл") || saldo.usluga.toLowerCase().contains("газ")) {
            holder.ivTitleIcon.setVisibility(View.VISIBLE);
            holder.ivTitleIcon.setImageResource(R.drawable.fire);
            holder.ivTitleIcon.setBackgroundResource(R.drawable.counter_fire_back);
        } else if (saldo.usluga.toLowerCase().contains("лектричест") || saldo.usluga.toLowerCase().contains("лектрическ")) {
            holder.ivTitleIcon.setVisibility(View.VISIBLE);
            holder.ivTitleIcon.setImageResource(R.drawable.lamp);
            holder.ivTitleIcon.setBackgroundResource(R.drawable.counter_lamp_back);
        } else if (saldo.usluga.equals("По всем услугам") || saldo.usluga.toLowerCase().contains("содержание") || saldo.usluga.equals("Пени")) {
            holder.ivTitleIcon.setVisibility(View.INVISIBLE);
        } else {
            holder.ivTitleIcon.setVisibility(View.INVISIBLE);
//            holder.ivTitleIcon.setImageResource(R.drawable.lamp);
//            holder.ivTitleIcon.setBackgroundResource(R.drawable.counter_lamp_back);
        }
        holder.layoutTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.layoutPays.isShown()) {
                    holder.layoutPays.setVisibility(View.GONE);
                    holder.ivArrow.setImageResource(R.drawable.ic_down);
                } else {
                    holder.layoutPays.setVisibility(View.VISIBLE);
                    holder.ivArrow.setImageResource(R.drawable.ic_up);
                }
            }
        });
        return convertView;
    }

    private void initViews(ViewHolder holder, View convertView) {
        holder.tvUsluga = convertView.findViewById(R.id.usluga);
        holder.tvStart = convertView.findViewById(R.id.start);
        holder.tvPlus = convertView.findViewById(R.id.plus);
        holder.tvEnd = convertView.findViewById(R.id.end);
        holder.layoutPays = convertView.findViewById(R.id.layout_pays);
        holder.layoutTitle = convertView.findViewById(R.id.layout_title);
        holder.ivArrow = convertView.findViewById(R.id.iv_arrow);
        holder.ivTitleIcon = convertView.findViewById(R.id.iv_title_icon);
    }

    private Saldo getSaldo(int position) {
        return ((Saldo) getItem(position));
    }

    public static class ViewHolder {
        private TextView tvUsluga, tvStart, tvPlus, tvEnd;
        private LinearLayout layoutPays, layoutTitle;
        private ImageView ivArrow, ivTitleIcon;
    }
}
