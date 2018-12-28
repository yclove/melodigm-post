package com.melodigm.post.widget.hashtaglink;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.util.handler.WeakRefHandler;

/**
 * Created by David Nuon on 3/7/14.
 */
public class CalloutLink extends ClickableSpan {
    Context context;
    WeakRefHandler mHandler;
    String mColor;

    public CalloutLink(Context ctx, String color, WeakRefHandler handler) {
        super();
        this.context = ctx;
        this.mColor = color;
        this.mHandler = handler;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(Integer.parseInt(mColor, 16) + 0xFF000000);
        ds.setTypeface(Typeface.DEFAULT_BOLD);

    }

    @Override
    public void onClick(View widget) {
        TextView tv = (TextView) widget;
        Spanned s = (Spanned) tv.getText();
        int start = s.getSpanStart(this);
        int end = s.getSpanEnd(this);
        String theWord = s.subSequence(start + 1, end).toString();

        Bundle data = new Bundle();
        data.putString("HASHTAGS", theWord);
        data.putString("COLORS", mColor);
        Message msg = new Message();
        msg.setData(data);
        msg.what = Constants.QUERY_POST_DATA;
        mHandler.sendMessage(msg);
    }
}