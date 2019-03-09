package com.twitter.sdk.android.tweetcomposer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.network.UrlUtils;
import com.twitter.sdk.android.core.internal.scribe.DefaultScribeClient;
import java.net.URL;

public class TweetComposer {
    private static final String KIT_SCRIBE_NAME = "TweetComposer";
    private static final String MIME_TYPE_JPEG = "image/jpeg";
    private static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
    private static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
    private static final String WEB_INTENT = "https://twitter.com/intent/tweet?text=%s&url=%s";
    @SuppressLint({"StaticFieldLeak"})
    static volatile TweetComposer instance;
    Context context = Twitter.getInstance().getContext(getIdentifier());
    GuestSessionProvider guestSessionProvider = TwitterCore.getInstance().getGuestSessionProvider();
    ScribeClient scribeClient = new ScribeClientImpl(null);
    SessionManager<TwitterSession> sessionManager = TwitterCore.getInstance().getSessionManager();

    public static class Builder {
        private final Context context;
        private Uri imageUri;
        private String text;
        private URL url;

        public Builder(Context context) {
            if (context != null) {
                this.context = context;
                return;
            }
            throw new IllegalArgumentException("Context must not be null.");
        }

        public Builder text(String str) {
            if (str == null) {
                throw new IllegalArgumentException("text must not be null.");
            } else if (this.text == null) {
                this.text = str;
                return this;
            } else {
                throw new IllegalStateException("text already set.");
            }
        }

        public Builder url(URL url) {
            if (url == null) {
                throw new IllegalArgumentException("url must not be null.");
            } else if (this.url == null) {
                this.url = url;
                return this;
            } else {
                throw new IllegalStateException("url already set.");
            }
        }

        public Builder image(Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("imageUri must not be null.");
            } else if (this.imageUri == null) {
                this.imageUri = uri;
                return this;
            } else {
                throw new IllegalStateException("imageUri already set.");
            }
        }

        public Intent createIntent() {
            Intent createTwitterIntent = createTwitterIntent();
            return createTwitterIntent == null ? createWebIntent() : createTwitterIntent;
        }

        /* Access modifiers changed, original: 0000 */
        public Intent createTwitterIntent() {
            Intent intent = new Intent("android.intent.action.SEND");
            StringBuilder stringBuilder = new StringBuilder();
            if (!TextUtils.isEmpty(this.text)) {
                stringBuilder.append(this.text);
            }
            if (this.url != null) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(' ');
                }
                stringBuilder.append(this.url.toString());
            }
            intent.putExtra("android.intent.extra.TEXT", stringBuilder.toString());
            intent.setType(TweetComposer.MIME_TYPE_PLAIN_TEXT);
            if (this.imageUri != null) {
                intent.putExtra("android.intent.extra.STREAM", this.imageUri);
                intent.setType(TweetComposer.MIME_TYPE_JPEG);
            }
            for (ResolveInfo resolveInfo : this.context.getPackageManager().queryIntentActivities(intent, 65536)) {
                if (resolveInfo.activityInfo.packageName.startsWith(TweetComposer.TWITTER_PACKAGE_NAME)) {
                    intent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                    return intent;
                }
            }
            return null;
        }

        /* Access modifiers changed, original: 0000 */
        public Intent createWebIntent() {
            String url = this.url == null ? "" : this.url.toString();
            return new Intent("android.intent.action.VIEW", Uri.parse(String.format(TweetComposer.WEB_INTENT, new Object[]{UrlUtils.urlEncode(this.text), UrlUtils.urlEncode(url)})));
        }

        public void show() {
            this.context.startActivity(createIntent());
        }
    }

    public String getIdentifier() {
        return "com.twitter.sdk.android:tweet-composer";
    }

    public String getVersion() {
        return "3.1.1.9";
    }

    public static TweetComposer getInstance() {
        if (instance == null) {
            synchronized (TweetComposer.class) {
                if (instance == null) {
                    instance = new TweetComposer();
                }
            }
        }
        return instance;
    }

    TweetComposer() {
        setUpScribeClient();
    }

    private void setUpScribeClient() {
        this.scribeClient = new ScribeClientImpl(new DefaultScribeClient(this.context, this.sessionManager, this.guestSessionProvider, Twitter.getInstance().getIdManager(), DefaultScribeClient.getScribeConfig(KIT_SCRIBE_NAME, getVersion())));
    }

    /* Access modifiers changed, original: 0000 */
    public ScribeClient getScribeClient() {
        return this.scribeClient;
    }
}
