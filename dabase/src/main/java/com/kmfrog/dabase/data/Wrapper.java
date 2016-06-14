package com.kmfrog.dabase.data;

public interface Wrapper<D> {

    D getWrapperIn();

    boolean isWrapperErrState();

    Throwable getWrapperErr();

}
