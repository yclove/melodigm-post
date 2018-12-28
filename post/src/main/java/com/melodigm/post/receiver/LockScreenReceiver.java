package com.melodigm.post.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.melodigm.post.LockScreenActivity;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.SPUtil;

public class LockScreenReceiver extends BroadcastReceiver {
    private KeyguardManager mKeyguardManager = null;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
    private TelephonyManager mTelephonyManager = null;
    // true = 정상, false = 통화중 or 벨이 울리는 중
    private boolean isPhoneIdle = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.e("LockScreenReceiver > onReceive ▶ " + intent.getAction());

        // 화면이 켜질 때는 ACTION_SCREEN_ON Intent가 broadcast
        // 화면이 꺼졌을 때는 ACTION_SCREEN_OFF Intent가 broadcast
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (PlayerConstants.SONG_PAUSED)
                reenableKeyguard();
            else {
                if (mKeyguardManager == null)
                    mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

                if (mKeyguardLock == null)
                    mKeyguardLock = mKeyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);

                if (mTelephonyManager == null){
                    mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                    mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
                }

                if (isPhoneIdle && SPUtil.getBooleanSharedPreference(context, Constants.SP_PLAYER_DISPLAY_LOCK_SCREEN_ALBUM, true)) {
                    disableKeyguard();

                    Intent i = new Intent(context, LockScreenActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }
    }

    public void reenableKeyguard() {
        // 기본 잠금화면 나타내기
        if (mKeyguardLock != null) mKeyguardLock.reenableKeyguard();
    }

    public void disableKeyguard() {
        // 기본 잠금화면 없애기
        if (mKeyguardLock != null) mKeyguardLock.disableKeyguard();
    }

    private PhoneStateListener phoneListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber){
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE :
                    isPhoneIdle = true;
                    break;
                case TelephonyManager.CALL_STATE_RINGING :
                    isPhoneIdle = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK :
                    isPhoneIdle = false;
                    break;
            }
        }
    };
}