package com.manyanger.data;

import android.util.Log;

import com.manyanger.GlobalData;
import com.manyanger.cache.ItemCache;
import com.manyanger.entries.BaseComicItem;
import com.manyanger.entries.BaseThemeItem;
import com.manyanger.entries.ChapterDetail;
import com.manyanger.entries.ChapterItem;
import com.manyanger.entries.DetailInfo;
import com.manyanger.entries.FirstPageModel;
import com.manyanger.entries.ListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: JsonParser
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-15 涓嬪崍10:06:54
 */
public class JsonParser {
    protected final static String JSON_KEY_BACKCODE = "BackCode";
    protected final static String JSON_KEY_ADLIST = "adList";
    protected final static String JSON_KEY_RECOMMENDLIST = "recommendComicList";
    protected final static String JSON_KEY_NEWESTLIST = "latestComicList";
    protected final static String JSON_KEY_FEATUREDLIST = "featuredComicList";
    protected final static String JSON_KEY_ID = "id";
    protected final static String JSON_KEY_CHAPTERCOUNT = "chapterCount";
    protected final static String JSON_KEY_COVER = "cover";
    protected final static String JSON_KEY_TITLE = "title";
    protected final static String JSON_KEY_AUTHOR = "author";
    protected final static String JSON_KEY_SCORE = "score";
    protected final static String JSON_KEY_PAGE = "page";
    protected final static String JSON_KEY_COMICPAGE = "comicPage";
    protected final static String JSON_KEY_COUNT = "count";
    protected final static String JSON_KEY_INDEX = "index";
    protected final static String JSON_KEY_HASNEXT = "next";
    protected final static String JSON_KEY_HASPREV = "prev";
    protected final static String JSON_KEY_PAGESIZE = "size";
    protected final static String JSON_KEY_PAGECOUNT = "total";
    protected final static String JSON_KEY_PAGEITEMS = "result";
    protected final static String JSON_KEY_IMAGEURL = "image";
    protected final static String JSON_KEY_DETAILURL = "url";
    protected final static String JSON_KEY_COMICTHEME = "comicTheme";
    protected final static String JSON_KEY_THEME = "theme";
    protected final static String JSON_KEY_THEMENAME = "name";
    protected final static String JSON_KEY_DEPICT = "depict";
    protected final static String JSON_KEY_CHAPTERLIST = "chapterList";
	protected final static String JSON_KEY_COMIC = "comic";
	protected final static String JSON_KEY_CHAPTER = "chapter";
	protected final static String JSON_KEY_IMAGECOUNT = "imageCount";
	protected final static String JSON_KEY_IMAGELIST = "images";
	protected final static String JSON_KEY_PROCESS = "process";

    
    
    protected final static int JSON_SUCCESS = 1001;
    
    private static JSONObject checkResultString(String jsonString){
    	if(jsonString == null){
            return null;
        }
    	JSONObject object = null;
        JSONTokener tokener = new JSONTokener(jsonString);
        try{
            object = (JSONObject)tokener.nextValue();
            if(object == null) {
                return null;
            }
            if(object.getInt(JSON_KEY_BACKCODE) != JSON_SUCCESS) {
                return null;
            }
            return object;
        } catch (JSONException e){
            Log.w("JSON", "checkResultString Exception");
       } 
       return null;
    }
    
    private static String getStringValue(JSONObject object, String key){
        String value = null;
        try{
            value = object.getString(key);
        }catch(JSONException e){
            Log.w("JSON", "no value by "+ key);
        }
        return value;
    }
    
    private static int getIntValue(JSONObject object, String key, int defaultValue){
        int value = defaultValue;
        try{
            value = object.getInt(key);
        }catch(JSONException e){
            Log.w("JSON", "no value by "+ key);
        }
        return value;
    }
    public static List<BaseComicItem> parseComicItems(JSONArray itemArray){
        if(itemArray == null){
            return null;
        }
        List<BaseComicItem> comicList = new ArrayList<BaseComicItem>();
        for(int i=0; i<itemArray.length(); i++) {
            try{
                JSONObject obj = (JSONObject)itemArray.get(i);
                if(obj != null){
                    BaseComicItem item = new BaseComicItem();
                    int id = obj.getInt(JSON_KEY_ID);
                    int chapters = getIntValue(obj, JSON_KEY_CHAPTERCOUNT, 0);
                    String cover = getStringValue(obj, JSON_KEY_COVER);
                    String title = getStringValue(obj, JSON_KEY_TITLE);
                    String author = getStringValue(obj, JSON_KEY_AUTHOR);

//                    int score = obj.getInt(JSON_KEY_SCORE);
                    
                    item.setId(id);
                    item = ItemCache.getInstance().checkVersion(item);
                    item.setChapter(chapters);
                    item.setTitle(title);
                    item.setCoverUrl(cover);
//                    item.setScore(score);
                    item.setAuthor(author);
                    
                    comicList.add(item);
                    
                }
            }catch(Exception e){
                
            }
            
        }
        
        return comicList;
    }
    
