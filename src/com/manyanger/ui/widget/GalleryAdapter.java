/**
 * 
 */
package com.manyanger.ui.widget;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.manyanger.common.Config;
import com.manyanger.common.PixValue;

import java.io.Serializable;



public abstract class GalleryAdapter extends BaseAdapter
{

    public int getBitmapWidth()
    {
        int count = Config.getOrientation() << 1;
        return (Config.getWidth() - PixValue.dip.valueOf(5) * (count + 1))
            / count;
    }

    public abstract int getBitmapHeight();

    public View getView(int position, View convertView)
    {
        return null;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView);
    }


    public abstract void setSkyGallery(SkyGallery skyGallery);


    public abstract Bitmap getBitmap(int position, boolean loadFromNet);


    public void recycle()
    {

    }

    public abstract Object getData();

    public static Serializable tmpData;

}
