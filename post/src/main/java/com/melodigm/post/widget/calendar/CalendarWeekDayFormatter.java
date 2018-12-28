package com.melodigm.post.widget.calendar;

import java.util.Calendar;
import java.util.Locale;

/**
 * Use a {@linkplain Calendar} to get week day labels.
 *
 * @see Calendar#getDisplayName(int, int, Locale)
 */
public class CalendarWeekDayFormatter implements WeekDayFormatter {

    private final Calendar calendar;

    /**
     * Format with a specific calendar
     *
     * @param calendar Calendar to retrieve formatting information from
     */
    public CalendarWeekDayFormatter(Calendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Format with a default calendar
     */
    public CalendarWeekDayFormatter() {
        this(CalendarUtils.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence format(int dayOfWeek) {
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        // YCLOVE - Calendar 영문표기를 위하여 변경
        // Locale.getDefault() > Locale.US
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US).toUpperCase();
    }
}
