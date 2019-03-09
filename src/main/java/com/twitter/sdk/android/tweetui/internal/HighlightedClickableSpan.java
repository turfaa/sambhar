package com.twitter.sdk.android.tweetui.internal;

import android.view.View;

public interface HighlightedClickableSpan {
    boolean isSelected();

    void onClick(View view);

    void select(boolean z);
}
