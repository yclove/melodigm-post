package com.melodigm.post.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.menu.BuyUseCouponActivity;
import com.melodigm.post.menu.CabinetActivity;
import com.melodigm.post.protocol.data.GetSearchOstRes;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.util.ArrayList;

/**
 * YCNOTE - FragmentPagerAdapter와 FragmentStatePagerAdapter
 * Fragment를 처리하는 PagerAdapter는 두 가지 Class가 존재한다.
 * 하나는 FragmentPagerAdapter 이고 다른 하나는 FragmentStatePagerAdapter이다.
 * FragmentPagerAdapter의 경우, 사용자가 ViewPager에서 좌/우로 스크롤(플링)하여 화면 전환을 하여 다음 Fragment가 표시되면 이전 Fragment를 메모리 상에 저장해 만일 사용자가 화면을 반대로 이동하면 메모리 상에 저장되어있는 Fragment를 사용하게된다.
 * 2번째 FragmentStatePagerAdapter는 ViewPager의 페이지를 이동하여 다음 Fragment가 표시되면 이전 Fragment는 메모리 상에서 제거된다.
 * 사용자가 화면을 다시 반대로 전환하면 기존에 저장된 상태값(state)을 기반으로 재생성합니다.
 * 그러므로 페이지 수가 정해져 있고 그 수가 많지 않다면 FragmentPagerAdapter를 사용하는 편이 좋고 반대로 페이지 수를 알 수 없거나 많다면 FragmentStatePagerAdapter를 사용하는 것이 좋다.
 */

/**
 * YCNOTE - PagerAdapter notifyDataSetChanged
 * PagerAdapter 에 대한 notifyDataSetChanged()는 오직 ViewPager 에게 data set 이 변경되었다는 사실만을 알려준다.
 * ViewPager 는 view 의 등록과 삭제를 getItemPosition( Object ) 과 getCount() 를 통해 한다.
 * notifyDataSetChanged() 가 불리면 ViewPager 는 child view의 position 을 getItemPosition( Object ) 을 호출하여 알아본다.
 * 만약 이 child view 가 POSITION_NONE 을 던지면 ViewPager 는 view 가 삭제되었음을 안다.
 * 그리고 destroyItem( ViewGroup, int, Object )을 불러 이 view 를 제거한다.
 * ViewPager 가 View 를 업데이트하지 않는 현상이 나타나면 다음과 같이 억지로 update 시킬 수 있다.
 * <p/>
 * 1. PagerAdapter의 getItemPosition( Object object ) 를 override 하고 여기서 POSITION_NONE 값을 return 한다.
 * 저 값은 -2로, 저 값이 들어가면 ViewPager 는 notifyDataSetChanged() 가 불릴 때마다 모든 View 를 다시 그린다.
 * 따라서 효율성이 떨어지긴 하지만 어쨌든 해결은 된다. 권장할만한 방법은 아니다.
 * <p/>
 * 2. setTag() 를 통해 Fragment 에 tag 를 매겨놓고, PagerAdapter 의 instantiateItem( View, position ) 을 override 하여 tag 값 기준으로 필요한 view 만 다시 생성한다.
 * 이 방법을 이용하면 notifyDataSetChanged() 를 부르지 않고, ViewPager.findViewWithTag( Object ) 를 통해서 update 를 시도해야 한다.
 */
public class SearchOstPagerAdapter extends PagerAdapter implements View.OnClickListener, AbsListView.OnScrollListener {
    Context mContext;
    WeakRefHandler mHandler;
    LayoutInflater inflater;
    ArrayList<OstDataItem> arrSongDataItem = new ArrayList<>();
    ArrayList<OstDataItem> arrAlbumDataItem = new ArrayList<>();
    ArrayList<OstDataItem> arrArtiDataItem = new ArrayList<>();
    SearchSongAdapter mSearchSongAdapter;
    SearchAlbumAdapter mSearchAlbumAdapter;
    SearchArtiAdapter mSearchArtiAdapter;
    PostDatabases mdbHelper;
    boolean isOstSelectedAll = true;
    LinearLayout[] llMusicSelectFooter = new LinearLayout[3];
    PostDialog mPostDialog;
    EditText mEditText;
    InputMethodManager mInputMethodManager;

