package com.melodigm.post.widget.wheel;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.melodigm.post.R;
import com.melodigm.post.widget.wheel.adapters.WheelViewAdapter;

public class HorizontalWheelView extends WheelView {
	
	protected int itemWidth;
	
	/**
	 * Constructor
	 */
	public HorizontalWheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
		PADDING = 0;
		//bgResourceId = R.drawable.horizontal_wheel_bg;
		bgResourceId = android.R.color.transparent;
	}

	/**
	 * Constructor
	 */
	public HorizontalWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
		PADDING = 0;
		//bgResourceId = R.drawable.horizontal_wheel_bg;
		bgResourceId = android.R.color.transparent;
	}

	/**
	 * Constructor
	 */
	public HorizontalWheelView(Context context) {
		super(context);
		initData(context);
		PADDING = 0;
		//bgResourceId = R.drawable.horizontal_wheel_bg;
		bgResourceId = android.R.color.transparent;
	}
	
	/**
	 * Initializes class data
	 * @param context the context
	 */
	public void initData(Context context) {
	    scroller = new HorizontalWheelScroller(getContext(), scrollingListener);
	}
	
	// Scrolling listener
	HorizontalWheelScroller.ScrollingListener scrollingListener = new HorizontalWheelScroller.ScrollingListener() {
        public void onStarted() {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
        
        public void onScroll(int distance) {
            doScroll(distance);
            
            int width = getWidth();
            if (scrollingOffset > width) {
                scrollingOffset = width;
                scroller.stopScrolling();
            } else if (scrollingOffset < -width) {
                scrollingOffset = -width;
                scroller.stopScrolling();
            }
        }
        
        public void onFinished() {
            if (isScrollingPerformed) {
                notifyScrollingListenersAboutEnd();
                isScrollingPerformed = false;
            }
            
            scrollingOffset = 0;
            invalidate();
        }

        public void onJustify() {
            if (Math.abs(scrollingOffset) > HorizontalWheelScroller.MIN_DELTA_FOR_SCROLLING) {
                scroller.scroll(scrollingOffset, 0);
            }
        }
    };
	
	/**
	 * Set the the specified scrolling interpolator
	 * @param interpolator the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.setInterpolator(interpolator);
	}
	
	/**
	 * Gets count of visible items
	 * 
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * Sets the desired count of visible items.
	 * Actual amount of visible items depends on wheel layout parameters.
	 * To apply changes and rebuild view call measure(). 
	 * 
	 * @param count the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
	}

	/**
	 * Gets view adapter
	 * @return the view adapter
	 */
	public WheelViewAdapter getViewAdapter() {
		return viewAdapter;
	}

	// Adapter listener
    protected DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            invalidateWheel(false);
        }

        @Override
        public void onInvalidated() {
            invalidateWheel(true);
        }
    };

	/**
	 * Sets view adapter. Usually new adapters contain different views, so
	 * it needs to rebuild view by calling measure().
	 *  
	 * @param viewAdapter the view adapter
	 */
	public void setViewAdapter(WheelViewAdapter viewAdapter) {
	    if (this.viewAdapter != null) {
	        this.viewAdapter.unregisterDataSetObserver(dataObserver);
	    }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(dataObserver);
        }
        
        invalidateWheel(true);
	}
	
	/**
	 * Adds wheel changing listener
	 * @param listener the listener 
	 */
	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes wheel changing listener
	 * @param listener the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}
	
	/**
	 * Notifies changing listeners
	 * @param oldValue the old wheel value
	 * @param newValue the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds wheel scrolling listener
	 * @param listener the listener 
	 */
	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 * @param listener the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}
	
	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

    /**
     * Adds wheel clicking listener
     * @param listener the listener 
     */
    public void addClickingListener(OnWheelClickedListener listener) {
        clickingListeners.add(listener);
    }

    /**
     * Removes wheel clicking listener
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener) {
        clickingListeners.remove(listener);
    }
    
    /**
     * Notifies listeners about clicking
     */
    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }

	/**
	 * Gets current value
	 * 
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 * 
	 * @param index the item index
	 * @param animated the animation flag
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return; // throw?
		}
		
		int itemCount = viewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount) {
			if (isCyclic) {
				while (index < 0) {
					index += itemCount;
				}
				index %= itemCount;
			} else{
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {
			    int itemsToScroll = index - currentItem;
			    if (isCyclic) {
			        int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
			        if (scroll < Math.abs(itemsToScroll)) {
			            itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
			        }
			    }
				scroll(itemsToScroll, 0);
			} else {
				scrollingOffset = 0;
			
				int old = currentItem;
				currentItem = index;
			
				notifyChangingListeners(old, currentItem);
			
				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 * 
	 * @param index the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}	
	
	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 * @param isCyclic the flag to set
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;
		invalidateWheel(false);
	}
	
	/**
	 * Invalidates wheel
	 * @param clearCaches if true then cached views will be clear
	 */
    public void invalidateWheel(boolean clearCaches) {
        if (clearCaches) {
            recycle.clearAll();
            if (itemsLayout != null) {
                itemsLayout.removeAllViews();
            }
            scrollingOffset = 0;
        } else if (itemsLayout != null) {
            // cache all items
	        recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());         
        }
        
        invalidate();
	}

	/**
	 * Initializes resources
	 */
	protected void initResourcesIfNecessary() {
		if (centerDrawable == null) {
			//centerDrawable = getContext().getResources().getDrawable(R.drawable.horizontal_wheel_val);
			centerDrawable = getContext().getResources().getDrawable(android.R.color.transparent);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(Orientation.LEFT_RIGHT, SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(Orientation.RIGHT_LEFT, SHADOWS_COLORS);
		}

		setBackgroundResource(bgResourceId);
	}
	
	/**
	 * Calculates desired height for layout
	 * 
	 * @param layout
	 *            the source layout
	 * @return the desired layout height
	 */
	protected int getDesiredWidth(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemWidth = layout.getChildAt(0).getMeasuredWidth();
		}

		int desired = itemWidth * visibleItems - itemWidth * ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumWidth());
	}

	/**
	 * Returns height of wheel item
	 * @return the item height
	 */
	protected int getItemHeight() {
		if (itemHeight != 0) {
			return itemHeight;
		}
		
		if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
			itemHeight = itemsLayout.getChildAt(0).getHeight();
			return itemHeight;
		}
		
		return getHeight() / visibleItems;
	}
	
	/**
	 * Returns width of wheel item
	 * @return the item height
	 */
	protected int getItemWidth() {
		if (itemWidth != 0) {
			return itemWidth;
		}
		
		if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
			itemWidth = itemsLayout.getChildAt(0).getWidth();
			return itemWidth;
		}
		
		return getHeight() / visibleItems;
	}

	/**
	 * Calculates control width and creates text layouts
	 * @param widthSize the input layout width
	 * @param mode the layout mode
	 * @return the calculated control width
	 */
	protected int calculateLayoutHeight(int heightSize, int mode) {
		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    itemsLayout.measure(MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED), 
	                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int height = itemsLayout.getMeasuredHeight();

		if (mode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height += 2 * PADDING;

			// Check against our minimum width
			height = Math.max(height, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && heightSize < height) {
				height = heightSize;
			}
		}
		
        itemsLayout.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.EXACTLY, height - 2 * PADDING), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return height;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		buildViewForMeasuring();
		
		int height = calculateLayoutHeight(heightSize, heightMode);

		int width;
		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = getDesiredWidth(itemsLayout);

			if (widthMode == MeasureSpec.AT_MOST) {
				width = Math.min(width, widthSize);
			}
		}

		setMeasuredDimension(width, height);
	}
	
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	layout(r - l, b - t);
    }

    /**
     * Sets layouts width and height
     * @param width the layout width
     * @param height the layout height
     */
    protected void layout(int width, int height) {
		int itemsHeight = height - 2 * PADDING;
		
		itemsLayout.layout(0, 0, width, itemsHeight);
    }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
	        updateView();

	        drawItems(canvas);
	        drawCenterRect(canvas);
		}
		
        //drawShadows(canvas);
	}

	/**
	 * Draws shadows on top and bottom of control
	 * @param canvas the canvas for drawing
	 */
	protected void drawShadows(Canvas canvas) {
		int width = (int)(1.5 * getItemWidth());
		topShadow.setBounds(0, 0, width, getHeight());
		topShadow.draw(canvas);

		bottomShadow.setBounds(getWidth() - width, 0, getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * Draws items
	 * @param canvas the canvas for drawing
	 */
	protected void drawItems(Canvas canvas) {
		canvas.save();
		
		int left = (currentItem - firstItem) * getItemWidth() + (getItemWidth() - getWidth()) / 2;
		canvas.translate(- left + scrollingOffset, PADDING);
		
		itemsLayout.draw(canvas);

		canvas.restore();
	}

	/**
	 * Draws rect for current value
	 * @param canvas the canvas for drawing
	 */
	protected void drawCenterRect(Canvas canvas) {
		int center = getWidth() / 2;
		int offset = (int) (getItemWidth() / 2 * 1.2);
		centerDrawable.setBounds(center - offset, 0, center + offset, getHeight());
		centerDrawable.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}
		
		switch (event.getAction()) {
		    case MotionEvent.ACTION_MOVE:
		        if (getParent() != null) {
		            getParent().requestDisallowInterceptTouchEvent(true);
		        }
		        break;
		        
		    case MotionEvent.ACTION_UP:
		        if (!isScrollingPerformed) {
		            int distance = (int) event.getX() - getWidth() / 2;
		            if (distance > 0) {
		                distance += getItemWidth() / 2;
		            } else {
                        distance -= getItemWidth() / 2;
		            }
		            int items = distance / getItemHeight();
		            if (items != 0 && isValidItemIndex(currentItem + items)) {
	                    notifyClickListenersAboutClick(currentItem + items);
		            }
		        }
		        break;
		}

		return scroller.onTouchEvent(event);
	}
	
	/**
	 * Scrolls the wheel
	 * @param delta the scrolling value
	 */
	protected void doScroll(int delta) {
		scrollingOffset += delta;
		
		int itemWidth = getItemWidth();
		int count = scrollingOffset / itemWidth;

		int pos = currentItem - count;
		int itemCount = viewAdapter.getItemsCount();
		
	    int fixPos = scrollingOffset % itemWidth;
	    if (Math.abs(fixPos) <= itemWidth / 2) {
	        fixPos = 0;
	    }
		if (isCyclic && itemCount > 0) {
		    if (fixPos > 0) {
		        pos--;
                count++;
		    } else if (fixPos < 0) {
		        pos++;
		        count--;
		    }
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount;
			}
			pos %= itemCount;
		} else {
			// 
			if (pos < 0) {
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {
				count = currentItem - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {
                pos--;
                count++;
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++;
                count--;
            }
		}
		
		int offset = scrollingOffset;
		if (pos != currentItem) {
			setCurrentItem(pos, false);
		} else {
			invalidate();
		}
		
		// update offset
		scrollingOffset = offset - count * itemWidth;
		if (scrollingOffset > getWidth()) {
			scrollingOffset = scrollingOffset % getWidth() + getWidth();
		}
	}
		
	/**
	 * Scroll the wheel
	 * @param itemsToSkip items to scroll
	 * @param time scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemWidth() - scrollingOffset;
        scroller.scroll(distance, time);
	}
	
	/**
	 * Calculates range for wheel items
	 * @return the items range
	 */
	protected ItemsRange getItemsRange() {
        if (getItemWidth() == 0) {
            return null;
        }
        
		int first = currentItem;
		int count = 1;
		
		while (count * getItemWidth() < getWidth()) {
			first--;
			count += 2; // top + bottom items
		}
		
		if (scrollingOffset != 0) {
			if (scrollingOffset > 0) {
				first--;
			}
			count++;
			
			// process empty items above the first or below the second
			int emptyItems = scrollingOffset / getItemWidth();
			first -= emptyItems;
			count += Math.asin(emptyItems);
		}
		return new ItemsRange(first, count);
	}

	/**
	 * Creates item layouts if necessary
	 */
	protected void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new LinearLayout(getContext());
			itemsLayout.setOrientation(LinearLayout.HORIZONTAL);
		}
	}
}
