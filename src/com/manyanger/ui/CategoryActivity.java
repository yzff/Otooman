package com.manyanger.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.google.common.base.Constants;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.manyanger.GlobalData;
import com.manyanger.adapter.CategoryAdapter;
import com.manyanger.common.ToastUtil;
import com.manyanger.data.ComicThemeDataLoader;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.entries.BaseThemeItem;
import com.manyanger.ui.widget.TipDialog;
import com.manyounger.otooman.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CategoryActivity
 * @Description: 漫画分类列表
 * @author Zephan.Yu
 * @date 2014-6-19 下午11:16:05
 */
public class CategoryActivity extends SlidingFragmentActivity implements OnDataLoadedListener,
		OnItemClickListener, OnClickListener {
	// UI
	private GridView gv_gategory;

	// DATA
	private CategoryAdapter mCategoryAdapter;
	private List<BaseThemeItem> categorys;
	private ComicThemeDataLoader mComicThemeDataLoader;
	
	private SlidingMenu sm;
	protected ListFragment mFrag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GlobalData.cateActivity = this;
		setContentView(R.layout.category);
		
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
		
		gv_gategory = (GridView) findViewById(R.id.gv_gategory);
		gv_gategory.setOnItemClickListener(this);

		categorys = GlobalData.getCategorys();
		if(categorys == null || categorys.size() == 0){
			mComicThemeDataLoader = new ComicThemeDataLoader(this);
			mComicThemeDataLoader.loadThemeList();
			TipDialog.showWait(this, null);
		} else {
			mCategoryAdapter = new CategoryAdapter(this, categorys,
					gv_gategory.getNumColumns());
			gv_gategory.setAdapter(mCategoryAdapter);
		}



	}
	
	private void initMenuView() {
		View mainBtn = findViewById(R.id.btn_main);
		if (mainBtn != null) {
			mainBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					Intent intent = new Intent(CategoryActivity.this,
							MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					sm.showContent();
//					finish();
				}

			});
		}

		View mineBtn = findViewById(R.id.btn_mine);
		if (mineBtn != null) {
			mineBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(CategoryActivity.this,
							MyComicActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					sm.showContent();
//					finish();
				}

			});
		}

		View catoryBtn = findViewById(R.id.btn_catory);
		if (catoryBtn != null) {
			catoryBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					sm.showContent();

				}

			});
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void OnDataLoaded(Message msg) {
		if (msg.what == GlobalData.NOTIFY_COMICTHEME_LOADED) {
			categorys = (List<BaseThemeItem>) msg.obj;
			// TODO 问题 暂时处理，NULL也传回来了，增加错误管理
			if (categorys == null)
				categorys = new ArrayList<BaseThemeItem>();
			mCategoryAdapter = new CategoryAdapter(this, categorys,
					3); //gv_gategory.getNumColumns());
			gv_gategory.setAdapter(mCategoryAdapter);
			GlobalData.setCategorys(categorys);
			TipDialog.dissmissTip();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GlobalData.cateActivity = null;
		if(mComicThemeDataLoader != null){
			mComicThemeDataLoader.unregisteOnDataLoadedListener(this);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final BaseThemeItem mBaseThemeItem = (BaseThemeItem) mCategoryAdapter
				.getItem(arg2);
		startComicListActivity(mBaseThemeItem.getId(),
				mBaseThemeItem.getTitle());
	}

	private void startComicListActivity(int id, String title) {
		Intent intent = new Intent(CategoryActivity.this,
				ComicListActivity.class);
		intent.putExtra(GlobalData.STR_TITLE, title);
		intent.putExtra(GlobalData.STR_LISTTYPE, Constants.COMIC_LIST_TYPE_THEME);
		intent.putExtra(GlobalData.STR_LISTID, id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.back_btn:
//			finish();
//			break;
		case R.id.menu_btn:
			toggle();
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		sm.showMenu();
		return true;
	}

	
    @Override
    public void onBackPressed()
    {
        ToastUtil.cancel();

            //“提示再按一次可退出..”
        toastExtrance();
    }
    
    private static long trigleCancel;
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
        	if(GlobalData.homeActivity != null){
        		GlobalData.homeActivity.finish();
        	}
            if(GlobalData.myActivity != null){
            	GlobalData.myActivity.finish();
            }

            finish();
            System.exit(0);
        }
    }
}
