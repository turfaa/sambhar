package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.ExecutorUtils;
import com.twitter.sdk.android.core.internal.IdManager;
import com.twitter.sdk.android.core.internal.scribe.ScribeEvent.Transform;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class DefaultScribeClient extends ScribeClient {
    private static final String DEBUG_BUILD = "debug";
    private static final String SCRIBE_PATH_TYPE = "sdk";
    private static final String SCRIBE_PATH_VERSION = "i";
    private static final String SCRIBE_URL = "https://syndication.twitter.com";
    private static volatile ScheduledExecutorService executor;
    private final String advertisingId;
    private final Context context;
    private final SessionManager<? extends Session<TwitterAuthToken>> sessionManager;

    public DefaultScribeClient(Context context, SessionManager<? extends Session<TwitterAuthToken>> sessionManager, GuestSessionProvider guestSessionProvider, IdManager idManager, ScribeConfig scribeConfig) {
        this(context, TwitterCore.getInstance().getAuthConfig(), sessionManager, guestSessionProvider, idManager, scribeConfig);
    }

    DefaultScribeClient(Context context, TwitterAuthConfig twitterAuthConfig, SessionManager<? extends Session<TwitterAuthToken>> sessionManager, GuestSessionProvider guestSessionProvider, IdManager idManager, ScribeConfig scribeConfig) {
        super(context, getExecutor(), scribeConfig, new Transform(getGson()), twitterAuthConfig, sessionManager, guestSessionProvider, idManager);
        this.context = context;
        this.sessionManager = sessionManager;
        this.advertisingId = idManager.getAdvertisingId();
    }

    public void scribe(EventNamespace... eventNamespaceArr) {
        for (EventNamespace scribe : eventNamespaceArr) {
            scribe(scribe, Collections.emptyList());
        }
    }

    public void scribe(EventNamespace eventNamespace, List<ScribeItem> list) {
        String str = "";
        EventNamespace eventNamespace2 = eventNamespace;
        scribe(ScribeEventFactory.newScribeEvent(eventNamespace2, str, System.currentTimeMillis(), getLanguage(), this.advertisingId, list));
    }

    public void scribe(ScribeEvent scribeEvent) {
        super.scribe(scribeEvent, getScribeSessionId(getActiveSession()));
    }

    public void scribe(EventNamespace eventNamespace, String str) {
        EventNamespace eventNamespace2 = eventNamespace;
        String str2 = str;
        scribe(ScribeEventFactory.newScribeEvent(eventNamespace2, str2, System.currentTimeMillis(), getLanguage(), this.advertisingId, Collections.emptyList()));
    }

    /* Access modifiers changed, original: 0000 */
    public Session getActiveSession() {
        return this.sessionManager.getActiveSession();
    }

    /* Access modifiers changed, original: 0000 */
    public long getScribeSessionId(Session session) {
        return session != null ? session.getId() : 0;
    }

    private String getLanguage() {
        return this.context.getResources().getConfiguration().locale.getLanguage();
    }

    private static Gson getGson() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    private static ScheduledExecutorService getExecutor() {
        if (executor == null) {
            synchronized (DefaultScribeClient.class) {
                if (executor == null) {
                    executor = ExecutorUtils.buildSingleThreadScheduledExecutorService("scribe");
                }
            }
        }
        return executor;
    }

    public static ScribeConfig getScribeConfig(String str, String str2) {
        return new ScribeConfig(isEnabled(), getScribeUrl(SCRIBE_URL, ""), SCRIBE_PATH_VERSION, "sdk", "", getUserAgent(str, str2), 100, ScribeConfig.DEFAULT_SEND_INTERVAL_SECONDS);
    }

    private static boolean isEnabled() {
        return "release".equals("debug") ^ 1;
    }

    static String getUserAgent(String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("TwitterKit/");
        stringBuilder.append("3.0");
        stringBuilder.append(" (Android ");
        stringBuilder.append(VERSION.SDK_INT);
        stringBuilder.append(") ");
        stringBuilder.append(str);
        stringBuilder.append("/");
        stringBuilder.append(str2);
        return stringBuilder.toString();
    }

    static String getScribeUrl(String str, String str2) {
        return !TextUtils.isEmpty(str2) ? str2 : str;
    }
}
