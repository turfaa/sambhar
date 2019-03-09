package com.twitter.sdk.android.tweetui;

import android.os.Handler;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthException;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import java.util.List;

class TweetRepository {
    private static final int DEFAULT_CACHE_SIZE = 20;
    final LruCache<Long, FormattedTweetText> formatCache;
    private final Handler mainHandler;
    final LruCache<Long, Tweet> tweetCache;
    private final TwitterCore twitterCore;
    private final SessionManager<TwitterSession> userSessionManagers;

    class MultiTweetsCallback extends Callback<List<Tweet>> {
        final Callback<List<Tweet>> cb;
        final List<Long> tweetIds;

        MultiTweetsCallback(List<Long> list, Callback<List<Tweet>> callback) {
            this.cb = callback;
            this.tweetIds = list;
        }

        public void success(Result<List<Tweet>> result) {
            if (this.cb != null) {
                this.cb.success(new Result(Utils.orderTweets(this.tweetIds, (List) result.data), result.response));
            }
        }

        public void failure(TwitterException twitterException) {
            this.cb.failure(twitterException);
        }
    }

    class SingleTweetCallback extends Callback<Tweet> {
        final Callback<Tweet> cb;

        SingleTweetCallback(Callback<Tweet> callback) {
            this.cb = callback;
        }

        public void success(Result<Tweet> result) {
            Tweet tweet = (Tweet) result.data;
            TweetRepository.this.updateCache(tweet);
            if (this.cb != null) {
                this.cb.success(new Result(tweet, result.response));
            }
        }

        public void failure(TwitterException twitterException) {
            this.cb.failure(twitterException);
        }
    }

    TweetRepository(Handler handler, SessionManager<TwitterSession> sessionManager) {
        this(handler, sessionManager, TwitterCore.getInstance());
    }

    TweetRepository(Handler handler, SessionManager<TwitterSession> sessionManager, TwitterCore twitterCore) {
        this.twitterCore = twitterCore;
        this.mainHandler = handler;
        this.userSessionManagers = sessionManager;
        this.tweetCache = new LruCache(20);
        this.formatCache = new LruCache(20);
    }

    /* Access modifiers changed, original: 0000 */
    public FormattedTweetText formatTweetText(Tweet tweet) {
        if (tweet == null) {
            return null;
        }
        FormattedTweetText formattedTweetText = (FormattedTweetText) this.formatCache.get(Long.valueOf(tweet.id));
        if (formattedTweetText != null) {
            return formattedTweetText;
        }
        formattedTweetText = TweetTextUtils.formatTweetText(tweet);
        if (!(formattedTweetText == null || TextUtils.isEmpty(formattedTweetText.text))) {
            this.formatCache.put(Long.valueOf(tweet.id), formattedTweetText);
        }
        return formattedTweetText;
    }

    /* Access modifiers changed, original: 0000 */
    public void updateCache(Tweet tweet) {
        this.tweetCache.put(Long.valueOf(tweet.id), tweet);
    }

    private void deliverTweet(final Tweet tweet, final Callback<Tweet> callback) {
        if (callback != null) {
            this.mainHandler.post(new Runnable() {
                public void run() {
                    callback.success(new Result(tweet, null));
                }
            });
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void favorite(long j, Callback<Tweet> callback) {
        final long j2 = j;
        final Callback<Tweet> callback2 = callback;
        getUserSession(new LoggingCallback<TwitterSession>(callback, Twitter.getLogger()) {
            public void success(Result<TwitterSession> result) {
                TweetRepository.this.twitterCore.getApiClient((TwitterSession) result.data).getFavoriteService().create(Long.valueOf(j2), Boolean.valueOf(false)).enqueue(callback2);
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void unfavorite(long j, Callback<Tweet> callback) {
        final long j2 = j;
        final Callback<Tweet> callback2 = callback;
        getUserSession(new LoggingCallback<TwitterSession>(callback, Twitter.getLogger()) {
            public void success(Result<TwitterSession> result) {
                TweetRepository.this.twitterCore.getApiClient((TwitterSession) result.data).getFavoriteService().destroy(Long.valueOf(j2), Boolean.valueOf(false)).enqueue(callback2);
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void retweet(long j, Callback<Tweet> callback) {
        final long j2 = j;
        final Callback<Tweet> callback2 = callback;
        getUserSession(new LoggingCallback<TwitterSession>(callback, Twitter.getLogger()) {
            public void success(Result<TwitterSession> result) {
                TweetRepository.this.twitterCore.getApiClient((TwitterSession) result.data).getStatusesService().retweet(Long.valueOf(j2), Boolean.valueOf(false)).enqueue(callback2);
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void unretweet(long j, Callback<Tweet> callback) {
        final long j2 = j;
        final Callback<Tweet> callback2 = callback;
        getUserSession(new LoggingCallback<TwitterSession>(callback, Twitter.getLogger()) {
            public void success(Result<TwitterSession> result) {
                TweetRepository.this.twitterCore.getApiClient((TwitterSession) result.data).getStatusesService().unretweet(Long.valueOf(j2), Boolean.valueOf(false)).enqueue(callback2);
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void getUserSession(Callback<TwitterSession> callback) {
        TwitterSession twitterSession = (TwitterSession) this.userSessionManagers.getActiveSession();
        if (twitterSession == null) {
            callback.failure(new TwitterAuthException("User authorization required"));
        } else {
            callback.success(new Result(twitterSession, null));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void loadTweet(long j, Callback<Tweet> callback) {
        Tweet tweet = (Tweet) this.tweetCache.get(Long.valueOf(j));
        if (tweet != null) {
            deliverTweet(tweet, callback);
        } else {
            this.twitterCore.getApiClient().getStatusesService().show(Long.valueOf(j), null, null, null).enqueue(new SingleTweetCallback(callback));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void loadTweets(List<Long> list, Callback<List<Tweet>> callback) {
        this.twitterCore.getApiClient().getStatusesService().lookup(TextUtils.join(",", list), null, null, null).enqueue(new MultiTweetsCallback(list, callback));
    }
}
