package com.patternjkh.ui.apps;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Photo;
import com.patternjkh.enums.FileType;
import com.patternjkh.utils.FileUtils;
import com.patternjkh.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;

public class Photo_Adapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<Photo> objects;
    private DB db;
    private Server server;
    private Handler handler;

    private AlertDialog.Builder dialogDeletePhoto;

    private int mPositionClick;

    public Photo_Adapter(Context ctx, ArrayList<Photo> objects) {
        this.ctx = ctx;
        this.objects = objects;
        server = new Server(ctx);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Photo photo = getPhoto(position);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View view = inflater.inflate(R.layout.item_photo, null, true);
        ImageView mPhotoImageView = view.findViewById(R.id.small_foto);
        TextView mPhotoNameTextView = view.findViewById(R.id.foto_text);
        TextView mPhotoDateTextView = view.findViewById(R.id.foto_date);
        ImageView mPhotoDeleteImageView = view.findViewById(R.id.del_foto);

        // Если этот функционал заработает, будут проблемы с корректным отображением файлов пришедших с сервера,
        // т.к. при добавление нового фото на сервер уходит файл с номером (mPhotos.size()+1)
        dialogDeletePhoto = new AlertDialog.Builder(ctx);
        dialogDeletePhoto.setTitle(R.string.choice_del_photo);
        dialogDeletePhoto.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                db = new DB(ctx);
                db.open();
                db.del_photo_by_name(photo.getNumber(), photo.getFoto_path());

                objects.remove(photo);
                notifyDataSetChanged();
            }
        });
        dialogDeletePhoto.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        String finalPhotoName = constructPhotoName(mPhotoImageView, photo);
        if (finalPhotoName.startsWith(" ")) {
            finalPhotoName = finalPhotoName.replaceFirst(" ", "");
        }
        mPhotoNameTextView.setText(finalPhotoName);
        mPhotoDateTextView.setText(photo.getDate());

        mPhotoDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDeletePhoto.show();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String photo_path = photo.getFoto_path();
                if (photo_path.equals("")) {

                    LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.download_layout, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setView(layout);
                    final AlertDialog alert = builder.create();
                    alert.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String line = server.getFile(photo.getId(), photo.getName());

                            if (!line.equals("")) {
                                alert.dismiss();
                                photo.setFoto_path(line);

                                db = new DB(ctx);
                                db.open();
                                db.updateFotoPath(photo.getId(), photo.getFoto_path());

                                mPositionClick = position;
                                handler.sendEmptyMessage(1);
                            } else {
                                alert.dismiss();
                                handler.sendEmptyMessage(2);
                            }

                        }

                    }).start();
                } else {
                    startActivity(photo);
                }
            }
        });

        handler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    startActivity(getPhoto(mPositionClick));
                } else if (message.what == 2) {
                    Toast.makeText(ctx, "Не переданы обязательные параметры", Toast.LENGTH_LONG).show();
                }
            }
        };

        return view;
    }

    private String constructPhotoName(ImageView mPhotoImageView, Photo photo) {
        final byte[] imgByte = photo.getSmall_image();
        String photo_name = photo.getName();
        if (imgByte != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            mPhotoImageView.setImageBitmap(bitmap);
            if (photo_name.contains("File")) {
                photo_name = photo_name.replaceAll("  ", " ");
                photo_name = photo_name.replaceFirst("File [0-9]*", "");
                photo_name = photo_name.replaceAll("\\(app ", "Фото по обращению №");
                photo_name = photo_name.replaceAll("\\).jpg", "");
            }
        } else {
            FileType fileType = photo.getFileType();
            int resId = ImageUtils.getImageResource(fileType);
            mPhotoImageView.setImageResource(resId);
            String fileExtension;
            String filePath = photo.getFoto_path();
            if (filePath != null && !filePath.equals("")) {
                fileExtension = FileUtils.getFileExtension(filePath);
            } else {
                fileExtension = FileUtils.getFileExtension(photo_name);
            }
            if (photo_name.contains("File")) {
                photo_name = photo_name.replaceAll("  ", " ");
                photo_name = photo_name.replaceFirst("File [0-9]*", "");
                photo_name = photo_name.replaceAll("\\(app ", "Файл по обращению №");
                photo_name = photo_name.replaceAll("\\)." + fileExtension, "");
            }
        }

        return photo_name;
    }

    private void startActivity(Photo photo) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(photo.getFoto_path());
        String fileExtensionForMimeType = FileUtils.getFileExtension(photo.getFoto_path());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionForMimeType);
        Uri uri = FileUtils.getUriForFile(ctx,file);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Нет приложения для открытия данного типа файла", Toast.LENGTH_LONG).show();
        }
    }

    private Photo getPhoto(int position) {
        return ((Photo) getItem(position));
    }

}
