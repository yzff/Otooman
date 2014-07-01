package com.manyanger.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manyanger.GlobalData;
import com.manyanger.common.ToastUtil;
import com.manyanger.data.OnDataLoadedListener;
import com.manyanger.data.PictureListDataLoader;
import com.manyanger.entries.ChapterDetail;
import com.manyanger.ui.widget.ReaderPagerAdapter;
import com.manyanger.ui.widget.TipDialog;
import com.manyounger.otooman.R;

public class ComicReaderActivity extends Activity implements OnDataLoadedListener {
    
    private final static String TAG="ReaderActivity";

    private Context context;
    private ViewPager viewPager;
    private RelativeLayout topBar;
    
    private Animation animation;
    
    private int chapterId;
    private String chapterName;
    private ImageView backBtn;
    private TextView titleView;
    private TextView pageNoView;
    private ChapterDetail chapterInfo;
    private boolean barshowed;
    
    private int currPos = 0;
    
    private PictureListDataLoader pictureListDataLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.context = this;
//        this.layoutInflater = LayoutInflater.from(this.context);
        setContentView(R.layout.reader_flur);
        
        this.chapterId = getIntent().getIntExtra(GlobalData.STR_CHAPTERID, 0);
        this.chapterName = getIntent().getStringExtra(GlobalData.STR_TITLE);

        initView();

        setListener();
        
        TipDialog.showWait(this, null);
        
        pictureListDataLoader = new PictureListDataLoader(this);
        pictureListDataLoader.loadPictureList(chapterId);

    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
        int id = intent.getIntExtra(GlobalData.STR_CHAPTERID, 0);
        if(id == this.chapterId){
        	return;
        }
        this.chapterId = id;
        this.chapterName = intent.getStringExtra(GlobalData.STR_TITLE);
        
        TipDialog.showWait(this, null);
        pictureListDataLoader.loadPictureList(chapterId);
        
        titleView.setText(chapterName);
        this.viewPager.setAdapter(null);

	}
	
	
    public void initView() {

        this.viewPager = ((ViewPager) findViewById(R.id.detail_view_pager));
        this.topBar = ((RelativeLayout) findViewById(R.id.top_bar));
        backBtn = (ImageView)findViewById(R.id.back_button);
        titleView = (TextView)findViewById(R.id.title);
        pageNoView = (TextView)findViewById(R.id.page_index);
        backBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                finish();
                
            }
            
        });
        titleView.setText(chapterName);

    }


    private void setListener() {
        this.viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
                //
            }

            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                //MediaPlayerUtil.play(MagazineDetailActivity.this);
                dismissBar();
                currPos = arg0;
            }

        });
    }


    public void barEvent() {
        if (!barshowed) {
            ComicReaderActivity.this.showBar();
        } else {
            ComicReaderActivity.this.dismissBar();
        }

    }


    private boolean getBarVisible() {
        return (this.topBar.getVisibility() == 0);
    }

    public void dismissBar() {
        if (this.topBar.getVisibility() == 0) {
            this.animation = AnimationUtils.loadAnimation(this.context, R.anim.top_bar_translate_out);
            this.topBar.startAnimation(this.animation);
            this.topBar.setVisibility(4);
        }

        this.barshowed = false;
    }

    private void showBar() {
        if (this.topBar.getVisibility() != 0) {
            this.pageNoView.setText(getPageNoStr(this.currPos+1));
            this.animation = AnimationUtils.loadAnimation(this.context, R.anim.top_bar_translate_in);
            this.topBar.startAnimation(this.animation);
            this.topBar.setVisibility(0);


        }

        this.barshowed = true;

    }

//    private String num2Str(String paramString, char paramChar, int paramInt) {
//        String str = paramString;
//        int i = paramInt - paramString.length();
//        if (paramInt <= 0) {
//            if (paramString.length() > paramInt)
//
//                for (int j = 0; j < i; j++)
//                    str = paramChar + str;
//        }
//        return str;
//    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }


    private String getPageNoStr(int pos){
        if(chapterInfo == null){
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(pos);
            sb.append("/");
            sb.append(chapterInfo.getImageCount());
            
            return sb.toString();
        }
    }

    @Override
    public void OnDataLoaded(Message msg) {
        if(msg.what == GlobalData.NOTIFY_PICTURELIST_LOADED && msg.arg1 == chapterId){
            chapterInfo = (ChapterDetail)msg.obj;
            TipDialog.dissmissTip();
            if(chapterInfo != null){
                titleView.setText(chapterInfo.getTitle());
                ReaderPagerAdapter mDetailViewPagerAdapter = new ReaderPagerAdapter(this);
                this.viewPager.setAdapter(mDetailViewPagerAdapter);
                mDetailViewPagerAdapter.setData(chapterInfo.getImages());

            } else {
                ToastUtil.showToast(R.string.no_net_hit);
            }

        }
    }

    @Override
    public void onBackPressed()
    {
    	finish();
    }
}
