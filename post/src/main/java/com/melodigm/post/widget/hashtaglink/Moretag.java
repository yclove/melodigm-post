package com.melodigm.post.widget.hashtaglink;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.melodigm.post.common.Constants;
import com.melodigm.post.util.handler.WeakRefHandler;

public class Moretag extends ClickableSpan{
    Context context;
    WeakRefHandler mHandler;
    TextPaint textPaint;

    public Moretag(Context ctx, WeakRefHandler handler) {
        super();
        this.context = ctx;
        this.mHandler = handler;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setColor(Color.parseColor("#CC959595"));
        //ds.setARGB(255, 255, 0, 0);
        //ds.setTypeface(Typekit.createFromAsset(context, "fonts/Roboto-Bold.ttf"));
    }

    @Override
    public void onClick(View widget) {
        if (mHandler == null) return;
        mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA_MORE);
    }
}
