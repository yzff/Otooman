package com.manyanger.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.data.ComicListDataLoader;
import com.manyanger.data.ImageDataLoader;
import com.manyanger.data.ImageDataLoader.OnIconLoadedListener;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.entries.BaseResponse;
import com.manyanger.entries.ListModel;
import com.manyanger.ui.widget.LoadingView;
import com.manyounger.otooman.R;

import java.util.List;

/**
 * @ClassName: BaseListAdapter
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-8 下午9:06:46
 */
public class BaseListAdapter extends BaseAdapter implements OnIconLoadedListener, OnClickListener, OnDataLoadedListener{
    
    private final static int STATUS_LOADITEM_IDLE = 0;

    protected final static int STATUS_LOADITEM_LOADING = 1;

    private final static int STATUS_LOADITEM_FINISHED = 2;

    private final static int STATUS_LOADITEM_ERROR = 3;
    
    private final Context context;
    
    private final List<BaseComicItem> itemList;
    
    private ListView mListView;

    protected LayoutInflater mInflater;
    
    protected int pageNo;
    
    protected boolean hasNextPage = true;
    
    protected int loadItemStatus;
    
    protected String loadCmd;
    
    private int indexType;
    private int themeId;
    private String keyword;
    
    private ViewGroup mFootViewGroup;
    
    private ComicListDataLoader listLoader;
    
    private final ImageCache mImageCache = ImageCache.getInstance();
    
    private final ImageDataLoader imageLoader = new ImageDataLoader(this, mImageCache);
    
    public BaseListAdapter(Context context, List<BaseComicItem> list){
        super();
        this.context = context;
        this.itemList = list;
        mInflater = LayoutInflater.from(context);
        loadItemStatus = STATUS_LOADITEM_FINISHED;
    }
    
    public BaseListAdapter(Context context, ListModel model){
        this(context, model.getItemList());
        this.pageNo = 0;
        this.hasNextPage = model.isHasNext();
        if(this.hasNextPage){
            loadItemStatus = STATUS_LOADITEM_IDLE;
        } else {
            loadItemStatus = STATUS_LOADITEM_FINISHED;
        }
    }
    
    public void setListView(ListView listView)
    {
        mListView = listView;
    }
    
    public void setLoadCondition(int indexType, int themeId, String keyword){
        // TODO:
        this.indexType = indexType;
        this.themeId = themeId;
        this.keyword = keyword;
    }


    @Override
    public int getCount() {
        if(itemList != null){
            int count = itemList.size();
            if(loadItemStatus != STATUS_LOADITEM_FINISHED){
                count++;
            }
            return count;
        }
        return 0;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(itemList == null){
            return null;
        }
        final int count = getCount();
        if (loadItemStatus != STATUS_LOADITEM_FINISHED
                && position == count - 1)
            {
                if (convertView == null)
                {
                    mFootViewGroup = makeFootView(loadItemStatus);
                    convertView = mFootViewGroup;
                }
                return convertView;
            }
        
        BaseComicItem item = itemList.get(position);
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.mTitleView = (TextView)convertView.findViewById(R.id.item_label);
            holder.mAuthView = (TextView)convertView.findViewById(R.id.item_auth);
            holder.mChapterView = (TextView)convertView.findViewById(R.id.item_chapter);
//            holder.mStarView = (RatingBar)convertView.findViewById(R.id.item_stars);
            holder.mIconView = (ImageView)convertView.findViewById(R.id.img_icon);
            
            convertView.setTag(holder);
            
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mItemModel = item;
        inflateHolderIcon(holder, item);
        
        if (position + 2 >= count && loadItemStatus == STATUS_LOADITEM_IDLE)
        {
            syncLoadItem();
        }
        
        return convertView;
    }
    
    private void syncLoadItem()
    {
        if (loadItemStatus == STATUS_LOADITEM_LOADING)
        {
            return;
        }
        loadItemStatus = STATUS_LOADITEM_LOADING;
        
        if(listLoader == null){
            listLoader = new ComicListDataLoader(this);
        }
        int page = pageNo + 1;
        if(indexType >= 0){
            loadCmd = listLoader.loadIndexList(indexType, page);
        } else if(themeId >= 0) {
            loadCmd = listLoader.loadThemeComicList(themeId, page);
        } else if(keyword != null && keyword.length() > 0) {
            loadCmd = listLoader.search(keyword, page);
        }
        

    }

    protected void inflateHolderIcon(ViewHolder viewHolder, BaseComicItem itemInfo) {
           viewHolder.mTitleView.setText(itemInfo.getTitle());
           viewHolder.mAuthView.setText(itemInfo.getAuthor());
           viewHolder.mChapterView.setText(itemInfo.getChapterString());
//           viewHolder.mStarView.setRating((float)itemInfo.getScore()/2);

            String key = itemInfo.getCoverIconKey();
// TODO:
//            Bitmap bitmap = mImageCache.getFromInMemery(key);
            Bitmap bitmap = mImageCache.get(key);
            if (bitmap != null && !bitmap.isRecycled())
            {
                viewHolder.mIconView.setImageDrawable(new BitmapDrawable(bitmap));
            }
            else
            {
                viewHolder.mIconView.setBackgroundDrawable(AppUtil.getDefaultIconBitmap());
                viewHolder.mIconView.setImageDrawable(null);
                if(itemInfo.getCoverState() != BaseComicItem.ICON_STATE_LOADING){
                	imageLoader.loadCoverImage(itemInfo);
                }
            }

        }
    
