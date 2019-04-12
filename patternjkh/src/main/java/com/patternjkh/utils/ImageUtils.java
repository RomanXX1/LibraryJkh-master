package com.patternjkh.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.patternjkh.R;
import com.patternjkh.enums.FileType;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

	public final static int BITMAP_SIZE_WIDTH = 120;
	public final static int BITMAP_SIZE_HEIGHT = 120;

	public static Bitmap getSizedBitmap(String imagePath, int reqWidth, int reqHeight) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		options.inJustDecodeBounds = false;
		return fixOrientation(BitmapFactory.decodeFile(imagePath, options));
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
											 int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	private static Bitmap fixOrientation(Bitmap bitmap) {
		if (bitmap.getWidth() > bitmap.getHeight()) {
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		}

		return bitmap;
	}

	public static byte[] convertBitmapToByte(Bitmap bitmap){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
		return bos.toByteArray();
	}

	public static int getImageResource(FileType fileType) {
		int resId;
		switch (fileType) {
			case UNKNOWN:
				resId = R.drawable.ic_file;
				break;
			case TXT:
				resId = R.drawable.ic_file_document;
				break;
			case EXCEL:
				resId = R.drawable.ic_file_excel;
				break;
			case WORD:
				resId = R.drawable.ic_file_word;
				break;
			case POWERPOINT:
				resId = R.drawable.ic_file_powerpoint;
				break;
			case PDF:
				resId = R.drawable.ic_file_pdf;
				break;
			case VIDEO:
				resId = R.drawable.ic_file_video;
				break;
			case MUSIC:
				resId = R.drawable.ic_file_music;
				break;
			default:
				resId = R.drawable.ic_file;
				break;
		}
		return resId;
	}
}