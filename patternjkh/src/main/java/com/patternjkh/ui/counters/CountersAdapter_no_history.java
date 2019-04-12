package com.patternjkh.ui.counters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.patternjkh.R;
import com.patternjkh.data.Counter;

import java.util.ArrayList;

public class CountersAdapter_no_history extends BaseAdapter {

    private Context ctx;
    private ArrayList<Counter> counters;

    public CountersAdapter_no_history(Context ctx, ArrayList<Counter> counters){
        this.ctx = ctx;
        this.counters = counters;
    }

    @Override
    public int getCount() {
        return counters.size();
    }

    @Override
    public Object getItem(int position) {
        return counters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Counter counter = getCounter(position);

        View view = null;

        LayoutInflater inflater = LayoutInflater.from(ctx);
        view = inflater.inflate(R.layout.counters_list_no_history, null, true);

        ((TextView) view.findViewById(R.id.pole1)).setText(counter.name + ", " + counter.ed_izm);
        ((TextView) view.findViewById(R.id.pole3)).setText(counter.value);

        return view;
    }

    private Counter getCounter(int position) {
        return ((Counter) getItem(position));
    }
}
