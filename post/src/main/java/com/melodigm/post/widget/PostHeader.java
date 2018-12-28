package com.melodigm.post.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.melodigm.post.R;


public class PostHeader extends LinearLayout {
	private Context mContext;

	public PostHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PostHeader(Context context) {
		super(context, null);
		initView(context);
	}
    
    public void initView(Context context) {
        mContext = context;
		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = layoutInflater.inflate(R.layout.view_post_header, this, false);
		addView(root);
	}
}
