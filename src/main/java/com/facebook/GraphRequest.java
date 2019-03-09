package com.facebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Pair;
import com.bumptech.glide.load.Key;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.internal.AttributionIdentifiers;
import com.facebook.internal.InternalSettings;
import com.facebook.internal.Logger;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.share.internal.ShareConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GraphRequest {
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    public static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String ATTACHED_FILES_PARAM = "attached_files";
    private static final String ATTACHMENT_FILENAME_PREFIX = "file";
    private static final String BATCH_APP_ID_PARAM = "batch_app_id";
    private static final String BATCH_BODY_PARAM = "body";
    private static final String BATCH_ENTRY_DEPENDS_ON_PARAM = "depends_on";
    private static final String BATCH_ENTRY_NAME_PARAM = "name";
    private static final String BATCH_ENTRY_OMIT_RESPONSE_ON_SUCCESS_PARAM = "omit_response_on_success";
    private static final String BATCH_METHOD_PARAM = "method";
    private static final String BATCH_PARAM = "batch";
    private static final String BATCH_RELATIVE_URL_PARAM = "relative_url";
    private static final String CAPTION_PARAM = "caption";
    private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String DEBUG_KEY = "__debug__";
    private static final String DEBUG_MESSAGES_KEY = "messages";
    private static final String DEBUG_MESSAGE_KEY = "message";
    private static final String DEBUG_MESSAGE_LINK_KEY = "link";
    private static final String DEBUG_MESSAGE_TYPE_KEY = "type";
    private static final String DEBUG_PARAM = "debug";
    private static final String DEBUG_SEVERITY_INFO = "info";
    private static final String DEBUG_SEVERITY_WARNING = "warning";
    public static final String FIELDS_PARAM = "fields";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_PARAM = "format";
    private static final String GRAPH_PATH_FORMAT = "%s/%s";
    private static final String ISO_8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final int MAXIMUM_BATCH_SIZE = 50;
    private static final String ME = "me";
    private static final String MIME_BOUNDARY;
    private static final String MY_FRIENDS = "me/friends";
    private static final String MY_PHOTOS = "me/photos";
    private static final String PICTURE_PARAM = "picture";
    private static final String SDK_ANDROID = "android";
    private static final String SDK_PARAM = "sdk";
    private static final String SEARCH = "search";
    public static final String TAG = "GraphRequest";
    private static final String USER_AGENT_BASE = "FBAndroidSDK";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String VIDEOS_SUFFIX = "/videos";
    private static String defaultBatchApplicationId;
    private static volatile String userAgent;
    private static Pattern versionPattern = Pattern.compile("^/?v\\d+\\.\\d+/(.*)");
    private AccessToken accessToken;
    private String batchEntryDependsOn;
    private String batchEntryName;
    private boolean batchEntryOmitResultOnSuccess;
    private Callback callback;
    private JSONObject graphObject;
    private String graphPath;
    private HttpMethod httpMethod;
    private String overriddenURL;
    private Bundle parameters;
    private boolean skipClientToken;
    private Object tag;
    private String version;

    private static class Attachment {
        private final GraphRequest request;
        private final Object value;

        public Attachment(GraphRequest graphRequest, Object obj) {
            this.request = graphRequest;
            this.value = obj;
        }

        public GraphRequest getRequest() {
            return this.request;
        }

        public Object getValue() {
            return this.value;
        }
    }

    public interface Callback {
        void onCompleted(GraphResponse graphResponse);
    }

    public interface GraphJSONArrayCallback {
        void onCompleted(JSONArray jSONArray, GraphResponse graphResponse);
    }

    public interface GraphJSONObjectCallback {
        void onCompleted(JSONObject jSONObject, GraphResponse graphResponse);
    }

    private interface KeyValueSerializer {
        void writeString(String str, String str2) throws IOException;
    }

    public static class ParcelableResourceWithMimeType<RESOURCE extends Parcelable> implements Parcelable {
        public static final Creator<ParcelableResourceWithMimeType> CREATOR = new Creator<ParcelableResourceWithMimeType>() {
            public ParcelableResourceWithMimeType createFromParcel(Parcel parcel) {
                return new ParcelableResourceWithMimeType(parcel, null);
            }

            public ParcelableResourceWithMimeType[] newArray(int i) {
                return new ParcelableResourceWithMimeType[i];
            }
        };
        private final String mimeType;
        private final RESOURCE resource;

        public int describeContents() {
            return 1;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public RESOURCE getResource() {
            return this.resource;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.mimeType);
            parcel.writeParcelable(this.resource, i);
        }

        public ParcelableResourceWithMimeType(RESOURCE resource, String str) {
            this.mimeType = str;
            this.resource = resource;
        }

        private ParcelableResourceWithMimeType(Parcel parcel) {
            this.mimeType = parcel.readString();
            this.resource = parcel.readParcelable(FacebookSdk.getApplicationContext().getClassLoader());
        }
    }

    public interface OnProgressCallback extends Callback {
        void onProgress(long j, long j2);
    }

    private static class Serializer implements KeyValueSerializer {
        private boolean firstWrite = true;
        private final Logger logger;
        private final OutputStream outputStream;
        private boolean useUrlEncode = false;

        public Serializer(OutputStream outputStream, Logger logger, boolean z) {
            this.outputStream = outputStream;
            this.logger = logger;
            this.useUrlEncode = z;
        }

        public void writeObject(String str, Object obj, GraphRequest graphRequest) throws IOException {
            if (this.outputStream instanceof RequestOutputStream) {
                ((RequestOutputStream) this.outputStream).setCurrentRequest(graphRequest);
            }
            if (GraphRequest.isSupportedParameterType(obj)) {
                writeString(str, GraphRequest.parameterToString(obj));
            } else if (obj instanceof Bitmap) {
                writeBitmap(str, (Bitmap) obj);
            } else if (obj instanceof byte[]) {
                writeBytes(str, (byte[]) obj);
            } else if (obj instanceof Uri) {
                writeContentUri(str, (Uri) obj, null);
            } else if (obj instanceof ParcelFileDescriptor) {
                writeFile(str, (ParcelFileDescriptor) obj, null);
            } else if (obj instanceof ParcelableResourceWithMimeType) {
                ParcelableResourceWithMimeType parcelableResourceWithMimeType = (ParcelableResourceWithMimeType) obj;
                Parcelable resource = parcelableResourceWithMimeType.getResource();
                String mimeType = parcelableResourceWithMimeType.getMimeType();
                if (resource instanceof ParcelFileDescriptor) {
                    writeFile(str, (ParcelFileDescriptor) resource, mimeType);
                } else if (resource instanceof Uri) {
                    writeContentUri(str, (Uri) resource, mimeType);
                } else {
                    throw getInvalidTypeError();
                }
            } else {
                throw getInvalidTypeError();
            }
        }

        private RuntimeException getInvalidTypeError() {
            return new IllegalArgumentException("value is not a supported type.");
        }

        public void writeRequestsAsJson(String str, JSONArray jSONArray, Collection<GraphRequest> collection) throws IOException, JSONException {
            if (this.outputStream instanceof RequestOutputStream) {
                RequestOutputStream requestOutputStream = (RequestOutputStream) this.outputStream;
                writeContentDisposition(str, null, null);
                write("[", new Object[0]);
                int i = 0;
                for (GraphRequest graphRequest : collection) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    requestOutputStream.setCurrentRequest(graphRequest);
                    if (i > 0) {
                        write(",%s", jSONObject.toString());
                    } else {
                        write("%s", jSONObject.toString());
                    }
                    i++;
                }
                write("]", new Object[0]);
                if (this.logger != null) {
                    Logger logger = this.logger;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("    ");
                    stringBuilder.append(str);
                    logger.appendKeyValue(stringBuilder.toString(), jSONArray.toString());
                }
                return;
            }
            writeString(str, jSONArray.toString());
        }

        public void writeString(String str, String str2) throws IOException {
            writeContentDisposition(str, null, null);
            writeLine("%s", str2);
            writeRecordBoundary();
            if (this.logger != null) {
                Logger logger = this.logger;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("    ");
                stringBuilder.append(str);
                logger.appendKeyValue(stringBuilder.toString(), str2);
            }
        }

        public void writeBitmap(String str, Bitmap bitmap) throws IOException {
            writeContentDisposition(str, str, "image/png");
            bitmap.compress(CompressFormat.PNG, 100, this.outputStream);
            writeLine("", new Object[0]);
            writeRecordBoundary();
            if (this.logger != null) {
                Logger logger = this.logger;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("    ");
                stringBuilder.append(str);
                logger.appendKeyValue(stringBuilder.toString(), "<Image>");
            }
        }

        public void writeBytes(String str, byte[] bArr) throws IOException {
            writeContentDisposition(str, str, "content/unknown");
            this.outputStream.write(bArr);
            writeLine("", new Object[0]);
            writeRecordBoundary();
            if (this.logger != null) {
                Logger logger = this.logger;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("    ");
                stringBuilder.append(str);
                logger.appendKeyValue(stringBuilder.toString(), String.format(Locale.ROOT, "<Data: %d>", new Object[]{Integer.valueOf(bArr.length)}));
            }
        }

        public void writeContentUri(String str, Uri uri, String str2) throws IOException {
            int i;
            if (str2 == null) {
                str2 = "content/unknown";
            }
            writeContentDisposition(str, str, str2);
            if (this.outputStream instanceof ProgressNoopOutputStream) {
                ((ProgressNoopOutputStream) this.outputStream).addProgress(Utility.getContentSize(uri));
                i = 0;
            } else {
                i = Utility.copyAndCloseInputStream(FacebookSdk.getApplicationContext().getContentResolver().openInputStream(uri), this.outputStream) + 0;
            }
            writeLine("", new Object[0]);
            writeRecordBoundary();
            if (this.logger != null) {
                Logger logger = this.logger;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("    ");
                stringBuilder.append(str);
                logger.appendKeyValue(stringBuilder.toString(), String.format(Locale.ROOT, "<Data: %d>", new Object[]{Integer.valueOf(i)}));
            }
        }

        public void writeFile(String str, ParcelFileDescriptor parcelFileDescriptor, String str2) throws IOException {
            int i;
            if (str2 == null) {
                str2 = "content/unknown";
            }
            writeContentDisposition(str, str, str2);
            if (this.outputStream instanceof ProgressNoopOutputStream) {
                ((ProgressNoopOutputStream) this.outputStream).addProgress(parcelFileDescriptor.getStatSize());
                i = 0;
            } else {
                i = Utility.copyAndCloseInputStream(new AutoCloseInputStream(parcelFileDescriptor), this.outputStream) + 0;
            }
            writeLine("", new Object[0]);
            writeRecordBoundary();
            if (this.logger != null) {
                Logger logger = this.logger;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("    ");
                stringBuilder.append(str);
                logger.appendKeyValue(stringBuilder.toString(), String.format(Locale.ROOT, "<Data: %d>", new Object[]{Integer.valueOf(i)}));
            }
        }

        public void writeRecordBoundary() throws IOException {
            if (this.useUrlEncode) {
                this.outputStream.write("&".getBytes());
                return;
            }
            writeLine("--%s", GraphRequest.MIME_BOUNDARY);
        }

        public void writeContentDisposition(String str, String str2, String str3) throws IOException {
            if (this.useUrlEncode) {
                this.outputStream.write(String.format("%s=", new Object[]{str}).getBytes());
                return;
            }
            write("Content-Disposition: form-data; name=\"%s\"", str);
            if (str2 != null) {
                write("; filename=\"%s\"", str2);
            }
            writeLine("", new Object[0]);
            if (str3 != null) {
                writeLine("%s: %s", GraphRequest.CONTENT_TYPE_HEADER, str3);
            }
            writeLine("", new Object[0]);
        }

        public void write(String str, Object... objArr) throws IOException {
            if (this.useUrlEncode) {
                this.outputStream.write(URLEncoder.encode(String.format(Locale.US, str, objArr), Key.STRING_CHARSET_NAME).getBytes());
                return;
            }
            if (this.firstWrite) {
                this.outputStream.write("--".getBytes());
                this.outputStream.write(GraphRequest.MIME_BOUNDARY.getBytes());
                this.outputStream.write("\r\n".getBytes());
                this.firstWrite = false;
            }
            this.outputStream.write(String.format(str, objArr).getBytes());
        }

        public void writeLine(String str, Object... objArr) throws IOException {
            write(str, objArr);
            if (!this.useUrlEncode) {
                write("\r\n", new Object[0]);
            }
        }
    }

    private static String getDefaultPhotoPathIfNull(String str) {
        return str == null ? "me/photos" : str;
    }

    static {
        char[] toCharArray = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        int nextInt = secureRandom.nextInt(11) + 30;
        for (int i = 0; i < nextInt; i++) {
            stringBuilder.append(toCharArray[secureRandom.nextInt(toCharArray.length)]);
        }
        MIME_BOUNDARY = stringBuilder.toString();
    }

    public GraphRequest() {
        this(null, null, null, null, null);
    }

    public GraphRequest(AccessToken accessToken, String str) {
        this(accessToken, str, null, null, null);
    }

    public GraphRequest(AccessToken accessToken, String str, Bundle bundle, HttpMethod httpMethod) {
        this(accessToken, str, bundle, httpMethod, null);
    }

    public GraphRequest(AccessToken accessToken, String str, Bundle bundle, HttpMethod httpMethod, Callback callback) {
        this(accessToken, str, bundle, httpMethod, callback, null);
    }

    public GraphRequest(AccessToken accessToken, String str, Bundle bundle, HttpMethod httpMethod, Callback callback, String str2) {
        this.batchEntryOmitResultOnSuccess = true;
        this.skipClientToken = false;
        this.accessToken = accessToken;
        this.graphPath = str;
        this.version = str2;
        setCallback(callback);
        setHttpMethod(httpMethod);
        if (bundle != null) {
            this.parameters = new Bundle(bundle);
        } else {
            this.parameters = new Bundle();
        }
        if (this.version == null) {
            this.version = FacebookSdk.getGraphApiVersion();
        }
    }

    GraphRequest(AccessToken accessToken, URL url) {
        this.batchEntryOmitResultOnSuccess = true;
        this.skipClientToken = false;
        this.accessToken = accessToken;
        this.overriddenURL = url.toString();
        setHttpMethod(HttpMethod.GET);
        this.parameters = new Bundle();
    }

    public static GraphRequest newDeleteObjectRequest(AccessToken accessToken, String str, Callback callback) {
        return new GraphRequest(accessToken, str, null, HttpMethod.DELETE, callback);
    }

    public static GraphRequest newMeRequest(AccessToken accessToken, final GraphJSONObjectCallback graphJSONObjectCallback) {
        return new GraphRequest(accessToken, ME, null, null, new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                if (graphJSONObjectCallback != null) {
                    graphJSONObjectCallback.onCompleted(graphResponse.getJSONObject(), graphResponse);
                }
            }
        });
    }

    public static GraphRequest newPostRequest(AccessToken accessToken, String str, JSONObject jSONObject, Callback callback) {
        GraphRequest graphRequest = new GraphRequest(accessToken, str, null, HttpMethod.POST, callback);
        graphRequest.setGraphObject(jSONObject);
        return graphRequest;
    }

    public static GraphRequest newMyFriendsRequest(AccessToken accessToken, final GraphJSONArrayCallback graphJSONArrayCallback) {
        return new GraphRequest(accessToken, MY_FRIENDS, null, null, new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                if (graphJSONArrayCallback != null) {
                    JSONObject jSONObject = graphResponse.getJSONObject();
                    graphJSONArrayCallback.onCompleted(jSONObject != null ? jSONObject.optJSONArray(ShareConstants.WEB_DIALOG_PARAM_DATA) : null, graphResponse);
                }
            }
        });
    }

    public static GraphRequest newGraphPathRequest(AccessToken accessToken, String str, Callback callback) {
        return new GraphRequest(accessToken, str, null, null, callback);
    }

    public static GraphRequest newPlacesSearchRequest(AccessToken accessToken, Location location, int i, int i2, String str, final GraphJSONArrayCallback graphJSONArrayCallback) {
        if (location == null && Utility.isNullOrEmpty(str)) {
            throw new FacebookException("Either location or searchText must be specified.");
        }
        Bundle bundle = new Bundle(5);
        bundle.putString("type", "place");
        bundle.putInt("limit", i2);
        if (location != null) {
            bundle.putString("center", String.format(Locale.US, "%f,%f", new Object[]{Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude())}));
            bundle.putInt("distance", i);
        }
        if (!Utility.isNullOrEmpty(str)) {
            bundle.putString("q", str);
        }
        return new GraphRequest(accessToken, SEARCH, bundle, HttpMethod.GET, new Callback() {
            public void onCompleted(GraphResponse graphResponse) {
                if (graphJSONArrayCallback != null) {
                    JSONObject jSONObject = graphResponse.getJSONObject();
                    graphJSONArrayCallback.onCompleted(jSONObject != null ? jSONObject.optJSONArray(ShareConstants.WEB_DIALOG_PARAM_DATA) : null, graphResponse);
                }
            }
        });
    }

    public static GraphRequest newUploadPhotoRequest(AccessToken accessToken, String str, Bitmap bitmap, String str2, Bundle bundle, Callback callback) {
        String defaultPhotoPathIfNull = getDefaultPhotoPathIfNull(str);
        Bundle bundle2 = new Bundle();
        if (bundle != null) {
            bundle2.putAll(bundle);
        }
        bundle2.putParcelable("picture", bitmap);
        if (!(str2 == null || str2.isEmpty())) {
            bundle2.putString("caption", str2);
        }
        return new GraphRequest(accessToken, defaultPhotoPathIfNull, bundle2, HttpMethod.POST, callback);
    }

    public static GraphRequest newUploadPhotoRequest(AccessToken accessToken, String str, File file, String str2, Bundle bundle, Callback callback) throws FileNotFoundException {
        String defaultPhotoPathIfNull = getDefaultPhotoPathIfNull(str);
        ParcelFileDescriptor open = ParcelFileDescriptor.open(file, 268435456);
        Bundle bundle2 = new Bundle();
        if (bundle != null) {
            bundle2.putAll(bundle);
        }
        bundle2.putParcelable("picture", open);
        if (!(str2 == null || str2.isEmpty())) {
            bundle2.putString("caption", str2);
        }
        return new GraphRequest(accessToken, defaultPhotoPathIfNull, bundle2, HttpMethod.POST, callback);
    }

    public static GraphRequest newUploadPhotoRequest(AccessToken accessToken, String str, Uri uri, String str2, Bundle bundle, Callback callback) throws FileNotFoundException {
        String defaultPhotoPathIfNull = getDefaultPhotoPathIfNull(str);
        if (Utility.isFileUri(uri)) {
            return newUploadPhotoRequest(accessToken, defaultPhotoPathIfNull, new File(uri.getPath()), str2, bundle, callback);
        }
        if (Utility.isContentUri(uri)) {
            Bundle bundle2 = new Bundle();
            if (bundle != null) {
                bundle2.putAll(bundle);
            }
            bundle2.putParcelable("picture", uri);
            if (!(str2 == null || str2.isEmpty())) {
                bundle2.putString("caption", str2);
            }
            return new GraphRequest(accessToken, defaultPhotoPathIfNull, bundle2, HttpMethod.POST, callback);
        }
        throw new FacebookException("The photo Uri must be either a file:// or content:// Uri");
    }

    public static GraphRequest newCustomAudienceThirdPartyIdRequest(AccessToken accessToken, Context context, String str, Callback callback) {
        if (str == null && accessToken != null) {
            str = accessToken.getApplicationId();
        }
        if (str == null) {
            str = Utility.getMetadataApplicationId(context);
        }
        if (str != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append("/custom_audience_third_party_id");
            String stringBuilder2 = stringBuilder.toString();
            AttributionIdentifiers attributionIdentifiers = AttributionIdentifiers.getAttributionIdentifiers(context);
            Bundle bundle = new Bundle();
            if (accessToken == null) {
                if (attributionIdentifiers != null) {
                    String attributionId;
                    if (attributionIdentifiers.getAttributionId() != null) {
                        attributionId = attributionIdentifiers.getAttributionId();
                    } else {
                        attributionId = attributionIdentifiers.getAndroidAdvertiserId();
                    }
                    if (attributionIdentifiers.getAttributionId() != null) {
                        bundle.putString("udid", attributionId);
                    }
                } else {
                    throw new FacebookException("There is no access token and attribution identifiers could not be retrieved");
                }
            }
            if (FacebookSdk.getLimitEventAndDataUsage(context) || (attributionIdentifiers != null && attributionIdentifiers.isTrackingLimited())) {
                bundle.putString("limit_event_usage", AppEventsConstants.EVENT_PARAM_VALUE_YES);
            }
            return new GraphRequest(accessToken, stringBuilder2, bundle, HttpMethod.GET, callback);
        }
        throw new FacebookException("Facebook App ID cannot be determined");
    }

    public static GraphRequest newCustomAudienceThirdPartyIdRequest(AccessToken accessToken, Context context, Callback callback) {
        return newCustomAudienceThirdPartyIdRequest(accessToken, context, null, callback);
    }

    public final JSONObject getGraphObject() {
        return this.graphObject;
    }

    public final void setGraphObject(JSONObject jSONObject) {
        this.graphObject = jSONObject;
    }

    public final String getGraphPath() {
        return this.graphPath;
    }

    public final void setGraphPath(String str) {
        this.graphPath = str;
    }

    public final HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    public final void setHttpMethod(HttpMethod httpMethod) {
        if (this.overriddenURL == null || httpMethod == HttpMethod.GET) {
            if (httpMethod == null) {
                httpMethod = HttpMethod.GET;
            }
            this.httpMethod = httpMethod;
            return;
        }
        throw new FacebookException("Can't change HTTP method on request with overridden URL.");
    }

    public final String getVersion() {
        return this.version;
    }

    public final void setVersion(String str) {
        this.version = str;
    }

    public final void setSkipClientToken(boolean z) {
        this.skipClientToken = z;
    }

    public final Bundle getParameters() {
        return this.parameters;
    }

    public final void setParameters(Bundle bundle) {
        this.parameters = bundle;
    }

    public final AccessToken getAccessToken() {
        return this.accessToken;
    }

    public final void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public final String getBatchEntryName() {
        return this.batchEntryName;
    }

    public final void setBatchEntryName(String str) {
        this.batchEntryName = str;
    }

    public final String getBatchEntryDependsOn() {
        return this.batchEntryDependsOn;
    }

    public final void setBatchEntryDependsOn(String str) {
        this.batchEntryDependsOn = str;
    }

    public final boolean getBatchEntryOmitResultOnSuccess() {
        return this.batchEntryOmitResultOnSuccess;
    }

    public final void setBatchEntryOmitResultOnSuccess(boolean z) {
        this.batchEntryOmitResultOnSuccess = z;
    }

    public static final String getDefaultBatchApplicationId() {
        return defaultBatchApplicationId;
    }

    public static final void setDefaultBatchApplicationId(String str) {
        defaultBatchApplicationId = str;
    }

    public final Callback getCallback() {
        return this.callback;
    }

    public final void setCallback(final Callback callback) {
        if (FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.GRAPH_API_DEBUG_INFO) || FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.GRAPH_API_DEBUG_WARNING)) {
            this.callback = new Callback() {
                public void onCompleted(GraphResponse graphResponse) {
                    JSONObject jSONObject = graphResponse.getJSONObject();
                    jSONObject = jSONObject != null ? jSONObject.optJSONObject(GraphRequest.DEBUG_KEY) : null;
                    JSONArray optJSONArray = jSONObject != null ? jSONObject.optJSONArray(GraphRequest.DEBUG_MESSAGES_KEY) : null;
                    if (optJSONArray != null) {
                        for (int i = 0; i < optJSONArray.length(); i++) {
                            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                            String optString = optJSONObject != null ? optJSONObject.optString("message") : null;
                            String optString2 = optJSONObject != null ? optJSONObject.optString("type") : null;
                            String optString3 = optJSONObject != null ? optJSONObject.optString("link") : null;
                            if (!(optString == null || optString2 == null)) {
                                LoggingBehavior loggingBehavior = LoggingBehavior.GRAPH_API_DEBUG_INFO;
                                if (optString2.equals(GraphRequest.DEBUG_SEVERITY_WARNING)) {
                                    loggingBehavior = LoggingBehavior.GRAPH_API_DEBUG_WARNING;
                                }
                                if (!Utility.isNullOrEmpty(optString3)) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append(optString);
                                    stringBuilder.append(" Link: ");
                                    stringBuilder.append(optString3);
                                    optString = stringBuilder.toString();
                                }
                                Logger.log(loggingBehavior, GraphRequest.TAG, optString);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onCompleted(graphResponse);
                    }
                }
            };
        } else {
            this.callback = callback;
        }
    }

    public final void setTag(Object obj) {
        this.tag = obj;
    }

    public final Object getTag() {
        return this.tag;
    }

    public final GraphResponse executeAndWait() {
        return executeAndWait(this);
    }

    public final GraphRequestAsyncTask executeAsync() {
        return executeBatchAsync(this);
    }

    public static HttpURLConnection toHttpConnection(GraphRequest... graphRequestArr) {
        return toHttpConnection(Arrays.asList(graphRequestArr));
    }

    public static HttpURLConnection toHttpConnection(Collection<GraphRequest> collection) {
        Validate.notEmptyAndContainsNoNulls(collection, "requests");
        return toHttpConnection(new GraphRequestBatch((Collection) collection));
    }

    public static HttpURLConnection toHttpConnection(GraphRequestBatch graphRequestBatch) {
        Throwable e;
        validateFieldsParamForGetRequests(graphRequestBatch);
        try {
            URL url;
            if (graphRequestBatch.size() == 1) {
                url = new URL(graphRequestBatch.get(0).getUrlForSingleRequest());
            } else {
                url = new URL(ServerProtocol.getGraphUrlBase());
            }
            URLConnection uRLConnection = null;
            try {
                URLConnection createConnection = createConnection(url);
                try {
                    serializeToUrlConnection(graphRequestBatch, createConnection);
                    return createConnection;
                } catch (IOException | JSONException e2) {
                    e = e2;
                    uRLConnection = createConnection;
                    Utility.disconnectQuietly(uRLConnection);
                    throw new FacebookException("could not construct request body", e);
                }
            } catch (IOException | JSONException e3) {
                e = e3;
                Utility.disconnectQuietly(uRLConnection);
                throw new FacebookException("could not construct request body", e);
            }
        } catch (MalformedURLException e4) {
            throw new FacebookException("could not construct URL for request", e4);
        }
    }

    public static GraphResponse executeAndWait(GraphRequest graphRequest) {
        List executeBatchAndWait = executeBatchAndWait(graphRequest);
        if (executeBatchAndWait != null && executeBatchAndWait.size() == 1) {
            return (GraphResponse) executeBatchAndWait.get(0);
        }
        throw new FacebookException("invalid state: expected a single response");
    }

    public static List<GraphResponse> executeBatchAndWait(GraphRequest... graphRequestArr) {
        Validate.notNull(graphRequestArr, "requests");
        return executeBatchAndWait(Arrays.asList(graphRequestArr));
    }

    public static List<GraphResponse> executeBatchAndWait(Collection<GraphRequest> collection) {
        return executeBatchAndWait(new GraphRequestBatch((Collection) collection));
    }

    public static List<GraphResponse> executeBatchAndWait(GraphRequestBatch graphRequestBatch) {
        Throwable th;
        Validate.notEmptyAndContainsNoNulls(graphRequestBatch, "requests");
        URLConnection uRLConnection = null;
        try {
            URLConnection toHttpConnection = toHttpConnection(graphRequestBatch);
            try {
                List executeConnectionAndWait = executeConnectionAndWait((HttpURLConnection) toHttpConnection, graphRequestBatch);
                Utility.disconnectQuietly(toHttpConnection);
                return executeConnectionAndWait;
            } catch (Throwable th2) {
                th = th2;
                uRLConnection = toHttpConnection;
                Utility.disconnectQuietly(uRLConnection);
                throw th;
            }
        } catch (Exception e) {
            List constructErrorResponses = GraphResponse.constructErrorResponses(graphRequestBatch.getRequests(), null, new FacebookException(e));
            runCallbacks(graphRequestBatch, constructErrorResponses);
            Utility.disconnectQuietly(null);
            return constructErrorResponses;
        } catch (Throwable th3) {
            th = th3;
            Utility.disconnectQuietly(uRLConnection);
            throw th;
        }
    }

    public static GraphRequestAsyncTask executeBatchAsync(GraphRequest... graphRequestArr) {
        Validate.notNull(graphRequestArr, "requests");
        return executeBatchAsync(Arrays.asList(graphRequestArr));
    }

    public static GraphRequestAsyncTask executeBatchAsync(Collection<GraphRequest> collection) {
        return executeBatchAsync(new GraphRequestBatch((Collection) collection));
    }

    public static GraphRequestAsyncTask executeBatchAsync(GraphRequestBatch graphRequestBatch) {
        Validate.notEmptyAndContainsNoNulls(graphRequestBatch, "requests");
        GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequestAsyncTask(graphRequestBatch);
        graphRequestAsyncTask.executeOnExecutor(FacebookSdk.getExecutor(), new Void[0]);
        return graphRequestAsyncTask;
    }

    public static List<GraphResponse> executeConnectionAndWait(HttpURLConnection httpURLConnection, Collection<GraphRequest> collection) {
        return executeConnectionAndWait(httpURLConnection, new GraphRequestBatch((Collection) collection));
    }

    public static List<GraphResponse> executeConnectionAndWait(HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) {
        List fromHttpConnection = GraphResponse.fromHttpConnection(httpURLConnection, graphRequestBatch);
        Utility.disconnectQuietly(httpURLConnection);
        if (graphRequestBatch.size() == fromHttpConnection.size()) {
            runCallbacks(graphRequestBatch, fromHttpConnection);
            AccessTokenManager.getInstance().extendAccessTokenIfNeeded();
            return fromHttpConnection;
        }
        throw new FacebookException(String.format(Locale.US, "Received %d responses while expecting %d", new Object[]{Integer.valueOf(fromHttpConnection.size()), Integer.valueOf(r4)}));
    }

    public static GraphRequestAsyncTask executeConnectionAsync(HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) {
        return executeConnectionAsync(null, httpURLConnection, graphRequestBatch);
    }

    public static GraphRequestAsyncTask executeConnectionAsync(Handler handler, HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) {
        Validate.notNull(httpURLConnection, "connection");
        GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequestAsyncTask(httpURLConnection, graphRequestBatch);
        graphRequestBatch.setCallbackHandler(handler);
        graphRequestAsyncTask.executeOnExecutor(FacebookSdk.getExecutor(), new Void[0]);
        return graphRequestAsyncTask;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{Request: ");
        stringBuilder.append(" accessToken: ");
        stringBuilder.append(this.accessToken == null ? "null" : this.accessToken);
        stringBuilder.append(", graphPath: ");
        stringBuilder.append(this.graphPath);
        stringBuilder.append(", graphObject: ");
        stringBuilder.append(this.graphObject);
        stringBuilder.append(", httpMethod: ");
        stringBuilder.append(this.httpMethod);
        stringBuilder.append(", parameters: ");
        stringBuilder.append(this.parameters);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    static void runCallbacks(final GraphRequestBatch graphRequestBatch, List<GraphResponse> list) {
        int size = graphRequestBatch.size();
        final ArrayList arrayList = new ArrayList();
        for (int i = 0; i < size; i++) {
            GraphRequest graphRequest = graphRequestBatch.get(i);
            if (graphRequest.callback != null) {
                arrayList.add(new Pair(graphRequest.callback, list.get(i)));
            }
        }
        if (arrayList.size() > 0) {
            AnonymousClass5 anonymousClass5 = new Runnable() {
                public void run() {
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        Pair pair = (Pair) it.next();
                        ((Callback) pair.first).onCompleted((GraphResponse) pair.second);
                    }
                    for (com.facebook.GraphRequestBatch.Callback onBatchCompleted : graphRequestBatch.getCallbacks()) {
                        onBatchCompleted.onBatchCompleted(graphRequestBatch);
                    }
                }
            };
            Handler callbackHandler = graphRequestBatch.getCallbackHandler();
            if (callbackHandler == null) {
                anonymousClass5.run();
            } else {
                callbackHandler.post(anonymousClass5);
            }
        }
    }

    private static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty(USER_AGENT_HEADER, getUserAgent());
        httpURLConnection.setRequestProperty(ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());
        httpURLConnection.setChunkedStreamingMode(0);
        return httpURLConnection;
    }

    private void addCommonParameters() {
        String token;
        if (this.accessToken != null) {
            if (!this.parameters.containsKey("access_token")) {
                token = this.accessToken.getToken();
                Logger.registerAccessToken(token);
                this.parameters.putString("access_token", token);
            }
        } else if (!(this.skipClientToken || this.parameters.containsKey("access_token"))) {
            token = FacebookSdk.getApplicationId();
            String clientToken = FacebookSdk.getClientToken();
            if (Utility.isNullOrEmpty(token) || Utility.isNullOrEmpty(clientToken)) {
                Utility.logd(TAG, "Warning: Request without access token missing application ID or client token.");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(token);
                stringBuilder.append("|");
                stringBuilder.append(clientToken);
                this.parameters.putString("access_token", stringBuilder.toString());
            }
        }
        this.parameters.putString("sdk", "android");
        this.parameters.putString(FORMAT_PARAM, FORMAT_JSON);
        if (FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.GRAPH_API_DEBUG_INFO)) {
            this.parameters.putString("debug", DEBUG_SEVERITY_INFO);
        } else if (FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.GRAPH_API_DEBUG_WARNING)) {
            this.parameters.putString("debug", DEBUG_SEVERITY_WARNING);
        }
    }

    private String appendParametersToBaseUrl(String str, Boolean bool) {
        if (!bool.booleanValue() && this.httpMethod == HttpMethod.POST) {
            return str;
        }
        Builder buildUpon = Uri.parse(str).buildUpon();
        for (String str2 : this.parameters.keySet()) {
            Object obj = this.parameters.get(str2);
            if (obj == null) {
                obj = "";
            }
            if (isSupportedParameterType(obj)) {
                buildUpon.appendQueryParameter(str2, parameterToString(obj).toString());
            } else if (this.httpMethod == HttpMethod.GET) {
                throw new IllegalArgumentException(String.format(Locale.US, "Unsupported parameter type for GET request: %s", new Object[]{obj.getClass().getSimpleName()}));
            }
        }
        return buildUpon.toString();
    }

    /* Access modifiers changed, original: final */
    public final String getRelativeUrlForBatchedRequest() {
        if (this.overriddenURL == null) {
            String format = String.format(GRAPH_PATH_FORMAT, new Object[]{ServerProtocol.getGraphUrlBase(), getGraphPathWithVersion()});
            addCommonParameters();
            Uri parse = Uri.parse(appendParametersToBaseUrl(format, Boolean.valueOf(true)));
            return String.format("%s?%s", new Object[]{parse.getPath(), parse.getQuery()});
        }
        throw new FacebookException("Can't override URL for a batch request");
    }

    /* Access modifiers changed, original: final */
    public final String getUrlForSingleRequest() {
        if (this.overriddenURL != null) {
            return this.overriddenURL.toString();
        }
        String graphVideoUrlBase;
        if (getHttpMethod() == HttpMethod.POST && this.graphPath != null && this.graphPath.endsWith(VIDEOS_SUFFIX)) {
            graphVideoUrlBase = ServerProtocol.getGraphVideoUrlBase();
        } else {
            graphVideoUrlBase = ServerProtocol.getGraphUrlBase();
        }
        graphVideoUrlBase = String.format(GRAPH_PATH_FORMAT, new Object[]{graphVideoUrlBase, getGraphPathWithVersion()});
        addCommonParameters();
        return appendParametersToBaseUrl(graphVideoUrlBase, Boolean.valueOf(false));
    }

    private String getGraphPathWithVersion() {
        if (versionPattern.matcher(this.graphPath).matches()) {
            return this.graphPath;
        }
        return String.format(GRAPH_PATH_FORMAT, new Object[]{this.version, this.graphPath});
    }

    private void serializeToBatch(JSONArray jSONArray, Map<String, Attachment> map) throws JSONException, IOException {
        JSONObject jSONObject = new JSONObject();
        if (this.batchEntryName != null) {
            jSONObject.put("name", this.batchEntryName);
            jSONObject.put(BATCH_ENTRY_OMIT_RESPONSE_ON_SUCCESS_PARAM, this.batchEntryOmitResultOnSuccess);
        }
        if (this.batchEntryDependsOn != null) {
            jSONObject.put(BATCH_ENTRY_DEPENDS_ON_PARAM, this.batchEntryDependsOn);
        }
        String relativeUrlForBatchedRequest = getRelativeUrlForBatchedRequest();
        jSONObject.put(BATCH_RELATIVE_URL_PARAM, relativeUrlForBatchedRequest);
        jSONObject.put(BATCH_METHOD_PARAM, this.httpMethod);
        if (this.accessToken != null) {
            Logger.registerAccessToken(this.accessToken.getToken());
        }
        ArrayList arrayList = new ArrayList();
        for (String str : this.parameters.keySet()) {
            Object obj = this.parameters.get(str);
            if (isSupportedAttachmentType(obj)) {
                String format = String.format(Locale.ROOT, "%s%d", new Object[]{ATTACHMENT_FILENAME_PREFIX, Integer.valueOf(map.size())});
                arrayList.add(format);
                map.put(format, new Attachment(this, obj));
            }
        }
        if (!arrayList.isEmpty()) {
            jSONObject.put(ATTACHED_FILES_PARAM, TextUtils.join(",", arrayList));
        }
        if (this.graphObject != null) {
            final ArrayList arrayList2 = new ArrayList();
            processGraphObject(this.graphObject, relativeUrlForBatchedRequest, new KeyValueSerializer() {
                public void writeString(String str, String str2) throws IOException {
                    arrayList2.add(String.format(Locale.US, "%s=%s", new Object[]{str, URLEncoder.encode(str2, Key.STRING_CHARSET_NAME)}));
                }
            });
            jSONObject.put(BATCH_BODY_PARAM, TextUtils.join("&", arrayList2));
        }
        jSONArray.put(jSONObject);
    }

    private static boolean hasOnProgressCallbacks(GraphRequestBatch graphRequestBatch) {
        for (com.facebook.GraphRequestBatch.Callback callback : graphRequestBatch.getCallbacks()) {
            if (callback instanceof com.facebook.GraphRequestBatch.OnProgressCallback) {
                return true;
            }
        }
        Iterator it = graphRequestBatch.iterator();
        while (it.hasNext()) {
            if (((GraphRequest) it.next()).getCallback() instanceof OnProgressCallback) {
                return true;
            }
        }
        return false;
    }

    private static void setConnectionContentType(HttpURLConnection httpURLConnection, boolean z) {
        if (z) {
            httpURLConnection.setRequestProperty(CONTENT_TYPE_HEADER, "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty(CONTENT_ENCODING_HEADER, "gzip");
            return;
        }
        httpURLConnection.setRequestProperty(CONTENT_TYPE_HEADER, getMimeContentType());
    }

    private static boolean isGzipCompressible(GraphRequestBatch graphRequestBatch) {
        Iterator it = graphRequestBatch.iterator();
        while (it.hasNext()) {
            GraphRequest graphRequest = (GraphRequest) it.next();
            for (String str : graphRequest.parameters.keySet()) {
                if (isSupportedAttachmentType(graphRequest.parameters.get(str))) {
                    return false;
                }
            }
        }
        return true;
    }

    static final boolean shouldWarnOnMissingFieldsParam(GraphRequest graphRequest) {
        String version = graphRequest.getVersion();
        boolean z = true;
        if (Utility.isNullOrEmpty(version)) {
            return true;
        }
        if (version.startsWith("v")) {
            version = version.substring(1);
        }
        String[] split = version.split("\\.");
        if ((split.length < 2 || Integer.parseInt(split[0]) <= 2) && (Integer.parseInt(split[0]) < 2 || Integer.parseInt(split[1]) < 4)) {
            z = false;
        }
        return z;
    }

    static final void validateFieldsParamForGetRequests(GraphRequestBatch graphRequestBatch) {
        Iterator it = graphRequestBatch.iterator();
        while (it.hasNext()) {
            GraphRequest graphRequest = (GraphRequest) it.next();
            if (HttpMethod.GET.equals(graphRequest.getHttpMethod()) && shouldWarnOnMissingFieldsParam(graphRequest)) {
                Bundle parameters = graphRequest.getParameters();
                if (!parameters.containsKey(FIELDS_PARAM) || Utility.isNullOrEmpty(parameters.getString(FIELDS_PARAM))) {
                    Logger.log(LoggingBehavior.DEVELOPER_ERRORS, 5, "Request", "starting with Graph API v2.4, GET requests for /%s should contain an explicit \"fields\" parameter.", graphRequest.getGraphPath());
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ce  */
    static final void serializeToUrlConnection(com.facebook.GraphRequestBatch r13, java.net.HttpURLConnection r14) throws java.io.IOException, org.json.JSONException {
        /*
        r6 = new com.facebook.internal.Logger;
        r0 = com.facebook.LoggingBehavior.REQUESTS;
        r1 = "Request";
        r6.<init>(r0, r1);
        r2 = r13.size();
        r5 = isGzipCompressible(r13);
        r0 = 0;
        r1 = 1;
        if (r2 != r1) goto L_0x001c;
    L_0x0015:
        r3 = r13.get(r0);
        r3 = r3.httpMethod;
        goto L_0x001e;
    L_0x001c:
        r3 = com.facebook.HttpMethod.POST;
    L_0x001e:
        r4 = r3.name();
        r14.setRequestMethod(r4);
        setConnectionContentType(r14, r5);
        r4 = r14.getURL();
        r7 = "Request:\n";
        r6.append(r7);
        r7 = "Id";
        r8 = r13.getId();
        r6.appendKeyValue(r7, r8);
        r7 = "URL";
        r6.appendKeyValue(r7, r4);
        r7 = "Method";
        r8 = r14.getRequestMethod();
        r6.appendKeyValue(r7, r8);
        r7 = "User-Agent";
        r8 = "User-Agent";
        r8 = r14.getRequestProperty(r8);
        r6.appendKeyValue(r7, r8);
        r7 = "Content-Type";
        r8 = "Content-Type";
        r8 = r14.getRequestProperty(r8);
        r6.appendKeyValue(r7, r8);
        r7 = r13.getTimeout();
        r14.setConnectTimeout(r7);
        r7 = r13.getTimeout();
        r14.setReadTimeout(r7);
        r7 = com.facebook.HttpMethod.POST;
        if (r3 != r7) goto L_0x0071;
    L_0x0070:
        r0 = 1;
    L_0x0071:
        if (r0 != 0) goto L_0x0077;
    L_0x0073:
        r6.log();
        return;
    L_0x0077:
        r14.setDoOutput(r1);
        r0 = 0;
        r1 = new java.io.BufferedOutputStream;	 Catch:{ all -> 0x00ca }
        r14 = r14.getOutputStream();	 Catch:{ all -> 0x00ca }
        r1.<init>(r14);	 Catch:{ all -> 0x00ca }
        if (r5 == 0) goto L_0x008f;
    L_0x0086:
        r14 = new java.util.zip.GZIPOutputStream;	 Catch:{ all -> 0x008c }
        r14.<init>(r1);	 Catch:{ all -> 0x008c }
        goto L_0x0090;
    L_0x008c:
        r13 = move-exception;
        r14 = r1;
        goto L_0x00cc;
    L_0x008f:
        r14 = r1;
    L_0x0090:
        r0 = hasOnProgressCallbacks(r13);	 Catch:{ all -> 0x00c8 }
        if (r0 == 0) goto L_0x00ba;
    L_0x0096:
        r0 = new com.facebook.ProgressNoopOutputStream;	 Catch:{ all -> 0x00c8 }
        r1 = r13.getCallbackHandler();	 Catch:{ all -> 0x00c8 }
        r0.<init>(r1);	 Catch:{ all -> 0x00c8 }
        r8 = 0;
        r7 = r13;
        r9 = r2;
        r10 = r4;
        r11 = r0;
        r12 = r5;
        processRequest(r7, r8, r9, r10, r11, r12);	 Catch:{ all -> 0x00c8 }
        r1 = r0.getMaxProgress();	 Catch:{ all -> 0x00c8 }
        r10 = r0.getProgressMap();	 Catch:{ all -> 0x00c8 }
        r0 = new com.facebook.ProgressOutputStream;	 Catch:{ all -> 0x00c8 }
        r11 = (long) r1;	 Catch:{ all -> 0x00c8 }
        r7 = r0;
        r8 = r14;
        r9 = r13;
        r7.<init>(r8, r9, r10, r11);	 Catch:{ all -> 0x00c8 }
        r14 = r0;
    L_0x00ba:
        r0 = r13;
        r1 = r6;
        r3 = r4;
        r4 = r14;
        processRequest(r0, r1, r2, r3, r4, r5);	 Catch:{ all -> 0x00c8 }
        r14.close();
        r6.log();
        return;
    L_0x00c8:
        r13 = move-exception;
        goto L_0x00cc;
    L_0x00ca:
        r13 = move-exception;
        r14 = r0;
    L_0x00cc:
        if (r14 == 0) goto L_0x00d1;
    L_0x00ce:
        r14.close();
    L_0x00d1:
        throw r13;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.GraphRequest.serializeToUrlConnection(com.facebook.GraphRequestBatch, java.net.HttpURLConnection):void");
    }

    private static void processRequest(GraphRequestBatch graphRequestBatch, Logger logger, int i, URL url, OutputStream outputStream, boolean z) throws IOException, JSONException {
        Serializer serializer = new Serializer(outputStream, logger, z);
        HashMap hashMap;
        if (i == 1) {
            GraphRequest graphRequest = graphRequestBatch.get(0);
            hashMap = new HashMap();
            for (String str : graphRequest.parameters.keySet()) {
                Object obj = graphRequest.parameters.get(str);
                if (isSupportedAttachmentType(obj)) {
                    hashMap.put(str, new Attachment(graphRequest, obj));
                }
            }
            if (logger != null) {
                logger.append("  Parameters:\n");
            }
            serializeParameters(graphRequest.parameters, serializer, graphRequest);
            if (logger != null) {
                logger.append("  Attachments:\n");
            }
            serializeAttachments(hashMap, serializer);
            if (graphRequest.graphObject != null) {
                processGraphObject(graphRequest.graphObject, url.getPath(), serializer);
                return;
            }
            return;
        }
        String batchAppId = getBatchAppId(graphRequestBatch);
        if (Utility.isNullOrEmpty(batchAppId)) {
            throw new FacebookException("App ID was not specified at the request or Settings.");
        }
        serializer.writeString(BATCH_APP_ID_PARAM, batchAppId);
        hashMap = new HashMap();
        serializeRequestsAsJSON(serializer, graphRequestBatch, hashMap);
        if (logger != null) {
            logger.append("  Attachments:\n");
        }
        serializeAttachments(hashMap, serializer);
    }

    private static boolean isMeRequest(String str) {
        Matcher matcher = versionPattern.matcher(str);
        if (matcher.matches()) {
            str = matcher.group(1);
        }
        if (str.startsWith("me/") || str.startsWith("/me/")) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    private static void processGraphObject(org.json.JSONObject r6, java.lang.String r7, com.facebook.GraphRequest.KeyValueSerializer r8) throws java.io.IOException {
        /*
        r0 = isMeRequest(r7);
        r1 = 1;
        r2 = 0;
        if (r0 == 0) goto L_0x001e;
    L_0x0008:
        r0 = ":";
        r0 = r7.indexOf(r0);
        r3 = "?";
        r7 = r7.indexOf(r3);
        r3 = 3;
        if (r0 <= r3) goto L_0x001e;
    L_0x0017:
        r3 = -1;
        if (r7 == r3) goto L_0x001c;
    L_0x001a:
        if (r0 >= r7) goto L_0x001e;
    L_0x001c:
        r7 = 1;
        goto L_0x001f;
    L_0x001e:
        r7 = 0;
    L_0x001f:
        r0 = r6.keys();
    L_0x0023:
        r3 = r0.hasNext();
        if (r3 == 0) goto L_0x0044;
    L_0x0029:
        r3 = r0.next();
        r3 = (java.lang.String) r3;
        r4 = r6.opt(r3);
        if (r7 == 0) goto L_0x003f;
    L_0x0035:
        r5 = "image";
        r5 = r3.equalsIgnoreCase(r5);
        if (r5 == 0) goto L_0x003f;
    L_0x003d:
        r5 = 1;
        goto L_0x0040;
    L_0x003f:
        r5 = 0;
    L_0x0040:
        processGraphObjectProperty(r3, r4, r8, r5);
        goto L_0x0023;
    L_0x0044:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.GraphRequest.processGraphObject(org.json.JSONObject, java.lang.String, com.facebook.GraphRequest$KeyValueSerializer):void");
    }

    private static void processGraphObjectProperty(String str, Object obj, KeyValueSerializer keyValueSerializer, boolean z) throws IOException {
        Class cls = obj.getClass();
        if (JSONObject.class.isAssignableFrom(cls)) {
            JSONObject jSONObject = (JSONObject) obj;
            if (z) {
                Iterator keys = jSONObject.keys();
                while (keys.hasNext()) {
                    Object[] objArr = new Object[]{str, (String) keys.next()};
                    processGraphObjectProperty(String.format("%s[%s]", objArr), jSONObject.opt((String) keys.next()), keyValueSerializer, z);
                }
            } else if (jSONObject.has(ShareConstants.WEB_DIALOG_PARAM_ID)) {
                processGraphObjectProperty(str, jSONObject.optString(ShareConstants.WEB_DIALOG_PARAM_ID), keyValueSerializer, z);
            } else if (jSONObject.has("url")) {
                processGraphObjectProperty(str, jSONObject.optString("url"), keyValueSerializer, z);
            } else if (jSONObject.has(NativeProtocol.OPEN_GRAPH_CREATE_OBJECT_KEY)) {
                processGraphObjectProperty(str, jSONObject.toString(), keyValueSerializer, z);
            }
        } else if (JSONArray.class.isAssignableFrom(cls)) {
            JSONArray jSONArray = (JSONArray) obj;
            int length = jSONArray.length();
            for (int i = 0; i < length; i++) {
                processGraphObjectProperty(String.format(Locale.ROOT, "%s[%d]", new Object[]{str, Integer.valueOf(i)}), jSONArray.opt(i), keyValueSerializer, z);
            }
        } else if (String.class.isAssignableFrom(cls) || Number.class.isAssignableFrom(cls) || Boolean.class.isAssignableFrom(cls)) {
            keyValueSerializer.writeString(str, obj.toString());
        } else if (Date.class.isAssignableFrom(cls)) {
            keyValueSerializer.writeString(str, new SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.US).format((Date) obj));
        }
    }

    private static void serializeParameters(Bundle bundle, Serializer serializer, GraphRequest graphRequest) throws IOException {
        for (String str : bundle.keySet()) {
            Object obj = bundle.get(str);
            if (isSupportedParameterType(obj)) {
                serializer.writeObject(str, obj, graphRequest);
            }
        }
    }

    private static void serializeAttachments(Map<String, Attachment> map, Serializer serializer) throws IOException {
        for (String str : map.keySet()) {
            Attachment attachment = (Attachment) map.get(str);
            if (isSupportedAttachmentType(attachment.getValue())) {
                serializer.writeObject(str, attachment.getValue(), attachment.getRequest());
            }
        }
    }

    private static void serializeRequestsAsJSON(Serializer serializer, Collection<GraphRequest> collection, Map<String, Attachment> map) throws JSONException, IOException {
        JSONArray jSONArray = new JSONArray();
        for (GraphRequest serializeToBatch : collection) {
            serializeToBatch.serializeToBatch(jSONArray, map);
        }
        serializer.writeRequestsAsJson(BATCH_PARAM, jSONArray, collection);
    }

    private static String getMimeContentType() {
        return String.format("multipart/form-data; boundary=%s", new Object[]{MIME_BOUNDARY});
    }

    private static String getUserAgent() {
        if (userAgent == null) {
            userAgent = String.format("%s.%s", new Object[]{USER_AGENT_BASE, FacebookSdkVersion.BUILD});
            if (!Utility.isNullOrEmpty(InternalSettings.getCustomUserAgent())) {
                userAgent = String.format(Locale.ROOT, GRAPH_PATH_FORMAT, new Object[]{userAgent, r0});
            }
        }
        return userAgent;
    }

    private static String getBatchAppId(GraphRequestBatch graphRequestBatch) {
        if (!Utility.isNullOrEmpty(graphRequestBatch.getBatchApplicationId())) {
            return graphRequestBatch.getBatchApplicationId();
        }
        Iterator it = graphRequestBatch.iterator();
        while (it.hasNext()) {
            AccessToken accessToken = ((GraphRequest) it.next()).accessToken;
            if (accessToken != null) {
                String applicationId = accessToken.getApplicationId();
                if (applicationId != null) {
                    return applicationId;
                }
            }
        }
        if (Utility.isNullOrEmpty(defaultBatchApplicationId)) {
            return FacebookSdk.getApplicationId();
        }
        return defaultBatchApplicationId;
    }

    private static boolean isSupportedAttachmentType(Object obj) {
        return (obj instanceof Bitmap) || (obj instanceof byte[]) || (obj instanceof Uri) || (obj instanceof ParcelFileDescriptor) || (obj instanceof ParcelableResourceWithMimeType);
    }

    private static boolean isSupportedParameterType(Object obj) {
        return (obj instanceof String) || (obj instanceof Boolean) || (obj instanceof Number) || (obj instanceof Date);
    }

    private static String parameterToString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        if ((obj instanceof Boolean) || (obj instanceof Number)) {
            return obj.toString();
        }
        if (obj instanceof Date) {
            return new SimpleDateFormat(ISO_8601_FORMAT_STRING, Locale.US).format(obj);
        }
        throw new IllegalArgumentException("Unsupported parameter type.");
    }
}
