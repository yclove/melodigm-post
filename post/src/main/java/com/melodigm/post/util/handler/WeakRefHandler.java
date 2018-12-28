package com.melodigm.post.util.handler;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class WeakRefHandler extends Handler {
    private final WeakReference<IOnHandlerMessage> mHandlerActivity;
    public WeakRefHandler(IOnHandlerMessage activity) {
        mHandlerActivity = new WeakReference<IOnHandlerMessage>(activity);
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        IOnHandlerMessage activity = (IOnHandlerMessage) mHandlerActivity.get();
        if ( activity == null ) return;
        activity.handleMessage(msg);
    }
}
