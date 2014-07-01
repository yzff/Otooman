package com.manyanger.adapter;

import java.util.List;

import com.manyanger.entries.ChapterItem;
import com.manyounger.otooman.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChapterAdapter extends BaseAdapter {

	private List<ChapterItem> chapterList;
	private LayoutInflater mInflater;
	private Context context;

	public ChapterAdapter(Context _context, List<ChapterItem> _chapterList) {
		chapterList = _chapterList;
		context = _context;
		mInflater = LayoutInflater.from(_context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return chapterList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return chapterList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder mViewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.chapter_item, null);
			mViewHolder = new ViewHolder();
			mViewHolder.tv_chapter = (TextView) convertView
					.findViewById(R.id.tv_chapter);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}

		mViewHolder.tv_chapter.setText(chapterList.get(position)
				.getTitle());
		return convertView;
	}

	static class ViewHolder {
		private TextView tv_chapter;
	}

}
