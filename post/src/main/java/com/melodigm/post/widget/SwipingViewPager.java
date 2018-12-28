package com.melodigm.post.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.melodigm.post.util.LogUtil;

public class SwipingViewPager extends ViewPager {
    private boolean isSwiping;

    public SwipingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isSwiping = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isSwiping) {
            // YCLOVE - java.lang.IllegalArgumentException: pointerIndex out of range Bug Fix
            try {
                return super.onTouchEvent(event);
            } catch (IllegalArgumentException e) {
                LogUtil.e(e.getMessage());
                return false;
            } catch (IllegalStateException e) {
                LogUtil.e(e.getMessage());
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isSwiping) {
            // YCLOVE - java.lang.IllegalArgumentException: pointerIndex out of range Bug Fix
            try {
                return super.onInterceptTouchEvent(event);
            } catch (IllegalArgumentException e) {
                LogUtil.e(e.getMessage());
                return false;
            } catch (IllegalStateException e) {
                LogUtil.e(e.getMessage());
                return false;
            }
        }

        return false;
    }

    public void setSwiping(boolean isSwiping) {
        this.isSwiping = isSwiping;
    }
}