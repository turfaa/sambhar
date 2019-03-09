package com.twitter.sdk.android.tweetcomposer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.twitter.Regex;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;

public class ComposerActivity extends Activity {
    static final String EXTRA_HASHTAGS = "EXTRA_HASHTAGS";
    static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    static final String EXTRA_TEXT = "EXTRA_TEXT";
    static final String EXTRA_THEME = "EXTRA_THEME";
    static final String EXTRA_USER_TOKEN = "EXTRA_USER_TOKEN";
    private static final int PLACEHOLDER_ID = -1;
    private static final String PLACEHOLDER_SCREEN_NAME = "";
    private ComposerController composerController;

    public static class Builder {
        private final Context context;
        private String hashtags;
        private Uri imageUri;
        private String text;
        private int themeResId = R.style.ComposerLight;
        private TwitterAuthToken token;

        public Builder(Context context) {
            if (context != null) {
                this.context = context;
                return;
            }
            throw new IllegalArgumentException("Context must not be null");
        }

        public Builder session(TwitterSession twitterSession) {
            if (twitterSession != null) {
                TwitterAuthToken twitterAuthToken = (TwitterAuthToken) twitterSession.getAuthToken();
                if (twitterAuthToken != null) {
                    this.token = twitterAuthToken;
                    return this;
                }
                throw new IllegalArgumentException("TwitterSession token must not be null");
            }
            throw new IllegalArgumentException("TwitterSession must not be null");
        }

        public Builder image(Uri uri) {
            this.imageUri = uri;
            return this;
        }

        public Builder text(String str) {
            this.text = str;
            return this;
        }

        public Builder hashtags(String... strArr) {
            if (strArr == null) {
                return this;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (CharSequence charSequence : strArr) {
                if (Regex.VALID_HASHTAG.matcher(charSequence).find()) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
                    }
                    stringBuilder.append(charSequence);
                }
            }
            this.hashtags = stringBuilder.length() == 0 ? null : stringBuilder.toString();
            return this;
        }

        public Builder darkTheme() {
            this.themeResId = R.style.ComposerDark;
            return this;
        }

        public Intent createIntent() {
            if (this.token != null) {
                Intent intent = new Intent(this.context, ComposerActivity.class);
                intent.putExtra(ComposerActivity.EXTRA_USER_TOKEN, this.token);
                intent.putExtra(ComposerActivity.EXTRA_IMAGE_URI, this.imageUri);
                intent.putExtra(ComposerActivity.EXTRA_THEME, this.themeResId);
                intent.putExtra(ComposerActivity.EXTRA_TEXT, this.text);
                intent.putExtra(ComposerActivity.EXTRA_HASHTAGS, this.hashtags);
                return intent;
            }
            throw new IllegalStateException("Must set a TwitterSession");
        }
    }

    interface Finisher {
        void finish();
    }

    class FinisherImpl implements Finisher {
        FinisherImpl() {
        }

        public void finish() {
            ComposerActivity.this.finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        TwitterSession twitterSession = new TwitterSession((TwitterAuthToken) intent.getParcelableExtra(EXTRA_USER_TOKEN), -1, "");
        Uri uri = (Uri) intent.getParcelableExtra(EXTRA_IMAGE_URI);
        String stringExtra = intent.getStringExtra(EXTRA_TEXT);
        String stringExtra2 = intent.getStringExtra(EXTRA_HASHTAGS);
        setTheme(intent.getIntExtra(EXTRA_THEME, R.style.ComposerLight));
        setContentView(R.layout.tw__activity_composer);
        this.composerController = new ComposerController((ComposerView) findViewById(R.id.tw__composer_view), twitterSession, uri, stringExtra, stringExtra2, new FinisherImpl());
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.composerController.onClose();
    }
}
