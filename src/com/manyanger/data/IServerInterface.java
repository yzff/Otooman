package com.manyanger.data;

/**
 * @ClassName: IServerInterface
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-14 下午4:14:07
 */
public interface IServerInterface {
    
    // 服务器地址
    public static final String SERVER_URL = "http://api.manyanger.com:8101/otooman/";
//    public static final String SERVER_URL = "http://test.manyanger.com:8101/otooman/";
    
    //    首页
    public static final String CMD_FIRSTPAGE = "index.htm";  
    public static final String CMD_INDEXLIST = "indexList.htm";
    //    更新全部 分页（pageNo：页码 type:类型）
//    public static final String NEWESTLIST_CMD = "indexList.htm";
    //    精选全部 分页（pageNo：页码 type:类型）
//    public static final String FEATUREDLIST_CMD = "indexList.htm?backMode=JSON&type=2";
    //    推荐全部 分页（pageNo：页码 type:类型）
//    public static final String RECOMMENDLIST_CMD = "indexList.htm?backMode=JSON&type=3";
    //    全部漫画 分页（pageNo：页码）
    public static final String CMD_COMICLIST = "comicList.htm";
    //    搜索漫画 分页（pageNo：页码 keyWord：搜索关键字）
    public static final String CMD_SEARCH = "comicList.htm";
    //    题材漫画 分页（pageNo：页码 theme：题材编号）
    public static final String CMD_THEMECONTENT = "comicList.htm";
    //    漫画详细（id：漫画编号）
    public static final String CMD_DETAIL = "comicDetail.htm";
    //    漫画阅读（chapterId：章节编号）
    public static final String CMD_READ = "comicRead.htm";
    //    漫画题材
    public static final String CMD_THEMELIST = "comicTheme.htm";
    
    public static final String CMD_CHAPTERLIST = "chapterList.htm";
    
    // 页码
    public static final String PARAMS_PAGENO = "pageNo=";
    // 分页大小
    public static final String PARAMS_PAGESIZE = "&pageSize=15";
    // 漫画id
    public static final String PARAMS_COMICID = "id=";
    // 章节id
    public static final String PARAMS_CHAPTERID = "chapterId=";
    // 题材
    public static final String PARAMS_THEME = "theme=";
    // 搜索关键字
    public static final String PARAMS_KEYWORD = "keyWord=";
    // 首页列表类型
    public static final String PARAMS_TYPE = "type=";
}
