package com.melodigm.post.search;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.analytics.HitBuilders;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.BaseApplication;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetMusicPathReq;
import com.melodigm.post.protocol.data.GetMusicPathRes;
import com.melodigm.post.protocol.data.GetSearchOstRelatedReq;
import com.melodigm.post.protocol.data.GetSearchOstRelatedRes;
import com.melodigm.post.protocol.data.GetSearchOstReq;
import com.melodigm.post.protocol.data.GetSearchOstRes;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.OstRelatedItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SearchOstActivity extends BaseActivity implements IOnHandlerMessage, AbsListView.OnScrollListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private String mPOI = "";
    private GetSearchOstRelatedRes getSearchOstRelatedRes;
    private GetSearchOstRes getSearchOstRes;
    private GetMusicPathRes mGetMusicPathRes;
    private ArrayList<OstDataItem> arrOstDataItem;
    private OstDataItem mOstDataItem;
	private EditText etOstName;
    private TextView btnDeleteRecentAll, tvEmptyMessage;

    private LinearLayout searchNoitemLayout, searchRecentListLayout, searchRelatedListLayout, searchOstListLayout;
    private RecentAdapter mRecentAdapter = null;            // 최근검색어 리스트 어뎁터
    private RelatedAdapter mRelatedAdapter = null;          // 자동완성 검색어 리스트 어뎁터
    private SearchOstAdapter mSearchOstAdapter = null; // OST 리스트 어뎁터
    private ListView lvOstList, lvRecentList, lvRelatedList;

    private InputMethodManager mInputMethodManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ost);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        Intent intent = getIntent();
        mPOI = intent.getStringExtra("POI");

        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.search), false);

        searchNoitemLayout = (LinearLayout)findViewById(R.id.searchNoitemLayout);
        tvEmptyMessage = (TextView) findViewById(R.id.tvEmptyMessage);
        searchRecentListLayout = (LinearLayout)findViewById(R.id.searchRecentListLayout);
        searchRelatedListLayout = (LinearLayout)findViewById(R.id.searchRelatedListLayout);
        searchOstListLayout = (LinearLayout)findViewById(R.id.searchOstListLayout);

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
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

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

        // 최근검색
        mRecentAdapter = new RecentAdapter(mContext);
        mRecentAdapter.setOnListItemClickListener(new RecentAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(String word, int position) {
                updateSearchWord(word);
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
                    tvEmptyMessage.setText(getString(R.string.msg_empty_keyword));
                    searchNoitemLayout.setVisibility(View.VISIBLE);
                    searchRecentListLayout.setVisibility(View.GONE);
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
                getData(Constants.QUERY_OST_SEARCH, ostRelatedItem.getKEYWORD(), ostRelatedItem.getTYPE());
            }
        });
        lvRelatedList = (ListView)findViewById(R.id.lvRelatedList);
        lvRelatedList.setOnScrollListener(this);
        lvRelatedList.setAdapter(mRelatedAdapter);

        // OST검색
        arrOstDataItem = new ArrayList<>();
        mSearchOstAdapter = new SearchOstAdapter(mContext, R.layout.adapter_search_ost, arrOstDataItem, mGlideRequestManager);
        mSearchOstAdapter.setOnListViewClickListener(new SearchOstAdapter.OnListViewClickListener() {
            @Override
            public void onViewClick(int position, String SSI) {
                if (position >= 0 && CommonUtil.isNotNull(SSI)) {
                    playMusic(position, SSI);
                }
            }
        });
        lvOstList = (ListView)findViewById(R.id.lvOstList);
        lvOstList.setOnScrollListener(this);
        lvOstList.setAdapter(mSearchOstAdapter);
        lvOstList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                if (!"Y".equals(mSearchOstAdapter.getItem(position).getOST_YN())) {
                    for (int i = 0; i < mSearchOstAdapter.getCount(); i++) {
                        mSearchOstAdapter.getItem(i).setIsChecked(false);
                    }
                    mSearchOstAdapter.getItem(position).setIsChecked(true);
                    mSearchOstAdapter.notifyDataSetChanged();
                    mOstDataItem = mSearchOstAdapter.getItem(position);
                    setDisplayHeaderCheck();
                }
            }
        });

        btnDeleteRecentAll = (TextView)findViewById(R.id.btnDeleteRecentAll);
        btnDeleteRecentAll.setOnClickListener(onClickListener);

        updateRecentSearchData();
        setDisplayHeaderCheck();
        initMediaPlayer();
	}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mInputMethodManager.hideSoftInputFromWindow(etOstName.getWindowToken(), 0);
    }

    private void setDisplayHeaderCheck() {
        if (mOstDataItem == null) {
            btnHeaderCheck.setAlpha(0.3f);
            btnHeaderCheck.setOnClickListener(null);
        } else {
            btnHeaderCheck.setAlpha(1.0f);
            btnHeaderCheck.setOnClickListener(onClickListener);
        }
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
            tvEmptyMessage.setText(getString(R.string.msg_empty_keyword));
            searchNoitemLayout.setVisibility(View.VISIBLE);
            searchRecentListLayout.setVisibility(View.GONE);
            searchRelatedListLayout.setVisibility(View.GONE);
            searchOstListLayout.setVisibility(View.GONE);
        } else {
            searchNoitemLayout.setVisibility(View.GONE);
            searchRecentListLayout.setVisibility(View.VISIBLE);
            searchRelatedListLayout.setVisibility(View.GONE);
            searchOstListLayout.setVisibility(View.GONE);
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
                // Header 확인 onClick
                case R.id.btnHeaderCheck:
                    if (mOstDataItem != null) {
                        Intent intent = getIntent();
                        intent.putExtra("OstDataItem", mOstDataItem);
                        setResult(Constants.RESULT_SUCCESS, intent);
                        finish();
                    }
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

                    tvEmptyMessage.setText(getString(R.string.msg_empty_keyword));
                    searchNoitemLayout.setVisibility(View.VISIBLE);
                    searchRecentListLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_OST_RELATED) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                thread = searchOstRelatedThread((String)args[0]);
            }
        } else if (queryType == Constants.QUERY_OST_SEARCH) {
            if (!isFinishing()) {
                if(mProgressDialog != null) {
                    mProgressDialog.showDialog(mContext);
                }
            }

            thread = searchOstThread(args);
        } else if (queryType == Constants.QUERY_MUSIC_PATH) {
            if (args != null && args.length > 1 && args[0] instanceof Integer && args[1] instanceof String) {
                if (!isFinishing()) {
                    if(mProgressDialog != null) {
                        mProgressDialog.showDialog(mContext);
                    }
                }

                thread = getMusicPathThread((Integer) args[0], (String)args[1]);
            }
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
	}

    public RunnableThread searchOstRelatedThread(final String word){
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetSearchOstRelatedReq getSearchOstRelatedReq = new GetSearchOstRelatedReq();
                    getSearchOstRelatedReq.setKEYWORD(word);

                    getSearchOstRelatedRes = request.getSearchOstRelated(getSearchOstRelatedReq, "OST");
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

    public RunnableThread searchOstThread(final Object... args){
        RunnableThread thread = new RunnableThread(new StopRunnable() {
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
                    getSearchOstReq.setCHO_SRC_ID(mPOI);

                    getSearchOstRes = request.getSearchOst(getSearchOstReq, "OST");

                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Constants.GA_CATEGORY_SEARCH)
                            .setAction(Constants.GA_ACTION_SEARCH_OST)
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

    public RunnableThread getMusicPathThread(final int position, final String SSI) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicPathReq getMusicPathReq = new GetMusicPathReq();
                    getMusicPathReq.setSSI(SSI);
                    getMusicPathReq.setTYPE("PRE");
                    mGetMusicPathRes = request.getMusicPath(getMusicPathReq);

                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putInt("POSITION", position);
                    msg.setData(data);
                    msg.what = Constants.QUERY_MUSIC_PATH;
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
                    searchRecentListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.VISIBLE);
                    searchOstListLayout.setVisibility(View.GONE);
                } else {
                    mRelatedAdapter.deleteAllItems();
                    mRelatedAdapter.notifyDataSetChanged();
                    searchNoitemLayout.setVisibility(View.GONE);
                    searchRecentListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.VISIBLE);
                    searchOstListLayout.setVisibility(View.GONE);
                }
                break;
            case Constants.QUERY_OST_DATA_VIEW:
                mSearchOstAdapter.addAllItems(getSearchOstRes.getOstDataItem());

                if (getSearchOstRes.getOstDataItem().size() > 0) {
                    searchNoitemLayout.setVisibility(View.GONE);
                    searchRecentListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.GONE);
                    searchOstListLayout.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyMessage.setText(getString(R.string.msg_ost_search_empty));
                    searchNoitemLayout.setVisibility(View.VISIBLE);
                    searchRecentListLayout.setVisibility(View.GONE);
                    searchRelatedListLayout.setVisibility(View.GONE);
                    searchOstListLayout.setVisibility(View.GONE);
                }

                mSearchOstAdapter.notifyDataSetChanged();
                break;
            // 미디어 파일 다운로드 조회 성공 후 Handler
            case Constants.QUERY_MUSIC_PATH:
                try {
                    int position = msg.getData().getInt("POSITION");

                    LogUtil.e("♬ MusicService ▶ 미리듣기 경로 : " + mGetMusicPathRes.getURL());
                    mPlayer.reset();
                    mPlayer.setDataSource(mGetMusicPathRes.getURL());
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepareAsync();

                    mPlayerState = PLAYING;
                    mSearchOstAdapter.updatePreControlUI(position);

                    Controls.pauseControl(mContext);
                } catch(Exception e) {
                    LogUtil.e("♬ MusicService ▶ Error : " + e.getMessage());
                }
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

    private MediaPlayer mPlayer = null;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private int mPlayerState = PLAY_STOP;
    private String preMusicSSI = "";

    private void initMediaPlayer() {
        // 미디어 플레이어 생성
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnCompletionListener(this);
        } else {
            mPlayer.reset();
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.e("♬ MusicService ▶ onDestroy");
        super.onDestroy();
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onPrepared(final MediaPlayer player) {
        LogUtil.e("♬ MusicService ▶ onPrepared");

        // stop을 호출하게 되면 노래의 재생은  Stopped 상태가 되며 완전히 종료 되게 된다. Stopped 상태에서는 다시 start()를 실행 할수 없다. 이때는 다시 prepare 를 시킨후 start를 실행 하여야 될것이다.
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtil.e(String.format(Locale.US, "♬ MusicService ▶ onError : what(%d), extra(%d)", what, extra));
        mPlayerState = PLAY_STOP;
        stopPlay();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtil.e("♬ MusicService ▶ onCompletion");
        mSearchOstAdapter.updatePreControlUI(-1);
    }

    private void playMusic(int position, String SSI) {
        // 현재 미리듣기 중인 노래와 같은 노래일 경우
        if (preMusicSSI.equalsIgnoreCase(SSI) && mPlayerState == PLAYING) {
            mPlayerState = PLAY_STOP;
            stopPlay();
        } else {
            preMusicSSI = SSI;
            if (mPlayerState == PLAY_STOP) {
                startPlay(position, SSI);
            } else if (mPlayerState == PLAYING) {
                stopPlay();
                startPlay(position, SSI);
            }
        }
    }

    private void startPlay(int position, String SSI) {
        LogUtil.e("♬ MusicService ▶ 미리듣기를 시작합니다.");
        checkMobileNetwork(position, SSI);
    }

    private void stopPlay() {
        LogUtil.e("♬ MusicService ▶ 미리듣기를 중지합니다.");

        mPlayer.stop();
        mSearchOstAdapter.updatePreControlUI(-1);
    }

    private void checkMobileNetwork(int position, String SSI) {
        // 데이터 네트워크 사용을 거부하고 현재 모바일 내트워크 접속 중일 경우
        if (!SPUtil.getBooleanSharedPreference(mContext, Constants.SP_USE_DATA_NETWORK) && PlayerConstants.MOBILE_NETWORK) {
            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
            mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, onGlobalClickListener, getString(R.string.dialog_confirm_data_network), getString(R.string.dialog_confirm_data_network_title));
            mPostDialog.show();

            mPlayerState = PLAY_STOP;
            stopPlay();
        } else {
            getData(Constants.QUERY_MUSIC_PATH, position, SSI);
        }
    }
}
