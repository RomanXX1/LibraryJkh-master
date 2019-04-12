package com.patternjkh.ui.others;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.patternjkh.R;
import com.patternjkh.data.Photo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

    private List<ViewHolderBinder> mBinders = Collections.emptyList();

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_new, parent, false);
        PhotoViewHolder photoViewHolder = new PhotoViewHolder(v);
        return photoViewHolder;
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder photoViewHolder, int position) {
        ViewHolderBinder binder = mBinders.get(position);
        binder.bind(photoViewHolder);
    }

    @Override
    public int getItemCount() {
        return mBinders.size();
    }

    public void setPhotos(List<Photo> photos) {
        mBinders = createBinders(photos);
        notifyDataSetChanged();
    }

    private List<ViewHolderBinder> createBinders(List<Photo> photos) {
        if (photos != null && photos.size() == 0) {
            return Collections.emptyList();
        }

        List<ViewHolderBinder> binders = new ArrayList<>(photos.size());

        for (Photo photo:photos){
            PhotoViewHolderBinder binder = new PhotoViewHolderBinder(photo);
            binders.add(binder);
        }

        return binders;
    }
}
