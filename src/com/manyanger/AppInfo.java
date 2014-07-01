package com.manyanger;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * @ClassName: AppInfo
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-7 下午1:37:00
 */
public class AppInfo {
    
    public final static String PRE_PATH = "/otooman";
    
    public static boolean disAllowIntercept = false;
    
    public static Context context;
    
    public static long uiTid;
    
    public static void initAppInfo(Application application) {
        
        Log.i("AppInfo", "application:"+application.toString());
        context = application;
        uiTid = Thread.currentThread().getId();
    }

    /**
     * @return the context
     */
    public static Context getContext() {
        if(context == null){
            Log.e("AppInfo", "context is null!");
            return null;
        }
        return context;
    }



}
