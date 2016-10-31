package com.kmfrog.dabase.data.model;

import android.net.Uri;
import android.util.Log;
import com.kmfrog.dabase.app.BaseApp;
import com.kmfrog.dabase.data.BaseRequest;
import com.kmfrog.dabase.data.JsonRequest;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.util.UriUtils;
import okhttp3.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dust on 16-10-12.
 */
public class ListLoader<D> extends UiModel<List<D>> {

    private static final String TAG = ListLoader.class.getSimpleName();

    private final boolean mAutoLoadNextPage;

    private int mCurrentOffset;

    private int mLastPositionRequested;

    private boolean mMoreAvailable;

    private int mWindowDistance;

    protected final List<D> mItems;

    protected final List<UrlOffsetPair> mUrlOffsetList;

    private boolean mOnClickJudgeMore;

    private UrlOffsetPair mUrlOffsetPair;

    private int mSize;

    private boolean mLoadMoreBtnFlag;

    private volatile AtomicBoolean mIsRefreshHold;

    private boolean mIsPost = false;// 当前是Post请求

    private int mPageSize = 20;// 默认20


    public ListLoader(BaseApp app, Uri uri, RawParser<List<D>, ResponseBody> parser) {
        this(app, uri, parser, true, false);
    }

    public ListLoader(BaseApp app, Uri uri, RawParser<List<D>, ResponseBody> parser, boolean autoLoadNextPage, boolean isPostMethod) {
        super(app, uri, parser);
        mIsPost = isPostMethod;
        mAutoLoadNextPage = autoLoadNextPage;

        mWindowDistance = 12;
        mItems = new ArrayList<D>();

        mUrlOffsetPair = new UrlOffsetPair(0, mUri.toString());
        mUrlOffsetList = new ArrayList<UrlOffsetPair>();
        mUrlOffsetList.add(mUrlOffsetPair);
        mMoreAvailable = true;

        mIsRefreshHold = new AtomicBoolean(false);

    }


    private void requestMoreItemsIfNoRequestExists(UrlOffsetPair uriOffsetPair) {
        if (mCurrentRequest != null) {
            if (mCurrentRequest.getUrl().endsWith(uriOffsetPair.uri)) {
                return;
            }
            mCurrentRequest.cancel();
        }
        mCurrentOffset = uriOffsetPair.offset;
        mCurrentRequest = makeRequest(uriOffsetPair.uri);
//        Log.i("qq", "==requestMoreItemsIfNoRequestExists==++getCount()>>>>>"
//                + getCount());
        if (mIsPost) {
            mCurrentRequest.setShouldCache(false);
        }
        putQueue(mCurrentRequest);
    }

    @Override
    public void clearTransientState() {
        mCurrentRequest = null;
    }

    public boolean hasMore() {
        return mMoreAvailable;
    }

    public int getCount() {
        return mItems.size();
    }

    public List<D> getItems() {
        return mItems;
    }

    public boolean getAutoLoadNextPage() {
        return mAutoLoadNextPage;
    }

    public void setWindowDistance(int i) {
        mWindowDistance = i;
    }


    public final D getItem(int index) {
        if (index < 0) {
            throw new IllegalArgumentException(String.format(
                    "Can't return an item with a negative index: %d", index));
        }
        mLastPositionRequested = index;
        D obj = null;
        if (index < getCount()) {
            obj = mItems.get(index);
            Log.i("qq", "==requestMoreItemsIfNoRequestExists==0>>0>>>"
                    + mAutoLoadNextPage + " " + mMoreAvailable + " "
                    + (getCount() - index));
            if (mAutoLoadNextPage && mMoreAvailable && (getCount() - index < 5)) {
                Log.i("qq", "==requestMoreItemsIfNoRequestExists==0>>>>>");
                requestMoreItemsIfNoRequestExists(mUrlOffsetList
                        .get(mUrlOffsetList.size() - 1));
            }
            if (obj == null) {
                UrlOffsetPair pair = null;
                for (Iterator<UrlOffsetPair> iter = mUrlOffsetList.iterator(); iter
                        .hasNext(); ) {
                    pair = iter.next();
                    if (pair.offset > index) {
                        break;
                    }
                }
                Log.i("qq", "==requestMoreItemsIfNoRequestExists==1>>>>>");
                requestMoreItemsIfNoRequestExists(pair);
            }
        }
        return obj;
    }

