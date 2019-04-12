package com.patternjkh.ui.others;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patternjkh.R;

import java.util.List;

public class PersonalAccountsAdapter extends RecyclerView.Adapter<PersonalAccountsAdapter.PersonalAccountsViewHolder> {

    private List<String> items;

    private OnLsDeleteListener onLsDeleteListener;

    public PersonalAccountsAdapter(List<String> items, OnLsDeleteListener onLsDeleteListener) {
        this.items = items;
        this.onLsDeleteListener = onLsDeleteListener;
    }

    @NonNull
    @Override
    public PersonalAccountsViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ls_profile, parent, false);
        return new PersonalAccountsViewHolder(itemView, onLsDeleteListener);
    }

    @Override
    public void onBindViewHolder(PersonalAccountsViewHolder holder, int position) {
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

    public class PersonalAccountsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvLs;
        private OnLsDeleteListener onLsDeleteListener;

        public PersonalAccountsViewHolder(View itemView, OnLsDeleteListener onLsDeleteListener) {
            super(itemView);
            this.onLsDeleteListener = onLsDeleteListener;
            itemView.setOnClickListener(this);
            tvLs = itemView.findViewById(R.id.tv_ls);
        }

        @Override
        public void onClick(View v) {
            onLsDeleteListener.onLsDeleteClicked(items.get(getAdapterPosition()));
        }
    }
}