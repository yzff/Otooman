/**
 * 
 */
package com.manyanger.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.manyanger.AppInfo;
import com.manyanger.common.AppUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;


/**
 * ImageCache
 * <p>
 * Nick.Zhang
 * <p>
 * 2011-10-21
 */
public final class ImageCache extends AbstCache<String, Bitmap>
{
    private static ImageCache imageCache;

    private static final String CACHE_DEST_NAME = "image";
    
    private static final String CACHE_PICTURE_NAME = "picture";

    private ImageCache(int threadPoolSize)
    {
        super(threadPoolSize, ReferenceType.SOFT);
    }

    private ImageCache(int initialCapacity, long cacheSaveInMinutes,
        long cacheSaveInMemory, int threadPoolSize, ReferenceType refType)
    {
        super(initialCapacity, cacheSaveInMinutes, cacheSaveInMemory,
            threadPoolSize, refType);

    }

    public static ImageCache buildTempImageCache()
    {
        ImageCache tempCache =
            new ImageCache(10, 7 * 24 * 60, -1, 1, ReferenceType.STRONG);
        if (AppUtil.getPreferredDataDir() != null)
        {
            tempCache.isDiskCacheEnable(AppInfo.getContext(),
                DISK_CACHE_SDCARD, CACHE_DEST_NAME);
        }
        else
        {
            tempCache.isDiskCacheEnable(AppInfo.getContext(),
                DISK_CACHE_UPHONE, CACHE_DEST_NAME);
        }
        return tempCache;
    }

    public static synchronized ImageCache getInstance()
    {
        final int threadPoolSize = 1;
        if (imageCache == null)
        {
            imageCache = new ImageCache(threadPoolSize);
            if (AppUtil.getPreferredDataDir() != null)
            {
                imageCache.isDiskCacheEnable(AppInfo.getContext(),
                    DISK_CACHE_SDCARD, CACHE_DEST_NAME);
            }
            else
            {
                imageCache.isDiskCacheEnable(AppInfo.getContext(),
                    DISK_CACHE_UPHONE, CACHE_DEST_NAME);
            }
        }
        return imageCache;
    }

    public static void setDiskEnable(Context context, boolean isSdcard)
    {
        if (imageCache == null)
        {
            return;
        }
        if (isSdcard)
        {
            imageCache.isDiskCacheEnable(context, DISK_CACHE_SDCARD,
                CACHE_DEST_NAME, false);
        }
        else
        {
            imageCache.isDiskCacheEnable(context, DISK_CACHE_UPHONE,
                CACHE_DEST_NAME, false);
        }
    }

    public void setDiskEnable(ImageCache cache, Context context,
        boolean isSdcard)
    {
        if (cache == null)
        {
            return;
        }
        if (isSdcard)
        {
            cache.isDiskCacheEnable(context, DISK_CACHE_SDCARD,
                CACHE_DEST_NAME, false);
        }
        else
        {
            cache.isDiskCacheEnable(context, DISK_CACHE_UPHONE,
                CACHE_DEST_NAME, false);
        }
    }

    @Override
    protected void writeValueToDisk(File file, Bitmap bitmap)
    {
        if (bitmap != null && file != null)
        {
            BufferedOutputStream ostream = null;
            try
            {
                FileOutputStream fos = new FileOutputStream(file);
                ostream = new BufferedOutputStream(fos);
                if (!bitmap.compress(CompressFormat.PNG, 100, ostream))
                {
                    bitmap.compress(CompressFormat.JPEG, 100, ostream);
                }
                ostream.flush();
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
    }

    /**
     * @param key
     * @return
     * @see com.skymobi.appstore.cache.AbstCache#getFileNameForKey(java.lang.Object)
     */
    @Override
    protected String getFileNameForKey(String key)
    {
        //return Constants.getFileNameFromUrl(key);
        return key;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     * @see com.skymobi.appstore.cache.AbstCache#readValueFromDisk(java.io.File)
     */
    @Override
    protected Bitmap readValueFromDisk(File file) throws IOException
    {
        FileInputStream ins = null;
        Bitmap temp = null;
        try
        {
            ins = new FileInputStream(file);
            temp = BitmapFactory.decodeStream(ins, null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            temp = null;
        }
        finally
        {
            AppUtil.closeInputStream(ins);
        }
        return temp;
    }

    /**
     * Method: clear
     * <p>Author: Nick.Zhang
     * <p>Description: 
     * <p>Modified: 2012-2-28 
     */
    @Override
    public synchronized void clear()
    {
        for (Map.Entry<String, Bitmap> entry : entrySet())
        {
            Bitmap map = entry.getValue();
            if (map != null && !map.isRecycled())
            {
                map.recycle();
                map = null;
            }
        }
        super.clear();
    }
}
