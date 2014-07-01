
package com.manyanger.ui.widget;

import java.util.Timer;
import java.util.TimerTask;

import com.manyounger.otooman.R;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;



public class LoadView extends FrameLayout
{

    private Context mContext;

    private Timer mTimer;

    private View loadview;

    private View thisview;

    private boolean isStartChange;

    private TextView tipTextView;

    private static final long TIP_PLAY_INTERVAL = 8000; // 8秒

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            if (loadview.getParent() != null && thisview.getParent() != null)// 进度条或者自己被移除，则停止
            {

//                tipTextView.setText(((StoreApplication) mContext.getApplicationContext())
//                        .getOperationsSupport().getOperationsTip());
            }
            else
            {
                stopTimer(mTimer);
            }

        };
    };;

    public LoadView(Context context, boolean startChange)
    {
        super(context);
        isStartChange = startChange;

        initView(context);
    }

    public LoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context)
    {
        mContext = context;
        thisview = inflate(mContext, R.layout.progress_bar_intern, this);

        loadview = thisview.findViewById(R.id.load_view);
        tipTextView = (TextView) thisview.findViewById(R.id.tips_content);
//        tipTextView.setText(((StoreApplication) mContext.getApplicationContext())
//                .getOperationsSupport().getOperationsTip());
        if (isStartChange)
        {
            startTimer();
        }
    }

    private void stopTimer(Timer timer)
    {
        if (timer != null)
        {
            try
            {
                timer.cancel();
            } catch (Exception e)
            {
            }

        }
        timer = null;
    }

    private void startTimer()
    {
        stopTimer(mTimer);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask()
        {

            @Override
            public void run()
            {
                mHandler.sendEmptyMessage(0);

            }
        }, TIP_PLAY_INTERVAL, TIP_PLAY_INTERVAL);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
            Rect previouslyFocusedRect)
    {
        // Log.i("LoadView", "onFocusChanged... ");
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    public void start()
    {
        startTimer();
//        tipTextView.setText(((StoreApplication) mContext.getApplicationContext())
//                .getOperationsSupport().getOperationsTip());
    }

    public void stop()
    {
        stopTimer(mTimer);
    }

}
