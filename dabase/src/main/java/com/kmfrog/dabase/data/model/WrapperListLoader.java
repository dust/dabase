package com.kmfrog.dabase.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.Filter;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.Wrapper;
import com.kmfrog.dabase.data.extra.JsonRequest;
import com.kmfrog.dabase.util.UriUtils;

public class WrapperListLoader<D, W extends Wrapper<List<D>>> extends WrapperForcegroundModel<List<D>, W> implements IListLoader {

    private static final String TAG=WrapperListLoader.class.getSimpleName();

    private final boolean mAutoLoadNextPage;

    private int mCurrentOffset;

    private int mLastPositionRequested;

    private boolean mMoreAvailable;

    private int mWindowDistance;

    protected final List<D> mItems;

    protected final List<D> mFilterResults;

    protected final List<UrlOffsetPair> mUrlOffsetList;

    protected final List<Filter<D>> mFilters;

    public WrapperListLoader(RequestQueue requestQueue, Uri uri) {
        this(requestQueue, uri, true);
    }

    public WrapperListLoader(RequestQueue requestQueue, Uri uri, boolean autoLoadNextPage) {
        this(requestQueue, uri, autoLoadNextPage, new ArrayList<Filter<D>>());
    }

    public WrapperListLoader(RequestQueue requestQueue, Uri uri, boolean autoLoadNextPage, List<Filter<D>> filters) {
        super(requestQueue, uri);
        mWindowDistance=12;
        mAutoLoadNextPage=autoLoadNextPage;
        mItems=new ArrayList<D>();
        mFilterResults=new ArrayList<D>();
        mUrlOffsetList=new ArrayList<UrlOffsetPair>();
        mFilters=filters;
        mUrlOffsetList.add(new UrlOffsetPair(0, uri.toString()));
        mMoreAvailable=true;

    }

    private void requestMoreItemsIfNoRequestExists(UrlOffsetPair uriOffsetPair) {
        if(mCurrentRequest != null) {
            if(mCurrentRequest.getUrl().endsWith(uriOffsetPair.uri)) {
                return;
            }
            mCurrentRequest.cancel();
        }
        mCurrentOffset=uriOffsetPair.offset;
        mCurrentRequest=makeRequest(uriOffsetPair.uri);
        putQueue(mCurrentRequest);
    }

    @Override
    public void clearTransientState() {
        mCurrentRequest=null;
    }

    public void flushUnusedPages() {
        if(mLastPositionRequested >= 0) {
            for(int i=0; i < mItems.size(); i++) {
                if(i < (mLastPositionRequested - mWindowDistance) - 1 || i >= mLastPositionRequested + mWindowDistance) {
                    mItems.set(i, null);
                }
            }
        }
    }

    public boolean hasMore() {
        return mMoreAvailable;
    }

    public int getCount() {
        return hasFilters() ? mFilterResults.size() : mItems.size();
    }

    public final D getItem(int index) {
        if(index < 0) {
            throw new IllegalArgumentException(String.format("Can't return an item with a negative index: %d", index));
        }
        mLastPositionRequested=index;
        D obj=null;
        if(index < getCount()) {
            obj=hasFilters() ? mFilterResults.get(index) : mItems.get(index);
            if(mAutoLoadNextPage && mMoreAvailable && getCount() - index < 5) {
                requestMoreItemsIfNoRequestExists(mUrlOffsetList.get(mUrlOffsetList.size() - 1));
            }
            if(obj == null) {
                UrlOffsetPair pair=null;
                for(Iterator<UrlOffsetPair> iter=mUrlOffsetList.iterator(); iter.hasNext();) {
                    pair=iter.next();
                    if(pair.offset > index) {
                        break;
                    }
                }
                requestMoreItemsIfNoRequestExists(pair);
            }
        }
        return obj;
    }

    public boolean isMoreAvailable() {
        return mMoreAvailable;
    }

    public void onDataBack(List<D> list, W w, Throwable ex) {
        if(list != null && ex == null) {// if(list!=null){//
            clearErrors();
            int listSize=list.size();
            if(mCurrentOffset >= mItems.size()) {
                mItems.addAll(list);
            } else {
                for(int i=0; i < listSize; i++) {
                    D d=list.get(i);
                    if(i + mCurrentOffset < mItems.size()) {
                        mItems.set(i + mCurrentOffset, d);
                    } else {
                        mItems.add(d);
                    }
                }
            }
            if(hasFilters()) {
                mFilterResults.addAll(doFilter(list));
            }
            UrlOffsetPair lastUrlOffsetPair=mUrlOffsetList.get(mUrlOffsetList.size() - 1);
            int ps=getPageSize(lastUrlOffsetPair.uri);
            if(mItems.size() - listSize == lastUrlOffsetPair.offset) {
                mMoreAvailable=ps > 0 && listSize >= ps && mAutoLoadNextPage;
                if(mMoreAvailable) {
                    Uri nextPageUri=getNextPageUri(Uri.parse(lastUrlOffsetPair.uri));
                    mUrlOffsetList.add(new UrlOffsetPair(mItems.size(), nextPageUri.toString()));
                }
            }
        }
    }

