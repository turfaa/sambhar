package com.twitter.sdk.android.tweetui;

import android.graphics.Color;

final class ColorUtils {
    private ColorUtils() {
    }

    static int calculateOpacityTransform(double d, int i, int i2) {
        int red = Color.red(i2);
        int red2 = Color.red(i);
        int green = Color.green(i2);
        int green2 = Color.green(i);
        i2 = Color.blue(i2);
        i = Color.blue(i);
        double d2 = 1.0d - d;
        double d3 = (double) red;
        Double.isNaN(d3);
        d3 *= d2;
        double d4 = (double) red2;
        Double.isNaN(d4);
        red = (int) (d3 + (d4 * d));
        double d5 = (double) green;
        Double.isNaN(d5);
        d5 *= d2;
        d3 = (double) green2;
        Double.isNaN(d3);
        red2 = (int) (d5 + (d3 * d));
        double d6 = (double) i2;
        Double.isNaN(d6);
        d2 *= d6;
        double d7 = (double) i;
        Double.isNaN(d7);
        return Color.rgb(red, red2, (int) (d2 + (d * d7)));
    }

    static boolean isLightColor(int i) {
        int red = Color.red(i);
        int green = Color.green(i);
        i = Color.blue(i);
        double d = (double) red;
        Double.isNaN(d);
        d *= 0.21d;
        double d2 = (double) green;
        Double.isNaN(d2);
        d += d2 * 0.72d;
        d2 = (double) i;
        Double.isNaN(d2);
        return d + (d2 * 0.07d) > 128.0d;
    }
}
