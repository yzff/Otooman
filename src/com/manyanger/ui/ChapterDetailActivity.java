package com.manyanger.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.manyanger.GlobalData;
import com.manyanger.adapter.ChapterAdapter;
import com.manyanger.data.ChapterDataLoader;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.entries.ChapterItem;
import com.manyanger.ui.widget.TipDialog;
import com.manyounger.otooman.R;

import java.util.List;

public class ChapterDetailActivity extends Activity implements
		OnDataLoadedListener, OnItemClickListener, OnClickListener {
	private ChapterDataLoader chapterLoader;

	private GridView gv_chapter;
	private ChapterAdapter mChapterAdapter;
	private List<ChapterItem> chapterList;
	private int mComicId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_chapter);
		initView();
		chapterLoader = new ChapterDataLoader(this);

		mComicId = getIntent().getIntExtra(GlobalData.STR_COMICID, -1);
		if (mComicId != -1) {
			TipDialog.showWait(this, null);
			chapterLoader.loadChapterList(mComicId);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);

		int comicId = intent.getIntExtra(GlobalData.STR_COMICID, -1);
		if (comicId != -1 && comicId != this.mComicId) {
			this.mComicId = comicId;
			mChapterAdapter = null;
			gv_chapter.setAdapter(null);
			TipDialog.showWait(this, null);
			chapterLoader.loadChapterList(comicId);
		}
	}
	
	/**
     * 
     */
	private void initView() {
		gv_chapter = (GridView) findViewById(R.id.gv_chapter);
		gv_chapter.setOnItemClickListener(this);
	}

	@SuppressWarnings("unchecked")
    @Override
	public void OnDataLoaded(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case GlobalData.NOTIFY_COMICDETAIL_LOADED:
			// TODO 注意为空,到LODER那边做处理

			chapterList = (List<ChapterItem>) msg.obj;
			if (chapterList != null) {
				mChapterAdapter = new ChapterAdapter(
						ChapterDetailActivity.this, chapterList);
				gv_chapter.setAdapter(mChapterAdapter);
			}
			TipDialog.dissmissTip();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	        long id) {
	      try{
	            ChapterItem item = chapterList.get(position);
	            if(item != null){
	                Intent intent = new Intent(ChapterDetailActivity.this, ComicReaderActivity.class);
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
		case R.id.back_btn:
			finish();
			break;

		default:
			break;
		}
	}
	
    @Override
    public void onBackPressed()
    {
    	finish();
    }
}
