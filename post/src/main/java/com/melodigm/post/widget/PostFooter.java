package com.melodigm.post.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.melodigm.post.R;


public class PostFooter extends LinearLayout {
	private Context mContext;

	public PostFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PostFooter(Context context) {
		super(context, null);
		initView(context);
	}
    
    public void initView(Context context){
        mContext = context;
		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = layoutInflater.inflate(R.layout.view_post_footer, this, false);
		addView(root);
	}
}
