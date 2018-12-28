package com.melodigm.post.widget.calendar;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.util.Calendar;

/**
 * Highlight Saturdays and Sundays with a background
 */
public class SaturdayDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();
    private final Drawable highlightDrawable;
    private static final int color = Color.parseColor("#228BC34A");
    private final Typeface mTypeface;

    public SaturdayDecorator(Typeface typeface) {
        highlightDrawable = new ColorDrawable(color);
        this.mTypeface = typeface;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SATURDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        //view.addSpan(new CustomTypefaceSpan("", this.mTypeface, 0, Color.parseColor("#FF00AFD5")));
    }
}
