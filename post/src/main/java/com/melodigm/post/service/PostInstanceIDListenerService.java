package com.melodigm.post.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class PostInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, PostRegistrationIntentService.class);
        startService(intent);
    }
}
