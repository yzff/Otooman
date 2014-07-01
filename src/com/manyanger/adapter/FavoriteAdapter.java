package com.manyanger.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.manyanger.GlobalData;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.AppUtil;
import com.manyanger.data.ImageDataLoader;
import com.manyanger.data.ImageDataLoader.OnIconLoadedListener;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.provider.OtooInfo;
import com.manyounger.otooman.R;

import java.util.ArrayList;

public class FavoriteAdapter extends CursorAdapter implements
		OnIconLoadedListener {
	private final Context context;
	private final LayoutInflater mInflater;
	private final ListView mListView;
	boolean edit = false;
	/** 选择的数据 */
	private final ArrayList<Integer> selectList = new ArrayList<Integer>();
	private final ImageCache mImageCache = ImageCache.getInstance();
	private final ImageDataLoader imageLoader = new ImageDataLoader(this,
			mImageCache);

	public FavoriteAdapter(Context _context, Cursor c, ListView _mListView) {
		super(_context, c);
		context = _context;
		mInflater = LayoutInflater.from(_context);
		mListView = _mListView;
	}

	public void setEdit(boolean _edit) {
		edit = _edit;
	}

	public void checkAll() {
		final Cursor cursor = getCursor();
		if (cursor != null) {
			final int position_temp = cursor.getPosition();
			selectList.clear();
			final int idIndex = cursor.getColumnIndex(OtooInfo.favorite._ID);
			if (cursor.moveToFirst()) {
				do {
					final int id = cursor.getInt(idIndex);
					selectList.add(id);
				} while (cursor.moveToNext());
			}
			cursor.moveToPosition(position_temp);
			notifyDataSetChanged();
		}
	}

	public void cancelAll() {
		selectList.clear();
		notifyDataSetChanged();
	}

	public ArrayList<Integer> getSelectList() {
		return selectList;
	}

	public static class ViewHolder {
		private ImageView img_icon;
		private TextView item_label;
		private TextView item_auth;
		private TextView item_chapter;
		private TextView tv_process;
		private TextView btn_red;
		private CheckBox checkbox;

		public BaseComicItem baseComicItem;

	}

	@Override
	public void bindView(View convertView, Context arg1, Cursor cursor) {
		final int book_id = cursor.getInt(cursor
				.getColumnIndex(OtooInfo.favorite.COL_BOOK_ID));
		final int id = cursor.getInt(cursor
				.getColumnIndex(OtooInfo.favorite._ID));
		final String name = cursor.getString(cursor
				.getColumnIndex(OtooInfo.favorite.COL_NAME));
		final String auth = cursor.getString(cursor
				.getColumnIndex(OtooInfo.favorite.COL_AUTHOR));
		final String cover_url = cursor.getString(cursor
				.getColumnIndex(OtooInfo.favorite.COL_COVER));
		final int chapter = cursor.getInt(cursor
				.getColumnIndex(OtooInfo.favorite.COL_CHAPTER_COUT));
		final String process = cursor.getString(cursor
				.getColumnIndex(OtooInfo.favorite.COL_PROCESS));

		final BaseComicItem baseComicItem = new BaseComicItem(book_id, name);
		baseComicItem.setAuthor(auth);
		baseComicItem.setChapter(chapter);
		baseComicItem.setCoverUrl(cover_url);

		final ViewHolder mViewHolder = (ViewHolder) convertView.getTag();

		mViewHolder.item_label.setText(name);
		mViewHolder.item_auth.setText(auth);
		mViewHolder.item_chapter.setText(String.format(
				context.getString(R.string.update_chapter), chapter));
//		mViewHolder.tv_process.setText(process);

		final String key = baseComicItem.getCoverIconKey();
		Bitmap bitmap = mImageCache.get(key);
		if (bitmap != null && !bitmap.isRecycled()) {
			mViewHolder.img_icon.setImageDrawable(new BitmapDrawable(bitmap));
		} else {
			mViewHolder.img_icon.setBackgroundDrawable(AppUtil
					.getDefaultIconBitmap());
			mViewHolder.img_icon.setImageDrawable(null);
			if (baseComicItem.getCoverState() != BaseComicItem.ICON_STATE_LOADING) {
				imageLoader.loadCoverImage(baseComicItem);
			}
		}

		mViewHolder.checkbox.setChecked(selectList.contains(id));
		mViewHolder.checkbox.setVisibility(edit ? View.VISIBLE : View.GONE);
		mViewHolder.btn_red.setVisibility(edit ? View.GONE : View.VISIBLE);
		mViewHolder.checkbox.setTag(id);
		mViewHolder.baseComicItem = baseComicItem;
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		final View convertView = mInflater
				.inflate(R.layout.favorite_item, null);
		final ViewHolder mViewHolder = new ViewHolder();
		mViewHolder.img_icon = (ImageView) convertView
				.findViewById(R.id.img_icon);
		mViewHolder.item_label = (TextView) convertView
				.findViewById(R.id.item_label);
		mViewHolder.item_auth = (TextView) convertView
				.findViewById(R.id.item_auth);
		mViewHolder.item_chapter = (TextView) convertView
				.findViewById(R.id.item_chapter);
//		mViewHolder.tv_process = (TextView) convertView
//				.findViewById(R.id.tv_process);
		mViewHolder.btn_red = (TextView) convertView.findViewById(R.id.btn_red);
		mViewHolder.checkbox = (CheckBox) convertView
				.findViewById(R.id.checkbox);
		mViewHolder.checkbox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Integer id = (Integer) v.getTag();
				if (selectList.contains(id))
					selectList.remove(id);
				else {
					selectList.add(id);
				}
			}
		});
		convertView.setTag(mViewHolder);
		return convertView;
	}

	@Override
	public void OnAppIconLoaded(Message msg) {
		if (msg.what == GlobalData.NOTIFY_COMICCOVER_LOADED) {
			int id = msg.arg1;
//			updateAppIcon(getViewPosById(id));
		}
	}


}
