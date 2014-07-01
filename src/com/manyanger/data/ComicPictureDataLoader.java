package com.manyanger.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.common.ThreadPool;
import com.manyanger.data.loader.ComicItemsLoader;

import java.io.InputStream;


/**
 * @ClassName: ComicPictureDataLoader
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-22 下午5:17:00
 */
public class ComicPictureDataLoader {
    private final OnPictureLoadedListener mListener;
    
    private final ImageCache imageCache;
    
    private final Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (mListener != null)
            {
                mListener.OnPictureLoaded(msg);
            }
        }
    };
    
    public void loadPicture(String url, int pos){
        ThreadPool.submitPreview(new LoadImageRunnable(url, pos));
    }
    
    private class LoadImageRunnable implements Runnable
    {
        private final String url;
        private final int pos;
        public LoadImageRunnable(String url, int pos)
        {
            this.url = url;
            this.pos = pos;
        }
        
        @Override
        public void run() {
            int state = 0;
            String iconKey = DataUtils.makeIconKey(url);

            Bitmap bitmap = imageCache.get(iconKey);
            if(bitmap != null && !bitmap.isRecycled()){

            } else {
            	Log.i("PICTURE", "loadPicture:"+url);
            	try{

                    InputStream is = ComicItemsLoader.loadImage(url);
                    if(is != null){
                    	byte[] data = AppUtil.inputStreamToByte(is);
//                        bitmap = BitmapFactory.decodeStream(is);
                    	bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null)
                        {
                            // 保存至磁盘
                            imageCache.put(iconKey, bitmap, data);
                        }

                    } else {
                        state = 1;
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    state = 1;
                }
                
            }
            
            if (mHandler == null)
            {
                return;
            }
            //准备回调
            Message message = mHandler.obtainMessage();
            message.what = GlobalData.NOTIFY_CONTENTIMAGE_LOADED;
            message.arg1 = pos; //state;
            message.obj = iconKey;
            mHandler.sendMessage(message);
        }
        
    }
    
    public ComicPictureDataLoader(OnPictureLoadedListener listener, ImageCache imageCache)
    {
        mListener = listener;
        this.imageCache = imageCache;
    }
    
    public static abstract interface OnPictureLoadedListener
    {
        public abstract void OnPictureLoaded(Message msg);
    }

}
