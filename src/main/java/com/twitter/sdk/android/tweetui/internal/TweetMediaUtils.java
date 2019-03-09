package com.twitter.sdk.android.tweetui.internal;

import android.os.Build.VERSION;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.core.models.VideoInfo.Variant;
import java.util.ArrayList;
import java.util.List;

public final class TweetMediaUtils {
    private static final String CONTENT_TYPE_HLS = "application/x-mpegURL";
    private static final String CONTENT_TYPE_MP4 = "video/mp4";
    public static final String GIF_TYPE = "animated_gif";
    private static final int LOOP_VIDEO_IN_MILLIS = 6500;
    public static final String PHOTO_TYPE = "photo";
    public static final String VIDEO_TYPE = "video";

    private TweetMediaUtils() {
    }

    public static MediaEntity getPhotoEntity(Tweet tweet) {
        List allMediaEntities = getAllMediaEntities(tweet);
        for (int size = allMediaEntities.size() - 1; size >= 0; size--) {
            MediaEntity mediaEntity = (MediaEntity) allMediaEntities.get(size);
            if (mediaEntity.type != null && isPhotoType(mediaEntity)) {
                return mediaEntity;
            }
        }
        return null;
    }

    public static List<MediaEntity> getPhotoEntities(Tweet tweet) {
        ArrayList arrayList = new ArrayList();
        TweetEntities tweetEntities = tweet.extendedEntities;
        if (tweetEntities == null || tweetEntities.media == null || tweetEntities.media.size() <= 0) {
            return arrayList;
        }
        for (int i = 0; i <= tweetEntities.media.size() - 1; i++) {
            MediaEntity mediaEntity = (MediaEntity) tweetEntities.media.get(i);
            if (mediaEntity.type != null && isPhotoType(mediaEntity)) {
                arrayList.add(mediaEntity);
            }
        }
        return arrayList;
    }

    public static boolean hasPhoto(Tweet tweet) {
        return getPhotoEntity(tweet) != null;
    }

    public static MediaEntity getVideoEntity(Tweet tweet) {
        for (MediaEntity mediaEntity : getAllMediaEntities(tweet)) {
            if (mediaEntity.type != null && isVideoType(mediaEntity)) {
                return mediaEntity;
            }
        }
        return null;
    }

    public static boolean hasSupportedVideo(Tweet tweet) {
        MediaEntity videoEntity = getVideoEntity(tweet);
        return (videoEntity == null || getSupportedVariant(videoEntity) == null) ? false : true;
    }

    static boolean isPhotoType(MediaEntity mediaEntity) {
        return "photo".equals(mediaEntity.type);
    }

    static boolean isVideoType(MediaEntity mediaEntity) {
        return "video".equals(mediaEntity.type) || "animated_gif".equals(mediaEntity.type);
    }

    public static Variant getSupportedVariant(MediaEntity mediaEntity) {
        for (Variant variant : mediaEntity.videoInfo.variants) {
            if (isVariantSupported(variant)) {
                return variant;
            }
        }
        return null;
    }

    public static boolean isLooping(MediaEntity mediaEntity) {
        return "animated_gif".equals(mediaEntity.type) || ("video".endsWith(mediaEntity.type) && mediaEntity.videoInfo.durationMillis < 6500);
    }

    public static boolean showVideoControls(MediaEntity mediaEntity) {
        return "animated_gif".equals(mediaEntity.type) ^ 1;
    }

    static boolean isVariantSupported(Variant variant) {
        if ((VERSION.SDK_INT < 21 || !CONTENT_TYPE_HLS.equals(variant.contentType)) && !CONTENT_TYPE_MP4.equals(variant.contentType)) {
            return false;
        }
        return true;
    }

    static List<MediaEntity> getAllMediaEntities(Tweet tweet) {
        ArrayList arrayList = new ArrayList();
        if (!(tweet.entities == null || tweet.entities.media == null)) {
            arrayList.addAll(tweet.entities.media);
        }
        if (!(tweet.extendedEntities == null || tweet.extendedEntities.media == null)) {
            arrayList.addAll(tweet.extendedEntities.media);
        }
        return arrayList;
    }
}
