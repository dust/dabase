package com.kmfrog.dabase.bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCache<K, V> {

    public LruCache(int i) {
        if(i <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else {
            maxSize=i;
            map=new LinkedHashMap<K, V>(0, 0.75F, true);
            return;
        }
    }

    private int safeSizeOf(K obj, V obj1) {
        int i=sizeOf(obj, obj1);
        if(i < 0)
            throw new IllegalStateException((new StringBuilder()).append("Negative size: ").append(obj).append("=").append(obj1)
                .toString());
        else
            return i;
    }

    private void trimToSize(int i) {
        while(size > i && !map.isEmpty()) {
            Map.Entry<K, V> entry=map.entrySet().iterator().next();
            if(entry != null) {
                K key=entry.getKey();
                V value=entry.getValue();
                map.remove(key);
                size-=safeSizeOf(key, value);
                evictionCount+=1;
                entryEvicted(key, value);
            }
            if(size < 0 || map.isEmpty() && size != 0)
                throw new IllegalStateException((new StringBuilder()).append(getClass().getName())
                    .append(".sizeOf() is reporting inconsistent results! s:").append(size).append(" \t").append(map.isEmpty()).toString());
            else
                return;
        }
    }

    protected V create(K obj) {
        return null;
    }

    protected void entryEvicted(K obj, V obj1) {
    }

    public final V get(K obj) {
        if(size == 0) {
            return null;
        }
        synchronized(this) {
            if(obj == null) {
                throw new NullPointerException("key==null");
            }
            V value=map.get(obj);
            if(value == null) {
                missCount+=1;
                V createdValue=create(obj);
                if(createdValue != null) {
                    createCount+=1;
                    size+=safeSizeOf(obj, createdValue);
                    map.put(obj, createdValue);
                    trimToSize(maxSize);
                }
                return createdValue;
            } else {
                hitCount+=1;
                return value;
            }
        }
    }

    public final Object put(K key, V value) {
        if(key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        synchronized(this) {

            V obj2;
            putCount=1 + putCount;
            size=size + safeSizeOf(key, value);
            obj2=map.put(key, value);
            if(obj2 != null) {
                size=size - safeSizeOf(key, obj2);
            }
            trimToSize(maxSize);

            return obj2;
        }
    }

    public final Object remove(K key) {
        if(key != null) {
            synchronized(this) {
                V value=map.remove(key);
                if(value!=null){
                    size-=safeSizeOf(key, value);
                }
            }
        }
        return null;
    }

    protected int sizeOf(K obj, V obj1) {
        return 1;
    }

    public final String toString() {
        int i=0;
        synchronized(this) {
            String s;
            int j=hitCount + missCount;
            if(j != 0) {
                i=(100 * hitCount) / j;
            }
            s=String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", maxSize, hitCount, missCount, i);
            return s;
        }

    }

    public void clear() {
        size=0;
        map.clear();
    }

    private int createCount;

    private int evictionCount;

    private int hitCount;

    protected final LinkedHashMap<K, V> map;

    private int maxSize;

    private int missCount;

    private int putCount;

    protected int size;
}
