package com.kmfrog.dabase.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.widget.ImageView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 *
 */
public class BitmapLoader {

    private static volatile BitmapLoader sInstance;

    private static final Object lock = new Object();

    private final ImageLoader mImageLoader;

    private BitmapLoader(Context ctx, int imgCacheSize) {
        File cacheDir = StorageUtils.getCacheDirectory(ctx);  //缓存文件夹路径
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(ctx);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new MixedFileNameGenerator());
        config.diskCache(new UnlimitedDiskCache(cacheDir));
        config.diskCacheSize(imgCacheSize); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
//        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
        mImageLoader = ImageLoader.getInstance();
    }


    public static BitmapLoader getInstance(final Context ctx, int imgCacheSize) {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new BitmapLoader(ctx, imgCacheSize);
                }
            }
        }
        return sInstance;
    }

    public void bind(final ImageView imageView, String url, int placeHolderResId, BitmapDisplayer decorator) {
        bind(imageView, url, placeHolderResId, decorator, 0, 0);
    }

    public void bind(final ImageView imageView, String url, int placeHolderResId) {
        bind(imageView, url, placeHolderResId, null, 0, 0);
    }

    public void bind(final ImageView imageView, String url, int placeHolderResId, int maxWidth, int maxHeight) {
        bind(imageView, url, placeHolderResId, null, maxWidth, maxHeight);
    }


    public void bind(final ImageView imageView, final String url, final int placeHolderResId, BitmapDisplayer decorator,
                     int maxWidth, int maxHeight) {

        DisplayImageOptions options;
        if (decorator != null) {
            options =
                    new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true)
                            .imageScaleType(ImageScaleType.IN_SAMPLE_INT).displayer(decorator).showImageForEmptyUri(placeHolderResId)
                            .showImageOnFail(placeHolderResId).showImageOnLoading(placeHolderResId).bitmapConfig(Bitmap.Config.RGB_565).build();
        } else {
            options =
                    new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true)
                            .imageScaleType(ImageScaleType.IN_SAMPLE_INT).showImageForEmptyUri(placeHolderResId)
                            .showImageOnFail(placeHolderResId).showImageOnLoading(placeHolderResId).bitmapConfig(Bitmap.Config.RGB_565).build();
        }

        // boolean isLoadFromNet=true;// 加载网络图片。不是省流量
        // Settings setting=new Settings(App.get(), Constants.SETTINGS_NAME);
        // boolean traffic=setting.getBoolean(Constants.SETTINGS_KEY_SAVING_TRAFFIC, false);
        // if(!CommonUtil.isWIFI() && traffic) {
        // isLoadFromNet=false;
        // }
        mImageLoader.displayImage(url, imageView, options);
    }


    /**
     * 加载圆形头像,不缓存;
     *
     * @param avatar
     * @param imageView
     * @param useCache
     */
    public void bindAvatar(String avatar, ImageView imageView, int placeHolderResId, boolean useCache) {
        final DisplayImageOptions options = getAvatarOptions(placeHolderResId, useCache);
        mImageLoader.displayImage(avatar, imageView, options);

    }

    private static DisplayImageOptions getAvatarOptions(int placeHolderResId, boolean shouldUseCach) {
        final DisplayImageOptions options =
                new DisplayImageOptions.Builder().showStubImage(placeHolderResId).resetViewBeforeLoading(true)
                        .showImageForEmptyUri(placeHolderResId).showImageOnFail(placeHolderResId)
                        .cacheInMemory(shouldUseCach).cacheOnDisc(shouldUseCach).imageScaleType(ImageScaleType.IN_SAMPLE_INT).displayer(
                        new BitmapDisplayer() {

                            @Override
                            public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
                                imageAware.setImageBitmap(toRoundBitmap(bitmap));
                            }
                        }).bitmapConfig(Bitmap.Config.RGB_565).build();
        return options;

    }


    public static Bitmap createBitmapThumbnail(Bitmap src, int w, int h) {
        return ThumbnailUtils.extractThumbnail(src, w, h);

    }


    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }


}
