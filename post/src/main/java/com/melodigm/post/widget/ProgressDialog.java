package com.melodigm.post.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.melodigm.post.R;
import com.melodigm.post.util.LogUtil;

public class ProgressDialog {
	private Dialog mDialog;
	private ImageView imageView;
	final boolean isCancelable = true;

    /** YCNOTE - Dialog
     * 다이얼로그를 호출할때 getApplicationContext() 로 Context 를 인자로 보내면 다음과 같은 오류가 발생한다.
     * android.view.WindowManager$BadTokenException: Unable to add window -- token null is not for an application
     * Unable to add window -- token null is not valid; is your activity running?
     * getApplicationContext() 대신에 액티비티명.this 를 인자로 보내면 해결.
     */
	public void showDialog(Context context) {
		try {
            mDialog = new Dialog(context, R.style.DialogTheme);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_progress, null);
			imageView = (ImageView)view.findViewById(R.id.ivRotate);

            //파라미터를 세팅해줌
            LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setContentView(view, paramlinear);

            // Dialog 밖을 터치 했을 경우 Dialog 사라지게 하기
            mDialog.setCanceledOnTouchOutside(false);

            // Back키 눌렀을 경우 Dialog Cancle 여부 설정
            mDialog.setCancelable(isCancelable);

            // Dialog 호출시 배경화면이 검정색으로 바뀌는 것 막기
            mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            //Dialog 자체 배경을 투명하게 하기
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            // Dialog 밖의 View를 터치할 수 있게 하기 (다른 View를 터치시 Dialog Dismiss)
            //mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

            // Dialog 애니메이션 효과 주기
            /*mDialog.getWindow().getAttributes().windowAnimations = R.style.PostDialogSlideUpAnimation;*/

            // Dialog의 영역을 System Bar를 포함시키기
            /*mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);*/

            // Dialog Cancle시 Event 받기
            /*mDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this, "cancle listener",
                            Toast.LENGTH_SHORT).show();
                }
            });*/

            // Dialog Show시 Event 받기
            /*mDialog.setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this, "show listener",
                            Toast.LENGTH_SHORT).show();
                }
            });*/

            // Dialog Dismiss시 Event 받기
            /*mDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this, "dismiss listener",
                            Toast.LENGTH_SHORT).show();
                }
            });*/

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(mDialog.getWindow().getAttributes());
            //This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mDialog.getWindow().setAttributes(lp);

            mDialog.show();

            /**
             * YCNOTE - Animation(RotateAnimation)
             */
            RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(500);
            rotate.setRepeatCount(Animation.INFINITE);
            imageView.startAnimation(rotate);
		}
		catch(Exception ex) {
            LogUtil.e("DIALOG : " + ex.getMessage());
        }
	}
	
	public void dissDialog() {
		try {
            if (mDialog != null) mDialog.dismiss();
            if (imageView != null) imageView.clearAnimation();
		}
		catch(Exception ex) {
            LogUtil.e("DIALOG : " + ex.getMessage());
        }
	}
	
	public boolean isShow() {
		if(mDialog == null) return false;
		return mDialog.isShowing();
	}
}
