package com.manyanger.adapter;

import java.util.List;

import com.manyanger.entries.BaseThemeItem;
import com.manyounger.otooman.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CategoryAdapter extends BaseAdapter {
	private List<BaseThemeItem> categorys;
	private LayoutInflater mInflater;
	private int goneLineIndex;
	private int numColumns;

	public CategoryAdapter(Context _context, List<BaseThemeItem> _categorys,
			int _numColumns) {
		categorys = _categorys;
		mInflater = LayoutInflater.from(_context);
		numColumns = _numColumns;
		goneLineIndex = getStartIndex();
	}

	@Override
	public int getCount() {
		return categorys.size();
	}

	@Override
	public Object getItem(int position) {
		return categorys.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.category_item, null);
			mViewHolder = new ViewHolder();
			mViewHolder.tv_title = (TextView) convertView
					.findViewById(R.id.tv_title); 
			mViewHolder.right_line = (LinearLayout) convertView
					.findViewById(R.id.right_line);
			mViewHolder.bottom_line = (LinearLayout) convertView
					.findViewById(R.id.bottom_line);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		mViewHolder.tv_title.setText(categorys.get(position).getTitle());
		setLineShow(mViewHolder, position);
		return convertView;
	}

	static class ViewHolder {
		private TextView tv_title;
		private LinearLayout right_line;
		private LinearLayout bottom_line;
	}

	private void setLineShow(ViewHolder mViewHolder, int position) {

		mViewHolder.right_line.setVisibility((position + 1) % numColumns == 0
				|| position + 1 == getCount() ? View.VISIBLE : View.GONE);
		mViewHolder.bottom_line
				.setVisibility((position + 1) > goneLineIndex ? View.VISIBLE
						: View.GONE);
	}

	private int getStartIndex() {
		int lastNumber = getCount() % numColumns;
		if (lastNumber == 0) {
			return getCount() - numColumns;
		} else
			return ((getCount() / numColumns) * numColumns - (numColumns - lastNumber));
	}

}
