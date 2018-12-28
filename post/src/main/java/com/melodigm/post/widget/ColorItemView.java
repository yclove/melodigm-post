package com.melodigm.post.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.melodigm.post.R;
import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.util.LogUtil;

public class ColorItemView extends RelativeLayout {
    private Context mContext;
    private int mIndex;
    private ColorItem mItem;
    private ImageView ivPostColorChoiceCirCle, ivPostColorCirCle;

    public ColorItemView(Context context) {
        super(context);
        initView(context);
    }

    public ColorItemView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        initView(context);
    }

    public ColorItemView(Context context, AttributeSet paramAttributeSet, int paramInt) {
        super(context, paramAttributeSet, paramInt);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.view_color_item, this, false);
        ivPostColorChoiceCirCle = (ImageView)root.findViewById(R.id.ivPostColorChoiceCirCle);
        ivPostColorCirCle = (ImageView)root.findViewById(R.id.ivPostColorCirCle);

        ivPostColorChoiceCirCle.setImageDrawable(new ColorCircleDrawable(Color.parseColor("#FFFFFFFF")));
        ivPostColorChoiceCirCle.setVisibility(View.GONE);
        addView(root);
    }

    public int getIndex() {
        return this.mIndex;
    }

    public void setIndex(int paramInt) {
        this.mIndex = paramInt;
    }

    @Override
    protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    }

    public void setSelected() {
        if (!"FFFFFF".equals(mItem.getCOLOR_CODE().toUpperCase())) {
            final int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics());
            final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics());

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.addRule(RelativeLayout.CENTER_VERTICAL);

            ivPostColorCirCle.setLayoutParams(lp);
            ivPostColorChoiceCirCle.setVisibility(View.VISIBLE);
        }
    }

    public void setUnSelected() {
        if (!"FFFFFF".equals(mItem.getCOLOR_CODE().toUpperCase())) {
            final int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
            final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.addRule(RelativeLayout.CENTER_VERTICAL);

            ivPostColorCirCle.setLayoutParams(lp);
            ivPostColorChoiceCirCle.setVisibility(View.GONE);
        }
    }

    public void setItemData(ColorItem item){
        mItem = item;

        if ("FFFFFF".equals(mItem.getCOLOR_CODE().toUpperCase())) {
            // TODO : YCLOVE
//            ivPostColorCirCle.setImageResource(R.drawable.icon_color_default);
        } else {
            ivPostColorCirCle.setImageDrawable(new ColorCircleDrawable(Integer.parseInt(mItem.getCOLOR_CODE(), 16) + 0xFF000000));
        }
        ivPostColorCirCle.setTag(mItem);
    }

    public ColorItem getItemData(){
        return mItem;
    }

    public String getICI(){
        return mItem.getICI();
    }
}
