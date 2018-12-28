package com.melodigm.post;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetOstRepleDeleteReq;
import com.melodigm.post.protocol.data.GetOstRepleReq;
import com.melodigm.post.protocol.data.GetOstRepleRes;
import com.melodigm.post.protocol.data.GetOstRepleWriteReq;
import com.melodigm.post.protocol.data.OstRepleItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.PostHeader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OstRepleActivity extends BaseActivity implements IOnHandlerMessage {
    private String mPOST_TYPE, mOTI, mORI;
    private GetOstRepleRes getOstRepleRes;
    private ArrayList<OstRepleItem> arrOstRepleItem;
    private OstRepleAdapter mOstRepleAdapter;
    private ListView lvOstReple;
    private LinearLayout btnOstRepleWrite, ostRepleWriteLayout, llOstRepleWriteFooter;
    private ScrollView ostRepleEmpty;
    private EditText etOstRepleCont;
    private RelativeLayout rootLayout, btnOstRepleWriteSubmit;
    private ImageView btnOstRepleWriteSubmitImage;

    // OST Layout View
    private PostHeader postHeader;
    private TextView tvOstRegDate, tvSongName, tvArtiName, tvOstCont, tvOstLikeCount, tvOstRepleCount;
    private CircularImageView btnOstImage;
    private ImageView ivOstTitle, btnOstLike, btnOstRepleImage;
    private LinearLayout ostContLayout;
    private RelativeLayout btnOstCngTitle, btnOstReple;

    private boolean isKeyBoardVisible;
    private int keyboardHeight;

    private InputMethodManager mInputMethodManager;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ost_reple);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        Intent intent = getIntent();
        mPOST_TYPE = intent.getStringExtra("POST_TYPE");
        mOTI = intent.getStringExtra("OTI");
        mORI = intent.getStringExtra("ORI");

        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        setDisplay();
	}

	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle("", false);

        // OST Layout View
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        llOstRepleWriteFooter = (LinearLayout)findViewById(R.id.llOstRepleWriteFooter);
        postHeader = (PostHeader)findViewById(R.id.postHeader);
        tvOstRegDate = (TextView)findViewById(R.id.tvOstRegDate);
        btnOstImage = (CircularImageView)findViewById(R.id.btnOstImage);
        tvSongName = (TextView)findViewById(R.id.tvSongName);
        tvArtiName = (TextView)findViewById(R.id.tvArtiName);
        ivOstTitle = (ImageView)findViewById(R.id.ivOstTitle);
        ostContLayout = (LinearLayout)findViewById(R.id.ostContLayout);
        tvOstCont = (TextView)findViewById(R.id.tvOstCont);
        btnOstLike = (ImageView)findViewById(R.id.btnOstLike);
        tvOstLikeCount = (TextView)findViewById(R.id.tvOstLikeCount);
        btnOstCngTitle = (RelativeLayout)findViewById(R.id.btnOstCngTitle);
        btnOstReple = (RelativeLayout)findViewById(R.id.btnOstReple);
        btnOstRepleImage = (ImageView)findViewById(R.id.btnOstRepleImage);
        tvOstRepleCount = (TextView)findViewById(R.id.tvOstRepleCount);

        ostRepleWriteLayout = (LinearLayout)findViewById(R.id.ostRepleWriteLayout);
        btnOstRepleWriteSubmit = (RelativeLayout)findViewById(R.id.btnOstRepleWriteSubmit);
        btnOstRepleWriteSubmit.setOnClickListener(onClickListener);
        btnOstRepleWriteSubmitImage = (ImageView)findViewById(R.id.btnOstRepleWriteSubmitImage);
        etOstRepleCont = (EditText)findViewById(R.id.etOstRepleCont);

        if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST))
            postHeader.setBackgroundColor(Color.parseColor("#E600AFD5"));
        else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO))
            postHeader.setBackgroundColor(Color.parseColor("#E6F65857"));
        else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY))
            postHeader.setBackgroundColor(Color.parseColor("#E6FFD83B"));

        /**
         * YCNOTE - TextWatcher
         * beforeTextChanged(CharSequence s, int start, int count, int after)
         *      CharSequence s : 현재 에디트텍스트에 입력된 문자열을 담고 있다.
         *      int start : s 에 저장된 문자열 내에 새로 추가될 문자열의 위치값을 담고있다.
         *      int count : s 에 담긴 문자열 가운데 새로 사용자가 입력할 문자열에 의해 변경될 문자열의 수가 담겨있다.
         *      int after : 새로 추가될 문자열의 수
         * onTextChanged(CharSequence s, int start, int before, int count)
         *      CharSequence s : 사용자가 새로 입력한 문자열을 포함한 에디트텍스트의 문자열이 들어있음
         *      int start : 새로 추가된 문자열의 시작 위치의 값
         *      int before : 새 문자열 대신 삭제된 기존 문자열의 수가 들어 있다
         *      int count : 새로 추가된 문자열의 수가 들어있다.
         * e.g)
         * abcde 라는 문자열이 있다.
         * 이때 cde를 선택한다음 키보드에서 t를 찾아 누르면 에디트텍스트 내 문자열은 다음과같이 abt 로 변한다
         * beforeTextChanged()
         * CharSequence s : abcde (에디트 텍스트에 들어있던 문자열)
         * int start : (abcde에서 변경될 텍스트가 c에서 시작하므로 start 값은 2 이다. 참고로 첫 문자열 a 는 0, b는 1)
         * int count : 3 (cde를 선택했으므로 cde의 길이는 3)
         * int after : 1(t가 입력되었으므로 t의 길이 1)
         * onTextChanged()
         * CharSequence s : abt (병경된 후 에디트 텍스트 내의 문자열 abt)
         * int start : 2(t는 a와 b다음이므로  위치 값은 2)
         * int before : 3(처음 abced에서 cde가 대신에 t 가 들어왔으므로 ced의 길이인3)
         * int count : 1 (새로 추가되었으므로 t의 길이 1)
         */
        etOstRepleCont.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                if (s.toString().length() > 0) {
                    if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
                        btnOstRepleWriteSubmitImage.setImageResource(R.drawable.icon_ost_rerel);
                    } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
                        btnOstRepleWriteSubmitImage.setImageResource(R.drawable.icon_rdo_rerel);
                    } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
                        btnOstRepleWriteSubmitImage.setImageResource(R.drawable.icon_tdo_rerel);
                    }
                } else {
                    btnOstRepleWriteSubmitImage.setImageResource(R.drawable.icon_ost_renor);
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        btnOstRepleWrite = (LinearLayout)findViewById(R.id.btnOstRepleWrite);
        btnOstRepleWrite.setOnClickListener(onClickListener);

        ostRepleEmpty = (ScrollView)findViewById(R.id.ostRepleEmpty);
        lvOstReple = (ListView)findViewById(R.id.lvOstReple);
        arrOstRepleItem = new ArrayList<>();
        mOstRepleAdapter = new OstRepleAdapter(mContext, R.layout.adapter_ost_reple, arrOstRepleItem, mHandler);
        lvOstReple.setAdapter(mOstRepleAdapter);

        checkKeyboardHeight(rootLayout);
        getData(Constants.QUERY_OST_REPLE);
	}

    private void updateOstUI() {
        if (getOstRepleRes == null) return;

        // OST 등록일시
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(getOstRepleRes.getOstDataItem().getREG_DATE());
            tvOstRegDate.setText(DateUtil.getDateDisplay(date));
        } catch (ParseException e) {
            tvOstRegDate.setText(getOstRepleRes.getOstDataItem().getREG_DATE());
        }

        // OST 이미지
        if (!"".equals(getOstRepleRes.getOstDataItem().getALBUM_PATH())) {
            mGlideRequestManager
                .load(getOstRepleRes.getOstDataItem().getALBUM_PATH())
                .error(R.drawable.icon_album_dummy)
                .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                .into(btnOstImage);
        }

        // OST 노래재목 / 가수 / 내용
        tvSongName.setText(getOstRepleRes.getOstDataItem().getSONG_NM());
        tvArtiName.setText(getOstRepleRes.getOstDataItem().getARTI_NM());

        if (CommonUtil.isNull(getOstRepleRes.getOstDataItem().getCONT())) {
            ostContLayout.setVisibility(View.GONE);
            tvOstCont.setText("");
        } else {
            ostContLayout.setVisibility(View.VISIBLE);
            tvOstCont.setText(getOstRepleRes.getOstDataItem().getCONT());
        }

        // OST 타이틀 선정 버튼
        if ("Y".equals(getOstRepleRes.getOstDataItem().getTITL_TOGGLE_YN())) {
            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
                ivOstTitle.setImageResource(R.drawable.icon_title_post);
                tvSongName.setTextColor(Color.parseColor("#FF00AFD5"));
            } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
                ivOstTitle.setImageResource(R.drawable.icon_title_radio);
                tvSongName.setTextColor(Color.parseColor("#FFF65857"));
            } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
                ivOstTitle.setImageResource(R.drawable.icon_title_today);
                tvSongName.setTextColor(Color.parseColor("#FFFFD83B"));
            }
        } else {
            ivOstTitle.setImageResource(R.drawable.bt_title_chk);
            tvSongName.setTextColor(Color.parseColor("#FF000000"));
        }

        String UAI = SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID);
        if (UAI.equals(getOstRepleRes.getOstDataItem().getPOST_UAI())) {
            btnOstCngTitle.setVisibility(View.VISIBLE);
        } else {
            if ("Y".equals(getOstRepleRes.getOstDataItem().getTITL_TOGGLE_YN()))
                btnOstCngTitle.setVisibility(View.VISIBLE);
            else
                btnOstCngTitle.setVisibility(View.GONE);
        }

        // OST 좋아요
        if ("Y".equals(getOstRepleRes.getOstDataItem().getLIKE_TOGGLE_YN()))
            btnOstLike.setImageResource(R.drawable.icon_like_rel);
        else
            btnOstLike.setImageResource(R.drawable.icon_ost_likenor);

        String LIKE_TOGGLE_YN = ( "Y".equals(getOstRepleRes.getOstDataItem().getLIKE_TOGGLE_YN()) ) ? "N" : "Y";
        btnOstLike.setTag(R.id.tag_oti, getOstRepleRes.getOstDataItem().getOTI());
        btnOstLike.setTag(R.id.tag_like_toggle_yn, LIKE_TOGGLE_YN);
        tvOstLikeCount.setText(getOstRepleRes.getOstDataItem().getLIKE_CNT());

        // OST 대댓글
        if ("Y".equals(getOstRepleRes.getOstDataItem().getOST_REPLY_YN()))
            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST))
                btnOstRepleImage.setImageResource(R.drawable.icon_ost_rerel);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO))
                btnOstRepleImage.setImageResource(R.drawable.icon_rdo_rerel);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY))
                btnOstRepleImage.setImageResource(R.drawable.icon_tdo_rerel);
        else
            btnOstRepleImage.setImageResource(R.drawable.icon_ost_renor);

        btnOstReple.setTag(R.id.tag_oti, getOstRepleRes.getOstDataItem().getOTI());
        tvOstRepleCount.setText(getOstRepleRes.getOstDataItem().getOST_REPLY_CNT());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    mInputMethodManager.hideSoftInputFromWindow(etOstRepleCont.getWindowToken(), 0);
                    finish();
                    break;
                // OST Reple Footer > 대댓글쓰기 onClick
                case R.id.btnOstRepleWrite:
                    ostRepleWriteLayout.setVisibility(View.VISIBLE);
                    btnOstRepleWrite.setVisibility(View.GONE);
                    etOstRepleCont.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    break;
                // OST Reple Footer > 대댓글쓰기 onClick
                case R.id.btnOstRepleWriteSubmit:
                    if (etOstRepleCont.getText().toString().length() > 0) {
                        getData(Constants.QUERY_OST_REPLE_WRITE);
                    }
                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        // OST Reple 목록 요청 Trigger
        if (queryType == Constants.QUERY_OST_REPLE) {
            thread = getOstRepleThread();
        }
        // OST Reple 등록 요청 Trigger
        else if (queryType == Constants.QUERY_OST_REPLE_WRITE) {
            thread = getOstRepleWriteThread();
        }
        // OST Reple 삭제 요청 Trigger
        else if (queryType == Constants.QUERY_OST_REPLE_DELETE) {
            if (args != null && args.length == 1 && args[0] instanceof String)
                thread = getOstRepleDeleteThread((String) args[0]);
        }

            if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getOstRepleThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetOstRepleReq getOstRepleReq = new GetOstRepleReq();
                    getOstRepleReq.setOTI(mOTI);
                    getOstRepleRes = request.getOstReple(getOstRepleReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_OST_REPLE);
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

    public RunnableThread getOstRepleWriteThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetOstRepleWriteReq getOstRepleWriteReq = new GetOstRepleWriteReq();
                    getOstRepleWriteReq.setOTI(mOTI);
                    getOstRepleWriteReq.setCONT(etOstRepleCont.getText().toString());
                    request.getOstRepleWrite(getOstRepleWriteReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_OST_REPLE_WRITE);
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

    public RunnableThread getOstRepleDeleteThread(final String ORI) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetOstRepleDeleteReq getOstRepleDeleteReq = new GetOstRepleDeleteReq();
                    getOstRepleDeleteReq.setORI(ORI);
                    getOstRepleDeleteReq.setOTI(mOTI);
                    request.getOstRepleDelete(getOstRepleDeleteReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_OST_REPLE_DELETE_RESULT);
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

        Bundle data = msg.getData();

        switch(msg.what) {
            // OST Reple 목록 요청 성공 Handler
            case Constants.QUERY_OST_REPLE:
                updateOstUI();

                if (getOstRepleRes.getOstRepleItem().size() > 0) {
                    ostRepleEmpty.setVisibility(View.GONE);
                    lvOstReple.setVisibility(View.VISIBLE);

                    arrOstRepleItem.clear();
                    for (OstRepleItem item : getOstRepleRes.getOstRepleItem()) {
                        arrOstRepleItem.add(item);
                    }
                    mOstRepleAdapter.notifyDataSetChanged();

                    // 알림바에서 대댓글 이동 알림을 클릭하여 들어온 경우
                    if (CommonUtil.isNotNull(mORI)) {
                        int selectPosition = 0;
                        for (int i = 0; i < getOstRepleRes.getOstRepleItem().size(); i++) {
                            if (mORI.equals(getOstRepleRes.getOstRepleItem().get(i).getORI())) {
                                selectPosition = i;
                                break;
                            }
                        }
                        lvOstReple.setSelection(selectPosition);
                        mORI = "";
                    }
                } else {
                    ostRepleEmpty.setVisibility(View.VISIBLE);
                    lvOstReple.setVisibility(View.GONE);
                }
                break;
            // OST Reple 등록 요청 성공 Handler
            case Constants.QUERY_OST_REPLE_WRITE:
                etOstRepleCont.setText("");
                getData(Constants.QUERY_OST_REPLE);
                break;
            // OST Reple 삭제 요청 Handler
            case Constants.QUERY_OST_REPLE_DELETE:
                getData(Constants.QUERY_OST_REPLE_DELETE, data.getString("ORI", ""));
                break;
            // OST Reple 삭제 성공 Handler
            case Constants.QUERY_OST_REPLE_DELETE_RESULT:
                getData(Constants.QUERY_OST_REPLE);
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

    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;
    private void checkKeyboardHeight(final View parentLayout) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                parentLayout.getWindowVisibleDisplayFrame(r);

                int screenHeight = parentLayout.getRootView().getHeight();
                int heightDifference = screenHeight - (r.bottom);

                previousHeightDiffrence = heightDifference;
                if (heightDifference > 100) {
                    isKeyBoardVisible = true;
                    changeKeyboardHeight(heightDifference);
                } else {
                    isKeyBoardVisible = false;
                }

                if (isKeyBoardVisible) {
                    llOstRepleWriteFooter.setVisibility(LinearLayout.VISIBLE);
                } else {
                    llOstRepleWriteFooter.setVisibility(LinearLayout.GONE);
                }
            }
        });
    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height
     *            minimum height by which we can make sure actual keyboard is
     *            open or not
     */
    private void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            llOstRepleWriteFooter.setLayoutParams(params);
        }
    }
}
