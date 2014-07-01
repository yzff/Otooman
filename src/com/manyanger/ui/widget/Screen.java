package com.manyanger.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Scroller;

import com.manyanger.AppInfo;
import com.manyanger.common.PixValue;
import com.manyanger.ui.widget.Indicator.OnIndicatorChangeListener;
import com.manyounger.otooman.R;

import java.util.ArrayList;
import java.util.List;


public class Screen extends ViewGroup implements OnIndicatorChangeListener {
	public static final int FLAG_BOUNCE = 4;

	public static final int FLAG_CIRCLE = 2;

	public static final int FLAG_SCROLLABLE = 1;

	private static final int SNAP_VELOCITY = 500;

	private static final int TOUCH_MODE_IDLE = 0;

	private static final int TOUCH_MODE_SCROLLING_X = 1;

	private static final int TOUCH_MODE_SCROLLING_Y = 2;

	protected List<OnScreenChangedListener> screenChangedListeners = new ArrayList<Screen.OnScreenChangedListener>();

	private int boundFedernSpace = PixValue.dip.valueOf(64);

	private int flags = FLAG_SCROLLABLE;

	private Scroller mBounceScroller;

	private int mCurScreen = -1;

	private float mLastMotionX;

	private float mLastMotionY;

	/**
	 * switch to the scren index
	 */
	private int mNewScreen;

	private OnBounceBackListener mOnBounceBackListener;

	private OnScrollToListener mOnScrollToListener;

	private final Scroller mScroller;

	private final int mTouchSlop;

	private int mTouchState;

	private VelocityTracker mVelocityTracker;


	public Screen(Context context) {
		this(context, null);
	}

