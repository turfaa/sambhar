package com.twitter;

import java.text.StringCharacterIterator;
import java.util.List;

public class HitHighlighter {
    public static final String DEFAULT_HIGHLIGHT_TAG = "em";
    protected String highlightTag = "em";

    public String highlight(String str, List<List<Integer>> list) {
        if (list == null || list.isEmpty()) {
            return str;
        }
        StringBuilder stringBuilder = new StringBuilder(str.length());
        StringCharacterIterator stringCharacterIterator = new StringCharacterIterator(str);
        Object obj = null;
        int i = 0;
        Object obj2 = 1;
        for (char first = stringCharacterIterator.first(); first != 65535; first = stringCharacterIterator.next()) {
            for (List list2 : list) {
                if (((Integer) list2.get(0)).intValue() == i) {
                    stringBuilder.append(tag(false));
                    obj = 1;
                } else if (((Integer) list2.get(1)).intValue() == i) {
                    stringBuilder.append(tag(true));
                    obj = null;
                }
            }
            if (first == '<') {
                obj2 = null;
            } else if (first == '>' && obj2 == null) {
                obj2 = 1;
            }
            if (obj2 != null) {
                i++;
            }
            stringBuilder.append(first);
        }
        if (obj != null) {
            stringBuilder.append(tag(true));
        }
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public String tag(boolean z) {
        StringBuilder stringBuilder = new StringBuilder(this.highlightTag.length() + 3);
        stringBuilder.append("<");
        if (z) {
            stringBuilder.append("/");
        }
        stringBuilder.append(this.highlightTag);
        stringBuilder.append(">");
        return stringBuilder.toString();
    }

    public String getHighlightTag() {
        return this.highlightTag;
    }

    public void setHighlightTag(String str) {
        this.highlightTag = str;
    }
}
