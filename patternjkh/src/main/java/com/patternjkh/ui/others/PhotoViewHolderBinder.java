package com.patternjkh.ui.others;

import android.support.v7.widget.RecyclerView;

import com.patternjkh.data.Photo;

public class PhotoViewHolderBinder implements ViewHolderBinder {

    private final Photo mPhoto;

    public PhotoViewHolderBinder(Photo photo) {
        mPhoto = photo;
    }

    @Override
    public void bind(RecyclerView.ViewHolder viewHolder) {
        PhotoViewHolder photoViewHolder = (PhotoViewHolder) viewHolder;
        photoViewHolder.bindView(mPhoto);
    }
}
