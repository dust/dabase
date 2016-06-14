package com.kmfrog.dabase.data;


public interface Filter<D> {
    
    boolean shouldDiscard(D d);

}
