package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ToggleImageButton extends ImageButton {
    private static final int[] STATE_TOGGLED_ON = new int[]{R.attr.state_toggled_on};
    String contentDescriptionOff;
    String contentDescriptionOn;
    boolean isToggledOn;
    final boolean toggleOnClick;

    public ToggleImageButton(Context context) {
        this(context, null);
    }

    public ToggleImageButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0048  */
    public ToggleImageButton(android.content.Context r4, android.util.AttributeSet r5, int r6) {
        /*
        r3 = this;
        r3.<init>(r4, r5, r6);
        r0 = 0;
        r4 = r4.getTheme();	 Catch:{ all -> 0x0044 }
        r1 = com.twitter.sdk.android.tweetui.R.styleable.ToggleImageButton;	 Catch:{ all -> 0x0044 }
        r2 = 0;
        r4 = r4.obtainStyledAttributes(r5, r1, r6, r2);	 Catch:{ all -> 0x0044 }
        r5 = com.twitter.sdk.android.tweetui.R.styleable.ToggleImageButton_contentDescriptionOn;	 Catch:{ all -> 0x0042 }
        r5 = r4.getString(r5);	 Catch:{ all -> 0x0042 }
        r6 = com.twitter.sdk.android.tweetui.R.styleable.ToggleImageButton_contentDescriptionOff;	 Catch:{ all -> 0x0042 }
        r6 = r4.getString(r6);	 Catch:{ all -> 0x0042 }
        if (r5 != 0) goto L_0x0023;
    L_0x001d:
        r5 = r3.getContentDescription();	 Catch:{ all -> 0x0042 }
        r5 = (java.lang.String) r5;	 Catch:{ all -> 0x0042 }
    L_0x0023:
        r3.contentDescriptionOn = r5;	 Catch:{ all -> 0x0042 }
        if (r6 != 0) goto L_0x002e;
    L_0x0027:
        r5 = r3.getContentDescription();	 Catch:{ all -> 0x0042 }
        r6 = r5;
        r6 = (java.lang.String) r6;	 Catch:{ all -> 0x0042 }
    L_0x002e:
        r3.contentDescriptionOff = r6;	 Catch:{ all -> 0x0042 }
        r5 = com.twitter.sdk.android.tweetui.R.styleable.ToggleImageButton_toggleOnClick;	 Catch:{ all -> 0x0042 }
        r6 = 1;
        r5 = r4.getBoolean(r5, r6);	 Catch:{ all -> 0x0042 }
        r3.toggleOnClick = r5;	 Catch:{ all -> 0x0042 }
        r3.setToggledOn(r2);	 Catch:{ all -> 0x0042 }
        if (r4 == 0) goto L_0x0041;
    L_0x003e:
        r4.recycle();
    L_0x0041:
        return;
    L_0x0042:
        r5 = move-exception;
        goto L_0x0046;
    L_0x0044:
        r5 = move-exception;
        r4 = r0;
    L_0x0046:
        if (r4 == 0) goto L_0x004b;
    L_0x0048:
        r4.recycle();
    L_0x004b:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.tweetui.ToggleImageButton.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 2);
        if (this.isToggledOn) {
            mergeDrawableStates(onCreateDrawableState, STATE_TOGGLED_ON);
        }
        return onCreateDrawableState;
    }

    public boolean performClick() {
        if (this.toggleOnClick) {
            toggle();
        }
        return super.performClick();
    }

    public void setToggledOn(boolean z) {
        this.isToggledOn = z;
        setContentDescription(z ? this.contentDescriptionOn : this.contentDescriptionOff);
        refreshDrawableState();
    }

    public void toggle() {
        setToggledOn(this.isToggledOn ^ 1);
    }

    public boolean isToggledOn() {
        return this.isToggledOn;
    }
}
