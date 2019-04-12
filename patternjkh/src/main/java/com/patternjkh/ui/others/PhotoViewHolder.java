package com.patternjkh.ui.others;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.patternjkh.R;
import com.patternjkh.data.Photo;

public class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    private ImageView mPhotoImageView;

    public PhotoViewHolder(View itemView) {
        super(itemView);
        mPhotoImageView = (ImageView) itemView.findViewById(R.id.photo);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void bindView(Photo photo) {
        byte[] imgByte = photo.getSmall_image();
        if (imgByte != null) {
            mPhotoImageView.setImageBitmap(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
        } else {
            mPhotoImageView.setImageResource(R.drawable.ic_photo_camera);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.id.make_photo,
                Menu.NONE, R.string.action_make_photo);
        menu.add(Menu.NONE, R.id.take_photo_from_gallery,
                Menu.NONE, R.string.action_take_photo_from_gallery);
        menu.add(Menu.NONE, R.id.delete_photo,
                Menu.NONE, R.string.action_delete_photo);
    }
}