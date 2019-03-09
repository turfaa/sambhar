package com.facebook;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Log;
import com.facebook.internal.FacebookRequestErrorClassification;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GraphResponse {
    private static final String BODY_KEY = "body";
    private static final String CODE_KEY = "code";
    public static final String NON_JSON_RESPONSE_PROPERTY = "FACEBOOK_NON_JSON_RESULT";
    private static final String RESPONSE_LOG_TAG = "Response";
    public static final String SUCCESS_KEY = "success";
    private static final String TAG = "GraphResponse";
    private final HttpURLConnection connection;
    private final FacebookRequestError error;
    private final JSONObject graphObject;
    private final JSONArray graphObjectArray;
    private final String rawResponse;
    private final GraphRequest request;

    public enum PagingDirection {
        NEXT,
        PREVIOUS
    }

    GraphResponse(GraphRequest graphRequest, HttpURLConnection httpURLConnection, String str, JSONObject jSONObject) {
        this(graphRequest, httpURLConnection, str, jSONObject, null, null);
    }

    GraphResponse(GraphRequest graphRequest, HttpURLConnection httpURLConnection, String str, JSONArray jSONArray) {
        this(graphRequest, httpURLConnection, str, null, jSONArray, null);
    }

    GraphResponse(GraphRequest graphRequest, HttpURLConnection httpURLConnection, FacebookRequestError facebookRequestError) {
        this(graphRequest, httpURLConnection, null, null, null, facebookRequestError);
    }

    GraphResponse(GraphRequest graphRequest, HttpURLConnection httpURLConnection, String str, JSONObject jSONObject, JSONArray jSONArray, FacebookRequestError facebookRequestError) {
        this.request = graphRequest;
        this.connection = httpURLConnection;
        this.rawResponse = str;
        this.graphObject = jSONObject;
        this.graphObjectArray = jSONArray;
        this.error = facebookRequestError;
    }

    public final FacebookRequestError getError() {
        return this.error;
    }

    public final JSONObject getJSONObject() {
        return this.graphObject;
    }

    public final JSONArray getJSONArray() {
        return this.graphObjectArray;
    }

    public final HttpURLConnection getConnection() {
        return this.connection;
    }

    public GraphRequest getRequest() {
        return this.request;
    }

    public String getRawResponse() {
        return this.rawResponse;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0028 A:{RETURN} */
    public com.facebook.GraphRequest getRequestForPagedResults(com.facebook.GraphResponse.PagingDirection r5) {
        /*
        r4 = this;
        r0 = r4.graphObject;
        r1 = 0;
        if (r0 == 0) goto L_0x0021;
    L_0x0005:
        r0 = r4.graphObject;
        r2 = "paging";
        r0 = r0.optJSONObject(r2);
        if (r0 == 0) goto L_0x0021;
    L_0x000f:
        r2 = com.facebook.GraphResponse.PagingDirection.NEXT;
        if (r5 != r2) goto L_0x001a;
    L_0x0013:
        r5 = "next";
        r5 = r0.optString(r5);
        goto L_0x0022;
    L_0x001a:
        r5 = "previous";
        r5 = r0.optString(r5);
        goto L_0x0022;
    L_0x0021:
        r5 = r1;
    L_0x0022:
        r0 = com.facebook.internal.Utility.isNullOrEmpty(r5);
        if (r0 == 0) goto L_0x0029;
    L_0x0028:
        return r1;
    L_0x0029:
        if (r5 == 0) goto L_0x0038;
    L_0x002b:
        r0 = r4.request;
        r0 = r0.getUrlForSingleRequest();
        r0 = r5.equals(r0);
        if (r0 == 0) goto L_0x0038;
    L_0x0037:
        return r1;
    L_0x0038:
        r0 = new com.facebook.GraphRequest;	 Catch:{ MalformedURLException -> 0x0049 }
        r2 = r4.request;	 Catch:{ MalformedURLException -> 0x0049 }
        r2 = r2.getAccessToken();	 Catch:{ MalformedURLException -> 0x0049 }
        r3 = new java.net.URL;	 Catch:{ MalformedURLException -> 0x0049 }
        r3.<init>(r5);	 Catch:{ MalformedURLException -> 0x0049 }
        r0.<init>(r2, r3);	 Catch:{ MalformedURLException -> 0x0049 }
        return r0;
    L_0x0049:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.GraphResponse.getRequestForPagedResults(com.facebook.GraphResponse$PagingDirection):com.facebook.GraphRequest");
    }

    public String toString() {
        String format;
        try {
            Locale locale = Locale.US;
            String str = "%d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(this.connection != null ? this.connection.getResponseCode() : Callback.DEFAULT_DRAG_ANIMATION_DURATION);
            format = String.format(locale, str, objArr);
        } catch (IOException unused) {
            format = "unknown";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{Response: ");
        stringBuilder.append(" responseCode: ");
        stringBuilder.append(format);
        stringBuilder.append(", graphObject: ");
        stringBuilder.append(this.graphObject);
        stringBuilder.append(", error: ");
        stringBuilder.append(this.error);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    static List<GraphResponse> fromHttpConnection(HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) {
        List<GraphResponse> httpURLConnection2;
        Closeable closeable = null;
        List<GraphResponse> e;
        try {
            InputStream errorStream;
            if (httpURLConnection2.getResponseCode() >= 400) {
                errorStream = httpURLConnection2.getErrorStream();
            } else {
                errorStream = httpURLConnection2.getInputStream();
            }
            closeable = errorStream;
            e = createResponsesFromStream(closeable, httpURLConnection2, graphRequestBatch);
            return e;
        } catch (FacebookException e2) {
            e = e2;
            Logger.log(LoggingBehavior.REQUESTS, RESPONSE_LOG_TAG, "Response <Error>: %s", e);
            httpURLConnection2 = constructErrorResponses(graphRequestBatch, httpURLConnection2, e);
            return httpURLConnection2;
        } catch (Exception e3) {
            e = e3;
            Logger.log(LoggingBehavior.REQUESTS, RESPONSE_LOG_TAG, "Response <Error>: %s", e);
            httpURLConnection2 = constructErrorResponses(graphRequestBatch, httpURLConnection2, new FacebookException((Throwable) e));
            return httpURLConnection2;
        } finally {
            Utility.closeQuietly(closeable);
        }
    }

    static List<GraphResponse> createResponsesFromStream(InputStream inputStream, HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) throws FacebookException, JSONException, IOException {
        Logger.log(LoggingBehavior.INCLUDE_RAW_RESPONSES, RESPONSE_LOG_TAG, "Response (raw)\n  Size: %d\n  Response:\n%s\n", Integer.valueOf(Utility.readStreamToString(inputStream).length()), r6);
        return createResponsesFromString(Utility.readStreamToString(inputStream), httpURLConnection, graphRequestBatch);
    }

    static List<GraphResponse> createResponsesFromString(String str, HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) throws FacebookException, JSONException, IOException {
        Logger.log(LoggingBehavior.REQUESTS, RESPONSE_LOG_TAG, "Response\n  Id: %s\n  Size: %d\n  Responses:\n%s\n", graphRequestBatch.getId(), Integer.valueOf(str.length()), createResponsesFromObject(httpURLConnection, graphRequestBatch, new JSONTokener(str).nextValue()));
        return createResponsesFromObject(httpURLConnection, graphRequestBatch, new JSONTokener(str).nextValue());
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0056  */
    private static java.util.List<com.facebook.GraphResponse> createResponsesFromObject(java.net.HttpURLConnection r7, java.util.List<com.facebook.GraphRequest> r8, java.lang.Object r9) throws com.facebook.FacebookException, org.json.JSONException {
        /*
        r0 = r8.size();
        r1 = new java.util.ArrayList;
        r1.<init>(r0);
        r2 = 0;
        r3 = 1;
        if (r0 != r3) goto L_0x0051;
    L_0x000d:
        r3 = r8.get(r2);
        r3 = (com.facebook.GraphRequest) r3;
        r4 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        r4.<init>();	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        r5 = "body";
        r4.put(r5, r9);	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        if (r7 == 0) goto L_0x0024;
    L_0x001f:
        r5 = r7.getResponseCode();	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        goto L_0x0026;
    L_0x0024:
        r5 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
    L_0x0026:
        r6 = "code";
        r4.put(r6, r5);	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        r5 = new org.json.JSONArray;	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        r5.<init>();	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        r5.put(r4);	 Catch:{ JSONException -> 0x0043, IOException -> 0x0034 }
        goto L_0x0052;
    L_0x0034:
        r4 = move-exception;
        r5 = new com.facebook.GraphResponse;
        r6 = new com.facebook.FacebookRequestError;
        r6.<init>(r7, r4);
        r5.<init>(r3, r7, r6);
        r1.add(r5);
        goto L_0x0051;
    L_0x0043:
        r4 = move-exception;
        r5 = new com.facebook.GraphResponse;
        r6 = new com.facebook.FacebookRequestError;
        r6.<init>(r7, r4);
        r5.<init>(r3, r7, r6);
        r1.add(r5);
    L_0x0051:
        r5 = r9;
    L_0x0052:
        r3 = r5 instanceof org.json.JSONArray;
        if (r3 == 0) goto L_0x0097;
    L_0x0056:
        r5 = (org.json.JSONArray) r5;
        r3 = r5.length();
        if (r3 != r0) goto L_0x0097;
    L_0x005e:
        r0 = r5.length();
        if (r2 >= r0) goto L_0x0096;
    L_0x0064:
        r0 = r8.get(r2);
        r0 = (com.facebook.GraphRequest) r0;
        r3 = r5.get(r2);	 Catch:{ JSONException -> 0x0085, FacebookException -> 0x0076 }
        r3 = createResponseFromObject(r0, r7, r3, r9);	 Catch:{ JSONException -> 0x0085, FacebookException -> 0x0076 }
        r1.add(r3);	 Catch:{ JSONException -> 0x0085, FacebookException -> 0x0076 }
        goto L_0x0093;
    L_0x0076:
        r3 = move-exception;
        r4 = new com.facebook.GraphResponse;
        r6 = new com.facebook.FacebookRequestError;
        r6.<init>(r7, r3);
        r4.<init>(r0, r7, r6);
        r1.add(r4);
        goto L_0x0093;
    L_0x0085:
        r3 = move-exception;
        r4 = new com.facebook.GraphResponse;
        r6 = new com.facebook.FacebookRequestError;
        r6.<init>(r7, r3);
        r4.<init>(r0, r7, r6);
        r1.add(r4);
    L_0x0093:
        r2 = r2 + 1;
        goto L_0x005e;
    L_0x0096:
        return r1;
    L_0x0097:
        r7 = new com.facebook.FacebookException;
        r8 = "Unexpected number of results";
        r7.<init>(r8);
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.GraphResponse.createResponsesFromObject(java.net.HttpURLConnection, java.util.List, java.lang.Object):java.util.List");
    }

    private static GraphResponse createResponseFromObject(GraphRequest graphRequest, HttpURLConnection httpURLConnection, Object obj, Object obj2) throws JSONException {
        if (obj instanceof JSONObject) {
            JSONObject jSONObject = (JSONObject) obj;
            FacebookRequestError checkResponseAndCreateError = FacebookRequestError.checkResponseAndCreateError(jSONObject, obj2, httpURLConnection);
            if (checkResponseAndCreateError != null) {
                Log.e(TAG, checkResponseAndCreateError.toString());
                if (checkResponseAndCreateError.getErrorCode() == FacebookRequestErrorClassification.EC_INVALID_TOKEN && Utility.isCurrentAccessToken(graphRequest.getAccessToken())) {
                    if (checkResponseAndCreateError.getSubErrorCode() != FacebookRequestErrorClassification.ESC_APP_INACTIVE) {
                        AccessToken.setCurrentAccessToken(null);
                    } else if (!AccessToken.getCurrentAccessToken().isExpired()) {
                        AccessToken.expireCurrentAccessToken();
                    }
                }
                return new GraphResponse(graphRequest, httpURLConnection, checkResponseAndCreateError);
            }
            obj = Utility.getStringPropertyAsJSON(jSONObject, BODY_KEY, NON_JSON_RESPONSE_PROPERTY);
            if (obj instanceof JSONObject) {
                return new GraphResponse(graphRequest, httpURLConnection, obj.toString(), (JSONObject) obj);
            }
            if (obj instanceof JSONArray) {
                return new GraphResponse(graphRequest, httpURLConnection, obj.toString(), (JSONArray) obj);
            }
            obj = JSONObject.NULL;
        }
        if (obj == JSONObject.NULL) {
            return new GraphResponse(graphRequest, httpURLConnection, obj.toString(), (JSONObject) null);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Got unexpected object type in response, class: ");
        stringBuilder.append(obj.getClass().getSimpleName());
        throw new FacebookException(stringBuilder.toString());
    }

    static List<GraphResponse> constructErrorResponses(List<GraphRequest> list, HttpURLConnection httpURLConnection, FacebookException facebookException) {
        int size = list.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(new GraphResponse((GraphRequest) list.get(i), httpURLConnection, new FacebookRequestError(httpURLConnection, (Exception) facebookException)));
        }
        return arrayList;
    }
}