    public static List<BaseComicItem> parseBannerItems(JSONArray itemArray){
        if(itemArray == null){
            return null;
        }
        List<BaseComicItem> comicList = new ArrayList<BaseComicItem>();
        for(int i=0; i<itemArray.length(); i++) {
            try{
                JSONObject obj = (JSONObject)itemArray.get(i);
                if(obj != null){
                    BaseComicItem item = new BaseComicItem();
                    
                    String imgUrl = obj.getString(JSON_KEY_IMAGEURL);
                    String detailUrl = obj.getString(JSON_KEY_DETAILURL);
                    String cmdline = GlobalData.CMD_DETAIL+"?id=";
                    if(detailUrl != null && detailUrl.contains(cmdline)){
                        int pos = detailUrl.indexOf(cmdline)+ cmdline.length();
                        String ids = detailUrl.substring(pos);
                        int id = -1;
                        try{
                            id = Integer.parseInt(ids);
                        } catch(Exception e){
                            
                        }
                        if(id != -1){
                            item.setId(id);
                            item = ItemCache.getInstance().checkVersion(item);
                        }
                    }
                    
                    item.setBigImageUrl(imgUrl);
                    item.setDetailUrl(detailUrl);
                    comicList.add(item);
                    
                }
            }catch(Exception e){
                e.printStackTrace();
            	Log.w("JSON", "parse banner Exception");
            }
            
        }
        
        return comicList;
    }
    public static ListModel parseComicList(String jsonString) {
        ListModel listModel = new ListModel();
        listModel.setSuccess(false);
        if(jsonString == null){
            return listModel;
        }

        JSONObject object = checkResultString(jsonString);
        if(object == null) {
        	return listModel;
        }

        try{
            JSONObject pageData = object.getJSONObject(JSON_KEY_COMICPAGE);
            if(pageData != null){
                int index = getIntValue(pageData, JSON_KEY_INDEX, -1);
                listModel.setPageIndex(index);
                
                boolean hasNext = false;
                try{
                    hasNext = pageData.getBoolean(JSON_KEY_HASNEXT);
                }catch(JSONException e){
                    try{
                        int pageCount = pageData.getInt(JSON_KEY_PAGECOUNT);
                        if(index < pageCount -1){
                            hasNext = true;
                        }
                    } catch(JSONException e1){
                    	Log.w("JSON", "no page count");
                    }
                }
                listModel.setHasNext(hasNext);
                
                int listSize = getIntValue(pageData, JSON_KEY_PAGESIZE, 0);

                listModel.setItemCount(listSize);
                
                JSONArray itemArray = pageData.getJSONArray(JSON_KEY_PAGEITEMS);
                List<BaseComicItem> comicItems = parseComicItems(itemArray);
                
                listModel.setItemList(comicItems);
                
                listModel.setSuccess(true);
            }
            
            
        } catch (JSONException e){
             e.printStackTrace();
             listModel.setSuccess(false);
        }
        return listModel;
    }

    
    public static FirstPageModel parseFirstPageList(String jsonString) {
        FirstPageModel listModel = new FirstPageModel();
        listModel.setSuccess(false);
        
        JSONObject object = checkResultString(jsonString);
        if(object == null) {
        	return listModel;
        }

            
            try{              
                JSONArray itemArray = object.getJSONArray(JSON_KEY_ADLIST);
                List<BaseComicItem> comicItems = parseBannerItems(itemArray);
                listModel.setAdList(comicItems);
            }catch (JSONException e1){
            	Log.w("JSON", "adlist failed");
            }
            
            try{              
                JSONArray itemArray = object.getJSONArray(JSON_KEY_RECOMMENDLIST);
                List<BaseComicItem> comicItems = parseComicItems(itemArray);
                listModel.setRecommendList(comicItems);
            }catch (JSONException e1){
            	Log.w("JSON", "recommendlist failed");
            }
            
            try{              
                JSONArray itemArray = object.getJSONArray(JSON_KEY_NEWESTLIST);
                List<BaseComicItem> comicItems = parseComicItems(itemArray);
                listModel.setNewestList(comicItems);
            }catch (JSONException e1){
            	Log.w("JSON", "newsetlist failed");
            }
            
            try{              
                JSONArray itemArray = object.getJSONArray(JSON_KEY_FEATUREDLIST);
                List<BaseComicItem> comicItems = parseComicItems(itemArray);
                listModel.setFeaturedList(comicItems);
            }catch (JSONException e1){
            	Log.w("JSON", "featuredlist failed");
            }
                
            listModel.setSuccess(true);


        return listModel;
    }
    
    public static List<BaseThemeItem> parsethemeItems(JSONArray itemArray) {
        if(itemArray == null){
            return null;
        }
        
        List<BaseThemeItem> list = new ArrayList<BaseThemeItem>();
        for(int i=0; i<itemArray.length(); i++) {
            try{
                JSONObject obj = (JSONObject)itemArray.get(i);
                if(obj != null){
                	BaseThemeItem item = new BaseThemeItem();
                    
                    int id = obj.getInt(JSON_KEY_ID);
                    item.setId(id);
                    String name = obj.getString(JSON_KEY_THEMENAME);
                    item.setTitle(name);
                    list.add(item);
                }
            }catch(Exception e){
                e.printStackTrace();
            	Log.w("JSON", "parse Theme items Exception");
            }
            
        }
        return list;
        
    }
    
