package com.manyanger.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.common.ThreadPool;
import com.manyanger.data.loader.ComicItemsLoader;
import com.manyanger.entries.BaseComicItem;

import java.io.InputStream;


public class ImageDataLoader {
	private final OnIconLoadedListener mListener;
	
	private final ImageCache imageCache;
	
    private final Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (mListener != null)
            {
                mListener.OnAppIconLoaded(msg);
            }
        }
    };
	
    public ImageDataLoader(OnIconLoadedListener listener, ImageCache imageCache)
    {
    	mListener = listener;
    	this.imageCache = imageCache;
    }
    
    public void loadCoverImage(BaseComicItem item){
        ThreadPool.submit(new LoadImageRunnable(item, false));
    }
    
    public void loadBigImage(BaseComicItem item){
        ThreadPool.submit(new LoadImageRunnable(item, true));
    }
    
    private class LoadImageRunnable implements Runnable
    {
    	private final BaseComicItem item;
    	private boolean isBig = false;
        public LoadImageRunnable(BaseComicItem item, boolean isBig)
        {
        	this.item = item;
        	this.isBig = isBig;
        }
        
		@Override
		public void run() {
			String iconKey;
			if(isBig){
				iconKey = item.getBigImageKey();
			} else {
				iconKey = item.getCoverIconKey();
			}
			
			Bitmap bitmap = imageCache.get(iconKey);
			if(bitmap != null && !bitmap.isRecycled()){
				if(isBig){
					item.setBigImageState(BaseComicItem.ICON_STATE_FINISHED);
				} else {
					item.setCoverState(BaseComicItem.ICON_STATE_FINISHED);
				}
			} else {
				String url;
				if(isBig){
					item.setBigImageState(BaseComicItem.ICON_STATE_LOADING);
					url = item.getBigImageUrl();
				} else {
					item.setCoverState(BaseComicItem.ICON_STATE_LOADING);
					url = item.getCoverUrl();
				}
//				Log.i("otooman", "LoadImage:"+url);
				
				try{
					InputStream is = ComicItemsLoader.loadImage(url);
					if(is != null){
                    	byte[] data = AppUtil.inputStreamToByte(is);
//						bitmap = BitmapFactory.decodeStream(is);
                    	bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	                    imageCache.putInMemery(iconKey, bitmap);

	                    if (bitmap != null)
	                    {
	                    	ImageCache tempCache = ImageCache.buildTempImageCache();
	                        // 保存至磁盘
	                        tempCache.put(iconKey, bitmap, data);
	                        tempCache.remove(iconKey, bitmap);
	                    }
						if(isBig){
							item.setBigImageState(BaseComicItem.ICON_STATE_FINISHED);
						} else {
							item.setCoverState(BaseComicItem.ICON_STATE_FINISHED);
						}
					} else {
						if(isBig){
							item.setBigImageState(BaseComicItem.ICON_STATE_FAILED);
						} else {
							item.setCoverState(BaseComicItem.ICON_STATE_FAILED);
						}
					}
				} catch(Exception e){
					e.printStackTrace();
					if(isBig){
						item.setBigImageState(BaseComicItem.ICON_STATE_FAILED);
					} else {
						item.setCoverState(BaseComicItem.ICON_STATE_FAILED);
					}
				}
				
			}
			
            if (mHandler == null)
            {
                return;
            }
            //准备回调
            Message message = mHandler.obtainMessage();
            message.what = GlobalData.NOTIFY_COMICCOVER_LOADED;
            message.arg1 = item.getId();
            message.obj = iconKey;
            mHandler.sendMessage(message);
		}
    	
    }
	
    public static abstract interface OnIconLoadedListener
    {
        public abstract void OnAppIconLoaded(Message msg);
    }

}
