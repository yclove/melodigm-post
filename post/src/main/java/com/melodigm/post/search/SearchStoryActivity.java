package com.melodigm.post.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.melodigm.post.PostDetailActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.protocol.data.GetPostDataRes;
import com.melodigm.post.protocol.data.GetSearchOstRelatedReq;
import com.melodigm.post.protocol.data.GetSearchOstRelatedRes;
import com.melodigm.post.protocol.data.GetSearchStoryReq;
import com.melodigm.post.protocol.data.MusPopKeyWordItem;
import com.melodigm.post.protocol.data.OstRelatedItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.data.PostHashTagKeyWordItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.ColorIndicator;
import com.melodigm.post.widget.ColorIndicatorView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;

import java.util.ArrayList;

public class SearchStoryActivity extends BaseActivity implements IOnHandlerMessage, AbsListView.OnScrollListener {
    private GetSearchOstRelatedRes getSearchOstRelatedRes;
    private GetPostDataRes mGetPostDataRes;
    private ArrayList<PostDataItem> arrPostDataItem;
    private int mCurrentPage = 1;
    private int mHeaderTab = 1;
    private String mKeyWord = "";
    private String mPostType = "";
    private String mOrderType = "REG";
    private boolean isProgrammingRelated;

    private EditText etStoryName;
    private TextView tvDeleteRecentSearchWordAll, tvEmptyMessage, tvMusicColor, tvHashTagWordBtn, tvPopSearchWordBtn, tvLastSearchWordBtn, tvChoiceColorHeaderTitle;

    private ColorIndicatorView mColorIndicatorView;
    private ArrayList<ColorItem> arrColorItem;
    private ArrayList<PostHashTagKeyWordItem> arrPostHashTagKeyWordItem;
    private ArrayList<MusPopKeyWordItem> arrPostPopKeyWordItem;
    private ColorItem mColorItem;
    private ImageView ivMusicColorCircle;
    private RelativeLayout btnPopupCloseLayout;
    private LinearLayout llSearchStoryHeaderTabLayout, llEmptyLayout, llMusicColorBtn, llHashTagListLayout, llPopularSearchWordListLayout, llRecentSearchWordListLayout, llRelatedSearchWordListLayout, llSearchStoryListLayout, mChoiceColorLayout;
    private ListView lvHashTagList, lvPopularSearchWordList, lvRecentSearchWordList, lvRelatedSearchWordList, lvSearchStoryList;
    private PostHashTagAdapter mPostHashTagAdapter = null;   // 해시태그 리스트 어뎁터
    private MusicPopAdapter mMusicPopAdapter = null;            // 인기 검색어 리스트 어뎁터
    private RecentAdapter mRecentAdapter = null;                    // 최근검색어 리스트 어뎁터
    private RelatedAdapter mRelatedAdapter = null;                  // 자동완성 검색어 리스트 어뎁터
    private SearchStoryAdapter mSearchStoryAdapter = null;  // 이야기 검색 리스트 어뎁터

    private TextView tvStoryTotalCnt, tvStoryFilterSort;
    private LetterSpacingTextView lstvStoryFilterType;
    private LinearLayout llStoryFilterSort, llStoryFilterType;

