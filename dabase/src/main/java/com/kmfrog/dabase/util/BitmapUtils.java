package com.kmfrog.dabase.util;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public abstract class BitmapUtils {

    public static Bitmap drawable2Bitmap(Drawable d) {
        Bitmap bitmap=
            Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), d.getOpacity() != PixelFormat.OPAQUE
                ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(bitmap);
        d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
        d.draw(canvas);
        return bitmap;
    }

    public static byte[] bitmap2ByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
