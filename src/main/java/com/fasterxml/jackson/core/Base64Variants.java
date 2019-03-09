package com.fasterxml.jackson.core;

import org.slf4j.Marker;

public final class Base64Variants {
    public static final Base64Variant MIME = new Base64Variant("MIME", STD_BASE64_ALPHABET, true, '=', 76);
    public static final Base64Variant MIME_NO_LINEFEEDS = new Base64Variant(MIME, "MIME-NO-LINEFEEDS", ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    public static final Base64Variant MODIFIED_FOR_URL;
    public static final Base64Variant PEM = new Base64Variant(MIME, "PEM", true, '=', 64);
    static final String STD_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    static {
        StringBuilder stringBuilder = new StringBuilder(STD_BASE64_ALPHABET);
        stringBuilder.setCharAt(stringBuilder.indexOf(Marker.ANY_NON_NULL_MARKER), '-');
        stringBuilder.setCharAt(stringBuilder.indexOf("/"), '_');
        MODIFIED_FOR_URL = new Base64Variant("MODIFIED-FOR-URL", stringBuilder.toString(), false, 0, (int) ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    public static Base64Variant getDefaultVariant() {
        return MIME_NO_LINEFEEDS;
    }

    public static Base64Variant valueOf(String str) throws IllegalArgumentException {
        if (MIME._name.equals(str)) {
            return MIME;
        }
        if (MIME_NO_LINEFEEDS._name.equals(str)) {
            return MIME_NO_LINEFEEDS;
        }
        if (PEM._name.equals(str)) {
            return PEM;
        }
        if (MODIFIED_FOR_URL._name.equals(str)) {
            return MODIFIED_FOR_URL;
        }
        if (str == null) {
            str = "<null>";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("'");
            stringBuilder.append(str);
            stringBuilder.append("'");
            str = stringBuilder.toString();
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("No Base64Variant with name ");
        stringBuilder2.append(str);
        throw new IllegalArgumentException(stringBuilder2.toString());
    }
}
