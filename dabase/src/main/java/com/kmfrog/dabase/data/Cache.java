package com.kmfrog.dabase.data;

/**
 * 缓存容器的封装
 * @author dust@downjoy.com
 */
public abstract class Cache {

    public abstract void clearFiles();

    public abstract Entry get(String key);

    public abstract void initialize();

    public abstract void invalidate(String key, boolean abandon);

    public abstract void put(String key, Entry entry);

    public abstract void remove(String key);

    /**
     * 默认基于http的缓存项实现
     * @author dust@downjoy.com
     */
    public static class Entry {

        /** 数据 **/
        public byte[] data;

        /** 数据产生的时间 */
        public long bornMillisTimes;

        public String charset;

        public String etag;

        public String dataMeta;

        public long softTtl;

        public long ttl;

        /**
         * 检查是否过期
         * @param ignoreExpiredTime 是否忽略过期时间。TODO:显得多余，业务使然 ＠_@
         * @return
         */

        boolean isExpired(boolean ignoreExpiredTime) {
            return !ignoreExpiredTime && ttl < System.currentTimeMillis();
        }

        /**
         * 是否需要刷新，用于预缓存或提前更新缓存
         * @param hasConnectedNetwork TODO
         * @return
         */
        boolean refreshNeeded(boolean hasConnectedNetwork) {
            return hasConnectedNetwork && softTtl < System.currentTimeMillis();
        }

        /**
         * 实际缓存的核心数据。
         * @return
         */
        public byte[] toBytes() {
            return data;
        }

        public Entry() {

        }
    }
}
