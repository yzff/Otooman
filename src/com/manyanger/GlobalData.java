package com.manyanger;

import java.util.List;

import android.app.Activity;

import com.manyanger.cache.ImageCache;
import com.manyanger.data.IServerInterface;
import com.manyanger.data.net.HttpService;
import com.manyanger.entries.BaseThemeItem;

/**
 * @ClassName: GlobalData
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-14 下午10:29:43
 */
public class GlobalData implements IServerInterface{
	public final static int STATE_INIT = 0;
	public final static int STATE_LOADING = 1;
	public final static int STATE_FINISHED = 2;
	public final static int STATE_FAILED = -1;
	
	public final static String STR_TITLE = "title";
	public final static String STR_LISTTYPE = "listType";
    public final static String STR_LISTID = "listId";
    public final static String STR_COMICID = "comicId";
	public final static String STR_URL = "url";
	public final static String STR_CHAPTERID = "chapterId";
	public final static String STR_KEYWORD = "keyword";
    
    public final static int NOTIFY_COMICLIST_LOADED = 1;
    public final static int NOTIFY_COMICTHEME_LOADED = 2;
    public final static int NOTIFY_COMICDETAIL_LOADED = 3;
    public final static int NOTIFY_COMICCOVER_LOADED = 4;
    public final static int NOTIFY_BANNERIMAGE_LOADED = 5;
    public final static int NOTIFY_CONTENTIMAGE_LOADED = 6;
    public final static int NOTIFY_PICTURELIST_LOADED = 7;
    
   
    private static ImageCache imageCache;
    
    private static HttpService httpService;
    
	private static List<BaseThemeItem> categorys;
	
	public static Activity homeActivity;
	public static Activity myActivity;
	public static Activity cateActivity;
    
    public static void init(){
        setImageCache(ImageCache.getInstance());
        
        setHttpService(new HttpService());
    }

    /**
     * @return the httpService
     */
    public static HttpService getHttpService() {
        return httpService;
    }

    /**
     * @param httpService the httpService to set
     */
    public static void setHttpService(HttpService httpService) {
        GlobalData.httpService = httpService;
    }

    /**
     * @return the imageCache
     */
    public static ImageCache getImageCache() {
        return imageCache;
    }

    /**
     * @param imageCache the imageCache to set
     */
    public static void setImageCache(ImageCache imageCache) {
        GlobalData.imageCache = imageCache;
    }

	public static List<BaseThemeItem> getCategorys() {
		return categorys;
	}

	public static void setCategorys(List<BaseThemeItem> categorys) {
		GlobalData.categorys = categorys;
	}
    
    
}
