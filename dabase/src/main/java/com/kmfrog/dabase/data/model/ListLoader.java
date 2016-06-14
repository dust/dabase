package com.kmfrog.dabase.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.Filter;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.extra.JsonRequest;
import com.kmfrog.dabase.exception.AppException;
import com.kmfrog.dabase.util.UriUtils;

public class ListLoader<D> extends ForegroundModel<List<D>> implements
		IListLoader {

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

	private boolean mIsPost = false;// 当前是Post请求

	private int mCnt = 20;// 默认20

	public ListLoader(RequestQueue requestQueue, Uri uri) {
		this(requestQueue, uri, true);
	}

	public ListLoader(RequestQueue requestQueue, Uri uri,
			boolean autoLoadNextPage) {
		this(requestQueue, uri, autoLoadNextPage, null);
	}

	public ListLoader(RequestQueue requestQueue, Uri uri,
			boolean autoLoadNextPage, boolean isPost) {
		this(requestQueue, uri, autoLoadNextPage, null);
		mIsPost = isPost;
	}

	public ListLoader(RequestQueue requestQueue, Uri uri,
			boolean autoLoadNextPage, Set<Filter<D>> filters) {
		super(requestQueue, uri);
		mWindowDistance = 12;
		mAutoLoadNextPage = autoLoadNextPage;
		mItems = new ArrayList<D>();
		// mFilters=filters;
		// mFilterResults=mFilters == null ? null : new ArrayList<D>();
		mUrlOffsetList = new ArrayList<UrlOffsetPair>();
		mUrlOffsetList.add(new UrlOffsetPair(0, uri.toString()));
		mMoreAvailable = true;
		mUrlOffsetPair = new UrlOffsetPair(0, uri.toString());

	}

	public ListLoader(RequestQueue requestQueue, Uri uri,
			boolean autoLoadNextPage, boolean moreAvailable,
			boolean onClickJudgeMore) {
		super(requestQueue, uri);
		mWindowDistance = 12;
		mAutoLoadNextPage = autoLoadNextPage;
		mItems = new ArrayList<D>();
		// mFilters=filters;
		// mFilterResults=mFilters == null ? null : new ArrayList<D>();
		mUrlOffsetList = new ArrayList<UrlOffsetPair>();
		mUrlOffsetList.add(new UrlOffsetPair(0, uri.toString()));
		mMoreAvailable = moreAvailable;
		mOnClickJudgeMore = onClickJudgeMore;
		mUrlOffsetPair = new UrlOffsetPair(0, uri.toString());

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
		Log.i("qq", "==requestMoreItemsIfNoRequestExists==++getCount()>>>>>"
				+ getCount());
		if ((mCurrentRequest instanceof JsonRequest) && mIsPost) {
			Map<String, String> map = ((JsonRequest) mCurrentRequest)
					.getPostParams();
			if (map != null) {
				String cntStr = map.get("cnt");
				mCnt = Integer.parseInt(cntStr);
				int currPage = getCount() / mCnt;
				int lastPage = getCount() % mCnt;
				if (lastPage != 0) {// 已经加载到最后一页
					mCurrentRequest.cancel();
					mMoreAvailable = false;
					Log.i("qq",
							"==requestMoreItemsIfNoRequestExists==++getCount()>>>mMoreAvailable>>" + false);
					return;
				}
				// map.put("pos", String.valueOf(currPage));
				map.put("pos", String.valueOf(getCount()));
				((JsonRequest) mCurrentRequest).addPostParams(map);
				Log.i("qq", "==requestMoreItemsIfNoRequestExists==++mSize>>>>>"
						+ currPage);
			}
		}
		putQueue(mCurrentRequest);
	}

	@Override
	public void clearTransientState() {
		mCurrentRequest = null;
	}

	public void flushUnusedPages() {
		if (mLastPositionRequested >= 0) {
			for (int i = 0; i < mItems.size(); i++) {
				if (i < (mLastPositionRequested - mWindowDistance) - 1
						|| i >= mLastPositionRequested + mWindowDistance) {
					mItems.set(i, null);
				}
			}
		}
	}

	public boolean hasMore() {
		return mMoreAvailable;
	}

	public int getCount() {
		return mItems.size();
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
						.hasNext();) {
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

	public boolean isMoreAvailable() {
		return mMoreAvailable;
	}

	public void onDataBack(List<D> list, Throwable ex) {
		if (ex instanceof AppException) {
			final AppException serverException = (AppException) ex;
			if (serverException.getResponseCode() == 555) {
				onDataBackServerEx555(ex);
				return;
			}
		}
		if (list != null && ex == null) {// if(list!=null){//
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
						mMoreAvailable = (getCount() % mCnt == 0)
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
	}

	public void startLoadItems() {
		if (mMoreAvailable && getCount() == 0) {
			clearErrors();
			Log.i("qq", "==requestMoreItemsIfNoRequestExists==3>>>>>");
			requestMoreItemsIfNoRequestExists(mUrlOffsetList.get(0));
		}
	}

	public boolean isErrorState() {
		return inErrorState();
	}

	public void retryLoadItems() {
		UrlOffsetPair urlOffsetPair;
		if (isErrorState()) {
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
			Log.i("qq", "==requestMoreItemsIfNoRequestExists==4>>>>>");
			requestMoreItemsIfNoRequestExists(urlOffsetPair);
		}
	}

	public void refresh() {
		reset();
		startLoadItems();
	}

	public void onDestory() {
		mItems.clear();
		mUrlOffsetList.clear();
		super.onDestory();
	}

	public void setWindowDistance(int i) {
		mWindowDistance = i;
	}

	public Uri getNextPageUri(Uri currentPageUri) {
		return UriUtils.setPageNo(currentPageUri,
				UriUtils.getPageNo(currentPageUri) + 1);
	}

	public int getPageSize(String url) {
		return UriUtils.getPageSize(Uri.parse(url));
	}

	public Request<List<D>, byte[]> makeRequest(String uri) {
		return new JsonRequest<List<D>>(Uri.parse(uri), this);
	}

	public boolean getAutoLoadNextPage() {
		return mAutoLoadNextPage;
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

	public void saveState(Bundle outState) {
		if (DLog.DEBUG) {
			DLog.d(TAG + "UrlOffsetPair.saveState");
		}
		outState.putString("state", "ok");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void sort(Comparator c) {
		Collections.sort(mItems, c);
		notifyDataChanged();
	}

	@Override
	public boolean hasFilters() {
		// return mFilters != null && mFilters.size() > 0;
		return false;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean addFilter(Filter filter) {
		return false; // mFilters.add(filter);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean removeFilter(Filter filter) {
		return false;// mFilters.remove(filter);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void filter(List<Filter> filters) {
	}

	protected List<D> doFilter(List<D> list) {
		ArrayList<D> results = new ArrayList<D>();
		for (D d : list) {
			if (!shouldDiscardAtFilters(d)) {
				results.add(d);
			}
		}
		return results;
	}

	private boolean shouldDiscardAtFilters(D d) {
		// for(Filter<D> f: mFilters) {
		// if(f.shouldDiscard(d)) {
		// return true;
		// }
		// }
		return false;
	}

	@Override
	public boolean onClickJudgeMore() {
		return mOnClickJudgeMore;
	}

	public void requestMoreItems(Uri uri) {
		mLoadMoreBtnFlag = true;
		UrlOffsetPair urlOffsetPair = new UrlOffsetPair(mItems.size(),
				uri.toString());
		Log.i("qq", "==requestMoreItemsIfNoRequestExists==6>>>>>");
		requestMoreItemsIfNoRequestExists(urlOffsetPair);
		mUrlOffsetList.clear();
		mUrlOffsetList.add(new UrlOffsetPair(0, uri.toString()));
	}

	public void reLoaadItems() {
		mUrlOffsetList.clear();
		mUrlOffsetList.add(mUrlOffsetPair);
	}

	protected void onDataBackServerEx555(Throwable ex)
			throws ClassCastException {

	}

}
