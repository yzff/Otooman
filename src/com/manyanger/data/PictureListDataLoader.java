package com.manyanger.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.manyanger.GlobalData;
import com.manyanger.common.ThreadPool;
import com.manyanger.data.loader.ComicItemsLoader;
import com.manyanger.data.net.DataException;
import com.manyanger.entries.ChapterDetail;

/**
 * @ClassName: PictureListDataLoader
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-24 下午8:31:36
 */
public class PictureListDataLoader {
    private Handler mHandler;

    // 列表加载完成的监听接口
    private OnDataLoadedListener mListener;

    public PictureListDataLoader(OnDataLoadedListener listener) {
        registerOnDataLoadedListener(listener);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (mListener != null) {
                    // 应用列表加载完成，回调
                    mListener.OnDataLoaded(msg);
                }
            }
        };
    }
    
    public String loadPictureList(int chapterId) {
        String cmd = GlobalData.CMD_READ;
        ThreadPool.submitText(new LoadDataRunnable(cmd, chapterId));

        return cmd;
    }
    
    private class LoadDataRunnable implements Runnable {
        String cmd;
        int chapterId;

        public LoadDataRunnable(String cmd, int chapterId) {
            this.cmd = cmd;
            this.chapterId = chapterId;

        }

        @Override
        public void run() {

            String resultStr = null;
            ChapterDetail item = null;
            String cmdLine = DataUtils.makeCmdString(cmd, GlobalData.PARAMS_CHAPTERID, chapterId);
            try {
                resultStr = ComicItemsLoader.load(cmdLine);

            } catch (DataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            if (resultStr == null) {

            } else {
                item = JsonParser.parseChapterDetail(resultStr);
            }

            if (mHandler == null) {
                return;
            }
            // 准备回调
            Message message = mHandler.obtainMessage();
            message.what = GlobalData.NOTIFY_PICTURELIST_LOADED;
            message.arg1 = chapterId;
            message.obj = item;
            mHandler.sendMessage(message);
        }

    }
    
    public void registerOnDataLoadedListener(OnDataLoadedListener listener) {
        mListener = listener;
    }

    public void unregisterOnDataLoadedListener(OnDataLoadedListener listener) {
        mListener = null;
        if (mHandler != null) {
            mHandler = null;
        }
    }

}
