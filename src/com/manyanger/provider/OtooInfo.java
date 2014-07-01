package com.manyanger.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 * 数据表定义
 * 
 * @author fred.ma
 * 
 */
public interface OtooInfo extends BaseColumns {
	String AUTHORITY = "com.manyanger.provider";
	Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * 阅读记录表
	 * 
	 * @author fred.ma
	 * 
	 */
	public interface redLog extends BaseColumns {
		String TABLE_NAME = "redLog";

		Uri CONTENT_URI = Uri
				.withAppendedPath(OtooInfo.CONTENT_URI, TABLE_NAME);

		String CONTENT_TYPE = "vnd.android.cursor.dir/redLog";

		/**
		 * 漫画ID
		 */
		String COL_BOOK_ID = "book_id";
		/**
		 * 章节ID
		 */
		String COL_CHAPTER_ID = "chapter_id";
		/**
		 * 第几页
		 */
		String COL_PAGE_INDEX = "page_index";
		/**
		 * 用户ID 保留字段
		 */
		String USER_ID = "user_id";
	}
	/**
	 * 收藏表
	 * 
	 * @author fred.ma
	 * 
	 */
	public interface favorite extends BaseColumns {
		String TABLE_NAME = "favorite";
		
		Uri CONTENT_URI = Uri
		.withAppendedPath(OtooInfo.CONTENT_URI, TABLE_NAME);
		
		String CONTENT_TYPE = "vnd.android.cursor.dir/favorite";
		
		/**
		 * 漫画ID
		 */
		String COL_BOOK_ID = "book_id";
		/**
		 * 名称
		 */
		String COL_NAME = "name";
		/**
		 * 作者
		 */
		String COL_AUTHOR = "author";
		/**
		 * 更新章节数
		 */
		String COL_CHAPTER_COUT = "chapter_count";
		/**
		 * 更新状态
		 */
		String COL_PROCESS = "process";
		/**
		 * 漫画封页
		 */
		String COL_COVER = "cover";
	}
}
