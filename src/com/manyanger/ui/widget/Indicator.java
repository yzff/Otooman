/**
 * 
 */
package com.manyanger.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.manyanger.common.Config;
import com.manyanger.ui.widget.Screen.OnScreenChangedListener;




public abstract class Indicator extends LinearLayout implements
		View.OnClickListener, OnScreenChangedListener {
	protected int mTextColor;

	protected int selector; // R.drawable.childtab_bg_selector;

	private int divider;

	private int dividerWidth;

	protected int idx;

	private OnIndicatorChangeListener onIndicatorChangeListener;

	private final int DEVIDER_WIDTH = 3;


	public Indicator(Context context) {
		this(context, null);
	}


	public Indicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		setGravity(Gravity.CENTER);
	}


	public void addItem(View item) {
		addItem(item, null, null);
	}


	public void addItem(View item, ImageView separator) {
		addItem(item, separator, null);
	}


	public void addItem(View item, ImageView separator, LayoutParams paras) {
		if (null == paras) {
			if (null == (paras = (LayoutParams) item.getLayoutParams())) {
				paras = preferredLayoutParams(-1);
			}
			paras.gravity = Gravity.CENTER;
		}
		item.setTag(idx++);
		item.setOnClickListener(this);
		addView(item, paras);
		if (null != separator) {
			addView(separator, dividerWidth, -1);
		}

	}


	public void addItem(View item, LayoutParams paras) {
		addItem(item, null, paras);
	}


	public abstract void createContent();


	public final int getCount() {
		return idx;
	}


	public int getDividerWidth() {
		return 0 == divider ? 0 : dividerWidth;
	}


	public int getOffset(int idx) {
		return idx * (getUnitWidth() + getDividerWidth());
	}


	public OnIndicatorChangeListener getOnIndicatorChangeListener() {
		return onIndicatorChangeListener;
	}


	@Override
	public void onClick(View v) {
		if (null != onIndicatorChangeListener) {
			onIndicatorChangeListener.onIndicatorChanged((Integer) v.getTag());
		}
	}


	@Override
	public void removeAllViews() {
		super.removeAllViews();
		idx = 0;
	}


	public void setDivider(int resId) {
		divider = resId;
	}


	public void setOnIndicatorChangeListener(
			OnIndicatorChangeListener onIndicatorChangeListener) {
		this.onIndicatorChangeListener = onIndicatorChangeListener;
	}

	public void setSelector(int selector) {
		this.selector = selector;
	}


	public void setTextColor(int colorRes) {
		mTextColor = colorRes;
	}


	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		if (null != p) {
			return super.generateLayoutParams(p);
		}
		return generateDefaultLayoutParams();
	}


	protected ImageView getDivider() {
		if (0 == divider) {
			return null;
		}
		ImageView iv = new ImageView(getContext());

		iv.setBackgroundResource(divider);
		// iv.setImageResource(divider);
		iv.setLayoutParams(preferredLayoutParams(dividerWidth = DEVIDER_WIDTH));
		return iv;
	}


	public int getUnitWidth() {
		return Math
				.round((Config.getWidth() - dividerWidth * (idx - 1f)) / idx);
	}


	private LayoutParams preferredLayoutParams(int width) {
		return new LayoutParams(width, -1, 1);
	}

	public interface OnIndicatorChangeListener {
		void onIndicatorChanged(int newTabId);
	}

}
