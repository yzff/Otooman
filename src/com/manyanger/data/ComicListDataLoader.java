package com.manyanger.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.manyanger.GlobalData;
import com.manyanger.common.ThreadPool;
import com.manyanger.data.loader.ComicItemsLoader;
import com.manyanger.data.net.DataException;
import com.manyanger.entries.FirstPageModel;
import com.manyanger.entries.ListModel;

/**
 * @ClassName: ComicListLoader
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-14 下午4:56:49
 */
public class ComicListDataLoader {
    private Handler mHandler;

    //列表加载完成的监听接口
    private OnDataLoadedListener mListener;
    
    public ComicListDataLoader(OnDataLoadedListener listener)
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
    
    public String loadIndexList(int type, int page){
        
        String cmdLine = DataUtils.makeCmdString(GlobalData.CMD_INDEXLIST, type, -1, page, -1, null);
        ThreadPool.submitText(new LoadListRunnable(cmdLine));
        return cmdLine;
    }
    
    public String loadAllComicList(int page){
        
        String cmdLine = DataUtils.makeCmdString(GlobalData.CMD_COMICLIST, -1, -1, page, -1, null);
        ThreadPool.submitText(new LoadListRunnable(cmdLine));
        return cmdLine;
    }
    
    public String loadThemeComicList(int theme, int page){
        String cmdLine = DataUtils.makeCmdString(GlobalData.CMD_THEMECONTENT, -1, -1, page, theme, null);
        ThreadPool.submitText(new LoadListRunnable(cmdLine));
        return cmdLine;
    }
    
    public String search(String keyword, int page){
        
        String cmdLine = DataUtils.makeCmdString(GlobalData.CMD_SEARCH, -1, -1, page, -1, keyword);
        ThreadPool.submitText(new LoadListRunnable(cmdLine));
        return cmdLine;
    }
    
    public String loadListByUrl(String url){

        ThreadPool.submitText(new LoadListRunnable(url));
        return url;
    }
    
    private class LoadListRunnable implements Runnable
    {
        String cmdLine;

        public LoadListRunnable(String cmd)
        {
            this.cmdLine = cmd;

        }

        @Override
        public void run()
        {
            
            String resultStr = null;
            ListModel listModel = null;

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
                listModel = new ListModel();
                listModel.setSuccess(false);
            } else {
                listModel = JsonParser.parseComicList(resultStr);
            }
            listModel.setKeyWord(cmdLine);
            
            if (mHandler == null)
            {
                return;
            }
            //准备回调
            Message message = mHandler.obtainMessage();
            message.what = 1; //Notify.NOTIFY_APPLIST_LOADED;
//            message.arg1 = listId;
            message.obj = listModel;
            mHandler.sendMessage(message);
        }

    }
    
    public String loadFirstList(){
        
        String cmdLine = GlobalData.CMD_FIRSTPAGE;
        ThreadPool.submitText(new LoadFirstRunnable(cmdLine));
        return cmdLine;
    }
    
    private class LoadFirstRunnable implements Runnable
    {
        String cmdLine;

        public LoadFirstRunnable(String cmd)
        {
            this.cmdLine = cmd;
        }

        @Override
        public void run()
        {
            
            String resultStr = null;
            FirstPageModel listModel = null;

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
                listModel = new FirstPageModel();
                listModel.setSuccess(false);
            } else {
                listModel = JsonParser.parseFirstPageList(resultStr);
            }
            listModel.setKeyWord(cmdLine);
            
            if (mHandler == null)
            {
                return;
            }
            //准备回调
            Message message = mHandler.obtainMessage();
            message.what = GlobalData.NOTIFY_COMICLIST_LOADED;
//            message.arg1 = listId;
            message.obj = listModel;
            mHandler.sendMessage(message);
        }

    }

    // 注册监听图片加载完成的接口
    public void registerOnDataLoadedListener(OnDataLoadedListener listener)
    {
        mListener = listener;
    }

    // 注销监听 图片加载完成
    public void unregisterOnDataLoadedListener(
    		OnDataLoadedListener listener)
    {
        mListener = null;
        if (mHandler != null)
        {
            mHandler = null;
        }
    }
    
}
