package com.manyanger.common;

import android.text.TextUtils;
import android.widget.Toast;

import com.manyanger.AppInfo;


public class ToastUtil
{

    private static Toast mToast;

    public static void showToast(String title)
    {
        showToast(title, Toast.LENGTH_SHORT);
    }

    public static void showToastShort(String title)
    {
        showToast(title, Toast.LENGTH_SHORT);
    }

    public static void showToast(final String title, int duration)
    {
        if (TextUtils.isEmpty(title))
        {
            return;
        }
        if (AppUtil.isCurrentThreadUiThread())
        {
            show(title);
        }
       
    }

    /**
     * Method: show
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-7-31
     * @param title 
     */
    private static void show(final String title)
    {
        if (mToast == null)
        {
            mToast =
                Toast.makeText(AppInfo.getContext(), title,
                    Toast.LENGTH_SHORT);
        }
        mToast.setText(title);
        mToast.show();
    }

    private ToastUtil()
    {

    }

    /**
     * Method: showToast
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-3-16
     * @param noData 
     */
    public static void showToast(int resId)
    {
        showToast(AppInfo.getContext().getResources().getString(resId));
    }

    public static void cancel()
    {
        if (mToast != null)
        {
            //            mToast.cancel();
            mToast = null;
        }
    }
}