	public Screen(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.sky);
		mScroller = new Scroller(context);
		if (a.getBoolean(R.styleable.sky_bouncable, false)) {
			mBounceScroller = new Scroller(context, new BounceInterpolator());
			setbounceable(true);
		}
		setScrollable(a.getBoolean(R.styleable.sky_scrollable, true));
		setCircle(a.getBoolean(R.styleable.sky_circlable, false));
		setBoundFedernSpace(a.getDimensionPixelSize(
				R.styleable.sky_federnSpace, boundFedernSpace));
		a.recycle();
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}


	public void addIndicator(Indicator indicator) {
		addOnScreenChangedListener(indicator);
	}

	public void addOnScreenChangedListener(OnScreenChangedListener listener) {
		if (null != listener) {
			screenChangedListeners.add(listener);
		}
	}

	public void removeOnScreenChangedListener(OnScreenChangedListener listener) {
		if (null != listener) {
			screenChangedListeners.remove(listener);
		}
	}


	@Override
	public void computeScroll() {
		boolean hasOffset = mScroller.computeScrollOffset();
		if (hasOffset) {
			if (getScrollX() != mScroller.getCurrX()) {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			} else {
				invalidate();
			}
		} else if (isbounceable() && mBounceScroller.computeScrollOffset()) {
			if (getScrollX() != mBounceScroller.getCurrX()) {
				scrollTo(mBounceScroller.getCurrX(), mBounceScroller.getCurrY());
			} else {
				invalidate();
			}
		} else {
			if (mTouchState == TOUCH_MODE_IDLE) {
				notifyScreenChanged();
				setWillNotDraw(true);
			}
		}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float x = ev.getX();
		float y = ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    AppInfo.disAllowIntercept = false;
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			if (AppInfo.disAllowIntercept) {
				float deltaX = Math.abs(x - mLastMotionX);
				if (deltaX > mTouchSlop >> 2
						&& deltaX > Math.abs(y - mLastMotionY)) {
					ev.setAction(0);
					mLastMotionX = x;
					mLastMotionY = y;
					return !super.dispatchTouchEvent(ev);
				}
			}
		}
		try {
			return super.dispatchTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}
	}


	public int getBoundFedernSpace() {
		return boundFedernSpace;
	}


	public int getCurrent() {
		return mCurScreen == -1 ? 0 : mCurScreen;
	}


	public OnBounceBackListener getOnBounceBackListener() {
		return mOnBounceBackListener;
	}


	public OnScrollToListener getOnScrollToListener() {
		return mOnScrollToListener;
	}


	public boolean isbounceable() {
		return 0 != (flags & FLAG_BOUNCE);
	}


	public boolean isCycle() {
		return (flags & FLAG_CIRCLE) != 0 && 1 < getChildCount();
	}


	public boolean isScrollable() {
		return 0 != (flags & FLAG_SCROLLABLE);
	}


	@Override
	public void onIndicatorChanged(int newTabId) {
		if (mCurScreen != newTabId) {
			setCurrent(newTabId);
		}
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		float x = ev.getX();
		float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_MODE_SCROLLING_Y) {
				return false;
			}
			int y_diff = (int) Math.abs(mLastMotionY - y);
			int x_diff = (int) Math.abs(mLastMotionX - x);
			int max = Math.max(x_diff, y_diff);
			if (max > mTouchSlop) {
				if (y_diff == max) {
					mTouchState = TOUCH_MODE_SCROLLING_Y;
				} else {
					mTouchState = TOUCH_MODE_SCROLLING_X;
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = TOUCH_MODE_IDLE;
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
				notifyScreenChanged();
				scrollTo(mNewScreen);
			}
			if (isbounceable() && !mBounceScroller.isFinished()) {
				mBounceScroller.abortAnimation();
				scrollTo(mCurScreen);
			}
			break;
		default:
			mTouchState = TOUCH_MODE_IDLE;
			break;
		}
		return false;
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int count = getChildCount();
		if (null == mVelocityTracker) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		int action = event.getAction();
		int scrollX = getScrollX() % getWidth();
		int cur = getScrollX() / getWidth();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_MOVE: {
			// VLog.v("onTouchEvent onInterceptTouchEvent:" + action
			// + ",scrollX:" + scrollX);
			int deltaX = (int) (mLastMotionX - x);
			if (isCycle() || cur != 0 && cur != count - 1) {
				scrollBy(deltaX, 0);
			} else if (cur == 0) {
				if (x > mLastMotionX)// left
				{
					if (scrollX > -boundFedernSpace) {
						deltaX = Math.max(deltaX, -boundFedernSpace - scrollX);
						scrollBy(deltaX, 0);
					}
				} else if (1 == count && -scrollX < boundFedernSpace)// right
				{
					deltaX = Math.min(deltaX, boundFedernSpace - scrollX);
					scrollBy(deltaX, 0);
				} else {
					scrollBy(deltaX, 0);
				}
			} else if (cur == count - 1) {
				if (x < mLastMotionX) {
					if (scrollX < boundFedernSpace) {
						deltaX = Math.min(deltaX, boundFedernSpace - scrollX);
						scrollBy(deltaX, 0);
					}
				} else {
					scrollBy(deltaX, 0);
				}
			}
			mLastMotionX = x;
			mTouchState = TOUCH_MODE_SCROLLING_X;
			break;
		}
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_MODE_SCROLLING_X) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();
				if (isScrollable()) {
					if (isbounceable()
							&& (cur == 0 && scrollX < 0 || cur == count - 1
									&& scrollX > 0)
							&& scrollX <= boundFedernSpace && !isCycle()) {
						mBounceScroller.startScroll(getScrollX(), 0, -scrollX,
								0, 500);
						invalidate();
						return true;
					}
					if (Math.abs(velocityX) > SNAP_VELOCITY) {
						boolean scrollable = velocityX > 0 && 0 != mCurScreen
								|| velocityX < 0 && mCurScreen != count - 1;
						if (isCycle() || scrollable) {
							scrollTo(mCurScreen + (velocityX > 0 ? -1 : 1));
						} else {
							scrollTo(Math.round(getScrollX() / getWidth()));
						}
					} else {
						scrollTo(Math.round((float) getScrollX() / getWidth()));
					}
				}
				mVelocityTracker.clear();
				mTouchState = TOUCH_MODE_IDLE;
				break;
			}
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_MODE_IDLE;
			break;
		default:
			return !AppInfo.disAllowIntercept;
		}
		return true;
	}


	@Override
	public void scrollBy(int x, int y) {
		if (isScrollable()) {
			if (0 == mCurScreen && x < 0 || getChildCount() - 1 == mCurScreen
					&& x > 0) {
				setWillNotDraw(false);
			}
			super.scrollBy(x, y);
		}
	}


	public void scrollTo(int whichScreen) {
		if (whichScreen == mCurScreen) {
			if (null != mOnBounceBackListener) {
				mOnBounceBackListener.onBounceBack();
			}
		}
		int delta = whichScreen * getWidth() - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0,
				Math.min(500, Math.abs(delta * 2)));
		mNewScreen = whichScreen;
		invalidate();
	}


	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		if (null != mOnScrollToListener) {
			mOnScrollToListener.onScrollTo(x, y);
		}
	}


	public void setbounceable(boolean isbounceable) {
		if (isbounceable) {
			flags |= FLAG_BOUNCE;
		} else {
			flags &= ~FLAG_BOUNCE;
		}
	}


	public void setBoundFedernSpace(int boundFedernSpace) {
		this.boundFedernSpace = boundFedernSpace;
	}


	public void setCurrent(int whichScreen) {
		if (mCurScreen != whichScreen) {
			mNewScreen = whichScreen < 0 ? 0 : whichScreen;
			mTouchState = TOUCH_MODE_IDLE;
			invalidate();
		}
	}


	public void setCircle(boolean isCycle) {
		if (isCycle) {
			flags |= FLAG_CIRCLE;
		} else {
			flags &= ~FLAG_CIRCLE;
		}
	}


	public void setOnBounceBackListener(
			OnBounceBackListener onBounceBackListener) {
		mOnBounceBackListener = onBounceBackListener;
	}


	public void setOnScrollToListener(OnScrollToListener onScrollToListener) {
		mOnScrollToListener = onScrollToListener;
	}


	public void setScrollable(boolean isScrollable) {
		if (isScrollable) {
			flags |= FLAG_SCROLLABLE;
		} else {
			flags &= ~FLAG_SCROLLABLE;
		}
	}


	protected void handleContentIfNeeded(int whichScreen) {
		View v = getChildAt(whichScreen);
		if (null != v && v instanceof AbsContentView) {
			AbsContentView view = (AbsContentView) v;
			view.setRenderable(true);
			view.prepare();
		}
		if (whichScreen != mCurScreen) {
			v = getChildAt(mCurScreen);
			if (null != v && v instanceof AbsContentView) {
				((AbsContentView) v).setRenderable(false);
			}
		}

	}


	@Override
	protected void onDraw(Canvas canvas) {
		if (isCycle()) {
			drawFrieds(canvas);
		}
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int childLeft = getPaddingLeft();
		int childWidth = r - l;
		int childHeight = b - t;
		for (int i = 0; i < childCount; i++) {
			try {
				View childView = getChildAt(i);

				if (childView.getVisibility() != View.GONE) {
					childView.layout(childWidth * i + childLeft,
							getPaddingTop(), childWidth * (i + 1)
									- getPaddingRight(), childHeight
									- getPaddingBottom() - getPaddingTop());
				}
			} catch (Exception e) {
				Log.e(VIEW_LOG_TAG, "Screen onLayout　Error!", e);

				// LogException.setLog(e, "Screen getChildAt　Error!");
				continue;
			}
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
			    try{
			        child.measure(widthMeasureSpec, heightMeasureSpec);
			    }catch(Exception e){
			        e.printStackTrace();
			    }
				
			}
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		scrollTo(w * getCurrent(), 0);
	}


	private int cycle(int whichScreen) {
		if (whichScreen > getChildCount() - 1) {
			whichScreen = 0;
		} else if (whichScreen < 0) {
			whichScreen = getChildCount() - 1;
		}
		return whichScreen;
	}


	private void drawFrieds(Canvas canvas) {
		// Log.i("", "drawFrieds:" + mCurScreen);
		int count = getChildCount();
		if (mCurScreen == 0) {
			canvas.save();
			canvas.translate(-getWidth() * count, 0);
			drawChild(canvas, getChildAt(count - 1), 0);
			canvas.restore();
		} else if (mCurScreen == count - 1) {
			canvas.save();
			canvas.translate(getWidth() * count, 0);
			drawChild(canvas, getChildAt(0), 0);
			canvas.restore();
		}
	}


	private int loadview(int whichScreen) {
		handleContentIfNeeded(whichScreen = cycle(whichScreen));
		return whichScreen;
	}


	private void notifyScreenChanged() {
		if (mCurScreen == mNewScreen) {
			return;
		}
		int old = mCurScreen < 0 ? 0 : mCurScreen;
		mCurScreen = mNewScreen = loadview(mNewScreen);
		onScreenChanged(old);
		scrollTo(mCurScreen * getWidth(), 0);
	}


	private synchronized void onScreenChanged(int old) {

		for (OnScreenChangedListener listener : screenChangedListeners) {
			listener.onScreenChanged(old, mCurScreen);
		}

	}


	public interface OnBounceBackListener {

		void onBounceBack();
	}


	public interface OnScreenChangedListener {

		void onScreenChanged(int srcPosition, int desPosition);

	}


	public interface OnScrollToListener {

		void onScrollTo(int x, int y);
	}

	public interface OnVisibilityChangeListener {
		void onVisibilityChange(boolean visible);
	}

	public interface OnConfigurationChangeListener {
		void onConfigurationChanged();
	}

}
