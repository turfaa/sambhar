package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.text.TextUtils;
import com.bumptech.glide.load.Key;
import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.internal.CommonUtils;
import com.twitter.sdk.android.core.internal.IdManager;
import com.twitter.sdk.android.core.internal.network.GuestAuthInterceptor;
import com.twitter.sdk.android.core.internal.network.OAuth1aInterceptor;
import com.twitter.sdk.android.core.internal.network.OkHttpClientHelper;
import com.twitter.sdk.android.core.internal.scribe.QueueFile.ElementReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

class ScribeFilesSender implements FilesSender {
    private static final byte[] COMMA = new byte[]{(byte) 44};
    private static final byte[] END_JSON_ARRAY = new byte[]{(byte) 93};
    private static final String SEND_FILE_FAILURE_ERROR = "Failed sending files";
    private static final byte[] START_JSON_ARRAY = new byte[]{(byte) 91};
    private final TwitterAuthConfig authConfig;
    private final Context context;
    private final ExecutorService executorService;
    private final GuestSessionProvider guestSessionProvider;
    private final IdManager idManager;
    private final long ownerId;
    private final ScribeConfig scribeConfig;
    private final AtomicReference<ScribeService> scribeService = new AtomicReference();
    private final SessionManager<? extends Session<TwitterAuthToken>> sessionManager;

    interface ScribeService {
        @FormUrlEncoded
        @POST("/{version}/jot/{type}")
        @Headers({"Content-Type: application/x-www-form-urlencoded;charset=UTF-8"})
        Call<ResponseBody> upload(@Path("version") String str, @Path("type") String str2, @Field("log[]") String str3);

        @FormUrlEncoded
        @POST("/scribe/{sequence}")
        @Headers({"Content-Type: application/x-www-form-urlencoded;charset=UTF-8"})
        Call<ResponseBody> uploadSequence(@Path("sequence") String str, @Field("log[]") String str2);
    }

    static class ConfigRequestInterceptor implements Interceptor {
        private static final String CLIENT_UUID_HEADER = "X-Client-UUID";
        private static final String POLLING_HEADER = "X-Twitter-Polling";
        private static final String POLLING_HEADER_VALUE = "true";
        private static final String USER_AGENT_HEADER = "User-Agent";
        private final IdManager idManager;
        private final ScribeConfig scribeConfig;

        ConfigRequestInterceptor(ScribeConfig scribeConfig, IdManager idManager) {
            this.scribeConfig = scribeConfig;
            this.idManager = idManager;
        }

        public Response intercept(Chain chain) throws IOException {
            Builder newBuilder = chain.request().newBuilder();
            if (!TextUtils.isEmpty(this.scribeConfig.userAgent)) {
                newBuilder.header(USER_AGENT_HEADER, this.scribeConfig.userAgent);
            }
            if (!TextUtils.isEmpty(this.idManager.getDeviceUUID())) {
                newBuilder.header(CLIENT_UUID_HEADER, this.idManager.getDeviceUUID());
            }
            newBuilder.header(POLLING_HEADER, "true");
            return chain.proceed(newBuilder.build());
        }
    }

    public ScribeFilesSender(Context context, ScribeConfig scribeConfig, long j, TwitterAuthConfig twitterAuthConfig, SessionManager<? extends Session<TwitterAuthToken>> sessionManager, GuestSessionProvider guestSessionProvider, ExecutorService executorService, IdManager idManager) {
        this.context = context;
        this.scribeConfig = scribeConfig;
        this.ownerId = j;
        this.authConfig = twitterAuthConfig;
        this.sessionManager = sessionManager;
        this.guestSessionProvider = guestSessionProvider;
        this.executorService = executorService;
        this.idManager = idManager;
    }

    public boolean send(List<File> list) {
        if (hasApiAdapter()) {
            try {
                String scribeEventsAsJsonArrayString = getScribeEventsAsJsonArrayString(list);
                CommonUtils.logControlled(this.context, scribeEventsAsJsonArrayString);
                retrofit2.Response upload = upload(scribeEventsAsJsonArrayString);
                if (upload.code() == Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                    return true;
                }
                CommonUtils.logControlledError(this.context, SEND_FILE_FAILURE_ERROR, null);
                if (upload.code() == 500 || upload.code() == 400) {
                    return true;
                }
            } catch (Exception e) {
                CommonUtils.logControlledError(this.context, SEND_FILE_FAILURE_ERROR, e);
            }
        } else {
            CommonUtils.logControlled(this.context, "Cannot attempt upload at this time");
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public String getScribeEventsAsJsonArrayString(List<File> list) throws IOException {
        Throwable th;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        final boolean[] zArr = new boolean[1];
        byteArrayOutputStream.write(START_JSON_ARRAY);
        for (File queueFile : list) {
            Closeable queueFile2;
            try {
                queueFile2 = new QueueFile(queueFile);
                try {
                    queueFile2.forEach(new ElementReader() {
                        public void read(InputStream inputStream, int i) throws IOException {
                            byte[] bArr = new byte[i];
                            inputStream.read(bArr);
                            if (zArr[0]) {
                                byteArrayOutputStream.write(ScribeFilesSender.COMMA);
                            } else {
                                zArr[0] = true;
                            }
                            byteArrayOutputStream.write(bArr);
                        }
                    });
                    CommonUtils.closeQuietly(queueFile2);
                } catch (Throwable th2) {
                    th = th2;
                    CommonUtils.closeQuietly(queueFile2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                queueFile2 = null;
                CommonUtils.closeQuietly(queueFile2);
                throw th;
            }
        }
        byteArrayOutputStream.write(END_JSON_ARRAY);
        return byteArrayOutputStream.toString(Key.STRING_CHARSET_NAME);
    }

    private boolean hasApiAdapter() {
        return getScribeService() != null;
    }

    /* Access modifiers changed, original: 0000 */
    public void setScribeService(ScribeService scribeService) {
        this.scribeService.set(scribeService);
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized ScribeService getScribeService() {
        if (this.scribeService.get() == null) {
            OkHttpClient build;
            Session session = getSession(this.ownerId);
            if (isValidSession(session)) {
                build = new OkHttpClient.Builder().certificatePinner(OkHttpClientHelper.getCertificatePinner()).addInterceptor(new ConfigRequestInterceptor(this.scribeConfig, this.idManager)).addInterceptor(new OAuth1aInterceptor(session, this.authConfig)).build();
            } else {
                build = new OkHttpClient.Builder().certificatePinner(OkHttpClientHelper.getCertificatePinner()).addInterceptor(new ConfigRequestInterceptor(this.scribeConfig, this.idManager)).addInterceptor(new GuestAuthInterceptor(this.guestSessionProvider)).build();
            }
            this.scribeService.compareAndSet(null, new Retrofit.Builder().baseUrl(this.scribeConfig.baseUrl).client(build).build().create(ScribeService.class));
        }
        return (ScribeService) this.scribeService.get();
    }

    private Session getSession(long j) {
        return this.sessionManager.getSession(j);
    }

    private boolean isValidSession(Session session) {
        return (session == null || session.getAuthToken() == null) ? false : true;
    }

    /* Access modifiers changed, original: 0000 */
    public retrofit2.Response<ResponseBody> upload(String str) throws IOException {
        ScribeService scribeService = getScribeService();
        if (TextUtils.isEmpty(this.scribeConfig.sequence)) {
            return scribeService.upload(this.scribeConfig.pathVersion, this.scribeConfig.pathType, str).execute();
        }
        return scribeService.uploadSequence(this.scribeConfig.sequence, str).execute();
    }
}
