package com.manyanger.entries;

import java.util.List;

/**
 * @ClassName: FirstPageModel
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-16 下午11:17:37
 */
public class FirstPageModel extends BaseResponse{

    private List<BaseComicItem> adList;
    
    private List<BaseComicItem> recommendList;
    
    private List<BaseComicItem> newestList;
    
    private List<BaseComicItem> featuredList;

    /**
     * @return the adList
     */
    public List<BaseComicItem> getAdList() {
        return adList;
    }

    /**
     * @param adList the adList to set
     */
    public void setAdList(List<BaseComicItem> adList) {
        this.adList = adList;
    }

    /**
     * @return the recommendList
     */
    public List<BaseComicItem> getRecommendList() {
        return recommendList;
    }

    /**
     * @param recommendList the recommendList to set
     */
    public void setRecommendList(List<BaseComicItem> recommendList) {
        this.recommendList = recommendList;
    }

    /**
     * @return the newestList
     */
    public List<BaseComicItem> getNewestList() {
        return newestList;
    }

    /**
     * @param newestList the newestList to set
     */
    public void setNewestList(List<BaseComicItem> newestList) {
        this.newestList = newestList;
    }

    /**
     * @return the featuredList
     */
    public List<BaseComicItem> getFeaturedList() {
        return featuredList;
    }

    /**
     * @param featuredList the featuredList to set
     */
    public void setFeaturedList(List<BaseComicItem> featuredList) {
        this.featuredList = featuredList;
    }
}
