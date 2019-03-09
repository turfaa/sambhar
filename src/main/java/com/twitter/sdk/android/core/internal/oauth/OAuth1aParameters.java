package com.twitter.sdk.android.core.internal.oauth;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.internal.network.UrlUtils;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okio.ByteString;

class OAuth1aParameters {
    private static final SecureRandom RAND = new SecureRandom();
    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String VERSION = "1.0";
    private final TwitterAuthConfig authConfig;
    private final TwitterAuthToken authToken;
    private final String callback;
    private final String method;
    private final Map<String, String> postParams;
    private final String url;

    public OAuth1aParameters(TwitterAuthConfig twitterAuthConfig, TwitterAuthToken twitterAuthToken, String str, String str2, String str3, Map<String, String> map) {
        this.authConfig = twitterAuthConfig;
        this.authToken = twitterAuthToken;
        this.callback = str;
        this.method = str2;
        this.url = str3;
        this.postParams = map;
    }

    public String getAuthorizationHeader() {
        String nonce = getNonce();
        String timestamp = getTimestamp();
        return constructAuthorizationHeader(nonce, timestamp, calculateSignature(constructSignatureBase(nonce, timestamp)));
    }

    private String getNonce() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(System.nanoTime()));
        stringBuilder.append(String.valueOf(Math.abs(RAND.nextLong())));
        return stringBuilder.toString();
    }

    private String getTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    /* Access modifiers changed, original: 0000 */
    public String constructSignatureBase(String str, String str2) {
        URI create = URI.create(this.url);
        TreeMap queryParams = UrlUtils.getQueryParams(create, true);
        if (this.postParams != null) {
            queryParams.putAll(this.postParams);
        }
        if (this.callback != null) {
            queryParams.put(OAuthConstants.PARAM_CALLBACK, this.callback);
        }
        queryParams.put(OAuthConstants.PARAM_CONSUMER_KEY, this.authConfig.getConsumerKey());
        queryParams.put(OAuthConstants.PARAM_NONCE, str);
        queryParams.put(OAuthConstants.PARAM_SIGNATURE_METHOD, SIGNATURE_METHOD);
        queryParams.put(OAuthConstants.PARAM_TIMESTAMP, str2);
        if (!(this.authToken == null || this.authToken.token == null)) {
            queryParams.put(OAuthConstants.PARAM_TOKEN, this.authToken.token);
        }
        queryParams.put(OAuthConstants.PARAM_VERSION, "1.0");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(create.getScheme());
        stringBuilder.append("://");
        stringBuilder.append(create.getHost());
        stringBuilder.append(create.getPath());
        str = stringBuilder.toString();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(this.method.toUpperCase(Locale.ENGLISH));
        stringBuilder2.append('&');
        stringBuilder2.append(UrlUtils.percentEncode(str));
        stringBuilder2.append('&');
        stringBuilder2.append(getEncodedQueryParams(queryParams));
        return stringBuilder2.toString();
    }

    private String getEncodedQueryParams(TreeMap<String, String> treeMap) {
        StringBuilder stringBuilder = new StringBuilder();
        int size = treeMap.size();
        int i = 0;
        for (Entry entry : treeMap.entrySet()) {
            stringBuilder.append(UrlUtils.percentEncode(UrlUtils.percentEncode((String) entry.getKey())));
            stringBuilder.append("%3D");
            stringBuilder.append(UrlUtils.percentEncode(UrlUtils.percentEncode((String) entry.getValue())));
            i++;
            if (i < size) {
                stringBuilder.append("%26");
            }
        }
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public String calculateSignature(String str) {
        try {
            String signingKey = getSigningKey();
            byte[] bytes = str.getBytes(UrlUtils.UTF8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(signingKey.getBytes(UrlUtils.UTF8), "HmacSHA1");
            Mac instance = Mac.getInstance("HmacSHA1");
            instance.init(secretKeySpec);
            bytes = instance.doFinal(bytes);
            return ByteString.of(bytes, 0, bytes.length).base64();
        } catch (InvalidKeyException e) {
            Twitter.getLogger().e("Twitter", "Failed to calculate signature", e);
            return "";
        } catch (NoSuchAlgorithmException e2) {
            Twitter.getLogger().e("Twitter", "Failed to calculate signature", e2);
            return "";
        } catch (UnsupportedEncodingException e3) {
            Twitter.getLogger().e("Twitter", "Failed to calculate signature", e3);
            return "";
        }
    }

    private String getSigningKey() {
        String str = this.authToken != null ? this.authToken.secret : null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UrlUtils.urlEncode(this.authConfig.getConsumerSecret()));
        stringBuilder.append('&');
        stringBuilder.append(UrlUtils.urlEncode(str));
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public String constructAuthorizationHeader(String str, String str2, String str3) {
        StringBuilder stringBuilder = new StringBuilder("OAuth");
        appendParameter(stringBuilder, OAuthConstants.PARAM_CALLBACK, this.callback);
        appendParameter(stringBuilder, OAuthConstants.PARAM_CONSUMER_KEY, this.authConfig.getConsumerKey());
        appendParameter(stringBuilder, OAuthConstants.PARAM_NONCE, str);
        appendParameter(stringBuilder, OAuthConstants.PARAM_SIGNATURE, str3);
        appendParameter(stringBuilder, OAuthConstants.PARAM_SIGNATURE_METHOD, SIGNATURE_METHOD);
        appendParameter(stringBuilder, OAuthConstants.PARAM_TIMESTAMP, str2);
        appendParameter(stringBuilder, OAuthConstants.PARAM_TOKEN, this.authToken != null ? this.authToken.token : null);
        appendParameter(stringBuilder, OAuthConstants.PARAM_VERSION, "1.0");
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    private void appendParameter(StringBuilder stringBuilder, String str, String str2) {
        if (str2 != null) {
            stringBuilder.append(' ');
            stringBuilder.append(UrlUtils.percentEncode(str));
            stringBuilder.append("=\"");
            stringBuilder.append(UrlUtils.percentEncode(str2));
            stringBuilder.append("\",");
        }
    }
}