    public void onDataBack(List<D> list, Throwable ex) {
        if (list != null && ex == null) {// if(list!=null){//
            if (mIsRefreshHold.get()) {
                mItems.clear();
                mIsRefreshHold.set(false);
            }
            clearErrors();
            int listSize = list.size();
            if (mCurrentOffset >= mItems.size()) {
                mItems.addAll(list);
            } else if (mLoadMoreBtnFlag && mOnClickJudgeMore) {
                mItems.addAll(list);
            } else {

                for (int i = 0; i < listSize; i++) {
                    D d = list.get(i);
                    if (i + mCurrentOffset < mItems.size()
                            && i + mCurrentOffset >= 0) {
                        mItems.set(i + mCurrentOffset, d);
                    } else {
                        mItems.add(d);
                    }
                }
            }
            // if(hasFilters()) {
            // mFilterResults.addAll(doFilter(list));
            // }
            int size = mUrlOffsetList.size();
            if (size > 0) {

                UrlOffsetPair lastUrlOffsetPair = mUrlOffsetList.get(size - 1);
                int ps = getPageSize(lastUrlOffsetPair.uri);// 此处的ps是根据GET请求的参数ps获得的
                // 所以对于POST的请求方式，不能将此参数比较在内
                if (mIsPost) {
                    ps = 1;
                }

                if (mLoadMoreBtnFlag && mOnClickJudgeMore) {
                    mSize += list.size();
                    if (mSize - listSize == lastUrlOffsetPair.offset) {
                        mMoreAvailable = ps > 0 && listSize >= ps
                                && mAutoLoadNextPage;
                        if (mIsPost) {// 所以对于POST的请求方式，不能将此参数比较在内
                            mMoreAvailable = listSize >= ps
                                    && mAutoLoadNextPage;
                        }
                        Log.i("qq", "----" + (ps > 0) + " " + (listSize >= ps));
                        if (mMoreAvailable) {
                            Uri nextPageUri = getNextPageUri(Uri
                                    .parse(lastUrlOffsetPair.uri));
                            mUrlOffsetList.add(new UrlOffsetPair(mSize,
                                    nextPageUri.toString()));
                        }
                    }
                    return;
                }

                if (mItems.size() - listSize == lastUrlOffsetPair.offset) {
                    mMoreAvailable = ps > 0 && listSize >= ps
                            && mAutoLoadNextPage;
                    if (mIsPost) {// 所以对于POST的请求方式，不能将此参数比较在内
                        // 此处判定规则
                        mMoreAvailable = (getCount() % mPageSize == 0)
                                && listSize >= ps && mAutoLoadNextPage;
                    }
                    Log.i("qq", "--++--" + (ps > 0) + " " + (listSize >= ps));
                    if (mMoreAvailable) {
                        Uri nextPageUri = getNextPageUri(Uri
                                .parse(lastUrlOffsetPair.uri));
                        mUrlOffsetList.add(new UrlOffsetPair(mItems.size(),
                                nextPageUri.toString()));
                    }
                }
            }
        }
    }


    public void reset() {
        mMoreAvailable = true;
        mItems.clear();
        mCurrentRequest = null;
        notifyDataChanged();
        mSize = 0;
        mLoadMoreBtnFlag = false;
        int size = mUrlOffsetList.size();
        if (size > 1) {
            for (int i = 1; i < size; i++) {
                mUrlOffsetList.remove(i);
            }
        }
    }

    public void startLoadItems() {
        if (!mIsRefreshHold.get() && mMoreAvailable && getCount() == 0) {
            clearErrors();
            //Log.i("qq", "==requestMoreItemsIfNoRequestExists==3>>>>>");
            requestMoreItemsIfNoRequestExists(mUrlOffsetList.get(0));
        }
    }

    public void retryLoadItems() {
        UrlOffsetPair urlOffsetPair;
        if (inErrorState()) {
            clearTransientState();
            urlOffsetPair = null;

            UrlOffsetPair urlOffsetPair2 = null;
            if (mCurrentOffset != -1) {
                Iterator<UrlOffsetPair> iter = mUrlOffsetList.iterator();
                do {
                    urlOffsetPair2 = iter.next();
                } while (mCurrentOffset != urlOffsetPair2.offset);
                urlOffsetPair = urlOffsetPair2;
            }

            if (urlOffsetPair == null) {
                urlOffsetPair = mUrlOffsetList.get(mUrlOffsetList.size() - 1);
            }
            //Log.i("qq", "==requestMoreItemsIfNoRequestExists==4>>>>>");
            requestMoreItemsIfNoRequestExists(urlOffsetPair);
        }
    }

    public void refresh() {
        reset();
        startLoadItems();
    }

    public synchronized void refreshWithHold() {
        if (!mIsRefreshHold.get()) {
            mIsRefreshHold.set(true);
            mMoreAvailable = true;
            mCurrentRequest = null;
            mLoadMoreBtnFlag = false;

            clearErrors();

            int size = mUrlOffsetList.size();
            UrlOffsetPair pair = null;
            if (size > 1) {
                for (int i = 1; i < size; i++) {
                    mUrlOffsetList.remove(i);
                }
                pair = mUrlOffsetList.get(0);
            }

            if (pair != null) {
                requestMoreItemsIfNoRequestExists(pair);
            }
        }
    }

    public void destory() {
        mItems.clear();
        mUrlOffsetList.clear();
        super.destory();
    }

    public Uri getNextPageUri(Uri currentPageUri) {
        return UriUtils.setPageNo(currentPageUri,
                UriUtils.getPageNo(currentPageUri) + 1);
    }

    public int getPageSize(String url) {
        return UriUtils.getPageSize(Uri.parse(url));
    }

    public final String getUriByPosition(int position) {
        if (mUrlOffsetList != null && mUrlOffsetList.size() > position) {
            int size = mUrlOffsetList.size();
            for (int i = 0; i < size; i++) {
                UrlOffsetPair previous = mUrlOffsetList.get(position);
                int nextIndex = i + 1;
                UrlOffsetPair next = nextIndex >= size ? null : mUrlOffsetList
                        .get(nextIndex);
                if (next == null || position >= previous.offset
                        && position < next.offset) {
                    return previous.uri;
                }
            }
        }
        return null;
    }

    public String getUriOffsetString() {
        return mUrlOffsetList.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void sort(Comparator c) {
        Collections.sort(mItems, c);
        notifyDataChanged();
    }

    public BaseRequest<List<D>, ResponseBody> makeRequest(String uri) {
        return new JsonRequest<List<D>>(Uri.parse(uri), mParser, this);
    }
}
