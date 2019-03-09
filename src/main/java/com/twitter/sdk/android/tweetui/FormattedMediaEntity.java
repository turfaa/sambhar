package com.twitter.sdk.android.tweetui;

import com.twitter.sdk.android.core.models.MediaEntity;

class FormattedMediaEntity extends FormattedUrlEntity {
    final String mediaUrlHttps;
    final String type;

    FormattedMediaEntity(MediaEntity mediaEntity) {
        super(mediaEntity.getStart(), mediaEntity.getEnd(), mediaEntity.displayUrl, mediaEntity.url, mediaEntity.expandedUrl);
        this.type = mediaEntity.type;
        this.mediaUrlHttps = mediaEntity.mediaUrlHttps;
    }
}
