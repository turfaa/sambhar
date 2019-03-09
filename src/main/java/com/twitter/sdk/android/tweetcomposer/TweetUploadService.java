package com.twitter.sdk.android.tweetcomposer;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TweetUploadService extends IntentService {
    static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    public static final String EXTRA_RETRY_INTENT = "EXTRA_RETRY_INTENT";
    public static final String EXTRA_TWEET_ID = "EXTRA_TWEET_ID";
    static final String EXTRA_TWEET_TEXT = "EXTRA_TWEET_TEXT";
    static final String EXTRA_USER_TOKEN = "EXTRA_USER_TOKEN";
    private static final int PLACEHOLDER_ID = -1;
    private static final String PLACEHOLDER_SCREEN_NAME = "";
    static final String TAG = "TweetUploadService";
    public static final String TWEET_COMPOSE_CANCEL = "com.twitter.sdk.android.tweetcomposer.TWEET_COMPOSE_CANCEL";
    public static final String UPLOAD_FAILURE = "com.twitter.sdk.android.tweetcomposer.UPLOAD_FAILURE";
    public static final String UPLOAD_SUCCESS = "com.twitter.sdk.android.tweetcomposer.UPLOAD_SUCCESS";
    DependencyProvider dependencyProvider;
    Intent intent;

    static class DependencyProvider {
        DependencyProvider() {
        }

        /* Access modifiers changed, original: 0000 */
        public TwitterApiClient getTwitterApiClient(TwitterSession twitterSession) {
            return TwitterCore.getInstance().getApiClient(twitterSession);
        }
    }

    public TweetUploadService() {
        this(new DependencyProvider());
    }

    TweetUploadService(DependencyProvider dependencyProvider) {
        super(TAG);
        this.dependencyProvider = dependencyProvider;
    }

    /* Access modifiers changed, original: protected */
    public void onHandleIntent(Intent intent) {
        TwitterAuthToken twitterAuthToken = (TwitterAuthToken) intent.getParcelableExtra(EXTRA_USER_TOKEN);
        this.intent = intent;
        uploadTweet(new TwitterSession(twitterAuthToken, -1, ""), intent.getStringExtra(EXTRA_TWEET_TEXT), (Uri) intent.getParcelableExtra(EXTRA_IMAGE_URI));
    }

    /* Access modifiers changed, original: 0000 */
    public void uploadTweet(final TwitterSession twitterSession, final String str, Uri uri) {
        if (uri != null) {
            uploadMedia(twitterSession, uri, new Callback<Media>() {
                public void success(Result<Media> result) {
                    TweetUploadService.this.uploadTweetWithMedia(twitterSession, str, ((Media) result.data).mediaIdString);
                }

                public void failure(TwitterException twitterException) {
                    TweetUploadService.this.fail(twitterException);
                }
            });
        } else {
            uploadTweetWithMedia(twitterSession, str, null);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void uploadTweetWithMedia(TwitterSession twitterSession, String str, String str2) {
        this.dependencyProvider.getTwitterApiClient(twitterSession).getStatusesService().update(str, null, null, null, null, null, null, Boolean.valueOf(true), str2).enqueue(new Callback<Tweet>() {
            public void success(Result<Tweet> result) {
                TweetUploadService.this.sendSuccessBroadcast(((Tweet) result.data).getId());
                TweetUploadService.this.stopSelf();
            }

            public void failure(TwitterException twitterException) {
                TweetUploadService.this.fail(twitterException);
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void uploadMedia(TwitterSession twitterSession, Uri uri, Callback<Media> callback) {
        TwitterApiClient twitterApiClient = this.dependencyProvider.getTwitterApiClient(twitterSession);
        String path = FileUtils.getPath(this, uri);
        if (path == null) {
            fail(new TwitterException("Uri file path resolved to null"));
            return;
        }
        File file = new File(path);
        twitterApiClient.getMediaService().upload(RequestBody.create(MediaType.parse(FileUtils.getMimeType(file)), file), null, null).enqueue(callback);
    }

    /* Access modifiers changed, original: 0000 */
    public void fail(TwitterException twitterException) {
        sendFailureBroadcast(this.intent);
        Twitter.getLogger().e(TAG, "Post Tweet failed", twitterException);
        stopSelf();
    }

    /* Access modifiers changed, original: 0000 */
    public void sendSuccessBroadcast(long j) {
        Intent intent = new Intent(UPLOAD_SUCCESS);
        intent.putExtra(EXTRA_TWEET_ID, j);
        intent.setPackage(getApplicationContext().getPackageName());
        sendBroadcast(intent);
    }

    /* Access modifiers changed, original: 0000 */
    public void sendFailureBroadcast(Intent intent) {
        Intent intent2 = new Intent(UPLOAD_FAILURE);
        intent2.putExtra(EXTRA_RETRY_INTENT, intent);
        intent2.setPackage(getApplicationContext().getPackageName());
        sendBroadcast(intent2);
    }
}
