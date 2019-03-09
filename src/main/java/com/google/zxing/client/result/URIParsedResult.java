package com.google.zxing.client.result;

import java.util.regex.Pattern;

public final class URIParsedResult extends ParsedResult {
    private static final Pattern USER_IN_HOST = Pattern.compile(":/*([^/@]+)@[^/]+");
    private final String title;
    private final String uri;

    public URIParsedResult(String str, String str2) {
        super(ParsedResultType.URI);
        this.uri = massageURI(str);
        this.title = str2;
    }

    public String getURI() {
        return this.uri;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isPossiblyMaliciousURI() {
        return USER_IN_HOST.matcher(this.uri).find();
    }

    public String getDisplayResult() {
        StringBuilder stringBuilder = new StringBuilder(30);
        ParsedResult.maybeAppend(this.title, stringBuilder);
        ParsedResult.maybeAppend(this.uri, stringBuilder);
        return stringBuilder.toString();
    }

    private static String massageURI(String str) {
        str = str.trim();
        int indexOf = str.indexOf(58);
        if (indexOf >= 0 && !isColonFollowedByPortNumber(str, indexOf)) {
            return str;
        }
        StringBuilder stringBuilder = new StringBuilder("http://");
        stringBuilder.append(str);
        return stringBuilder.toString();
    }

    private static boolean isColonFollowedByPortNumber(String str, int i) {
        i++;
        int indexOf = str.indexOf(47, i);
        if (indexOf < 0) {
            indexOf = str.length();
        }
        return ResultParser.isSubstringOfDigits(str, i, indexOf - i);
    }
}
