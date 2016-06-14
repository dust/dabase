package com.kmfrog.dabase.data.extra;

import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.ParserException;
import com.kmfrog.dabase.util.BitmapUtils;


public class DrawableBitmapParser implements RawParser<Bitmap, Drawable> {

    public DrawableBitmapParser() {
    }

    @Override
    public Bitmap parse(Drawable raw, Map<String, Object> extra) throws ParserException {
        return BitmapUtils.drawable2Bitmap(raw);
    }

}
