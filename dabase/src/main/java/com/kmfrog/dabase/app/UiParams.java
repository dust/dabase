package com.kmfrog.dabase.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public final class UiParams {

    final int mScreenWidth;

    final int mScreenHeight;

    final double mDensity;

    final double mScaleX;

    final double mScaleXPixels;

    final double mScaleY;

    final int mDensityDpi;

    final boolean mIsPad;

    final int mScreenSize;

    UiParams(Context context) {
        final Resources resources=context.getResources();
        DisplayMetrics display=resources.getDisplayMetrics();
        Configuration cfg=resources.getConfiguration();
        mScreenSize=cfg.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int screenWidth=display.widthPixels;
        int screenHeight=display.heightPixels;
        if(screenWidth > screenHeight) {
            int tmp=screenWidth;
            screenWidth=screenHeight;
            screenHeight=tmp;
        }
        mScreenWidth=screenWidth;
        mScreenHeight=screenHeight;
        mDensity=display.density;
        mDensityDpi=display.densityDpi;
        double rate=Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight) / (160 * mDensity);
        if(rate > 6.0f) {
            mIsPad=true;
        } else {
            mIsPad=false;
        }
        if(mIsPad) {
            mScaleX=1;
        } else {
            mScaleX=mScreenWidth / 480d;
        }
        mScaleXPixels=mScreenWidth / 480d;
        mScaleY=mScreenHeight / 800d;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public double getDensity() {
        return mDensity;
    }

    public double getScaleX() {
        return mScaleX;
    }

    public double getScaleXPixels() {
        return mScaleXPixels;
    }

    public double getScaleY() {
        return mScaleY;
    }

    public int getDensityDpi() {
        return mDensityDpi;
    }

    public boolean isPad() {
        return mIsPad;
    }

    public int getScreenSize() {
        return mScreenSize;
    }

    public int getIntForScalX(int i) {
        return (int)(i * mScaleX);
    }

    public int getIntForScalY(int i) {
        return (int)(i * mScaleY);
    }

    public int getTextSize(int i) {
        return (int)(i * mScaleX / mDensity);
    }

}
