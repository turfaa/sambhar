package com.twitter.sdk.android.tweetui.internal;

import android.annotation.SuppressLint;
import android.text.Layout;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class SpanClickHandler {
    private HighlightedClickableSpan highlightedClickableSpan;
    private Layout layout;
    private float left;
    private float top;
    private final View view;

    /* renamed from: com.twitter.sdk.android.tweetui.internal.SpanClickHandler$1 */
    static class AnonymousClass1 implements OnTouchListener {
        final /* synthetic */ SpanClickHandler val$helper;

        AnonymousClass1(SpanClickHandler spanClickHandler) {
            this.val$helper = spanClickHandler;
        }

        @SuppressLint({"ClickableViewAccessibility"})
        public boolean onTouch(View view, MotionEvent motionEvent) {
            TextView textView = (TextView) view;
            Layout layout = textView.getLayout();
            if (layout == null) {
                return false;
            }
            this.val$helper.layout = layout;
            this.val$helper.left = (float) (textView.getTotalPaddingLeft() + textView.getScrollX());
            this.val$helper.top = (float) (textView.getTotalPaddingTop() + textView.getScrollY());
            return this.val$helper.handleTouchEvent(motionEvent);
        }
    }

    public static void enableClicksOnSpans(TextView textView) {
        textView.setOnTouchListener(new AnonymousClass1(new SpanClickHandler(textView, null)));
    }

    public SpanClickHandler(View view, Layout layout) {
        this.view = view;
        this.layout = layout;
    }

    public boolean handleTouchEvent(MotionEvent motionEvent) {
        CharSequence text = this.layout.getText();
        Spanned spanned = text instanceof Spanned ? (Spanned) text : null;
        if (spanned == null) {
            return false;
        }
        int action = motionEvent.getAction() & 255;
        int x = (int) (motionEvent.getX() - this.left);
        int y = (int) (motionEvent.getY() - this.top);
        if (x < 0 || x >= this.layout.getWidth() || y < 0 || y >= this.layout.getHeight()) {
            deselectSpan();
            return false;
        }
        y = this.layout.getLineForVertical(y);
        float f = (float) x;
        if (f < this.layout.getLineLeft(y) || f > this.layout.getLineRight(y)) {
            deselectSpan();
            return false;
        }
        if (action == 0) {
            y = this.layout.getOffsetForHorizontal(y, f);
            HighlightedClickableSpan[] highlightedClickableSpanArr = (HighlightedClickableSpan[]) spanned.getSpans(y, y, HighlightedClickableSpan.class);
            if (highlightedClickableSpanArr.length > 0) {
                selectSpan(highlightedClickableSpanArr[0]);
                return true;
            }
        } else if (action == 1) {
            HighlightedClickableSpan highlightedClickableSpan = this.highlightedClickableSpan;
            if (highlightedClickableSpan != null) {
                highlightedClickableSpan.onClick(this.view);
                deselectSpan();
                return true;
            }
        }
        return false;
    }

    private void selectSpan(HighlightedClickableSpan highlightedClickableSpan) {
        highlightedClickableSpan.select(true);
        this.highlightedClickableSpan = highlightedClickableSpan;
        invalidate();
    }

    private void deselectSpan() {
        HighlightedClickableSpan highlightedClickableSpan = this.highlightedClickableSpan;
        if (highlightedClickableSpan != null && highlightedClickableSpan.isSelected()) {
            highlightedClickableSpan.select(false);
            this.highlightedClickableSpan = null;
            invalidate();
        }
    }

    private void invalidate() {
        this.view.invalidate((int) this.left, (int) this.top, ((int) this.left) + this.layout.getWidth(), ((int) this.top) + this.layout.getHeight());
    }
}
