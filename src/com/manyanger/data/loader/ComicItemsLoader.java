package com.manyanger.data.loader;

import com.manyanger.GlobalData;
import com.manyanger.data.net.DataException;

import java.io.InputStream;

/**
 * @ClassName: ComicItemsLoader
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-14 下午9:46:02
 */
public class ComicItemsLoader {
    private static final String TAG = "list_";
    
    private static String buildCacheUrl()
    {
        StringBuilder sBuilder = new StringBuilder(TAG);

        return sBuilder.toString();
    }

    public static String load(String cmdLine) throws DataException, Exception{
        // cache
        

        //
        try {
            String respone =
                    GlobalData.getHttpService().getHttpResponseInString(cmdLine);
            return respone;
        } catch (DataException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }


    }
    
    public static InputStream loadImage(String url) throws DataException, Exception{
    	
    	try {
    		InputStream indata = 
    				GlobalData.getHttpService().getHttpResponseInputStream(url);
    		return indata;
    	} catch (DataException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
