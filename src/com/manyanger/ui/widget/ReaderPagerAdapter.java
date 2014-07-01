package com.manyanger.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.data.ComicPictureDataLoader;
import com.manyanger.data.ComicPictureDataLoader.OnPictureLoadedListener;
import com.manyanger.data.DataUtils;
import com.manyanger.ui.ComicReaderActivity;

import java.util.ArrayList;
import java.util.List;

public class ReaderPagerAdapter extends PagerAdapter implements OnPictureLoadedListener {
    
    private final static int STATE_INIT = 0;
    private final static int STATE_LOADING = 1;
    private final static int STATE_FINISHED = 2;
    private final static int STATE_FAILED = 3;

	private final Context context;

	private final ImageCache imageCache;

	private List<String> mItems;
	
	private final ComicPictureDataLoader pictureLoader;
	
//    private ImageView magazine_content;
	private ViewGroup viewContainer;
    
    private int currPos;
    
    private int[] loadStats;



	public void setData(List<String> data) {
	    if(data != null){
    		this.mItems = data;
    		this.loadStats = new int[data.size()];
    		notifyDataSetChanged();
	    }
	}

	public ReaderPagerAdapter(Context context) {
		this.context = context;
		imageCache = ImageCache.buildTempImageCache();
		pictureLoader = new ComicPictureDataLoader(this, imageCache);
	}

	@Override
	public int getCount() {
		return mItems != null ? mItems.size() : 0;
	}


	
	@Override
    public View instantiateItem(final ViewGroup container, final int position) {
        
        ((ComicReaderActivity)context).dismissBar();
        
        PhotoView photoView = new PhotoView(container.getContext());

		String item = mItems.get(position);
		if (item != null) {
		    try{
		        String iconKey = DataUtils.makeIconKey(item);
		        photoView.setTag(iconKey);
    			Bitmap bmp;
    			bmp = imageCache.get(iconKey);
                if (bmp != null && !bmp.isRecycled()) {
                	photoView.setImageBitmap(bmp);
                } else {
                	photoView.setImageBitmap(AppUtil.getDefaultBigIconBitmap());
                	if(loadStats[position] != STATE_LOADING){
                	    pictureLoader.loadPicture(item, position);
                	}
                }
                currPos = position;

		    }catch(Exception e){
		        
		    }
		}
        container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.viewContainer = container;
		return photoView;
	}
	
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    	View view = (View)object;
    	try{
    		String iconKey = (String)view.getTag();
    		if(iconKey != null){
    			Bitmap bitmap = imageCache.getFromInMemery(iconKey);
    			if(bitmap != null){
    				bitmap.recycle();
    			}
    			imageCache.remove(iconKey);
    		}
    	} catch(Exception e){
    		e.printStackTrace();
    	}
        
    	((ComicReaderActivity)context).dismissBar();
        container.removeView((View) object);

    }
    
	@SuppressWarnings("deprecation")
    private void updateImageView(String iconKey){
//	    for(MyImageView view : imageViews){
//	        String key = (String)view.getTag();
//	        if(key.equals(iconKey)){
//	            Bitmap bmp = imageCache.get(iconKey);
//	            if(bmp != null){
//	                view.setImageBitmap(bmp);
//	            }
//	        }
//	    }
		ImageView child = (ImageView)viewContainer.findViewWithTag(iconKey);
        if(child != null){
            Bitmap bmp = imageCache.get(iconKey);
            if(bmp != null){
//            	magazine_content.setImageDrawable(new BitmapDrawable(bmp));
            	child.setImageBitmap(bmp);
            }
        }
	}

    @Override
    public void OnPictureLoaded(Message msg) {
        if(msg.what == GlobalData.NOTIFY_CONTENTIMAGE_LOADED){
            int pos = msg.arg1;
            loadStats[pos] = STATE_FINISHED;
            String iconKey = (String)msg.obj;
            updateImageView(iconKey);
            
//            pos += 1;
//            if(pos < getCount() && pos <= currPos + 1){
//                if(loadStats[pos] != STATE_LOADING){
//                    String item = mItems.get(pos);
//                    pictureLoader.loadPicture(item, pos);
//                }
//            }
            
        }
        
        
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}


}
