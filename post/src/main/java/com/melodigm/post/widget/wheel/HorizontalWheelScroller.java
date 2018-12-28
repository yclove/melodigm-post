package com.melodigm.post.widget.wheel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class HorizontalWheelScroller extends WheelScroller {
	/**
     * Constructor
     * @param context the current context
     * @param listener the scrolling listener
     */
    public HorizontalWheelScroller(Context context, ScrollingListener listener) {
    	super(context, listener);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
        
        scroller = new Scroller(context);

        this.listener = listener;
        this.context = context;
    }
    
    /**
     * Set the the specified scrolling interpolator
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(context, interpolator);
    }
    
    /**
     * Scroll the wheel
     * @param distance the scrolling distance
     * @param time the scrolling duration
     */
    public void scroll(int distance, int time) {
        scroller.forceFinished(true);

        lastScrollY = 0;
        
        scroller.startScroll(0, 0, distance, 0, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);
        
        startScrolling();
    }
   
    /**
     * Stops scrolling
     */
    public void stopScrolling() {
        scroller.forceFinished(true);
    }
    
    /**
     * Handles Touch event 
     * @param event the motion event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchedY = event.getX();
                scroller.forceFinished(true);
                clearMessages();
                break;
    
            case MotionEvent.ACTION_MOVE:
                // perform scrolling
                int distanceX = (int)(event.getX() - lastTouchedY);
                if (distanceX != 0) {
                    startScrolling();
                    listener.onScroll(distanceX);
                    lastTouchedY = event.getX();
                }
                break;
        }
        
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }

        return true;
    }
    
    // gesture listener
    protected SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Do scrolling in onTouchEvent() since onScroll() are not call immediately
            //  when user touch and move the wheel
            return true;
        }
        
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lastScrollY = 0;
            final int maxX = 0x7FFFFFFF;
            final int minX = -maxX;
            scroller.fling(lastScrollY, 0, (int) -velocityX, 0, minX, maxX, 0, 0);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    // Messages
    protected final int MESSAGE_SCROLL = 0;
    protected final int MESSAGE_JUSTIFY = 1;
    
    /**
     * Set next message to queue. Clears queue before.
     * 
     * @param message the message to set
     */
    protected void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    protected void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }
    
    // animation handler
    protected Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currX = scroller.getCurrX();
            int delta = lastScrollY - currX;
            lastScrollY = currX;
            if (delta != 0) {
                listener.onScroll(delta);
            }
            
            // scrolling is not finished when it comes to final Y
            // so, finish it manually 
            if (Math.abs(currX - scroller.getFinalX()) < MIN_DELTA_FOR_SCROLLING) {
                currX = scroller.getFinalX();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };
    
    /**
     * Justifies wheel
     */
    protected void justify() {
        listener.onJustify();
        setNextMessage(MESSAGE_JUSTIFY);
    }

    /**
     * Starts scrolling
     */
    protected void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            listener.onStarted();
        }
    }

    /**
     * Finishes scrolling
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinished();
            isScrollingPerformed = false;
        }
    }
}
