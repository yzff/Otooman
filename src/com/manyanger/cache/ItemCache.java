/**
 * 
 */
package com.manyanger.cache;

import com.manyanger.entries.BaseComicItem;

import java.util.HashMap;



public class ItemCache 
{
    //单例
    private static ItemCache mItemCache;

    //整个应用程序唯一一份Map表
    public HashMap<Integer, BaseComicItem> mComicInfoMap;


    public static ItemCache getInstance()
    {
        if (mItemCache == null)
        {
            mItemCache = new ItemCache();
        }
        return mItemCache;
    }

    /**
     * 销毁缓存
     * */
    public static void destory()
    {

        mItemCache = null;
    }

    private ItemCache()
    {
        //初始化map，注册观察者监听
        mComicInfoMap = new HashMap<Integer, BaseComicItem>();
    }

    /**
     * 从map中获取Download对象，Key为漫画ID
     * */
    public BaseComicItem get(int comicId)
    {
        BaseComicItem comicInfo;
        synchronized (mItemCache)
        {
            comicInfo = mComicInfoMap.get(comicId);
        }
        return comicInfo;
    }


    public BaseComicItem checkVersion(BaseComicItem info)
    {
        if (info == null)
        {
            return null;
        }
        //从缓存中判断是否有这个对象，如果没有，则put一份
        BaseComicItem temp = ItemCache.getInstance().findSingle(info);
        //未取到对象
        if (temp == null)
        {
            temp = info;
        }

        return temp;
    }

    /**************************************************
     * 输入info引用，在hashmap中查找，是否有对应的实例， 如果有则，返回之;如果没有,则返回null
     * 该函数的目的是为了确保每个应用对象在内存中只有一份数据，在下载，安装等列表中便于维护
     */
    /***********************************************/
    public BaseComicItem findSingle(BaseComicItem info)
    {
        BaseComicItem ret = null;

        if (info.getId() == -1)
        {
            return null;
        }

        BaseComicItem comicInfo;
        synchronized (mItemCache)
        {
            comicInfo = mComicInfoMap.get(info.getId());
        }
        if (comicInfo != null)
        {
            ret = comicInfo;
        }
        else
        {
            synchronized (mItemCache)
            {
                mComicInfoMap.put(info.getId(), info);
            }
        }
        return ret;
    }

    
}
