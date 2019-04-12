package com.patternjkh.ui.others;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;

import java.util.List;

public class PersonalAccountsMainAdapter extends RecyclerView.Adapter<PersonalAccountsMainAdapter.PersonalAccountsMainViewHolder> {

    private List<String> items;

    public PersonalAccountsMainAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PersonalAccountsMainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ls_main, parent, false);
        return new PersonalAccountsMainViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PersonalAccountsMainViewHolder holder, int position) {
        String item = items.get(position);
        holder.tvLs.setText(item);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class PersonalAccountsMainViewHolder extends RecyclerView.ViewHolder {
        TextView tvLs;

        public PersonalAccountsMainViewHolder(View itemView) {
            super(itemView);
            tvLs = itemView.findViewById(R.id.tv_ls);
        }
    }
}
