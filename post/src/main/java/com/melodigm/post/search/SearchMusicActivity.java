package com.melodigm.post.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.analytics.HitBuilders;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.BaseApplication;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.AddCabinetMusicItem;
import com.melodigm.post.protocol.data.AddCabinetMusicReq;
import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.protocol.data.GetSearchOstRelatedReq;
import com.melodigm.post.protocol.data.GetSearchOstRelatedRes;
import com.melodigm.post.protocol.data.GetSearchOstReq;
import com.melodigm.post.protocol.data.GetSearchOstRes;
import com.melodigm.post.protocol.data.MusPopKeyWordItem;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.OstRelatedItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.ColorIndicator;
import com.melodigm.post.widget.ColorIndicatorView;
import com.melodigm.post.widget.SwipingViewPager;

import java.util.ArrayList;

public class SearchMusicActivity extends BaseActivity implements IOnHandlerMessage, AbsListView.OnScrollListener {
    private String mBXI = "";
    private GetSearchOstRelatedRes getSearchOstRelatedRes;
    private GetSearchOstRes getSearchOstRes;
    private ColorItem mColorItem;
	private EditText etOstName;
    private TextView btnDeleteRecentAll, tvSong, tvAlbum, tvArti, tvSongCount, tvAlbumCount, tvArtiCount, tvMusicColor, tvPopSearchWordBtn, tvLastSearchWordBtn, tvChoiceColorHeaderTitle;
    private ImageView ivMusicColorCircle;
    private boolean isMusicPopShow = true;

    private LinearLayout searchNoitemLayout, llSearchMusicPopListLayout, llSearchRecentListLayout, searchRelatedListLayout, llCountLayout, songLayout, albumLayout, artiLayout, llMusicColorBtn, mChoiceColorLayout, llPopLastLayout;
    private ColorIndicatorView mColorIndicatorView;
    private ArrayList<ColorItem> arrColorItem;
    private ArrayList<MusPopKeyWordItem> arrMusPopKeyWordItem;
    private MusicPopAdapter mMusicPopAdapter = null;            // 인기 검색어 리스트 어뎁터
    private RecentAdapter mRecentAdapter = null;            // 최근검색어 리스트 어뎁터
    private RelatedAdapter mRelatedAdapter = null;          // 자동완성 검색어 리스트 어뎁터
    private SearchOstPagerAdapter mSearchOstPagerAdapter = null; // SONG 리스트 어뎁터
    private ListView lvMusicPopList, lvRecentList, lvRelatedList;
    private RelativeLayout viewPagerLayout, btnPopupCloseLayout;
    private SwipingViewPager viewPager;
    private ArrayList<AddCabinetMusicItem> arrAddCabinetMusicItem;

    private InputMethodManager mInputMethodManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        arrColorItem = mdbHelper.getAllPostColors();
        arrMusPopKeyWordItem = mdbHelper.getAllMusPopKeyWords();
        mdbHelper.close();

