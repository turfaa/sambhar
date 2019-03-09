package com.twitter;

import com.twitter.Extractor.Entity;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class Validator {
    public static final int MAX_TWEET_LENGTH = 140;
    private Extractor extractor = new Extractor();
    protected int shortUrlLength = 23;
    protected int shortUrlLengthHttps = 23;

    public int getTweetLength(String str) {
        str = Normalizer.normalize(str, Form.NFC);
        int codePointCount = str.codePointCount(0, str.length());
        for (Entity entity : this.extractor.extractURLsWithIndices(str)) {
            codePointCount = (codePointCount + (entity.start - entity.end)) + (entity.value.toLowerCase().startsWith("https://") ? this.shortUrlLengthHttps : this.shortUrlLength);
        }
        return codePointCount;
    }

    public boolean isValidTweet(String str) {
        boolean z = false;
        if (str == null || str.length() == 0) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (c == 65534 || c == 65279 || c == 65535 || (c >= 8234 && c <= 8238)) {
                return false;
            }
        }
        if (getTweetLength(str) <= 140) {
            z = true;
        }
        return z;
    }

    public int getShortUrlLength() {
        return this.shortUrlLength;
    }

    public void setShortUrlLength(int i) {
        this.shortUrlLength = i;
    }

    public int getShortUrlLengthHttps() {
        return this.shortUrlLengthHttps;
    }

    public void setShortUrlLengthHttps(int i) {
        this.shortUrlLengthHttps = i;
    }
}
