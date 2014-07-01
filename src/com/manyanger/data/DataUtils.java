package com.manyanger.data;

import android.util.Log;

import com.manyanger.GlobalData;

/**
 * @ClassName: DataUtils
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-15 上午12:20:04
 */
public class DataUtils {
    
    public static String makeCmdString(String cmd, int type, int listId, int pageNo, int theme, String keyword)
    {
        if(cmd == null){
            return null;
        }
        boolean isFirstParam = true;
        StringBuilder sb = new StringBuilder(cmd);
        
        if(type != -1){
            sb.append(isFirstParam ? "?" : "&");
            sb.append(GlobalData.PARAMS_TYPE);
            sb.append(type);
            isFirstParam = false;
        }
        
        if(listId != -1){
            sb.append(isFirstParam ? "?" : "&");
            sb.append(GlobalData.PARAMS_COMICID);
            sb.append(listId);
            isFirstParam = false;
        }
        
        if(theme != -1){
            sb.append(isFirstParam ? "?" : "&");
            sb.append(GlobalData.PARAMS_THEME);
            sb.append(theme);
            isFirstParam = false;
        }
        
        if(keyword != null){
            sb.append(isFirstParam ? "?" : "&");
            sb.append(GlobalData.PARAMS_KEYWORD);
            sb.append(keyword);
            isFirstParam = false;
        }
        
        if(pageNo != -1){
            sb.append(isFirstParam ? "?" : "&");
            sb.append(GlobalData.PARAMS_PAGENO);
            sb.append(pageNo);
            sb.append(GlobalData.PARAMS_PAGESIZE);
            isFirstParam = false;
        }
        
        
        Log.i("Net", "cmdString:"+sb.toString());
        return sb.toString();
    }
    
    public static String makeCmdString(String cmd, String key, int id)
    {
        if(cmd == null){
            return null;
        }
        StringBuilder sb = new StringBuilder(cmd);
        
        if(key != null){
            sb.append("?");
            sb.append(key);
            sb.append(id);
        }
        
        return sb.toString();
    }
    
    public static String makeIconKey(String url){
        if(url == null){
            return "";
        }
        String keystr = url;
        if(url.startsWith("http://")){
            keystr = url.substring("http://".length()+1);
            int pos = keystr.indexOf('/');
            if(pos > 0){
                keystr = keystr.substring(pos+1);
            }
        }
        keystr = keystr.replace("/", "_");
        
        return keystr;
    }

}
