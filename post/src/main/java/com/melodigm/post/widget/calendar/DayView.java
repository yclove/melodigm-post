package com.melodigm.post.widget.calendar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckedTextView;

import com.melodigm.post.widget.calendar.MaterialCalendarView.ShowOtherDates;

import java.util.List;

import static com.melodigm.post.widget.calendar.MaterialCalendarView.showDecoratedDisabled;
import static com.melodigm.post.widget.calendar.MaterialCalendarView.showOtherMonths;
import static com.melodigm.post.widget.calendar.MaterialCalendarView.showOutOfRange;

/**
 * Display one day of a {@linkplain MaterialCalendarView}
 */
@SuppressLint("ViewConstructor")
class DayView extends CheckedTextView {

    private CalendarDay date;
    private int selectionColor = Color.GRAY;

    private final int fadeTime;
    private Drawable customBackground = null;
    private Drawable selectionDrawable;
    private DayFormatter formatter = DayFormatter.DEFAULT;

    private boolean isInRange = true;
    private boolean isInMonth = true;
    private boolean isDecoratedDisabled = false;
    @ShowOtherDates
    private int showOtherDates = MaterialCalendarView.SHOW_DEFAULTS;

    private static Context mContext;
    public DayView(Context context, CalendarDay day) {
        super(context);
        mContext = context;

        fadeTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        setSelectionColor(this.selectionColor);

        setGravity(Gravity.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }

        setDay(day);
    }

    public void setDay(CalendarDay date) {
        this.date = date;
        setText(getLabel());
    }

    /**
     * Set the new label formatter and reformat the current label. This preserves current spans.
     *
     * @param formatter new label formatter
     */
    public void setDayFormatter(DayFormatter formatter) {
        this.formatter = formatter == null ? DayFormatter.DEFAULT : formatter;
        CharSequence currentLabel = getText();
        Object[] spans = null;
        if (currentLabel instanceof Spanned) {
            spans = ((Spanned) currentLabel).getSpans(0, currentLabel.length(), Object.class);
        }
        SpannableString newLabel = new SpannableString(getLabel());
        if (spans != null) {
            for (Object span : spans) {
                newLabel.setSpan(span, 0, newLabel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        setText(newLabel);
    }

    @NonNull
    public String getLabel() {
        return formatter.format(date);
    }

    public void setSelectionColor(int color) {
        // YCLOVE : MaterialCalendarView - Calendar 선택 색상 고정
        color = Color.parseColor("#FFD73D66");
        this.selectionColor = color;
        regenerateBackground();
    }

    /**
     * @param drawable custom selection drawable
     */
    public void setSelectionDrawable(Drawable drawable) {
        if (drawable == null) {
            this.selectionDrawable = null;
        } else {
            this.selectionDrawable = drawable.getConstantState().newDrawable(getResources());
        }
        regenerateBackground();
    }

    /**
     * @param drawable background to draw behind everything else
     */
    public void setCustomBackground(Drawable drawable) {
        if (drawable == null) {
            this.customBackground = null;
        } else {
            this.customBackground = drawable.getConstantState().newDrawable(getResources());
        }
        invalidate();
    }

    public CalendarDay getDate() {
        return date;
    }

    private void setEnabled() {
        boolean enabled = isInMonth && isInRange && !isDecoratedDisabled;
        super.setEnabled(enabled);

        boolean showOtherMonths = showOtherMonths(showOtherDates);
        boolean showOutOfRange = showOutOfRange(showOtherDates) || showOtherMonths;
        boolean showDecoratedDisabled = showDecoratedDisabled(showOtherDates);

        boolean shouldBeVisible = enabled;

        if (!isInMonth && showOtherMonths) {
            shouldBeVisible = true;
        }

        if (!isInRange && showOutOfRange) {
            shouldBeVisible |= isInMonth;
        }

        if (isDecoratedDisabled && showDecoratedDisabled) {
            shouldBeVisible |= isInMonth && isInRange;
        }

        setVisibility(shouldBeVisible ? View.VISIBLE : View.INVISIBLE);
    }

    protected void setupSelection(@ShowOtherDates int showOtherDates, boolean inRange, boolean inMonth) {
        this.showOtherDates = showOtherDates;
        this.isInMonth = inMonth;
        this.isInRange = inRange;
        setEnabled();
    }

    private final Rect tempRect = new Rect();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (customBackground != null) {
            canvas.getClipBounds(tempRect);
            customBackground.setBounds(tempRect);
            customBackground.setState(getDrawableState());
            customBackground.draw(canvas);
        }
        super.onDraw(canvas);
    }

    private void regenerateBackground() {
        if (selectionDrawable != null) {
            setBackgroundDrawable(selectionDrawable);
        } else {
            setBackgroundDrawable(generateBackground(selectionColor, fadeTime));
        }
    }

    private static Drawable generateBackground(int color, int fadeTime) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.setExitFadeDuration(fadeTime);
        // YCLOVE : MaterialCalendarView - 선택 시 리소스 이미지로 변경.
        /*drawable.addState(new int[]{android.R.attr.state_checked}, generateCircleDrawable(color));*/
        drawable.addState(new int[]{android.R.attr.state_checked}, new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*drawable.addState(new int[]{android.R.attr.state_pressed}, generateRippleDrawable(color));*/
            drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
        } else {
            /*drawable.addState(new int[]{android.R.attr.state_pressed}, generateCircleDrawable(color));*/
            drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
        }

        drawable.addState(new int[]{}, generateCircleDrawable(Color.TRANSPARENT));

        return drawable;
    }

    public static class ColorCircleDrawable extends Drawable {
        private final Paint mPaint;
        private int mRadius = 0;

        public ColorCircleDrawable(final int color) {
            this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.mPaint.setColor(color);
        }

        @Override
        public void draw(final Canvas canvas) {
            final Rect bounds = getBounds();
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), mRadius, mPaint);
        }

        @Override
        protected void onBoundsChange(final Rect bounds) {
            super.onBoundsChange(bounds);
            // YCLOVE : MaterialCalendarView - 선택 시 리소스 이미지로 변경.
            /*mRadius = Math.min(bounds.width(), bounds.height()) / 2;*/
            Resources r = mContext.getResources();
            mRadius = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
        }

        @Override
        public void setAlpha(final int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(final ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static Drawable generateCircleDrawable(final int color) {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(0, 0, 0, 0, color, color, Shader.TileMode.REPEAT);
            }
        });
        return drawable;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Drawable generateRippleDrawable(final int color) {
        ColorStateList list = ColorStateList.valueOf(color);
        Drawable mask = generateCircleDrawable(Color.WHITE);
        return new RippleDrawable(list, null, mask);
    }

    /**
     * @param facade apply the facade to us
     */
    void applyFacade(DayViewFacade facade) {
        this.isDecoratedDisabled = facade.areDaysDisabled();
        setEnabled();

        setCustomBackground(facade.getBackgroundDrawable());
        setSelectionDrawable(facade.getSelectionDrawable());

        // Facade has spans
        List<DayViewFacade.Span> spans = facade.getSpans();
        if (!spans.isEmpty()) {
            String label = getLabel();
            SpannableString formattedLabel = new SpannableString(getLabel());
            for (DayViewFacade.Span span : spans) {
                formattedLabel.setSpan(span.span, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            setText(formattedLabel);
        }
        // Reset in case it was customized previously
        else {
            setText(getLabel());
        }
    }
}
