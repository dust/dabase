package com.kmfrog.dabase.data.extra;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.ParserException;
import okhttp3.ResponseBody;

import java.util.Map;


public class BitmapRawParser implements RawParser<Bitmap, ResponseBody> {

    @Override
    public Bitmap parse(ResponseBody raw, Map<String, Object> extra) throws ParserException {
        final int maxHeight = (Integer) extra.get("mMaxHeight");
        final int maxWidth = (Integer) extra.get("mMaxWidth");
        final Bitmap.Config decodeConfig = (Bitmap.Config) extra.get("mDecodeConfig");
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bitmap = null;
        try {
            if (maxHeight == 0 && maxWidth == 0) {
                opts.inPreferredConfig = decodeConfig;
                BitmapFactory.decodeStream(raw.byteStream(), null, opts);
                //bitmap=BitmapFactory.decodeByteArray(raw, 0, raw.length,opts);
            } else {
                opts.inJustDecodeBounds = true;
                int i = opts.outWidth;
                int j = opts.outHeight;
                int k = getResizedDimension(maxWidth, maxHeight, i, j);
                int l = getResizedDimension(maxHeight, maxWidth, j, i);
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = findBestSampleSize(i, j, k, l);
                //TODO: outRect!!!!
                Bitmap def = BitmapFactory.decodeStream(raw.byteStream(), null, opts);
                if (def != null && (def.getWidth() != k || def.getHeight() != l)) {
                    bitmap = Bitmap.createScaledBitmap(def, k, l, true);
                    def.recycle();
                    def = null;
                } else {
                    bitmap = def;
                }
            }
            raw.close();
        } catch (Throwable ex) {//OOM
            throw new ParserException(ex.getMessage(), ex);
        }
        return bitmap;
    }

    static int findBestSampleSize(int i, int j, int k, int l) {
        double d = Math.min((double) i / (double) k, (double) j / (double) l);
        float f;
        for (f = 1F; (double) (f * 2F) <= d; f *= 2F) ;
        return (int) f;
    }

    private static int getResizedDimension(int i, int j, int k, int l) {
        if (i != 0 || j != 0)
            if (i == 0)
                k = (int) (((double) j / (double) l) * (double) k);
            else if (j == 0) {
                k = i;
            } else {
                double d = (double) l / (double) k;
                int i1 = i;
                if (d * (double) i1 > (double) j)
                    i1 = (int) ((double) j / d);
                k = i1;
            }
        return k;
    }

}