        Intent intent = getIntent();
        mBXI = intent.getStringExtra("BXI");
        arrAddCabinetMusicItem = new ArrayList<>();

        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getString(R.string.search), false);

        searchNoitemLayout = (LinearLayout)findViewById(R.id.searchNoitemLayout);
        llSearchMusicPopListLayout = (LinearLayout)findViewById(R.id.llSearchMusicPopListLayout);
        llSearchRecentListLayout = (LinearLayout)findViewById(R.id.llSearchRecentListLayout);
        searchRelatedListLayout = (LinearLayout)findViewById(R.id.searchRelatedListLayout);

        llPopLastLayout = (LinearLayout)findViewById(R.id.llPopLastLayout);
        llCountLayout = (LinearLayout)findViewById(R.id.llCountLayout);

        tvPopSearchWordBtn = (TextView)findViewById(R.id.tvPopSearchWordBtn);
        tvPopSearchWordBtn.setOnClickListener(onClickListener);

        tvLastSearchWordBtn = (TextView)findViewById(R.id.tvLastSearchWordBtn);
        tvLastSearchWordBtn.setOnClickListener(onClickListener);

        llMusicColorBtn = (LinearLayout)findViewById(R.id.llMusicColorBtn);
        llMusicColorBtn.setOnClickListener(onClickListener);

        tvMusicColor = (TextView)findViewById(R.id.tvMusicColor);
        ivMusicColorCircle = (ImageView)findViewById(R.id.ivMusicColorCircle);
        ivMusicColorCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFFFFFFF")));

        btnPopupCloseLayout = (RelativeLayout)findViewById(R.id.btnPopupCloseLayout);
        btnPopupCloseLayout.setOnClickListener(onClickListener);

        mChoiceColorLayout = (LinearLayout)findViewById(R.id.choiceColorLayout);
        tvChoiceColorHeaderTitle = (TextView)findViewById(R.id.tvChoiceColorHeaderTitle);
        tvChoiceColorHeaderTitle.setText(getString(R.string.msg_music_color_info));
        mColorIndicatorView = (ColorIndicatorView)findViewById(R.id.choiceColorIndicator);
        if (mColorIndicatorView != null && arrColorItem.size() > 0) {
            mColorIndicatorView.addAllItems(arrColorItem);
            mColorItem = arrColorItem.get(0);
        }
        mColorIndicatorView.setOnChangeTabListener(new ColorIndicator() {
            @Override
            public void changeTabIndicator(ColorItem item) {
                mColorItem = item;
                tvMusicColor.setTextColor(Color.parseColor("#FF" + mColorItem.getCOLOR_CODE()));
                ivMusicColorCircle.setBackground(new ColorCircleDrawable(Integer.parseInt(mColorItem.getCOLOR_CODE(), 16) + 0xFF000000));
            }
        });

        etOstName = (EditText)findViewById(R.id.etOstName);

        etOstName.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        String searchWord = etOstName.getText().toString();
                        if (CommonUtil.isNotNull(searchWord)) {
                            updateSearchWord(searchWord);
                            getData(Constants.QUERY_OST_SEARCH, searchWord);
                        }
                        break;
                }
                return true;
            }
        });

        etOstName.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchKeyword = etOstName.getText().toString();

                if (CommonUtil.isNotNull(searchKeyword)) {
                    getData(Constants.QUERY_OST_RELATED, searchKeyword);
                } else {
                    updateRecentSearchData();
                }
            }
        });

        songLayout = (LinearLayout)findViewById(R.id.songLayout);
        albumLayout = (LinearLayout)findViewById(R.id.albumLayout);
        artiLayout = (LinearLayout)findViewById(R.id.artiLayout);
        tvSong = (TextView)findViewById(R.id.tvSong);
        tvAlbum = (TextView)findViewById(R.id.tvAlbum);
        tvArti = (TextView)findViewById(R.id.tvArti);
        tvSongCount = (TextView)findViewById(R.id.tvSongCount);
        tvAlbumCount = (TextView)findViewById(R.id.tvAlbumCount);
        tvArtiCount = (TextView)findViewById(R.id.tvArtiCount);
        viewPagerLayout = (RelativeLayout)findViewById(R.id.viewPagerLayout);

        viewPager = (SwipingViewPager)findViewById(R.id.viewPager);
        mSearchOstPagerAdapter = new SearchOstPagerAdapter(mContext, getLayoutInflater(), mHandler, mGlideRequestManager, etOstName);
        viewPager.setAdapter(mSearchOstPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    switch (viewPager.getCurrentItem()) {
                        case 0:
                            tvSong.setTextColor(Color.parseColor("#FF3399FF"));
                            tvAlbum.setTextColor(Color.parseColor("#FFFFFFFF"));
                            tvArti.setTextColor(Color.parseColor("#FFFFFFFF"));
                            break;
                        case 1:
                            tvSong.setTextColor(Color.parseColor("#FFFFFFFF"));
                            tvAlbum.setTextColor(Color.parseColor("#FF3399FF"));
                            tvArti.setTextColor(Color.parseColor("#FFFFFFFF"));
                            break;
                        case 2:
                            tvSong.setTextColor(Color.parseColor("#FFFFFFFF"));
                            tvAlbum.setTextColor(Color.parseColor("#FFFFFFFF"));
                            tvArti.setTextColor(Color.parseColor("#FF3399FF"));
                            break;
                    }
                }
            }
        });

        songLayout.setOnClickListener(onClickListener);
        albumLayout.setOnClickListener(onClickListener);
        artiLayout.setOnClickListener(onClickListener);

        // 인기 검색어
        mMusicPopAdapter = new MusicPopAdapter(mContext, arrMusPopKeyWordItem);
        mMusicPopAdapter.setOnListItemClickListener(new MusicPopAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(MusPopKeyWordItem item) {
                if (item != null) {
                    updateSearchWord(item.getKEYWORD());
                    etOstName.setText(item.getKEYWORD());
                    getData(Constants.QUERY_OST_SEARCH, item.getKEYWORD());
                }
            }
        });
        lvMusicPopList = (ListView)findViewById(R.id.lvMusicPopList);
        lvMusicPopList.setOnScrollListener(this);
        lvMusicPopList.setAdapter(mMusicPopAdapter);

        // 최근검색
        mRecentAdapter = new RecentAdapter(mContext);
        mRecentAdapter.setOnListItemClickListener(new RecentAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(String word, int position) {
                updateSearchWord(word);
                etOstName.setText(word);
                getData(Constants.QUERY_OST_SEARCH, word);
            }

            @Override
            public void onItemDeleteClick(String word, int position) {
                //최근 검색어 제거하기!!
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                mdbHelper.deleteRecentSearchWord(word);
                mdbHelper.close();

                mRecentAdapter.deleteItem(position);
                mRecentAdapter.notifyDataSetChanged();

                if(mRecentAdapter.getCount() == 0){
                    searchNoitemLayout.setVisibility(View.VISIBLE);
                    llSearchRecentListLayout.setVisibility(View.GONE);
                    llSearchMusicPopListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.GONE);
                }
            }
        });
        lvRecentList = (ListView)findViewById(R.id.lvRecentList);
        lvRecentList.setOnScrollListener(this);
        lvRecentList.setAdapter(mRecentAdapter);

        // 연관검색
        mRelatedAdapter = new RelatedAdapter(mContext);
        mRelatedAdapter.setOnListItemClickListener(new RelatedAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(OstRelatedItem ostRelatedItem) {
                updateSearchWord(ostRelatedItem.getKEYWORD());
                etOstName.setText(ostRelatedItem.getKEYWORD());
                getData(Constants.QUERY_OST_SEARCH, ostRelatedItem.getKEYWORD(), ostRelatedItem.getTYPE());
            }
        });
        lvRelatedList = (ListView)findViewById(R.id.lvRelatedList);
        lvRelatedList.setOnScrollListener(this);
        lvRelatedList.setAdapter(mRelatedAdapter);

        btnDeleteRecentAll = (TextView)findViewById(R.id.btnDeleteRecentAll);
        btnDeleteRecentAll.setOnClickListener(onClickListener);

        updateRecentSearchData();
	}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mInputMethodManager.hideSoftInputFromWindow(etOstName.getWindowToken(), 0);
    }

    private void updateSearchWord(String searchWord) {
        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        mdbHelper.updateSearchWord(searchWord);
        mdbHelper.close();
    }

    public void updateRecentSearchData(){
        mRecentAdapter.deleteAllItems();
        mRecentAdapter.setDeleteBtnVisibility(true);

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        ArrayList<String> data = mdbHelper.getAllRecentSearchWords();
        mdbHelper.close();
        mRecentAdapter.addAllItems(data);

        if(mRecentAdapter.getCount() == 0){
            searchNoitemLayout.setVisibility(View.VISIBLE);
            llSearchRecentListLayout.setVisibility(View.GONE);
            llSearchMusicPopListLayout.setVisibility(View.GONE);
            searchRelatedListLayout.setVisibility(View.GONE);
        } else {
            searchNoitemLayout.setVisibility(View.GONE);

            if (isMusicPopShow) {
                llSearchMusicPopListLayout.setVisibility(View.VISIBLE);
                llSearchRecentListLayout.setVisibility(View.GONE);
            } else {
                llSearchMusicPopListLayout.setVisibility(View.GONE);
                llSearchRecentListLayout.setVisibility(View.VISIBLE);
            }

            searchRelatedListLayout.setVisibility(View.GONE);
        }

        mRecentAdapter.notifyDataSetChanged();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // 모두삭제
                case R.id.btnDeleteRecentAll:
                    if(mRecentAdapter == null || mRecentAdapter.getCount() == 0) return;
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    //최근 검색어 데이터 호출?!?!
                    //DB에 저장된 최근 검색어 모두삭제
                    mdbHelper.deleteAllRecentSearchWords();
                    mdbHelper.close();
                    mRecentAdapter.deleteAllItems();
                    mRecentAdapter.notifyDataSetChanged();

                    searchNoitemLayout.setVisibility(View.VISIBLE);
                    llSearchRecentListLayout.setVisibility(View.GONE);
                    break;
                // 인기 검색어 onClick
                case R.id.tvPopSearchWordBtn:
                    isMusicPopShow = true;
                    tvPopSearchWordBtn.setAlpha(1.0f);
                    tvLastSearchWordBtn.setAlpha(0.3f);
                    llSearchMusicPopListLayout.setVisibility(View.VISIBLE);
                    llSearchRecentListLayout.setVisibility(View.GONE);
                    break;
                // 최근 검색어 onclick
                case R.id.tvLastSearchWordBtn:
                    isMusicPopShow = false;
                    tvPopSearchWordBtn.setAlpha(0.3f);
                    tvLastSearchWordBtn.setAlpha(1.0f);
                    llSearchMusicPopListLayout.setVisibility(View.GONE);
                    llSearchRecentListLayout.setVisibility(View.VISIBLE);
                    break;
                // 컬러 선택 onClick
                case R.id.llMusicColorBtn:
                    if (mChoiceColorLayout.getVisibility() == View.GONE) {
                        Animation animation = new AlphaAnimation(0.0f, 1.0f);
                        animation.setDuration(500);
                        mChoiceColorLayout.setVisibility(View.VISIBLE);
                        mChoiceColorLayout.setAnimation(animation);
                    } else {
                        Animation animation = new AlphaAnimation(1.0f, 0.0f);
                        animation.setDuration(500);
                        mChoiceColorLayout.setVisibility(View.GONE);
                        mChoiceColorLayout.setAnimation(animation);
                    }
                    break;
                // 컬러 팝업 닫기 onClick
                case R.id.btnPopupCloseLayout :
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);
                    mChoiceColorLayout.setVisibility(View.GONE);
                    mChoiceColorLayout.setAnimation(animation);
                    break;
                // 곡
                case R.id.songLayout:
                    viewPager.setCurrentItem(0);
                    break;
                // 앨범
                case R.id.albumLayout:
                    viewPager.setCurrentItem(1);
                    break;
                // 아티스트
                case R.id.artiLayout:
                    viewPager.setCurrentItem(2);
                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        if (queryType == Constants.QUERY_OST_RELATED) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                searchOstRelatedThread((String)args[0]);
            }
        } else if (queryType == Constants.QUERY_OST_SEARCH) {
            if (!isFinishing()) {
                if(mProgressDialog != null) {
                    mProgressDialog.showDialog(mContext);
                }
            }

            searchOstThread(args);
        } else if (queryType == Constants.QUERY_ADD_CABINET) {
            addCabinetMusicThread();
        }
	}

    public Thread searchOstRelatedThread(final String word){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetSearchOstRelatedReq getSearchOstRelatedReq = new GetSearchOstRelatedReq();
                    getSearchOstRelatedReq.setKEYWORD(word);

                    getSearchOstRelatedRes = request.getSearchOstRelated(getSearchOstRelatedReq, "");
                    mHandler.sendEmptyMessage(Constants.QUERY_OST_RELATED_VIEW);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public Thread searchOstThread(final Object... args){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    Bundle data = new Bundle();
                    Message msg = new Message();

                    GetSearchOstReq getSearchOstReq = new GetSearchOstReq();
                    if (args != null && args.length > 0 && args[0] instanceof String)
                        getSearchOstReq.setKEYWORD((String)args[0]);
                    if (args != null && args.length > 1 && args[1] instanceof String) {
                        getSearchOstReq.setTYPE((String) args[1]);
                        data.putString("TYPE", (String) args[1]);
                    }
                    if (!"00000000000000FFFFFF".equals(mColorItem.getICI()))
                        getSearchOstReq.setICI(mColorItem.getICI());
                    getSearchOstRes = request.getSearchOst(getSearchOstReq, "");

                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.GA_CATEGORY_SEARCH)
                            .setAction(Constants.GA_ACTION_SEARCH_MUSIC)
                            .setLabel(getSearchOstReq.getKEYWORD())
                            .build());

                    msg.setData(data);
                    msg.what = Constants.QUERY_OST_DATA_VIEW;
                    mHandler.sendMessage(msg);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public Thread addCabinetMusicThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    arrAddCabinetMusicItem.clear();
                    for (OstDataItem item : getSearchOstRes.getSongDataItem()) {
                        if (item.isChecked()) {
                            AddCabinetMusicItem addCabinetMusicItem = new AddCabinetMusicItem();
                            addCabinetMusicItem.setSSI(item.getSSI());
                            addCabinetMusicItem.setOTI(item.getOTI());
                            arrAddCabinetMusicItem.add(addCabinetMusicItem);
                        }
                    }

                    AddCabinetMusicReq addCabinetMusicReq = new AddCabinetMusicReq();
                    addCabinetMusicReq.setBXI(mBXI);
                    addCabinetMusicReq.setAddCabinetMusicItem(arrAddCabinetMusicItem);
                    request.addCabinetMusic(addCabinetMusicReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_ADD_CABINET);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            // 연관 검색 후 Handler
            case Constants.QUERY_OST_RELATED_VIEW:
                String word = etOstName.getText().toString();
                if(getSearchOstRelatedRes.getOstRelatedItem().size() > 0) {
                    mRelatedAdapter.setWord(word);
                    mRelatedAdapter.addAllItems(getSearchOstRelatedRes.getOstRelatedItem());
                    mRelatedAdapter.notifyDataSetChanged();
                    searchNoitemLayout.setVisibility(View.GONE);
                    llSearchRecentListLayout.setVisibility(View.GONE);
                    llSearchMusicPopListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.VISIBLE);
                } else {
                    mRelatedAdapter.deleteAllItems();
                    mRelatedAdapter.notifyDataSetChanged();
                    searchNoitemLayout.setVisibility(View.VISIBLE);
                    llSearchRecentListLayout.setVisibility(View.GONE);
                    llSearchMusicPopListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.GONE);
                }
                break;
            // 노래 검색 후 Handler
            case Constants.QUERY_OST_DATA_VIEW:
                tvSongCount.setText(String.valueOf(getSearchOstRes.getSONG_CNT()));
                tvAlbumCount.setText(String.valueOf(getSearchOstRes.getALBUM_CNT()));
                tvArtiCount.setText(String.valueOf(getSearchOstRes.getARTI_CNT()));
                mSearchOstPagerAdapter.addAllItems(getSearchOstRes);

                searchNoitemLayout.setVisibility(View.GONE);
                llSearchRecentListLayout.setVisibility(View.GONE);
                llSearchMusicPopListLayout.setVisibility(View.GONE);
                searchRelatedListLayout.setVisibility(View.GONE);

                llCountLayout.setVisibility(View.VISIBLE);
                llPopLastLayout.setVisibility(View.GONE);

                viewPagerLayout.setVisibility(View.VISIBLE);

                Bundle data = msg.getData();
                String TYPE = data.getString("TYPE", "SONG");
                if ("SONG".equalsIgnoreCase(TYPE)) {
                    tvSong.setTextColor(Color.parseColor("#FF3399FF"));
                    viewPager.setCurrentItem(0);
                } else if ("ALBUM".equalsIgnoreCase(TYPE)) {
                    tvAlbum.setTextColor(Color.parseColor("#FF3399FF"));
                    viewPager.setCurrentItem(1);
                } else if ("ARTI".equalsIgnoreCase(TYPE)) {
                    tvArti.setTextColor(Color.parseColor("#FF3399FF"));
                    viewPager.setCurrentItem(2);
                }
                break;
            // 보관함에 담기 후 Handler
            case Constants.QUERY_ADD_CABINET:
                DeviceUtil.showToast(mContext, getString(R.string.msg_add_cabinet_result));
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Footer > 담기 > 보관함 이동 후 ActivityResult
            case Constants.QUERY_CABINET_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    String mCabinetType = data.getStringExtra(Constants.REQUEST_CABINET_TYPE);
                    if (CommonUtil.isNotNull(mCabinetType)) {
                        if (Constants.REQUEST_CABINET_TYPE_PUT.equals(mCabinetType)) {
                            mBXI = data.getStringExtra("BXI");
                            if (CommonUtil.isNotNull(mBXI)) {
                                getData(Constants.QUERY_ADD_CABINET);
                            }
                        }
                    }
                }
                break;
        }
    }
}
