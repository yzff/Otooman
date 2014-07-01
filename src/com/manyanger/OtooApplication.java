package com.manyanger;

import android.app.Application;
import android.content.Context;

/**
 * @ClassName: OtooApplication
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-7 下午1:34:22
 */
public class OtooApplication extends Application {

    private Context context;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        getResources().getDisplayMetrics().scaledDensity =
                getResources().getDisplayMetrics().scaledDensity
                        / getResources().getConfiguration().fontScale;
        
        context = this;
        AppInfo.initAppInfo(this);
        GlobalData.init();
    }
    
    
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {

        getResources().getDisplayMetrics().scaledDensity =
                getResources().getDisplayMetrics().scaledDensity
                        / newConfig.fontScale;

        super.onConfigurationChanged(newConfig);
    }


}
