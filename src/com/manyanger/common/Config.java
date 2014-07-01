/**
 * 
 */
package com.manyanger.common;

import android.content.res.Configuration;

import com.manyanger.AppInfo;



public class Config
{

    public static boolean checkOrientationChanged(int oldOrientation)
    {
        final int orientation = getOrientation();
        if (oldOrientation != orientation)
        {
            return true;
        }
        return false;
    }


    public static int getOrientation()
    {
        return AppInfo.getContext().getResources().getConfiguration().orientation;
    }


    public static boolean isPortal()
    {
        return getOrientation() == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isLandscape()
    {
        return getOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }
    

    public static int getHeight()
    {
        return AppInfo.getContext().getResources().getDisplayMetrics().heightPixels;
    }


    public static int getWidth()
    {
        return AppInfo.getContext().getResources().getDisplayMetrics().widthPixels;
    }

}
