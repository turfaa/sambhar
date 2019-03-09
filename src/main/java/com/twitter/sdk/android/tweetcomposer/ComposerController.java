package com.twitter.sdk.android.tweetcomposer;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.twitter.Validator;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

class ComposerController {
    final ComposerView composerView;
    final DependencyProvider dependencyProvider;
    final Finisher finisher;
    final Uri imageUri;
    final TwitterSession session;

    interface ComposerCallbacks {
        void onCloseClick();

        void onTextChanged(String str);

        void onTweetPost(String str);
    }

    static class DependencyProvider {
        final Validator tweetValidator = new Validator();

        DependencyProvider() {
        }

        /* Access modifiers changed, original: 0000 */
        public TwitterApiClient getApiClient(TwitterSession twitterSession) {
            return TwitterCore.getInstance().getApiClient(twitterSession);
        }

        /* Access modifiers changed, original: 0000 */
        public Validator getTweetValidator() {
            return this.tweetValidator;
        }

        /* Access modifiers changed, original: 0000 */
        public ComposerScribeClient getScribeClient() {
            return new ComposerScribeClientImpl(TweetComposer.getInstance().getScribeClient());
        }
    }

    class ComposerCallbacksImpl implements ComposerCallbacks {
        ComposerCallbacksImpl() {
        }

        public void onTextChanged(String str) {
            int tweetTextLength = ComposerController.this.tweetTextLength(str);
            ComposerController.this.composerView.setCharCount(ComposerController.remainingCharCount(tweetTextLength));
            if (ComposerController.isTweetTextOverflow(tweetTextLength)) {
                ComposerController.this.composerView.setCharCountTextStyle(R.style.tw__ComposerCharCountOverflow);
            } else {
                ComposerController.this.composerView.setCharCountTextStyle(R.style.tw__ComposerCharCount);
            }
            ComposerController.this.composerView.postTweetEnabled(ComposerController.isPostEnabled(tweetTextLength));
        }

        public void onTweetPost(String str) {
            ComposerController.this.dependencyProvider.getScribeClient().click("tweet");
            Intent intent = new Intent(ComposerController.this.composerView.getContext(), TweetUploadService.class);
            intent.putExtra("EXTRA_USER_TOKEN", (Parcelable) ComposerController.this.session.getAuthToken());
            intent.putExtra("EXTRA_TWEET_TEXT", str);
            intent.putExtra("EXTRA_IMAGE_URI", ComposerController.this.imageUri);
            ComposerController.this.composerView.getContext().startService(intent);
            ComposerController.this.finisher.finish();
        }

        public void onCloseClick() {
            ComposerController.this.onClose();
        }
    }

    static boolean isPostEnabled(int i) {
        return i > 0 && i <= 140;
    }

    static boolean isTweetTextOverflow(int i) {
        return i > 140;
    }

    static int remainingCharCount(int i) {
        return 140 - i;
    }

    ComposerController(ComposerView composerView, TwitterSession twitterSession, Uri uri, String str, String str2, Finisher finisher) {
        this(composerView, twitterSession, uri, str, str2, finisher, new DependencyProvider());
    }

    ComposerController(ComposerView composerView, TwitterSession twitterSession, Uri uri, String str, String str2, Finisher finisher, DependencyProvider dependencyProvider) {
        this.composerView = composerView;
        this.session = twitterSession;
        this.imageUri = uri;
        this.finisher = finisher;
        this.dependencyProvider = dependencyProvider;
        composerView.setCallbacks(new ComposerCallbacksImpl());
        composerView.setTweetText(generateText(str, str2));
        setProfilePhoto();
        setImageView(uri);
        dependencyProvider.getScribeClient().impression();
    }

    /* Access modifiers changed, original: 0000 */
    public String generateText(String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(str)) {
            stringBuilder.append(str);
        }
        if (!TextUtils.isEmpty(str2)) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            }
            stringBuilder.append(str2);
        }
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public void setProfilePhoto() {
        this.dependencyProvider.getApiClient(this.session).getAccountService().verifyCredentials(Boolean.valueOf(false), Boolean.valueOf(true), Boolean.valueOf(false)).enqueue(new Callback<User>() {
            public void success(Result<User> result) {
                ComposerController.this.composerView.setProfilePhotoView((User) result.data);
            }

            public void failure(TwitterException twitterException) {
                ComposerController.this.composerView.setProfilePhotoView(null);
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void setImageView(Uri uri) {
        if (uri != null) {
            this.composerView.setImageView(uri);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onClose() {
        this.dependencyProvider.getScribeClient().click("cancel");
        sendCancelBroadcast();
        this.finisher.finish();
    }

    /* Access modifiers changed, original: 0000 */
    public int tweetTextLength(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        return this.dependencyProvider.getTweetValidator().getTweetLength(str);
    }

    /* Access modifiers changed, original: 0000 */
    public void sendCancelBroadcast() {
        Intent intent = new Intent(TweetUploadService.TWEET_COMPOSE_CANCEL);
        intent.setPackage(this.composerView.getContext().getPackageName());
        this.composerView.getContext().sendBroadcast(intent);
    }
}