    @Override
    public int getItemPosition(Object object) {
        // 전체 view를 다시 로드
        return POSITION_NONE;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle data = new Bundle();
        Message msg = new Message();
        String POI = "", SSI = "";
        boolean isChecked = false;
        int checkedCount = 0;

        switch (v.getId()) {
            // 곡 Header > 전체선택 / 전체해제 onClick
            case R.id.llSongSelectAll:
                int checkedOstDataItemCount = 0;

                if (isOstSelectedAll) {
                    checkedOstDataItemCount = arrSongDataItem.size();

                    for (OstDataItem item : arrSongDataItem) {
                        item.setIsChecked(true);
                    }
                    ((TextView)v.findViewById(R.id.tvOstSelectAll)).setText(mContext.getString(R.string.common_unselect_all));
                    llMusicSelectFooter[0].setVisibility(View.VISIBLE);
                    ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvOstSelectCnt)).setText(String.valueOf(arrSongDataItem.size()));
                    ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setText(String.valueOf(Constants.RIGHT_COUNT));
                    mSearchSongAdapter.notifyDataSetChanged();
                } else {
                    for (OstDataItem item : arrSongDataItem) {
                        item.setIsChecked(false);
                    }
                    ((TextView)v.findViewById(R.id.tvOstSelectAll)).setText(mContext.getString(R.string.common_select_all));
                    llMusicSelectFooter[0].setVisibility(View.GONE);
                    ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvOstSelectCnt)).setText("0");
                    ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setText(String.valueOf(Constants.RIGHT_COUNT));
                    mSearchSongAdapter.notifyDataSetChanged();
                }

                // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                if (checkedOstDataItemCount > Constants.RIGHT_COUNT) {
                    ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setTextColor(ContextCompat.getColor(mContext, R.color.post));
                    llMusicSelectFooter[0].findViewById(R.id.rlBuyUseCouponBtn).setVisibility(View.VISIBLE);
                } else {
                    ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                    llMusicSelectFooter[0].findViewById(R.id.rlBuyUseCouponBtn).setVisibility(View.GONE);
                }

                isOstSelectedAll = !isOstSelectedAll;
                break;
            // 곡 Header > 전체듣기 onClick
            case R.id.llSongPlayAll:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAY_ALL, this);
                mPostDialog.show();
                break;
            // 곡 Header > 전체듣기 > 재생 목록에 추가하고 듣기 onClick
            case R.id.btnPlayAllAddList:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                for(OstDataItem item : arrSongDataItem) {
                    isChecked = true;
                    if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                        POI = item.getPOI();
                        SSI = item.getSSI();
                    }
                    checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                }
                mdbHelper.close();

                if (isChecked) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, mContext.getResources().getString(R.string.msg_ost_add_play_list, checkedCount));
                    mPostDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        }
                    }, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    intent.putExtra("POI", POI);
                    intent.putExtra("SSI", SSI);
                    mContext.startService(intent);
                } else {
                    DeviceUtil.showToast(mContext, mContext.getResources().getString(R.string.msg_required_ost_selected));
                }
                break;
            // 곡 Header > 전체듣기 > 재생 목록을 교체하고 듣기 onClick
            case R.id.btnPlayAllChangeList:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                mdbHelper.deleteAllOstPlayList();
                for(OstDataItem item : arrSongDataItem) {
                    isChecked = true;
                    if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                        POI = item.getPOI();
                        SSI = item.getSSI();
                    }
                    checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                }
                mdbHelper.close();

                if (isChecked) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, mContext.getResources().getString(R.string.msg_ost_add_play_list, checkedCount));
                    mPostDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        }
                    }, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    intent.putExtra("POI", POI);
                    intent.putExtra("SSI", SSI);
                    mContext.startService(intent);
                } else {
                    DeviceUtil.showToast(mContext, mContext.getResources().getString(R.string.msg_required_ost_selected));
                }
                break;
            // Footer > 듣기 onClick
            case R.id.llMusicListeningBtn:
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                for(OstDataItem item : arrSongDataItem) {
                    if (item.isChecked()) {
                        isChecked = true;
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                    }
                }
                mdbHelper.close();

                if (isChecked) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, mContext.getString(R.string.msg_ost_add_play_list, checkedCount));
                    mPostDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        }
                    }, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    intent.putExtra("POI", POI);
                    intent.putExtra("SSI", SSI);
                    mContext.startService(intent);
                } else {
                    DeviceUtil.showToast(mContext, mContext.getResources().getString(R.string.msg_required_music_selected));
                }
                break;
            // Footer > 추가 onClick
            case R.id.llMusicAddBtn:
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                for(OstDataItem item : arrSongDataItem) {
                    if (item.isChecked()) {
                        isChecked = true;
                        checkedCount++;
                        mdbHelper.updateOstPlayList(item);
                    }
                }
                mdbHelper.close();

                if (isChecked) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, mContext.getString(R.string.msg_ost_add_play_list, checkedCount));
                    mPostDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        }
                    }, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_PUT);
                    mContext.startService(intent);
                } else {
                    DeviceUtil.showToast(mContext, mContext.getResources().getString(R.string.msg_required_music_selected));
                }
                break;
            // Footer > 담기 onClick
            case R.id.llMusicPutBtn:
                if (arrSongDataItem.size() > 0) {
                    intent = new Intent(mContext, CabinetActivity.class);
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                    ((Activity)mContext).startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                }
                break;
            // 이용권구매 onClick
            case R.id.rlBuyUseCouponBtn:
                intent = new Intent(mContext, BuyUseCouponActivity.class);
                mContext.startActivity(intent);
                break;
        }
    }

    public SearchOstPagerAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler, RequestManager requestManager, EditText editText) {
        // 전달 받은 LayoutInflater를 멤버변수로 전달
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
        this.mEditText = editText;
        mInputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchSongAdapter = new SearchSongAdapter(mContext, R.layout.adapter_search_song, arrSongDataItem, requestManager);
        mSearchAlbumAdapter = new SearchAlbumAdapter(mContext, R.layout.adapter_search_album, arrAlbumDataItem, requestManager);
        mSearchArtiAdapter = new SearchArtiAdapter(mContext, R.layout.adapter_search_arti, arrArtiDataItem);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    // PagerAdapter가 가지고 잇는 View의 개수를 리턴
    // 보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴
    @Override
    public int getCount() {
        return 3;
    }

    // ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    // 쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 새로운 View 객체를 Layoutinflater를 이용해서 생성
        // 만들어질 View의 설계는 res폴더>>layout폴더>>viewpager_main.xml 레이아웃 파일 사용
        View view = inflater.inflate(R.layout.viewpager_search_song, null);

        // 만들어진 View안에 있는 ImageView 객체 참조
        // 위에서 inflated 되어 만들어진 view로부터 findViewById()를 해야 하는 것에 주의.
        final View viewOstFooter = ((Activity)mContext).getLayoutInflater().inflate(R.layout.view_ost_footer, null, false);
        final ListView lvDataList = (ListView)view.findViewById(R.id.lvDataList);
        lvDataList.setOnScrollListener(this);
        final LinearLayout searchNoitemLayout = (LinearLayout)view.findViewById(R.id.searchNoitemLayout);

        // 각 페이지의 Header
        final LinearLayout llSongHeader = (LinearLayout)view.findViewById(R.id.llSongHeader);
        final TextView tvOstSelectAll = (TextView)view.findViewById(R.id.tvOstSelectAll);
        final LinearLayout llSongSelectAll = (LinearLayout)view.findViewById(R.id.llSongSelectAll);
        final LinearLayout llSongPlayAll = (LinearLayout)view.findViewById(R.id.llSongPlayAll);

        final LinearLayout llAlbumHeader = (LinearLayout)view.findViewById(R.id.llAlbumHeader);
        final TextView tvAlbumType = (TextView)view.findViewById(R.id.tvAlbumType);
        final LinearLayout llAlbumType = (LinearLayout)view.findViewById(R.id.llAlbumType);

        final LinearLayout llMusicSort = (LinearLayout)view.findViewById(R.id.llMusicSort);
        
        llMusicSelectFooter[position] = (LinearLayout)view.findViewById(R.id.llMusicSelectFooter);
        llMusicSelectFooter[position].findViewById(R.id.rlBuyUseCouponBtn).setOnClickListener(this);

        final LinearLayout llMusicListeningBtn = (LinearLayout)view.findViewById(R.id.llMusicListeningBtn);
        final LinearLayout llMusicAddBtn = (LinearLayout)view.findViewById(R.id.llMusicAddBtn);
        final LinearLayout llMusicPutBtn = (LinearLayout)view.findViewById(R.id.llMusicPutBtn);

        // 초기화 작업
        if (position == 0) {
            llSongHeader.setVisibility(View.VISIBLE);
            llAlbumHeader.setVisibility(View.GONE);
            lvDataList.addFooterView(viewOstFooter, null, false);
        } else if (position == 1) {
            llSongHeader.setVisibility(View.GONE);
            llAlbumHeader.setVisibility(View.VISIBLE);
            lvDataList.removeFooterView(viewOstFooter);
        } else {
            llSongHeader.setVisibility(View.GONE);
            llAlbumHeader.setVisibility(View.GONE);
            lvDataList.removeFooterView(viewOstFooter);
        }

        int checkedOstDataItemCount = 0;
        for (OstDataItem item : arrSongDataItem) {
            if (item.isChecked()) checkedOstDataItemCount++;
        }

        if (position == 0 && checkedOstDataItemCount > 0) {
            llMusicSelectFooter[0].setVisibility(View.VISIBLE);
            ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvOstSelectCnt)).setText(String.valueOf(checkedOstDataItemCount));
            ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setText(String.valueOf(Constants.RIGHT_COUNT));
            tvOstSelectAll.setText(mContext.getString(R.string.common_unselect_all));
            isOstSelectedAll = false;
        } else {
            llMusicSelectFooter[position].setVisibility(View.GONE);
            tvOstSelectAll.setText(mContext.getString(R.string.common_select_all));
            isOstSelectedAll = true;
        }

        llMusicListeningBtn.setTag(R.id.tag_position, position);
        llMusicListeningBtn.setOnClickListener(this);

        llMusicAddBtn.setTag(R.id.tag_position, position);
        llMusicAddBtn.setOnClickListener(this);

        llMusicPutBtn.setTag(R.id.tag_position, position);
        llMusicPutBtn.setOnClickListener(this);

        switch (position) {
            case 0:
                lvDataList.setAdapter(mSearchSongAdapter);
                mSearchSongAdapter.setOnListItemClickListener(new SearchMusicInterface() {
                    @Override
                    public void onItemClick(int page, int position) {
                        // 곡 리스트 아이템 Listener
                        if (page == 0) {
                            int checkedOstDataItemCount = 0;

                            arrSongDataItem.get(position).setIsChecked(!arrSongDataItem.get(position).isChecked());
                            for (OstDataItem item : arrSongDataItem) {
                                if (item.isChecked()) checkedOstDataItemCount++;
                            }
                            ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvOstSelectCnt)).setText(String.valueOf(checkedOstDataItemCount));
                            ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setText(String.valueOf(Constants.RIGHT_COUNT));
                            mSearchSongAdapter.notifyDataSetChanged();
                            mSearchAlbumAdapter.notifyDataSetChanged();
                            mSearchArtiAdapter.notifyDataSetChanged();

                            if (checkedOstDataItemCount > 0) {
                                llMusicSelectFooter[0].setVisibility(View.VISIBLE);
                                tvOstSelectAll.setText(mContext.getString(R.string.common_unselect_all));
                                isOstSelectedAll = false;
                            } else {
                                llMusicSelectFooter[0].setVisibility(View.GONE);
                                tvOstSelectAll.setText(mContext.getString(R.string.common_select_all));
                                isOstSelectedAll = true;
                            }

                            // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                            if (checkedOstDataItemCount > Constants.RIGHT_COUNT) {
                                ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setTextColor(ContextCompat.getColor(mContext, R.color.post));
                                llMusicSelectFooter[0].findViewById(R.id.rlBuyUseCouponBtn).setVisibility(View.VISIBLE);
                            } else {
                                ((TextView)llMusicSelectFooter[0].findViewById(R.id.tvRightCnt)).setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                                llMusicSelectFooter[0].findViewById(R.id.rlBuyUseCouponBtn).setVisibility(View.GONE);
                            }
                        }
                    }
                });
                break;
            case 1:
                lvDataList.setAdapter(mSearchAlbumAdapter);
                break;
            case 2:
                lvDataList.setAdapter(mSearchArtiAdapter);
                break;
        }

        if (lvDataList.getAdapter().getCount() > 0) {
            if (position == 0) {
                llSongSelectAll.setAlpha(1.0f);
                llSongSelectAll.setOnClickListener(this);
                llSongPlayAll.setAlpha(1.0f);
                llSongPlayAll.setOnClickListener(this);
            } else if (position == 1) {
                llAlbumType.setAlpha(1.0f);
                llAlbumType.setOnClickListener(this);
            }

            llMusicSort.setAlpha(1.0f);
            llMusicSort.setOnClickListener(null);

            lvDataList.setVisibility(View.VISIBLE);
            searchNoitemLayout.setVisibility(View.GONE);
        } else {
            if (position == 0) {
                llSongSelectAll.setAlpha(0.2f);
                llSongSelectAll.setOnClickListener(null);
                llSongPlayAll.setAlpha(0.2f);
                llSongPlayAll.setOnClickListener(null);
            } else if (position == 1) {
                llAlbumType.setAlpha(0.2f);
                llAlbumType.setOnClickListener(null);
            }

            llMusicSort.setAlpha(0.2f);
            llMusicSort.setOnClickListener(null);

            lvDataList.setVisibility(View.GONE);
            searchNoitemLayout.setVisibility(View.VISIBLE);
        }

        // ViewPager에 만들어 낸 View 추가
        container.addView(view);

        // Image가 세팅된 View를 리턴
        return view;
    }

    // 화면에 보이지 않은 View는파괴를 해서 메모리를 관리함.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
    // 세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // ViewPager에서 보이지 않는 View는 제거
        // 세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
        container.removeView((View) object);
    }

    // instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == obj;
    }

    public void addAllItems(GetSearchOstRes getSearchOstRes) {
        if (getSearchOstRes != null) {
            arrSongDataItem = getSearchOstRes.getSongDataItem();
            arrAlbumDataItem = getSearchOstRes.getAlbumDataItem();
            arrArtiDataItem = getSearchOstRes.getArtiDataItem();
            mSearchSongAdapter.addAllItems(arrSongDataItem);
            mSearchAlbumAdapter.addAllItems(arrAlbumDataItem);
            mSearchArtiAdapter.addAllItems(arrArtiDataItem);
            mSearchSongAdapter.notifyDataSetChanged();
            mSearchAlbumAdapter.notifyDataSetChanged();
            mSearchArtiAdapter.notifyDataSetChanged();
        }
        notifyDataSetChanged();
    }
}
