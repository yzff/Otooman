package com.manyanger.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库实现
 * 
 * @author fred.ma
 * 
 */
public class OtooDBhelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "outman.db";

	private static final int DATABASE_VERSION = 1;

	public OtooDBhelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	private void createTable(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + OtooInfo.redLog.TABLE_NAME + " (" +

		OtooInfo.redLog._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

		OtooInfo.redLog.COL_BOOK_ID + " INTEGER  NOT NULL," +

		OtooInfo.redLog.COL_CHAPTER_ID + " INTEGER NOT NULL," +

		OtooInfo.redLog.COL_PAGE_INDEX + " INTEGER NOT NULL);");

		db.execSQL("CREATE TABLE " + OtooInfo.favorite.TABLE_NAME + " (" +

		OtooInfo.favorite._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

		OtooInfo.favorite.COL_BOOK_ID + " INTEGER  NOT NULL," +

		OtooInfo.favorite.COL_NAME + " TEXT," + 
		
		OtooInfo.favorite.COL_AUTHOR+ " TEXT," + 
		
		OtooInfo.favorite.COL_PROCESS + " TEXT,"+ 
		
		OtooInfo.favorite.COL_COVER + " TEXT," +

		OtooInfo.favorite.COL_CHAPTER_COUT + " INTEGER);");

	}

}
