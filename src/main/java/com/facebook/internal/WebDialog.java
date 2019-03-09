package com.facebook.internal;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.facebook.AccessToken;
import com.facebook.FacebookDialogException;
import com.facebook.FacebookException;
import com.facebook.FacebookGraphResponseException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphResponse;
import com.facebook.common.R;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.internal.ShareInternalUtility;
import com.facebook.share.widget.ShareDialog;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebDialog extends Dialog {
    private static final int API_EC_DIALOG_CANCEL = 4201;
    private static final int BACKGROUND_GRAY = -872415232;
    private static final int DEFAULT_THEME = R.style.com_facebook_activity_theme;
    static final boolean DISABLE_SSL_CHECK_FOR_TESTING = false;
    private static final String DISPLAY_TOUCH = "touch";
    private static final String LOG_TAG = "FacebookSDK.WebDialog";
    private static final int MAX_PADDING_SCREEN_HEIGHT = 1280;
    private static final int MAX_PADDING_SCREEN_WIDTH = 800;
    private static final double MIN_SCALE_FACTOR = 0.5d;
    private static final int NO_PADDING_SCREEN_HEIGHT = 800;
    private static final int NO_PADDING_SCREEN_WIDTH = 480;
    private static volatile int webDialogTheme;
    private FrameLayout contentFrameLayout;
    private ImageView crossImageView;
    private String expectedRedirectUrl;
    private boolean isDetached;
    private boolean isPageFinished;
    private boolean listenerCalled;
    private OnCompleteListener onCompleteListener;
    private ProgressDialog spinner;
    private UploadStagingResourcesTask uploadTask;
    private String url;
    private WebView webView;
    private LayoutParams windowParams;

    public static class Builder {
        private AccessToken accessToken;
        private String action;
        private String applicationId;
        private Context context;
        private OnCompleteListener listener;
        private Bundle parameters;
        private int theme;

        public Builder(Context context, String str, Bundle bundle) {
            this.accessToken = AccessToken.getCurrentAccessToken();
            if (!AccessToken.isCurrentAccessTokenActive()) {
                String metadataApplicationId = Utility.getMetadataApplicationId(context);
                if (metadataApplicationId != null) {
                    this.applicationId = metadataApplicationId;
                } else {
                    throw new FacebookException("Attempted to create a builder without a valid access token or a valid default Application ID.");
                }
            }
            finishInit(context, str, bundle);
        }

        public Builder(Context context, String str, String str2, Bundle bundle) {
            if (str == null) {
                str = Utility.getMetadataApplicationId(context);
            }
            Validate.notNullOrEmpty(str, "applicationId");
            this.applicationId = str;
            finishInit(context, str2, bundle);
        }

        public Builder setTheme(int i) {
            this.theme = i;
            return this;
        }

        public Builder setOnCompleteListener(OnCompleteListener onCompleteListener) {
            this.listener = onCompleteListener;
            return this;
        }

        public WebDialog build() {
            if (this.accessToken != null) {
                this.parameters.putString("app_id", this.accessToken.getApplicationId());
                this.parameters.putString("access_token", this.accessToken.getToken());
            } else {
                this.parameters.putString("app_id", this.applicationId);
            }
            return WebDialog.newInstance(this.context, this.action, this.parameters, this.theme, this.listener);
        }

        public String getApplicationId() {
            return this.applicationId;
        }

        public Context getContext() {
            return this.context;
        }

        public int getTheme() {
            return this.theme;
        }

        public Bundle getParameters() {
            return this.parameters;
        }

        public OnCompleteListener getListener() {
            return this.listener;
        }

        private void finishInit(Context context, String str, Bundle bundle) {
            this.context = context;
            this.action = str;
            if (bundle != null) {
                this.parameters = bundle;
            } else {
                this.parameters = new Bundle();
            }
        }
    }

    private class DialogWebViewClient extends WebViewClient {
        private DialogWebViewClient() {
        }

        /* synthetic */ DialogWebViewClient(WebDialog webDialog, AnonymousClass1 anonymousClass1) {
            this();
        }

        /* JADX WARNING: Removed duplicated region for block: B:31:0x0096  */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0090  */
        public boolean shouldOverrideUrlLoading(android.webkit.WebView r6, java.lang.String r7) {
            /*
            r5 = this;
            r6 = "FacebookSDK.WebDialog";
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r1 = "Redirect URL: ";
            r0.append(r1);
            r0.append(r7);
            r0 = r0.toString();
            com.facebook.internal.Utility.logd(r6, r0);
            r6 = com.facebook.internal.WebDialog.this;
            r6 = r6.expectedRedirectUrl;
            r6 = r7.startsWith(r6);
            r0 = 1;
            if (r6 == 0) goto L_0x00a6;
        L_0x0023:
            r6 = com.facebook.internal.WebDialog.this;
            r6 = r6.parseResponseUri(r7);
            r7 = "error";
            r7 = r6.getString(r7);
            if (r7 != 0) goto L_0x0037;
        L_0x0031:
            r7 = "error_type";
            r7 = r6.getString(r7);
        L_0x0037:
            r1 = "error_msg";
            r1 = r6.getString(r1);
            if (r1 != 0) goto L_0x0045;
        L_0x003f:
            r1 = "error_message";
            r1 = r6.getString(r1);
        L_0x0045:
            if (r1 != 0) goto L_0x004d;
        L_0x0047:
            r1 = "error_description";
            r1 = r6.getString(r1);
        L_0x004d:
            r2 = "error_code";
            r2 = r6.getString(r2);
            r3 = com.facebook.internal.Utility.isNullOrEmpty(r2);
            r4 = -1;
            if (r3 != 0) goto L_0x005f;
        L_0x005a:
            r2 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x005f }
            goto L_0x0060;
        L_0x005f:
            r2 = -1;
        L_0x0060:
            r3 = com.facebook.internal.Utility.isNullOrEmpty(r7);
            if (r3 == 0) goto L_0x0074;
        L_0x0066:
            r3 = com.facebook.internal.Utility.isNullOrEmpty(r1);
            if (r3 == 0) goto L_0x0074;
        L_0x006c:
            if (r2 != r4) goto L_0x0074;
        L_0x006e:
            r7 = com.facebook.internal.WebDialog.this;
            r7.sendSuccessToListener(r6);
            goto L_0x00a5;
        L_0x0074:
            if (r7 == 0) goto L_0x008c;
        L_0x0076:
            r6 = "access_denied";
            r6 = r7.equals(r6);
            if (r6 != 0) goto L_0x0086;
        L_0x007e:
            r6 = "OAuthAccessDeniedException";
            r6 = r7.equals(r6);
            if (r6 == 0) goto L_0x008c;
        L_0x0086:
            r6 = com.facebook.internal.WebDialog.this;
            r6.cancel();
            goto L_0x00a5;
        L_0x008c:
            r6 = 4201; // 0x1069 float:5.887E-42 double:2.0756E-320;
            if (r2 != r6) goto L_0x0096;
        L_0x0090:
            r6 = com.facebook.internal.WebDialog.this;
            r6.cancel();
            goto L_0x00a5;
        L_0x0096:
            r6 = new com.facebook.FacebookRequestError;
            r6.<init>(r2, r7, r1);
            r7 = com.facebook.internal.WebDialog.this;
            r2 = new com.facebook.FacebookServiceException;
            r2.<init>(r6, r1);
            r7.sendErrorToListener(r2);
        L_0x00a5:
            return r0;
        L_0x00a6:
            r6 = "fbconnect://cancel";
            r6 = r7.startsWith(r6);
            if (r6 == 0) goto L_0x00b4;
        L_0x00ae:
            r6 = com.facebook.internal.WebDialog.this;
            r6.cancel();
            return r0;
        L_0x00b4:
            r6 = "touch";
            r6 = r7.contains(r6);
            r1 = 0;
            if (r6 == 0) goto L_0x00be;
        L_0x00bd:
            return r1;
        L_0x00be:
            r6 = com.facebook.internal.WebDialog.this;	 Catch:{ ActivityNotFoundException -> 0x00d3 }
            r6 = r6.getContext();	 Catch:{ ActivityNotFoundException -> 0x00d3 }
            r2 = new android.content.Intent;	 Catch:{ ActivityNotFoundException -> 0x00d3 }
            r3 = "android.intent.action.VIEW";
            r7 = android.net.Uri.parse(r7);	 Catch:{ ActivityNotFoundException -> 0x00d3 }
            r2.<init>(r3, r7);	 Catch:{ ActivityNotFoundException -> 0x00d3 }
            r6.startActivity(r2);	 Catch:{ ActivityNotFoundException -> 0x00d3 }
            return r0;
        L_0x00d3:
            return r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.facebook.internal.WebDialog$DialogWebViewClient.shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String):boolean");
        }

        public void onReceivedError(WebView webView, int i, String str, String str2) {
            super.onReceivedError(webView, i, str, str2);
            WebDialog.this.sendErrorToListener(new FacebookDialogException(str, i, str2));
        }

        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            super.onReceivedSslError(webView, sslErrorHandler, sslError);
            sslErrorHandler.cancel();
            WebDialog.this.sendErrorToListener(new FacebookDialogException(null, -11, null));
        }

        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            String str2 = WebDialog.LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Webview loading URL: ");
            stringBuilder.append(str);
            Utility.logd(str2, stringBuilder.toString());
            super.onPageStarted(webView, str, bitmap);
            if (!WebDialog.this.isDetached) {
                WebDialog.this.spinner.show();
            }
        }

        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            if (!WebDialog.this.isDetached) {
                WebDialog.this.spinner.dismiss();
            }
            WebDialog.this.contentFrameLayout.setBackgroundColor(0);
            WebDialog.this.webView.setVisibility(0);
            WebDialog.this.crossImageView.setVisibility(0);
            WebDialog.this.isPageFinished = true;
        }
    }

    public interface OnCompleteListener {
        void onComplete(Bundle bundle, FacebookException facebookException);
    }

    private class UploadStagingResourcesTask extends AsyncTask<Void, Void, String[]> {
        private String action;
        private Exception[] exceptions;
        private Bundle parameters;

        UploadStagingResourcesTask(String str, Bundle bundle) {
            this.action = str;
            this.parameters = bundle;
        }

        /* Access modifiers changed, original: protected|varargs */
        public String[] doInBackground(Void... voidArr) {
            String[] stringArray = this.parameters.getStringArray("media");
            final String[] strArr = new String[stringArray.length];
            this.exceptions = new Exception[stringArray.length];
            final CountDownLatch countDownLatch = new CountDownLatch(stringArray.length);
            ConcurrentLinkedQueue concurrentLinkedQueue = new ConcurrentLinkedQueue();
            AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
            int i = 0;
            while (i < stringArray.length) {
                Iterator it;
                try {
                    if (isCancelled()) {
                        it = concurrentLinkedQueue.iterator();
                        while (it.hasNext()) {
                            ((AsyncTask) it.next()).cancel(true);
                        }
                        return null;
                    }
                    Uri parse = Uri.parse(stringArray[i]);
                    if (Utility.isWebUri(parse)) {
                        strArr[i] = parse.toString();
                        countDownLatch.countDown();
                    } else {
                        concurrentLinkedQueue.add(ShareInternalUtility.newUploadStagingResourceWithImageRequest(currentAccessToken, parse, new Callback() {
                            public void onCompleted(GraphResponse graphResponse) {
                                try {
                                    FacebookRequestError error = graphResponse.getError();
                                    if (error != null) {
                                        String errorMessage = error.getErrorMessage();
                                        if (errorMessage == null) {
                                            errorMessage = "Error staging photo.";
                                        }
                                        throw new FacebookGraphResponseException(graphResponse, errorMessage);
                                    }
                                    JSONObject jSONObject = graphResponse.getJSONObject();
                                    if (jSONObject != null) {
                                        String optString = jSONObject.optString(ShareConstants.MEDIA_URI);
                                        if (optString != null) {
                                            strArr[i] = optString;
                                            countDownLatch.countDown();
                                            return;
                                        }
                                        throw new FacebookException("Error staging photo.");
                                    }
                                    throw new FacebookException("Error staging photo.");
                                } catch (Exception e) {
                                    UploadStagingResourcesTask.this.exceptions[i] = e;
                                }
                            }
                        }).executeAsync());
                    }
                    i++;
                } catch (Exception unused) {
                    it = concurrentLinkedQueue.iterator();
                    while (it.hasNext()) {
                        ((AsyncTask) it.next()).cancel(true);
                    }
                    return null;
                }
            }
            countDownLatch.await();
            return strArr;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(String[] strArr) {
            WebDialog.this.spinner.dismiss();
            for (Throwable th : this.exceptions) {
                if (th != null) {
                    WebDialog.this.sendErrorToListener(th);
                    return;
                }
            }
            if (strArr == null) {
                WebDialog.this.sendErrorToListener(new FacebookException("Failed to stage photos for web dialog"));
                return;
            }
            List asList = Arrays.asList(strArr);
            if (asList.contains(null)) {
                WebDialog.this.sendErrorToListener(new FacebookException("Failed to stage photos for web dialog"));
                return;
            }
            Utility.putJSONValueInBundle(this.parameters, "media", new JSONArray(asList));
            String dialogAuthority = ServerProtocol.getDialogAuthority();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(FacebookSdk.getGraphApiVersion());
            stringBuilder.append("/");
            stringBuilder.append(ServerProtocol.DIALOG_PATH);
            stringBuilder.append(this.action);
            WebDialog.this.url = Utility.buildUri(dialogAuthority, stringBuilder.toString(), this.parameters).toString();
            WebDialog.this.setUpWebView((WebDialog.this.crossImageView.getDrawable().getIntrinsicWidth() / 2) + 1);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0028, code skipped:
            return;
     */
    protected static void initDefaultTheme(android.content.Context r2) {
        /*
        if (r2 != 0) goto L_0x0003;
    L_0x0002:
        return;
    L_0x0003:
        r0 = r2.getPackageManager();	 Catch:{ NameNotFoundException -> 0x0029 }
        r2 = r2.getPackageName();	 Catch:{ NameNotFoundException -> 0x0029 }
        r1 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        r2 = r0.getApplicationInfo(r2, r1);	 Catch:{ NameNotFoundException -> 0x0029 }
        if (r2 == 0) goto L_0x0028;
    L_0x0013:
        r0 = r2.metaData;
        if (r0 != 0) goto L_0x0018;
    L_0x0017:
        goto L_0x0028;
    L_0x0018:
        r0 = webDialogTheme;
        if (r0 != 0) goto L_0x0027;
    L_0x001c:
        r2 = r2.metaData;
        r0 = "com.facebook.sdk.WebDialogTheme";
        r2 = r2.getInt(r0);
        setWebDialogTheme(r2);
    L_0x0027:
        return;
    L_0x0028:
        return;
    L_0x0029:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.internal.WebDialog.initDefaultTheme(android.content.Context):void");
    }

    public static WebDialog newInstance(Context context, String str, Bundle bundle, int i, OnCompleteListener onCompleteListener) {
        initDefaultTheme(context);
        return new WebDialog(context, str, bundle, i, onCompleteListener);
    }

    public static int getWebDialogTheme() {
        Validate.sdkInitialized();
        return webDialogTheme;
    }

    public static void setWebDialogTheme(int i) {
        if (i == 0) {
            i = DEFAULT_THEME;
        }
        webDialogTheme = i;
    }

    protected WebDialog(Context context, String str) {
        this(context, str, getWebDialogTheme());
    }

    private WebDialog(Context context, String str, int i) {
        if (i == 0) {
            i = getWebDialogTheme();
        }
        super(context, i);
        this.expectedRedirectUrl = ServerProtocol.DIALOG_REDIRECT_URI;
        this.listenerCalled = false;
        this.isDetached = false;
        this.isPageFinished = false;
        this.url = str;
    }

    private WebDialog(Context context, String str, Bundle bundle, int i, OnCompleteListener onCompleteListener) {
        if (i == 0) {
            i = getWebDialogTheme();
        }
        super(context, i);
        this.expectedRedirectUrl = ServerProtocol.DIALOG_REDIRECT_URI;
        this.listenerCalled = false;
        this.isDetached = false;
        this.isPageFinished = false;
        if (bundle == null) {
            bundle = new Bundle();
        }
        this.expectedRedirectUrl = Utility.isChromeOS(context) ? ServerProtocol.DIALOG_REDIRECT_CHROME_OS_URI : ServerProtocol.DIALOG_REDIRECT_URI;
        bundle.putString(ServerProtocol.DIALOG_PARAM_REDIRECT_URI, this.expectedRedirectUrl);
        bundle.putString(ServerProtocol.DIALOG_PARAM_DISPLAY, "touch");
        bundle.putString("client_id", FacebookSdk.getApplicationId());
        bundle.putString("sdk", String.format(Locale.ROOT, "android-%s", new Object[]{FacebookSdk.getSdkVersion()}));
        this.onCompleteListener = onCompleteListener;
        if (str.equals(ShareDialog.WEB_SHARE_DIALOG) && bundle.containsKey("media")) {
            this.uploadTask = new UploadStagingResourcesTask(str, bundle);
            return;
        }
        String dialogAuthority = ServerProtocol.getDialogAuthority();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FacebookSdk.getGraphApiVersion());
        stringBuilder.append("/");
        stringBuilder.append(ServerProtocol.DIALOG_PATH);
        stringBuilder.append(str);
        this.url = Utility.buildUri(dialogAuthority, stringBuilder.toString(), bundle).toString();
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public OnCompleteListener getOnCompleteListener() {
        return this.onCompleteListener;
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            cancel();
        }
        return super.onKeyDown(i, keyEvent);
    }

    public void dismiss() {
        if (this.webView != null) {
            this.webView.stopLoading();
        }
        if (!(this.isDetached || this.spinner == null || !this.spinner.isShowing())) {
            this.spinner.dismiss();
        }
        super.dismiss();
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        if (this.uploadTask == null || this.uploadTask.getStatus() != Status.PENDING) {
            resize();
            return;
        }
        this.uploadTask.execute(new Void[0]);
        this.spinner.show();
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        if (this.uploadTask != null) {
            this.uploadTask.cancel(true);
            this.spinner.dismiss();
        }
        super.onStop();
    }

    public void onDetachedFromWindow() {
        this.isDetached = true;
        super.onDetachedFromWindow();
    }

    public void onAttachedToWindow() {
        this.isDetached = false;
        if (Utility.mustFixWindowParamsForAutofill(getContext()) && this.windowParams != null && this.windowParams.token == null) {
            this.windowParams.token = getOwnerActivity().getWindow().getAttributes().token;
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Set token on onAttachedToWindow(): ");
            stringBuilder.append(this.windowParams.token);
            Utility.logd(str, stringBuilder.toString());
        }
        super.onAttachedToWindow();
    }

    public void onWindowAttributesChanged(LayoutParams layoutParams) {
        if (layoutParams.token == null) {
            this.windowParams = layoutParams;
        }
        super.onWindowAttributesChanged(layoutParams);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.spinner = new ProgressDialog(getContext());
        this.spinner.requestWindowFeature(1);
        this.spinner.setMessage(getContext().getString(R.string.com_facebook_loading));
        this.spinner.setCanceledOnTouchOutside(false);
        this.spinner.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                WebDialog.this.cancel();
            }
        });
        requestWindowFeature(1);
        this.contentFrameLayout = new FrameLayout(getContext());
        resize();
        getWindow().setGravity(17);
        getWindow().setSoftInputMode(16);
        createCrossImage();
        if (this.url != null) {
            setUpWebView((this.crossImageView.getDrawable().getIntrinsicWidth() / 2) + 1);
        }
        this.contentFrameLayout.addView(this.crossImageView, new ViewGroup.LayoutParams(-2, -2));
        setContentView(this.contentFrameLayout);
    }

    /* Access modifiers changed, original: protected */
    public void setExpectedRedirectUrl(String str) {
        this.expectedRedirectUrl = str;
    }

    /* Access modifiers changed, original: protected */
    public Bundle parseResponseUri(String str) {
        Uri parse = Uri.parse(str);
        Bundle parseUrlQueryString = Utility.parseUrlQueryString(parse.getQuery());
        parseUrlQueryString.putAll(Utility.parseUrlQueryString(parse.getFragment()));
        return parseUrlQueryString;
    }

    /* Access modifiers changed, original: protected */
    public boolean isListenerCalled() {
        return this.listenerCalled;
    }

    /* Access modifiers changed, original: protected */
    public boolean isPageFinished() {
        return this.isPageFinished;
    }

    /* Access modifiers changed, original: protected */
    public WebView getWebView() {
        return this.webView;
    }

    public void resize() {
        Display defaultDisplay = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        getWindow().setLayout(Math.min(getScaledSize(displayMetrics.widthPixels < displayMetrics.heightPixels ? displayMetrics.widthPixels : displayMetrics.heightPixels, displayMetrics.density, NO_PADDING_SCREEN_WIDTH, 800), displayMetrics.widthPixels), Math.min(getScaledSize(displayMetrics.widthPixels < displayMetrics.heightPixels ? displayMetrics.heightPixels : displayMetrics.widthPixels, displayMetrics.density, 800, MAX_PADDING_SCREEN_HEIGHT), displayMetrics.heightPixels));
    }

    private int getScaledSize(int i, float f, int i2, int i3) {
        int i4 = (int) (((float) i) / f);
        double d = MIN_SCALE_FACTOR;
        if (i4 <= i2) {
            d = 1.0d;
        } else if (i4 < i3) {
            double d2 = (double) (i3 - i4);
            double d3 = (double) (i3 - i2);
            Double.isNaN(d2);
            Double.isNaN(d3);
            d = MIN_SCALE_FACTOR + ((d2 / d3) * MIN_SCALE_FACTOR);
        }
        double d4 = (double) i;
        Double.isNaN(d4);
        return (int) (d4 * d);
    }

    /* Access modifiers changed, original: protected */
    public void sendSuccessToListener(Bundle bundle) {
        if (this.onCompleteListener != null && !this.listenerCalled) {
            this.listenerCalled = true;
            this.onCompleteListener.onComplete(bundle, null);
            dismiss();
        }
    }

    /* Access modifiers changed, original: protected */
    public void sendErrorToListener(Throwable th) {
        if (this.onCompleteListener != null && !this.listenerCalled) {
            FacebookException facebookException;
            this.listenerCalled = true;
            if (th instanceof FacebookException) {
                facebookException = (FacebookException) th;
            } else {
                facebookException = new FacebookException(th);
            }
            this.onCompleteListener.onComplete(null, facebookException);
            dismiss();
        }
    }

    public void cancel() {
        if (this.onCompleteListener != null && !this.listenerCalled) {
            sendErrorToListener(new FacebookOperationCanceledException());
        }
    }

    private void createCrossImage() {
        this.crossImageView = new ImageView(getContext());
        this.crossImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WebDialog.this.cancel();
            }
        });
        this.crossImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.com_facebook_close));
        this.crossImageView.setVisibility(4);
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void setUpWebView(int i) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        this.webView = new WebView(getContext()) {
            public void onWindowFocusChanged(boolean z) {
                try {
                    super.onWindowFocusChanged(z);
                } catch (NullPointerException unused) {
                }
            }
        };
        this.webView.setVerticalScrollBarEnabled(false);
        this.webView.setHorizontalScrollBarEnabled(false);
        this.webView.setWebViewClient(new DialogWebViewClient(this, null));
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.loadUrl(this.url);
        this.webView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        this.webView.setVisibility(4);
        this.webView.getSettings().setSavePassword(false);
        this.webView.getSettings().setSaveFormData(false);
        this.webView.setFocusable(true);
        this.webView.setFocusableInTouchMode(true);
        this.webView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!view.hasFocus()) {
                    view.requestFocus();
                }
                return false;
            }
        });
        linearLayout.setPadding(i, i, i, i);
        linearLayout.addView(this.webView);
        linearLayout.setBackgroundColor(BACKGROUND_GRAY);
        this.contentFrameLayout.addView(linearLayout);
    }
}
