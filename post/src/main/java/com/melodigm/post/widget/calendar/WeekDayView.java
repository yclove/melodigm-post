package com.melodigm.post.widget.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Display a day of the week
 */
@SuppressLint("ViewConstructor")
class WeekDayView extends TextView {

    private WeekDayFormatter formatter = WeekDayFormatter.DEFAULT;
    private int dayOfWeek;

    public WeekDayView(Context context, int dayOfWeek) {
        super(context);

        setGravity(Gravity.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }

        setDayOfWeek(dayOfWeek);
    }

    public void setWeekDayFormatter(WeekDayFormatter formatter) {
        this.formatter = formatter == null ? WeekDayFormatter.DEFAULT : formatter;
        setDayOfWeek(dayOfWeek);
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        setText(formatter.format(dayOfWeek));

        // YCLOVE : MaterialCalendarView - 요일에 스타일을 입힌다.
        if (dayOfWeek == 1) {
            setTextColor(Color.parseColor("#FFD73D66"));
        } else if (dayOfWeek == 7) {
            setTextColor(Color.parseColor("#FF00AFD5"));
        } else {
            setTextColor(Color.parseColor("#80000000"));
        }
        // TODO : YCLOVE
//        setTypeface(Typekit.createFromAsset(getContext(), "fonts/Montserrat-Regular.ttf"));
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
    }

    public void setDayOfWeek(Calendar calendar) {
        setDayOfWeek(CalendarUtils.getDayOfWeek(calendar));
    }
}
