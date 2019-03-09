package com.facebook.appevents.codeless.internal;

import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

public class SensitiveUserDataUtils {
    public static boolean isSensitiveUserData(View view) {
        boolean z = false;
        if (!(view instanceof TextView)) {
            return false;
        }
        TextView textView = (TextView) view;
        if (isPassword(textView) || isCreditCard(textView) || isPersonName(textView) || isPostalAddress(textView) || isPhoneNumber(textView) || isEmail(textView)) {
            z = true;
        }
        return z;
    }

    private static boolean isPassword(TextView textView) {
        if (textView.getInputType() == 128) {
            return true;
        }
        return textView.getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    private static boolean isEmail(TextView textView) {
        if (textView.getInputType() == 32) {
            return true;
        }
        String textOfView = ViewHierarchy.getTextOfView(textView);
        return (textOfView == null || textOfView.length() == 0) ? false : Patterns.EMAIL_ADDRESS.matcher(textOfView).matches();
    }

    private static boolean isPersonName(TextView textView) {
        return textView.getInputType() == 96;
    }

    private static boolean isPostalAddress(TextView textView) {
        return textView.getInputType() == 112;
    }

    private static boolean isPhoneNumber(TextView textView) {
        return textView.getInputType() == 3;
    }

    private static boolean isCreditCard(TextView textView) {
        String replaceAll = ViewHierarchy.getTextOfView(textView).replaceAll("\\s", "");
        int length = replaceAll.length();
        boolean z = false;
        if (length < 12 || length > 19) {
            return false;
        }
        int i = 0;
        int i2 = 0;
        for (length--; length >= 0; length--) {
            char charAt = replaceAll.charAt(length);
            if (charAt < '0' || charAt > '9') {
                return false;
            }
            int i3 = charAt - 48;
            if (i2 != 0) {
                i3 *= 2;
                if (i3 > 9) {
                    i3 = (i3 % 10) + 1;
                }
            }
            i += i3;
            i2 ^= 1;
        }
        if (i % 10 == 0) {
            z = true;
        }
        return z;
    }
}
