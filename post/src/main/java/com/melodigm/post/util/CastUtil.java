package com.melodigm.post.util;

import android.content.Context;

import java.text.DecimalFormat;
import java.util.Locale;

public class CastUtil {
    /**
     * 픽셀단위를 현재 디스플레이 화면에 비례한 크기로 반환합니다.
     *
     * @param PX 픽셀
     * @return 변환된 값 (DP)
     */
    public static int PxToDp(Context context, int PX) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (PX / 0.5f * scale);
    }

    /**
     * 현재 디스플레이 화면에 비례한 DP단위를 픽셀 크기로 반환합니다.
     *
     * @param DP 픽셀
     * @return 변환된 값 (pixel)
     */
    public static int DpToPx(Context context, final int DP) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (DP * scale + 0.5f);
    }

    public static float DpToPx(Context context, final float DP) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return DP * scale + 0.5f;
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * String을 byte[]로 변환
     * @param a_Key
     * @return
     */
    public static byte[] StringToByte(String key){
        byte[] byteKey;
        byteKey = key.getBytes();
        return byteKey;
    }

    public static String getPercent(long total, long size) {
        double _total = Double.valueOf(String.valueOf(total));
        double _size = Double.valueOf(String.valueOf(size));
        return String.format(Locale.US, "%.2f%%", _size / _total * 100.0);
    }
}