    public static List<BaseThemeItem> parseThemeList(String jsonString) {
    	List<BaseThemeItem> themeList = null;
    	
        JSONObject object = checkResultString(jsonString);
        if(object == null) {
        	return null;
        }
        
        try{
        	JSONArray itemArray = object.getJSONArray(JSON_KEY_COMICTHEME);
        	themeList = parsethemeItems(itemArray);
        } catch (JSONException e){
            e.printStackTrace();
            themeList = null;
       } 
    	
		return themeList;
    	
    }
    
    public static List<ChapterItem> parseChapterItems(JSONArray itemArray) {
        if(itemArray == null){
            return null;
        }
        
        List<ChapterItem> list = new ArrayList<ChapterItem>();
        for(int i=0; i<itemArray.length(); i++) {
            try{
                JSONObject obj = (JSONObject)itemArray.get(i);
                if(obj != null){
                    ChapterItem item = new ChapterItem();
                    
                    int id = obj.getInt(JSON_KEY_ID);
                    item.setId(id);
                    String name = obj.getString(JSON_KEY_TITLE);
                    item.setTitle(name);
                    list.add(item);
                }
            }catch(Exception e){
                e.printStackTrace();
                Log.w("JSON", "parseChapterItems Exception");
            }
            
        }
        return list;
    }
    
    public static DetailInfo parseDetailInfo(String jsonString) {
        JSONObject object = checkResultString(jsonString);
        if(object == null) {
            return null;
        }
        
        DetailInfo item = new DetailInfo();
        try{
			JSONObject comic = object.getJSONObject(JSON_KEY_COMIC);
			int id = comic.getInt(JSON_KEY_ID);
			item.setId(id);
			int chapters = comic.getInt(JSON_KEY_CHAPTERCOUNT);
			item.setChapters(chapters);
			String cover = comic.getString(JSON_KEY_COVER);
			item.setCoverUrl(cover);
			String title = comic.getString(JSON_KEY_TITLE);
			item.setTitle(title);
			String author = comic.getString(JSON_KEY_AUTHOR);
			item.setAuthor(author);
			String brief = comic.getString(JSON_KEY_DEPICT);
			item.setDepict(brief);
			String theme = comic.getString(JSON_KEY_THEME);
			item.setTheme(theme);
			String process = comic.getString(JSON_KEY_PROCESS);
			item.setProcess(process);

			final JSONArray itemArray = object
					.getJSONArray(JSON_KEY_CHAPTERLIST);
            List<ChapterItem> chapterList = parseChapterItems(itemArray);
            item.setChapterList(chapterList);
            
        }catch(Exception e){
            e.printStackTrace();
            Log.w("JSON", "parseDetailInfo Exception");
            Log.w("JSON", jsonString);
        }
        
        return item;
    }
    public static List<ChapterItem> parseChapterList(String jsonString) {
    	JSONObject object = checkResultString(jsonString);
    	if(object == null) {
    		return null;
    	}
    	try{
    		JSONArray itemArray = object.getJSONArray(JSON_KEY_CHAPTERLIST);
    		return parseChapterItems(itemArray);
    		
    	}catch(Exception e){
    	    e.printStackTrace();
    		Log.w("JSON", "parseChapterList Exception");
    		Log.w("JSON", jsonString);
    	}
    	
    	return null;
    }

    /**
     * @param resultStr
     * @return
     */
    public static ChapterDetail parseChapterDetail(String jsonString) {
        JSONObject object = checkResultString(jsonString);
        if(object == null) {
            return null;
        }
        
        ChapterDetail item = new ChapterDetail();
        try{
            JSONObject chapter = object.getJSONObject(JSON_KEY_CHAPTER);
            int id = getIntValue(chapter, JSON_KEY_ID, -1);
            item.setId(id);
            String title = getStringValue(chapter, JSON_KEY_TITLE);
            item.setTitle(title);
            int imgCount = getIntValue(chapter, JSON_KEY_IMAGECOUNT, 0);
            item.setImageCount(imgCount);
            
            String imagesStr = chapter.getString(JSON_KEY_IMAGELIST);
            String[] imageList = imagesStr.split(",");
            List<String> imageArray = new ArrayList<String>();
            for(int i=0; i<imageList.length; i++){
            	imageArray.add(imageList[i].trim());
            }
//            imageArray.addAll(Arrays.asList(imageList));
            item.setImages(imageArray);
            
        }catch(Exception e){
            e.printStackTrace();
            Log.w("JSON", "parseChapterDetail Exception");
            Log.w("JSON", jsonString);
            return null;
        }
        
        return item;
    }

}
 