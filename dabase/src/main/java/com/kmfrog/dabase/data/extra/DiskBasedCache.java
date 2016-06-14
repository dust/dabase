package com.kmfrog.dabase.data.extra;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.Cache;

/**
 * 基于磁盘的缓存容器。缓存项为字节数组。
 * @author dust@downjoy.com
 *
 */
public class DiskBasedCache extends Cache {

    private long mTotalSize;

    private final File mRootDirectory;

    private final int mMaxCacheSizeInBytes;

    private final Map<String, CacheHeader> mEntries;

    private static class CacheHeader {

        public String key;

        public long bornMillisTimes;

        public long size;

        public String etag;
        
        public long ttl;

        public long softTtl;
        
        public String charset;

        public void readHeader(InputStream is) throws IOException {

            ObjectInputStream objectinputstream=new ObjectInputStream(is);
            if(objectinputstream.readByte() != 1)
                throw new IOException();
            key=objectinputstream.readUTF();
            bornMillisTimes=objectinputstream.readLong();
            ttl=objectinputstream.readLong();
            softTtl=objectinputstream.readLong();
            etag=objectinputstream.readUTF();
            if("".equals(etag)) {
                etag=null;
            }
            charset=objectinputstream.readUTF();
        }

        private CacheHeader() {

        }

        public CacheHeader(String s, Cache.Entry entry) {
            key=s;
            size=entry.data.length;
            etag=entry.etag;
            bornMillisTimes=entry.bornMillisTimes;
            ttl=entry.ttl;
            softTtl=entry.softTtl;
            charset=entry.charset;
        }

        public Cache.Entry toEntry(byte[] bytes) {
            Cache.Entry entry=new Cache.Entry();
            entry.data=bytes;
            entry.etag=etag;
            entry.bornMillisTimes=bornMillisTimes;
            entry.ttl=ttl;
            entry.softTtl=softTtl;
            entry.charset=charset;
            return entry;
        }

        public boolean writeHeader(OutputStream outputstream) {
            boolean flag=true;
            try {
                ObjectOutputStream objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeByte(1);
                objectoutputstream.writeUTF(key);
                objectoutputstream.writeLong(bornMillisTimes);
                objectoutputstream.writeLong(ttl);
                objectoutputstream.writeLong(softTtl);
                String tmp=(etag == null) ? "" : etag;
                objectoutputstream.writeUTF(tmp);
                tmp=(charset==null)?"":charset;
                objectoutputstream.writeUTF(tmp);
                objectoutputstream.flush();
            } catch(IOException ioexception) {
                if(DLog.DEBUG) {
                    DLog.d(getClass().getSimpleName() + ".writeHeader", ioexception);
                }
                flag=false;
            }
            return flag;
        }
    }

    public DiskBasedCache(File dir, int maxCacheSizeInBytes) {
        mEntries=new LinkedHashMap<String, CacheHeader>(16, 0.75f, true);
        mTotalSize=0L;
        mRootDirectory=dir;
        mMaxCacheSizeInBytes=maxCacheSizeInBytes;
    }

    private File getFileForKey(String file) {
        return new File(mRootDirectory, getFilenameForKey(file));
    }

    private String getFilenameForKey(String s) {
        int pos=s.length() / 2;
        String part0HashCode=String.valueOf(s.substring(0, pos).hashCode());
        final String part1HashCode=String.valueOf(s.substring(pos).hashCode());
        return new StringBuilder().append(part0HashCode).append(part1HashCode).toString();
    }

    private void pruneIfNeeded(long i) {
        if(mTotalSize + i >= mMaxCacheSizeInBytes) {
            if(DLog.DEBUG) {
                DLog.v("Pruning old cache entries.", new Object[0]);
            }
            int j=0;
            long l1=System.currentTimeMillis();
            Iterator<Map.Entry<String, CacheHeader>> iterator=mEntries.entrySet().iterator();
            do {
                if(!iterator.hasNext())
                    break;
                CacheHeader cacheheader=iterator.next().getValue();

                if(getFileForKey(cacheheader.key).delete()) {
                    mTotalSize=mTotalSize - cacheheader.size;
                } else {
                    DLog.d("Could not delete cache entry for key=%s, filename=%s", new Object[]{cacheheader.key,
                        getFilenameForKey(cacheheader.key)});
                }
                iterator.remove();
                j++;
            } while((float)(mTotalSize + (long)i) >= 0.9F * (float)mMaxCacheSizeInBytes);

            if(DLog.DEBUG) {
                DLog.v("pruned %d files, %d bytes, %d ms", new Object[]{j, mTotalSize - l1, System.currentTimeMillis() - l1});
            }
        }
    }

