package com.melodigm.post.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetSearchLocationReq;
import com.melodigm.post.protocol.data.GetSearchLocationRes;
import com.melodigm.post.protocol.data.LocationItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.util.ArrayList;

public class SearchLocationActivity extends BaseActivity implements IOnHandlerMessage/*, AbsListView.OnScrollListener, View.OnTouchListener*/ {
	private GetSearchLocationRes getSearchLocationRes;
    private ArrayList<LocationItem> arrLocationItem;
    private SearchLocationAdapter mSearchLocationAdapter;
	private EditText etLocationName;
	private ListView lvLocationList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_loaction);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(getString(R.string.title_regist_location), false);

        etLocationName = (EditText) findViewById(R.id.etLocationName);
        lvLocationList = (ListView) findViewById(R.id.lvLocationList);

        arrLocationItem = new ArrayList<>();
        mSearchLocationAdapter = new SearchLocationAdapter(mContext, R.layout.adapter_search_location, arrLocationItem);
        lvLocationList.setAdapter(mSearchLocationAdapter);

        etLocationName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH :
                        getData();
                        break;
				}
				return true;
			}
		});

        lvLocationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
				mSearchLocationAdapter.setLocationCheck(position);
            }
        });

        /*lvLocationList.setOnTouchListener(this);
        lvLocationListNd.setOnScrollListener(this);*/

        getData();
	}

    /*@Override
    public boolean onTouch(View arg0, MotionEvent event) {
        lvLocationListNd.dispatchTouchEvent(event);

        return false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View v=view.getChildAt(0);
        if(v != null)
            lvLocationList.setSelectionFromTop(firstVisibleItem, v.getTop());
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE : // 스크롤이 정지되어 있는 상태야.
                //정지되어 있는 상태일 때 해야 할 일들을 써줘.
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL : // 스크롤이 터치되어 있을 때 상태고,
                //스크롤이 터치되어 있는 상태일 때 해야 할 일들을 써줘.
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING : // 이건 스크롤이 움직이고 있을때 상태야.
                //여기도 마찬가지.
                break;
        }
    }*/

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.btnHeaderCheck :
                    Intent intent = getIntent();

                    for(LocationItem item : arrLocationItem) {
                        if (item.isChecked()) {
                            intent.putExtra("LOCA_LAT", item.getLat());
                            intent.putExtra("LOCA_LNG", item.getLng());
                            intent.putExtra("PLACE", item.getName());
                            break;
                        }
                    }

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    break;
            }
        }
    };

	private void getData() {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        new Thread(searchLocationThread).start();
	}

    private Runnable searchLocationThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(mContext);
            try {
                GetSearchLocationReq getSearchLocationReq = new GetSearchLocationReq();
                getSearchLocationReq.setLOCA_LAT(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LAT));
                getSearchLocationReq.setLOCA_LNG(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LNG));
                getSearchLocationReq.setPLACE(etLocationName.getText().toString());
                getSearchLocationRes = request.getSearchLocation(getSearchLocationReq);
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NON);
            } catch(RequestException e) {
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
            } catch(POSTException e) {
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
    };
	
    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            case Constants.DIALOG_EXCEPTION_NON :
                arrLocationItem.clear();

                LocationItem somewhere = new LocationItem("", "", getString(R.string.common_somewhere), "");
                arrLocationItem.add(somewhere);

                LocationItem km = new LocationItem(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LAT), SPUtil.getSharedPreference(mContext, Constants.SP_USER_LNG), "", "");
                arrLocationItem.add(km);

                for(LocationItem item : getSearchLocationRes.getLocationItemList()) {
                    arrLocationItem.add(item);
                }

                if (getSearchLocationRes.getLocationItemList().size() > 0)
                    mSearchLocationAdapter.setLocationCheck(0);

                mSearchLocationAdapter.notifyDataSetChanged();
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
