package com.melodigm.post.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.melodigm.post.R;

public class SpoilerTextView extends TextView {
    private static final int DEFAULT_TRIM_LENGTH = 200;
    private static final String ELLIPSIS = "... 더 보기";

    private CharSequence originalText;
    private CharSequence trimmedText;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;
    private ViewMoreSpan viewMoreSpan;

    public SpoilerTextView(Context context) {
        this(context, null);
    }

    public SpoilerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpoilerTextView);
        this.trimLength = typedArray.getInt(R.styleable.SpoilerTextView_trimLength, DEFAULT_TRIM_LENGTH);
        typedArray.recycle();
        viewMoreSpan = new ViewMoreSpan();
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimmedText(text);
        bufferType = type;
        setText();
    }

    private CharSequence getTrimmedText(CharSequence text) {
        if (originalText != null && originalText.length() > trimLength) {
            SpannableStringBuilder s = new SpannableStringBuilder(originalText, 0, trimLength + 1).append("...See more");
            s.setSpan(viewMoreSpan,s.length() -10, s.length() ,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return s;
        } else {
            return originalText;
        }
    }

    public CharSequence getOriginalText() {
        return originalText;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        trimmedText = getTrimmedText(originalText);
        setText();
    }

    public int getTrimLength() {
        return trimLength;
    }

    private class ViewMoreSpan extends ClickableSpan{
        @Override
        public void onClick(View widget) {
            trim = !trim;
            setText();
            requestFocusFromTouch();
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(getLinkTextColors().getDefaultColor());
            //ds.setUnderlineText(true);
        }
    }
}