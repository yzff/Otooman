package com.manyanger.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.manyanger.GlobalData;
import com.manyanger.adapter.ChapterAdapter;
import com.manyanger.cache.ItemCache;
import com.manyanger.common.ToastUtil;
import com.manyanger.data.DetailDataLoader;
import com.manyanger.data.ImageDataLoader;
import com.manyanger.data.ImageDataLoader.OnIconLoadedListener;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.entries.ChapterItem;
import com.manyanger.entries.DetailInfo;
import com.manyanger.provider.OtooInfo;
import com.manyanger.ui.widget.InScrollGridView;
import com.manyanger.ui.widget.TipDialog;
import com.manyounger.otooman.R;

/**
 * @ClassName: DetailActivity
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-8 下午10:43:05
 */
public class DetailActivity extends Activity implements OnDataLoadedListener,
		OnItemClickListener, OnClickListener, OnIconLoadedListener {
	private BaseComicItem mComicItem;
	private String detailUrl;

	private DetailDataLoader detailLoader;
	private String cmdLine;

	private ImageView iconView;
	private boolean hasShowIcon = false;
	private TextView titleView;
	private TextView authorView;
	private TextView themeView;
	private TextView chapterView;
	private TextView favor_btn;
	private TextView tv_brief;
	private TextView tv_process;
	private Button readBtn;
	private View moreBtnView; 
	private InScrollGridView gv_chapter;
	private ChapterAdapter mChapterAdapter;

	private DetailInfo mDetailInfo;
	private int comicId;
	private boolean isFavorite;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_detail);

		comicId = getIntent().getIntExtra(GlobalData.STR_COMICID, -1);
		if (comicId != -1) {
			mComicItem = ItemCache.getInstance().get(comicId);
		}
		detailUrl = getIntent().getStringExtra(GlobalData.STR_URL);

		initView();
		fillContentByItem(mComicItem);

		TipDialog.showWait(this, null);

		detailLoader = new DetailDataLoader(this);
		if (comicId != -1) {
			cmdLine = detailLoader.loadComicDetail(comicId);
		} else {
			cmdLine = detailLoader.loadComicDetail(detailUrl);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		int id = intent.getIntExtra(GlobalData.STR_COMICID, -1);
		if(id != -1 && comicId == id){
			return;
		}
		comicId = id;
		if (comicId != -1) {
			mComicItem = ItemCache.getInstance().get(comicId);
		}
		detailUrl = intent.getStringExtra(GlobalData.STR_URL);

		fillContentByItem(mComicItem);

		TipDialog.showWait(this, null);

		detailLoader = new DetailDataLoader(this);
		if (comicId != -1) {
			cmdLine = detailLoader.loadComicDetail(comicId);
		} else {
			cmdLine = detailLoader.loadComicDetail(detailUrl);
		}
		
		mChapterAdapter = null;
		gv_chapter.setAdapter(null);
	}

	/**
	 * @param item
	 */
	private void fillContentByItem(BaseComicItem item) {
		if (item == null) {
			return;
		}

		titleView.setText(item.getTitle());
		authorView.setText(item.getAuthor());
		// themeView.setText(item.getTheme());
		chapterView.setText(item.getChapterString());
		if(item.getCoverUrl() != null){
		    String key = item.getCoverIconKey();
		    Bitmap bitmap = GlobalData.getImageCache().get(key);
	        if (bitmap != null && !bitmap.isRecycled()){
	            iconView.setImageBitmap(bitmap);
	            hasShowIcon = true;
	        }
		}
		
		final Cursor mCursor = getContentResolver().query(
				OtooInfo.favorite.CONTENT_URI,
				new String[] { OtooInfo.favorite.COL_BOOK_ID },
				OtooInfo.favorite.COL_BOOK_ID + "='" + comicId + "'", null,
				null);
		if (mCursor != null) {
			isFavorite = mCursor.getCount() != 0 ? true : false;
			mCursor.close();
		}
		setFavorite();

	}

	/**
     * 
     */
	private void initView() {
		TextView winTitleView = (TextView) findViewById(R.id.title);
		if (winTitleView != null) {
			winTitleView.setText(R.string.title_detail);
		}

		ImageView menuBtn = (ImageView) findViewById(R.id.menu_btn);
		if (menuBtn != null) {
			menuBtn.setVisibility(View.GONE);
		}

		ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
		if (backBtn != null) {
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					finish();
				}

			});
		}

		ImageView searchBtn = (ImageView) findViewById(R.id.search_btn);
		if (searchBtn != null) {
			searchBtn.setVisibility(View.GONE);
		}

		iconView = (ImageView) findViewById(R.id.img_icon);
		titleView = (TextView) findViewById(R.id.item_label);
		authorView = (TextView) findViewById(R.id.item_auth);
		themeView = (TextView) findViewById(R.id.item_theme);
		chapterView = (TextView) findViewById(R.id.item_chapter);
		favor_btn = (TextView) findViewById(R.id.favor_btn);

		tv_brief = (TextView) findViewById(R.id.tv_brief);
		tv_process = (TextView) findViewById(R.id.tv_process);
		readBtn = (Button) findViewById(R.id.read_btn);
		moreBtnView = findViewById(R.id.btn_more_lay);

		gv_chapter = (InScrollGridView) findViewById(R.id.gv_chapter);
		gv_chapter.setOnItemClickListener(this);
	}

	private void setFavorite() {
		if (isFavorite) {
			favor_btn.setText(getString(R.string.favoring));
			final Drawable drawable = getResources().getDrawable(
					R.drawable.heart_h);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			favor_btn.setCompoundDrawables(drawable, null, null, null);
		} else {
			favor_btn.setText(getString(R.string.favor));
			final Drawable drawable = getResources().getDrawable(
					R.drawable.heart);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			favor_btn.setCompoundDrawables(drawable, null, null, null);
		}

	}

	@Override
	public void OnDataLoaded(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case GlobalData.NOTIFY_COMICDETAIL_LOADED:
			
			TipDialog.dissmissTip();
			mDetailInfo = (DetailInfo) msg.obj;
			if(mDetailInfo == null){
				ToastUtil.showToast(R.string.no_net_hit);
				return;
			}
			
			if(mDetailInfo.getChapterList() != null){
    			mChapterAdapter = new ChapterAdapter(DetailActivity.this,
    					mDetailInfo.getChapterList());
    			gv_chapter.setAdapter(mChapterAdapter);
    			if(mDetailInfo.getChapters() > mDetailInfo.getChapterList().size()){
    			    moreBtnView.setVisibility(View.VISIBLE);
    			} else {
    				moreBtnView.setVisibility(View.GONE);
    			}
			}
			if(mDetailInfo.getDepict() != null && mDetailInfo.getDepict().length() > 0){
			    tv_brief.setText(mDetailInfo.getDepict());
			} else {
			    tv_brief.setText(R.string.no_brief);
			}
			tv_process
					.setText(mDetailInfo.getProcess());
			themeView.setText(mDetailInfo.getTheme());
			if(mDetailInfo.getTitle() != null){
			    titleView.setText(mDetailInfo.getTitle());
			}
			if(mDetailInfo.getAuthor() != null){
		        authorView.setText(mDetailInfo.getAuthor());
			}
			
			chapterView.setText(mDetailInfo.getChapterString());
			
			if(!hasShowIcon && mDetailInfo.getCoverUrl() != null){
				mComicItem.setCoverUrl(mDetailInfo.getCoverUrl());
				String iconKey = mComicItem.getCoverIconKey();
				Bitmap bitmap = GlobalData.getImageCache().get(iconKey);
		        if (bitmap != null && !bitmap.isRecycled()){
		            iconView.setImageBitmap(bitmap);
		            hasShowIcon = true;
		        } else {
		        	(new ImageDataLoader(this, GlobalData.getImageCache())).loadCoverImage(mComicItem);
		        }
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
	    try{
    	    ChapterItem item = mDetailInfo.getChapterList().get(position);
    	    if(item != null){
        		Intent intent = new Intent(DetailActivity.this, ComicReaderActivity.class);
        		intent.putExtra(GlobalData.STR_CHAPTERID, item.getId());
        		intent.putExtra(GlobalData.STR_TITLE, item.getTitle());
        		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	    startActivity(intent);
    	    }
	    } catch(Exception e){
	        
	    }
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.read_btn:
		    if(mDetailInfo.getChapterList() != null && mDetailInfo.getChapterList().size() > 0){
		        ChapterItem chapter = mDetailInfo.getChapterList().get(0);
	            Intent intent = new Intent(DetailActivity.this, ComicReaderActivity.class);
	            intent.putExtra(GlobalData.STR_CHAPTERID, chapter.getId());
	            intent.putExtra(GlobalData.STR_TITLE, chapter.getTitle());
	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
		    }
		    break;
		case R.id.btn_more:
			// TODO 更多跳转 增加为空判断
			final Intent intent = new Intent(this, ChapterDetailActivity.class);
			intent.putExtra(GlobalData.STR_COMICID, mDetailInfo.getId());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.favor_btn:
			if (isFavorite) {
				final int cout = getContentResolver().delete(
						OtooInfo.favorite.CONTENT_URI,
						OtooInfo.favorite.COL_BOOK_ID + "='" + comicId + "'",
						null);
				if (cout != 0) {
					isFavorite = false;
					setFavorite();
				} else
					Toast.makeText(this, R.string.cancel_fail, Toast.LENGTH_SHORT)
							.show();
			} else {
				// TODO 插入最好放到异步做
				final ContentValues values = new ContentValues();
				values.put(OtooInfo.favorite.COL_BOOK_ID, comicId);
				values.put(OtooInfo.favorite.COL_AUTHOR,
						mDetailInfo.getAuthor());
				values.put(OtooInfo.favorite.COL_CHAPTER_COUT,
						mDetailInfo.getChapters());
				values.put(OtooInfo.favorite.COL_NAME, mComicItem.getTitle());
				values.put(OtooInfo.favorite.COL_COVER, mComicItem.getCoverUrl());
				values.put(OtooInfo.favorite.COL_PROCESS,
						mDetailInfo.getProcess());
				Uri uri = getContentResolver().insert(
						OtooInfo.favorite.CONTENT_URI, values);
				if (uri != null) {
					Toast.makeText(this, R.string.favoring, Toast.LENGTH_SHORT)
							.show();
					isFavorite = true;
					setFavorite();
				} else
					Toast.makeText(this, R.string.favor_fail,
							Toast.LENGTH_SHORT).show();

			}
			break;

		default:
			break;
		}
	}

	@Override
	public void OnAppIconLoaded(Message msg) {
		if(msg.what == GlobalData.NOTIFY_COMICCOVER_LOADED){
			int id = msg.arg1;
			if(id == mComicItem.getId()){
				
			}
			String iconKey = mComicItem.getCoverIconKey();
			Bitmap bitmap = GlobalData.getImageCache().get(iconKey);
	        if (bitmap != null && !bitmap.isRecycled()){
	            iconView.setImageBitmap(bitmap);
	            hasShowIcon = true;
	        }
		}
	}
	
    @Override
    public void onBackPressed()
    {
    	finish();
    }
}
