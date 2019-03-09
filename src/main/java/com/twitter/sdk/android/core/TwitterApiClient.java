package com.twitter.sdk.android.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.internal.network.OkHttpClientHelper;
import com.twitter.sdk.android.core.models.BindingValues;
import com.twitter.sdk.android.core.models.BindingValuesAdapter;
import com.twitter.sdk.android.core.models.SafeListAdapter;
import com.twitter.sdk.android.core.models.SafeMapAdapter;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.CollectionService;
import com.twitter.sdk.android.core.services.ConfigurationService;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.ListService;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.StatusesService;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

public class TwitterApiClient {
    final Retrofit retrofit;
    final ConcurrentHashMap<Class, Object> services;

    public TwitterApiClient() {
        this(OkHttpClientHelper.getOkHttpClient(TwitterCore.getInstance().getGuestSessionProvider()), new TwitterApi());
    }

    public TwitterApiClient(OkHttpClient okHttpClient) {
        this(OkHttpClientHelper.getCustomOkHttpClient(okHttpClient, TwitterCore.getInstance().getGuestSessionProvider()), new TwitterApi());
    }

    public TwitterApiClient(TwitterSession twitterSession) {
        this(OkHttpClientHelper.getOkHttpClient(twitterSession, TwitterCore.getInstance().getAuthConfig()), new TwitterApi());
    }

    public TwitterApiClient(TwitterSession twitterSession, OkHttpClient okHttpClient) {
        this(OkHttpClientHelper.getCustomOkHttpClient(okHttpClient, twitterSession, TwitterCore.getInstance().getAuthConfig()), new TwitterApi());
    }

    TwitterApiClient(OkHttpClient okHttpClient, TwitterApi twitterApi) {
        this.services = buildConcurrentMap();
        this.retrofit = buildRetrofit(okHttpClient, twitterApi);
    }

    private Retrofit buildRetrofit(OkHttpClient okHttpClient, TwitterApi twitterApi) {
        return new Builder().client(okHttpClient).baseUrl(twitterApi.getBaseHostUrl()).addConverterFactory(GsonConverterFactory.create(buildGson())).build();
    }

    private Gson buildGson() {
        return new GsonBuilder().registerTypeAdapterFactory(new SafeListAdapter()).registerTypeAdapterFactory(new SafeMapAdapter()).registerTypeAdapter(BindingValues.class, new BindingValuesAdapter()).create();
    }

    private ConcurrentHashMap buildConcurrentMap() {
        return new ConcurrentHashMap();
    }

    public AccountService getAccountService() {
        return (AccountService) getService(AccountService.class);
    }

    public FavoriteService getFavoriteService() {
        return (FavoriteService) getService(FavoriteService.class);
    }

    public StatusesService getStatusesService() {
        return (StatusesService) getService(StatusesService.class);
    }

    public SearchService getSearchService() {
        return (SearchService) getService(SearchService.class);
    }

    public ListService getListService() {
        return (ListService) getService(ListService.class);
    }

    public CollectionService getCollectionService() {
        return (CollectionService) getService(CollectionService.class);
    }

    public ConfigurationService getConfigurationService() {
        return (ConfigurationService) getService(ConfigurationService.class);
    }

    public MediaService getMediaService() {
        return (MediaService) getService(MediaService.class);
    }

    /* Access modifiers changed, original: protected */
    public <T> T getService(Class<T> cls) {
        if (!this.services.contains(cls)) {
            this.services.putIfAbsent(cls, this.retrofit.create(cls));
        }
        return this.services.get(cls);
    }
}