    private InputMethodManager mInputMethodManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_story);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        arrColorItem = mdbHelper.getAllPostColors();
        arrPostHashTagKeyWordItem = mdbHelper.getAllPostHashTagKeyWords();
        arrPostPopKeyWordItem = mdbHelper.getAllPostPopKeyWords();
        mdbHelper.close();

        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        setDisplay();
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.search_story), false);

        llSearchStoryHeaderTabLayout = (LinearLayout)findViewById(R.id.llSearchStoryHeaderTabLayout);
        llEmptyLayout = (LinearLayout)findViewById(R.id.llEmptyLayout);
        tvEmptyMessage = (TextView)findViewById(R.id.tvEmptyMessage);
        llHashTagListLayout = (LinearLayout)findViewById(R.id.llHashTagListLayout);
        llPopularSearchWordListLayout = (LinearLayout)findViewById(R.id.llPopularSearchWordListLayout);
        llRecentSearchWordListLayout = (LinearLayout)findViewById(R.id.llRecentSearchWordListLayout);
        llRelatedSearchWordListLayout = (LinearLayout)findViewById(R.id.llRelatedSearchWordListLayout);
        llSearchStoryListLayout = (LinearLayout)findViewById(R.id.llSearchStoryListLayout);

        etStoryName = (EditText)findViewById(R.id.etStoryName);

        etStoryName.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        mKeyWord = etStoryName.getText().toString();
                        if (CommonUtil.isNotNull(mKeyWord)) {
                            updateSearchWord(mKeyWord);
                            getData(Constants.QUERY_STORY_SEARCH);
                        }
                        break;
                }
                return true;
            }
        });

        etStoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isProgrammingRelated) {
                    isProgrammingRelated = false;
                } else {
                    String searchKeyword = etStoryName.getText().toString();

                    if (CommonUtil.isNotNull(searchKeyword)) {
                        getData(Constants.QUERY_STORY_RELATED, searchKeyword);
                    } else {
                        updateRecentSearchData();
                    }
                }
            }
        });

        // 해시태그 버튼
        tvHashTagWordBtn = (TextView)findViewById(R.id.tvHashTagWordBtn);
        tvHashTagWordBtn.setOnClickListener(onClickListener);

        // 인기 검색어 버튼
        tvPopSearchWordBtn = (TextView)findViewById(R.id.tvPopSearchWordBtn);
        tvPopSearchWordBtn.setOnClickListener(onClickListener);

        // 최근 검색어 버튼
        tvLastSearchWordBtn = (TextView)findViewById(R.id.tvLastSearchWordBtn);
        tvLastSearchWordBtn.setOnClickListener(onClickListener);

        // 컬러 선택 버튼
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

        // 최근 검색어 > 모두 삭제 버튼
        tvDeleteRecentSearchWordAll = (TextView)findViewById(R.id.tvDeleteRecentSearchWordAll);
        tvDeleteRecentSearchWordAll.setOnClickListener(onClickListener);

        // 해시태그 리스트
        mPostHashTagAdapter = new PostHashTagAdapter(mContext, arrPostHashTagKeyWordItem);
        mPostHashTagAdapter.setOnListItemClickListener(new PostHashTagAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(PostHashTagKeyWordItem item) {
                if (item != null) {
                    mKeyWord = item.getKEYWORD();
                    updateSearchWord(mKeyWord);
                    isProgrammingRelated = true;
                    etStoryName.setText(mKeyWord);
                    getData(Constants.QUERY_STORY_SEARCH);
                }
            }
        });
        lvHashTagList = (ListView)findViewById(R.id.lvHashTagList);
        lvHashTagList.setOnScrollListener(this);
        lvHashTagList.setAdapter(mPostHashTagAdapter);

        // 인기 검색어 리스트
        mMusicPopAdapter = new MusicPopAdapter(mContext, arrPostPopKeyWordItem);
        mMusicPopAdapter.setOnListItemClickListener(new MusicPopAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(MusPopKeyWordItem item) {
                if (item != null) {
                    mKeyWord = item.getKEYWORD();
                    updateSearchWord(mKeyWord);
                    isProgrammingRelated = true;
                    etStoryName.setText(mKeyWord);
                    getData(Constants.QUERY_STORY_SEARCH);
                }
            }
        });
        lvPopularSearchWordList = (ListView)findViewById(R.id.lvPopularSearchWordList);
        lvPopularSearchWordList.setOnScrollListener(this);
        lvPopularSearchWordList.setAdapter(mMusicPopAdapter);

        // 최근 검색어 리스트
        mRecentAdapter = new RecentAdapter(mContext);
        mRecentAdapter.setOnListItemClickListener(new RecentAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(String word, int position) {
                mKeyWord = word;
                updateSearchWord(mKeyWord);
                isProgrammingRelated = true;
                etStoryName.setText(mKeyWord);
                getData(Constants.QUERY_STORY_SEARCH);
            }

            @Override
            public void onItemDeleteClick(String word, int position) {
                //최근 검색어 제거하기!!
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                mdbHelper.deletePostRecentSearchWord(word);
                mdbHelper.close();

                mRecentAdapter.deleteItem(position);
                mRecentAdapter.notifyDataSetChanged();
            }
        });
        lvRecentSearchWordList = (ListView)findViewById(R.id.lvRecentSearchWordList);
        lvRecentSearchWordList.setOnScrollListener(this);
        lvRecentSearchWordList.setAdapter(mRecentAdapter);

        // 연관검색 리스트
        mRelatedAdapter = new RelatedAdapter(mContext);
        mRelatedAdapter.setOnListItemClickListener(new RelatedAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(OstRelatedItem ostRelatedItem) {
                mKeyWord = ostRelatedItem.getKEYWORD();
                updateSearchWord(mKeyWord);
                isProgrammingRelated = true;
                etStoryName.setText(mKeyWord);
                getData(Constants.QUERY_STORY_SEARCH);
            }
        });
        lvRelatedSearchWordList = (ListView)findViewById(R.id.lvRelatedSearchWordList);
        lvRelatedSearchWordList.setOnScrollListener(this);
        lvRelatedSearchWordList.setAdapter(mRelatedAdapter);

        // 이야기 검색
        arrPostDataItem = new ArrayList<>();
        mSearchStoryAdapter = new SearchStoryAdapter(mContext, R.layout.adapter_search_story, arrPostDataItem, mGlideRequestManager);
        lvSearchStoryList = (ListView)findViewById(R.id.lvSearchStoryList);
        lvSearchStoryList.setOnScrollListener(this);
        lvSearchStoryList.setAdapter(mSearchStoryAdapter);
        lvSearchStoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                if (CommonUtil.isNotNull(mGetPostDataRes.getPostDataItemList().get(position).getPOI())) {
                    Intent intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra("POST_TYPE", mGetPostDataRes.getPostDataItemList().get(position).getPOST_TYPE());
                    intent.putExtra("POI", mGetPostDataRes.getPostDataItemList().get(position).getPOI());
                    startActivity(intent);
                }
            }
        });

        // 이야기 검색 리스트 > Header
        tvStoryTotalCnt = (TextView)findViewById(R.id.tvStoryTotalCnt);
        tvStoryFilterSort = (TextView)findViewById(R.id.tvStoryFilterSort);
        llStoryFilterSort = (LinearLayout)findViewById(R.id.llStoryFilterSort);
        llStoryFilterSort.setOnClickListener(onClickListener);
        lstvStoryFilterType = (LetterSpacingTextView) findViewById(R.id.lstvStoryFilterType);
        lstvStoryFilterType.setSpacing(0.0f);
        lstvStoryFilterType.setText(getString(R.string.all));
        llStoryFilterType = (LinearLayout)findViewById(R.id.llStoryFilterType);
        llStoryFilterType.setOnClickListener(onClickListener);

        updateRecentSearchData();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mInputMethodManager.hideSoftInputFromWindow(etStoryName.getWindowToken(), 0);
    }

    private void updateLayoutUI(LinearLayout visibleLayout) {
        if (mHeaderTab == 1) {
            tvHashTagWordBtn.setAlpha(1.0f);
            tvPopSearchWordBtn.setAlpha(0.3f);
            tvLastSearchWordBtn.setAlpha(0.3f);
        } else if (mHeaderTab == 2) {
            tvHashTagWordBtn.setAlpha(0.3f);
            tvPopSearchWordBtn.setAlpha(1.0f);
            tvLastSearchWordBtn.setAlpha(0.3f);
        } else {
            tvHashTagWordBtn.setAlpha(0.3f);
            tvPopSearchWordBtn.setAlpha(0.3f);
            tvLastSearchWordBtn.setAlpha(1.0f);
        }

        llHashTagListLayout.setVisibility(View.GONE);
        llPopularSearchWordListLayout.setVisibility(View.GONE);
        llRecentSearchWordListLayout.setVisibility(View.GONE);
        llRelatedSearchWordListLayout.setVisibility(View.GONE);
        llSearchStoryListLayout.setVisibility(View.GONE);

        if (visibleLayout != null) {
            visibleLayout.setVisibility(View.VISIBLE);

            if ("llRelatedSearchWordListLayout".equals(visibleLayout.getResources().getResourceEntryName(visibleLayout.getId())) || "llSearchStoryListLayout".equals(visibleLayout.getResources().getResourceEntryName(visibleLayout.getId()))) {
                llSearchStoryHeaderTabLayout.setVisibility(View.GONE);
            } else {
                llSearchStoryHeaderTabLayout.setVisibility(View.VISIBLE);
            }
        } else {
            llSearchStoryHeaderTabLayout.setVisibility(View.VISIBLE);

            if (mHeaderTab == 1) {
                llHashTagListLayout.setVisibility(View.VISIBLE);
            } else if (mHeaderTab == 2) {
                llPopularSearchWordListLayout.setVisibility(View.VISIBLE);
            } else {
                llRecentSearchWordListLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateSearchWord(String searchWord) {
        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        mdbHelper.updatePostSearchWord(searchWord);
        mdbHelper.close();
    }

    public void updateRecentSearchData(){
        mRecentAdapter.deleteAllItems();
        mRecentAdapter.setDeleteBtnVisibility(true);

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        ArrayList<String> data = mdbHelper.getAllPostRecentSearchWords();
        mdbHelper.close();
        mRecentAdapter.addAllItems(data);
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
                // 해시태그 onClick
                case R.id.tvHashTagWordBtn:
                    mHeaderTab = 1;
                    updateLayoutUI(llHashTagListLayout);
                    break;
                // 인기 검색어 onClick
                case R.id.tvPopSearchWordBtn:
                    mHeaderTab = 2;
                    updateLayoutUI(llPopularSearchWordListLayout);
                    break;
                // 최근 검색어 onclick
                case R.id.tvLastSearchWordBtn:
                    mHeaderTab = 3;
                    updateLayoutUI(llRecentSearchWordListLayout);
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
                case R.id.btnPopupCloseLayout:
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);
                    mChoiceColorLayout.setVisibility(View.GONE);
                    mChoiceColorLayout.setAnimation(animation);
                    break;
                // 모두삭제
                case R.id.tvDeleteRecentSearchWordAll:
                    if(mRecentAdapter == null || mRecentAdapter.getCount() == 0) return;
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    //최근 검색어 데이터 호출?!?!
                    //DB에 저장된 최근 검색어 모두삭제
                    mdbHelper.deleteAllPostRecentSearchWords();
                    mdbHelper.close();
                    mRecentAdapter.deleteAllItems();
                    mRecentAdapter.notifyDataSetChanged();
                    break;
                // 이야기 검색 리스트 > Header > 최신순 onClick
                case R.id.llStoryFilterSort:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_STORY_SORT, this, mOrderType);
                    mPostDialog.show();
                    break;
                // 이야기 검색 리스트 > Header > 최신순 > Item onClick
                case R.id.llStorySortLatest:
                case R.id.llStorySortPopular:
                case R.id.llStorySortOst:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mOrderType = (String)v.getTag(R.id.tag_sort_field);
                    if ("REG".equals(mOrderType))
                        tvStoryFilterSort.setText(getString(R.string.sort_latest));
                    else if ("LIKE".equals(mOrderType))
                        tvStoryFilterSort.setText(getString(R.string.sort_pop));
                    else if ("OST".equals(mOrderType))
                        tvStoryFilterSort.setText(getString(R.string.sort_ost));
                    getData(Constants.QUERY_STORY_SEARCH);
                    break;
                // 이야기 검색 리스트 > Header > 전체 onClick
                case R.id.llStoryFilterType:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_STORY_TYPE, this, mPostType);
                    mPostDialog.show();
                    break;
                // 이야기 검색 리스트 > Header > 전체 > Item onClick
                case R.id.llStoryTypeAllBtn:
                case R.id.llStoryTypePostBtn:
                case R.id.llStoryTypeTodayBtn:
                case R.id.llStoryTypeRadioBtn:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostType = (String)v.getTag(R.id.tag_post_type);

                    if ("".equals(mPostType)) {
                        lstvStoryFilterType.setSpacing(0.0f);
                        lstvStoryFilterType.setTextColor(Color.parseColor("#FFFFFFFF"));
                        lstvStoryFilterType.setText(getString(R.string.all));
                    } else if (Constants.REQUEST_TYPE_POST.equals(mPostType)) {
                        lstvStoryFilterType.setSpacing(Constants.TEXT_VIEW_SPACING);
                        lstvStoryFilterType.setTextColor(Color.parseColor("#FF00AFD5"));
                        lstvStoryFilterType.setText(getString(R.string.post));
                    } else if (Constants.REQUEST_TYPE_TODAY.equals(mPostType)) {
                        lstvStoryFilterType.setSpacing(Constants.TEXT_VIEW_SPACING);
                        lstvStoryFilterType.setTextColor(Color.parseColor("#FFFFD83B"));
                        lstvStoryFilterType.setText(getString(R.string.today));
                    } else if (Constants.REQUEST_TYPE_RADIO.equals(mPostType)) {
                        lstvStoryFilterType.setSpacing(Constants.TEXT_VIEW_SPACING);
                        lstvStoryFilterType.setTextColor(Color.parseColor("#FFF65857"));
                        lstvStoryFilterType.setText(getString(R.string.radio));
                    }
                    getData(Constants.QUERY_STORY_SEARCH);
                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        if (queryType == Constants.QUERY_STORY_RELATED) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                searchStoryRelatedThread((String)args[0]);
            }
        } else if (queryType == Constants.QUERY_STORY_SEARCH) {
            if (!isFinishing()) {
                if(mProgressDialog != null) {
                    mProgressDialog.showDialog(mContext);
                }
            }

            searchStoryThread();
        }
    }

    public Thread searchStoryRelatedThread(final String word){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetSearchOstRelatedReq getSearchOstRelatedReq = new GetSearchOstRelatedReq();
                    getSearchOstRelatedReq.setKEYWORD(word);

                    getSearchOstRelatedRes = request.getSearchOstRelated(getSearchOstRelatedReq, "STORY");
                    mHandler.sendEmptyMessage(Constants.QUERY_STORY_RELATED);
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

    public Thread searchStoryThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (CommonUtil.isNotNull(mKeyWord)) {
                    HPRequest request = new HPRequest(mContext);
                    try {
                        GetSearchStoryReq getSearchStoryReq = new GetSearchStoryReq();
                        getSearchStoryReq.setKEYWORD(mKeyWord);
                        getSearchStoryReq.setCURRENT_PAGE(mCurrentPage);

                        if (!"00000000000000FFFFFF".equals(mColorItem.getICI()))
                            getSearchStoryReq.setICI(mColorItem.getICI());

                        getSearchStoryReq.setPOST_TYPE(mPostType);
                        getSearchStoryReq.setORDER_TYPE(mOrderType);
                        mGetPostDataRes = request.getSearchStory(getSearchStoryReq);

                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.GA_CATEGORY_SEARCH)
                                .setAction(Constants.GA_ACTION_SEARCH_STORY)
                                .setLabel(getSearchStoryReq.getKEYWORD())
                                .build());

                        mHandler.sendEmptyMessage(Constants.QUERY_STORY_SEARCH);
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
            case Constants.QUERY_STORY_RELATED:
                if (getSearchOstRelatedRes.getOstRelatedItem().size() > 0) {
                    String word = etStoryName.getText().toString();
                    mRelatedAdapter.setWord(word);
                    mRelatedAdapter.addAllItems(getSearchOstRelatedRes.getOstRelatedItem());
                    mRelatedAdapter.notifyDataSetChanged();
                    updateLayoutUI(llRelatedSearchWordListLayout);
                } else {
                    updateLayoutUI(null);
                }
                break;
            // 이야기 검색 후 Handler
            case Constants.QUERY_STORY_SEARCH:
                mSearchStoryAdapter.addAllItems(mGetPostDataRes.getPostDataItemList());

                if (mGetPostDataRes.getPostDataItemList().size() > 0) {
                    tvStoryTotalCnt.setText(getString(R.string.total_n_count, mGetPostDataRes.getTOTAL_CNT()));
                    llEmptyLayout.setVisibility(View.GONE);
                    lvSearchStoryList.setVisibility(View.VISIBLE);
                } else {
                    tvStoryTotalCnt.setText(getString(R.string.total_n_count, 0));
                    tvEmptyMessage.setText(getString(R.string.msg_ost_search_empty));
                    llEmptyLayout.setVisibility(View.VISIBLE);
                    lvSearchStoryList.setVisibility(View.GONE);
                }
                updateLayoutUI(llSearchStoryListLayout);
                mSearchStoryAdapter.notifyDataSetChanged();
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
}
