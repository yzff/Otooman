package com.manyanger.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.manyanger.GlobalData;
import com.manyanger.common.ThreadPool;
import com.manyanger.data.loader.ComicItemsLoader;
import com.manyanger.data.net.DataException;
import com.manyanger.entries.BaseThemeItem;

import java.util.List;


public class ComicThemeDataLoader {
    private Handler mHandler;

    //列表加载完成的监听接口
    private OnDataLoadedListener mListener;
    
    public ComicThemeDataLoader(OnDataLoadedListener listener)
    {
    	registerOnDataLoadedListener(listener);
        mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (mListener != null)
                {
                    //应用列表加载完成，回调
                    mListener.OnDataLoaded(msg);
                }
            }
        };
    }
    
    public void loadThemeList(){
    	ThreadPool.submitText(new LoadDataRunnable(GlobalData.CMD_THEMELIST));
    }

    private class LoadDataRunnable implements Runnable
    {
        String cmdLine;

        public LoadDataRunnable(String cmd)
        {
            this.cmdLine = cmd;

        }

        @Override
        public void run()
        {
            
            String resultStr = null;
            List<BaseThemeItem> themeList = null;

            try{
                resultStr = ComicItemsLoader.load(cmdLine);
            } catch(DataException e){
//                listModel.setSuccess(false);
                resultStr = null;
            } catch(Exception e){
//                listModel.setSuccess(false);
                resultStr = null;
            }
            
            if(resultStr == null){

            } else {
                themeList = JsonParser.parseThemeList(resultStr);
            }
            
            if (mHandler == null)
            {
                return;
            }
            //准备回调
            Message message = mHandler.obtainMessage();
            message.what = GlobalData.NOTIFY_COMICTHEME_LOADED;
//            message.arg1 = listId;
            message.obj = themeList;
            mHandler.sendMessage(message);
        }

    }
	
    // 注册监听图片加载完成的接口
    public void registerOnDataLoadedListener(OnDataLoadedListener listener)
    {
        mListener = listener;
    }

    // 注销监听 图片加载完成
    public void unregisteOnDataLoadedListener(
    		OnDataLoadedListener listener)
    {
        mListener = null;
        if (mHandler != null)
        {
            mHandler = null;
        }
    }
    

}
