package com.melodigm.post.util;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 날짜비교
     *
     * @param now
     * @param dataCal
     * @return 일치 true , 불일치 false
     */
    public static boolean isSameDay(Calendar now, Calendar dataCal) {
        if (now.get(Calendar.YEAR) == dataCal.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == dataCal.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_MONTH) == dataCal.get(Calendar.DAY_OF_MONTH))
            return true;
        else
            return false;
    }

    /**
     * @param calNow 현재시간 Calendar
     * @param stH    근무시작 시
     * @param stM    근무시작 분
     * @param endH   근무종료 시
     * @param endM   근무종료 분
     * @return
     */
    public static boolean isWorking(Calendar calNow, int stH, int stM, int endH, int endM) {
        int hour = calNow.get(Calendar.HOUR_OF_DAY);
        int min = calNow.get(Calendar.MINUTE);
        int timeNow = Integer.valueOf(hour + String.format("%02d", min)); // 분을 2자릿수로 만듬(range 비교하기 위함)
        int timeStart = Integer.valueOf(stH + String.format("%02d", stM)); // 분을 2자릿수로 만듬(range 비교하기 위함)
        int timeEnd = Integer.valueOf(endH + String.format("%02d", endM)); // 분을 2자릿수로 만듬(range 비교하기 위함)
        if (timeStart <= timeNow && timeNow <= timeEnd)
            return true;
        else
            return false;
    }

    // to calculate difference between two dates in milliseconds
    public static long getDateDiffInMsec(Date da, Date db) {
        long diffMSec = 0;
        diffMSec = db.getTime() - da.getTime();
        return diffMSec;
    }

    // to convert Milliseconds into DD HH:MM:SS format.
    public static String getDateFromMsec(long diffMSec, int[] returnList) {
        int left = 0;
        int ss = 0;
        int mm = 0;
        int hh = 0;
        int dd = 0;
        left = (int) (diffMSec / 1000);
        ss = left % 60;
        left = (int) left / 60;
        if (left > 0) {
            mm = left % 60;
            left = (int) left / 60;
            if (left > 0) {
                hh = left % 24;
                left = (int) left / 24;
                if (left > 0) {
                    dd = left;
                }
            }
        }
        returnList[0] = dd;
        returnList[1] = hh;
        returnList[2] = mm;
        returnList[3] = ss;

        String diff = Integer.toString(dd) + ":" + Integer.toString(hh) + ":" + Integer.toString(mm) + ":" + Integer.toString(ss);
        return diff;

    }
}
