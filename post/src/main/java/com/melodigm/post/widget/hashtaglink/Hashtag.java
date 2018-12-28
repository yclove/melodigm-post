package com.melodigm.post.widget.hashtaglink;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.util.handler.WeakRefHandler;

public class Hashtag extends ClickableSpan{
    Context context;
    WeakRefHandler mHandler;
    TextPaint textPaint;

    public Hashtag(Context ctx, WeakRefHandler handler) {
        super();
        this.context = ctx;
        this.mHandler = handler;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setColor(Color.parseColor("#CCD73D66"));
        //ds.setARGB(255, 255, 0, 0);
        // TODO : YCLOVE
//        ds.setTypeface(Typekit.createFromAsset(context, "fonts/Roboto-Bold.ttf"));
    }

    @Override
    public void onClick(View widget) {
        if (mHandler == null) return;

        TextView tv = (TextView) widget;
        Spanned s = (Spanned) tv.getText();
        int start = s.getSpanStart(this);
        int end = s.getSpanEnd(this);
        String theWord = s.subSequence(start + 1, end).toString();

        Bundle data = new Bundle();
        data.putString("HASHTAGS", theWord);
        Message msg = new Message();
        msg.setData(data);
        msg.what = Constants.QUERY_POST_DATA;
        mHandler.sendMessage(msg);
    }
}
