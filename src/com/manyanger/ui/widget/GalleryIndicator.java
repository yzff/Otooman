/**
 * 
 */

package com.manyanger.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.manyanger.cache.ImageCache;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.ui.widget.SkyGallery.GalleryPageSwitchListener;
import com.manyounger.otooman.R;

import java.util.List;

public class GalleryIndicator extends FrameLayout implements GalleryPageSwitchListener {

//    private final TextView label;

    private final Indicator indicator;

//    private final ImageView icon;

//    private AppIconController iconLoader;

    private ImageCache imageCache;

    private List<BaseComicItem> mHeader;

    private int mLast = -1;

    public GalleryIndicator(Context context) {
        this(context, null);
    }

    public GalleryIndicator(Context context, AttributeSet attr) {
        super(context, attr);
        inflate(context, R.layout.tuijian_bottom, this);
//        label = (TextView) findViewById(R.id.label);
        indicator = (Indicator) findViewById(R.id.m_indicator);
//        icon = (ImageView) findViewById(R.id.thumb);
    }

    @Override
    public void onPageChanged(int pageIndex) {
        if (pageIndex == mLast) {
            return;
        }
//        BaseItemModel base = mHeader.get(pageIndex);
//        if (base.itemType == BaseItemModel.ITEMTYPE_LEAF) {
//            DownloadInfo info = (DownloadInfo) base;
//            label.setText(info.appName);
//        } else {
//            NodeModel node = (NodeModel) base;
//            label.setText(node.nodeName);
//        }
//        Bitmap bitmap = getBitmap(base);
//        if (null != bitmap) {
//            icon.setBackgroundDrawable(new BitmapDrawable(bitmap));
//        } else {
//            icon.setBackgroundResource(R.drawable.default_icon);
//            if (iconLoader != null) {
//                iconLoader.loadIcon(pageIndex);
//            }
//        }
        indicator.onScreenChanged(mLast, pageIndex);
        mLast = pageIndex;
    }

    public void setData(List<BaseComicItem> mHeader, ImageCache imageCache) {
        if (null == mHeader) {
            return;
        }
        this.imageCache = imageCache;
        this.mHeader = mHeader;
//        BusProvider.get().register(this);
//        iconLoader = new AppIconController(mHeader, imageCache);
//
//        iconLoader.loadIcon(0);

        int size = mHeader.size();
        // 当只有一个时，不要显示白点。
        if (size > 1) {
            for (int i = 0; i < size; i++) {
                ImageView id = new ImageView(getContext());
                id.setScaleType(ScaleType.CENTER_INSIDE);
                id.setImageResource(R.drawable.dot_gallery);
                indicator.addItem(id);
            }
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        BusProvider.get().unregister(this);
    }

//    public void onEventMainThread(ImgLoadedEvent event) {
//        final Message msg = event.msg;
//        final int index = msg.arg1;
//
//        if (null == mHeader || mHeader.size() == 0 || index >= mHeader.size()) {
//            return;
//        }
//        final BaseItemModel base = mHeader.get(index);
//        if (base == null || msg.obj == null || !base.equals(msg.obj)) {
//            return;
//        }
//        switch (msg.what) {
//            case ImgLoadedEvent.ICON_LOADED: {
//                Bitmap bitmap = getBitmap(base);
//                if (bitmap != null) {
//                    icon.setBackgroundDrawable(new BitmapDrawable(bitmap));
//                }
//                break;
//            }
//            default:
//
//                base.mImageStatus = BaseItemModel.IMAGE_STATUS_NOINIT;
//                break;
//        }
//
//    }

//    private Bitmap getBitmap(final BaseItemModel base) {
//        if (imageCache == null) {
//            return null;
//        }
//        Bitmap bitmap = imageCache.get(base.getIconKey()).getData();
//        if (bitmap == null || bitmap.isRecycled()) {
//            return null;
//        }
//        return bitmap;
//    }

}
