package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import com.twitter.sdk.android.core.IntentUtils;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.models.Tweet;

class ShareTweetAction implements OnClickListener {
    final Tweet tweet;
    final TweetScribeClient tweetScribeClient;
    final TweetUi tweetUi;

    ShareTweetAction(Tweet tweet, TweetUi tweetUi) {
        this(tweet, tweetUi, new TweetScribeClientImpl(tweetUi));
    }

    ShareTweetAction(Tweet tweet, TweetUi tweetUi, TweetScribeClient tweetScribeClient) {
        this.tweet = tweet;
        this.tweetUi = tweetUi;
        this.tweetScribeClient = tweetScribeClient;
    }

    public void onClick(View view) {
        onClick(view.getContext(), view.getResources());
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeShareAction() {
        this.tweetScribeClient.share(this.tweet);
    }

    /* Access modifiers changed, original: 0000 */
    public void onClick(Context context, Resources resources) {
        if (this.tweet != null && this.tweet.user != null) {
            scribeShareAction();
            launchShareIntent(Intent.createChooser(getShareIntent(getShareSubject(resources), getShareContent(resources)), resources.getString(R.string.tw__share_tweet)), context);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public String getShareContent(Resources resources) {
        return resources.getString(R.string.tw__share_content_format, new Object[]{this.tweet.user.screenName, Long.toString(this.tweet.id)});
    }

    /* Access modifiers changed, original: 0000 */
    public String getShareSubject(Resources resources) {
        return resources.getString(R.string.tw__share_subject_format, new Object[]{this.tweet.user.name, this.tweet.user.screenName});
    }

    /* Access modifiers changed, original: 0000 */
    public void launchShareIntent(Intent intent, Context context) {
        if (!IntentUtils.safeStartActivity(context, intent)) {
            Twitter.getLogger().e("TweetUi", "Activity cannot be found to handle share intent");
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Intent getShareIntent(String str, String str2) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.SUBJECT", str);
        intent.putExtra("android.intent.extra.TEXT", str2);
        intent.setType("text/plain");
        return intent;
    }
}
