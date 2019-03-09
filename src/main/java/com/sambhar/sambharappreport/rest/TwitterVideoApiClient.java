package com.sambhar.sambharappreport.rest;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Media;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class TwitterVideoApiClient extends TwitterApiClient {

    public interface CustomService {
        @POST("https://upload.twitter.com/1.1/media/upload.json")
        @Multipart
        Call<Media> finalizeUpload(@Part("command") RequestBody requestBody, @Part("media_id") RequestBody requestBody2);

        @FormUrlEncoded
        @POST("https://upload.twitter.com/1.1/media/upload.json")
        Call<Media> finalizeUploadForm(@Field("command") String str, @Field("media_id") String str2, @Field("media_category") String str3);

        @GET("https://upload.twitter.com/1.1/media/upload.json")
        Call<MediaTwitterEntity> statusUpload(@Query("command") String str, @Query("media_id") String str2);

        @POST("https://upload.twitter.com/1.1/media/upload.json")
        @Multipart
        Call<String> uploadVideoAppend(@Part("command") RequestBody requestBody, @Part("media_id") RequestBody requestBody2, @Part("media") RequestBody requestBody3, @Part("segment_index") RequestBody requestBody4);

        @FormUrlEncoded
        @POST("https://upload.twitter.com/1.1/media/upload.json")
        Call<Media> uploadVideoInit(@Field("command") String str, @Field("media_type") String str2, @Field("total_bytes") String str3, @Field("media_category") String str4);
    }

    public TwitterVideoApiClient(TwitterSession twitterSession, OkHttpClient okHttpClient) {
        super(twitterSession, okHttpClient);
    }

    public CustomService getCustomService() {
        return (CustomService) getService(CustomService.class);
    }
}
