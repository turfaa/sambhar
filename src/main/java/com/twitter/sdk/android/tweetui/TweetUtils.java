package com.twitter.sdk.android.tweetui;

import android.net.Uri;
import android.text.TextUtils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.List;
import java.util.Locale;

public final class TweetUtils {
    private static final String HASHTAG_URL = "https://twitter.com/hashtag/%s?ref_src=twsrc%%5Etwitterkit";
    static final String LOAD_TWEET_DEBUG = "loadTweet failure for Tweet Id %d.";
    private static final String PROFILE_URL = "https://twitter.com/%s?ref_src=twsrc%%5Etwitterkit";
    private static final String SYMBOL_URL = "https://twitter.com/search?q=%%24%s&ref_src=twsrc%%5Etwitterkit";
    private static final String TWEET_URL = "https://twitter.com/%s/status/%d?ref_src=twsrc%%5Etwitterkit";
    private static final String TWITTER_KIT_REF = "ref_src=twsrc%%5Etwitterkit";
    private static final String TWITTER_URL = "https://twitter.com/";
    private static final String UNKNOWN_SCREEN_NAME = "twitter_unknown";

    private TweetUtils() {
    }

    public static void loadTweet(long j, final Callback<Tweet> callback) {
        TweetUi.getInstance().getTweetRepository().loadTweet(j, new LoggingCallback<Tweet>(Twitter.getLogger(), callback) {
            public void success(Result<Tweet> result) {
                if (callback != null) {
                    callback.success(result);
                }
            }
        });
    }

    public static void loadTweets(List<Long> list, final Callback<List<Tweet>> callback) {
        TweetUi.getInstance().getTweetRepository().loadTweets(list, new LoggingCallback<List<Tweet>>(Twitter.getLogger(), callback) {
            public void success(Result<List<Tweet>> result) {
                if (callback != null) {
                    callback.success(result);
                }
            }
        });
    }

    static boolean isTweetResolvable(Tweet tweet) {
        return (tweet == null || tweet.id <= 0 || tweet.user == null || TextUtils.isEmpty(tweet.user.screenName)) ? false : true;
    }

    static Tweet getDisplayTweet(Tweet tweet) {
        return (tweet == null || tweet.retweetedStatus == null) ? tweet : tweet.retweetedStatus;
    }

    static boolean showQuoteTweet(Tweet tweet) {
        return tweet.quotedStatus != null && tweet.card == null && (tweet.entities == null || tweet.entities.media == null || tweet.entities.media.isEmpty());
    }

    static Uri getPermalink(String str, long j) {
        if (j <= 0) {
            return null;
        }
        if (TextUtils.isEmpty(str)) {
            str = String.format(Locale.US, TWEET_URL, new Object[]{UNKNOWN_SCREEN_NAME, Long.valueOf(j)});
        } else {
            str = String.format(Locale.US, TWEET_URL, new Object[]{str, Long.valueOf(j)});
        }
        return Uri.parse(str);
    }

    static String getProfilePermalink(String str) {
        if (TextUtils.isEmpty(str)) {
            return String.format(Locale.US, PROFILE_URL, new Object[]{UNKNOWN_SCREEN_NAME});
        }
        return String.format(Locale.US, PROFILE_URL, new Object[]{str});
    }

    static String getHashtagPermalink(String str) {
        return String.format(Locale.US, HASHTAG_URL, new Object[]{str});
    }

    static String getSymbolPermalink(String str) {
        return String.format(Locale.US, SYMBOL_URL, new Object[]{str});
    }
}
