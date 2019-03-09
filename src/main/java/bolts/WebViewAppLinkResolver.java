package bolts;

import android.content.Context;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.bumptech.glide.load.Key;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebViewAppLinkResolver implements AppLinkResolver {
    private static final String KEY_AL_VALUE = "value";
    private static final String KEY_ANDROID = "android";
    private static final String KEY_APP_NAME = "app_name";
    private static final String KEY_CLASS = "class";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_SHOULD_FALLBACK = "should_fallback";
    private static final String KEY_URL = "url";
    private static final String KEY_WEB = "web";
    private static final String KEY_WEB_URL = "url";
    private static final String META_TAG_PREFIX = "al";
    private static final String PREFER_HEADER = "Prefer-Html-Meta-Tags";
    private static final String TAG_EXTRACTION_JAVASCRIPT = "javascript:boltsWebViewAppLinkResolverResult.setValue((function() {  var metaTags = document.getElementsByTagName('meta');  var results = [];  for (var i = 0; i < metaTags.length; i++) {    var property = metaTags[i].getAttribute('property');    if (property && property.substring(0, 'al:'.length) === 'al:') {      var tag = { \"property\": metaTags[i].getAttribute('property') };      if (metaTags[i].hasAttribute('content')) {        tag['content'] = metaTags[i].getAttribute('content');      }      results.push(tag);    }  }  return JSON.stringify(results);})())";
    private final Context context;

    public WebViewAppLinkResolver(Context context) {
        this.context = context;
    }

    public Task<AppLink> getAppLinkFromUrlInBackground(final Uri uri) {
        final Capture capture = new Capture();
        final Capture capture2 = new Capture();
        return Task.callInBackground(new Callable<Void>() {
            public Void call() throws Exception {
                URL url = new URL(uri.toString());
                Void voidR = null;
                URLConnection uRLConnection = null;
                while (url != null) {
                    uRLConnection = url.openConnection();
                    boolean z = uRLConnection instanceof HttpURLConnection;
                    if (z) {
                        ((HttpURLConnection) uRLConnection).setInstanceFollowRedirects(true);
                    }
                    uRLConnection.setRequestProperty(WebViewAppLinkResolver.PREFER_HEADER, WebViewAppLinkResolver.META_TAG_PREFIX);
                    uRLConnection.connect();
                    if (z) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) uRLConnection;
                        if (httpURLConnection.getResponseCode() >= 300 && httpURLConnection.getResponseCode() < 400) {
                            URL url2 = new URL(httpURLConnection.getHeaderField("Location"));
                            httpURLConnection.disconnect();
                            url = url2;
                        }
                    }
                    url = null;
                }
                try {
                    capture.set(WebViewAppLinkResolver.readFromConnection(uRLConnection));
                    capture2.set(uRLConnection.getContentType());
                    return voidR;
                } finally {
                    voidR = uRLConnection instanceof HttpURLConnection;
                    if (voidR != null) {
                        ((HttpURLConnection) uRLConnection).disconnect();
                    }
                }
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONArray>>() {
            public Task<JSONArray> then(Task<Void> task) throws Exception {
                final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
                WebView webView = new WebView(WebViewAppLinkResolver.this.context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setNetworkAvailable(false);
                webView.setWebViewClient(new WebViewClient() {
                    private boolean loaded = false;

                    private void runJavaScript(WebView webView) {
                        if (!this.loaded) {
                            this.loaded = true;
                            webView.loadUrl(WebViewAppLinkResolver.TAG_EXTRACTION_JAVASCRIPT);
                        }
                    }

                    public void onPageFinished(WebView webView, String str) {
                        super.onPageFinished(webView, str);
                        runJavaScript(webView);
                    }

                    public void onLoadResource(WebView webView, String str) {
                        super.onLoadResource(webView, str);
                        runJavaScript(webView);
                    }
                });
                webView.addJavascriptInterface(new Object() {
                    @JavascriptInterface
                    public void setValue(String str) {
                        try {
                            taskCompletionSource.trySetResult(new JSONArray(str));
                        } catch (JSONException e) {
                            taskCompletionSource.trySetError(e);
                        }
                    }
                }, "boltsWebViewAppLinkResolverResult");
                webView.loadDataWithBaseURL(uri.toString(), (String) capture.get(), capture2.get() != null ? ((String) capture2.get()).split(";")[0] : null, null, null);
                return taskCompletionSource.getTask();
            }
        }, Task.UI_THREAD_EXECUTOR).onSuccess(new Continuation<JSONArray, AppLink>() {
            public AppLink then(Task<JSONArray> task) throws Exception {
                return WebViewAppLinkResolver.makeAppLinkFromAlData(WebViewAppLinkResolver.parseAlData((JSONArray) task.getResult()), uri);
            }
        });
    }

    private static Map<String, Object> parseAlData(JSONArray jSONArray) throws JSONException {
        HashMap hashMap = new HashMap();
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            String[] split = jSONObject.getString("property").split(":");
            if (split[0].equals(META_TAG_PREFIX)) {
                Map map = hashMap;
                int i2 = 1;
                while (true) {
                    Map map2 = null;
                    if (i2 >= split.length) {
                        break;
                    }
                    List list = (List) map.get(split[i2]);
                    if (list == null) {
                        list = new ArrayList();
                        map.put(split[i2], list);
                    }
                    if (list.size() > 0) {
                        map2 = (Map) list.get(list.size() - 1);
                    }
                    if (map2 == null || i2 == split.length - 1) {
                        map = new HashMap();
                        list.add(map);
                    } else {
                        map = map2;
                    }
                    i2++;
                }
                if (jSONObject.has("content")) {
                    if (jSONObject.isNull("content")) {
                        map.put(KEY_AL_VALUE, null);
                    } else {
                        map.put(KEY_AL_VALUE, jSONObject.getString("content"));
                    }
                }
            }
        }
        return hashMap;
    }

    private static List<Map<String, Object>> getAlList(Map<String, Object> map, String str) {
        List list = (List) map.get(str);
        return list == null ? Collections.emptyList() : list;
    }

    /* JADX WARNING: Missing block: B:34:0x011b, code skipped:
            if (java.util.Arrays.asList(new java.lang.String[]{"no", "false", com.facebook.appevents.AppEventsConstants.EVENT_PARAM_VALUE_NO}).contains(((java.lang.String) ((java.util.Map) r14.get(0)).get(KEY_AL_VALUE)).toLowerCase()) != false) goto L_0x011f;
     */
    private static bolts.AppLink makeAppLinkFromAlData(java.util.Map<java.lang.String, java.lang.Object> r14, android.net.Uri r15) {
        /*
        r0 = new java.util.ArrayList;
        r0.<init>();
        r1 = "android";
        r1 = r14.get(r1);
        r1 = (java.util.List) r1;
        if (r1 != 0) goto L_0x0013;
    L_0x000f:
        r1 = java.util.Collections.emptyList();
    L_0x0013:
        r1 = r1.iterator();
    L_0x0017:
        r2 = r1.hasNext();
        r3 = 0;
        r4 = 0;
        if (r2 == 0) goto L_0x00c2;
    L_0x001f:
        r2 = r1.next();
        r2 = (java.util.Map) r2;
        r5 = "url";
        r5 = getAlList(r2, r5);
        r6 = "package";
        r6 = getAlList(r2, r6);
        r7 = "class";
        r7 = getAlList(r2, r7);
        r8 = "app_name";
        r2 = getAlList(r2, r8);
        r8 = r5.size();
        r9 = r6.size();
        r10 = r7.size();
        r11 = r2.size();
        r10 = java.lang.Math.max(r10, r11);
        r9 = java.lang.Math.max(r9, r10);
        r8 = java.lang.Math.max(r8, r9);
    L_0x0059:
        if (r4 >= r8) goto L_0x0017;
    L_0x005b:
        r9 = r5.size();
        if (r9 <= r4) goto L_0x006e;
    L_0x0061:
        r9 = r5.get(r4);
        r9 = (java.util.Map) r9;
        r10 = "value";
        r9 = r9.get(r10);
        goto L_0x006f;
    L_0x006e:
        r9 = r3;
    L_0x006f:
        r9 = (java.lang.String) r9;
        r9 = tryCreateUrl(r9);
        r10 = r6.size();
        if (r10 <= r4) goto L_0x0088;
    L_0x007b:
        r10 = r6.get(r4);
        r10 = (java.util.Map) r10;
        r11 = "value";
        r10 = r10.get(r11);
        goto L_0x0089;
    L_0x0088:
        r10 = r3;
    L_0x0089:
        r10 = (java.lang.String) r10;
        r11 = r7.size();
        if (r11 <= r4) goto L_0x009e;
    L_0x0091:
        r11 = r7.get(r4);
        r11 = (java.util.Map) r11;
        r12 = "value";
        r11 = r11.get(r12);
        goto L_0x009f;
    L_0x009e:
        r11 = r3;
    L_0x009f:
        r11 = (java.lang.String) r11;
        r12 = r2.size();
        if (r12 <= r4) goto L_0x00b4;
    L_0x00a7:
        r12 = r2.get(r4);
        r12 = (java.util.Map) r12;
        r13 = "value";
        r12 = r12.get(r13);
        goto L_0x00b5;
    L_0x00b4:
        r12 = r3;
    L_0x00b5:
        r12 = (java.lang.String) r12;
        r13 = new bolts.AppLink$Target;
        r13.<init>(r10, r11, r9, r12);
        r0.add(r13);
        r4 = r4 + 1;
        goto L_0x0059;
    L_0x00c2:
        r1 = "web";
        r14 = r14.get(r1);
        r14 = (java.util.List) r14;
        if (r14 == 0) goto L_0x013e;
    L_0x00cc:
        r1 = r14.size();
        if (r1 <= 0) goto L_0x013e;
    L_0x00d2:
        r14 = r14.get(r4);
        r14 = (java.util.Map) r14;
        r1 = "url";
        r1 = r14.get(r1);
        r1 = (java.util.List) r1;
        r2 = "should_fallback";
        r14 = r14.get(r2);
        r14 = (java.util.List) r14;
        if (r14 == 0) goto L_0x011e;
    L_0x00ea:
        r2 = r14.size();
        if (r2 <= 0) goto L_0x011e;
    L_0x00f0:
        r14 = r14.get(r4);
        r14 = (java.util.Map) r14;
        r2 = "value";
        r14 = r14.get(r2);
        r14 = (java.lang.String) r14;
        r2 = 3;
        r2 = new java.lang.String[r2];
        r5 = "no";
        r2[r4] = r5;
        r5 = "false";
        r6 = 1;
        r2[r6] = r5;
        r5 = 2;
        r6 = "0";
        r2[r5] = r6;
        r2 = java.util.Arrays.asList(r2);
        r14 = r14.toLowerCase();
        r14 = r2.contains(r14);
        if (r14 == 0) goto L_0x011e;
    L_0x011d:
        goto L_0x011f;
    L_0x011e:
        r3 = r15;
    L_0x011f:
        if (r3 == 0) goto L_0x013c;
    L_0x0121:
        if (r1 == 0) goto L_0x013c;
    L_0x0123:
        r14 = r1.size();
        if (r14 <= 0) goto L_0x013c;
    L_0x0129:
        r14 = r1.get(r4);
        r14 = (java.util.Map) r14;
        r1 = "value";
        r14 = r14.get(r1);
        r14 = (java.lang.String) r14;
        r14 = tryCreateUrl(r14);
        goto L_0x013f;
    L_0x013c:
        r14 = r3;
        goto L_0x013f;
    L_0x013e:
        r14 = r15;
    L_0x013f:
        r1 = new bolts.AppLink;
        r1.<init>(r15, r0, r14);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: bolts.WebViewAppLinkResolver.makeAppLinkFromAlData(java.util.Map, android.net.Uri):bolts.AppLink");
    }

    private static Uri tryCreateUrl(String str) {
        return str == null ? null : Uri.parse(str);
    }

    private static String readFromConnection(URLConnection uRLConnection) throws IOException {
        InputStream inputStream;
        if (uRLConnection instanceof HttpURLConnection) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) uRLConnection;
            try {
                inputStream = uRLConnection.getInputStream();
            } catch (Exception unused) {
                inputStream = httpURLConnection.getErrorStream();
            }
        } else {
            inputStream = uRLConnection.getInputStream();
        }
        try {
            int read;
            int i;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                read = inputStream.read(bArr);
                i = 0;
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            String contentEncoding = uRLConnection.getContentEncoding();
            if (contentEncoding == null) {
                String[] split = uRLConnection.getContentType().split(";");
                read = split.length;
                while (i < read) {
                    String trim = split[i].trim();
                    if (trim.startsWith("charset=")) {
                        contentEncoding = trim.substring("charset=".length());
                        break;
                    }
                    i++;
                }
                if (contentEncoding == null) {
                    contentEncoding = Key.STRING_CHARSET_NAME;
                }
            }
            String str = new String(byteArrayOutputStream.toByteArray(), contentEncoding);
            return str;
        } finally {
            inputStream.close();
        }
    }
}
