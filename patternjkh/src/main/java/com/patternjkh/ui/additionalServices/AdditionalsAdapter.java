package com.patternjkh.ui.additionalServices;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdditionalsAdapter extends RecyclerView.Adapter<AdditionalsAdapter.AdditionalsGroupViewHolder> {

    private List<AdditionalService> services;
    private List<AdditionalGroup> groups;
    private String hex;
    private Context context;

    public AdditionalsAdapter(Context context, List<AdditionalService> services, List<AdditionalGroup> groups, String hex) {
        this.services = services;
        this.hex = hex;
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public AdditionalsGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AdditionalsGroupViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View itemViewGroup = inflater.inflate(R.layout.item_view_additionals_group, parent, false);
        viewHolder = new AdditionalsGroupViewHolder(itemViewGroup);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdditionalsGroupViewHolder holder, int position) {
        AdditionalGroup group = groups.get(position);

        holder.groupName.setText(group.getGroupName());
        holder.layoutGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.layoutAdditionals.getVisibility() == View.VISIBLE) {
                    holder.layoutAdditionals.setVisibility(View.GONE);
                    holder.ivArrow.setImageDrawable(context.getDrawable(R.drawable.ic_down));
                } else {
                    holder.layoutAdditionals.setVisibility(View.VISIBLE);
                    holder.ivArrow.setImageDrawable(context.getDrawable(R.drawable.ic_up));
                }
            }
        });

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (AdditionalService item : services) {
            if (item.getType().equals(group.getGroupName())) {

                View v = inflater.inflate(R.layout.item_view_additionals_child, null);

                TextView title = v.findViewById(R.id.tv_item_adds_child_title);
                TextView descr = v.findViewById(R.id.tv_item_adds_child_descr);
                TextView address = v.findViewById(R.id.tv_item_adds_child_address);
                ImageView imageView = v.findViewById(R.id.iv_item_adds_child);
                TextView phone = v.findViewById(R.id.tv_item_adds_child_phone);

                title.setText(item.getName());
                descr.setText(item.getDescr());
                if (item.getAddress().equals("")) {
                    address.setVisibility(View.GONE);
                } else {
                    address.setVisibility(View.VISIBLE);
                    address.setText(item.getAddress());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        address.setLinkTextColor(Color.parseColor("#" + hex));
                    }
                }

                if (!item.getPhone().equals("")) {
                    phone.setText(item.getPhone());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        phone.setLinkTextColor(Color.parseColor("#" + hex));
                    }
                } else {
                    phone.setVisibility(View.GONE);
                }

                Picasso.with(imageView.getContext())
                        .load(item.getLogo())
                        .noFade()
                        .into(imageView);
                holder.layoutAdditionals.addView(v);
            }
        }
    }
    @Override
    public int getItemCount() {
        if (groups == null) {
            return 0;
        }
        return groups.size();
    }

    public class AdditionalsGroupViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;
        LinearLayout layoutAdditionals;
        ConstraintLayout layoutGroup;
        ImageView ivArrow;

        public AdditionalsGroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.tv_item_group);
            layoutAdditionals = itemView.findViewById(R.id.layout_additional_child);
            layoutGroup = itemView.findViewById(R.id.layout_additional_group);
            ivArrow = itemView.findViewById(R.id.iv_item_group_arrow);
        }
    }
}