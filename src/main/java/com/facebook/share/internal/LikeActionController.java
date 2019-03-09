package com.facebook.share.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.internal.AppCall;
import com.facebook.internal.BundleJSONConverter;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;
import com.facebook.internal.FileLruCache;
import com.facebook.internal.FileLruCache.Limits;
import com.facebook.internal.FragmentWrapper;
import com.facebook.internal.Logger;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.PlatformServiceClient.CompletedListener;
import com.facebook.internal.Utility;
import com.facebook.internal.WorkQueue;
import com.facebook.share.internal.LikeContent.Builder;
import com.facebook.share.widget.LikeView.ObjectType;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class LikeActionController {
    @Deprecated
    public static final String ACTION_LIKE_ACTION_CONTROLLER_DID_ERROR = "com.facebook.sdk.LikeActionController.DID_ERROR";
    @Deprecated
    public static final String ACTION_LIKE_ACTION_CONTROLLER_DID_RESET = "com.facebook.sdk.LikeActionController.DID_RESET";
    @Deprecated
    public static final String ACTION_LIKE_ACTION_CONTROLLER_UPDATED = "com.facebook.sdk.LikeActionController.UPDATED";
    @Deprecated
    public static final String ACTION_OBJECT_ID_KEY = "com.facebook.sdk.LikeActionController.OBJECT_ID";
    private static final int ERROR_CODE_OBJECT_ALREADY_LIKED = 3501;
    @Deprecated
    public static final String ERROR_INVALID_OBJECT_ID = "Invalid Object Id";
    @Deprecated
    public static final String ERROR_PUBLISH_ERROR = "Unable to publish the like/unlike action";
    private static final String JSON_BOOL_IS_OBJECT_LIKED_KEY = "is_object_liked";
    private static final String JSON_BUNDLE_FACEBOOK_DIALOG_ANALYTICS_BUNDLE = "facebook_dialog_analytics_bundle";
    private static final String JSON_INT_OBJECT_TYPE_KEY = "object_type";
    private static final String JSON_INT_VERSION_KEY = "com.facebook.share.internal.LikeActionController.version";
    private static final String JSON_STRING_LIKE_COUNT_WITHOUT_LIKE_KEY = "like_count_string_without_like";
    private static final String JSON_STRING_LIKE_COUNT_WITH_LIKE_KEY = "like_count_string_with_like";
    private static final String JSON_STRING_OBJECT_ID_KEY = "object_id";
    private static final String JSON_STRING_SOCIAL_SENTENCE_WITHOUT_LIKE_KEY = "social_sentence_without_like";
    private static final String JSON_STRING_SOCIAL_SENTENCE_WITH_LIKE_KEY = "social_sentence_with_like";
    private static final String JSON_STRING_UNLIKE_TOKEN_KEY = "unlike_token";
    private static final String LIKE_ACTION_CONTROLLER_STORE = "com.facebook.LikeActionController.CONTROLLER_STORE_KEY";
    private static final String LIKE_ACTION_CONTROLLER_STORE_OBJECT_SUFFIX_KEY = "OBJECT_SUFFIX";
    private static final String LIKE_ACTION_CONTROLLER_STORE_PENDING_OBJECT_ID_KEY = "PENDING_CONTROLLER_KEY";
    private static final int LIKE_ACTION_CONTROLLER_VERSION = 3;
    private static final String LIKE_DIALOG_RESPONSE_LIKE_COUNT_STRING_KEY = "like_count_string";
    private static final String LIKE_DIALOG_RESPONSE_OBJECT_IS_LIKED_KEY = "object_is_liked";
    private static final String LIKE_DIALOG_RESPONSE_SOCIAL_SENTENCE_KEY = "social_sentence";
    private static final String LIKE_DIALOG_RESPONSE_UNLIKE_TOKEN_KEY = "unlike_token";
    private static final int MAX_CACHE_SIZE = 128;
    private static final int MAX_OBJECT_SUFFIX = 1000;
    private static final String TAG = "LikeActionController";
    private static AccessTokenTracker accessTokenTracker;
    private static final ConcurrentHashMap<String, LikeActionController> cache = new ConcurrentHashMap();
    private static FileLruCache controllerDiskCache;
    private static WorkQueue diskIOWorkQueue = new WorkQueue(1);
    private static Handler handler;
    private static boolean isInitialized;
    private static WorkQueue mruCacheWorkQueue = new WorkQueue(1);
    private static String objectIdForPendingController;
    private static volatile int objectSuffix;
    private AppEventsLogger appEventsLogger;
    private Bundle facebookDialogAnalyticsBundle;
    private boolean isObjectLiked;
    private boolean isObjectLikedOnServer;
    private boolean isPendingLikeOrUnlike;
    private String likeCountStringWithLike;
    private String likeCountStringWithoutLike;
    private String objectId;
    private boolean objectIsPage;
    private ObjectType objectType;
    private String socialSentenceWithLike;
    private String socialSentenceWithoutLike;
    private String unlikeToken;
    private String verifiedObjectId;

    /* renamed from: com.facebook.share.internal.LikeActionController$12 */
    static /* synthetic */ class AnonymousClass12 {
        static final /* synthetic */ int[] $SwitchMap$com$facebook$share$widget$LikeView$ObjectType = new int[ObjectType.values().length];

        static {
            try {
                $SwitchMap$com$facebook$share$widget$LikeView$ObjectType[ObjectType.PAGE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    /* renamed from: com.facebook.share.internal.LikeActionController$2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ LikeActionController val$controllerToRefresh;

        AnonymousClass2(LikeActionController likeActionController) {
            this.val$controllerToRefresh = likeActionController;
        }

        public void run() {
            this.val$controllerToRefresh.refreshStatusAsync();
        }
    }

    private static class CreateLikeActionControllerWorkItem implements Runnable {
        private CreationCallback callback;
        private String objectId;
        private ObjectType objectType;

        CreateLikeActionControllerWorkItem(String str, ObjectType objectType, CreationCallback creationCallback) {
            this.objectId = str;
            this.objectType = objectType;
            this.callback = creationCallback;
        }

        public void run() {
            LikeActionController.createControllerForObjectIdAndType(this.objectId, this.objectType, this.callback);
        }
    }

    @Deprecated
    public interface CreationCallback {
        void onComplete(LikeActionController likeActionController, FacebookException facebookException);
    }

    private static class MRUCacheWorkItem implements Runnable {
        private static ArrayList<String> mruCachedItems = new ArrayList();
        private String cacheItem;
        private boolean shouldTrim;

        MRUCacheWorkItem(String str, boolean z) {
            this.cacheItem = str;
            this.shouldTrim = z;
        }

        public void run() {
            if (this.cacheItem != null) {
                mruCachedItems.remove(this.cacheItem);
                mruCachedItems.add(0, this.cacheItem);
            }
            if (this.shouldTrim && mruCachedItems.size() >= 128) {
                while (64 < mruCachedItems.size()) {
                    LikeActionController.cache.remove((String) mruCachedItems.remove(mruCachedItems.size() - 1));
                }
            }
        }
    }

    private interface RequestCompletionCallback {
        void onComplete();
    }

    private interface RequestWrapper {
        void addToBatch(GraphRequestBatch graphRequestBatch);

        FacebookRequestError getError();
    }

    private static class SerializeToDiskWorkItem implements Runnable {
        private String cacheKey;
        private String controllerJson;

        SerializeToDiskWorkItem(String str, String str2) {
            this.cacheKey = str;
            this.controllerJson = str2;
        }

        public void run() {
            LikeActionController.serializeToDiskSynchronously(this.cacheKey, this.controllerJson);
        }
    }

    private abstract class AbstractRequestWrapper implements RequestWrapper {
        protected FacebookRequestError error;
        protected String objectId;
        protected ObjectType objectType;
        private GraphRequest request;

        public abstract void processSuccess(GraphResponse graphResponse);

        protected AbstractRequestWrapper(String str, ObjectType objectType) {
            this.objectId = str;
            this.objectType = objectType;
        }

        public void addToBatch(GraphRequestBatch graphRequestBatch) {
            graphRequestBatch.add(this.request);
        }

        public FacebookRequestError getError() {
            return this.error;
        }

        /* Access modifiers changed, original: protected */
        public void setRequest(GraphRequest graphRequest) {
            this.request = graphRequest;
            graphRequest.setVersion(FacebookSdk.getGraphApiVersion());
            graphRequest.setCallback(new Callback() {
                public void onCompleted(GraphResponse graphResponse) {
                    AbstractRequestWrapper.this.error = graphResponse.getError();
                    if (AbstractRequestWrapper.this.error != null) {
                        AbstractRequestWrapper.this.processError(AbstractRequestWrapper.this.error);
                    } else {
                        AbstractRequestWrapper.this.processSuccess(graphResponse);
                    }
                }
            });
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error running request for object '%s' with type '%s' : %s", this.objectId, this.objectType, facebookRequestError);
        }
    }

    private interface LikeRequestWrapper extends RequestWrapper {
        String getUnlikeToken();

        boolean isObjectLiked();
    }

    private class GetEngagementRequestWrapper extends AbstractRequestWrapper {
        String likeCountStringWithLike = LikeActionController.this.likeCountStringWithLike;
        String likeCountStringWithoutLike = LikeActionController.this.likeCountStringWithoutLike;
        String socialSentenceStringWithLike = LikeActionController.this.socialSentenceWithLike;
        String socialSentenceStringWithoutLike = LikeActionController.this.socialSentenceWithoutLike;

        GetEngagementRequestWrapper(String str, ObjectType objectType) {
            super(str, objectType);
            Bundle bundle = new Bundle();
            bundle.putString(GraphRequest.FIELDS_PARAM, "engagement.fields(count_string_with_like,count_string_without_like,social_sentence_with_like,social_sentence_without_like)");
            bundle.putString("locale", Locale.getDefault().toString());
            setRequest(new GraphRequest(AccessToken.getCurrentAccessToken(), str, bundle, HttpMethod.GET));
        }

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
            JSONObject tryGetJSONObjectFromResponse = Utility.tryGetJSONObjectFromResponse(graphResponse.getJSONObject(), "engagement");
            if (tryGetJSONObjectFromResponse != null) {
                this.likeCountStringWithLike = tryGetJSONObjectFromResponse.optString("count_string_with_like", this.likeCountStringWithLike);
                this.likeCountStringWithoutLike = tryGetJSONObjectFromResponse.optString("count_string_without_like", this.likeCountStringWithoutLike);
                this.socialSentenceStringWithLike = tryGetJSONObjectFromResponse.optString(LikeActionController.JSON_STRING_SOCIAL_SENTENCE_WITH_LIKE_KEY, this.socialSentenceStringWithLike);
                this.socialSentenceStringWithoutLike = tryGetJSONObjectFromResponse.optString(LikeActionController.JSON_STRING_SOCIAL_SENTENCE_WITHOUT_LIKE_KEY, this.socialSentenceStringWithoutLike);
            }
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error fetching engagement for object '%s' with type '%s' : %s", this.objectId, this.objectType, facebookRequestError);
            LikeActionController.this.logAppEventForError("get_engagement", facebookRequestError);
        }
    }

    private class GetOGObjectIdRequestWrapper extends AbstractRequestWrapper {
        String verifiedObjectId;

        GetOGObjectIdRequestWrapper(String str, ObjectType objectType) {
            super(str, objectType);
            Bundle bundle = new Bundle();
            bundle.putString(GraphRequest.FIELDS_PARAM, "og_object.fields(id)");
            bundle.putString("ids", str);
            setRequest(new GraphRequest(AccessToken.getCurrentAccessToken(), "", bundle, HttpMethod.GET));
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            if (facebookRequestError.getErrorMessage().contains("og_object")) {
                this.error = null;
                return;
            }
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error getting the FB id for object '%s' with type '%s' : %s", this.objectId, this.objectType, facebookRequestError);
        }

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
            JSONObject tryGetJSONObjectFromResponse = Utility.tryGetJSONObjectFromResponse(graphResponse.getJSONObject(), this.objectId);
            if (tryGetJSONObjectFromResponse != null) {
                tryGetJSONObjectFromResponse = tryGetJSONObjectFromResponse.optJSONObject("og_object");
                if (tryGetJSONObjectFromResponse != null) {
                    this.verifiedObjectId = tryGetJSONObjectFromResponse.optString(ShareConstants.WEB_DIALOG_PARAM_ID);
                }
            }
        }
    }

    private class GetOGObjectLikesRequestWrapper extends AbstractRequestWrapper implements LikeRequestWrapper {
        private final String objectId;
        private boolean objectIsLiked = LikeActionController.this.isObjectLiked;
        private final ObjectType objectType;
        private String unlikeToken;

        GetOGObjectLikesRequestWrapper(String str, ObjectType objectType) {
            super(str, objectType);
            this.objectId = str;
            this.objectType = objectType;
            Bundle bundle = new Bundle();
            bundle.putString(GraphRequest.FIELDS_PARAM, "id,application");
            bundle.putString("object", this.objectId);
            setRequest(new GraphRequest(AccessToken.getCurrentAccessToken(), "me/og.likes", bundle, HttpMethod.GET));
        }

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
            JSONArray tryGetJSONArrayFromResponse = Utility.tryGetJSONArrayFromResponse(graphResponse.getJSONObject(), ShareConstants.WEB_DIALOG_PARAM_DATA);
            if (tryGetJSONArrayFromResponse != null) {
                for (int i = 0; i < tryGetJSONArrayFromResponse.length(); i++) {
                    JSONObject optJSONObject = tryGetJSONArrayFromResponse.optJSONObject(i);
                    if (optJSONObject != null) {
                        this.objectIsLiked = true;
                        JSONObject optJSONObject2 = optJSONObject.optJSONObject("application");
                        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                        if (optJSONObject2 != null && AccessToken.isCurrentAccessTokenActive() && Utility.areObjectsEqual(currentAccessToken.getApplicationId(), optJSONObject2.optString(ShareConstants.WEB_DIALOG_PARAM_ID))) {
                            this.unlikeToken = optJSONObject.optString(ShareConstants.WEB_DIALOG_PARAM_ID);
                        }
                    }
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error fetching like status for object '%s' with type '%s' : %s", this.objectId, this.objectType, facebookRequestError);
            LikeActionController.this.logAppEventForError("get_og_object_like", facebookRequestError);
        }

        public boolean isObjectLiked() {
            return this.objectIsLiked;
        }

        public String getUnlikeToken() {
            return this.unlikeToken;
        }
    }

    private class GetPageIdRequestWrapper extends AbstractRequestWrapper {
        boolean objectIsPage;
        String verifiedObjectId;

        GetPageIdRequestWrapper(String str, ObjectType objectType) {
            super(str, objectType);
            Bundle bundle = new Bundle();
            bundle.putString(GraphRequest.FIELDS_PARAM, ShareConstants.WEB_DIALOG_PARAM_ID);
            bundle.putString("ids", str);
            setRequest(new GraphRequest(AccessToken.getCurrentAccessToken(), "", bundle, HttpMethod.GET));
        }

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
            JSONObject tryGetJSONObjectFromResponse = Utility.tryGetJSONObjectFromResponse(graphResponse.getJSONObject(), this.objectId);
            if (tryGetJSONObjectFromResponse != null) {
                this.verifiedObjectId = tryGetJSONObjectFromResponse.optString(ShareConstants.WEB_DIALOG_PARAM_ID);
                this.objectIsPage = Utility.isNullOrEmpty(this.verifiedObjectId) ^ 1;
            }
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error getting the FB id for object '%s' with type '%s' : %s", this.objectId, this.objectType, facebookRequestError);
        }
    }

    private class GetPageLikesRequestWrapper extends AbstractRequestWrapper implements LikeRequestWrapper {
        private boolean objectIsLiked = LikeActionController.this.isObjectLiked;
        private String pageId;

        public String getUnlikeToken() {
            return null;
        }

        GetPageLikesRequestWrapper(String str) {
            super(str, ObjectType.PAGE);
            this.pageId = str;
            Bundle bundle = new Bundle();
            bundle.putString(GraphRequest.FIELDS_PARAM, ShareConstants.WEB_DIALOG_PARAM_ID);
            AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("me/likes/");
            stringBuilder.append(str);
            setRequest(new GraphRequest(currentAccessToken, stringBuilder.toString(), bundle, HttpMethod.GET));
        }

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
            JSONArray tryGetJSONArrayFromResponse = Utility.tryGetJSONArrayFromResponse(graphResponse.getJSONObject(), ShareConstants.WEB_DIALOG_PARAM_DATA);
            if (tryGetJSONArrayFromResponse != null && tryGetJSONArrayFromResponse.length() > 0) {
                this.objectIsLiked = true;
            }
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error fetching like status for page id '%s': %s", this.pageId, facebookRequestError);
            LikeActionController.this.logAppEventForError("get_page_like", facebookRequestError);
        }

        public boolean isObjectLiked() {
            return this.objectIsLiked;
        }
    }

    private class PublishLikeRequestWrapper extends AbstractRequestWrapper {
        String unlikeToken;

        PublishLikeRequestWrapper(String str, ObjectType objectType) {
            super(str, objectType);
            Bundle bundle = new Bundle();
            bundle.putString("object", str);
            setRequest(new GraphRequest(AccessToken.getCurrentAccessToken(), "me/og.likes", bundle, HttpMethod.POST));
        }

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
            this.unlikeToken = Utility.safeGetStringFromResponse(graphResponse.getJSONObject(), ShareConstants.WEB_DIALOG_PARAM_ID);
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            if (facebookRequestError.getErrorCode() == LikeActionController.ERROR_CODE_OBJECT_ALREADY_LIKED) {
                this.error = null;
                return;
            }
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error liking object '%s' with type '%s' : %s", this.objectId, this.objectType, facebookRequestError);
            LikeActionController.this.logAppEventForError("publish_like", facebookRequestError);
        }
    }

    private class PublishUnlikeRequestWrapper extends AbstractRequestWrapper {
        private String unlikeToken;

        /* Access modifiers changed, original: protected */
        public void processSuccess(GraphResponse graphResponse) {
        }

        PublishUnlikeRequestWrapper(String str) {
            super(null, null);
            this.unlikeToken = str;
            setRequest(new GraphRequest(AccessToken.getCurrentAccessToken(), str, null, HttpMethod.DELETE));
        }

        /* Access modifiers changed, original: protected */
        public void processError(FacebookRequestError facebookRequestError) {
            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Error unliking object with unlike token '%s' : %s", this.unlikeToken, facebookRequestError);
            LikeActionController.this.logAppEventForError("publish_unlike", facebookRequestError);
        }
    }

    @Deprecated
    public boolean shouldEnableView() {
        return false;
    }

    @Deprecated
    public static boolean handleOnActivityResult(final int i, final int i2, final Intent intent) {
        if (Utility.isNullOrEmpty(objectIdForPendingController)) {
            objectIdForPendingController = FacebookSdk.getApplicationContext().getSharedPreferences(LIKE_ACTION_CONTROLLER_STORE, 0).getString(LIKE_ACTION_CONTROLLER_STORE_PENDING_OBJECT_ID_KEY, null);
        }
        if (Utility.isNullOrEmpty(objectIdForPendingController)) {
            return false;
        }
        getControllerForObjectId(objectIdForPendingController, ObjectType.UNKNOWN, new CreationCallback() {
            public void onComplete(LikeActionController likeActionController, FacebookException facebookException) {
                if (facebookException == null) {
                    likeActionController.onActivityResult(i, i2, intent);
                } else {
                    Utility.logd(LikeActionController.TAG, (Exception) facebookException);
                }
            }
        });
        return true;
    }

    @Deprecated
    public static void getControllerForObjectId(String str, ObjectType objectType, CreationCallback creationCallback) {
        if (!isInitialized) {
            performFirstInitialize();
        }
        LikeActionController controllerFromInMemoryCache = getControllerFromInMemoryCache(str);
        if (controllerFromInMemoryCache != null) {
            verifyControllerAndInvokeCallback(controllerFromInMemoryCache, objectType, creationCallback);
        } else {
            diskIOWorkQueue.addActiveWorkItem(new CreateLikeActionControllerWorkItem(str, objectType, creationCallback));
        }
    }

    private static void verifyControllerAndInvokeCallback(LikeActionController likeActionController, ObjectType objectType, CreationCallback creationCallback) {
        FacebookException facebookException;
        ObjectType mostSpecificObjectType = ShareInternalUtility.getMostSpecificObjectType(objectType, likeActionController.objectType);
        if (mostSpecificObjectType == null) {
            facebookException = new FacebookException("Object with id:\"%s\" is already marked as type:\"%s\". Cannot change the type to:\"%s\"", likeActionController.objectId, likeActionController.objectType.toString(), objectType.toString());
            likeActionController = null;
        } else {
            likeActionController.objectType = mostSpecificObjectType;
            facebookException = null;
        }
        invokeCallbackWithController(creationCallback, likeActionController, facebookException);
    }

    private static void createControllerForObjectIdAndType(String str, ObjectType objectType, CreationCallback creationCallback) {
        LikeActionController controllerFromInMemoryCache = getControllerFromInMemoryCache(str);
        if (controllerFromInMemoryCache != null) {
            verifyControllerAndInvokeCallback(controllerFromInMemoryCache, objectType, creationCallback);
            return;
        }
        controllerFromInMemoryCache = deserializeFromDiskSynchronously(str);
        if (controllerFromInMemoryCache == null) {
            controllerFromInMemoryCache = new LikeActionController(str, objectType);
            serializeToDiskAsync(controllerFromInMemoryCache);
        }
        putControllerInMemoryCache(str, controllerFromInMemoryCache);
        handler.post(new AnonymousClass2(controllerFromInMemoryCache));
        invokeCallbackWithController(creationCallback, controllerFromInMemoryCache, null);
    }

    private static synchronized void performFirstInitialize() {
        synchronized (LikeActionController.class) {
            if (isInitialized) {
                return;
            }
            handler = new Handler(Looper.getMainLooper());
            objectSuffix = FacebookSdk.getApplicationContext().getSharedPreferences(LIKE_ACTION_CONTROLLER_STORE, 0).getInt(LIKE_ACTION_CONTROLLER_STORE_OBJECT_SUFFIX_KEY, 1);
            controllerDiskCache = new FileLruCache(TAG, new Limits());
            registerAccessTokenTracker();
            CallbackManagerImpl.registerStaticCallback(RequestCodeOffset.Like.toRequestCode(), new CallbackManagerImpl.Callback() {
                public boolean onActivityResult(int i, Intent intent) {
                    return LikeActionController.handleOnActivityResult(RequestCodeOffset.Like.toRequestCode(), i, intent);
                }
            });
            isInitialized = true;
        }
    }

    private static void invokeCallbackWithController(final CreationCallback creationCallback, final LikeActionController likeActionController, final FacebookException facebookException) {
        if (creationCallback != null) {
            handler.post(new Runnable() {
                public void run() {
                    creationCallback.onComplete(likeActionController, facebookException);
                }
            });
        }
    }

    private static void registerAccessTokenTracker() {
        accessTokenTracker = new AccessTokenTracker() {
            /* Access modifiers changed, original: protected */
            public void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                Context applicationContext = FacebookSdk.getApplicationContext();
                if (accessToken2 == null) {
                    LikeActionController.objectSuffix = (LikeActionController.objectSuffix + 1) % 1000;
                    applicationContext.getSharedPreferences(LikeActionController.LIKE_ACTION_CONTROLLER_STORE, 0).edit().putInt(LikeActionController.LIKE_ACTION_CONTROLLER_STORE_OBJECT_SUFFIX_KEY, LikeActionController.objectSuffix).apply();
                    LikeActionController.cache.clear();
                    LikeActionController.controllerDiskCache.clearCache();
                }
                LikeActionController.broadcastAction(null, LikeActionController.ACTION_LIKE_ACTION_CONTROLLER_DID_RESET);
            }
        };
    }

    private static void putControllerInMemoryCache(String str, LikeActionController likeActionController) {
        str = getCacheKeyForObjectId(str);
        mruCacheWorkQueue.addActiveWorkItem(new MRUCacheWorkItem(str, true));
        cache.put(str, likeActionController);
    }

    private static LikeActionController getControllerFromInMemoryCache(String str) {
        str = getCacheKeyForObjectId(str);
        LikeActionController likeActionController = (LikeActionController) cache.get(str);
        if (likeActionController != null) {
            mruCacheWorkQueue.addActiveWorkItem(new MRUCacheWorkItem(str, false));
        }
        return likeActionController;
    }

    private static void serializeToDiskAsync(LikeActionController likeActionController) {
        String serializeToJson = serializeToJson(likeActionController);
        String cacheKeyForObjectId = getCacheKeyForObjectId(likeActionController.objectId);
        if (!Utility.isNullOrEmpty(serializeToJson) && !Utility.isNullOrEmpty(cacheKeyForObjectId)) {
            diskIOWorkQueue.addActiveWorkItem(new SerializeToDiskWorkItem(cacheKeyForObjectId, serializeToJson));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0026  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x002c  */
    private static void serializeToDiskSynchronously(java.lang.String r2, java.lang.String r3) {
        /*
        r0 = 0;
        r1 = controllerDiskCache;	 Catch:{ IOException -> 0x001c }
        r2 = r1.openPutStream(r2);	 Catch:{ IOException -> 0x001c }
        r3 = r3.getBytes();	 Catch:{ IOException -> 0x0017, all -> 0x0014 }
        r2.write(r3);	 Catch:{ IOException -> 0x0017, all -> 0x0014 }
        if (r2 == 0) goto L_0x0029;
    L_0x0010:
        com.facebook.internal.Utility.closeQuietly(r2);
        goto L_0x0029;
    L_0x0014:
        r3 = move-exception;
        r0 = r2;
        goto L_0x002a;
    L_0x0017:
        r3 = move-exception;
        r0 = r2;
        goto L_0x001d;
    L_0x001a:
        r3 = move-exception;
        goto L_0x002a;
    L_0x001c:
        r3 = move-exception;
    L_0x001d:
        r2 = TAG;	 Catch:{ all -> 0x001a }
        r1 = "Unable to serialize controller to disk";
        android.util.Log.e(r2, r1, r3);	 Catch:{ all -> 0x001a }
        if (r0 == 0) goto L_0x0029;
    L_0x0026:
        com.facebook.internal.Utility.closeQuietly(r0);
    L_0x0029:
        return;
    L_0x002a:
        if (r0 == 0) goto L_0x002f;
    L_0x002c:
        com.facebook.internal.Utility.closeQuietly(r0);
    L_0x002f:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.share.internal.LikeActionController.serializeToDiskSynchronously(java.lang.String, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x003a  */
    /* JADX WARNING: Missing block: B:10:0x001f, code skipped:
            if (r5 != null) goto L_0x0021;
     */
    /* JADX WARNING: Missing block: B:11:0x0021, code skipped:
            com.facebook.internal.Utility.closeQuietly(r5);
     */
    /* JADX WARNING: Missing block: B:18:0x0033, code skipped:
            if (r5 != null) goto L_0x0021;
     */
    /* JADX WARNING: Missing block: B:19:0x0036, code skipped:
            return r0;
     */
    private static com.facebook.share.internal.LikeActionController deserializeFromDiskSynchronously(java.lang.String r5) {
        /*
        r0 = 0;
        r5 = getCacheKeyForObjectId(r5);	 Catch:{ IOException -> 0x002a, all -> 0x0025 }
        r1 = controllerDiskCache;	 Catch:{ IOException -> 0x002a, all -> 0x0025 }
        r5 = r1.get(r5);	 Catch:{ IOException -> 0x002a, all -> 0x0025 }
        if (r5 == 0) goto L_0x001f;
    L_0x000d:
        r1 = com.facebook.internal.Utility.readStreamToString(r5);	 Catch:{ IOException -> 0x001d }
        r2 = com.facebook.internal.Utility.isNullOrEmpty(r1);	 Catch:{ IOException -> 0x001d }
        if (r2 != 0) goto L_0x001f;
    L_0x0017:
        r1 = deserializeFromJson(r1);	 Catch:{ IOException -> 0x001d }
        r0 = r1;
        goto L_0x001f;
    L_0x001d:
        r1 = move-exception;
        goto L_0x002c;
    L_0x001f:
        if (r5 == 0) goto L_0x0036;
    L_0x0021:
        com.facebook.internal.Utility.closeQuietly(r5);
        goto L_0x0036;
    L_0x0025:
        r5 = move-exception;
        r4 = r0;
        r0 = r5;
        r5 = r4;
        goto L_0x0038;
    L_0x002a:
        r1 = move-exception;
        r5 = r0;
    L_0x002c:
        r2 = TAG;	 Catch:{ all -> 0x0037 }
        r3 = "Unable to deserialize controller from disk";
        android.util.Log.e(r2, r3, r1);	 Catch:{ all -> 0x0037 }
        if (r5 == 0) goto L_0x0036;
    L_0x0035:
        goto L_0x0021;
    L_0x0036:
        return r0;
    L_0x0037:
        r0 = move-exception;
    L_0x0038:
        if (r5 == 0) goto L_0x003d;
    L_0x003a:
        com.facebook.internal.Utility.closeQuietly(r5);
    L_0x003d:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.share.internal.LikeActionController.deserializeFromDiskSynchronously(java.lang.String):com.facebook.share.internal.LikeActionController");
    }

    private static LikeActionController deserializeFromJson(String str) {
        LikeActionController likeActionController = null;
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.optInt(JSON_INT_VERSION_KEY, -1) != 3) {
                return null;
            }
            LikeActionController likeActionController2 = new LikeActionController(jSONObject.getString("object_id"), ObjectType.fromInt(jSONObject.optInt("object_type", ObjectType.UNKNOWN.getValue())));
            likeActionController2.likeCountStringWithLike = jSONObject.optString(JSON_STRING_LIKE_COUNT_WITH_LIKE_KEY, null);
            likeActionController2.likeCountStringWithoutLike = jSONObject.optString(JSON_STRING_LIKE_COUNT_WITHOUT_LIKE_KEY, null);
            likeActionController2.socialSentenceWithLike = jSONObject.optString(JSON_STRING_SOCIAL_SENTENCE_WITH_LIKE_KEY, null);
            likeActionController2.socialSentenceWithoutLike = jSONObject.optString(JSON_STRING_SOCIAL_SENTENCE_WITHOUT_LIKE_KEY, null);
            likeActionController2.isObjectLiked = jSONObject.optBoolean(JSON_BOOL_IS_OBJECT_LIKED_KEY);
            likeActionController2.unlikeToken = jSONObject.optString("unlike_token", null);
            JSONObject optJSONObject = jSONObject.optJSONObject(JSON_BUNDLE_FACEBOOK_DIALOG_ANALYTICS_BUNDLE);
            if (optJSONObject != null) {
                likeActionController2.facebookDialogAnalyticsBundle = BundleJSONConverter.convertToBundle(optJSONObject);
            }
            likeActionController = likeActionController2;
            return likeActionController;
        } catch (JSONException e) {
            Log.e(TAG, "Unable to deserialize controller from JSON", e);
        }
    }

    private static String serializeToJson(LikeActionController likeActionController) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(JSON_INT_VERSION_KEY, 3);
            jSONObject.put("object_id", likeActionController.objectId);
            jSONObject.put("object_type", likeActionController.objectType.getValue());
            jSONObject.put(JSON_STRING_LIKE_COUNT_WITH_LIKE_KEY, likeActionController.likeCountStringWithLike);
            jSONObject.put(JSON_STRING_LIKE_COUNT_WITHOUT_LIKE_KEY, likeActionController.likeCountStringWithoutLike);
            jSONObject.put(JSON_STRING_SOCIAL_SENTENCE_WITH_LIKE_KEY, likeActionController.socialSentenceWithLike);
            jSONObject.put(JSON_STRING_SOCIAL_SENTENCE_WITHOUT_LIKE_KEY, likeActionController.socialSentenceWithoutLike);
            jSONObject.put(JSON_BOOL_IS_OBJECT_LIKED_KEY, likeActionController.isObjectLiked);
            jSONObject.put("unlike_token", likeActionController.unlikeToken);
            if (likeActionController.facebookDialogAnalyticsBundle != null) {
                JSONObject convertToJSON = BundleJSONConverter.convertToJSON(likeActionController.facebookDialogAnalyticsBundle);
                if (convertToJSON != null) {
                    jSONObject.put(JSON_BUNDLE_FACEBOOK_DIALOG_ANALYTICS_BUNDLE, convertToJSON);
                }
            }
            return jSONObject.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Unable to serialize controller to JSON", e);
            return null;
        }
    }

    private static String getCacheKeyForObjectId(String str) {
        String token = AccessToken.isCurrentAccessTokenActive() ? AccessToken.getCurrentAccessToken().getToken() : null;
        if (token != null) {
            token = Utility.md5hash(token);
        }
        return String.format(Locale.ROOT, "%s|%s|com.fb.sdk.like|%d", new Object[]{str, Utility.coerceValueIfNullOrEmpty(token, ""), Integer.valueOf(objectSuffix)});
    }

    private static void broadcastAction(LikeActionController likeActionController, String str) {
        broadcastAction(likeActionController, str, null);
    }

    private static void broadcastAction(LikeActionController likeActionController, String str, Bundle bundle) {
        Intent intent = new Intent(str);
        if (likeActionController != null) {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putString(ACTION_OBJECT_ID_KEY, likeActionController.getObjectId());
        }
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        LocalBroadcastManager.getInstance(FacebookSdk.getApplicationContext()).sendBroadcast(intent);
    }

    private LikeActionController(String str, ObjectType objectType) {
        this.objectId = str;
        this.objectType = objectType;
    }

    @Deprecated
    public String getObjectId() {
        return this.objectId;
    }

    @Deprecated
    public String getLikeCountString() {
        return this.isObjectLiked ? this.likeCountStringWithLike : this.likeCountStringWithoutLike;
    }

    @Deprecated
    public String getSocialSentence() {
        return this.isObjectLiked ? this.socialSentenceWithLike : this.socialSentenceWithoutLike;
    }

    @Deprecated
    public boolean isObjectLiked() {
        return this.isObjectLiked;
    }

    @Deprecated
    public void toggleLike(Activity activity, FragmentWrapper fragmentWrapper, Bundle bundle) {
        boolean z = true;
        int i = this.isObjectLiked ^ 1;
        if (canUseOGPublish()) {
            updateLikeState(i);
            if (this.isPendingLikeOrUnlike) {
                getAppEventsLogger().logSdkEvent(AnalyticsEvents.EVENT_LIKE_VIEW_DID_UNDO_QUICKLY, null, bundle);
                return;
            } else if (!publishLikeOrUnlikeAsync(i, bundle)) {
                if (i != 0) {
                    z = false;
                }
                updateLikeState(z);
                presentLikeDialog(activity, fragmentWrapper, bundle);
                return;
            } else {
                return;
            }
        }
        presentLikeDialog(activity, fragmentWrapper, bundle);
    }

    private AppEventsLogger getAppEventsLogger() {
        if (this.appEventsLogger == null) {
            this.appEventsLogger = AppEventsLogger.newLogger(FacebookSdk.getApplicationContext());
        }
        return this.appEventsLogger;
    }

    private boolean publishLikeOrUnlikeAsync(boolean z, Bundle bundle) {
        if (canUseOGPublish()) {
            if (z) {
                publishLikeAsync(bundle);
                return true;
            } else if (!Utility.isNullOrEmpty(this.unlikeToken)) {
                publishUnlikeAsync(bundle);
                return true;
            }
        }
        return false;
    }

    private void publishDidError(boolean z) {
        updateLikeState(z);
        Bundle bundle = new Bundle();
        bundle.putString(NativeProtocol.STATUS_ERROR_DESCRIPTION, ERROR_PUBLISH_ERROR);
        broadcastAction(this, ACTION_LIKE_ACTION_CONTROLLER_DID_ERROR, bundle);
    }

    private void updateLikeState(boolean z) {
        updateState(z, this.likeCountStringWithLike, this.likeCountStringWithoutLike, this.socialSentenceWithLike, this.socialSentenceWithoutLike, this.unlikeToken);
    }

    private void updateState(boolean z, String str, String str2, String str3, String str4, String str5) {
        str = Utility.coerceValueIfNullOrEmpty(str, null);
        str2 = Utility.coerceValueIfNullOrEmpty(str2, null);
        str3 = Utility.coerceValueIfNullOrEmpty(str3, null);
        str4 = Utility.coerceValueIfNullOrEmpty(str4, null);
        str5 = Utility.coerceValueIfNullOrEmpty(str5, null);
        Object obj = (z == this.isObjectLiked && Utility.areObjectsEqual(str, this.likeCountStringWithLike) && Utility.areObjectsEqual(str2, this.likeCountStringWithoutLike) && Utility.areObjectsEqual(str3, this.socialSentenceWithLike) && Utility.areObjectsEqual(str4, this.socialSentenceWithoutLike) && Utility.areObjectsEqual(str5, this.unlikeToken)) ? null : 1;
        if (obj != null) {
            this.isObjectLiked = z;
            this.likeCountStringWithLike = str;
            this.likeCountStringWithoutLike = str2;
            this.socialSentenceWithLike = str3;
            this.socialSentenceWithoutLike = str4;
            this.unlikeToken = str5;
            serializeToDiskAsync(this);
            broadcastAction(this, ACTION_LIKE_ACTION_CONTROLLER_UPDATED);
        }
    }

    private void presentLikeDialog(Activity activity, FragmentWrapper fragmentWrapper, Bundle bundle) {
        String str;
        if (LikeDialog.canShowNativeDialog()) {
            str = AnalyticsEvents.EVENT_LIKE_VIEW_DID_PRESENT_DIALOG;
        } else if (LikeDialog.canShowWebFallback()) {
            str = AnalyticsEvents.EVENT_LIKE_VIEW_DID_PRESENT_FALLBACK;
        } else {
            logAppEventForError("present_dialog", bundle);
            Utility.logd(TAG, "Cannot show the Like Dialog on this device.");
            broadcastAction(null, ACTION_LIKE_ACTION_CONTROLLER_UPDATED);
            str = null;
        }
        if (str != null) {
            if (this.objectType != null) {
                str = this.objectType.toString();
            } else {
                str = ObjectType.UNKNOWN.toString();
            }
            LikeContent build = new Builder().setObjectId(this.objectId).setObjectType(str).build();
            if (fragmentWrapper != null) {
                new LikeDialog(fragmentWrapper).show(build);
            } else {
                new LikeDialog(activity).show(build);
            }
            saveState(bundle);
            getAppEventsLogger().logSdkEvent(AnalyticsEvents.EVENT_LIKE_VIEW_DID_PRESENT_DIALOG, null, bundle);
        }
    }

    private void onActivityResult(int i, int i2, Intent intent) {
        ShareInternalUtility.handleActivityResult(i, i2, intent, getResultProcessor(this.facebookDialogAnalyticsBundle));
        clearState();
    }

    private ResultProcessor getResultProcessor(final Bundle bundle) {
        return new ResultProcessor(null) {
            public void onSuccess(AppCall appCall, Bundle bundle) {
                if (bundle != null && bundle.containsKey(LikeActionController.LIKE_DIALOG_RESPONSE_OBJECT_IS_LIKED_KEY)) {
                    String string;
                    String str;
                    String string2;
                    String str2;
                    String string3;
                    boolean z = bundle.getBoolean(LikeActionController.LIKE_DIALOG_RESPONSE_OBJECT_IS_LIKED_KEY);
                    String access$700 = LikeActionController.this.likeCountStringWithLike;
                    String access$800 = LikeActionController.this.likeCountStringWithoutLike;
                    if (bundle.containsKey(LikeActionController.LIKE_DIALOG_RESPONSE_LIKE_COUNT_STRING_KEY)) {
                        string = bundle.getString(LikeActionController.LIKE_DIALOG_RESPONSE_LIKE_COUNT_STRING_KEY);
                        str = string;
                    } else {
                        string = access$700;
                        str = access$800;
                    }
                    access$700 = LikeActionController.this.socialSentenceWithLike;
                    access$800 = LikeActionController.this.socialSentenceWithoutLike;
                    if (bundle.containsKey(LikeActionController.LIKE_DIALOG_RESPONSE_SOCIAL_SENTENCE_KEY)) {
                        string2 = bundle.getString(LikeActionController.LIKE_DIALOG_RESPONSE_SOCIAL_SENTENCE_KEY);
                        str2 = string2;
                    } else {
                        string2 = access$700;
                        str2 = access$800;
                    }
                    if (bundle.containsKey(LikeActionController.LIKE_DIALOG_RESPONSE_OBJECT_IS_LIKED_KEY)) {
                        string3 = bundle.getString("unlike_token");
                    } else {
                        string3 = LikeActionController.this.unlikeToken;
                    }
                    String str3 = string3;
                    bundle = bundle == null ? new Bundle() : bundle;
                    bundle.putString(AnalyticsEvents.PARAMETER_CALL_ID, appCall.getCallId().toString());
                    LikeActionController.this.getAppEventsLogger().logSdkEvent(AnalyticsEvents.EVENT_LIKE_VIEW_DIALOG_DID_SUCCEED, null, bundle);
                    LikeActionController.this.updateState(z, string, str, string2, str2, str3);
                }
            }

            public void onError(AppCall appCall, FacebookException facebookException) {
                Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Like Dialog failed with error : %s", facebookException);
                Bundle bundle = bundle == null ? new Bundle() : bundle;
                bundle.putString(AnalyticsEvents.PARAMETER_CALL_ID, appCall.getCallId().toString());
                LikeActionController.this.logAppEventForError("present_dialog", bundle);
                LikeActionController.broadcastAction(LikeActionController.this, LikeActionController.ACTION_LIKE_ACTION_CONTROLLER_DID_ERROR, NativeProtocol.createBundleForException(facebookException));
            }

            public void onCancel(AppCall appCall) {
                onError(appCall, new FacebookOperationCanceledException());
            }
        };
    }

    private void saveState(Bundle bundle) {
        storeObjectIdForPendingController(this.objectId);
        this.facebookDialogAnalyticsBundle = bundle;
        serializeToDiskAsync(this);
    }

    private void clearState() {
        this.facebookDialogAnalyticsBundle = null;
        storeObjectIdForPendingController(null);
    }

    private static void storeObjectIdForPendingController(String str) {
        objectIdForPendingController = str;
        FacebookSdk.getApplicationContext().getSharedPreferences(LIKE_ACTION_CONTROLLER_STORE, 0).edit().putString(LIKE_ACTION_CONTROLLER_STORE_PENDING_OBJECT_ID_KEY, objectIdForPendingController).apply();
    }

    private boolean canUseOGPublish() {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        return (this.objectIsPage || this.verifiedObjectId == null || !AccessToken.isCurrentAccessTokenActive() || currentAccessToken.getPermissions() == null || !currentAccessToken.getPermissions().contains("publish_actions")) ? false : true;
    }

    private void publishLikeAsync(final Bundle bundle) {
        this.isPendingLikeOrUnlike = true;
        fetchVerifiedObjectId(new RequestCompletionCallback() {
            public void onComplete() {
                if (Utility.isNullOrEmpty(LikeActionController.this.verifiedObjectId)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(NativeProtocol.STATUS_ERROR_DESCRIPTION, LikeActionController.ERROR_INVALID_OBJECT_ID);
                    LikeActionController.broadcastAction(LikeActionController.this, LikeActionController.ACTION_LIKE_ACTION_CONTROLLER_DID_ERROR, bundle);
                    return;
                }
                GraphRequestBatch graphRequestBatch = new GraphRequestBatch();
                final PublishLikeRequestWrapper publishLikeRequestWrapper = new PublishLikeRequestWrapper(LikeActionController.this.verifiedObjectId, LikeActionController.this.objectType);
                publishLikeRequestWrapper.addToBatch(graphRequestBatch);
                graphRequestBatch.addCallback(new GraphRequestBatch.Callback() {
                    public void onBatchCompleted(GraphRequestBatch graphRequestBatch) {
                        LikeActionController.this.isPendingLikeOrUnlike = false;
                        if (publishLikeRequestWrapper.getError() != null) {
                            LikeActionController.this.publishDidError(false);
                            return;
                        }
                        LikeActionController.this.unlikeToken = Utility.coerceValueIfNullOrEmpty(publishLikeRequestWrapper.unlikeToken, null);
                        LikeActionController.this.isObjectLikedOnServer = true;
                        LikeActionController.this.getAppEventsLogger().logSdkEvent(AnalyticsEvents.EVENT_LIKE_VIEW_DID_LIKE, null, bundle);
                        LikeActionController.this.publishAgainIfNeeded(bundle);
                    }
                });
                graphRequestBatch.executeAsync();
            }
        });
    }

    private void publishUnlikeAsync(final Bundle bundle) {
        this.isPendingLikeOrUnlike = true;
        GraphRequestBatch graphRequestBatch = new GraphRequestBatch();
        final PublishUnlikeRequestWrapper publishUnlikeRequestWrapper = new PublishUnlikeRequestWrapper(this.unlikeToken);
        publishUnlikeRequestWrapper.addToBatch(graphRequestBatch);
        graphRequestBatch.addCallback(new GraphRequestBatch.Callback() {
            public void onBatchCompleted(GraphRequestBatch graphRequestBatch) {
                LikeActionController.this.isPendingLikeOrUnlike = false;
                if (publishUnlikeRequestWrapper.getError() != null) {
                    LikeActionController.this.publishDidError(true);
                    return;
                }
                LikeActionController.this.unlikeToken = null;
                LikeActionController.this.isObjectLikedOnServer = false;
                LikeActionController.this.getAppEventsLogger().logSdkEvent(AnalyticsEvents.EVENT_LIKE_VIEW_DID_UNLIKE, null, bundle);
                LikeActionController.this.publishAgainIfNeeded(bundle);
            }
        });
        graphRequestBatch.executeAsync();
    }

    private void refreshStatusAsync() {
        if (AccessToken.isCurrentAccessTokenActive()) {
            fetchVerifiedObjectId(new RequestCompletionCallback() {
                public void onComplete() {
                    LikeRequestWrapper getOGObjectLikesRequestWrapper;
                    if (AnonymousClass12.$SwitchMap$com$facebook$share$widget$LikeView$ObjectType[LikeActionController.this.objectType.ordinal()] != 1) {
                        getOGObjectLikesRequestWrapper = new GetOGObjectLikesRequestWrapper(LikeActionController.this.verifiedObjectId, LikeActionController.this.objectType);
                    } else {
                        getOGObjectLikesRequestWrapper = new GetPageLikesRequestWrapper(LikeActionController.this.verifiedObjectId);
                    }
                    final GetEngagementRequestWrapper getEngagementRequestWrapper = new GetEngagementRequestWrapper(LikeActionController.this.verifiedObjectId, LikeActionController.this.objectType);
                    GraphRequestBatch graphRequestBatch = new GraphRequestBatch();
                    getOGObjectLikesRequestWrapper.addToBatch(graphRequestBatch);
                    getEngagementRequestWrapper.addToBatch(graphRequestBatch);
                    graphRequestBatch.addCallback(new GraphRequestBatch.Callback() {
                        public void onBatchCompleted(GraphRequestBatch graphRequestBatch) {
                            if (getOGObjectLikesRequestWrapper.getError() == null && getEngagementRequestWrapper.getError() == null) {
                                LikeActionController.this.updateState(getOGObjectLikesRequestWrapper.isObjectLiked(), getEngagementRequestWrapper.likeCountStringWithLike, getEngagementRequestWrapper.likeCountStringWithoutLike, getEngagementRequestWrapper.socialSentenceStringWithLike, getEngagementRequestWrapper.socialSentenceStringWithoutLike, getOGObjectLikesRequestWrapper.getUnlikeToken());
                                return;
                            }
                            Logger.log(LoggingBehavior.REQUESTS, LikeActionController.TAG, "Unable to refresh like state for id: '%s'", LikeActionController.this.objectId);
                        }
                    });
                    graphRequestBatch.executeAsync();
                }
            });
        } else {
            refreshStatusViaService();
        }
    }

    private void refreshStatusViaService() {
        LikeStatusClient likeStatusClient = new LikeStatusClient(FacebookSdk.getApplicationContext(), FacebookSdk.getApplicationId(), this.objectId);
        if (likeStatusClient.start()) {
            likeStatusClient.setCompletedListener(new CompletedListener() {
                public void completed(Bundle bundle) {
                    if (bundle != null && bundle.containsKey(ShareConstants.EXTRA_OBJECT_IS_LIKED)) {
                        String string;
                        String string2;
                        boolean z = bundle.getBoolean(ShareConstants.EXTRA_OBJECT_IS_LIKED);
                        if (bundle.containsKey(ShareConstants.EXTRA_LIKE_COUNT_STRING_WITH_LIKE)) {
                            string = bundle.getString(ShareConstants.EXTRA_LIKE_COUNT_STRING_WITH_LIKE);
                        } else {
                            string = LikeActionController.this.likeCountStringWithLike;
                        }
                        String str = string;
                        if (bundle.containsKey(ShareConstants.EXTRA_LIKE_COUNT_STRING_WITHOUT_LIKE)) {
                            string = bundle.getString(ShareConstants.EXTRA_LIKE_COUNT_STRING_WITHOUT_LIKE);
                        } else {
                            string = LikeActionController.this.likeCountStringWithoutLike;
                        }
                        String str2 = string;
                        if (bundle.containsKey(ShareConstants.EXTRA_SOCIAL_SENTENCE_WITH_LIKE)) {
                            string = bundle.getString(ShareConstants.EXTRA_SOCIAL_SENTENCE_WITH_LIKE);
                        } else {
                            string = LikeActionController.this.socialSentenceWithLike;
                        }
                        String str3 = string;
                        if (bundle.containsKey(ShareConstants.EXTRA_SOCIAL_SENTENCE_WITHOUT_LIKE)) {
                            string = bundle.getString(ShareConstants.EXTRA_SOCIAL_SENTENCE_WITHOUT_LIKE);
                        } else {
                            string = LikeActionController.this.socialSentenceWithoutLike;
                        }
                        String str4 = string;
                        if (bundle.containsKey(ShareConstants.EXTRA_UNLIKE_TOKEN)) {
                            string2 = bundle.getString(ShareConstants.EXTRA_UNLIKE_TOKEN);
                        } else {
                            string2 = LikeActionController.this.unlikeToken;
                        }
                        LikeActionController.this.updateState(z, str, str2, str3, str4, string2);
                    }
                }
            });
        }
    }

    private void publishAgainIfNeeded(Bundle bundle) {
        if (this.isObjectLiked != this.isObjectLikedOnServer && !publishLikeOrUnlikeAsync(this.isObjectLiked, bundle)) {
            publishDidError(this.isObjectLiked ^ 1);
        }
    }

    private void fetchVerifiedObjectId(final RequestCompletionCallback requestCompletionCallback) {
        if (Utility.isNullOrEmpty(this.verifiedObjectId)) {
            final GetOGObjectIdRequestWrapper getOGObjectIdRequestWrapper = new GetOGObjectIdRequestWrapper(this.objectId, this.objectType);
            final GetPageIdRequestWrapper getPageIdRequestWrapper = new GetPageIdRequestWrapper(this.objectId, this.objectType);
            GraphRequestBatch graphRequestBatch = new GraphRequestBatch();
            getOGObjectIdRequestWrapper.addToBatch(graphRequestBatch);
            getPageIdRequestWrapper.addToBatch(graphRequestBatch);
            graphRequestBatch.addCallback(new GraphRequestBatch.Callback() {
                public void onBatchCompleted(GraphRequestBatch graphRequestBatch) {
                    LikeActionController.this.verifiedObjectId = getOGObjectIdRequestWrapper.verifiedObjectId;
                    if (Utility.isNullOrEmpty(LikeActionController.this.verifiedObjectId)) {
                        LikeActionController.this.verifiedObjectId = getPageIdRequestWrapper.verifiedObjectId;
                        LikeActionController.this.objectIsPage = getPageIdRequestWrapper.objectIsPage;
                    }
                    if (Utility.isNullOrEmpty(LikeActionController.this.verifiedObjectId)) {
                        FacebookRequestError error;
                        Logger.log(LoggingBehavior.DEVELOPER_ERRORS, LikeActionController.TAG, "Unable to verify the FB id for '%s'. Verify that it is a valid FB object or page", LikeActionController.this.objectId);
                        LikeActionController likeActionController = LikeActionController.this;
                        String str = "get_verified_id";
                        if (getPageIdRequestWrapper.getError() != null) {
                            error = getPageIdRequestWrapper.getError();
                        } else {
                            error = getOGObjectIdRequestWrapper.getError();
                        }
                        likeActionController.logAppEventForError(str, error);
                    }
                    if (requestCompletionCallback != null) {
                        requestCompletionCallback.onComplete();
                    }
                }
            });
            graphRequestBatch.executeAsync();
            return;
        }
        if (requestCompletionCallback != null) {
            requestCompletionCallback.onComplete();
        }
    }

    private void logAppEventForError(String str, Bundle bundle) {
        Bundle bundle2 = new Bundle(bundle);
        bundle2.putString("object_id", this.objectId);
        bundle2.putString("object_type", this.objectType.toString());
        bundle2.putString(AnalyticsEvents.PARAMETER_LIKE_VIEW_CURRENT_ACTION, str);
        getAppEventsLogger().logSdkEvent(AnalyticsEvents.EVENT_LIKE_VIEW_ERROR, null, bundle2);
    }

    private void logAppEventForError(String str, FacebookRequestError facebookRequestError) {
        Bundle bundle = new Bundle();
        if (facebookRequestError != null) {
            JSONObject requestResult = facebookRequestError.getRequestResult();
            if (requestResult != null) {
                bundle.putString("error", requestResult.toString());
            }
        }
        logAppEventForError(str, bundle);
    }
}
