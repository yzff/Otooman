package com.manyanger.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.manyounger.otooman.R;



public abstract class AbsContentView extends LinearLayout implements Runnable, OnCancelListener
{
    public static final LayoutParams FILL_PARENT =
        new LinearLayout.LayoutParams(-1, -1);

    public static final LayoutParams WRAP_CONTENT =
        new LinearLayout.LayoutParams(-2, -2);

    protected static final int STATUS_ERROR = 4;

    protected static final int STATUS_INIT = 0;

    protected static final int STATUS_LOADING = 1;

    protected static final int STATUS_RIGHT = 2;

    protected static final int STATUS_RIGHT_EMPTY = 3;

    private static final String TAG = "abs";

    public static View inflate(Context context, int resource, ViewGroup root)
    {
        return LayoutInflater.from(context).inflate(resource, root, false);
    }

//    protected final DataController dataLoader;

    private final Handler handler = new Handler(Looper.getMainLooper())
    {

        @Override
        public void handleMessage(Message msg)
        {
            Log.i("abs", "handleMessage");
            AbsContentView v = (AbsContentView) msg.obj;
            v.mStatus = msg.what;
            v.handleResult(msg.arg1);
            TipDialog.dissmissTip();
        }
    };

    //    private static final int STATUS_FINISHED = 4;

    private View error;

    /**
     * should render or not
     */
    private boolean isRenderable;

    /**
     * whether data and view has been setup
     */
    private boolean isRendered;

    private View mContentView;

    private View mLoadingView;

    private int mStatus;

    public AbsContentView(Context context)
    {
        this(context, null);
    }

    private void doSomeThing()
    {
        setGravity(Gravity.CENTER);
        setLayoutParams(FILL_PARENT);
        mContentView = initContentView();
        if (null != (mLoadingView = initLoading()))
        {
            addView(mLoadingView);
        }
        else
        {
            if (mContentView.getParent() != null)
            {
                AbsContentView temp = (AbsContentView) mContentView.getParent();
                temp.removeView(mContentView);
            }
            addView(mContentView);
        }
    }


    public AbsContentView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
//        dataLoader = new DataController(this);
        doSomeThing();
    }

    public int index;

    public AbsContentView(Context context, int index)
    {
        super(context, null);
        this.index = index;
//        dataLoader = new DataController(this);
        doSomeThing();
    }


    public View getContent()
    {
        return mContentView;
    }


//    public DataController getDataLoader()
//    {
//        return dataLoader;
//    }


    public boolean isRenderable()
    {
        return isRenderable;
    }


    @Override
    public void onCancel(DialogInterface dialog)
    {

    }


//    @Override
//    public void onDataLoaded(Object data, int requestCode, int resultCode)
//    {
//    }


    public void onErrorResp(int errorCode)
    {
        removeAllViews();
        if (null == error)
        {
            initError();
        }
        else
        {
            resetError();
        }
        addView(error);
    }


    public synchronized void onRightResp()
    {
        if (null != error)
        {
            removeView(error);
            addView(mContentView);
            error = null;
        }
        else if (null != mLoadingView)
        {
            removeView(mLoadingView);
            addView(mContentView);
        }
    }


    public void prepare()
    {
        if (mStatus == STATUS_INIT)
        {
            submitTask();
            mStatus = STATUS_LOADING;
        }
        else if (!isRendered)
        {
            handleResult(-1);
        }
        else
        {
            Log.v(TAG, this + " has rendered!");
        }
    }


    public View render()
    {
        setRenderable(true).prepare();
        return this;
    }


    public void reset()
    {
        isRenderable = true;
        isRendered = false;
        mStatus = STATUS_INIT;
    }


    @Override
    public void run()
    {
        int status = initData() ? STATUS_RIGHT : STATUS_ERROR;
        Message.obtain(handler, status, this).sendToTarget();
    }


    public AbsContentView setRenderable(boolean isRenderable)
    {
        this.isRenderable = isRenderable;
        return this;
    }

    public void showEmpty()
    {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.empty, this);
    }


    public void updateContent()
    {
    }


    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        return FILL_PARENT;
    }


    protected abstract View initContentView();


    protected abstract boolean initData();


    protected View initLoading()
    {
        if (null == mLoadingView)
        {
            mLoadingView =
                inflate(getContext(), R.layout.progress_bar_intern, this);
        }
        return mLoadingView;
    }


    protected void onErrorClicked()
    {
        readyErrorProgress();
        reset();
        prepare();
    }


    protected void submitTask()
    {
//        ThreadPool.submitText(this);
    }


    private void handleResult(int resCode)
    {
        if (isRendered || !isRenderable)
        {
            return;
        }
        Log.i(TAG, "handlerResult:" + mStatus);
        switch (mStatus)
        {
            case STATUS_INIT:
            case STATUS_LOADING:
                Log.w(TAG, "handlerResult:STATUS-" + mStatus);
                break;
            case STATUS_RIGHT:
                onRightResp();
                isRendered = true;
                break;
            case STATUS_ERROR:
                onErrorResp(-1);
                isRendered = true;
                break;
            default:
                break;
        }

    }


    private void initError()
    {
        error = inflate(getContext(), R.layout.error, null);
        error.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onErrorClicked();
            }
        });
    }


    private void readyErrorProgress()
    {
        removeView(error);
        error = null;
        addView(initLoading());
    }


    private void resetError()
    {
    }

}
