package com.sambhar.sambharappreport;

import android.app.Activity;
import android.app.Application;
import com.sambhar.sambharappreport.di.AppInjector;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig.Builder;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import javax.inject.Inject;

public class SambharApplication extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    public void onCreate() {
        super.onCreate();
        Twitter.initialize(new Builder(this).logger(new DefaultLogger(3)).twitterAuthConfig(new TwitterAuthConfig("4g52tLKR641TqfaeuRLNMsgVa", "PEXMKe9VQ5arw7mKpKfpZPj6hVvZo2g44O77WGTKRlp0Po0oSL")).debug(true).build());
        Sentry.init("https://557f935394ac4692bd384cf2e462294b@sentry.io/1395495", new AndroidSentryClientFactory(this));
        AppInjector.init(this);
    }

    public AndroidInjector<Activity> activityInjector() {
        return this.dispatchingAndroidInjector;
    }
}