    private ViewGroup makeFootView(int loadStatus)
    {
        switch (loadStatus)
        {
            case STATUS_LOADITEM_IDLE:
            case STATUS_LOADITEM_LOADING:
            {
                if (mFootViewGroup == null)
                {
                    mFootViewGroup = createLoadingView();
                }
                mFootViewGroup.setOnClickListener(null);
                startLoadingView(mFootViewGroup);
            }
                break;
            case STATUS_LOADITEM_ERROR:
                if (mFootViewGroup == null)
                {
                    mFootViewGroup = createLoadingView();
                }
                mFootViewGroup.setOnClickListener(this);
                stopLoading(mFootViewGroup);
                break;
            case STATUS_LOADITEM_FINISHED:
                mFootViewGroup = null;
                break;
            default:
                break;
        }
        return mFootViewGroup;
    }
    
    protected ViewGroup createLoadingView()
    {
        return new LoadingView(context);
    }
    
    protected void startLoadingView(ViewGroup mFootViewGroup)
    {
        ImageView bar =
            (ImageView) mFootViewGroup.findViewById(R.id.loading_icon);
        bar.setVisibility(View.GONE);
        mFootViewGroup.findViewById(R.id.load_progress).setVisibility(
            View.VISIBLE);
        TextView hint = (TextView) mFootViewGroup.findViewById(R.id.hint);
        hint.setText(R.string.loaditem_loading);
    }

    /**
     * 停止关闭页脚“正在加载..”的视图
     * Method: stopLoading
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-4-18 
     */
    protected void stopLoading(ViewGroup mFootViewGroup)
    {
        ViewHolder retryViewHolder = new ViewHolder();
//        retryViewHolder.mTagId = TAG_LOADITEM_RETRY;
        mFootViewGroup.setTag(retryViewHolder);

        ImageView bar =
            (ImageView) mFootViewGroup.findViewById(R.id.loading_icon);
        bar.setVisibility(View.VISIBLE);
        //wsl 修改,图标统一放背景层
        bar.setBackgroundResource(R.drawable.load_failed);
        //bar.setImageResource(R.drawable.load_failed);
        bar.setTag(retryViewHolder);
        mFootViewGroup.findViewById(R.id.load_progress)
            .setVisibility(View.GONE);

        TextView hint = (TextView) mFootViewGroup.findViewById(R.id.hint);
        hint.setText(R.string.try_again);
        hint.setOnClickListener(this);
        hint.setTag(retryViewHolder);
    }


    private int getViewPosById(int id){
    	int pos = 0;
        if(itemList == null){
            return -1;
        }
        
        for(BaseComicItem item : itemList){
        	if(item.getId() == id) {
        		return pos;
        	}
        	pos++;
        }
    	
        return -1;
    }
    

	private void updateAppIcon(int position) {
        if (mListView == null)
        {
            return;
        }
        int firstPos = mListView.getFirstVisiblePosition();
        int lastPos = mListView.getLastVisiblePosition();
        if (position < firstPos || position > lastPos)
        {
            return;
        }

        View view =
            mListView.getChildAt(position - firstPos
                + mListView.getHeaderViewsCount());
        if (view == null)
        {
            return;
        }

        ViewHolder viewHolder;
        if ((viewHolder = (ViewHolder) view.getTag()) == null)
        { 
            return;
        }

        String key = viewHolder.mItemModel.getCoverIconKey();

        Bitmap bitmap = mImageCache.get(key);
        if (bitmap != null && !bitmap.isRecycled())
        {
            viewHolder.mIconView.setImageDrawable(new BitmapDrawable(bitmap));
        }
        
    }
	
	@Override
	public void OnAppIconLoaded(Message msg) {
		if(msg.what == GlobalData.NOTIFY_COMICCOVER_LOADED){
			int id = msg.arg1;
			updateAppIcon(getViewPosById(id));
		}
		
	}

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void OnDataLoaded(Message msg) {
        if (msg == null) {
            return;
        }
        if (msg.what == GlobalData.NOTIFY_COMICLIST_LOADED) {
            if (msg.obj == null) {
                if (loadItemStatus == STATUS_LOADITEM_LOADING) {
                    loadItemStatus = STATUS_LOADITEM_ERROR;
                    makeFootView(loadItemStatus);
                }
                return;
            }
            BaseResponse respone = (BaseResponse) msg.obj;
            if (respone.getKeyWord().equals(loadCmd)) {
                if (respone.isSuccess()) {
                    ListModel listModel = (ListModel) respone;
                    if(listModel.getItemList() != null){
                        itemList.addAll(listModel.getItemList());
                    }
                    if(listModel.isHasNext()){
                        hasNextPage = true;
                        loadItemStatus = STATUS_LOADITEM_IDLE;
                    } else {
                        hasNextPage = false;
                        loadItemStatus = STATUS_LOADITEM_FINISHED;
                    }
                    pageNo = listModel.getPageIndex();
                    notifyDataSetChanged();
                } else {
                    if (loadItemStatus == STATUS_LOADITEM_LOADING) {
                        loadItemStatus = STATUS_LOADITEM_ERROR;
                        makeFootView(loadItemStatus);
                    }
                }
            }

            // listLoader = null;
        }
        
    }

}
