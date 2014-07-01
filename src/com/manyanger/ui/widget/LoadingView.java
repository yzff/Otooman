/**
 * 
 */
package com.manyanger.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manyanger.common.PixValue;
import com.manyounger.otooman.R;


/**
 * Class: LoadingView
 * <p>Author:  syn.lee
 * <p>Created: 2012-8-17
 * <p>Descreption: 
 * <p>Copyright : Copyright (C) 2008-2011 HangZhou SKY-MOBI CO.,LTD
 */
public class LoadingView extends LinearLayout
{

    private final ProgressBar progress;

    private final TextView hint;

    private final ImageView img;

  
    public LoadingView(Context context)
    {
        this(context, null);
    }


    public LoadingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setMinimumHeight(PixValue.dip.valueOf(50));
        inflate(context, R.layout.footview, this);
        progress = (ProgressBar) findViewById(R.id.load_progress);
        hint = (TextView) findViewById(R.id.hint);
        hint.setClickable(false);
        img = (ImageView) findViewById(R.id.loading_icon);
    }

    public static final int STATUS_LOADING = 3;

    public static final int STATUS_FINISHED = 1;

    public static final int STATUS_ERROR = 2;


    public void setStatus(int status, String... tip)
    {
        switch (status)
        {
            case STATUS_LOADING:
                setClickable(false);
                hint.setClickable(false);
                img.setVisibility(GONE);
                progress.setVisibility(VISIBLE);
                break;
            case STATUS_ERROR:
                setClickable(true);
                hint.setClickable(true);
                img.setVisibility(VISIBLE);
                progress.setVisibility(GONE);
                break;
            case STATUS_FINISHED:
                setClickable(false);
                hint.setClickable(false);
                img.setVisibility(GONE);
                progress.setVisibility(GONE);
                break;
        }
        if (null != tip && 0 != tip.length)
        {
            hint.setText(tip[0]);
        }
    }


    public void setStatus(int statusLoading, int resId)
    {
        setStatus(statusLoading, getResources().getString(resId));
    }


    @Override
    public void setOnClickListener(OnClickListener l)
    {
        hint.setOnClickListener(l);
    }

}
