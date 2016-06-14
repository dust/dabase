package com.kmfrog.dabase.adapter;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.fragment.BaseFragment;
import com.kmfrog.dabase.fragment.TagListener;


public class FragmentsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, TagListener{
    
   public static class FragmentInfo{
        public String title;
        public Class<? extends Fragment> frgmntClz;
        public Bundle args;
        public String tag;
        
        public FragmentInfo(String title, Class<? extends BaseFragment> frgmntClz, Bundle args){
            this.title = title;
            this.frgmntClz = frgmntClz;
            this.args = args;
        }
    }
    
    private Context mContext;
    
    private List<FragmentInfo> mFrgmntInfos;
    
    private FragmentManager mFrgmntMgr;

    FragmentsAdapter(Context ctx,FragmentManager fm) {
        super(fm);
        mContext=ctx;
        mFrgmntMgr=fm;
    }
    
    public FragmentsAdapter(Context ctx, FragmentManager fm, List<FragmentInfo> frgmntInfos){
        this(ctx,fm);
        mFrgmntInfos=frgmntInfos;
    }
    
    public final BaseFragment getFragment(int position){
        String tag = mFrgmntInfos.get(position).tag;
        if(tag==null){
            return null;
        }
        return (BaseFragment)mFrgmntMgr.findFragmentByTag(tag);
    }

    @Override
    public Fragment getItem(int position) {
        FragmentInfo frgmntInfo=mFrgmntInfos.get(position);
        BaseFragment f=(BaseFragment)BaseFragment.createInstance(mContext, frgmntInfo.frgmntClz.getName(), frgmntInfo.args, this, position);
        return f;
    }

    @Override
    public int getCount() {
        return mFrgmntInfos.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        FragmentInfo frgmntInfo=mFrgmntInfos.get(position);
        return frgmntInfo.title;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        
    }

    @Override
    public void onPageSelected(int position) {
        setFragmentSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setFragmentSelected(int position){
        Toast.makeText(mContext, "page(fragment) selected:"+position,Toast.LENGTH_LONG).show();
    }

    @Override
    public void notifyTag(BaseFragment f, int position, String tag) {
        Class<? extends BaseFragment> clz=f.getClass();
        FragmentInfo fi=mFrgmntInfos.get(position);
        if(fi!=null && fi.frgmntClz.equals(clz)){
            fi.tag = tag;
        }
        if(DLog.DEBUG){
            DLog.d("%s.tag=%s FragmentInfo.tag=%s", clz.getSimpleName(),tag,fi.tag);
        }
    }
}
