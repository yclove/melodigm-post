package com.melodigm.post.widget.calendar;

import android.animation.Animator;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.sql.Date;

class TitleChanger {

    public static final int DEFAULT_ANIMATION_DELAY = 400;
    public static final int DEFAULT_Y_TRANSLATION_DP = 20;

    private final TextView title;
    private LetterSpacingTextView titleYear = null, titleMonth = null, titleMonthEng = null;
    private TitleFormatter titleFormatter;

    private final int animDelay;
    private final int animDuration;
    private final int yTranslate;
    private final Interpolator interpolator = new DecelerateInterpolator(2f);

    private long lastAnimTime = 0;
    private CalendarDay previousMonth = null;

    public TitleChanger(TextView title) {
        this.title = title;

        Resources res = title.getResources();

        animDelay = DEFAULT_ANIMATION_DELAY;

        animDuration = res.getInteger(android.R.integer.config_shortAnimTime) / 2;

        yTranslate = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_Y_TRANSLATION_DP, res.getDisplayMetrics()
        );
    }

    public void setTitleYear(LetterSpacingTextView titleYear) {
        this.titleYear = titleYear;
    }

    public void setTitleMonth(LetterSpacingTextView titleMonth) {
        this.titleMonth = titleMonth;
    }

    public void setTitleMonthEng(LetterSpacingTextView titleMonthEng) {
        this.titleMonthEng = titleMonthEng;
    }

    public void change(final CalendarDay currentMonth) {
        long currentTime = System.currentTimeMillis();

        if (currentMonth == null) {
            return;
        }

        if (TextUtils.isEmpty(title.getText()) || (currentTime - lastAnimTime) < animDelay) {
            doChange(currentTime, currentMonth, false);
        }

        if (currentMonth.equals(previousMonth) || currentMonth.getMonth() == previousMonth.getMonth()) {
            return;
        }

        doChange(currentTime, currentMonth, true);
    }

    private void doChange(final long now, final CalendarDay currentMonth, boolean animate) {
        title.animate().cancel();
        title.setTranslationY(0);
        title.setAlpha(1);
        lastAnimTime = now;

        // YCLOVE - Calendar newTitle(yyyy-MM-dd)
        final CharSequence newTitle = titleFormatter.format(currentMonth);

        try {
            final String titleYearVal = DateUtil.getDateDisplayUnit(Date.valueOf(newTitle.toString()), "yyyy");
            final String titleMonthVal = DateUtil.getDateDisplayUnit(Date.valueOf(newTitle.toString()), "MM");
            final String titleMonthEngVal = DateUtil.getDateDisplayUnit(Date.valueOf(newTitle.toString()), "MMMM");

            if (!animate) {
                title.setText(newTitle);
                if (titleYear != null) titleYear.setText(titleYearVal);
                if (titleMonth != null) titleMonth.setText(titleMonthVal);
                if (titleMonthEng != null) titleMonthEng.setText(titleMonthEngVal);
            } else {
                final int yTranslation = yTranslate * (previousMonth.isBefore(currentMonth) ? 1 : -1);

                title.animate()
                        .translationY(yTranslation * -1)
                        .alpha(0)
                        .setDuration(animDuration)
                        .setInterpolator(interpolator)
                        .setListener(new AnimatorListener() {

                            @Override
                            public void onAnimationCancel(Animator animator) {
                                title.setTranslationY(0);
                                title.setAlpha(1);
                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                title.setText(newTitle);
                                title.setTranslationY(yTranslation);
                                title.animate()
                                        .translationY(0)
                                        .alpha(1)
                                        .setDuration(animDuration)
                                        .setInterpolator(interpolator)
                                        .setListener(new AnimatorListener())
                                        .start();
                            }
                        }).start();

                if (titleYear != null && !titleYearVal.equals(titleYear.getText().toString())) {
                    titleYear.animate()
                            .translationY(yTranslation * -1)
                            .alpha(0)
                            .setDuration(animDuration)
                            .setInterpolator(interpolator)
                            .setListener(new AnimatorListener() {

                                @Override
                                public void onAnimationCancel(Animator animator) {
                                    titleYear.setTranslationY(0);
                                    titleYear.setAlpha(1);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    titleYear.setText(titleYearVal);
                                    titleYear.setTranslationY(yTranslation);
                                    titleYear.animate()
                                            .translationY(0)
                                            .alpha(1)
                                            .setDuration(animDuration)
                                            .setInterpolator(interpolator)
                                            .setListener(new AnimatorListener())
                                            .start();
                                }
                            }).start();
                }

                if (titleMonth != null) {
                    titleMonth.animate()
                            .translationY(yTranslation * -1)
                            .alpha(0)
                            .setDuration(animDuration)
                            .setInterpolator(interpolator)
                            .setListener(new AnimatorListener() {

                                @Override
                                public void onAnimationCancel(Animator animator) {
                                    titleMonth.setTranslationY(0);
                                    titleMonth.setAlpha(1);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    titleMonth.setText(titleMonthVal);
                                    titleMonth.setTranslationY(yTranslation);
                                    titleMonth.animate()
                                            .translationY(0)
                                            .alpha(1)
                                            .setDuration(animDuration)
                                            .setInterpolator(interpolator)
                                            .setListener(new AnimatorListener())
                                            .start();
                                }
                            }).start();
                }

                if (titleMonthEng != null) {
                    titleMonthEng.animate()
                            .translationY(yTranslation * -1)
                            .alpha(0)
                            .setDuration(animDuration)
                            .setInterpolator(interpolator)
                            .setListener(new AnimatorListener() {

                                @Override
                                public void onAnimationCancel(Animator animator) {
                                    titleMonthEng.setTranslationY(0);
                                    titleMonthEng.setAlpha(1);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    titleMonthEng.setText(titleMonthEngVal);
                                    titleMonthEng.setTranslationY(yTranslation);
                                    titleMonthEng.animate()
                                            .translationY(0)
                                            .alpha(1)
                                            .setDuration(animDuration)
                                            .setInterpolator(interpolator)
                                            .setListener(new AnimatorListener())
                                            .start();
                                }
                            }).start();
                }
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }

        previousMonth = currentMonth;
    }

    public TitleFormatter getTitleFormatter() {
        return titleFormatter;
    }

    public void setTitleFormatter(TitleFormatter titleFormatter) {
        this.titleFormatter = titleFormatter;
    }

    public void setPreviousMonth(CalendarDay previousMonth) {
        this.previousMonth = previousMonth;
    }
}
