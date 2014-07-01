package com.manyanger.data;

import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.manyanger.GlobalData;
import com.manyanger.common.ThreadPool;
import com.manyanger.data.loader.ComicItemsLoader;
import com.manyanger.data.net.DataException;
import com.manyanger.entries.ChapterItem;
import com.manyanger.entries.DetailInfo;

/**
 * @ClassName: DetailDataLoader
 * @Description: TODO
 * @author Zephan.Yu
 * @date 2014-6-20 下午11:10:27
 */
public class ChapterDataLoader {
	private Handler mHandler;

	// 列表加载完成的监听接口
	private OnDataLoadedListener mListener;

	public ChapterDataLoader(OnDataLoadedListener listener) {
		registerOnDataLoadedListener(listener);
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (mListener != null) {
					// 应用列表加载完成，回调
					mListener.OnDataLoaded(msg);
				}
			}
		};
	}

	public String loadChapterList(int comicId) {
		String cmd = DataUtils.makeCmdString(GlobalData.CMD_CHAPTERLIST,
				GlobalData.PARAMS_COMICID, comicId);
		ThreadPool.submitText(new LoadDataRunnable(cmd));

		return cmd;
	}

	public String loadComicDetail(String url) {
		ThreadPool.submitText(new LoadDataRunnable(url));

		return url;
	}

	private class LoadDataRunnable implements Runnable {
		String cmdLine;

		public LoadDataRunnable(String cmd) {
			this.cmdLine = cmd;

		}

		@Override
		public void run() {

			String resultStr = null;
			List<ChapterItem> chapterItems = null;
			try {
				resultStr = ComicItemsLoader.load(cmdLine);
			} catch (DataException e) {
				resultStr = null;
			} catch (Exception e) {
				resultStr = null;
			}

			if (resultStr == null) {

			} else {
				chapterItems = JsonParser.parseChapterList(resultStr);
			}

			if (mHandler == null) {
				return;
			}
			// 准备回调
			Message message = mHandler.obtainMessage();
			message.what = GlobalData.NOTIFY_COMICDETAIL_LOADED;
			// message.arg1 = listId;
			message.obj = chapterItems;
			mHandler.sendMessage(message);
		}

	}

	public void registerOnDataLoadedListener(OnDataLoadedListener listener) {
		mListener = listener;
	}

	public void unregisterOnDataLoadedListener(OnDataLoadedListener listener) {
		mListener = null;
		if (mHandler != null) {
			mHandler = null;
		}
	}

}
