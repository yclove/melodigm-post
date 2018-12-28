package com.melodigm.post.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.service.MusicService;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;

public class NotificationBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                	if(!PlayerConstants.SONG_PAUSED){
    					Controls.pauseControl(context);
                	}else{
    					Controls.playControl(context);
                	}
                	break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                	break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                	break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                	break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                	LogUtil.e("TAG: KEYCODE_MEDIA_NEXT");
                	Controls.nextControl(context);
                	break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    LogUtil.e("TAG: KEYCODE_MEDIA_PREVIOUS");
                	Controls.previousControl(context);
                	break;
            }
		}  else {
            	if (intent.getAction().equals(Constants.NOTIFY_PLAY)) {
    				Controls.playControl(context);
        		} else if (intent.getAction().equals(Constants.NOTIFY_PAUSE)) {
    				Controls.pauseControl(context);
        		} else if (intent.getAction().equals(Constants.NOTIFY_NEXT)) {
        			Controls.nextControl(context);
        		} else if (intent.getAction().equals(Constants.NOTIFY_DELETE)) {
					Intent i = new Intent(context, MusicService.class);
					context.stopService(i);
					/*Intent in = new Intent(context, PostActivity.class);
			        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        context.startActivity(in);*/
        		}else if (intent.getAction().equals(Constants.NOTIFY_PREVIOUS)) {
    				Controls.previousControl(context);
        		}
		}
	}
	
	public String ComponentName() {
		return this.getClass().getName(); 
	}
}
