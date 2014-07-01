package com.manyanger.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 用于ScrollView中解决高度问题
 * @author fred.ma
 *
 */
public class InScrollGridView extends GridView {

	public InScrollGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public InScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
