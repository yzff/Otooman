package com.manyanger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.common.base.Constants;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.common.PixValue;
import com.manyanger.common.ToastUtil;
import com.manyanger.data.ComicListDataLoader;
import com.manyanger.data.ComicThemeDataLoader;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.entries.BaseResponse;
import com.manyanger.entries.FirstPageModel;
import com.manyanger.ui.widget.FirstAdapter;
import com.manyanger.ui.widget.GalleryAdapter;
import com.manyanger.ui.widget.GalleryIndicator;
import com.manyanger.ui.widget.HomeGalleryAdapter;
import com.manyanger.ui.widget.LoadView;
import com.manyanger.ui.widget.SkyGallery;
import com.manyounger.otooman.R;

import java.util.List;

public class MainActivity extends SlidingFragmentActivity implements
		OnDataLoadedListener, OnClickListener {
    
    private static long trigleCancel;

	protected ListFragment mFrag;

	private FrameLayout contentView;
	private LayoutInflater mLayoutInflater;
	private RelativeLayout noml_title;
	private RelativeLayout layout_search;
	private EditText et_search;
	private Button btn_cancel;

	private ComicListDataLoader listLoader;
	private String cmdKey;

	private int state;

	// private List<BaseComicItem> bannerList;
	// private List<BaseComicItem> featuredList;
	// private List<BaseComicItem> recommendList;
	// private List<BaseComicItem> newestList;

	private FirstPageModel listModel;

	private SlidingMenu sm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GlobalData.homeActivity = this;

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			// mFrag = new SampleListFragment();
			// t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (ListFragment) this.getSupportFragmentManager()
					.findFragmentById(R.id.menu_frame);
		}

		// customize the SlidingMenu
		sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
