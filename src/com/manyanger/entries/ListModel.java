package com.manyanger.entries;

import java.util.List;

/**
 * @ClassName: ListModel
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-14 下午9:22:29
 */
public class ListModel extends BaseResponse{
//    int listType;
//    int listId;

    
    private int pageIndex;
//    int listCount;
    private int itemCount;
//    int totalCount;
    
    private boolean hasNext;
    
//    List<BaseComicItem>[] listArray;
    private List<BaseComicItem> itemList;

    /**
     * @return the pageIndex
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * @param pageIndex the pageIndex to set
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * @return the itemCount
     */
    public int getItemCount() {
//        return itemCount;
    	if(itemList == null){
    		return 0;
    	}
    	return itemList.size();
    }

    /**
     * @param itemCount the itemCount to set
     */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * @return the hasNext
     */
    public boolean isHasNext() {
        return hasNext;
    }

    /**
     * @param hasNext the hasNext to set
     */
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    /**
     * @return the itemList
     */
    public List<BaseComicItem> getItemList() {
        return itemList;
    }

    /**
     * @param itemList the itemList to set
     */
    public void setItemList(List<BaseComicItem> itemList) {
        this.itemList = itemList;
    }

}
