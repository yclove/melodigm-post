package com.melodigm.post.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateUtil {

    public static String getCurrentDate(String format) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(calendar.getTime());
    }

    public static Date addDays(Date baseDate, int daysToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        return calendar.getTime();
    }

    public static String getDateDisplay(Date from) {
        Date now = new Date();
        long difference = now.getTime() - from.getTime();
        long distanceInMin = difference / 60000;

        if ( 0 <= distanceInMin && distanceInMin <= 1 ) {
            return "바로 전"; // Less than 1 minute ago
        } else if ( 1 <= distanceInMin && distanceInMin <= 45 ) {
            return distanceInMin + "분 전";
        } else if ( 45 <= distanceInMin && distanceInMin <= 89 ) {
            return "1시간 전"; // About 1 hour
        } else if ( 90 <= distanceInMin && distanceInMin <= 1439 ) {
            return (distanceInMin / 60) + "시간 전"; // "About " + (distanceInMin / 60) + " hours ago
        } else if ( 1440 <= distanceInMin && distanceInMin <= 2529 ) {
            return "어제"; // 1 day
        } else if ( 2530 <= distanceInMin && distanceInMin <= 43199 ) {
            return (distanceInMin / 1440) + "일 전"; // (distanceInMin / 1440) + "days ago"
        } else if ( 43200 <= distanceInMin && distanceInMin <= 86399 ) {
            return "1달 전"; // About 1 month ago
        } else if ( 86400 <= distanceInMin && distanceInMin <= 525599 ) {
            return (distanceInMin / 43200) + "달 전"; // "About " + (distanceInMin / 43200) + " months ago"
        } else {
            long distanceInYears = distanceInMin / 525600;
            return distanceInYears + "년 전"; // "About " + distanceInYears + " years ago"
        }
    }

    public static String getDateDisplayBy12_24(Date from) {
        Date now = new Date();
        long difference = now.getTime() - from.getTime();
        long distanceInMin = difference / 60000;

        if ( 0 <= distanceInMin && distanceInMin <= 1 ) {
            return "바로 전"; // Less than 1 minute ago
        } else if ( 1 <= distanceInMin && distanceInMin <= 45 ) {
            return distanceInMin + "분 전";
        } else if ( 45 <= distanceInMin && distanceInMin <= 89 ) {
            return "1시간 전"; // About 1 hour
        } else {
            StringBuilder sb = new StringBuilder();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(from);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            if (hour >= 12) {
                sb.append(hour - 12).append( ":" ).append(minute).append(" pm");
            } else {
                sb.append(hour).append( ":" ).append(minute).append(" am");
            }
            return sb.toString();
        }
    }

    public static String getDateDisplayUnit(Date date, String template) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat dayFormat = new SimpleDateFormat(template, Locale.US);
        String weekDay = dayFormat.format(calendar.getTime());

        return weekDay.toUpperCase();
    }

    /**
     * @param date
     *      yyyy-MM-dd
     * @return
     */
    public static boolean isDate(String date) {
        StringBuffer reg = new StringBuffer("^((\\d{2}(([02468][048])|([13579][26]))-?((((0?");
        reg.append("[13578])|(1[02]))-?((0?[1-9])|([1-2][0-9])|(3[01])))");
        reg.append("|(((0?[469])|(11))-?((0?[1-9])|([1-2][0-9])|(30)))|");
        reg.append("(0?2-?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][12");
        reg.append("35679])|([13579][01345789]))-?((((0?[13578])|(1[02]))");
        reg.append("-?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))");
        reg.append("-?((0?[1-9])|([1-2][0-9])|(30)))|(0?2-?((0?[");
        reg.append("1-9])|(1[0-9])|(2[0-8]))))))");
        Pattern p = Pattern.compile(reg.toString());
        return p.matcher(date).matches();
    }

    public static String getConvertMsToFormat(long totalSecs) {
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        if (hours > 0)
            return String.format("%01d %02d´%02d˝", hours, minutes, seconds);
        else
            return String.format("%02d´%02d˝", minutes, seconds);
    }
}