    public void reset() {
        mMoreAvailable=true;
        mItems.clear();
        mFilterResults.clear();
        mCurrentRequest=null;
        notifyDataChanged();
    }

    public void startLoadItems() {
        if(mMoreAvailable && getCount() == 0) {
            clearErrors();
            requestMoreItemsIfNoRequestExists(mUrlOffsetList.get(0));
        }
    }

    public boolean isErrorState() {
        return inErrorState();
    }

    public void retryLoadItems() {
        UrlOffsetPair urlOffsetPair;
        if(inErrorState()) {
            clearTransientState();
            urlOffsetPair=null;

            UrlOffsetPair urlOffsetPair2=null;
            if(mCurrentOffset != -1) {
                Iterator<UrlOffsetPair> iter=mUrlOffsetList.iterator();
                do {
                    urlOffsetPair2=iter.next();
                } while(mCurrentOffset != urlOffsetPair2.offset);
                urlOffsetPair=urlOffsetPair2;
            }

            if(urlOffsetPair == null) {
                urlOffsetPair=mUrlOffsetList.get(mUrlOffsetList.size() - 1);
            }
            requestMoreItemsIfNoRequestExists(urlOffsetPair);
        }
    }

    public void refresh() {
        reset();
        startLoadItems();
    }

    public void onDestory() {
        mItems.clear();
        mFilterResults.clear();
        mUrlOffsetList.clear();
        mFilters.clear();
        super.onDestory();
    }

    public void setWindowDistance(int i) {
        mWindowDistance=i;
    }

    public Uri getNextPageUri(Uri currentPageUri) {
        return UriUtils.setPageNo(currentPageUri, UriUtils.getPageNo(currentPageUri) + 1);
    }

    public int getPageSize(String url) {
        return UriUtils.getPageSize(Uri.parse(url));
    }

    public Request<W, byte[]> makeRequest(String uri) {
        return new JsonRequest<W>(Uri.parse(uri), this);
    }

    public boolean getAutoLoadNextPage() {
        return mAutoLoadNextPage;
    }

    public final String getUriByPosition(int position) {
        if(mUrlOffsetList != null && mUrlOffsetList.size() > position) {
            int size=mUrlOffsetList.size();
            for(int i=0; i < size; i++) {
                UrlOffsetPair previous=mUrlOffsetList.get(position);
                int nextIndex=i + 1;
                UrlOffsetPair next=nextIndex >= size ? null : mUrlOffsetList.get(nextIndex);
                if(next == null || position >= previous.offset && position < next.offset) {
                    return previous.uri;
                }
            }
        }
        return null;
    }

    public String getUriOffsetString() {
        return mUrlOffsetList.toString();
    }

    public void saveState(Bundle outState) {
        if(DLog.DEBUG) {
            DLog.d(TAG + "UrlOffsetPair.saveState");
        }
        outState.putString("state", "ok");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void sort(Comparator c) {
        Collections.sort(hasFilters() ? mFilterResults : mItems, c);
        notifyDataChanged();
    }

    @Override
    public boolean hasFilters() {
        return mFilters != null && mFilters.size() > 0;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean addFilter(Filter filter) {
        return mFilters.add(filter);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public boolean removeFilter(Filter filter) {
        return mFilters.remove(filter);
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public void filter(List<Filter> filters) {
        mFilters.clear();
        if(filters != null) {
            for(Filter f: filters) {
                addFilter(f);
            }
        }
        if(hasFilters()) {
            List<D> filterResults=doFilter(mItems);
            mFilterResults.clear();
            mFilterResults.addAll(filterResults);
        }
        notifyDataChanged();
    }

    protected List<D> doFilter(List<D> list) {
        ArrayList<D> results=new ArrayList<D>();
        for(D d: list) {
            if(!shouldDiscardAtFilters(d)) {
                results.add(d);
            }
        }
        return results;
    }

    private boolean shouldDiscardAtFilters(D d) {
        for(Filter<D> f: mFilters) {
            if(f.shouldDiscard(d)) {
                return true;
            }
        }
        return false;
    }

	@Override
	public boolean onClickJudgeMore() {
		return false;
	}

}
