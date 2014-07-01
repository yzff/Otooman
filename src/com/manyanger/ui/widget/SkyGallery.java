
package com.manyanger.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Scroller;

import com.manyanger.AppInfo;
import com.manyanger.cache.ImageCache;
import com.manyanger.common.Config;
import com.manyanger.ui.widget.Screen.OnConfigurationChangeListener;
import com.manyounger.otooman.R;



public class SkyGallery extends ViewGroup implements
        Screen.OnVisibilityChangeListener, OnConfigurationChangeListener {

    public static final int FLAG_CIRCLE = 0x1;

    public static final int FLAG_SLOT = 0x2;

	private static final String TAG = "SkyGallery";
    

    protected GalleryAdapter mGalleryAdapter;

    protected int mItemCount;

    private final int mAnimationDuration;

    private int mScrollState;

    public int getScrollState() {
        return mScrollState;
    }

    private ImageCache imageCache = null;

    public void setImageCache(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    protected int getAdjustOffset() {
        return startOffset < 0 ? -1 : startOffset > 0 ? 1 : 0;
    }

    public void update() {
        if (null == mGalleryAdapter) {
            return;
        }

        int total = getChildCount();
        for (int i = 0; i < total; i++) {
            View v = getChildAt(i);
            if (null != v && v instanceof ImageView) {
                Object o = mGalleryAdapter.getItem((i + mFirstPosition)
                        % mItemCount);
                String key = ""; 
                // TODO:

                if (imageCache == null) {
                    return;
                }

                Bitmap bitmap = imageCache.get(key);

                if (null != bitmap && !bitmap.isRecycled()) {
                    ImageView iv = (ImageView) v;

                    iv.setBackgroundDrawable(new BitmapDrawable(bitmap));

                }
            }
        }
    }

    public void setScrollState(int scrollState) {
        mScrollState = scrollState;
    }

    private final Context mContext;

    private int mCurPosition = 0;

    private boolean mDataChanged;

    private int mFirstPosition;

    private int mFlag;

    private final FlingRunnable mFlingRunnable = new FlingRunnable();

    private GalleryPageSwitchListener mGalleryPageSwitchListener;

    private final GestureDetector mGestureDetector;

    private int mHeightMeasureSpec;

    private float mLastMotionX;

    private float mLastMotionY;

    private final int mLastPosition = 0;

    private int mLeft;

    private int mLeftMost;

    private OnItemClickListener mListener;

    private int mPaddingLeft;

    private int mPaddingRight;

    private View mPressed;

    private final RecycleBin mRecycler = new RecycleBin();

    private int mRight;

    private int mRightMost;

    private boolean mShouldStopFling;

    private int mSpacing;

    private final int mTouchSlop;

    private int mWidthMeasureSpec;

    int mStatus;

    public SkyGallery(Context context) {
        this(context, null);
    }

    @SuppressWarnings("deprecation")
    public SkyGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStaticTransformationsEnabled(false);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SkyGallery);
        mAnimationDuration = a.getInt(R.styleable.SkyGallery_animationDuration,
                500);
        mSpacing = a.getDimensionPixelOffset(R.styleable.SkyGallery_space, 0);
        setCircle(a.getBoolean(R.styleable.SkyGallery_circle, true));
        setSlot(a.getBoolean(R.styleable.SkyGallery_slot, true));
        mContext = context;
        mGestureDetector = new GestureDetector(new MyDetector());
        mGestureDetector.setIsLongpressEnabled(false);

        a.recycle();
        setCircle(true);
        setSlot(true);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

//        if (context instanceof Activity) {
//            Activity base = (Activity) context;
//            base.addOnConfigurationChangedListener(this);
//        }
    }

    public void setPaddingAndSpace(int space, int paddingLeft, int paddingRight)
    {
        mSpacing = space;
        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
    }

    private int getPOffset() {
        if (getContext() instanceof Activity) {
            Activity base = (Activity) getContext();
            ViewGroup g = (ViewGroup) base.getWindow().getDecorView();
            Rect pRect = new Rect();
            g.offsetDescendantRectToMyCoords(this, pRect);
            return pRect.left;
        }
        return 0;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
                holdOnTouchEvent(true);
                mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = Math.abs(mLastMotionY - ev.getY());
                float dx = Math.abs(mLastMotionX - ev.getX());
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
                if (dy > dx && dy > mTouchSlop / 3) {
                    clearSelection();
                    holdOnTouchEvent(false);
                    AppInfo.disAllowIntercept = true;
                    mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
                    // if (isCircle())
                    {
                        scrollIntoSlots();
                    }
                    return false;
                }
        }
        return super.dispatchTouchEvent(ev);
    }

    public int getFirstVisiblePosition() {
        return mFirstPosition;
    }

    public int getlastVisiblePosition() {
        return mLastPosition;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                clearSelection();
                if (mFlingRunnable.mScroller.isFinished()) {
                    mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
                    return scrollIntoSlots();
                }
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    public void setAdapter(GalleryAdapter galleryAdapter) {
        mGalleryAdapter = galleryAdapter;
        mItemCount = galleryAdapter.getCount();
        galleryAdapter.setSkyGallery(this);
        configGalleryParams();
        requestLayout();
        invalidate();
    }

    private volatile boolean isAutoScrolling = true;

    public boolean isAutoScrolling() {

//        return ((NetStateEvent) BusProvider.get().getStickyEvent(NetStateEvent.class)).isWifi
//                && isCircle() && isAutoScrolling;
        return isCircle() && isAutoScrolling;
    }

    public void setAutoScrolling(boolean isAutoScrolling) {
        this.isAutoScrolling = isAutoScrolling;
    }

    private final Runnable autoScroller = new Runnable() {
        @Override
        public void run() {
            if (isAutoScrolling()) {
                int count = getCapacity();
                if (mItemCount > count
                        && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    scrollOneScreen();
                }
                postDelayed(this, 10000);
            }
        }
    };

    public void startScrolling() {
        isAutoScrolling = true;
        removeCallbacks(autoScroller);
        mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
        postDelayed(autoScroller, 10000);
    }

    public void pauseScrolling() {
        isAutoScrolling = false;
    }

    public void setCircle(boolean circle) {
        if (circle) {
            mFlag |= FLAG_CIRCLE;
        } else {
            mFlag &= ~FLAG_CIRCLE;
        }
    }

    public boolean isCircle() {
        return 0 != (mFlag & FLAG_CIRCLE);
    }

    public void setGalleryPageSwitchListener(GalleryPageSwitchListener listener) {
        mGalleryPageSwitchListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setSlot(boolean slot) {
        if (slot) {
            mFlag |= FLAG_SLOT;
        } else {
            mFlag &= ~FLAG_SLOT;
        }
    }

    private boolean mDiscipline = true;

    public void setDiscipline(boolean discipline) {
        mDiscipline = discipline;
    }

    private int mSelection;

    private int startOffset;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        changed = changed | mDataChanged;
        if (!changed || 0 == mItemCount) {
            return;
        }

        if (mGalleryAdapter == null) {
            return;
        }

        Log.i(TAG, "onLayout");
        recycleAllViews();

        detachAllViewsFromParent();

        r = r - l;
        int _w = mGalleryAdapter.getBitmapWidth();// v.getMeasuredWidth();
        int childcount = r / _w;
        if (childcount > mItemCount) {
            childcount = mItemCount;
        }
        if (childcount < 1)
        {
            childcount = 1;
        }
        //        if (mDiscipline)
        //        {
        //            //            int allSpace = r - _w * childcount;
        //            //            mSpacing = Math.round(allSpace / (childcount + 1f));
        //            //            //            needAdjust = allSpace % (childcount + 1) > (childcount + 1) / 2;
        //            //            mPaddingLeft = l == 0 ? mSpacing : 0;// - getPOffset();
        //            //            mPaddingRight = mSpacing;
        //            mSpacing = 0;
        //            mPaddingLeft = 0;
        //            mPaddingRight = 0;
        //        }
        //        else
        //        {
        //            mSpacing = PixValue.dip.valueOf(20);
        //            if (0 == (mPaddingLeft = getPaddingLeft()))
        //            {
        //                mPaddingLeft = mSpacing / 2;
        //            }
        //            if (0 == (mPaddingRight = getPaddingRight()))
        //            {
        //                mPaddingRight = mSpacing / 2;
        //            }
        //        }
        int left = mPaddingLeft + startOffset;
        View v = null;
//        for (int i = 0; i < childcount || !mDiscipline && null != v
//                && v.getRight() + mPaddingRight < r; i++) {
        for (int i = 0; i < childcount && (left < r); i++) {
            v = makeAndAddView(escape(mFirstPosition + i), getPaddingTop(),
                    left, true);
            left += _w + mSpacing;
        }
        if (null != mGalleryPageSwitchListener)// set default or configuration
                                               // changed
        {
            mGalleryPageSwitchListener.onPageChanged(mSelection);
        }
        mDataChanged = false;
    }

    void recycleAllViews() {
        int childCount = getChildCount();
        RecycleBin recycleBin = mRecycler;

        // All views go in recycler
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            int index = mFirstPosition + i;
            recycleBin.put(index, v);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize;
        int heightSize;

        int preferredHeight = 0;
        int preferredWidth = 0;
        boolean needsMeasuring = true;


        if (mFirstPosition >= 0 && mGalleryAdapter != null
                && mFirstPosition < mItemCount) {
            // Try looking in the recycler. (Maybe we were measured once
            // already)
            View view = mRecycler.get(mFirstPosition);
            if (view == null) {
                // Make a new one
                view = mGalleryAdapter.getView(mFirstPosition, null, this);
                mRecycler.put(mFirstPosition, view);
                // Put in recycler for re-measuring and/or layout
            } else {
                view = mGalleryAdapter.getView(mFirstPosition, view, this);
            }

            // 如果发生了，表明item没有create成功
            if (view == null) {
                return;
            }
            if (view.getLayoutParams() == null) {
                view.setLayoutParams(generateDefaultLayoutParams());
            }
            measureChild(view, widthMeasureSpec, heightMeasureSpec);

            preferredHeight = getChildHeight(view);// + mSpinnerPadding.top
                                                   // +
                                                   // mSpinnerPadding.bottom;
            preferredWidth = getChildWidth(view);// + mSpinnerPadding.left +
                                                 // mSpinnerPadding.right;

            needsMeasuring = false;
        }

        heightSize = resolveSize(preferredHeight, heightMeasureSpec);
        widthSize = resolveSize(preferredWidth, widthMeasureSpec);

        setMeasuredDimension(mWidthMeasureSpec = widthSize + getPaddingLeft()
                + getPaddingRight(), mHeightMeasureSpec = heightSize
                + getPaddingBottom() + getPaddingTop());
    }

    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        final int firstPosition = mFirstPosition;
        int start = 0;
        int count = 0;

        if (toLeft) {
            final int galleryLeft = mPaddingLeft;
            for (int i = 0; i < numChildren; i++) {
                final View child = getChildAt(i);
                if (child.getRight() >= galleryLeft) {
                    break;
                } else {
                    count++;
                    mRecycler.put((firstPosition + i) % mItemCount, child);
                }
            }
        } else {
            final int galleryRight = getWidth() - mPaddingRight;
            for (int i = numChildren - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                if (child.getLeft() <= galleryRight) {
                    break;
                } else {
                    start = i;
                    count++;
                    mRecycler.put((firstPosition + i) % mItemCount, child);
                }
            }
        }

        detachViewsFromParent(start, count);

        if (toLeft) {
            mFirstPosition += count;
            if (mFirstPosition >= mItemCount) {
                mFirstPosition %= mItemCount;
            }
        }
    }

    private void fillToGalleryLeft() {
        int itemSpacing = mSpacing;
        int galleryLeft = mPaddingLeft;

        if (!isCircle()) {
            if (mFirstPosition <= 0) {
                return;
            }
        }

        // Set state for initial iteration
        View prevIterationView = getChildAt(0);
        int curPosition;
        int curRightEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            // No children available!
            curPosition = 0;
            curRightEdge = mRight - mLeft - mPaddingRight;
            mShouldStopFling = true;
            mFlingRunnable.stop(false);
        }

        while (curRightEdge > galleryLeft) {

            prevIterationView = makeAndAddView(curPosition, getPaddingTop(),
                    curRightEdge, false);
            mFirstPosition = curPosition;
            // Remember some state

            // Set state for next iteration
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
        if (mFirstPosition < 0) {
            mFirstPosition += mItemCount;
        }
        mCurPosition = curPosition;
    }

    private void fillToGalleryRight() {
        if (!isCircle()) {
            if (mFirstPosition + getChildCount() >= mItemCount) {
                return;
            }
        }

        int itemSpacing = mSpacing;
        // TODO:
        int galleryRight = getRight() - getLeft() - mPaddingRight;
        int numChildren = getChildCount();
        int numItems = mItemCount;

        // Set state for initial iteration
        View prevIterationView = getChildAt(numChildren - 1);
        int curPosition;
        int curLeftEdge;

        if (prevIterationView != null) {
            curPosition = mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            mFirstPosition = curPosition = mItemCount - 1;
            curLeftEdge = mPaddingLeft;
            mShouldStopFling = true;
            mFlingRunnable.stop(false);
        }

        while (curLeftEdge < galleryRight) {
            // Log.v(TAG, "fillToGalleryRight position = curL" + curLeftEdge
            // + ",r-" + galleryRight);
            prevIterationView = makeAndAddView(curPosition, getPaddingTop(),
                    curLeftEdge, true);

            // Set state for next iteration
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition++;
        }
        mCurPosition = curPosition;
    }


    private View findView(MotionEvent event) {
        final int count = getChildCount();
        int i;
        int x = (int) event.getX();
        View v = null;
        for (i = 0; i < count; i++) {
            v = getChildAt(i);

            if (v.getLeft() <= x && v.getRight() > x) {
                break;
            }
        }
        if (i < count) {
            return v;
        }
        return null;
    }

    private int getChildHeight(View child) {
        return null != mGalleryAdapter ? mGalleryAdapter.getBitmapHeight()
                : child.getMeasuredHeight();
    }

    private int getChildWidth(View child) {
        return null != mGalleryAdapter ? mGalleryAdapter.getBitmapWidth()
                : child.getMeasuredWidth();
    }


    private boolean holdOnTouchEvent(boolean on) {
        getParent().requestDisallowInterceptTouchEvent(on);
        return on;
    }

    private View makeAndAddView(int position, int offset, int x,
            boolean fromLeft) {
        position = escape(position);

        View child;

        if (!mDataChanged) {
            child = mRecycler.get(position);
            if (child != null) {
                mGalleryAdapter.getView(position, child, this);
                // Can reuse an existing view
                int childLeft = child.getLeft();

                // Remember left and right edges of where views have been placed
                mRightMost = Math.max(mRightMost,
                        childLeft + child.getMeasuredWidth());
                mLeftMost = Math.min(mLeftMost, childLeft);

                // Position the view
                setUpChild(child, offset, x, fromLeft);

                return child;
            }
        }

        // Nothing found in the recycler -- ask the adapter for a view
        child = mGalleryAdapter.getView(position, null, this);
        mRecycler.put(position, child);
        // Position the view
        setUpChild(child, offset, x, fromLeft);
//        child.setTag(R.id.indicator, position);
        return child;
    }


    private int escape(int position) {
        if (position < 0) {
            position += mItemCount;
        } else if (position >= mItemCount) {
            position = position % mItemCount;
        }
        return position;
    }

    private void offsetChildrenLeftAndRight(int offset) {

        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);

            v.offsetLeftAndRight(offset);
        }

    }

    private void onFinishedMovement() {

        mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
        mShouldStopFling = true;
    }


    private boolean scrollIntoSlots(boolean... toLeft) {
        if (getChildCount() == 0
                || mScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            return true;
        }
//        if(getChildCount() <= getCapacity()){
        if(!checkScrolls_2()){
        	return true;
        }

        final View v = getChildAt(0);
        final int firstLeft = v.getLeft() - mPaddingLeft;
        final int firstRight = v.getRight();
        int scrollAmount = 0;
        if (2 < Math.abs(firstLeft - startOffset)) {
            boolean left = Math.abs(firstLeft) > firstRight;
            if (null != toLeft && 0 != toLeft.length) {
                left = toLeft[0];
            }
            scrollAmount = left ? firstRight - startOffset
                    + (mDiscipline ? 0 : mSpacing) : firstLeft
                    + getAdjustOffset() * mSpacing + startOffset;
        }

        if (scrollAmount != 0) {
            mFlingRunnable.startUsingDistance(-scrollAmount);
            return false;
        }
        onFinishedMovement();
        return true;
    }

    private void setUpChild(View child, int offset, int x, boolean fromLeft) {

        // Respect layout params that are already in the view. Otherwise
        // make some up...
        LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = generateDefaultLayoutParams();
        }

        addViewInLayout(child, fromLeft ? -1 : 0, lp);

        // Get measure specs
        int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
                offset * 2, lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
                mPaddingLeft + mPaddingRight, lp.width);

        // Measure child
        child.measure(childWidthSpec, childHeightSpec);

        int childLeft;
        int childRight;

        // Position vertically based on gravity setting
        int childTop = offset;
        int childBottom = childTop + child.getMeasuredHeight();// +
        // mGalleryAdapter.getBitmapHeight();

        int width = child.getMeasuredWidth();// mGalleryAdapter.getBitmapWidth();//
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }
        child.layout(childLeft, childTop, childRight, childBottom);
    }

    int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
        View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

        if (extremeChild == null) {
            return deltaX;
        }
        int maxMargin = mSpacing * 3 / 2;

        return motionToLeft ? Math.max(-extremeChild.getRight() + getWidth()
                - maxMargin, deltaX) : Math.min(-extremeChild.getLeft()
                + maxMargin, deltaX);
    }


    private boolean checkScrolls() {
        return mItemCount > getCapacity();
    }

    private boolean checkScrolls_2() {
        return isCircle() || (mItemCount > getCapacity());
    }
    
    private int getCapacity() {
    	try{
    		return Config.getWidth() / mGalleryAdapter.getBitmapWidth();
    	}catch(Exception e){
    		return 1;
    	}
    }


    private void clearSelection() {
        if (null != mPressed) {
            mPressed.setSelected(false);
            mPressed = null;
            invalidate();
        }
    }

    boolean trackMotionScroll(int deltaX) {
        boolean more = true;
        if (getChildCount() == 0) {
            return false;
        }
//        int count = getChildCount();
//        int capacity = getCapacity();
//        Log.i(TAG, "trackMotionScroll count:"+count+" cap:"+capacity);
//        if (getChildCount() <= getCapacity()) {
        if(!checkScrolls_2()){
            return false;
        }
        
        boolean toLeft = deltaX < 0;

        if (!isCircle() || !checkScrolls()) {
            deltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
            // more = false;
        } else {
            /*
             * toLeft ? Math.min(0, Math.max(space - getChildAt(0).getLeft(),
             * deltaX)) : Math .max( 0, Math.min(deltaX, getRight() - space -
             * getChildAt(getChildCount() - 1).getRight()));
             */
        }

        offsetChildrenLeftAndRight(deltaX);

        detachOffScreenChildren(toLeft);

        if (toLeft) {
            // If moved left, there will be empty space on the right
            fillToGalleryRight();
        } else {
            // Similarly, empty space on the left
            fillToGalleryLeft();
        }
        // Clear unused views
        // mRecycler.clear();
        checkSelection(deltaX);
        invalidate();
        return more;
    }

 
    private void checkSelection(int offset) {
        View v = getChildAt(escape(mSelection - mFirstPosition));
        if (null == v) {
            mSelection = escape(mFirstPosition);
        } else if (null != mGalleryPageSwitchListener) {
            int left = v.getLeft() - mSpacing, pivot = mGalleryAdapter
                    .getBitmapWidth() / 2 + mSpacing;
            if (left <= -pivot && left - offset > -pivot || left <= pivot
                    && left - offset > pivot) {
                mSelection = escape(mSelection + 1);
            } else if (left >= pivot && left - offset < pivot || left >= -pivot
                    && left - offset < -pivot) {
                mSelection = escape(mSelection - 1);
            }
        }
        onPageChanged();
    }


    private void onPageChanged() {
        if (null != mGalleryPageSwitchListener) {
            mGalleryPageSwitchListener.onPageChanged(mSelection);
        }
    }

    public interface GalleryPageSwitchListener {
        void onPageChanged(int pageIndex);
    }

    private class FlingRunnable implements Runnable {
        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX = 0;

        /**
         * Tracks the decay of a fling scroll
         */
        private final Scroller mScroller;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        @Override
        public void run() {

            if (mItemCount == 0) {
                // endFling((mFlag & FLAG_SLOT) == FLAG_SLOT);
                endFling(true);
                return;
            }

            mShouldStopFling = false;

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int delta = mLastFlingX - x;

            // Pretend that each frame of a fling scroll is a touch scroll
            if (delta > 0) {
                // Moving towards the left. Use first view as mDownTouchPosition
                // mDownTouchPosition = mFirstPosition;

                // Don't fling more than 1 screen
                // delta =
                // Math.min(getWidth() / 2 - mPaddingLeft - mPaddingRight
                // - mSpacing, delta);
            } else {
                // Moving towards the right. Use last view as mDownTouchPosition
                int offsetToLast = getChildCount() - 1;
                // mDownTouchPosition = mFirstPosition + offsetToLast;

                // Don't fling more than 1 screen
                // delta =
                // Math.max(
                // -(getWidth() / 2 - mPaddingRight - mPaddingLeft - mSpacing),
                // delta);
            }

            if (trackMotionScroll(delta)) {
                if (more && !mShouldStopFling) {
                    mLastFlingX = x;
                    post(this);
                } else {
                    // endFling((mFlag & FLAG_SLOT) == FLAG_SLOT);
                    endFling(true);
                }

            }

        }

        public void startUsingDistance(int distance) {
            if (distance == 0) {
                return;
            }

            startCommon();

            mLastFlingX = 0;
            mScroller.startScroll(
                    0,
                    0,
                    -distance,
                    0,
                    Math.min(mAnimationDuration,
                            isCircle() ? Math.abs(distance * 2) : 300));
            post(this);
        }

        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity == 0) {
                return;
            }

            startCommon();

            int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingX = initialX;
            mScroller.fling(initialX, 0, initialVelocity, 0, 0,
                    Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            post(this);
        }

        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        private void endFling(boolean scrollIntoSlots) {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);
            if (scrollIntoSlots) {
                scrollIntoSlots();
            }
        }

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }

    }

    class MyDetector extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if (AppInfo.disAllowIntercept) {
                AppInfo.disAllowIntercept = false;
                return true;
            }
           // Log.i(TAG, "onDown");
            mFlingRunnable.stop(false);
            View v = findView(e);
            if (null != v) {
                mScrollState = OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
                v.setSelected(true);
                mPressed = v;
                invalidate();
            } else {
                mPressed = null;
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
        	Log.i(TAG, "onFling");
            // Fling the gallery!
            mScrollState = OnScrollListener.SCROLL_STATE_FLING;
            final int dx = (int) -velocityX;
            // if (isAutoScrolling())
            // {
            // mFlingRunnable.startUsingDistance(velocityX > 0 ? Config
            // .getWidth() - (int) (e2.getX()) : (int) -e2.getX());
            // }
            // else
            if (isCircle() && checkScrolls()) {
                scrollIntoSlots(velocityX < 0);
                return true;
            }
            {
                int min = Math.min(Math.abs(dx),
                        Math.abs(getLimitedMotionScrollAmount(dx < 0, dx)));
                // Log.v(TAG, "--------------------------min:" + min);
                mFlingRunnable.startUsingVelocity((dx < 0 ? -1 : 1)
                        * Math.min(Config.getWidth() * 5 / 2, min));
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent ev1, MotionEvent ev2,
                float paramFloat1, float paramFloat2) {
           Log.i(TAG, "onScroll");
            int deltaX = -1 * (int) paramFloat1;
            mScrollState = OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
            trackMotionScroll(deltaX);
            clearSelection();
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
        	Log.i(TAG, "onSingleTapUp");
            if (mListener != null) {
                View v = findView(e);
                if (null != v && v == mPressed && mPressed.isSelected()) {
                    mListener.onItemClick(null, v, mFirstPosition
                            + indexOfChild(v), 0);
                    clearSelection();
                }
            }
            return true;
        }

    }

    class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<View>();

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }

        View get(int position) {
            View result = mScrapHeap.get(position);
            if (result != null) {
                mScrapHeap.delete(position);
            }
            return result;
        }

        View peek(int position) {
            return mScrapHeap.get(position);
        }

        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.valueAt(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }
            scrapHeap.clear();
        }
    }


    @Override
    public void onVisibilityChange(boolean visible) {
        if (visible) {
            if (!isAutoScrolling) {
                startScrolling();
            }
            update();
        } else {
            pauseScrolling();
            if (null != mGalleryAdapter) {
                mGalleryAdapter.recycle();
            }
        }
        mVisibale = visible;
    }

    public boolean mVisibale = true;


    @Override
    public void onConfigurationChanged() {
        if (null != mGalleryAdapter) {
            if (!checkScrolls()) {
                pauseScrolling();
                removeCallbacks(autoScroller);
            }
            configGalleryParams();
            mDataChanged = true;
            mRecycler.clear();
            removeAllViews();
        }

    }


    protected void configGalleryParams() {

        if (isCircle()) {
            if (mGalleryAdapter instanceof HomeGalleryAdapter) {
                int p = Config.getOrientation();
                if (checkScrolls()) {
                    startOffset = -mGalleryAdapter.getBitmapWidth() / 2
                            * (p - 1);
                    mFirstPosition = escape(mSelection - p / 2);
                    setDiscipline(p == 1);
                } else {
                    setDiscipline(true);
                    mFirstPosition = mSelection;
                }
            }
        } else {
            if (mGalleryAdapter instanceof HomeGalleryAdapter) {
                startOffset = 0;
                setDiscipline(true);
            } else if (Config.isLandscape()) {
                int i = getCapacity();
                if (mItemCount < i) {
                    mFirstPosition = 0;
                } else if (mFirstPosition > mItemCount - i) {
                    mFirstPosition = mItemCount - i;
                }
            }
        }
    }

    /**
     * Method: getAdapter
     * <p>
     * Author: syn.lee
     * <p>
     * Description:
     * <p>
     * Modified: 2012-8-24
     * 
     * @return
     */
    public GalleryAdapter getAdapter() {
        return mGalleryAdapter;
    }

    /**
     * Method: getLastVisiblePosition
     * <p>
     * Author: syn.lee
     * <p>
     * Description:
     * <p>
     * Modified: 2012-8-24
     * 
     * @return
     */
    public int getLastVisiblePosition() {
        return mFirstPosition + getChildCount();
    }

    /**
     * Method: scrollOneScreen
     * <p>
     * Author: syn.lee
     * <p>
     * Description:
     * <p>
     * Modified: 2012-9-24
     */
    private void scrollOneScreen() {
        mFlingRunnable
                .startUsingDistance(-(mGalleryAdapter.getBitmapWidth() + mSpacing)
                /* * getCapacity() */);
    }
}
