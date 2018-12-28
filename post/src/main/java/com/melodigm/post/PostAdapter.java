package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.data.GetMusicPathReq;
import com.melodigm.post.protocol.data.GetMusicPathRes;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.util.CastUtil;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.EllipsizingTextView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.parallaxscroll.ParallaxImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class PostAdapter extends PagerAdapter implements IOnHandlerMessage, View.OnClickListener, MediaPlayer.OnCompletionListener {
    Context mContext;
    WeakRefHandler mHandler;
    GetMusicPathRes mGetMusicPathRes;
    HashMap<Integer, RunnableThread> mThreads = null;
    String dataSourceInfo;
    WeakRefHandler mLocalHandler;
    RequestManager mGlideRequestManager;
    String mPOST_TYPE, mGetPOI;
    LayoutInflater inflater;
    List<PostDataItem> mItems = new ArrayList<>();
    String mColor, mPOI;
    PostDialog mPostDialog;
    int mPosition = 0;
    boolean isShowMap = false;

    @Override
    public int getItemPosition(Object object) {
        // 전체 view를 다시 로드
        return POSITION_NONE;
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();
        int position;

        switch (v.getId()) {
            case R.id.tvPostSubject:
                data.putString("ICI", (String) v.getTag(R.id.tag_color));
                data.putString("COLOR_HEX", (String) v.getTag(R.id.tag_color_hex));
                msg.setData(data);
                msg.what = Constants.QUERY_POST_DATA;
                mHandler.sendMessage(msg);
                break;
            case R.id.postColorLayout:
                data.putString("ICI", "");
                msg.setData(data);
                msg.what = Constants.QUERY_POST_DATA;
                mHandler.sendMessage(msg);
                break;
            case R.id.postLocationLayout:
                position = (int)v.getTag(R.id.tag_position);
                boolean isShowMap = (boolean) v.getTag(R.id.tag_show_map);
                String PLACE = (isShowMap) ? "" : mItems.get(position).getPLACE();
                setShowMap(!isShowMap);

                data.putString("PLACE", PLACE);
                msg.setData(data);
                msg.what = Constants.QUERY_POST_DATA;
                mHandler.sendMessage(msg);
                break;
            // POST 신고 / 삭제 onClick
            case R.id.llNotifyDeleteBtn:
                String NOTIFY_DELETE_FLAG = (String)v.getTag(R.id.tag_notify_delete);
                if ("NOTIFY".equals(NOTIFY_DELETE_FLAG)) {
                    data.putString("POI", (String) v.getTag(R.id.tag_poi));
                    msg.setData(data);
                    msg.what = Constants.QUERY_POST_NOTIFY;
                    mHandler.sendMessage(msg);
                } else {
                    mPOI = (String)v.getTag(R.id.tag_poi);
                    mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete));
                    mPostDialog.show();
                }
                break;
            // POST 삭제 확인 onClick
            case R.id.btnConfirmConfirm:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                data.putString("POI", mPOI);
                msg.setData(data);
                msg.what = Constants.QUERY_POST_DELETE;
                mHandler.sendMessage(msg);
                break;
            // POST 좋아요 onClick
            case R.id.llPostLikeBtn:
                data.putInt("POSITION", (int) v.getTag(R.id.tag_position));
                msg.setData(data);
                msg.what = Constants.QUERY_POST_LIKE;
                mHandler.sendMessage(msg);
                break;
            // OST 데이터 조회 onClick
            case R.id.postOstEmptyLayout:
            case R.id.llPostOstBtn:
                data.putString("POI", (String) v.getTag(R.id.tag_poi));
                msg.setData(data);
                msg.what = Constants.QUERY_OST_DATA;
                mHandler.sendMessage(msg);
                break;
            // OST 타이틀 onClick
            case R.id.postOstLayout:
                PostDataItem postDataItem = (PostDataItem)v.getTag(R.id.tag_post_data_item);

                // 현재 재생 노래인 경우
                if (PlayerConstants.SONGS_LIST.size() > 0 && PlayerConstants.SONGS_LIST.size() > PlayerConstants.SONG_NUMBER && postDataItem.getPOI().equals(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI()) && postDataItem.getSSI().equals(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI())) {
                    if (PlayerConstants.SONG_PAUSED)
                        Controls.playControl(mContext);
                    else
                        Controls.pauseControl(mContext);
                } else {
                    data.putParcelable("PostDataItem", postDataItem);
                    msg.setData(data);
                    msg.what = Constants.QUERY_OST_ADD_PLAY_LIST;
                    mHandler.sendMessage(msg);
                }
                break;
            // POST RADIO 재생 onClick
            case R.id.btnRadioPlay:
                position = (int)v.getTag(R.id.tag_position);
                String POI = (String)v.getTag(R.id.tag_poi);
                mBtnStartPlayOnClick(position, POI);
                break;
        }
    }

    public PostAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler, RequestManager requestManager) {
        // 전달 받은 LayoutInflater를 멤버변수로 전달
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
        this.mLocalHandler = new WeakRefHandler(this);
        this.mThreads = new HashMap<>();
        this.mGlideRequestManager = requestManager;
    }

    // PagerAdapter가 가지고 잇는 View의 개수를 리턴
    // 보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴
    @Override
    public int getCount() {
        return mItems.size();
    }

    // ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    // 쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        PostDataItem item;

        view = inflater.inflate(R.layout.viewpager_main, null);

        item = mItems.get(position);

        LinearLayout contentLayout = (LinearLayout)view.findViewById(R.id.contentLayout);
        ParallaxImageView ivRoot = (ParallaxImageView)view.findViewById(R.id.ivRoot);
        LinearLayout postTitleEmptyLayout = (LinearLayout)view.findViewById(R.id.postTitleEmptyLayout);
        LinearLayout postContentEmptyLayout = (LinearLayout)view.findViewById(R.id.postContentEmptyLayout);
        LetterSpacingTextView tvPostEmptySubject = (LetterSpacingTextView)view.findViewById(R.id.tvPostEmptySubject);
        tvPostEmptySubject.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvPostEmptySubject.setText(mContext.getString(R.string.post_radio));
        LinearLayout postTitleLayout = (LinearLayout)view.findViewById(R.id.postTitleLayout);
        LinearLayout postContentLayout = (LinearLayout)view.findViewById(R.id.postContentLayout);
        LetterSpacingTextView tvPostSubject = (LetterSpacingTextView)view.findViewById(R.id.tvPostSubject);
        LinearLayout llPostColorLayout = (LinearLayout)view.findViewById(R.id.llPostColorLayout);
        LinearLayout llPostSubjectUnderLine = (LinearLayout)view.findViewById(R.id.llPostSubjectUnderLine);
        RelativeLayout postColorLayout = (RelativeLayout)view.findViewById(R.id.postColorLayout);
        ImageView ivPostColorCirCle = (ImageView)view.findViewById(R.id.ivPostColorCirCle);
        EllipsizingTextView tvPostContent = (EllipsizingTextView)view.findViewById(R.id.tvPostContent);
        LinearLayout postLocationLayout = (LinearLayout)view.findViewById(R.id.postLocationLayout);
        ImageView ivMapPin = (ImageView)view.findViewById(R.id.ivMapPin);
        LetterSpacingTextView tvPostLocationName = (LetterSpacingTextView)view.findViewById(R.id.tvPostLocationName);
        TextView tvPostRegDate = (TextView)view.findViewById(R.id.tvPostRegDate);
        LinearLayout llNotifyDeleteBtn = (LinearLayout)view.findViewById(R.id.llNotifyDeleteBtn);
        ImageView btnPostNotify = (ImageView)view.findViewById(R.id.btnPostNotify);
        ImageView btnPostDelete = (ImageView)view.findViewById(R.id.btnPostDelete);
        LinearLayout llPostLikeBtn = (LinearLayout)view.findViewById(R.id.llPostLikeBtn);
        ImageView btnPostLike = (ImageView)view.findViewById(R.id.btnPostLike);
        TextView tvPostLikeCount = (TextView)view.findViewById(R.id.tvPostLikeCount);
        LinearLayout llPostOstBtn = (LinearLayout)view.findViewById(R.id.llPostOstBtn);
        ImageView btnPostOst = (ImageView)view.findViewById(R.id.btnPostOst);
        TextView tvPostOstCount = (TextView)view.findViewById(R.id.tvPostOstCount);
        LinearLayout postOstLayout = (LinearLayout)view.findViewById(R.id.postOstLayout);
        LinearLayout postOstEmptyLayout = (LinearLayout)view.findViewById(R.id.postOstEmptyLayout);
        TextView tvOstEmptyInfo = (TextView)view.findViewById(R.id.tvOstEmptyInfo);
        CircularImageView btnOstImage = (CircularImageView)view.findViewById(R.id.btnOstImage);
        ImageView ivPostOstTitle = (ImageView)view.findViewById(R.id.ivPostOstTitle);
        TextView tvOstSongName = (TextView)view.findViewById(R.id.tvOstSongName);
        TextView tvOstArtiName = (TextView)view.findViewById(R.id.tvOstArtiName);
        RelativeLayout btnRadioPlay = (RelativeLayout)view.findViewById(R.id.btnRadioPlay);

        LinearLayout radioPlayerLayout = (LinearLayout)view.findViewById(R.id.radioPlayerLayout);
        LetterSpacingTextView tvRadioPlay = (LetterSpacingTextView)view.findViewById(R.id.tvRadioPlay);
        ImageView ivRadioPlay = (ImageView)view.findViewById(R.id.ivRadioPlay);
        SeekBar playProgressBar = (SeekBar)view.findViewById(R.id.playProgressBar);
        LetterSpacingTextView tvRadioPlayDuration = (LetterSpacingTextView)view.findViewById(R.id.tvRadioPlayDuration);

        TextView tvTodaySubject = (TextView)view.findViewById(R.id.tvTodaySubject);
        LinearLayout llMapNotifyLayout = (LinearLayout)view.findViewById(R.id.llMapNotifyLayout);
        View vPostRadioSpace = view.findViewById(R.id.vPostRadioSpace);
        View vTodaySpace = view.findViewById(R.id.vTodaySpace);
        LinearLayout llTodayHeaderSpace = (LinearLayout)view.findViewById(R.id.llTodayHeaderSpace);

        item.setTvRadioPlay(tvRadioPlay);
        item.setIvRadioPlay(ivRadioPlay);
        item.setSeekbar(playProgressBar);
        item.setTvRadioPlayDuration(tvRadioPlayDuration);
        item.setLlPostLikeBtn(llPostLikeBtn);
        item.setIvRoot(ivRoot); // for registerSensorManager

        // POST Indicator
        int padding_top = CastUtil.DpToPx(mContext, 73);
        int padding_top_today = CastUtil.DpToPx(mContext, 165);
        int padding_bottom = CastUtil.DpToPx(mContext, 92);

        if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
            contentLayout.setPadding(0, padding_top_today, 0, padding_bottom);
        } else {
            contentLayout.setPadding(0, padding_top, 0, padding_bottom);
        }

        // POST 신고 / 삭제 버튼
        String UAI = SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID);
        String NOTIFY_DELETE_FLAG;
        if (UAI.equals(item.getUAI())) {
            NOTIFY_DELETE_FLAG = "DELETE";
            btnPostNotify.setVisibility(View.GONE);
            btnPostDelete.setVisibility(View.VISIBLE);
        } else {
            NOTIFY_DELETE_FLAG = "NOTIFY";
            btnPostNotify.setVisibility(View.VISIBLE);
            btnPostDelete.setVisibility(View.GONE);
        }

        if ("Y".equals(item.getDCRE_TOGGLE_YN()))
            btnPostNotify.setImageResource(R.drawable.arwdown_white50);
        else
            btnPostNotify.setImageResource(R.drawable.arwdown_white50);

        llNotifyDeleteBtn.setTag(R.id.tag_notify_delete, NOTIFY_DELETE_FLAG);
        llNotifyDeleteBtn.setTag(R.id.tag_poi, item.getPOI());
        llNotifyDeleteBtn.setOnClickListener(this);

        // POST 좋아요
        if ("Y".equals(item.getLIKE_TOGGLE_YN()))
            btnPostLike.setImageResource(R.drawable.icon_like_rel);
        else
            btnPostLike.setImageResource(R.drawable.icon_like_nor);

        if (!UAI.equals(item.getUAI())) {
            llPostLikeBtn.setTag(R.id.tag_position, position);
            llPostLikeBtn.setOnClickListener(this);
        }

        // OST 목록
        if ("Y".equals(item.getOST_REG_YN())) {
            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST))
                btnPostOst.setImageResource(R.drawable.icon_ost_rel);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO))
                btnPostOst.setImageResource(R.drawable.icon_rdo_rel);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY))
                btnPostOst.setImageResource(R.drawable.icon_tod_rel);
        } else
            btnPostOst.setImageResource(R.drawable.icon_ost_nor);

        llPostOstBtn.setTag(R.id.tag_poi, item.getPOI());
        llPostOstBtn.setOnClickListener(this);

        // OST 타이틀
        postOstLayout.setTag(R.id.tag_post_data_item, item);
        postOstLayout.setOnClickListener(this);
        postOstLayout.setOnTouchListener(touchListener);

        // POST 배경 / 지도
        if (CommonUtil.isNotNull(item.getBG_MAP_PATH()) && CommonUtil.isNull(mGetPOI)) {
            postLocationLayout.setTag(R.id.tag_position, position);
            postLocationLayout.setTag(R.id.tag_show_map, isShowMap);
            postLocationLayout.setOnClickListener(this);
        }

        if (isShowMap) {
            postLocationLayout.setVisibility(View.GONE);
            ivMapPin.setImageResource(R.drawable.icon_pin_rel);
            tvPostLocationName.setTextColor(Color.parseColor("#FFFFCC4F"));

            ivRoot.setImageResource(R.color.transparent);
        } else {
            ivMapPin.setImageResource(R.drawable.icon_pin_nor);
            tvPostLocationName.setTextColor(Color.parseColor("#99FFFFFF"));

            if (!"".equals(item.getBG_USER_PATH())) {
                mGlideRequestManager
                    .load(item.getBG_USER_PATH())
                    .error(R.drawable.img_int_temp)
                    .thumbnail(Constants.GLIDE_THUMBNAIL)
                    .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                    .into(ivRoot);
            } else {
                mGlideRequestManager
                    .load(item.getBG_PIC_PATH())
                    .error(R.drawable.img_int_temp)
                    .thumbnail(Constants.GLIDE_THUMBNAIL)
                    .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                    .into(ivRoot);
            }
       }

        // POST 제목
        if (CommonUtil.isNull(mColor)) {
            tvPostSubject.setTag(R.id.tag_color, item.getCOLOR());
            tvPostSubject.setTag(R.id.tag_color_hex, item.getCOLOR_HEX());

            if ("FFFFFF".equals(item.getCOLOR_HEX().toUpperCase()) || CommonUtil.isNotNull(mGetPOI)) {
                tvPostSubject.setOnClickListener(null);
            } else {
                tvPostSubject.setOnClickListener(this);
            }

            postColorLayout.setVisibility(View.GONE);
        } else {
            postColorLayout.setOnClickListener(this);
            postColorLayout.setVisibility(View.GONE);
        }

        if (CommonUtil.isNull(item.getPOST_SUBJ())) {
            tvPostSubject.setTextColor(Color.parseColor("#FFFFFFFF"));
            tvPostSubject.setAlpha(0.5f);
            tvPostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            tvPostSubject.setSpacing(Constants.TEXT_VIEW_SPACING);

            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
                tvPostSubject.setText(mContext.getString(R.string.post_story));
            } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
                tvPostSubject.setText(mContext.getString(R.string.post_radio));
            }
        } else {
            tvPostSubject.setSpacing(0.0f);
            tvPostSubject.setText(item.getPOST_SUBJ());
            if (!"".equals(item.getCOLOR_HEX())) {
                tvPostSubject.setTextColor(Color.parseColor("#" + item.getCOLOR_HEX()));
                ivPostColorCirCle.setBackground(new ColorCircleDrawable(Color.parseColor("#" + item.getCOLOR_HEX())));
            }
        }

        if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
            postTitleLayout.setVisibility(View.GONE);
            tvTodaySubject.setVisibility(View.VISIBLE);
            llMapNotifyLayout.setVisibility(View.GONE);
            vPostRadioSpace.setVisibility(View.GONE);
            vTodaySpace.setVisibility(View.VISIBLE);
            llTodayHeaderSpace.setVisibility(View.VISIBLE);

            tvTodaySubject.setText(item.getPOST_SUBJ());
            if (!"".equals(item.getCOLOR_HEX())) tvTodaySubject.setTextColor(Color.parseColor("#" + item.getCOLOR_HEX()));
        } else {
            postTitleLayout.setVisibility(View.VISIBLE);
            tvTodaySubject.setVisibility(View.GONE);
            llMapNotifyLayout.setVisibility(View.VISIBLE);
            vPostRadioSpace.setVisibility(View.VISIBLE);
            vTodaySpace.setVisibility(View.GONE);
            llTodayHeaderSpace.setVisibility(View.GONE);
        }

        // POST 내용
        String postContentText = item.getPOST_CONT();
        if (Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)) {
            if (CommonUtil.isNull(item.getPOST_SUBJ())) {
                if (CommonUtil.isNull(postContentText)) {
                    postTitleLayout.setVisibility(View.GONE);
                    postTitleEmptyLayout.setVisibility(View.VISIBLE);

                    postContentLayout.setVisibility(View.GONE);
                    postContentEmptyLayout.setVisibility(View.VISIBLE);
                } else {
                    postTitleLayout.setVisibility(View.VISIBLE);
                    postTitleEmptyLayout.setVisibility(View.GONE);

                    postContentLayout.setVisibility(View.VISIBLE);
                    postContentEmptyLayout.setVisibility(View.GONE);
                }
            } else {
                postContentLayout.setVisibility(View.VISIBLE);
                postContentEmptyLayout.setVisibility(View.GONE);
            }
        }

        if (!Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
            tvPostContent.setHandler(mHandler);
        }
        tvPostContent.setScrollEnabled(false);
        tvPostContent.setText(postContentText);

        // 제목 / 내용 정렬
        if (Constants.REQUEST_POST_PST_TYPE_LEFT.equals(item.getPOST_PST_TYPE())) {
            tvPostSubject.setGravity(Gravity.START|Gravity.CENTER);
            llPostColorLayout.setGravity(Gravity.END);
            llPostSubjectUnderLine.setGravity(Gravity.START);
            tvPostContent.setGravity(Gravity.TOP|Gravity.START);
        } else if (Constants.REQUEST_POST_PST_TYPE_CENTER.equals(item.getPOST_PST_TYPE())) {
            tvPostSubject.setGravity(Gravity.CENTER|Gravity.CENTER);
            llPostColorLayout.setGravity(Gravity.END);
            llPostSubjectUnderLine.setGravity(Gravity.CENTER);
            tvPostContent.setGravity(Gravity.TOP|Gravity.CENTER);
        } else if (Constants.REQUEST_POST_PST_TYPE_RIGHT.equals(item.getPOST_PST_TYPE())) {
            tvPostSubject.setGravity(Gravity.END|Gravity.CENTER);
            llPostColorLayout.setGravity(Gravity.START);
            llPostSubjectUnderLine.setGravity(Gravity.END);
            tvPostContent.setGravity(Gravity.TOP|Gravity.END);
        }
        llPostSubjectUnderLine.setVisibility(View.VISIBLE);

        // POST RADIO
        fullFilePath = item.getRADIO_PATH();
        int height = 0;
        radioPlayerLayout.setVisibility(View.GONE);
        if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
            tvPostContent.setMaxLines(8);
            height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 124, mContext.getResources().getDisplayMetrics());
        } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
            tvPostContent.setMaxLines(4);
            tvPostContent.setMoreLinkEnabled(false);
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 78, mContext.getResources().getDisplayMetrics());
        } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
            tvPostContent.setMaxLines(6);
            height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, mContext.getResources().getDisplayMetrics());

            if (CommonUtil.isNotNull(fullFilePath)) {
                radioPlayerLayout.setVisibility(View.VISIBLE);
                mPlayerState = PLAY_STOP;
                updateUI(position);
                initMediaPlayer();
            }
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        postContentLayout.setLayoutParams(lp);

        btnRadioPlay.setTag(R.id.tag_position, position);
        btnRadioPlay.setTag(R.id.tag_poi, item.getPOI());
        btnRadioPlay.setOnClickListener(this);

        tvRadioPlayDuration.setText(DateUtil.getConvertMsToFormat(item.getRADIO_RUNTIME() / 1000));

        // POST 위치
        tvPostLocationName.setText(item.getPLACE());
        if (CommonUtil.isRegexOnlyEng(item.getPLACE().replaceAll(" ", "")))
            tvPostLocationName.setSpacing(Constants.TEXT_VIEW_SPACING);

        // POST 등록일시
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(item.getREG_DATE());
            tvPostRegDate.setText(DateUtil.getDateDisplay(date));
        } catch (ParseException e) {
            tvPostRegDate.setText(item.getREG_DATE());
        }

        // OST 타이틀 영역
        if (CommonUtil.isNull(item.getOTI())) {
            postOstLayout.setVisibility(View.GONE);
            postOstEmptyLayout.setVisibility(View.VISIBLE);
            postOstEmptyLayout.setTag(R.id.tag_poi, item.getPOI());
            postOstEmptyLayout.setOnClickListener(this);

            if (UAI.equals(item.getUAI()))
                tvOstEmptyInfo.setText(mContext.getString(R.string.msg_ost_empty_info_self));
            else
                tvOstEmptyInfo.setText(mContext.getString(R.string.msg_ost_empty_info));
        } else {
            postOstLayout.setVisibility(View.VISIBLE);
            postOstEmptyLayout.setVisibility(View.GONE);
            postOstEmptyLayout.setOnClickListener(null);

            if (!"".equals(item.getTITLE_ALBUM_PATH())) {
                mGlideRequestManager
                    .load(item.getTITLE_ALBUM_PATH())
                    .error(R.drawable.icon_album_dummy)
                    .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                    .into(btnOstImage);
            }
            tvOstSongName.setText(item.getTITLE_SONG_NM());
            tvOstArtiName.setText(item.getTITLE_ARTI_NM());

            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST))
                ivPostOstTitle.setImageResource(R.drawable.icon_title_post);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO))
                ivPostOstTitle.setImageResource(R.drawable.icon_title_radio);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY))
                ivPostOstTitle.setImageResource(R.drawable.icon_title_today);
        }

        // POST 좋아요 / OST 카운트
        tvPostLikeCount.setText(String.valueOf(item.getLIKE_CNT()));
        tvPostOstCount.setText(String.valueOf(item.getOST_CNT()));

        container.addView(view);

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

    public void addAllItems(ArrayList<PostDataItem> data) {
        if (data != null) {
            mItems = data;
        } else {
            mItems.clear();
        }
        notifyDataSetChanged();
    }

    public void deleteAllItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    protected OnItemTouchListener mOnItemTouchListener = null;
    public interface OnItemTouchListener{
        boolean onTouch(View v, MotionEvent event);
    }

    public void setOnItemTouchListener(OnItemTouchListener listener){
        mOnItemTouchListener = listener;
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event){
            mOnItemTouchListener.onTouch(v, event);
            return false;
        }
    };

    public String getPostType() {
        return mPOST_TYPE;
    }

    public void setPostType(String postType) {
        this.mPOST_TYPE = postType;
    }

    public void setGetPOI(String POI) {
        this.mGetPOI = POI;
    }

    public void setShowMap(boolean showMap) {
        this.isShowMap = showMap;
    }

    public void setAdapterColor(String color) {
        this.mColor = color;
    }

    public void resetPlayer(int position) {
        LogUtil.e("PLAYER 초기화");
        mPlayerState = PLAY_STOP;
        updateUI(position);
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public void setParallaxPosition(int position, boolean parallaxSet) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getIvRoot() != null) {
                mItems.get(i).getIvRoot().unregisterSensorManager();
            }
        }

        if (parallaxSet) {
            LogUtil.e("배경이미지 센서 등록 : 아이템 사이즈 = " + mItems.size() + ", 센서 등록 포지션 = " + position);
            if (mItems.size() > position) {
                if (mItems.get(position).getIvRoot() != null) {
                    mItems.get(position).getIvRoot().registerSensorManager();
                } else {
                    LogUtil.e("배경이미지 센서 등록 뷰가 존재하지 않습니다.");
                }
            }
        }
    }

    private String fullFilePath;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;

    private MediaPlayer mPlayer = null;
    private int mPlayerState = PLAY_STOP;

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);

        updateUI(mPosition);
    }

    // 재생시 SeekBar 처리
    Handler mProgressHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            if (mPlayer == null) return;

            try {
                if (mPlayer.isPlaying()) {
                    mItems.get(msg.what).getSeekbar().setProgress(mPlayer.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(msg.what, 1000);
                }
            } catch (IllegalStateException e) {
            } catch (Exception e) {}
        }
    };

    private void mBtnStartPlayOnClick(int position, String POI) {
        if (mPlayerState == PLAY_STOP) {
            startPlay(position, POI);
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_PAUSE;
            pausePlay();
            updateUI(position);
        } else if (mPlayerState == PLAY_PAUSE) {
            startPlay(position, POI);
        }
    }

    private void initMediaPlayer() {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);
    }

    boolean isRequesting = false;
    // 음성 재생 시작
    private void startPlay(int position, String POI) {
        LogUtil.e("음성 재생 시작");

        if (mPlayerState == PLAY_PAUSE) {
            mPlayerState = PLAYING;
            mPlayer.start();
            updateUI(position);
        } else if (mPlayerState == PLAY_STOP) {
            if (!isRequesting) {
                isRequesting = true;
                getData(Constants.QUERY_MUSIC_PATH, position, POI);
            }
        }
    }

    private void pausePlay() {
        LogUtil.e("pausePlay().....");

        // 재생을 일시 정지하고
        mPlayer.pause();

        // 재생이 일시정지되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void updateUI(int position) {
        if (mPlayerState == PLAY_STOP) {
            if (mItems.get(position).getTvRadioPlay() != null)
                mItems.get(position).getTvRadioPlay().setText(mContext.getString(R.string.play));
            if (mItems.get(position).getIvRadioPlay() != null)
                mItems.get(position).getIvRadioPlay().setImageResource(R.drawable.bt_rad_play);
            if (mItems.get(position).getSeekbar() != null)
                mItems.get(position).getSeekbar().setProgress(0);
        } else if (mPlayerState == PLAYING) {
            if (mItems.get(position).getTvRadioPlay() != null)
                mItems.get(position).getTvRadioPlay().setText(mContext.getString(R.string.stop));
            if (mItems.get(position).getIvRadioPlay() != null)
                mItems.get(position).getIvRadioPlay().setImageResource(R.drawable.bt_rad_pause);
        } else if (mPlayerState == PLAY_PAUSE) {
            if (mItems.get(position).getTvRadioPlay() != null)
                mItems.get(position).getTvRadioPlay().setText(mContext.getString(R.string.play));
            if (mItems.get(position).getIvRadioPlay() != null)
                mItems.get(position).getIvRadioPlay().setImageResource(R.drawable.bt_rad_play);
        }
    }

    private void getData(int queryType, Object... args) {
        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_MUSIC_PATH) {
            if (args != null && args.length > 1 && args[0] instanceof Integer && args[1] instanceof String) {
                thread = getMusicPathThread((Integer) args[0], (String)args[1]);
            }
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getMusicPathThread(final int position, final String POI) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicPathReq getMusicPathReq = new GetMusicPathReq();
                    getMusicPathReq.setPOI(POI);
                    mGetMusicPathRes = request.getMusicPath(getMusicPathReq);

                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putInt("POSITION", position);
                    msg.setData(data);
                    msg.what = Constants.QUERY_MUSIC_PATH;
                    mLocalHandler.sendMessage(msg);
                } catch(Exception e) {

                } finally {
                    isRequesting = false;
                }
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            // 미디어 파일 다운로드 조회 성공 후 Handler
            case Constants.QUERY_MUSIC_PATH:
                try {
                    int position = msg.getData().getInt("POSITION");

                    mPlayerState = PLAYING;
                    updateUI(position);

                    dataSourceInfo = mGetMusicPathRes.getURL();
                    LogUtil.e("♬ MusicService ▶ 라디오 경로 : " + dataSourceInfo);
                    mPlayer.reset();
                    mPlayer.setDataSource(dataSourceInfo);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepare();

                    int point = mPlayer.getDuration();
                    mItems.get(position).getSeekbar().setMax(point);

                    int maxMinPoint = point / 1000 / 60;
                    int maxSecPoint = (point / 1000) % 60;
                    String maxMinPointStr = "";
                    String maxSecPointStr = "";

                    if (maxMinPoint < 10)
                        maxMinPointStr = "0" + maxMinPoint + "´";
                    else
                        maxMinPointStr = maxMinPoint + "´";

                    if (maxSecPoint < 10)
                        maxSecPointStr = "0" + maxSecPoint + "˝";
                    else
                        maxSecPointStr = maxSecPoint + "˝";

                    mItems.get(position).getTvRadioPlayDuration().setText(maxMinPointStr + maxSecPointStr);

                    mItems.get(position).getSeekbar().setProgress(0);

                    Controls.pauseControl(mContext);
                    mPlayer.start();

                    // SeekBar의 상태를 1초마다 체크
                    mProgressHandler2.sendEmptyMessageDelayed(position, 1000);
                } catch(Exception e) {
                    LogUtil.e("미디어 플레이어 Prepare Error ==========> " + e);
                }
                break;
            case Constants.QUERY_MUSIC_PAYMENT:
                break;
        }
    }
}
