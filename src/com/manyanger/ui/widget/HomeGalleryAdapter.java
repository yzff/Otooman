/**
 * 
 */
package com.manyanger.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.common.Config;
import com.manyanger.data.ImageDataLoader;
import com.manyanger.data.ImageDataLoader.OnIconLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.ui.DetailActivity;

import java.lang.ref.WeakReference;
import java.util.List;




public class HomeGalleryAdapter extends GalleryAdapter implements
    OnItemClickListener, OnIconLoadedListener
{

    private SkyGallery mSkyGallery;

    Context mContext;

    List<BaseComicItem> mHeader;

    int mItemCount;

    private boolean mIsFull = false;
    
    private final ImageCache mImageCache = ImageCache.getInstance();
    
    private final ImageDataLoader imageLoader = new ImageDataLoader(this, mImageCache);

    public HomeGalleryAdapter(Context context, List<BaseComicItem> list)
    {
        mContext = context;
        mHeader = list;
    }



    @Override
    public Bitmap getBitmap(int position, boolean loadFromNet)
    {
        Bitmap bitmap = null;
        String key = null;
        if (position >= 0 && position < mHeader.size())
        {
            BaseComicItem itemInfo = mHeader.get(position);
            key = itemInfo.getBigImageKey();
            
            if(loadFromNet){
            	// TODO:
//            	bitmap = mImageCache.getFromInMemery(key);
            	bitmap = mImageCache.get(key);
            } else {
            	bitmap = mImageCache.get(key);
            }
            if (bitmap == null || bitmap.isRecycled())
            {
                if(loadFromNet && itemInfo.getBigImageState() != BaseComicItem.ICON_STATE_LOADING){
                	imageLoader.loadBigImage(itemInfo);
                }

            }
            if (bitmap == null || bitmap.isRecycled())
            {
                 bitmap = AppUtil.getDefaultBannerBitmap();
            } 
        }
        return bitmap;
    }

    public void setIsShowFull(boolean isFull)
    {
        mIsFull = isFull;
    }

    @Override
    public int getBitmapWidth()
    {
        if (mIsFull)
        {
            return Config.getWidth();

        }
        else
        {
            return super.getBitmapWidth();
        }
    }

    @Override
    public int getBitmapHeight()
    {
        return getBitmapWidth() / 2;
    }

    @Override
    public final int getCount()
    {
        if (mItemCount == 0)
        {
            if (mHeader == null)
            {
                return 0;
            }
            return mItemCount = mHeader.size();
        }
        else
        {
            return mItemCount;
        }
    }


    @Override
    public View getView(int position, View convertView)
    {
        if (null == convertView)
        {
            ImageView iv = new ImageView(mContext);
//            iv.setStyle(Style.solid);
            iv.setScaleType(ScaleType.FIT_XY);
            iv.setLayoutParams(new LayoutParams(getBitmapWidth(),
                getBitmapHeight()));
            convertView = iv;
        }
        convertView.setTag(mHeader.get(position));
        ((ImageView) convertView).setImageBitmap(getBitmap(position, true));
        return convertView;
        

    }

    
    public void onDestory()
    {
        mContext = null;
        mHeader = null;
//        mBriefLauncher = null;
//        mGalleryIconLoader.unregisterOnAppIconLoadedListener(this);
//        mGalleryIconLoader = null;
    }

    public void onGalleryLoaded(int position)
    {
        int firstPosition = mSkyGallery.getFirstVisiblePosition();
        ImageView view;
        int pos;
        if (firstPosition <= position)
        {
            pos = position - firstPosition;
        }
        else
        {
            pos = position + mItemCount - firstPosition;
        }
        view = (ImageView) mSkyGallery.getChildAt(pos);
        if (null != view)
        {
            final Bitmap bitmap = getBitmap(position, false);
            view.setImageBitmap(bitmap);
            Log.v("", view + "--------------" + pos + "," + bitmap);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id)
    {
        BaseComicItem itemInfo = (BaseComicItem) view.getTag();
        if (itemInfo == null)
        {
            return;
        }
        
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra(GlobalData.STR_COMICID, itemInfo.getId());
        intent.putExtra(GlobalData.STR_URL, itemInfo.getDetailUrl());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);

    }

    @Override
    public void recycle()
    {
        if (null != mHeader)
        {
            for (int i = 0; i < mHeader.size(); i++)
            {
                BaseComicItem itemInfo = mHeader.get(i);
                String key = itemInfo.getBigImageKey();

                Bitmap map = mImageCache.getFromInMemery(key);
                if (map != null)
                {
                    WeakReference<String> weakKey =
                        new WeakReference<String>(key);
                    WeakReference<Bitmap> weakValue =
                        new WeakReference<Bitmap>(map);
                    mImageCache.remove(weakKey, weakValue);

                }
            }
        }
    }

    @Override
    public void setSkyGallery(SkyGallery skyGallery)
    {
        mSkyGallery = skyGallery;
//        mGalleryIconLoader.registerOnAppIconLoadedListener(this);
        skyGallery.setOnItemClickListener(this);
    }


    @Override
    public Object getData()
    {
        return mHeader;
    }


    @Override
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return position;
    }
    
    private int getViewPosById(int id){
    	int pos = 0;
        if(mHeader == null){
            return -1;
        }
        
        for(BaseComicItem item : mHeader){
        	if(item.getId() == id) {
        		return pos;
        	}
        	pos++;
        }
    	
        return -1;
    }
	@Override
	public void OnAppIconLoaded(Message msg) {

		if(msg.what == GlobalData.NOTIFY_COMICCOVER_LOADED){
			int id = msg.arg1;
			onGalleryLoaded(getViewPosById(id));
		}
		
		
	
		
	}

}
