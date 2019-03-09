package com.twitter.sdk.android.tweetui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.scribe.DefaultScribeClient;
import com.twitter.sdk.android.core.internal.scribe.EventNamespace;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import java.util.List;

public class TweetUi {
    private static final String KIT_SCRIBE_NAME = "TweetUi";
    static final String LOGTAG = "TweetUi";
    @SuppressLint({"StaticFieldLeak"})
    static volatile TweetUi instance;
    Context context = Twitter.getInstance().getContext(getIdentifier());
    GuestSessionProvider guestSessionProvider;
    private Picasso imageLoader;
    DefaultScribeClient scribeClient;
    SessionManager<TwitterSession> sessionManager;
    private TweetRepository tweetRepository;

    public String getIdentifier() {
        return "com.twitter.sdk.android:tweet-ui";
    }

    public String getVersion() {
        return "3.1.1.9";
    }

    public static TweetUi getInstance() {
        if (instance == null) {
            synchronized (TweetUi.class) {
                if (instance == null) {
                    instance = new TweetUi();
                }
            }
        }
        return instance;
    }

    TweetUi() {
        TwitterCore instance = TwitterCore.getInstance();
        this.sessionManager = instance.getSessionManager();
        this.guestSessionProvider = instance.getGuestSessionProvider();
        this.tweetRepository = new TweetRepository(new Handler(Looper.getMainLooper()), instance.getSessionManager());
        this.imageLoader = Picasso.with(Twitter.getInstance().getContext(getIdentifier()));
        setUpScribeClient();
    }

    private void setUpScribeClient() {
        this.scribeClient = new DefaultScribeClient(this.context, this.sessionManager, this.guestSessionProvider, Twitter.getInstance().getIdManager(), DefaultScribeClient.getScribeConfig("TweetUi", getVersion()));
    }

    /* Access modifiers changed, original: varargs */
    public void scribe(EventNamespace... eventNamespaceArr) {
        if (this.scribeClient != null) {
            for (EventNamespace eventNamespace : eventNamespaceArr) {
                this.scribeClient.scribe(eventNamespace);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void scribe(EventNamespace eventNamespace, List<ScribeItem> list) {
        if (this.scribeClient != null) {
            this.scribeClient.scribe(eventNamespace, (List) list);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public TweetRepository getTweetRepository() {
        return this.tweetRepository;
    }

    /* Access modifiers changed, original: 0000 */
    public void setTweetRepository(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    public Picasso getImageLoader() {
        return this.imageLoader;
    }

    /* Access modifiers changed, original: 0000 */
    public void setImageLoader(Picasso picasso) {
        this.imageLoader = picasso;
    }
}
