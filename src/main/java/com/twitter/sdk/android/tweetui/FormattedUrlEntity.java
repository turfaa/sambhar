package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.SymbolEntity;
import com.twitter.sdk.android.core.models.UrlEntity;

class FormattedUrlEntity {
    final String displayUrl;
    int end;
    final String expandedUrl;
    int start;
    final String url;

    FormattedUrlEntity(int i, int i2, String str, String str2, String str3) {
        this.start = i;
        this.end = i2;
        this.displayUrl = str;
        this.url = str2;
        this.expandedUrl = str3;
    }

    static FormattedUrlEntity createFormattedUrlEntity(UrlEntity urlEntity) {
        return new FormattedUrlEntity(urlEntity.getStart(), urlEntity.getEnd(), urlEntity.displayUrl, urlEntity.url, urlEntity.expandedUrl);
    }

    static FormattedUrlEntity createFormattedUrlEntity(HashtagEntity hashtagEntity) {
        String hashtagPermalink = TweetUtils.getHashtagPermalink(hashtagEntity.text);
        int start = hashtagEntity.getStart();
        int end = hashtagEntity.getEnd();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#");
        stringBuilder.append(hashtagEntity.text);
        return new FormattedUrlEntity(start, end, stringBuilder.toString(), hashtagPermalink, hashtagPermalink);
    }

    static FormattedUrlEntity createFormattedUrlEntity(MentionEntity mentionEntity) {
        String profilePermalink = TweetUtils.getProfilePermalink(mentionEntity.screenName);
        int start = mentionEntity.getStart();
        int end = mentionEntity.getEnd();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@");
        stringBuilder.append(mentionEntity.screenName);
        return new FormattedUrlEntity(start, end, stringBuilder.toString(), profilePermalink, profilePermalink);
    }

    static FormattedUrlEntity createFormattedUrlEntity(SymbolEntity symbolEntity) {
        String symbolPermalink = TweetUtils.getSymbolPermalink(symbolEntity.text);
        int start = symbolEntity.getStart();
        int end = symbolEntity.getEnd();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("$");
        stringBuilder.append(symbolEntity.text);
        return new FormattedUrlEntity(start, end, stringBuilder.toString(), symbolPermalink, symbolPermalink);
    }
}
