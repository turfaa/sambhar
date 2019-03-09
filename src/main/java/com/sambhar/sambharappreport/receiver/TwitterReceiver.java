package com.sambhar.sambharappreport.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

public class TwitterReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (!TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction()) && !TweetUploadService.UPLOAD_FAILURE.equals(intent.getAction())) {
            TweetUploadService.TWEET_COMPOSE_CANCEL.equals(intent.getAction());
        }
    }
}