//		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		
		initMenuView();

		initView();
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		state = GlobalData.STATE_INIT;
		initData();
	}

	private void initMenuView() {
		View mainBtn = findViewById(R.id.btn_main);
		if (mainBtn != null) {
			mainBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					sm.showContent();
					toggle();
				}

			});
		}

		View mineBtn = findViewById(R.id.btn_mine);
		if (mineBtn != null) {
			mineBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					sm.showContent();

					Intent intent = new Intent(MainActivity.this,
							MyComicActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					toggle();
				}

			});
		}

		View catoryBtn = findViewById(R.id.btn_catory);
		if (catoryBtn != null) {
			catoryBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					sm.showContent();
					Intent intent = new Intent(MainActivity.this,
							CategoryActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					toggle();
				}

			});
		}

	}

	protected void initView() {
		mLayoutInflater = LayoutInflater.from(this);
		View activityView = mLayoutInflater.inflate(R.layout.activity_main,
				null);
		setContentView(activityView);

		noml_title = (RelativeLayout) findViewById(R.id.noml_title);
		layout_search = (RelativeLayout) findViewById(R.id.layout_search);
		et_search = (EditText) findViewById(R.id.et_search);
		contentView = (FrameLayout) findViewById(R.id.content_lay);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		et_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.length() != 0) {
					btn_cancel.setText(R.string.conmm_confirm);
				} else {
					btn_cancel.setText(R.string.conmm_cancel);
				}
			}
		});

	}

	private void initData() {
		if (state != GlobalData.STATE_LOADING) {
			state = GlobalData.STATE_LOADING;
			showLoadingView();
			if (listLoader == null) {
				listLoader = new ComicListDataLoader(this);
			}
			cmdKey = listLoader.loadFirstList();
		}
	}

	private void loadThemeList() {
		new ComicThemeDataLoader(this).loadThemeList();

	}

	private void showLoadingView() {
		contentView.removeAllViews();
		LoadView viewGroup = new LoadView(this, true);
		contentView.addView(viewGroup);
		viewGroup.setId(1);
	}

	private void showErrorView() {
		View view = mLayoutInflater.inflate(R.layout.error, null);
		Button retryBtn = (Button) view.findViewById(R.id.refresh_btn);
		if (retryBtn != null) {
			retryBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					initData();
				}

			});
		}

		contentView.removeAllViews();
		contentView.addView(view);
	}

	private void showContentView() {
		View view = mLayoutInflater.inflate(R.layout.main_content, null);

		contentView.removeAllViews();
		contentView.addView(view);

		initCartoonList();
	}

	private void startComicListActivity(int id, int titleId) {
		Intent intent = new Intent(MainActivity.this, ComicListActivity.class);
		intent.putExtra(GlobalData.STR_TITLE, AppUtil.getString(titleId));
		intent.putExtra(GlobalData.STR_LISTTYPE, Constants.COMIC_LIST_TYPE_ALL);
		intent.putExtra(GlobalData.STR_LISTID, id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

	}

	private void initCartoonList() {

		SkyGallery bannerView = (SkyGallery) findViewById(R.id.home_banner);
		if (bannerView != null && listModel.getAdList() != null) {
			// List<BaseComicItem> bannerList = testBuildBannerList();
			HomeGalleryAdapter bannerAdapter = new HomeGalleryAdapter(this,
					listModel.getAdList());

			bannerView.setAdapter(bannerAdapter);
			bannerView.setDiscipline(true);
			bannerView.setCircle(true);
			bannerView.setPaddingAndSpace(0, 0, 0);
			bannerView.setPadding(0, 0, 0, PixValue.dip.valueOf(1));
			bannerAdapter.setIsShowFull(true);
			GalleryIndicator galleryIndicator = (GalleryIndicator) findViewById(R.id.g_indicator);
			if (galleryIndicator != null) {
				galleryIndicator.setData(listModel.getAdList(),
						ImageCache.getInstance());
				bannerView.setGalleryPageSwitchListener(galleryIndicator);
			}
			bannerAdapter.setSkyGallery(bannerView);
		}

		SkyGallery recommendView = (SkyGallery) findViewById(R.id.home_recommend);
		if (recommendView != null && listModel.getRecommendList() != null) {
			setGalleryView(recommendView, listModel.getRecommendList());
		}

		SkyGallery featuredView = (SkyGallery) findViewById(R.id.home_featured);
		if (featuredView != null && listModel.getFeaturedList() != null) {
			setGalleryView(featuredView, listModel.getFeaturedList());
		}

		SkyGallery newestView = (SkyGallery) findViewById(R.id.home_newest);
		if (newestView != null && listModel.getNewestList() != null) {
			setGalleryView(newestView, listModel.getNewestList());
		}
	}

	private void setGalleryView(SkyGallery gallery, List<BaseComicItem> list) {
		gallery.setDiscipline(false);
		gallery.setCircle(false);
		gallery.setPaddingAndSpace(PixValue.dip.valueOf(12),
				PixValue.dip.valueOf(12), PixValue.dip.valueOf(12));

		GalleryAdapter adapter = new FirstAdapter(this, list);

		gallery.setAdapter(adapter);
		adapter.setSkyGallery(gallery);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		sm.showMenu();
		return true;
	}

	
	@Override
	public void OnDataLoaded(Message msg) {
		if (msg == null) {
			return;
		}
		if (msg.what == GlobalData.NOTIFY_COMICLIST_LOADED) {
			if (msg.obj == null) {
				if (state == GlobalData.STATE_LOADING) {
					state = GlobalData.STATE_FAILED;
					showErrorView();
				}
				return;
			}

			BaseResponse respone = (BaseResponse) msg.obj;
			if (respone.getKeyWord().equals(cmdKey)) {
				if (respone.isSuccess()) {
					state = GlobalData.STATE_FINISHED;
					listModel = (FirstPageModel) respone;
					showContentView();
				} else {
					state = GlobalData.STATE_FAILED;
					showErrorView();

				}
			}

			listLoader = null;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.home_recommend_btn:
			startComicListActivity(3, R.string.first_recommend);
			break;
		case R.id.home_featured_btn:
			startComicListActivity(2, R.string.first_featured);
			break;
		case R.id.home_newest_btn:
			startComicListActivity(-1, R.string.first_newest);
			break;
		case R.id.search_btn:

			noml_title.setVisibility(View.GONE);
			layout_search.setVisibility(View.VISIBLE);

			break;
		case R.id.btn_cancel:
			if (!et_search.getText().toString().equals("")) {
				if (!et_search.getText().toString().equals("")) {
					final Intent intent = new Intent(this,
							ComicListActivity.class);
					intent.putExtra(GlobalData.STR_TITLE, "搜索");
					intent.putExtra(GlobalData.STR_KEYWORD, et_search.getText()
							.toString());
					intent.putExtra(GlobalData.STR_LISTTYPE,
							Constants.COMIC_LIST_TYPE_SEARCH);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}

			} else {
				noml_title.setVisibility(View.VISIBLE);
				layout_search.setVisibility(View.GONE);
				et_search.clearFocus();  
		         InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);   
		           imm.hideSoftInputFromWindow(et_search.getWindowToken(),0);  
			}
			break;
		case R.id.menu_btn:
//			if (sm.isActivated()) {
//				sm.showContent();
//			} else {
//				sm.showMenu();
//			}
			toggle();
			break;
		case R.id.iv_delete:
			et_search.setText("");
			break;
		default:
			break;
		}

	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
       	GlobalData.homeActivity = null;
	}
	
    @Override
    public void onBackPressed()
    {
        ToastUtil.cancel();

            //“提示再按一次可退出..”
        toastExtrance();
    }
    protected void toastExtrance()
    {
        final long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis - trigleCancel > 2000)
        {
            trigleCancel = uptimeMillis;
            ToastUtil.showToastShort(getString(R.string.note_exit));
        }
        else
        {
            finish();
            if(GlobalData.myActivity != null){
            	GlobalData.myActivity.finish();
            }
            if(GlobalData.cateActivity != null){
            	GlobalData.cateActivity.finish();
            }
            System.exit(0);
        }
    }


}
