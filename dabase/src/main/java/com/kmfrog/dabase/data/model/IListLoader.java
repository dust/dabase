package com.kmfrog.dabase.data.model;

import java.util.Comparator;
import java.util.List;

import android.net.Uri;

import com.kmfrog.dabase.data.Filter;
import com.kmfrog.dabase.data.Request;

public interface IListLoader {

    void addChangedListener(ChangedListener l);

    int getCount();

    void retryLoadItems();

    void startLoadItems();

    void refresh();

    void onDestory();

    boolean hasMore();

    Object getItem(int index);

    boolean isMoreAvailable();

    void reset();

    String getUriByPosition(int position);

    void setWindowDistance(int i);

    Uri getNextPageUri(Uri currentPageUri);

    int getPageSize(String url);

    @SuppressWarnings("rawtypes")
    Request makeRequest(String uri);

    boolean getAutoLoadNextPage();

    boolean isErrorState();

    @SuppressWarnings("rawtypes")
    void sort(Comparator c);

    boolean hasFilters();

    @SuppressWarnings("rawtypes")
    boolean addFilter(Filter filter);

    @SuppressWarnings("rawtypes")
    boolean removeFilter(Filter filter);

    @SuppressWarnings("rawtypes")
    void filter(List<Filter> filters);
    
    boolean  onClickJudgeMore();
}
