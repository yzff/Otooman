package com.manyanger.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;




public class MenuBar extends Indicator
{


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {

            super.onLayout(changed, l, t, r, b);
    }


    private Drawable[] icons;

    private String[] labels;

    private int textSize = 18;

 
    public MenuBar(Context context)
    {
        this(context, null);
    }


    public MenuBar(Context context, AttributeSet attr)
    {
        super(context, attr);
        setFocusableInTouchMode(true);
    }


    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (0 != getChildCount() && null == getChildAt(0).getTag())
        {
            for (int i = 0; i < getChildCount(); i++)
            {
                final View child = getChildAt(i);
                child.setOnClickListener(this);
                child.setTag(idx++);
            }
        }
    }



    @Override
    public void createContent()
    {
        checkParams();
        if (getChildCount() != 0)
        {
            removeAllViews();
        }
//        for (int i = 0; i < labels.length; i++)
//        {
//            addItem(getTitleView(i), i < labels.length - 1 ? getDivider()
//                : null);
//
//        }
//        getChildAt(0).setSelected(true);
    }

 
    public void createContent(String[] labels, Drawable[] icons)
    {
        this.labels = labels;
        this.icons = icons;
        createContent();
    }


    public Drawable[] geticons()
    {
        return icons;
    }

  
    public String[] getlabels()
    {
        return labels;
    }

    
    @Override
    public void onScreenChanged(int srcPosition, int desPosition)
    {
        View v = findViewWithTag(desPosition);
        if (null != v)
        {
            v.setSelected(true);
        }
        if (desPosition != srcPosition)
        {
            v = findViewWithTag(srcPosition);
            if (null != v)
            {
                v.setSelected(false);
            }
        }
    }

 
    public void seticons(Drawable[] icons)
    {
        this.icons = icons;
    }

  
    public void setItems(String[] labels, Drawable[] icons)
    {
        this.labels = labels;
        this.icons = icons;
    }

   
    public void setlabels(String[] labels)
    {
        this.labels = labels;
    }

  
    public void setTextSize(int size)
    {
        textSize = size;
    }


    private void checkParams()
    {
        if (null == labels && null == icons)
        {
            throw new NullPointerException(
                "label and icon must not be null at the same time");
        }
        if (null != labels && null != icons && labels.length != icons.length)
        {
            throw new IllegalArgumentException(
                "label and icon should be with the same amount");
        }
        if (null == labels)
        {
            labels = new String[icons.length];
            Arrays.fill(labels, null);
        }
        if (null == icons)
        {
            icons = new Drawable[labels.length];
            Arrays.fill(icons, null);
        }
    }


//    TextView tv(int i)
//    {
//        TextView item = new TextView(getContext());
//        item.setText(labels[i]);
//        item.setGravity(Gravity.CENTER);
//        item.setTextSize(textSize);
//        item.setCompoundDrawablesWithIntrinsicBounds(null, icons[i], null, null);
//        item.setBackgroundResource(selector);
//        item.setOnClickListener(this);
//        try
//        {
//            item.setTextColor(ColorStateList.createFromXml(getResources(),
//                getResources().getXml(R.color.font_menu_color)));
//        }
//        catch (Exception e)
//        {
//        }
//        return item;
//    }

//    private View getTitleView(int i)
//    {
//        ViewGroup g =
//            (ViewGroup) LayoutInflater.from(getContext()).inflate(
//                com.skymobi.appstore.common.R.layout.tab_indicator(), null);
//        TextView title = (TextView) g.findViewById(R.id.title);
//        title.setText(labels[i]);
//        title.setTextSize(textSize, TypedValue.COMPLEX_UNIT_SP);
//        if (0 != mTextColor)
//        {
//            try
//            {
//                title.setTextColor(getResources().getColor(mTextColor));
//            }
//            catch (Exception e)
//            {
//            }
//        }
//        g.setBackgroundResource(selector);
//        return g;
//    }
}
