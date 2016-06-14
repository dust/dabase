package com.kmfrog.dabase.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.entity.mime.content.ByteArrayBody;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ImageCompressionUtil {

	private static String DRAFT_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/moyoyo/draft";
	private static String DRAFT_SHOW_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/moyoyo/draft/showtemp";
	private String mFileName;
	private File mZipFile;

	public ImageCompressionUtil() {
	}

	private File getZipFile(ByteArrayOutputStream bos) {
		File dirFile = new File(DRAFT_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File showFile = new File(DRAFT_SHOW_PATH);
		if (!showFile.exists()) {
			showFile.mkdirs();
		}
		mZipFile = new File(DRAFT_SHOW_PATH + "/" + mFileName + ".jpg");
		FileOutputStream fos2 = null;
		try {
			fos2 = new FileOutputStream(mZipFile);
			bos.writeTo(fos2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos2 != null) {
				try {
					fos2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return mZipFile;
	}

	public void deleteZipFile() {
		if (mZipFile != null && mZipFile.exists()) {
			mZipFile.delete();
		}
	}

	public File compressImageZipFile(File file, String fileName) {
		ByteArrayOutputStream bos = compressImageBos(file, fileName);
		if (bos == null) {
			return null;
		}
		return getZipFile(bos);
	}

	/**
	 * 图片上传之前，进行压缩
	 */
	public ByteArrayBody compressImage(File file, String fileName) {
		ByteArrayOutputStream bos = compressImageBos(file, fileName);
		if (bos == null) {
			return null;
		}
		byte[] data = bos.toByteArray();
		return new ByteArrayBody(data, fileName + ".jpg");
	}

	private ByteArrayOutputStream compressImageBos(File file, String fileName) {
		if (file == null) {
			return null;
		}
		mFileName = fileName;

		int angle = ImageUtil.readPictureDegree(file.getPath());// 图片可能会有旋转

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// 之前的计算图片的方法，不准确，这里修改获取缩放比例的方法
		// opts.inSampleSize = computeSampleSize(opts, -1,
		// maxNumOfPixels);

		opts.inSampleSize = computeSampleSize(file, opts.outWidth,
				opts.outHeight);
		Log.i("base", opts.inSampleSize + " " + opts.outWidth + " "
				+ opts.outHeight + " " + angle + " " + file.getAbsolutePath());
		// Log.i("base", "Runtime=Memory=0="
		// + Runtime.getRuntime().maxMemory() + " "
		// + Runtime.getRuntime().totalMemory());
		opts.inJustDecodeBounds = false;
		Bitmap bmpCompressed = BitmapFactory.decodeFile(file.getAbsolutePath(),
				opts);
		// BitmapFactory.decodeFileDescriptor(file.get
		Log.i("base", "Runtime=Memory=1=" + Runtime.getRuntime().maxMemory()
				+ " " + Runtime.getRuntime().totalMemory());
		// Bitmap angleBmp;
		if (angle <= 0) {
			bmpCompressed.compress(CompressFormat.JPEG, 90, bos);
		} else {
			Bitmap angleBmp = ImageUtil.rotaingImageView(angle, bmpCompressed);
			angleBmp.compress(CompressFormat.JPEG, 90, bos);
			if (angleBmp != null && !angleBmp.isRecycled()) {
				angleBmp.recycle();
				angleBmp = null;
			}
		}
		if (bmpCompressed != null && !bmpCompressed.isRecycled()) {
			bmpCompressed.recycle();
			bmpCompressed = null;
		}
		return bos;
	}

	private int computeSampleSize(File file, int imgW, int imgH) {
		float baseNumOfPixels = 1 * 1024 * 1024;// 1M基准
		int inSampleSize = 1;

		int imgPixels = imgW * imgH;

		long fileSize = 0;
		try {
			fileSize = getFileSize(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 小于500k标记为小图片
		// 主要针对电脑截图。因为手机拍照图片都是很大的
		boolean smallBmp = fileSize < 500 * 1024 && fileSize > 0;

		if (imgPixels <= baseNumOfPixels) {
			inSampleSize = 1;
		} else if (imgPixels <= baseNumOfPixels * 4) {
			if (smallBmp) {
				inSampleSize = 1;
			} else {
				inSampleSize = 2;
			}
		} else if (imgPixels <= baseNumOfPixels * 9) {
			if (smallBmp) {
				inSampleSize = 2;
			} else {
				inSampleSize = 3;
			}
		} else if (imgPixels <= baseNumOfPixels * 16) {
			if (smallBmp) {
				inSampleSize = 3;
			} else {
				inSampleSize = 4;
			}
		} else if (imgPixels <= baseNumOfPixels * 25) {
			if (smallBmp) {
				inSampleSize = 4;
			} else {
				inSampleSize = 5;
			}
		} else {
			float pixScale = imgPixels / baseNumOfPixels;
			inSampleSize = (int) Math.ceil(Math.sqrt(pixScale));
		}
		return inSampleSize;
	}

	private long getFileSize(File file) throws Exception {// 取得文件大小
		long s = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			s = fis.available();
		} else {
			file.createNewFile();
		}
		return s;
	}

}
