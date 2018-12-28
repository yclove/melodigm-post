package com.melodigm.post.widget.hashtaglink;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.handler.WeakRefHandler;

public class AgreementTag extends ClickableSpan{
    Context context;
    WeakRefHandler mHandler;
    TextPaint textPaint;
    String mTag;

    public AgreementTag(Context ctx, WeakRefHandler handler, String tag) {
        super();
        this.context = ctx;
        this.mHandler = handler;
        this.mTag = tag;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setColor(Color.parseColor("#FFFFFFFF"));
    }

    @Override
    public void onClick(View widget) {
        if (mHandler == null) return;

        Bundle data = new Bundle();
        data.putString("AGREEMENT_TYPE", mTag);
        Message msg = new Message();
        msg.setData(data);
        msg.what = Constants.QUERY_AGREEMENT_DATA;
        mHandler.sendMessage(msg);
    }
}
