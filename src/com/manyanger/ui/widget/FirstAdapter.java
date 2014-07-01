/**
 * 
 */

package com.manyanger.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.manyanger.GlobalData;
import com.manyanger.adapter.ViewHolder;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.common.PixValue;
import com.manyanger.data.ImageDataLoader;
import com.manyanger.data.ImageDataLoader.OnIconLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.ui.DetailActivity;
import com.manyounger.otooman.R;

import java.util.ArrayList;
import java.util.List;


public class FirstAdapter extends GalleryAdapter implements
OnItemClickListener, OnIconLoadedListener
{
    private SkyGallery mSkyGallery;

    private static List<ReaptMap> list = new ArrayList<ReaptMap>();

    private ViewHolder holder;

    Context mContext;

    List<BaseComicItem> mHeader;

    int mItemCount;

    private final ImageCache mImageCache = ImageCache.getInstance();
    
    private final ImageDataLoader imageLoader = new ImageDataLoader(this, mImageCache);

    public FirstAdapter(Context context, List<BaseComicItem> list)
    {
        mContext = context;
        mHeader = list;
    }


    @Override
    public Bitmap getBitmap(int position, boolean loadFromNet)
    {
        Bitmap bitmap = null;
        BaseComicItem itemInfo = mHeader.get(position);
        String key = itemInfo.getCoverIconKey();
        // TODO:
//        bitmap = mImageCache.getFromInMemery(key);
        bitmap = mImageCache.get(key);
        if (bitmap == null || bitmap.isRecycled())
        {
            if(loadFromNet && itemInfo.getCoverState() != BaseComicItem.ICON_STATE_LOADING){
            	imageLoader.loadCoverImage(itemInfo);
            }
        }
        if (null == bitmap)
        {
            bitmap = AppUtil.getDefaultBigIconBitmap();
        }
        return bitmap;
    }

    @Override
    public int getBitmapHeight()
    {
        return PixValue.dip.valueOf(162);
    }

    @Override
    public int getBitmapWidth()
    {
        return PixValue.dip.valueOf(108);
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
    public int getItemViewType(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView)
    {
        if (null == convertView)
        {
            convertView = View.inflate(mContext, R.layout.home_item, null);
            holder = new ViewHolder();
            holder.mTitleView =
                    (TextView) convertView.findViewById(R.id.label);
            holder.mChapterView =
                    (TextView) convertView.findViewById(R.id.chapter);
            holder.mIconView =
                    (ImageView) convertView.findViewById(R.id.img_icon);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        BaseComicItem base = mHeader.get(position);
        holder.mItemModel = base;

        holder.mTitleView.setText(base.getTitle());
        holder.mChapterView.setText(base.getChapterStringShort());
        holder.mIconView.setImageBitmap(getBitmap(position, true));
        return convertView;
    }


    @Override
    public int getViewTypeCount()
    {
        return 1;
    }


    public void onDestory()
    {
        mContext = null;
        mHeader = null;
        // mGalleryIconLoader.unregisterOnAppIconLoadedListener(this);
        // mGalleryIconLoader = null;
    }

    public void onGalleryLoaded(int position)
    {
        int firstPosition = mSkyGallery.getFirstVisiblePosition();
        int pos;
        if (firstPosition <= position)
        {
            pos = position - firstPosition;
        }
        else
        {
            pos = position + mItemCount - firstPosition;
        }
        View v = mSkyGallery.getChildAt(pos);
        if (null != v)
        {
            ViewHolder viewHolder = (ViewHolder) v.getTag();
            if (null != viewHolder)
            {
                Bitmap bitmap =
                        ImageCache.getInstance().get(
                                getKey(viewHolder.mItemModel));
                if (null == bitmap)
                {
                    bitmap = AppUtil.getDefaultBigIconBitmap();
                }
                viewHolder.mIconView.setImageBitmap(bitmap);
                for (int i = 0; i < list.size(); i++)
                {
                    if (list.get(i).key.equals(getKey(viewHolder.mItemModel)))
                    {
                        ImageView image = (ImageView) list.get(i).view;
                        image.setImageBitmap(bitmap);
                        list.remove(i);
                    }
                }
            }
        }

    }

    @Override
    public void recycle()
    {
        /*
         * if (null != mHeader) { for (int i = 0; i < mHeader.size(); i++) {
         * BaseItemData itemInfo = mHeader.get(i); String key =
         * itemInfo.itemType == BaseItemData.ITEMTYPE_LEAF ? ((DownloadInfo)
         * itemInfo).getKey(true) : ((NodeModel) itemInfo).getKey(); Bitmap map
         * = Constants.priviewImageCache.getFromInMemery(key); if (map != null)
         * { WeakReference<String> weakKey = new WeakReference<String>(key);
         * WeakReference<Bitmap> weakValue = new WeakReference<Bitmap>(map);
         * Constants.priviewImageCache.remove(weakKey, weakValue); } } }
         */
    }

    @Override
    public void setSkyGallery(SkyGallery skyGallery)
    {
        mSkyGallery = skyGallery;
        // mGalleryIconLoader.registerOnAppIconLoadedListener(this);
        mSkyGallery.setOnItemClickListener(this);
    }

    private String getKey(BaseComicItem model)
    {
        return model.getCoverIconKey();
    }

    @Override
    public Object getData()
    {
        return mHeader;
    }

    class ReaptMap
    {
        public String key;

        public View view;

        public int position;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null || viewHolder.mItemModel == null)
        {
            return;
        }
        
        BaseComicItem itemInfo = viewHolder.mItemModel;
        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra("comicId", itemInfo.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
        
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
