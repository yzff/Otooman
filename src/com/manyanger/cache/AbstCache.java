package com.manyanger.cache;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.common.collect.MapMaker;
import com.manyanger.AppInfo;
import com.manyanger.common.AppUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public abstract class AbstCache<Key, Val>
{

    private static final String TAG = "[AbstCache]";

    protected String destCacheDir;

    /*
     * cache time (Minutes)
     */
    private long cacheSaveInMinutes;

    private boolean isDiskCacheEnabled;

    protected ConcurrentMap<Key, Val> cache;

    /**
     * cache in user phone
     */
    public static final int DISK_CACHE_UPHONE = 0;

    /**
     * cache in user sdcard
     */
    public static final int DISK_CACHE_SDCARD = 1;

    protected int whichDiskSave = DISK_CACHE_SDCARD;

    public int getWhichDiskSave()
    {
        return whichDiskSave;
    }

    public AbstCache(int threadPoolSize, ReferenceType refType)
    {
        this(25, 7 * 24 * 60, 30, threadPoolSize, refType);
    }

    public static enum ReferenceType
    {
        STRONG, WEAK, SOFT
    }

    /**
     * @param initialCapacity
     *            the HashMap initial capacity
     * @param cacheSaveInMinutes
     *            cache effictive time(in minutes)
     */
    public AbstCache(int initialCapacity, long cacheSaveInMinutes,
        long cacheSaveInMemory, int threadPoolSize, ReferenceType refType)
    {
        this.cacheSaveInMinutes = cacheSaveInMinutes;
        MapMaker mapMaker = new MapMaker();
        mapMaker.initialCapacity(initialCapacity);
        mapMaker.concurrencyLevel(threadPoolSize > 0 ? threadPoolSize : 3);
        if (refType != null)
        {
            if (refType.equals(ReferenceType.SOFT))
            {
                mapMaker.softValues();
                mapMaker.expiration(cacheSaveInMemory, TimeUnit.SECONDS);
            }
            else if (refType.equals(ReferenceType.WEAK))
            {
                mapMaker.weakValues();
                mapMaker.expiration(cacheSaveInMemory, TimeUnit.SECONDS);
            }
        }
        this.cache = mapMaker.makeMap();
    }

    /**
     * Enable caching to the phone's internal storage or SD card.
     * @param context
     * @param storageDevice
     * @param destDir
     *        both of the start and the end won't '/'
     * @return
     */
    public boolean isDiskCacheEnable(Context context, int storageDevice,
        String destDir)
    {
        return isDiskCacheEnable(context, storageDevice, destDir, false);
    }

    /**
     * Enable caching to the phone's internal storage or SD card.
     * @param context
     * @param storageDevice
     * @param destDir
     *        both of the start and the end won't '/'
     * @param isSanitizeDisk
     * 
     * @return
     */
    public boolean isDiskCacheEnable(Context context, int storageDevice,
        String destDir, boolean isSanitizeDisk)
    {
        whichDiskSave = storageDevice;
        StringBuffer destBuf = null;
        if (storageDevice == DISK_CACHE_SDCARD
            && AppUtil.getPreferredDataDir() != null)
        {
            // sdcard available
            destBuf =
                new StringBuffer(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            destBuf.append(AppInfo.PRE_PATH);
            destBuf.append("/cache");
        }
        else
        {
            File internalCacheDir = context.getCacheDir();
            if (internalCacheDir == null)
            {
                return isDiskCacheEnabled = false;
            }
            destBuf = new StringBuffer(internalCacheDir.getAbsolutePath());
            destBuf.append("/cache");
        }
        destBuf.append(File.separator);
        if (destDir != null && destDir.trim().length() > 0)
        {
            destBuf.append(destDir);
            destBuf.append(File.separator);
        }

        this.destCacheDir = destBuf.toString();
        File outFile = new File(destCacheDir);
        if (!(isDiskCacheEnabled = outFile.exists()))
        {
            isDiskCacheEnabled = outFile.mkdirs();
        }

        if (!isDiskCacheEnabled)
        {
            Log.e(TAG, "Failed creating disk cache directory "
                    + destCacheDir);
        }
        else
        {
            // sanitize disk cache
            if (isSanitizeDisk)
            {
                sanitizeDiskCache();
            }
        }

        return isDiskCacheEnabled;
    }

    /*
     * sanitize disk cache that is overdue
     */
    public void sanitizeDiskCache()
    {
        File[] cachedFiles = new File(destCacheDir).listFiles();
        if (cachedFiles == null)
        {
            return;
        }
        for (File f : cachedFiles)
        {
            long lastModified = f.lastModified();
            Date now = new Date();
            long ageInMinutes = (now.getTime() - lastModified) / (1000 * 60);

            if (ageInMinutes >= cacheSaveInMinutes)
            {
                f.delete();
            }
        }
    }

    public String getDiskCacheDirectory()
    {
        return destCacheDir;
    }

    protected abstract String getFileNameForKey(Key key);

    protected abstract Val readValueFromDisk(File file) throws IOException;

    protected byte[] readByteFromDisk(File file) throws IOException
    {
        if (file == null || file.length() == 0)
        {
            return null;
        }
        BufferedInputStream istream =
            new BufferedInputStream(new FileInputStream(file));
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE)
        {
            throw new IOException("Cannot read files larger than "
                + Integer.MAX_VALUE + " bytes");
        }

        int imageDataLength = (int) fileSize;

        byte[] imageData = new byte[imageDataLength];
        istream.read(imageData, 0, imageDataLength);
        istream.close();
        return imageData;
    }

    protected abstract void writeValueToDisk(File file, Val value)
        throws IOException;

    protected void writeByteToDisk(File file, byte[] data, int offset,
        int length)
    {
        if (data == null || data.length == 0)
        {
            return;
        }
        /*
         * check cache Disk is have enough storage
         */
        switch (whichDiskSave)
        {
            case DISK_CACHE_SDCARD:
                if (!AppUtil.checkSdcardHavEnghStorage(data.length))
                {
                    return;
                }
                break;
            case DISK_CACHE_UPHONE:
                if (!AppUtil.checkPhoneHavEnghStorage(data.length))
                {
                    return;
                }
                break;
            default:
                return;
        }
        BufferedOutputStream ostream = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            ostream = new BufferedOutputStream(fos);
            ostream.write(data, offset, length);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            AppUtil.closeOutputStream(ostream);
        }
    }

    private void cacheToDisk(Key key, Val value, byte[] data, int offset,
        int length)
    {
        File file = getFileForKey(key);
        if (file.exists())
        {
            return;
        }
        try
        {
            if (file.createNewFile())
            {
                if (data != null && data.length > 0)
                {
                    writeByteToDisk(file, data, offset, length);
                }
                else
                {
                    writeValueToDisk(file, value);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public File getFileForKey(Key key)
    {
        File outFile = new File(destCacheDir);
        if (!(isDiskCacheEnabled = outFile.exists()))
        {
            isDiskCacheEnabled = outFile.mkdirs();
        }
        return new File(outFile, getFileNameForKey(key));
    }

    /**
     * read from cache or disk
     * @param elementKey
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized Val get(Object elementKey)
    {
        if(elementKey == null){
            return null;
        }
        Key key = (Key) elementKey;
        Val value = cache.get(key);
        if (value != null)
        {
            return value;
        }

        File file = getFileForKey(key);
        if (file.exists())
        {
            try
            {
                value = readValueFromDisk(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
            if (value == null)
            {
                return null;
            }
            cache.put(key, value);
            return value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean isCacheEffictive(Object elementKey, long time)
    {
        Key key = (Key) elementKey;
        File file = getFileForKey(key);
        if (file.exists())
        {
            long modifyTime = file.lastModified();
            long nowTime = System.currentTimeMillis();
            if (nowTime - modifyTime < time)
            {
                return true;
            }
        }
        return false;
    }

    public Val getFromInMemery(Object elementKey)
    {
        @SuppressWarnings("unchecked")
        Key key = (Key) elementKey;
        if(key == null){
            return null;
        }
        Val value = cache.get(key);
        return value;
    }

    public synchronized Val getFromDisk(Object elementKey)
    {
        @SuppressWarnings("unchecked")
        Key key = (Key) elementKey;
        if(key == null){
            return null;
        }
        File file = getFileForKey(key);
        Val value = null;
        if (file.exists())
        {
            try
            {
                value = readValueFromDisk(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
            if (value != null)
            {
                cache.put(key, value);
            }
        }
        return value;
    }

    /**
     * put value into ram and into disk,in sub must Override writeValueToDisk
     * method
     * @param key
     * @param value
     * @return
     */
    public synchronized Val put(Key key, Val value)
    {
        return put(key, value, null);
    }

    public Val putInMemery(Key key, Val value)
    {
        return cache.put(key, value);
    }

    /**
     * put value into ram and into disk,in sub class
     * not need Override writeValueToDisk method
     * @param key
     * @param value
     * @return
     */
    public synchronized Val put(Key key, Val value, byte[] data)
    {
        if (isDiskCacheEnabled)
        {
            cacheToDisk(key, value, data, 0, data == null ? 0 : data.length);
        }
        return cache.put(key, value);
    }

    /**
     * Method: put
     * <p>Author: Nick.Zhang
     * <p>Description: 
     * <p>Modified: 2012-7-25 
     */
    public synchronized Val put(Key key, Val value, byte[] data, int offset,
        int length)
    {
        if (isDiskCacheEnabled)
        {
            cacheToDisk(key, value, data, offset, length);
        }
        Val v = null;
        if (null != value)
        {
            v = cache.put(key, value);
        }
        return v;
    }

    /**
     * Checks if a key is present in the in-memory cache or in-disk
     * @param key
     *            the cache key
     * @return true if the value is currently hold in memory or in-disk false
     *         otherwise
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean containsKey(Object key)
    {
        return cache.containsKey(key) || isDiskCacheEnabled
            && getFileForKey((Key) key).exists();
    }

    /**
     * Checks if a value is present in the in-memory cache. ignores the disk
     * cache
     * @param key
     *            the cache key
     * @return true if the value is currently hold in memory false otherwise
     */
    public synchronized boolean containsKeyInMemory(Object key)
    {
        return cache.containsKey(key);
    }

    /**
     * Checks if the given value is currently hold in memory.
     */
    public synchronized boolean containsValue(Object value)
    {
        return cache.containsValue(value);
    }

    public synchronized Val remove(Object key)
    {
        return cache.remove(key);
    }

    public synchronized boolean remove(Object key, Object value)
    {
        return cache.remove(key, value);
    }

    @SuppressWarnings("unchecked")
    public synchronized Val delete(Object key)
    {
        if (isDiskCacheEnabled)
        {
            File file = getFileForKey((Key) key);
            if (file.exists())
            {
                file.delete();
            }
        }
        return cache.remove(key);
    }

    public synchronized Val update(Key key, Val value, byte[] data)
    {
        if (isDiskCacheEnabled)
        {
            delete(key);
            cacheToDisk(key, value, data, 0, data == null ? 0 : data.length);
        }
        return cache.put(key, value);
    }

    public Set<Key> keySet()
    {
        return cache.keySet();
    }

    public Set<Map.Entry<Key, Val>> entrySet()
    {
        return cache.entrySet();
    }

    public synchronized int size()
    {
        return cache.size();
    }

    public synchronized boolean isEmpty()
    {
        return cache.isEmpty();
    }

    public boolean isDiskCacheEnabled()
    {
        return isDiskCacheEnabled;
    }

    public synchronized void clear()
    {
        cache.clear();
    }

    public Collection<Val> values()
    {
        return cache.values();
    }

}
