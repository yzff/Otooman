package com.manyanger.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Constants;
import com.manyanger.GlobalData;
import com.manyanger.adapter.BaseListAdapter;
import com.manyanger.adapter.ViewHolder;
import com.manyanger.data.ComicListDataLoader;
import com.manyanger.data.ImageDataLoader.OnIconLoadedListener;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.entries.BaseResponse;
import com.manyanger.entries.ListModel;
import com.manyanger.ui.widget.LoadView;
import com.manyounger.otooman.R;

/**
 * @ClassName: ListActivity
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-8 下午5:38:43
 */
public class ComicListActivity extends Activity implements OnItemClickListener,
		OnDataLoadedListener, OnClickListener {

	private int listId;
	private int listType;

	private FrameLayout contentView;
	private LayoutInflater mLayoutInflater;

	private ComicListDataLoader listLoader;
	private String cmdKey;
	private int state;

	private ListModel listModel;

	private ListView listView;
	private RelativeLayout noml_title;
	private RelativeLayout layout_search;
	private EditText et_search;
	private Button btn_cancel;
	
	private BaseListAdapter listAdapter;
	private String keyword;
	private final int page = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		String title = "";
		title = getIntent().getStringExtra(GlobalData.STR_TITLE);
		listType = getIntent().getIntExtra(GlobalData.STR_LISTTYPE, 0);
		listId = getIntent().getIntExtra(GlobalData.STR_LISTID, 1);
		keyword = getIntent().getStringExtra(GlobalData.STR_KEYWORD);

		TextView titleView = (TextView) findViewById(R.id.title);
		if (titleView != null) {
			titleView.setText(title);
		}

		initView();
		showLoadingView();
		state = GlobalData.STATE_INIT;
		initData();

	}

	/**
     * 
     */
	private void initView() {
		mLayoutInflater = LayoutInflater.from(this);
		ImageView menuBtn = (ImageView) findViewById(R.id.menu_btn);
		if (menuBtn != null) {
			menuBtn.setVisibility(View.GONE);
		}
		ImageView searchBtn = (ImageView) findViewById(R.id.search_btn);
		ImageView back_btn = (ImageView) findViewById(R.id.back_btn);
		back_btn.setVisibility(View.VISIBLE);
		searchBtn
				.setVisibility(listType == Constants.COMIC_LIST_TYPE_SEARCH ? View.GONE
						: View.VISIBLE);
		contentView = (FrameLayout) findViewById(R.id.content_lay);
		
		
		noml_title = (RelativeLayout) findViewById(R.id.noml_title);
		layout_search = (RelativeLayout) findViewById(R.id.layout_search);
		et_search = (EditText) findViewById(R.id.et_search);
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
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		int type = intent.getIntExtra(GlobalData.STR_LISTTYPE, 0);
		int id = intent.getIntExtra(GlobalData.STR_LISTID, 1);
		if(type != Constants.COMIC_LIST_TYPE_SEARCH 
				&& type == this.listType && id == this.listId){
			return;
		}
		
		String title = "";
		title = intent.getStringExtra(GlobalData.STR_TITLE);
		listType = type;
		listId = id;
		keyword = intent.getStringExtra(GlobalData.STR_KEYWORD);

		TextView titleView = (TextView) findViewById(R.id.title);
		if (titleView != null) {
			titleView.setText(title);
		}

		initView();
		showLoadingView();
		state = GlobalData.STATE_INIT;
		initData();
		
	}

	private void showLoadingView() {
		contentView.removeAllViews();
		LoadView viewGroup = new LoadView(this, true);
		contentView.addView(viewGroup);
		viewGroup.setId(1);
	}

	private void showErrorView() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View view = layoutInflater.inflate(R.layout.error, null);
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
	
	private void showEmptyView() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View view = layoutInflater.inflate(R.layout.no_data, null);

		contentView.removeAllViews();
		contentView.addView(view);
	}

	private void showContentView(boolean isEmpty) {
		contentView.removeAllViews();
		if (!isEmpty) {
			listView = (ListView) mLayoutInflater.inflate(R.layout.comic_list,
					null);

//			listAdapter = new BaseListAdapter(this, listModel.getItemList());
			listAdapter = new BaseListAdapter(this, listModel);
			listView.setAdapter(listAdapter);
			listAdapter.setListView(listView);
			listView.setOnItemClickListener(this);
			switch (listType) {
	            case Constants.COMIC_LIST_TYPE_ALL:
	                listAdapter.setLoadCondition(listId, -1, null);
	                break;
	            case Constants.COMIC_LIST_TYPE_THEME:
	                listAdapter.setLoadCondition(-1, listId, null);
	                break;
	            case Constants.COMIC_LIST_TYPE_SEARCH:
	                listAdapter.setLoadCondition(-1, -1, keyword);
	                break;

	            default:
	                break;
	            }
			contentView.addView(listView);
		} else
		{
			showEmptyView();
		}

	}

	private void initData() {
		if (state != GlobalData.STATE_LOADING) {
			state = GlobalData.STATE_LOADING;
			showLoadingView();
			if (listLoader == null) {
				listLoader = new ComicListDataLoader(this);
			}
			switch (listType) {
			case Constants.COMIC_LIST_TYPE_ALL:
				if(listId == -1){
					cmdKey = listLoader.loadAllComicList(page);
				} else {
					cmdKey = listLoader.loadIndexList(listId, page);
				}
				break;
			case Constants.COMIC_LIST_TYPE_THEME:
				cmdKey = listLoader.loadThemeComicList(listId, page);
				break;
			case Constants.COMIC_LIST_TYPE_SEARCH:
				// TODO
				cmdKey = listLoader.search(keyword, page);
				break;

			default:
				break;
			}
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		if (viewHolder == null || viewHolder.mItemModel == null) {
			return;
		}

		BaseComicItem itemInfo = viewHolder.mItemModel;
		Intent intent = new Intent(ComicListActivity.this, DetailActivity.class);
		intent.putExtra("comicId", itemInfo.getId());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

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
					listModel = (ListModel) respone;
					showContentView(listModel.getItemCount()==0);
				} else {
					state = GlobalData.STATE_FAILED;
					showErrorView();

				}
			}

			// listLoader = null;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.search_btn:

			noml_title.setVisibility(View.GONE);
			layout_search.setVisibility(View.VISIBLE);

			break;
		case R.id.iv_delete:
			et_search.setText("");
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
