package com.kmfrog.dabase.util;

import java.io.PrintWriter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.kmfrog.dabase.DLog;

public class Alarm extends BroadcastReceiver {

    private static PowerManager.WakeLock mWakeLock;

    private String mAction;

    private AlarmManager mAlarmMgr;

    private Runnable mCallback;

    private Context mContext;

    private Intent mIntent;

    private PendingIntent mIntentSender;

    private long mNextAlarmTime;

    private String mTag;

    private long mInterval;
    
    private volatile boolean isPending;

    public Alarm(Context context, String tag, Runnable runnable) {
        mContext=context;
        mAlarmMgr=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        if(TextUtils.isEmpty(tag)) {
            tag=toString();
        }
        mWakeLock=((PowerManager)mContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        setTag(tag);
        mCallback=runnable;
    }

    public void setTag(String tag) {
        mTag=tag;
    }

    public void setAction(String action) {
        mAction=action;
    }

    public void setInterval(long interval) {
        mInterval=interval;
    }

    public void dump(PrintWriter out) {
        out.println((new StringBuilder()).append("alarm(").append(mTag).append(")").toString());
        out.println((new StringBuilder()).append("    mIntent=").append(mIntent).toString());
        out.println((new StringBuilder()).append("    mTimer=").append(mInterval).append("ms").toString());
        out.println((new StringBuilder()).append("    mNextAlarmTime=").append(mNextAlarmTime).toString());
        if(mNextAlarmTime < SystemClock.elapsedRealtime())
            out.println("    alarm in the past");
        out.println((new StringBuilder()).append("    ").append(mWakeLock.toString()).toString());
    }

    public synchronized long getNextAlarmTime() {
        return mNextAlarmTime;
    }

    public synchronized void initAlarm() {
        if(mIntent == null) {
            if(!TextUtils.isEmpty(mAction)) {
                if(DLog.DEBUG) {
                    DLog.d(new StringBuilder().append("initAlarm for ").append(mTag).toString());
                }
                mContext.registerReceiver(this, new IntentFilter(mAction));
                mIntent=new Intent(mAction);
                mIntentSender=PendingIntent.getBroadcast(mContext, 0, mIntent, 0);
            } else {
                if(DLog.DEBUG) {
                    DLog.e("[Alarm] initAlarm: action is not set!");
                }
            }
        }
    }

    public synchronized boolean isStarted() {
        return mNextAlarmTime != 0L;
    }

    public synchronized void clearAlarm() {
        if(mIntent == null) {
            if(DLog.DEBUG) {
                DLog.d("[Alarm(%s)]clearAlarm:alarm not set", mTag);
            }
        } else {
            if(DLog.DEBUG) {
                DLog.d("[Alarm(%s)]clearAlarm for %s", mTag, mTag);
            }
            stop();
            mContext.unregisterReceiver(this);
            mIntent=null;
            mIntentSender.cancel();
            mIntentSender=null;
        }
    }

    public synchronized void start() {
        mNextAlarmTime=SystemClock.elapsedRealtime() + mInterval;
        if(DLog.DEBUG) {
            DLog.d("[Alarm(%s)]start for %s , mTimer=%d", mTag, mTag, mInterval);
        }
        mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mNextAlarmTime, mIntentSender);
        isPending=true;
    }
    
    public synchronized boolean isPending(){
        return isPending;
    }

    public synchronized void stop() {
        if(mNextAlarmTime != 0L) {
            if(DLog.DEBUG) {
                DLog.d("[Alarm(%s)]stop alarm for", mTag, mTag);
            }
            mAlarmMgr.cancel(mIntentSender);
            mNextAlarmTime=0L;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mWakeLock.acquire();
        long start=SystemClock.elapsedRealtime();
        try{
            if(mCallback==null){
                if(DLog.DEBUG){
                    DLog.d("[Alarm(%s)] onReceive: no callback", mTag);
                }
            }
            else{
                mCallback.run();
            }
            isPending=false;
            if(DLog.DEBUG){
                DLog.v("[Alarm(%s)] onReceive elapsed timemillis: %d", mTag, SystemClock.elapsedRealtime()-start);
            }
        }
        finally{
            mWakeLock.release();
        }
    }

}
