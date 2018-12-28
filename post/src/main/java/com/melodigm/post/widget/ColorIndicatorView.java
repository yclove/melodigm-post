package com.melodigm.post.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.util.DeviceUtil;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ColorIndicatorView extends HorizontalScrollView  {
    private Context mContext;
    private LinearLayout mTabLayout;
    private Runnable mTabSelector;
    private ColorIndicator mColorIndicator;
    private ArrayList<ColorItem> mItems;
    private int spaceWidth;

    public ColorIndicatorView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public ColorIndicatorView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        this.mContext = context;
        initView();
    }

    private final OnClickListener mTabClickListener = new OnClickListener() {
        public void onClick(View paramAnonymousView) {
            ColorItemView colorItemView = (ColorItemView)paramAnonymousView;
            // 선택 효과
            changeTabSeqIndicator(colorItemView.getICI());
            if(mColorIndicator != null){
                mColorIndicator.changeTabIndicator(colorItemView.getItemData());
            }
        }
    };

    public void addAllItems(ArrayList<ColorItem> data){
        if(mItems != null) {
            mItems.clear();
            mTabLayout.removeAllViews();
        }

        if(data != null) {
            // YCLOVE - 스크롤 좌우 여백처리로 인한 추가
            mTabLayout.addView(new LinearLayout(mContext), new LinearLayout.LayoutParams(spaceWidth, WRAP_CONTENT));

            mItems = data;
            for(int i = 0; i < mItems.size(); i++)
                // YCLOVE - 스크롤 좌우 여백처리로 인한 변경
                // addTab(i, mItems.get(i));
                addTab(i + 1, mItems.get(i));

            // YCLOVE - 스크롤 좌우 여백처리로 인한 추가
            mTabLayout.addView(new LinearLayout(mContext), new LinearLayout.LayoutParams(spaceWidth, WRAP_CONTENT));
        }
    }

    public ArrayList<ColorItem> getAllItems(){
        return mItems;
    }

    public void addTab(int paramInt, ColorItem item) {
        ColorItemView localTabView = new ColorItemView(getContext());
        localTabView.setIndex(paramInt);
        localTabView.setFocusable(true);
        localTabView.setOnClickListener(mTabClickListener);
        localTabView.setItemData(item);

        // YCLOVE - 스크롤 좌우 여백처리로 인한 변경
        // if (paramInt == 0)
        if (paramInt == 1)
            localTabView.setSelected();
        else
            localTabView.setUnSelected();
        mTabLayout.addView(localTabView, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1.0F));
    }

    private void animateToTab(int paramInt) {
        final View tabView = mTabLayout.getChildAt(paramInt);
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
        mTabSelector = new Runnable() {
            public void run() {
                final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mTabSelector = null;
            }
        };
        post(mTabSelector);
    }

    private void initView() {
        spaceWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (DeviceUtil.getScreenWidthInDPs(mContext) / 2) - 30, getResources().getDisplayMetrics());
        mTabLayout = new LinearLayout(mContext);
        addView(mTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
    }

    public void changeTabSeqIndicator(String ICI) {
        int size = mTabLayout.getChildCount();

        // YCLOVE - 스크롤 좌우 여백처리로 인한 변경
        // for (int i = 0; i < size; i++) {
        for (int i = 1; i < size - 1; i++) {
            final ColorItemView colorItemView = (ColorItemView)this.mTabLayout.getChildAt(i);
            boolean bool;
            if(colorItemView.getICI().equalsIgnoreCase(ICI)) {
                bool = true;
                if (!bool)
                    break;

                animateToTab(colorItemView.getIndex());
                colorItemView.setSelected();
                new Handler().post(new Runnable() {
                    public void run() {
                        requestLayout();
                    }
                });
            } else {
                bool = false;
                colorItemView.setUnSelected();
            }
        }
    }

    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (this.mTabSelector != null)
            post(this.mTabSelector);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mTabSelector != null)
            removeCallbacks(this.mTabSelector);
    }

    public void setOnChangeTabListener(ColorIndicator listener){
        mColorIndicator = listener;
    }
}
