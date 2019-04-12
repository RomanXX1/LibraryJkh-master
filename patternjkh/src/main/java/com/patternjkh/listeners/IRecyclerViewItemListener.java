package com.patternjkh.listeners;

import android.view.View;

public interface IRecyclerViewItemListener {

    void onItemClick(View view, int adapterPosition);

    void onLongClick(View view, int adapterPosition);
}
