package com.manyanger.entries;

import java.util.List;

/**
 * @ClassName: ChapterDetail
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-22 下午4:38:56
 */
public class ChapterDetail {
    
    private int id;
    
    private String title;
    
    private int imageCount;
    
    private List<String> images;

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
     * @return the images
     */
    public List<String> getImages() {
        return images;
    }

    /**
     * @param images the images to set
     */
    public void setImages(List<String> images) {
        this.images = images;
    }

    /**
     * @return the imageCount
     */
    public int getImageCount() {
        return imageCount;
    }

    /**
     * @param imageCount the imageCount to set
     */
    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

}
