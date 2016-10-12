package com.kmfrog.dabase.data.model;

class UrlOffsetPair {

    public final int offset;

    public final String uri;

    public UrlOffsetPair(int offset, String uri) {
        this.offset=offset;
        this.uri=uri;
    }

    public String toString() {
        return String.format("%s\t%d", uri, offset);
    }
}
