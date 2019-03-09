package com.facebook.share;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookGraphResponseException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.internal.CollectionMapper;
import com.facebook.internal.CollectionMapper.OnErrorListener;
import com.facebook.internal.CollectionMapper.OnMapValueCompleteListener;
import com.facebook.internal.CollectionMapper.OnMapperCompleteListener;
import com.facebook.internal.CollectionMapper.ValueMapper;
import com.facebook.internal.Mutable;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.Utility;
import com.facebook.share.Sharer.Result;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.internal.ShareContentValidation;
import com.facebook.share.internal.ShareInternalUtility;
import com.facebook.share.internal.VideoUploader;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideoContent;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ShareApi {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String DEFAULT_GRAPH_NODE = "me";
    private static final String GRAPH_PATH_FORMAT = "%s/%s";
    private static final String PHOTOS_EDGE = "photos";
    private static final String TAG = "ShareApi";
    private String graphNode = DEFAULT_GRAPH_NODE;
    private String message;
    private final ShareContent shareContent;

    public static void share(ShareContent shareContent, FacebookCallback<Result> facebookCallback) {
        new ShareApi(shareContent).share(facebookCallback);
    }

    public ShareApi(ShareContent shareContent) {
        this.shareContent = shareContent;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    public String getGraphNode() {
        return this.graphNode;
    }

    public void setGraphNode(String str) {
        this.graphNode = str;
    }

    public ShareContent getShareContent() {
        return this.shareContent;
    }

    public boolean canShare() {
        if (getShareContent() == null) {
            return false;
        }
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        if (!AccessToken.isCurrentAccessTokenActive()) {
            return false;
        }
        Set permissions = currentAccessToken.getPermissions();
        if (permissions == null || !permissions.contains("publish_actions")) {
            Log.w(TAG, "The publish_actions permissions are missing, the share will fail unless this app was authorized to publish in another installation.");
        }
        return true;
    }

    public void share(FacebookCallback<Result> facebookCallback) {
        if (canShare()) {
            ShareContent shareContent = getShareContent();
            try {
                ShareContentValidation.validateForApiShare(shareContent);
                if (shareContent instanceof ShareLinkContent) {
                    shareLinkContent((ShareLinkContent) shareContent, facebookCallback);
                } else if (shareContent instanceof SharePhotoContent) {
                    sharePhotoContent((SharePhotoContent) shareContent, facebookCallback);
                } else if (shareContent instanceof ShareVideoContent) {
                    shareVideoContent((ShareVideoContent) shareContent, facebookCallback);
                } else if (shareContent instanceof ShareOpenGraphContent) {
                    shareOpenGraphContent((ShareOpenGraphContent) shareContent, facebookCallback);
                }
                return;
            } catch (FacebookException e) {
                ShareInternalUtility.invokeCallbackWithException(facebookCallback, e);
                return;
            }
        }
        ShareInternalUtility.invokeCallbackWithError(facebookCallback, "Insufficient permissions for sharing content via Api.");
    }

    private String getGraphPath(String str) {
        try {
            return String.format(Locale.ROOT, GRAPH_PATH_FORMAT, new Object[]{URLEncoder.encode(getGraphNode(), "UTF-8"), str});
        } catch (UnsupportedEncodingException unused) {
            return null;
        }
    }

    private void addCommonParameters(Bundle bundle, ShareContent shareContent) {
        Collection peopleIds = shareContent.getPeopleIds();
        if (!Utility.isNullOrEmpty(peopleIds)) {
            bundle.putString("tags", TextUtils.join(", ", peopleIds));
        }
        if (!Utility.isNullOrEmpty(shareContent.getPlaceId())) {
            bundle.putString("place", shareContent.getPlaceId());
        }
        if (!Utility.isNullOrEmpty(shareContent.getPageId())) {
            bundle.putString("page", shareContent.getPageId());
        }
        if (!Utility.isNullOrEmpty(shareContent.getRef())) {
            bundle.putString("ref", shareContent.getRef());
        }
    }

    private void shareOpenGraphContent(ShareOpenGraphContent shareOpenGraphContent, final FacebookCallback<Result> facebookCallback) {
        final AnonymousClass1 anonymousClass1 = new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                String str;
                JSONObject jSONObject = graphResponse.getJSONObject();
                if (jSONObject == null) {
                    str = null;
                } else {
                    str = jSONObject.optString(ShareConstants.WEB_DIALOG_PARAM_ID);
                }
                ShareInternalUtility.invokeCallbackWithResults(facebookCallback, str, graphResponse);
            }
        };
        final ShareOpenGraphAction action = shareOpenGraphContent.getAction();
        Bundle bundle = action.getBundle();
        addCommonParameters(bundle, shareOpenGraphContent);
        if (!Utility.isNullOrEmpty(getMessage())) {
            bundle.putString("message", getMessage());
        }
        final Bundle bundle2 = bundle;
        final FacebookCallback<Result> facebookCallback2 = facebookCallback;
        stageOpenGraphAction(bundle, new OnMapperCompleteListener() {
            public void onComplete() {
                try {
                    ShareApi.handleImagesOnAction(bundle2);
                    new GraphRequest(AccessToken.getCurrentAccessToken(), ShareApi.this.getGraphPath(URLEncoder.encode(action.getActionType(), "UTF-8")), bundle2, HttpMethod.POST, anonymousClass1).executeAsync();
                } catch (UnsupportedEncodingException e) {
                    ShareInternalUtility.invokeCallbackWithException(facebookCallback2, e);
                }
            }

            public void onError(FacebookException facebookException) {
                ShareInternalUtility.invokeCallbackWithException(facebookCallback2, facebookException);
            }
        });
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:14:0x0040 */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(2:14|15) */
    /* JADX WARNING: Missing block: B:15:?, code skipped:
            putImageInBundleWithArrayFormat(r9, 0, new org.json.JSONObject(r0));
            r9.remove(com.facebook.share.internal.MessengerShareContentUtility.MEDIA_IMAGE);
     */
    private static void handleImagesOnAction(android.os.Bundle r9) {
        /*
        r0 = "image";
        r0 = r9.getString(r0);
        if (r0 == 0) goto L_0x004d;
    L_0x0008:
        r1 = 0;
        r2 = new org.json.JSONArray;	 Catch:{ JSONException -> 0x0040 }
        r2.<init>(r0);	 Catch:{ JSONException -> 0x0040 }
        r3 = 0;
    L_0x000f:
        r4 = r2.length();	 Catch:{ JSONException -> 0x0040 }
        if (r3 >= r4) goto L_0x003a;
    L_0x0015:
        r4 = r2.optJSONObject(r3);	 Catch:{ JSONException -> 0x0040 }
        if (r4 == 0) goto L_0x001f;
    L_0x001b:
        putImageInBundleWithArrayFormat(r9, r3, r4);	 Catch:{ JSONException -> 0x0040 }
        goto L_0x0037;
    L_0x001f:
        r4 = r2.getString(r3);	 Catch:{ JSONException -> 0x0040 }
        r5 = java.util.Locale.ROOT;	 Catch:{ JSONException -> 0x0040 }
        r6 = "image[%d][url]";
        r7 = 1;
        r7 = new java.lang.Object[r7];	 Catch:{ JSONException -> 0x0040 }
        r8 = java.lang.Integer.valueOf(r3);	 Catch:{ JSONException -> 0x0040 }
        r7[r1] = r8;	 Catch:{ JSONException -> 0x0040 }
        r5 = java.lang.String.format(r5, r6, r7);	 Catch:{ JSONException -> 0x0040 }
        r9.putString(r5, r4);	 Catch:{ JSONException -> 0x0040 }
    L_0x0037:
        r3 = r3 + 1;
        goto L_0x000f;
    L_0x003a:
        r2 = "image";
        r9.remove(r2);	 Catch:{ JSONException -> 0x0040 }
        return;
    L_0x0040:
        r2 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x004d }
        r2.<init>(r0);	 Catch:{ JSONException -> 0x004d }
        putImageInBundleWithArrayFormat(r9, r1, r2);	 Catch:{ JSONException -> 0x004d }
        r0 = "image";
        r9.remove(r0);	 Catch:{ JSONException -> 0x004d }
    L_0x004d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.share.ShareApi.handleImagesOnAction(android.os.Bundle):void");
    }

    private static void putImageInBundleWithArrayFormat(Bundle bundle, int i, JSONObject jSONObject) throws JSONException {
        Iterator keys = jSONObject.keys();
        while (keys.hasNext()) {
            Object[] objArr = new Object[]{Integer.valueOf(i), (String) keys.next()};
            bundle.putString(String.format(Locale.ROOT, "image[%d][%s]", objArr), jSONObject.get((String) keys.next()).toString());
        }
    }

    private void sharePhotoContent(SharePhotoContent sharePhotoContent, FacebookCallback<Result> facebookCallback) {
        FacebookCallback<Result> facebookCallback2 = facebookCallback;
        Mutable mutable = new Mutable(Integer.valueOf(0));
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        ArrayList arrayList = new ArrayList();
        final ArrayList arrayList2 = new ArrayList();
        final ArrayList arrayList3 = new ArrayList();
        final Mutable mutable2 = mutable;
        final FacebookCallback<Result> facebookCallback3 = facebookCallback;
        AnonymousClass3 anonymousClass3 = new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                JSONObject jSONObject = graphResponse.getJSONObject();
                if (jSONObject != null) {
                    arrayList2.add(jSONObject);
                }
                if (graphResponse.getError() != null) {
                    arrayList3.add(graphResponse);
                }
                mutable2.value = Integer.valueOf(((Integer) mutable2.value).intValue() - 1);
                if (((Integer) mutable2.value).intValue() != 0) {
                    return;
                }
                if (!arrayList3.isEmpty()) {
                    ShareInternalUtility.invokeCallbackWithResults(facebookCallback3, null, (GraphResponse) arrayList3.get(0));
                } else if (!arrayList2.isEmpty()) {
                    ShareInternalUtility.invokeCallbackWithResults(facebookCallback3, ((JSONObject) arrayList2.get(0)).optString(ShareConstants.WEB_DIALOG_PARAM_ID), graphResponse);
                }
            }
        };
        try {
            ArrayList arrayList4;
            for (SharePhoto sharePhoto : sharePhotoContent.getPhotos()) {
                try {
                    Bundle sharePhotoCommonParameters = getSharePhotoCommonParameters(sharePhoto, sharePhotoContent);
                    Bitmap bitmap = sharePhoto.getBitmap();
                    Uri imageUrl = sharePhoto.getImageUrl();
                    String caption = sharePhoto.getCaption();
                    if (caption == null) {
                        caption = getMessage();
                    }
                    String str = caption;
                    if (bitmap != null) {
                        arrayList4 = arrayList;
                        arrayList4.add(GraphRequest.newUploadPhotoRequest(currentAccessToken, getGraphPath(PHOTOS_EDGE), bitmap, str, sharePhotoCommonParameters, (Callback) anonymousClass3));
                    } else {
                        arrayList4 = arrayList;
                        if (imageUrl != null) {
                            arrayList4.add(GraphRequest.newUploadPhotoRequest(currentAccessToken, getGraphPath(PHOTOS_EDGE), imageUrl, str, sharePhotoCommonParameters, (Callback) anonymousClass3));
                        }
                    }
                    arrayList = arrayList4;
                } catch (JSONException e) {
                    ShareInternalUtility.invokeCallbackWithException(facebookCallback2, e);
                    return;
                }
            }
            arrayList4 = arrayList;
            mutable.value = Integer.valueOf(((Integer) mutable.value).intValue() + arrayList4.size());
            Iterator it = arrayList4.iterator();
            while (it.hasNext()) {
                ((GraphRequest) it.next()).executeAsync();
            }
        } catch (FileNotFoundException e2) {
            ShareInternalUtility.invokeCallbackWithException(facebookCallback2, e2);
        }
    }

    private void shareLinkContent(ShareLinkContent shareLinkContent, final FacebookCallback<Result> facebookCallback) {
        AnonymousClass4 anonymousClass4 = new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                String str;
                JSONObject jSONObject = graphResponse.getJSONObject();
                if (jSONObject == null) {
                    str = null;
                } else {
                    str = jSONObject.optString(ShareConstants.WEB_DIALOG_PARAM_ID);
                }
                ShareInternalUtility.invokeCallbackWithResults(facebookCallback, str, graphResponse);
            }
        };
        Bundle bundle = new Bundle();
        addCommonParameters(bundle, shareLinkContent);
        bundle.putString("message", getMessage());
        bundle.putString("link", Utility.getUriString(shareLinkContent.getContentUrl()));
        bundle.putString("picture", Utility.getUriString(shareLinkContent.getImageUrl()));
        bundle.putString("name", shareLinkContent.getContentTitle());
        bundle.putString("description", shareLinkContent.getContentDescription());
        bundle.putString("ref", shareLinkContent.getRef());
        new GraphRequest(AccessToken.getCurrentAccessToken(), getGraphPath("feed"), bundle, HttpMethod.POST, anonymousClass4).executeAsync();
    }

    private void shareVideoContent(ShareVideoContent shareVideoContent, FacebookCallback<Result> facebookCallback) {
        try {
            VideoUploader.uploadAsync(shareVideoContent, getGraphNode(), facebookCallback);
        } catch (FileNotFoundException e) {
            ShareInternalUtility.invokeCallbackWithException(facebookCallback, e);
        }
    }

    private Bundle getSharePhotoCommonParameters(SharePhoto sharePhoto, SharePhotoContent sharePhotoContent) throws JSONException {
        Bundle parameters = sharePhoto.getParameters();
        if (!(parameters.containsKey("place") || Utility.isNullOrEmpty(sharePhotoContent.getPlaceId()))) {
            parameters.putString("place", sharePhotoContent.getPlaceId());
        }
        if (!(parameters.containsKey("tags") || Utility.isNullOrEmpty(sharePhotoContent.getPeopleIds()))) {
            Collection<String> peopleIds = sharePhotoContent.getPeopleIds();
            if (!Utility.isNullOrEmpty((Collection) peopleIds)) {
                JSONArray jSONArray = new JSONArray();
                for (String str : peopleIds) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("tag_uid", str);
                    jSONArray.put(jSONObject);
                }
                parameters.putString("tags", jSONArray.toString());
            }
        }
        if (!(parameters.containsKey("ref") || Utility.isNullOrEmpty(sharePhotoContent.getRef()))) {
            parameters.putString("ref", sharePhotoContent.getRef());
        }
        return parameters;
    }

    private void stageArrayList(final ArrayList arrayList, final OnMapValueCompleteListener onMapValueCompleteListener) {
        final JSONArray jSONArray = new JSONArray();
        stageCollectionValues(new CollectionMapper.Collection<Integer>() {
            public Iterator<Integer> keyIterator() {
                final int size = arrayList.size();
                final Mutable mutable = new Mutable(Integer.valueOf(0));
                return new Iterator<Integer>() {
                    public void remove() {
                    }

                    public boolean hasNext() {
                        return ((Integer) mutable.value).intValue() < size;
                    }

                    public Integer next() {
                        Integer num = (Integer) mutable.value;
                        Mutable mutable = mutable;
                        mutable.value = Integer.valueOf(((Integer) mutable.value).intValue() + 1);
                        return num;
                    }
                };
            }

            public Object get(Integer num) {
                return arrayList.get(num.intValue());
            }

            public void set(Integer num, Object obj, OnErrorListener onErrorListener) {
                try {
                    jSONArray.put(num.intValue(), obj);
                } catch (JSONException e) {
                    String localizedMessage = e.getLocalizedMessage();
                    if (localizedMessage == null) {
                        localizedMessage = "Error staging object.";
                    }
                    onErrorListener.onError(new FacebookException(localizedMessage));
                }
            }
        }, new OnMapperCompleteListener() {
            public void onComplete() {
                onMapValueCompleteListener.onComplete(jSONArray);
            }

            public void onError(FacebookException facebookException) {
                onMapValueCompleteListener.onError(facebookException);
            }
        });
    }

    private <T> void stageCollectionValues(CollectionMapper.Collection<T> collection, OnMapperCompleteListener onMapperCompleteListener) {
        CollectionMapper.iterate(collection, new ValueMapper() {
            public void mapValue(Object obj, OnMapValueCompleteListener onMapValueCompleteListener) {
                if (obj instanceof ArrayList) {
                    ShareApi.this.stageArrayList((ArrayList) obj, onMapValueCompleteListener);
                } else if (obj instanceof ShareOpenGraphObject) {
                    ShareApi.this.stageOpenGraphObject((ShareOpenGraphObject) obj, onMapValueCompleteListener);
                } else if (obj instanceof SharePhoto) {
                    ShareApi.this.stagePhoto((SharePhoto) obj, onMapValueCompleteListener);
                } else {
                    onMapValueCompleteListener.onComplete(obj);
                }
            }
        }, onMapperCompleteListener);
    }

    private void stageOpenGraphAction(final Bundle bundle, OnMapperCompleteListener onMapperCompleteListener) {
        stageCollectionValues(new CollectionMapper.Collection<String>() {
            public Iterator<String> keyIterator() {
                return bundle.keySet().iterator();
            }

            public Object get(String str) {
                return bundle.get(str);
            }

            public void set(String str, Object obj, OnErrorListener onErrorListener) {
                if (!Utility.putJSONValueInBundle(bundle, str, obj)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unexpected value: ");
                    stringBuilder.append(obj.toString());
                    onErrorListener.onError(new FacebookException(stringBuilder.toString()));
                }
            }
        }, onMapperCompleteListener);
    }

    private void stageOpenGraphObject(final ShareOpenGraphObject shareOpenGraphObject, final OnMapValueCompleteListener onMapValueCompleteListener) {
        String string = shareOpenGraphObject.getString("type");
        if (string == null) {
            string = shareOpenGraphObject.getString("og:type");
        }
        final String str = string;
        if (str == null) {
            onMapValueCompleteListener.onError(new FacebookException("Open Graph objects must contain a type value."));
            return;
        }
        final JSONObject jSONObject = new JSONObject();
        AnonymousClass9 anonymousClass9 = new CollectionMapper.Collection<String>() {
            public Iterator<String> keyIterator() {
                return shareOpenGraphObject.keySet().iterator();
            }

            public Object get(String str) {
                return shareOpenGraphObject.get(str);
            }

            public void set(String str, Object obj, OnErrorListener onErrorListener) {
                try {
                    jSONObject.put(str, obj);
                } catch (JSONException e) {
                    str = e.getLocalizedMessage();
                    if (str == null) {
                        str = "Error staging object.";
                    }
                    onErrorListener.onError(new FacebookException(str));
                }
            }
        };
        final AnonymousClass10 anonymousClass10 = new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                FacebookRequestError error = graphResponse.getError();
                String errorMessage;
                if (error != null) {
                    errorMessage = error.getErrorMessage();
                    if (errorMessage == null) {
                        errorMessage = "Error staging Open Graph object.";
                    }
                    onMapValueCompleteListener.onError(new FacebookGraphResponseException(graphResponse, errorMessage));
                    return;
                }
                JSONObject jSONObject = graphResponse.getJSONObject();
                if (jSONObject == null) {
                    onMapValueCompleteListener.onError(new FacebookGraphResponseException(graphResponse, "Error staging Open Graph object."));
                    return;
                }
                errorMessage = jSONObject.optString(ShareConstants.WEB_DIALOG_PARAM_ID);
                if (errorMessage == null) {
                    onMapValueCompleteListener.onError(new FacebookGraphResponseException(graphResponse, "Error staging Open Graph object."));
                } else {
                    onMapValueCompleteListener.onComplete(errorMessage);
                }
            }
        };
        final OnMapValueCompleteListener onMapValueCompleteListener2 = onMapValueCompleteListener;
        stageCollectionValues(anonymousClass9, new OnMapperCompleteListener() {
            public void onComplete() {
                String jSONObject = jSONObject.toString();
                Bundle bundle = new Bundle();
                bundle.putString("object", jSONObject);
                try {
                    AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                    ShareApi shareApi = ShareApi.this;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("objects/");
                    stringBuilder.append(URLEncoder.encode(str, "UTF-8"));
                    new GraphRequest(currentAccessToken, shareApi.getGraphPath(stringBuilder.toString()), bundle, HttpMethod.POST, anonymousClass10).executeAsync();
                } catch (UnsupportedEncodingException e) {
                    jSONObject = e.getLocalizedMessage();
                    if (jSONObject == null) {
                        jSONObject = "Error staging Open Graph object.";
                    }
                    onMapValueCompleteListener2.onError(new FacebookException(jSONObject));
                }
            }

            public void onError(FacebookException facebookException) {
                onMapValueCompleteListener2.onError(facebookException);
            }
        });
    }

    private void stagePhoto(final SharePhoto sharePhoto, final OnMapValueCompleteListener onMapValueCompleteListener) {
        Bitmap bitmap = sharePhoto.getBitmap();
        Uri imageUrl = sharePhoto.getImageUrl();
        if (bitmap == null && imageUrl == null) {
            onMapValueCompleteListener.onError(new FacebookException("Photos must have an imageURL or bitmap."));
            return;
        }
        Callback anonymousClass12 = new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                FacebookRequestError error = graphResponse.getError();
                if (error != null) {
                    String errorMessage = error.getErrorMessage();
                    if (errorMessage == null) {
                        errorMessage = "Error staging photo.";
                    }
                    onMapValueCompleteListener.onError(new FacebookGraphResponseException(graphResponse, errorMessage));
                    return;
                }
                JSONObject jSONObject = graphResponse.getJSONObject();
                if (jSONObject == null) {
                    onMapValueCompleteListener.onError(new FacebookException("Error staging photo."));
                    return;
                }
                String optString = jSONObject.optString(ShareConstants.MEDIA_URI);
                if (optString == null) {
                    onMapValueCompleteListener.onError(new FacebookException("Error staging photo."));
                    return;
                }
                JSONObject jSONObject2 = new JSONObject();
                try {
                    jSONObject2.put("url", optString);
                    jSONObject2.put(NativeProtocol.IMAGE_USER_GENERATED_KEY, sharePhoto.getUserGenerated());
                    onMapValueCompleteListener.onComplete(jSONObject2);
                } catch (JSONException e) {
                    optString = e.getLocalizedMessage();
                    if (optString == null) {
                        optString = "Error staging photo.";
                    }
                    onMapValueCompleteListener.onError(new FacebookException(optString));
                }
            }
        };
        if (bitmap != null) {
            ShareInternalUtility.newUploadStagingResourceWithImageRequest(AccessToken.getCurrentAccessToken(), bitmap, anonymousClass12).executeAsync();
            return;
        }
        try {
            ShareInternalUtility.newUploadStagingResourceWithImageRequest(AccessToken.getCurrentAccessToken(), imageUrl, anonymousClass12).executeAsync();
        } catch (FileNotFoundException e) {
            String localizedMessage = e.getLocalizedMessage();
            if (localizedMessage == null) {
                localizedMessage = "Error staging photo.";
            }
            onMapValueCompleteListener.onError(new FacebookException(localizedMessage));
        }
    }
}
