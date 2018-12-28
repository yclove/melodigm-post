package com.melodigm.post.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.hashtaglink.Hashtag;
import com.melodigm.post.widget.hashtaglink.Moretag;

import java.util.ArrayList;
import java.util.List;

public class EllipsizingTextView extends TextView {
    private String ELLIPSIS = "#... 더 보기";
    private String ELLIPSIS_NON_LINK = "...";

    private boolean isMoreLinkEnabled = true;

    public void setMoreLinkEnabled(boolean moreLinkEnabled) {
        if (!moreLinkEnabled)
            ELLIPSIS = ELLIPSIS_NON_LINK;
        this.isMoreLinkEnabled = moreLinkEnabled;
    }

    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean ellipsized);
    }

    private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<>();
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private String fullText;
    private int maxLines = -1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;

    private Context mContext;
    private WeakRefHandler mHandler;
    private boolean scrollEnabled = true;

    public void setHandler(WeakRefHandler handler) {
        this.mHandler = handler;
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    public EllipsizingTextView(Context context) {
        super(context);
        init(context);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setHighlightColor(Color.parseColor("#00000000"));
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        ellipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text.toString();
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        int maxLines = getMaxLines();
        String workingText = fullText;
        boolean ellipsized = false;
        if (maxLines != -1) {
            Layout layout = createWorkingLayout(workingText);
            if (layout.getLineCount() > maxLines) {
                workingText = fullText.substring(0, layout.getLineEnd(maxLines - 1)).trim();
                while (createWorkingLayout(workingText + ELLIPSIS).getLineCount() > maxLines) {
                    int lastSpace = workingText.lastIndexOf(' ');
                    if (lastSpace == -1) {
                        break;
                    }
                    workingText = workingText.substring(0, lastSpace);
                }
                workingText = workingText + ELLIPSIS;
                ellipsized = true;
            }
        }
        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(workingText, '#');
                SpannableString spannableString = new SpannableString(workingText);

                for (int i = 0; i < hashtagSpans.size(); i++) {
                    int[] span = hashtagSpans.get(i);
                    int hashTagStart = span[0];
                    int hashTagEnd = span[1];

                    if (hashTagStart >= 0 && hashTagEnd >= 0) {
                        spannableString.setSpan(new RelativeSizeSpan(0.0f), hashTagStart, hashTagStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new Hashtag(mContext, mHandler), hashTagStart, hashTagEnd, 0);
                    }
                }

                if (ellipsized && isMoreLinkEnabled) {
                    spannableString.setSpan(new RelativeSizeSpan(0.0f), workingText.length() - 9, workingText.length() - 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new Moretag(mContext, mHandler), workingText.length() - 4, workingText.length(), 0);
                }
                if (mHandler != null) {
                    setMovementMethod(LinkMovementMethod.getInstance());
                }
                setText(spannableString);
            } finally {
                programmaticChange = false;
            }
        } else {
            programmaticChange = true;
            try {
                ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(workingText, '#');
                SpannableString spannableString = new SpannableString(workingText);
                for (int i = 0; i < hashtagSpans.size(); i++) {
                    int[] span = hashtagSpans.get(i);
                    int hashTagStart = span[0];
                    int hashTagEnd = span[1];

                    if (hashTagStart >= 0 && hashTagEnd >= 0) {
                        spannableString.setSpan(new RelativeSizeSpan(0.0f), hashTagStart, hashTagStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new Hashtag(mContext, mHandler), hashTagStart, hashTagEnd, 0);
                    }
                }

                if (mHandler != null) {
                    setMovementMethod(LinkMovementMethod.getInstance());
                }
                setText(spannableString);
            } finally {
                programmaticChange = false;
            }
        }
        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
            for (EllipsizeListener listener : ellipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }
        }
    }

    private Layout createWorkingLayout(String workingText) {
        return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(),
                Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
    }

    @Override
    public void setEllipsize(TruncateAt where) {
        // Ellipsize settings are not respected
    }

    @Override
    public void scrollTo(int x, int y) {
        if (scrollEnabled)
            super.scrollTo(x, y);
    }
}