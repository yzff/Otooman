package com.manyanger.entries;

import com.manyanger.common.AppUtil;
import com.manyounger.otooman.R;

/**
 * @ClassName: BaseItemData
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-7 涓嬪崍5:11:28
 */
public class BaseComicItem {
	
	public final static int ICON_STATE_INIT = 0;
	public final static int ICON_STATE_LOADING = 1;
	public final static int ICON_STATE_FINISHED = 2;
	public final static int ICON_STATE_FAILED = 3;
    
    private int id;
    
    private String title; //
    
    private String author; // 
    
    private int chapters; // 
    
//    private String theme; // 
    
//    private int process; // 
     
//    private String depict; // 
    
    private String coverUrl; // 
    
    private String bigImageUrl;
    
    private String detailUrl; // 
    
//    private String iconKey;
    
//    private List<ChapterItem> chapterList;
    
    private int coverState;
    private int bigImageState;
    
    public BaseComicItem(){
        id = -1;
    }
    
    public BaseComicItem(int id, String title) {
        this.id = id;
        this.title = title;
    }
    /**
     * @return
     */
    public String getCoverIconKey() {
    	// todo: always has http://
       if(coverUrl != null && coverUrl.length() > "http://".length()){
    	   String keystr = coverUrl.substring("http://".length()+1);
    	   int pos = keystr.indexOf('/');
    	   if(pos > 0){
    		   keystr = keystr.substring(pos+1);
    	   }
    	   keystr = keystr.replace("/", "_");
    	   return keystr;
       }
        return "";
    }
    
    public String getBigImageKey() {
    	// todo: always has http://
       if(bigImageUrl != null && bigImageUrl.length() > "http://".length()){
    	   String keystr = bigImageUrl.substring("http://".length()+1);
    	   int pos = keystr.indexOf('/');
    	   if(pos > 0){
    		   keystr = keystr.substring(pos+1);
    	   }
    	   keystr = keystr.replace("/", "_");
    	   
    	   return keystr;
       }
        return "";
    }
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
//        StringBuilder s = new StringBuilder();
//        s.append(AppUtil.getString(R.string.title_author));
//        s.append(author);
//        
//        return s.toString();
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the chapter
     */
    public int getChapter() {
        return chapters;
    }

    /**
     * @param chapter the chapter to set
     */
    public void setChapter(int chapter) {
        this.chapters = chapter;
    }
    
    public String getChapterString() {
        StringBuilder s = new StringBuilder();
        s.append(AppUtil.getString(R.string.update_to));
        s.append(chapters);
        s.append(AppUtil.getString(R.string.hua));
        
        return s.toString();
    }
    
    public String getChapterStringShort() {
        StringBuilder s = new StringBuilder();
        s.append(chapters);
        s.append(AppUtil.getString(R.string.hua));
        
        return s.toString();
    }

    /**
     * @return the coverUrl
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * @param coverUrl the coverUrl to set
     */
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * @return the brief
     */
//    public String getBrief() {
//        return depict;
//    }

    /**
     * @param brief the brief to set
     */
//    public void setBrief(String brief) {
//        this.depict = brief;
//    }

	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public void setBigImageUrl(String bigImageUrl) {
		this.bigImageUrl = bigImageUrl;
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

	public int getCoverState() {
		return coverState;
	}

	public void setCoverState(int coverState) {
		this.coverState = coverState;
	}

	public int getBigImageState() {
		return bigImageState;
	}

	public void setBigImageState(int bigImageState) {
		this.bigImageState = bigImageState;
	}

    /**
     * @return the theme
     */
//    public String getTheme() {
//        return theme;
//    }

    /**
     * @param theme the theme to set
     */
//    public void setTheme(String theme) {
//        this.theme = theme;
//    }

    /**
     * @return the process
     */
//    public int getProcess() {
//        return process;
//    }

    /**
     * @param process the process to set
     */
//    public void setProcess(int process) {
//        this.process = process;
//    }

    /**
     * @return the chapterList
     */
//    public List<ChapterItem> getChapterList() {
//        return chapterList;
//    }

    /**
     * @param chapterList the chapterList to set
     */
//    public void setChapterList(List<ChapterItem> chapterList) {
//        this.chapterList = chapterList;
//    }

    
}
