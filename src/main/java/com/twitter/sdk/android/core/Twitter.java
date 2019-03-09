package com.twitter.sdk.android.core;

import android.annotation.SuppressLint;
import android.content.Context;
import com.twitter.sdk.android.core.TwitterConfig.Builder;
import com.twitter.sdk.android.core.internal.ActivityLifecycleManager;
import com.twitter.sdk.android.core.internal.CommonUtils;
import com.twitter.sdk.android.core.internal.ExecutorUtils;
import com.twitter.sdk.android.core.internal.IdManager;
import java.io.File;
import java.util.concurrent.ExecutorService;

public class Twitter {
    private static final String CONSUMER_KEY = "com.twitter.sdk.android.CONSUMER_KEY";
    private static final String CONSUMER_SECRET = "com.twitter.sdk.android.CONSUMER_SECRET";
    static final Logger DEFAULT_LOGGER = new DefaultLogger();
    private static final String NOT_INITIALIZED_MESSAGE = "Must initialize Twitter before using getInstance()";
    public static final String TAG = "Twitter";
    @SuppressLint({"StaticFieldLeak"})
    static volatile Twitter instance;
    private final Context context;
    private final boolean debug;
    private final ExecutorService executorService;
    private final IdManager idManager = new IdManager(this.context);
    private final ActivityLifecycleManager lifecycleManager = new ActivityLifecycleManager(this.context);
    private final Logger logger;
    private final TwitterAuthConfig twitterAuthConfig;

    private Twitter(TwitterConfig twitterConfig) {
        this.context = twitterConfig.context;
        if (twitterConfig.twitterAuthConfig == null) {
            this.twitterAuthConfig = new TwitterAuthConfig(CommonUtils.getStringResourceValue(this.context, CONSUMER_KEY, ""), CommonUtils.getStringResourceValue(this.context, CONSUMER_SECRET, ""));
        } else {
            this.twitterAuthConfig = twitterConfig.twitterAuthConfig;
        }
        if (twitterConfig.executorService == null) {
            this.executorService = ExecutorUtils.buildThreadPoolExecutorService("twitter-worker");
        } else {
            this.executorService = twitterConfig.executorService;
        }
        if (twitterConfig.logger == null) {
            this.logger = DEFAULT_LOGGER;
        } else {
            this.logger = twitterConfig.logger;
        }
        if (twitterConfig.debug == null) {
            this.debug = false;
        } else {
            this.debug = twitterConfig.debug.booleanValue();
        }
    }

    public static void initialize(Context context) {
        createTwitter(new Builder(context).build());
    }

    public static void initialize(TwitterConfig twitterConfig) {
        createTwitter(twitterConfig);
    }

    static synchronized Twitter createTwitter(TwitterConfig twitterConfig) {
        synchronized (Twitter.class) {
            Twitter twitter;
            if (instance == null) {
                instance = new Twitter(twitterConfig);
                twitter = instance;
                return twitter;
            }
            twitter = instance;
            return twitter;
        }
    }

    static void checkInitialized() {
        if (instance == null) {
            throw new IllegalStateException(NOT_INITIALIZED_MESSAGE);
        }
    }

    public static Twitter getInstance() {
        checkInitialized();
        return instance;
    }

    public Context getContext(String str) {
        Context context = this.context;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".TwitterKit");
        stringBuilder.append(File.separator);
        stringBuilder.append(str);
        return new TwitterContext(context, str, stringBuilder.toString());
    }

    public IdManager getIdManager() {
        return this.idManager;
    }

    public TwitterAuthConfig getTwitterAuthConfig() {
        return this.twitterAuthConfig;
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public ActivityLifecycleManager getActivityLifecycleManager() {
        return this.lifecycleManager;
    }

    public static boolean isDebug() {
        if (instance == null) {
            return false;
        }
        return instance.debug;
    }

    public static Logger getLogger() {
        if (instance == null) {
            return DEFAULT_LOGGER;
        }
        return instance.logger;
    }
}
