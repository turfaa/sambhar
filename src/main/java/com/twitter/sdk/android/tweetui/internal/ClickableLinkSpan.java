package com.twitter.sdk.android.tweetui.internal;

import android.text.TextPaint;
import android.text.style.ClickableSpan;

public abstract class ClickableLinkSpan extends ClickableSpan implements HighlightedClickableSpan {
    private final boolean colored;
    public final int linkColor;
    private boolean selected;
    private final int selectedColor;
    private final boolean underlined;

    public ClickableLinkSpan(int i, int i2, boolean z) {
        this(i, i2, true, z);
    }

    ClickableLinkSpan(int i, int i2, boolean z, boolean z2) {
        this.selectedColor = i;
        this.linkColor = i2;
        this.colored = z;
        this.underlined = z2;
    }

    public void updateDrawState(TextPaint textPaint) {
        if (this.colored) {
            textPaint.setColor(this.linkColor);
        } else {
            textPaint.setColor(textPaint.linkColor);
        }
        if (this.selected) {
            textPaint.bgColor = this.selectedColor;
        } else {
            textPaint.bgColor = 0;
        }
        if (this.underlined) {
            textPaint.setUnderlineText(true);
        }
    }

    public void select(boolean z) {
        this.selected = z;
    }

    public boolean isSelected() {
        return this.selected;
    }
}
