package com.manyanger.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.manyanger.GlobalData;
import com.manyanger.adapter.FavoriteAdapter;
import com.manyanger.common.ToastUtil;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.provider.OtooInfo;
import com.manyounger.otooman.R;

import java.util.ArrayList;

public class MyComicActivity extends SlidingFragmentActivity implements OnItemClickListener,
		OnClickListener {
	private ListView lv_comic;
	private TextView tv_editor;
	private LinearLayout line_eidt;
	private FavoriteAdapter mFavoriteAdapter;
	private boolean editor = false;
	private boolean checkAll = true;// 全选按钮状态
	private Button btn_checkall;// 全选按钮状态
	
	private SlidingMenu sm;
	protected ListFragment mFrag;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GlobalData.myActivity = this;
		
		setContentView(R.layout.shelf);
		
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
//				sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				
				initMenuView();
				
		initView();

		final Cursor mCursor = getContentResolver().query(
				OtooInfo.favorite.CONTENT_URI, null, null, null, null);
		mFavoriteAdapter = new FavoriteAdapter(this, mCursor, lv_comic);
		lv_comic.setAdapter(mFavoriteAdapter);

	}

	private void initView() {
		lv_comic = (ListView) findViewById(R.id.lv_comic);
		tv_editor = (TextView) findViewById(R.id.tv_editor);
		lv_comic.setOnItemClickListener(this);
		line_eidt = (LinearLayout) findViewById(R.id.line_eidt);
		btn_checkall = (Button) findViewById(R.id.btn_checkall);
	}
	
	private void initMenuView() {
		View mainBtn = findViewById(R.id.btn_main);
		if (mainBtn != null) {
			mainBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					Intent intent = new Intent(MyComicActivity.this,
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
					sm.showContent();
					
				}

			});
		}

		View catoryBtn = findViewById(R.id.btn_catory);
		if (catoryBtn != null) {
			catoryBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(MyComicActivity.this,
							CategoryActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					sm.showContent();
//					finish();

				}

			});
		}

	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO 进阅读页面
		final BaseComicItem itemInfo = ((FavoriteAdapter.ViewHolder) view
				.getTag()).baseComicItem;
		
		if(itemInfo != null){
	        Intent intent = new Intent(MyComicActivity.this, DetailActivity.class);
	        intent.putExtra("comicId", itemInfo.getId());
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(intent);
	    }
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
		case R.id.btn_checkall:
			if (checkAll) {
				mFavoriteAdapter.checkAll();
				checkAll = false;
				btn_checkall.setText(R.string.check_all_cancel);
			} else {
				mFavoriteAdapter.cancelAll();
				checkAll = true;
				btn_checkall.setText(R.string.check_all);
			}

			break;
		case R.id.btn_delete:
			final ArrayList<Integer> selectList = mFavoriteAdapter
					.getSelectList();
			if (selectList.size() != 0) {
				final StringBuffer ids = new StringBuffer();
				ids.append("(");
				for (Integer id : selectList) {
					ids.append(id + ",");
				}
				ids.delete(ids.length() - 1, ids.length());
				ids.append(")");
				getContentResolver().delete(OtooInfo.favorite.CONTENT_URI,
						OtooInfo.favorite._ID + " in " + ids.toString(), null);
				Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
				mFavoriteAdapter.notifyDataSetChanged();
				uptaeEdit();
			}else
				Toast.makeText(this, "未选择漫画", Toast.LENGTH_SHORT).show();
			break;
		case R.id.tv_editor:
			uptaeEdit();
			break;

		default:
			break;
		}
	}

	private void uptaeEdit() {
		if (!editor) {
			line_eidt.setVisibility(View.VISIBLE);
			tv_editor.setText(R.string.conmm_cancel);
		} else {
			line_eidt.setVisibility(View.GONE);
			tv_editor.setText(R.string.editor);
		}
		editor = !editor;
		mFavoriteAdapter.setEdit(editor);
		mFavoriteAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		sm.showMenu();
		return true;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
       	GlobalData.myActivity = null;
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
            if(GlobalData.cateActivity != null){
            	GlobalData.cateActivity.finish();
            }
            finish();
            System.exit(0);
        }
    }
}