    private void putEntry(String s, CacheHeader cacheheader) {
        if(!mEntries.containsKey(s)) {
            mTotalSize=mTotalSize + cacheheader.size;
        } else {
            CacheHeader cacheheader1=(CacheHeader)mEntries.get(s);
            mTotalSize=mTotalSize + (cacheheader.size - cacheheader1.size);
        }
        mEntries.put(s, cacheheader);
    }

    private void removeEntry(String s) {
        CacheHeader cacheheader=(CacheHeader)mEntries.get(s);
        if(cacheheader != null) {
            mTotalSize=mTotalSize - cacheheader.size;
            mEntries.remove(s);
        }
    }

    private static byte[] streamToBytes(InputStream inputstream) throws IOException {
        ByteArrayOutputStream bytearrayoutputstream=new ByteArrayOutputStream();
        byte[] bytes=new byte[1024];
        do {
            int i=inputstream.read(bytes);
            if(i != -1) {
                bytearrayoutputstream.write(bytes, 0, i);
            } else {
                byte abyte1[]=bytearrayoutputstream.toByteArray();
                bytearrayoutputstream.close();
                return abyte1;
            }
        } while(true);
    }

    @Override
    public void clearFiles() {
        synchronized(this) {
            File[] files=mRootDirectory.listFiles();
            if(files != null) {
                for(int i=0; i < files.length; i++) {
                    files[i].delete();
                }
            }
            mEntries.clear();
            mTotalSize=0L;
            if(DLog.DEBUG) {
                DLog.d("Cache cleared");
            }
        }
    }

    @Override
    public Cache.Entry get(String key) {
        Cache.Entry entry=null;
        synchronized(this) {
            CacheHeader cacheHeader=mEntries.get(key);
            if(cacheHeader != null) {
                File file=getFileForKey(key);
                try {
                    FileInputStream fis=new FileInputStream(file);
                    cacheHeader.readHeader(fis);
                    entry=cacheHeader.toEntry(streamToBytes(fis));
                    if(fis != null) {
                        fis.close();
                    }
                    if(cacheHeader.size<=0L){
                        cacheHeader.size=entry.data.length;
                    }
                } catch(IOException ex) {
                    if(DLog.DEBUG) {
                        DLog.d("DiskBasedCache.get %s: %s", file.getAbsoluteFile(), ex.toString());
                    }
                    remove(key);
                } catch(Throwable ex) {
                    if(DLog.DEBUG) {
                        DLog.e("DiskBasedCache.get", ex);
                    }
                }
            }
        }
        return entry;
    }

    public void remove(String s1) {
        boolean deleted;
        synchronized(this) {
            deleted=getFileForKey(s1).delete();
            removeEntry(s1);
            if(!deleted) {
                if(DLog.DEBUG) {
                    DLog.e("Could not delete cache entry for key=%s, filename=%s", s1, getFilenameForKey(s1));
                }
            }
        }
    }

    @Override
    public void initialize() {
        synchronized(this) {
            try {
                File[] files=mRootDirectory.listFiles();
                if(files != null) {
                    int len=files.length;
                    if(len > 0) {
                        for(File file: files) {
                            FileInputStream fis=new FileInputStream(file);
                            try {
                                CacheHeader cacheHeader=new CacheHeader();
                                cacheHeader.readHeader(fis);
                                putEntry(cacheHeader.key, cacheHeader);
                                if(fis != null) {
                                    fis.close();
                                }
                            } catch(IOException ex) {
                                if(DLog.DEBUG) {
                                    DLog.e("DiskBasedCache.initialize", ex);
                                }
                                file.delete();
                            }
                        }
                    }
                }
            } catch(Exception ex) {
                if(DLog.DEBUG) {
                    DLog.e("DiskBasedCache", ex);
                }
            }
        }
    }

    @Override
    public void invalidate(String s1, boolean abandon) {
        Cache.Entry entry=null;
        synchronized(this) {
            entry=get(s1);
            if(entry != null) {
                entry.softTtl=0;
                if(abandon) {
                    entry.ttl=0;
                }
                entry.bornMillisTimes=0;
            }
            put(s1, entry);
        }
    }

    @Override
    public void put(String key, Cache.Entry entry) {
        synchronized(this) {
            try {
                pruneIfNeeded(entry.data.length);
                File file=getFileForKey(key);
                try {
                    FileOutputStream fos=new FileOutputStream(file);
                    CacheHeader cacheHeader=new CacheHeader(key, entry);
                    cacheHeader.writeHeader(fos);
                    fos.write(entry.toBytes());
                    fos.close();
                    putEntry(key, cacheHeader);
                } catch(Exception ex) {
                    file.delete();
                    if(DLog.DEBUG) {
                        DLog.e("Cloud not clean up file %s", file.getAbsolutePath());
                    }
                }
            } catch(Exception ex) {
            }
        }
    }

}
