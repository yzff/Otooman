package com.manyanger.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * 数据库操作封装
 * 
 * @author fred.ma
 * 
 */
public class OtooProvider extends ContentProvider {
	private static final String LOG_TAG = OtooProvider.class.getSimpleName();
	private OtooDBhelper dbHelper;

	private static final int RED_LOG = 1;
	private static final int FAVORITE = 2;

	private static final UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(OtooInfo.AUTHORITY, OtooInfo.redLog.TABLE_NAME,
				RED_LOG);
		sUriMatcher.addURI(OtooInfo.AUTHORITY, OtooInfo.favorite.TABLE_NAME,
				FAVORITE);
	};

	@Override
	public boolean onCreate() {
		dbHelper = new OtooDBhelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		try {
			final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			final SQLiteDatabase db = dbHelper.getReadableDatabase();
			switch (sUriMatcher.match(uri)) {
			case RED_LOG:
				qb.setTables(OtooInfo.redLog.TABLE_NAME);
				break;
			case FAVORITE:
				qb.setTables(OtooInfo.favorite.TABLE_NAME);
				break;
			default:
				break;
			}
			final Cursor cursor = qb.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);
			if (cursor != null) {
				cursor.setNotificationUri(getContext().getContentResolver(),
						uri);
			}
			return cursor;
		} catch (Exception e) {
			Log.e(LOG_TAG, "query error", e);
			return null;
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case RED_LOG:
			return OtooInfo.redLog.CONTENT_TYPE;
		case FAVORITE:
			return OtooInfo.favorite.CONTENT_TYPE;

		default:
			Log.e(LOG_TAG, "getType Unknown URI: " + uri);
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		try {
			final SQLiteDatabase db = dbHelper.getWritableDatabase();
			long rowId;
			Uri resultUri = null;
			switch (sUriMatcher.match(uri)) {
			case RED_LOG:
				rowId = db.insert(OtooInfo.redLog.TABLE_NAME, null, values);
				if (rowId > 0) {
					resultUri = ContentUris.withAppendedId(
							OtooInfo.redLog.CONTENT_URI, rowId);
				}
				break;
			case FAVORITE:
				rowId = db.insert(OtooInfo.favorite.TABLE_NAME, null, values);
				if (rowId > 0) {
					resultUri = ContentUris.withAppendedId(
							OtooInfo.favorite.CONTENT_URI, rowId);
				}
				break;
			default:
				break;
			}
			if (resultUri != null) {
				getContext().getContentResolver().notifyChange(resultUri, null);
				return resultUri;
			}

			return resultUri;
		} catch (Exception e) {
			Log.e(LOG_TAG, "insert error", e);
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		try {
			final SQLiteDatabase db = dbHelper.getWritableDatabase();
			int count = 0;
			switch (sUriMatcher.match(uri)) {
			case RED_LOG:
				count = db.delete(OtooInfo.redLog.TABLE_NAME, selection,
						selectionArgs);
				break;
			case FAVORITE:
				count = db.delete(OtooInfo.favorite.TABLE_NAME, selection,
						selectionArgs);
				break;
			default:
				Log.e(LOG_TAG, "delete Unknown URI: " + uri);
				break;
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		} catch (Exception e) {
			Log.e(LOG_TAG, "delete error", e);
			return 0;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		try {
			final SQLiteDatabase db = dbHelper.getWritableDatabase();
			int count = 0;
			switch (sUriMatcher.match(uri)) {
			case RED_LOG:
				count = db.update(OtooInfo.redLog.TABLE_NAME, values,
						selection, selectionArgs);
				break;
			case FAVORITE:
				count = db.update(OtooInfo.favorite.TABLE_NAME, values,
						selection, selectionArgs);
				break;
			default:
				Log.e(LOG_TAG, "update Unknown URI: " + uri);
				break;
			}
			 getContext().getContentResolver().notifyChange(uri, null);
			return count;
		} catch (Exception e) {
			Log.e(LOG_TAG, "update error", e);
			return 0;
		}
	}

}
