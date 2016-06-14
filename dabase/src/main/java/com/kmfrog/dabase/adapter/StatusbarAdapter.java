package com.kmfrog.dabase.adapter;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.kmfrog.dabase.data.RequestQueue;
import com.kmfrog.dabase.data.model.IListLoader;

public abstract class StatusbarAdapter<D> extends BaseListAdapter<D> {

	private FooterMode mFooterMode;

	protected View.OnClickListener mRetryClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mFooterMode == FooterMode.ERROR) {
				// if(!DiguaApp.get().hasConnectedNetwork()) {
				// ToastUtil.getInstance(DiguaApp.get().getContext()).makeText(R.string.dialog_no_network_title);
				// return;
				// }
				retryLoadingItems();
				setFooterMode(FooterMode.LOADING);
			}
		}
	};

	public StatusbarAdapter(RequestQueue requestQueue, Uri uri,
			boolean autoLoadNextPage) {
		super(requestQueue, uri, autoLoadNextPage);
		setStatusBar();
	}

	public StatusbarAdapter(IListLoader loader) {
		super(loader);
		setStatusBar();
	}

	protected void setStatusBar() {
		if (mListLoader.isErrorState()) {
			mFooterMode = FooterMode.ERROR;
		} else if (mListLoader.isMoreAvailable()) {
			mFooterMode = FooterMode.LOADING;
		} else if (mListLoader.onClickJudgeMore()) {
			mFooterMode = FooterMode.LOADBTN;
		} else {
			mFooterMode = FooterMode.NONE;
		}
	}

	@Override
	public int getCount() {
		if (mListLoader == null) {
			return 0;
		}
		int count = mListLoader.getCount();
		if (getFooterMode() != FooterMode.NONE
				&& getFooterMode() != FooterMode.LOADBTN) {
			count++;
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public D getItem(int position) {
		if (mListLoader == null || position > mListLoader.getCount()) {
			return null;
		}
		return (D) mListLoader.getItem(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == getCount() - 1) {
			switch (getFooterMode()) {
			case LOADING:
				return getLoadingFooterView(convertView, parent);
			case ERROR:
				return getErrorFooterView(convertView, parent,
						mRetryClickListener);
			}
		}
		convertView = bindView(position, convertView, parent);
		return convertView;
	}

	@Override
	public void onChanged() {
		if (mListLoader.hasMore()) {
			setFooterMode(FooterMode.LOADING);
		} else {
			if (mListLoader.onClickJudgeMore()) {
				setFooterMode(FooterMode.LOADBTN);
			} else {
				setFooterMode(FooterMode.NONE);
			}
		}
	}

	@Override
	public void onError(Throwable ex) {
		triggerFooterErrorMode();
	}

	public void triggerFooterErrorMode() {
		setFooterMode(FooterMode.ERROR);
	}

	@Override
	public boolean isEnabled(int position) {
		if (position == getCount() - 1) {
			switch (getFooterMode()) {
			case LOADING:
				return false;
			case ERROR:
				return false;
			}
		}
		return true;
	}

	protected abstract View getErrorFooterView(View convertView,
			ViewGroup parent, View.OnClickListener retryOnClickListener);

	protected abstract View getLoadingFooterView(View convertView,
			ViewGroup parent);

	protected abstract View bindView(int position, View convertView,
			ViewGroup parent);

	public FooterMode getFooterMode() {
		return mFooterMode;
	}

	public boolean isFooterModeNone() {
		return mFooterMode == FooterMode.NONE;
	}

	protected static enum FooterMode {
		LOADING, ERROR, NONE, LOADBTN;
	};

	private void setFooterMode(FooterMode footerMode) {
		mFooterMode = footerMode;
		notifyDataSetChanged();
	}

}
