package com.twitter.sdk.android.core.identity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.facebook.AccessToken;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthException;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.oauth.OAuth1aService;
import com.twitter.sdk.android.core.internal.oauth.OAuthConstants;
import com.twitter.sdk.android.core.internal.oauth.OAuthResponse;

class OAuthController implements Listener {
    private final TwitterAuthConfig authConfig;
    final Listener listener;
    private final OAuth1aService oAuth1aService;
    TwitterAuthToken requestToken;
    private final ProgressBar spinner;
    private final WebView webView;

    interface Listener {
        void onComplete(int i, Intent intent);
    }

    OAuthController(ProgressBar progressBar, WebView webView, TwitterAuthConfig twitterAuthConfig, OAuth1aService oAuth1aService, Listener listener) {
        this.spinner = progressBar;
        this.webView = webView;
        this.authConfig = twitterAuthConfig;
        this.oAuth1aService = oAuth1aService;
        this.listener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public void startAuth() {
        Twitter.getLogger().d("Twitter", "Obtaining request token to start the sign in flow");
        this.oAuth1aService.requestTempToken(newRequestTempTokenCallback());
    }

    /* Access modifiers changed, original: 0000 */
    public Callback<OAuthResponse> newRequestTempTokenCallback() {
        return new Callback<OAuthResponse>() {
            public void success(Result<OAuthResponse> result) {
                OAuthController.this.requestToken = ((OAuthResponse) result.data).authToken;
                String authorizeUrl = OAuthController.this.oAuth1aService.getAuthorizeUrl(OAuthController.this.requestToken);
                Twitter.getLogger().d("Twitter", "Redirecting user to web view to complete authorization flow");
                OAuthController.this.setUpWebView(OAuthController.this.webView, new OAuthWebViewClient(OAuthController.this.oAuth1aService.buildCallbackUrl(OAuthController.this.authConfig), OAuthController.this), authorizeUrl, new OAuthWebChromeClient());
            }

            public void failure(TwitterException twitterException) {
                Twitter.getLogger().e("Twitter", "Failed to get request token", twitterException);
                OAuthController.this.handleAuthError(1, new TwitterAuthException("Failed to get request token"));
            }
        };
    }

    /* Access modifiers changed, original: protected */
    public void handleAuthError(int i, TwitterAuthException twitterAuthException) {
        Intent intent = new Intent();
        intent.putExtra("auth_error", twitterAuthException);
        this.listener.onComplete(i, intent);
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpWebView(WebView webView, WebViewClient webViewClient, String str, WebChromeClient webChromeClient) {
        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccess(false);
        settings.setJavaScriptEnabled(false);
        settings.setSaveFormData(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(str);
        webView.setVisibility(4);
        webView.setWebChromeClient(webChromeClient);
    }

    private void handleWebViewSuccess(Bundle bundle) {
        Twitter.getLogger().d("Twitter", "OAuth web view completed successfully");
        if (bundle != null) {
            String string = bundle.getString(OAuthConstants.PARAM_VERIFIER);
            if (string != null) {
                Twitter.getLogger().d("Twitter", "Converting the request token to an access token.");
                this.oAuth1aService.requestAccessToken(newRequestAccessTokenCallback(), this.requestToken, string);
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Failed to get authorization, bundle incomplete ");
        stringBuilder.append(bundle);
        Twitter.getLogger().e("Twitter", stringBuilder.toString(), null);
        handleAuthError(1, new TwitterAuthException("Failed to get authorization, bundle incomplete"));
    }

    /* Access modifiers changed, original: 0000 */
    public Callback<OAuthResponse> newRequestAccessTokenCallback() {
        return new Callback<OAuthResponse>() {
            public void success(Result<OAuthResponse> result) {
                Intent intent = new Intent();
                OAuthResponse oAuthResponse = (OAuthResponse) result.data;
                intent.putExtra("screen_name", oAuthResponse.userName);
                intent.putExtra(AccessToken.USER_ID_KEY, oAuthResponse.userId);
                intent.putExtra("tk", oAuthResponse.authToken.token);
                intent.putExtra("ts", oAuthResponse.authToken.secret);
                OAuthController.this.listener.onComplete(-1, intent);
            }

            public void failure(TwitterException twitterException) {
                Twitter.getLogger().e("Twitter", "Failed to get access token", twitterException);
                OAuthController.this.handleAuthError(1, new TwitterAuthException("Failed to get access token"));
            }
        };
    }

    private void handleWebViewError(WebViewException webViewException) {
        Twitter.getLogger().e("Twitter", "OAuth web view completed with an error", webViewException);
        handleAuthError(1, new TwitterAuthException("OAuth web view completed with an error"));
    }

    private void dismissWebView() {
        this.webView.stopLoading();
        dismissSpinner();
    }

    private void dismissSpinner() {
        this.spinner.setVisibility(8);
    }

    public void onPageFinished(WebView webView, String str) {
        dismissSpinner();
        webView.setVisibility(0);
    }

    public void onSuccess(Bundle bundle) {
        handleWebViewSuccess(bundle);
        dismissWebView();
    }

    public void onError(WebViewException webViewException) {
        handleWebViewError(webViewException);
        dismissWebView();
    }
}
