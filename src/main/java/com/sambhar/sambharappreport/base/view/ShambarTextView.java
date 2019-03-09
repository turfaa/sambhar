package com.sambhar.sambharappreport.base.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.sambhar.sambharappreport.R;

public class ShambarTextView extends AppCompatTextView {
    String customFont;

    public ShambarTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        style(context, attributeSet);
    }

    public ShambarTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        style(context, attributeSet);
    }

    private void style(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CustomFontTextView);
        int integer = obtainStyledAttributes.getInteger(0, 0);
        int i = R.string.Roboto_Regular;
        switch (integer) {
            case 1:
                i = R.string.Roboto_Bold;
                break;
            case 2:
                i = R.string.Roboto_Light;
                break;
        }
        this.customFont = getResources().getString(i);
        AssetManager assets = context.getAssets();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.customFont);
        stringBuilder.append(".ttf");
        setTypeface(Typeface.createFromAsset(assets, stringBuilder.toString()));
        obtainStyledAttributes.recycle();
    }
}
