package com.melodigm.post.widget.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.widget.LetterSpacingTextView;

public class ScalePageTransformer implements ViewPager.PageTransformer{
    private static final float MIN_SCALE = 0.5f;
    private static final float MIN_ALPHA = 0.3f;

    public void transformPage(View view, float position) {
        LetterSpacingTextView lstvDayWeekDay = (LetterSpacingTextView)view.findViewById(R.id.lstvDayWeekDay);
        TextView tvKWD = (TextView)view.findViewById(R.id.tvKWD);

        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            //view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));

            /*float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                tvKWD.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                tvKWD.setTranslationX(-horzMargin + vertMargin / 2);
            }*/

            // Scale the page down (between MIN_SCALE and 1)
            tvKWD.setScaleX(scaleFactor);
            tvKWD.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            lstvDayWeekDay.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            //view.setAlpha(0);
        }
    }
}