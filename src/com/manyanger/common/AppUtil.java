package com.manyanger.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.StatFs;

import com.manyanger.AppInfo;
import com.manyounger.otooman.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @ClassName: AppUtil
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-7 下午2:41:48
 */
public final class AppUtil {
    

    private static Activity currentActivity;
    
    public static boolean isCurrentThreadUiThread()
    {
        return Thread.currentThread().getId() == AppInfo.uiTid;
    }


    public static File getPreferredDataDir()
    {
        if (Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED))
        {
            File sdCardDir = Environment.getExternalStorageDirectory();
            return sdCardDir;
        }
        return null;
    }
    
    public static boolean checkPhoneHavEnghStorage(long size)
    {
        return AppUtil.checkStorageRom(Environment.getDataDirectory(), size);
    }

    public static boolean checkSdcardHavEnghStorage(long size)
    {
        return AppUtil.checkStorageRom(getPreferredDataDir(), size);
    }

    static synchronized boolean checkStorageRom(File file, long size)
    {
        if (file == null)
        {
            return false;
        }

        StatFs mStat = new StatFs(file.getAbsolutePath());
        long blockSize = mStat.getBlockSize();
        long avaleCout = mStat.getAvailableBlocks();
        long val = avaleCout * blockSize;
        if (size + 10 * 1024 * 1024 <= val)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * close InputStream
     * @param ins
     */
    public static void closeInputStream(InputStream ins)
    {
        if (ins != null)
        {
            try
            {
                ins.close();
                ins = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * close OutputStream
     * @param out
     */
    public static void closeOutputStream(OutputStream out)
    {
        if (out != null)
        {
            try
            {
                out.close();
                out = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    /**
     * @return the currentActivity
     */
    public static Activity getCurrentActivity() {
        return currentActivity;
    }


    /**
     * @param currentActivity the currentActivity to set
     */
    public static void setCurrentActivity(Activity currentActivity) {
        AppUtil.currentActivity = currentActivity;
    }
    
    private static BitmapDrawable mDefaultIconBitmap = null;
    public static BitmapDrawable getDefaultIconBitmap()
    {
        if (mDefaultIconBitmap == null
            || mDefaultIconBitmap.getBitmap().isRecycled())
        {
            mDefaultIconBitmap =
                new BitmapDrawable(BitmapFactory.decodeResource(AppInfo
                    .getContext().getResources(), R.drawable.default_big_icon));
        }
        return mDefaultIconBitmap;
    }
    
    public static Bitmap getDefaultBigIconBitmap()
    {
        return BitmapFactory.decodeResource(AppInfo.getContext()
            .getResources(), R.drawable.default_big_icon);
    }
    
    public static Bitmap getDefaultBannerBitmap()
    {
        return BitmapFactory.decodeResource(AppInfo.getContext()
            .getResources(), R.drawable.default_banner);
    }

    public static String getString(int resId){
        return AppInfo.getContext().getString(resId);
    }
    
    public static byte[] inputStreamToByte(InputStream in)
    {
        final int BUFFER_SIZE = 2046;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        try
        {
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            {
                outStream.write(data, 0, count);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            data = null;
            try
            {
                in.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return outStream.toByteArray();
    }
}
