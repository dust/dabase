package com.kmfrog.dabase.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.kmfrog.dabase.util.DigestUtils;
import com.kmfrog.dabase.util.ParameterUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.UUID;

public abstract class BaseApp extends Application {

    private static String DOWNJOY_IMEI = "DOWNJOY_IMEI";


    protected UiParams mUiParams;

    protected String mIMEI;

    protected String mMAC;

    protected String mIMSI;

    protected String mNumber;

    protected String mMobileOperator;

    protected String mVersionName;

    protected int mVersionCode = -1;

    protected String mHeadValue;

    protected abstract String getChannelId();

    protected abstract String getKey();

    @Override
    @SuppressWarnings("rawtypes")
    public void onCreate() {
        super.onCreate();
        mUiParams = new UiParams(this);


        startBuildHeaderValue();
    }

    public UiParams getUiParams() {
        return mUiParams;
    }

    public int getScreenWidth() {
        return mUiParams.mScreenWidth;
    }

    public int getScreenHeight() {
        return mUiParams.mScreenHeight;
    }

    public double getDensity() {
        return mUiParams.mDensity;
    }

    public double getScaleX() {
        return mUiParams.mScaleX;
    }

    public double getScaleXPixels() {
        return mUiParams.mScaleXPixels;
    }

    public double getScaleY() {
        return mUiParams.mScaleY;
    }

    public int getDensityDpi() {
        return mUiParams.mDensityDpi;
    }

    public boolean isPad() {
        return mUiParams.mIsPad;
    }

    public int getIntForScalX(int i) {
        return mUiParams.getIntForScalX(i);
    }

    public int getIntForScalY(int i) {
        return mUiParams.getIntForScalY(i);
    }

    public int getTextSize(int i) {
        return mUiParams.getTextSize(i);
    }

    public boolean isProt() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        return true;
    }

    protected void initNumber(TelephonyManager telephonymanager) {
        try {
            mNumber = telephonymanager.getLine1Number();
        } catch (Exception ex) {
        }
    }

    public String rebuildHeaderValue() {
        buildHeaderValue();
        return mHeadValue;
    }

    private void buildHeaderValue() {
        final String pkg = getPackageName();
        if (mVersionName == null && mVersionCode == -1) {
            try {
                PackageInfo packageinfo = getPackageManager().getPackageInfo(
                        pkg, PackageManager.GET_META_DATA);
                mVersionName = packageinfo.versionName;
                mVersionCode = packageinfo.versionCode;
            } catch (Exception ex) {
            }
        }

        TelephonyManager telephonymanager = null;
        if (mIMEI == null) {
            telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mIMEI = telephonymanager.getDeviceId();// Settings.Secure.getString(context.getContentResolver(),
            // Settings.Secure.ANDROID_ID);
        }
        if (mIMSI == null) {
            if (telephonymanager == null) {
                telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            }
            mIMSI = telephonymanager.getSubscriberId();
        }
        if (mMobileOperator == null) {
            if (telephonymanager == null) {
                telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            }
            mMobileOperator = telephonymanager.getSimOperator();
        }
        if (TextUtils.isEmpty(mNumber)) {
            if (telephonymanager == null) {
                telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            }
            initNumber(telephonymanager);
        }
        if (TextUtils.isEmpty(mIMEI)) {
            mIMEI = ParameterUtil.getParameter(this, DOWNJOY_IMEI, null);
        }
        if (TextUtils.isEmpty(mIMEI)) {
            mIMEI = UUID.randomUUID().toString();
            ParameterUtil.saveParameter(this, DOWNJOY_IMEI, mIMEI, false);
        }
        mMAC = getMacAddress(this);
        final String channelId = getChannelId();
        final int sdkInt = Build.VERSION.SDK_INT;
        final String device = Build.MODEL;
        JSONObject root = new JSONObject();
        try {
            JSONObject clientInfo = new JSONObject();
            clientInfo.put("imei", mIMEI);
            clientInfo.put("pkg", pkg);
            clientInfo.put("vc", mVersionCode);
            clientInfo.put("ccid", channelId);
            clientInfo.put("num", mNumber);
            if (mUiParams != null) {
                clientInfo.put("ss", mUiParams.mScreenSize);
            }
            clientInfo.put("sdk", sdkInt);
            clientInfo.put("vn", mVersionName);
            clientInfo.put("dev", device);
            clientInfo.put("imsi", mIMSI);
            clientInfo.put("op", mMobileOperator);
            clientInfo.put("mac", mMAC);
            JSONObject appendInfo = appendHeaderClientInfo();
            if (appendInfo != null) {
                clientInfo.put("ext", appendInfo);
            }
            root.put("clientInfo", clientInfo);

            JSONObject userInfo = appendHeaderUserInfo();
            if (userInfo != null) {
                root.put("userInfo", userInfo);
            }
            root.put(
                    "vfc",
                    genVerifyCode(channelId, mIMEI, mVersionCode, pkg, device,
                            mVersionName, mUiParams.mScreenSize, mNumber,
                            sdkInt, userInfo, getKey()));
        } catch (Exception ex) {

        }
        mHeadValue = root.toString();
    }

    public String getHeadValue() {
        return mHeadValue;
    }

    private String getMacAddress() {
        String macSerial = null;
        String str = "";
        try {
            java.lang.Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    protected String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return getMacAddress();
        }
        WifiInfo info = wifi.getConnectionInfo();
        if (info == null) {
            return getMacAddress();
        }
        return info.getMacAddress();
    }

    public void startBuildHeaderValue() {
        (new Thread() {

            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                buildHeaderValue();
            }
        }).start();
    }

    private static String genVerifyCode(String channelId, String imei,
                                        int versionCode, String pkg, String device, String versionName,
                                        int screenSize, String number, int sdkInt, JSONObject userJsonObj,
                                        String key) {
        StringBuilder sb = new StringBuilder();
        sb.append(channelId).append("_").append(imei).append("_")
                .append(versionCode).append("_").append(pkg);
        if (!TextUtils.isEmpty(device)) {
            sb.append("_").append(device);
        }
        if (!TextUtils.isEmpty(versionName)) {
            sb.append("_").append(versionName);
        }

        if (screenSize > 0) {
            sb.append("_").append(screenSize);
        }
        if (!TextUtils.isEmpty(number)) {
            sb.append("_").append(number);
        }
        if (sdkInt > 0L) {
            sb.append("_").append(sdkInt);
        }
        if (userJsonObj != null && userJsonObj.has("uid")) {
            sb.append("_").append(userJsonObj.optString("uid"));
        }
        sb.append("_").append(key);
        return DigestUtils.getMd5Hex(sb.toString());

    }


    protected File getCacheDir(String dir) {
        File file = new File(getCacheDir(), dir);
        file.mkdirs();
        return file;
    }

    protected JSONObject appendHeaderClientInfo() {
        return null;
    }

    protected JSONObject appendHeaderUserInfo() {
        return null;
    }

}
