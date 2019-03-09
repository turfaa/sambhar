package com.sambhar.sambharappreport.page.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy.Builder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.facebook.share.widget.ShareDialog.Mode;
import com.readystatesoftware.chuck.ChuckInterceptor;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.BaseActivity;
import com.sambhar.sambharappreport.base.view.ShambarTextView;
import com.sambhar.sambharappreport.data.SambharConstant;
import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.databinding.ActivityHomeBinding;
import com.sambhar.sambharappreport.entity.bodypost.JobCheckBodyPost;
import com.sambhar.sambharappreport.entity.bodypost.NotifyFormBody;
import com.sambhar.sambharappreport.entity.response.JobCheckResponse;
import com.sambhar.sambharappreport.entity.response.JobCreateResponse;
import com.sambhar.sambharappreport.entity.response.NotifyResponse;
import com.sambhar.sambharappreport.page.editprofile.EditProfileActivity;
import com.sambhar.sambharappreport.page.login.LoginActivity;
import com.sambhar.sambharappreport.rest.MediaTwitterEntity;
import com.sambhar.sambharappreport.rest.Resource;
import com.sambhar.sambharappreport.rest.TwitterVideoApiClient;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.internal.scribe.EventsFilesManager;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import io.sentry.Sentry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;

public class MainActivity extends BaseActivity<MainViewModel, ActivityHomeBinding> {
    private static final int IMAGE_TYPE = 1;
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PHOTOCODE = 101;
    private static final int PICK_IMAGE = 103;
    private static final int PICK_VIDEO = 104;
    private static final int TYPE_POST_IMAGE = 6;
    private static final int TYPE_POST_VIDEO = 7;
    private static final int VIDEOCODE = 102;
    private static final int VIDEO_TYPE = 2;
    private boolean allPermissionGranted = true;
    private CallbackManager callbackManager;
    private TwitterAuthClient client;
    private AlertDialog dialog;
    private boolean fbStatus;
    int fileType;
    private boolean fromGalleryVideo;
    private boolean hasShareMedia;
    private boolean igStatus;
    Uri imageGalleryUri;
    String mCurrentPhotoPath;
    String mCurrentVideoPath;
    private int mJobId;
    private String mJobTs;
    @Inject
    UserSharedPref mPref;
    private String mSource;
    private String notifyFileName;
    private String platformChoose;
    private boolean reShareFacebook;
    private boolean reShareTwitter;
    private boolean readyToShare;
    private ShareDialog shareDialog;
    private boolean successFb;
    private boolean successIg;
    private boolean successTwitter;
    private String twitterAccessToken;
    private String twitterSecrets;
    private boolean twitterStatus;
    private long twitterUserId;
    Uri videoGalletyUri;

    public int setLayoutView() {
        return R.layout.activity_home;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        deleteImageDir();
        StrictMode.setVmPolicy(new Builder().build());
        StrictMode.setThreadPolicy(new ThreadPolicy.Builder().permitAll().build());
        this.callbackManager = Factory.create();
        this.shareDialog = new ShareDialog((Activity) this);
        this.shareDialog.registerCallback(this.callbackManager, new FacebookCallback<Result>() {
            public void onCancel() {
            }

            public void onSuccess(Result result) {
                NotifyFormBody notifyFormBody = new NotifyFormBody();
                notifyFormBody.setJobId(MainActivity.this.mJobId);
                notifyFormBody.setSource(MainActivity.this.mSource);
                notifyFormBody.setCaption(((ActivityHomeBinding) MainActivity.this.getDataBinding()).etCaption.getText().toString());
                if (MainActivity.this.fileType == 1) {
                    notifyFormBody.setFileName(MainActivity.this.notifyFileName);
                } else if (MainActivity.this.fileType == 2) {
                    notifyFormBody.setFileName(MainActivity.this.notifyFileName);
                }
                MainActivity.this.successFb = true;
                MainActivity.this.hasShareMedia = true;
                ((MainViewModel) MainActivity.this.getViewModel()).notifyServer(notifyFormBody);
            }

            public void onError(FacebookException facebookException) {
                Sentry.capture((Throwable) facebookException);
                MainActivity.this.showErrorSnackbar(((ActivityHomeBinding) MainActivity.this.getDataBinding()).llHomeRoot, facebookException.getMessage());
            }
        });
        updateStat();
        checkAvailability();
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1);
        ((ActivityHomeBinding) getDataBinding()).btImage.setOnClickListener(new -$$Lambda$MainActivity$S4ALfpyWndEqaiczLZ6gxqvHKWE(this));
        ((ActivityHomeBinding) getDataBinding()).btVideo.setOnClickListener(new -$$Lambda$MainActivity$M1JBXKIY84Xb6rBefSaUTXBH_dk(this));
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                ((ActivityHomeBinding) getDataBinding()).etCaption.setText(intent.getStringExtra("android.intent.extra.TEXT"));
            }
            File file;
            if (type.startsWith("image/")) {
                Uri uri = (Uri) getIntent().getParcelableExtra("android.intent.extra.STREAM");
                ((ActivityHomeBinding) getDataBinding()).etCaption.setText(intent.getStringExtra("android.intent.extra.TEXT"));
                this.imageGalleryUri = uri;
                file = new File(getImagePathFromInputStreamUri(uri));
                this.mCurrentPhotoPath = file.getAbsolutePath();
                this.fileType = 1;
                ((MainViewModel) getViewModel()).createJob();
                Glide.with((FragmentActivity) this).asBitmap().load(file).into(((ActivityHomeBinding) getDataBinding()).ivImage);
            } else if (type.startsWith("video/")) {
                this.fileType = 2;
                this.fromGalleryVideo = true;
                ((ActivityHomeBinding) getDataBinding()).etCaption.setText(intent.getStringExtra("android.intent.extra.TEXT"));
                try {
                    this.videoGalletyUri = (Uri) getIntent().getParcelableExtra("android.intent.extra.STREAM");
                    file = new File(getImagePathFromInputStreamUri(this.videoGalletyUri));
                    ((MainViewModel) getViewModel()).createJob();
                    this.mCurrentVideoPath = file.getAbsolutePath();
                    ((ActivityHomeBinding) getDataBinding()).ivImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(this.mCurrentVideoPath, 3));
                } catch (Exception e) {
                    Sentry.capture(e);
                    e.printStackTrace();
                }
            }
        }
        ((ActivityHomeBinding) getDataBinding()).btShareIg.setOnClickListener(new -$$Lambda$MainActivity$MwjByEE_mlb-mVeu3vOjriuyU7E(this));
        ((ActivityHomeBinding) getDataBinding()).btShareFacebook.setOnClickListener(new -$$Lambda$MainActivity$AvNBBQMOzuMFKAXhDqc39KTbGm0(this));
        ((ActivityHomeBinding) getDataBinding()).btShareTwitter.setOnClickListener(new -$$Lambda$MainActivity$0CoJeHcIXLyysYjTxBimL3EEFtI(this));
        ((MainViewModel) getViewModel()).jobData().observe(this, new -$$Lambda$MainActivity$xPn4_tnltb4IvwuhyvWko9jkf5w(this));
        ((MainViewModel) getViewModel()).notifyData().observe(this, new -$$Lambda$MainActivity$kebAcAW7g9fKmnig6hV9vN0Ccck(this));
        ((MainViewModel) getViewModel()).jobCheckData().observe(this, new -$$Lambda$MainActivity$tguf47wGkRriqqTBPVi2tZivVjA(this));
    }

    public static /* synthetic */ void lambda$onCreate$2(MainActivity mainActivity, View view) {
        mainActivity.platformChoose = SambharConstant.SOURCE_INSTAGRAM;
        if (!TextUtils.isEmpty(mainActivity.mCurrentPhotoPath) || !TextUtils.isEmpty(mainActivity.mCurrentVideoPath)) {
            mainActivity.reShareFacebook = false;
            mainActivity.reShareTwitter = false;
            JobCheckBodyPost jobCheckBodyPost = new JobCheckBodyPost();
            jobCheckBodyPost.setJobId(mainActivity.mJobId);
            ((MainViewModel) mainActivity.getViewModel()).checkJob(jobCheckBodyPost);
        }
    }

    public static /* synthetic */ void lambda$onCreate$5(MainActivity mainActivity, View view) {
        mainActivity.platformChoose = SambharConstant.SOURCE_FB;
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(false);
        builder.setTitle((CharSequence) "Pilih mode untuk berbagi");
        builder.setMessage((CharSequence) "Pilih RE-Share jika content berupa Link Foto atau Link Video, Pilih Posting jika content foto/video di share langsung dari WA atau diambil dari Take Picture/Video");
        builder.setPositiveButton((CharSequence) "Posting", new -$$Lambda$MainActivity$m79D9ionEHV0d6jK6vDTjSsr3ao(mainActivity));
        builder.setNegativeButton((CharSequence) "Re-share", new -$$Lambda$MainActivity$g_bicuhk7rW8KWLWIEs9S74F_OU(mainActivity));
        builder.create().show();
    }

    public static /* synthetic */ void lambda$null$3(MainActivity mainActivity, DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        if (TextUtils.isEmpty(mainActivity.mCurrentPhotoPath) && TextUtils.isEmpty(mainActivity.mCurrentVideoPath)) {
            dialogInterface.dismiss();
            mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, "Anda belum memilih konten gambar/video");
            return;
        }
        mainActivity.reShareFacebook = false;
        mainActivity.reShareTwitter = false;
        JobCheckBodyPost jobCheckBodyPost = new JobCheckBodyPost();
        jobCheckBodyPost.setJobId(mainActivity.mJobId);
        ((MainViewModel) mainActivity.getViewModel()).checkJob(jobCheckBodyPost);
    }

    public static /* synthetic */ void lambda$null$4(MainActivity mainActivity, DialogInterface dialogInterface, int i) {
        if (TextUtils.isEmpty(((ActivityHomeBinding) mainActivity.getDataBinding()).etUrlReshare.getText().toString())) {
            mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, "Anda belum mengisi kolom URL RESHARE");
            return;
        }
        mainActivity.reShareFacebook = true;
        mainActivity.reShareTwitter = false;
        mainActivity.mSource = SambharConstant.SOURCE_FB;
        dialogInterface.dismiss();
        ((MainViewModel) mainActivity.getViewModel()).createJob();
    }

    public static /* synthetic */ void lambda$onCreate$8(MainActivity mainActivity, View view) {
        mainActivity.platformChoose = "twitter";
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(false);
        builder.setTitle((CharSequence) "Pilih mode untuk berbagi");
        builder.setMessage((CharSequence) "Pilih RE-Share jika content berupa Link Foto atau Link Video, Pilih Posting jika content foto/video di share langsung dari WA atau diambil dari Take Picture/Video");
        builder.setPositiveButton((CharSequence) "Posting", new -$$Lambda$MainActivity$bs4hw_GlWYUtles7AHjI4gA_qhY(mainActivity));
        builder.setNegativeButton((CharSequence) "ReShare", new -$$Lambda$MainActivity$SZBbXrAgyx0Q2JEVrvckNc98Px8(mainActivity));
        builder.create().show();
    }

    public static /* synthetic */ void lambda$null$6(MainActivity mainActivity, DialogInterface dialogInterface, int i) {
        mainActivity.reShareFacebook = false;
        mainActivity.reShareTwitter = false;
        dialogInterface.dismiss();
        if (TextUtils.isEmpty(mainActivity.mCurrentPhotoPath) && TextUtils.isEmpty(mainActivity.mCurrentVideoPath)) {
            dialogInterface.dismiss();
            mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, "Anda belum memilih konten gambar/video");
        } else if (((TwitterSession) TwitterCore.getInstance().getSessionManager().getActiveSession()) == null) {
            mainActivity.authenticateUser(mainActivity.fileType);
        } else {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            mainActivity.authenticateUser(mainActivity.fileType);
        }
    }

    public static /* synthetic */ void lambda$null$7(MainActivity mainActivity, DialogInterface dialogInterface, int i) {
        mainActivity.reShareFacebook = false;
        mainActivity.reShareTwitter = true;
        dialogInterface.dismiss();
        if (TextUtils.isEmpty(((ActivityHomeBinding) mainActivity.getDataBinding()).etUrlReshare.getText().toString())) {
            dialogInterface.dismiss();
            mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, "Anda belum mengisi kolom URL RESHARE");
            return;
        }
        ((MainViewModel) mainActivity.getViewModel()).createJob();
    }

    public static /* synthetic */ void lambda$onCreate$9(MainActivity mainActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                mainActivity.showLoading(mainActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                mainActivity.dismissLoading();
                mainActivity.failedCreateJob();
                return;
            case SUCCESS:
                mainActivity.dismissLoading();
                mainActivity.mJobId = ((JobCreateResponse) resource.data).getEntity().getJobId();
                mainActivity.mPref.setFacebookCount(((JobCreateResponse) resource.data).getEntity().getCount().getFbCount());
                mainActivity.mPref.setInstagramCount(((JobCreateResponse) resource.data).getEntity().getCount().getInstagramCount());
                mainActivity.mPref.setTwitterCount(((JobCreateResponse) resource.data).getEntity().getCount().getTwitterCount());
                mainActivity.updateStat();
                if (mainActivity.reShareFacebook) {
                    JobCheckBodyPost jobCheckBodyPost = new JobCheckBodyPost();
                    jobCheckBodyPost.setJobId(mainActivity.mJobId);
                    ((MainViewModel) mainActivity.getViewModel()).checkJob(jobCheckBodyPost);
                }
                if (!mainActivity.reShareTwitter) {
                    return;
                }
                if (((TwitterSession) TwitterCore.getInstance().getSessionManager().getActiveSession()) == null) {
                    mainActivity.authenticateUser(mainActivity.fileType);
                    return;
                }
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                mainActivity.authenticateUser(mainActivity.fileType);
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$10(MainActivity mainActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                mainActivity.showLoading(mainActivity.getString(R.string.please_wait));
                return;
            case ERROR:
                mainActivity.dismissLoading();
                mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, resource.message);
                return;
            case SUCCESS:
                mainActivity.dismissLoading();
                Toast.makeText(mainActivity, ((NotifyResponse) resource.data).getMessageFromServer(), 1).show();
                mainActivity.mPref.setInstagramCount(((NotifyResponse) resource.data).getEntity().getShareCount().getInstagramCount());
                mainActivity.mPref.setTwitterCount(((NotifyResponse) resource.data).getEntity().getShareCount().getTwitterCount());
                mainActivity.mPref.setFacebookCount(((NotifyResponse) resource.data).getEntity().getShareCount().getFbCount());
                mainActivity.updateStat();
                if (mainActivity.mSource.equalsIgnoreCase("twitter") || mainActivity.mSource.equalsIgnoreCase(SambharConstant.SOURCE_FB)) {
                    mainActivity.mSource = "";
                    mainActivity.postNotify();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public static /* synthetic */ void lambda$onCreate$11(MainActivity mainActivity, Resource resource) {
        switch (resource.status) {
            case LOADING:
                mainActivity.showLoading("Check Job Data");
                return;
            case ERROR:
                mainActivity.dismissLoading();
                mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, resource.message);
                return;
            case SUCCESS:
                mainActivity.dismissLoading();
                if (((JobCheckResponse) resource.data).getStatus() == 1) {
                    mainActivity.mJobTs = ((JobCheckResponse) resource.data).getJobTimeEntity().getTs();
                    mainActivity.handleInitiate((JobCheckResponse) resource.data);
                    return;
                } else if (((JobCheckResponse) resource.data).getStatus() == 0) {
                    mainActivity.showErrorSnackbar(((ActivityHomeBinding) mainActivity.getDataBinding()).llHomeRoot, ((JobCheckResponse) resource.data).getMessage());
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private void handleInitiate(JobCheckResponse jobCheckResponse) {
        if (jobCheckResponse.getStatus() == 1) {
            proceedShare();
        }
    }

    private void initializeTwitterSdk(String str, String str2) {
        Twitter.initialize(new TwitterConfig.Builder(this).logger(new DefaultLogger(3)).twitterAuthConfig(new TwitterAuthConfig(str, str2)).debug(true).build());
        proceedShare();
    }

    private void proceedShare() {
        if (this.platformChoose.equals(SambharConstant.SOURCE_INSTAGRAM)) {
            if (this.fileType == 1 && !TextUtils.isEmpty(this.mCurrentPhotoPath)) {
                createInstagramIntent("image/*", this.mCurrentPhotoPath);
            } else if (this.fileType == 2 && !TextUtils.isEmpty(this.mCurrentVideoPath)) {
                createInstagramIntent("video/*", this.mCurrentVideoPath);
            }
        } else if (this.platformChoose.equals(SambharConstant.SOURCE_FB)) {
            if (this.reShareFacebook) {
                shareLinkFacebook(((ActivityHomeBinding) getDataBinding()).etUrlReshare.getText().toString());
            } else if (this.fileType == 1 && !TextUtils.isEmpty(this.mCurrentPhotoPath)) {
                sharePhotoFacebook();
            } else if (this.fileType == 2 && !TextUtils.isEmpty(this.mCurrentVideoPath)) {
                shareVideoFacebook();
            }
        } else if (!this.platformChoose.equals("twitter")) {
        } else {
            if (this.reShareTwitter) {
                createTwitterLinkShareIntent();
            } else if (this.fileType == 1 && !TextUtils.isEmpty(this.mCurrentPhotoPath)) {
                createTwitterIntent("image/*", this.mCurrentPhotoPath);
            } else if (this.fileType == 2 && !TextUtils.isEmpty(this.mCurrentVideoPath)) {
                createTwitterIntent("video/*", this.mCurrentVideoPath);
            }
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 3);
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 2);
        }
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 1) {
            if (iArr.length > 0) {
                for (int i2 : iArr) {
                    if (i2 == -1) {
                        this.allPermissionGranted = false;
                    } else if (i2 == 0) {
                        checkPermission();
                    }
                }
            }
        } else if (i == 3) {
            if (iArr.length > 0) {
                for (int i22 : iArr) {
                    if (i22 == -1) {
                        this.allPermissionGranted = false;
                    } else if (i22 == 0) {
                        checkPermission();
                    }
                }
            }
        } else if (i == 2 && iArr.length > 0) {
            for (int i222 : iArr) {
                if (i222 == -1) {
                    this.allPermissionGranted = false;
                } else if (i222 == 0) {
                    checkPermission();
                }
            }
        }
        if (!this.allPermissionGranted) {
            showWarningDialogPermission();
        }
    }

    private void showWarningDialogPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage((CharSequence) "Aplikasi ini butuh mengakses perangkat yang ada pada ponsel anda. Berikan akses?");
        builder.setPositiveButton((CharSequence) "Iya", new -$$Lambda$MainActivity$V5NmRxsI_CTlhsybvFDG1UhH928(this));
        builder.setNegativeButton((CharSequence) "Tidak", new -$$Lambda$MainActivity$2teqkudg7Vkj5GRMH0R75LynqIY(this));
        builder.create().show();
    }

    private void createInstagramIntent(String str, String str2) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType(str);
        intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(str2)));
        intent.setPackage("com.instagram.android");
        this.successIg = true;
        this.hasShareMedia = true;
        this.mSource = SambharConstant.SOURCE_INSTAGRAM;
        if (!TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString())) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Caption", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
        }
        startActivity(Intent.createChooser(intent, "Send to Instagram"));
    }

    private void createFacebookIntent(String str, String str2) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType(str);
        intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(str2)));
        intent.setPackage("com.facebook.katana");
        this.successFb = true;
        this.hasShareMedia = true;
        this.mSource = SambharConstant.SOURCE_FB;
        if (!TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString())) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Caption", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
        }
        startActivity(Intent.createChooser(intent, "Send to Facebook"));
    }

    private void createTwitterLinkShareIntent() {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setPackage("com.twitter.android");
        intent.setType("text/plain");
        this.successTwitter = true;
        this.hasShareMedia = true;
        this.mSource = "twitter";
        if (!(TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()) || TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etUrlReshare.getText().toString()))) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString());
            stringBuilder.append("\n");
            stringBuilder.append(((ActivityHomeBinding) getDataBinding()).etUrlReshare.getText().toString());
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Caption", stringBuilder.toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString());
            stringBuilder2.append("\n");
            stringBuilder2.append(((ActivityHomeBinding) getDataBinding()).etUrlReshare.getText().toString());
            intent.putExtra("android.intent.extra.TEXT", stringBuilder2.toString());
        }
        startActivity(Intent.createChooser(intent, "Send to Twitter"));
    }

    private void createTwitterIntent(String str, String str2) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType(str);
        intent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(str2)));
        intent.setPackage("com.twitter.android");
        this.successTwitter = true;
        this.hasShareMedia = true;
        this.mSource = "twitter";
        if (!TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString())) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Caption", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
            intent.putExtra("android.intent.extra.TEXT", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString());
        }
        startActivity(Intent.createChooser(intent, "Send to Twitter"));
    }

    private void photoOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage((CharSequence) "Add Photo");
        builder.setPositiveButton((CharSequence) "Capture", new -$$Lambda$MainActivity$u5HhCDlAUIwWMoSK_93vmeIJhdU(this));
        builder.setNegativeButton((CharSequence) "Gallery", new -$$Lambda$MainActivity$vZMN7O_f8CPfvRUe9Hyx0YSUVhY(this));
        builder.create().show();
    }

    private void failedCreateJob() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage((CharSequence) "Gagal memuat data");
        builder.setPositiveButton((CharSequence) "Tutup", new -$$Lambda$MainActivity$8mg2itXjaKJxt6oZf_FfK98VedM(this));
        builder.create().show();
    }

    public void videoOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage((CharSequence) "Add Video");
        builder.setPositiveButton((CharSequence) "Record", new -$$Lambda$MainActivity$0Z8BkOi3cVkppSoHjeC_vpU0bh4(this));
        builder.setNegativeButton((CharSequence) "Gallery", new -$$Lambda$MainActivity$uMWExI7peyfwu0NM9AJyjN9ACc4(this));
        builder.create().show();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        if (this.dialog != null && this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(this.mSource) && !this.mSource.equals(SambharConstant.SOURCE_FB)) {
            postNotify();
        }
    }

    private void postNotify() {
        if (!TextUtils.isEmpty(this.mSource) && (this.mSource.equals(SambharConstant.SOURCE_INSTAGRAM) || this.mSource.equals("twitter"))) {
            NotifyFormBody notifyFormBody = new NotifyFormBody();
            notifyFormBody.setJobId(this.mJobId);
            notifyFormBody.setSource(this.mSource);
            notifyFormBody.setCaption(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString());
            if (this.fileType == 1) {
                notifyFormBody.setFileName(this.notifyFileName);
            } else if (this.fileType == 2) {
                notifyFormBody.setFileName(this.notifyFileName);
            } else {
                notifyFormBody.setFileName("-");
            }
            notifyFormBody.setTs(this.mJobTs);
            notifyFormBody.setTwitterAccessToken(this.twitterAccessToken);
            notifyFormBody.setTwitterSecrets(this.twitterSecrets);
            ((MainViewModel) getViewModel()).notifyServer(notifyFormBody);
            this.mSource = "";
        }
        if (this.successFb && this.successIg && this.successTwitter) {
            handleOnResume();
        } else if (this.hasShareMedia) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage((CharSequence) "Apakah anda sudah selesai?");
            builder.setPositiveButton((CharSequence) "Ya", new -$$Lambda$MainActivity$TKqRAp914dqy42aQAWS-N_mNNYs(this));
            builder.setNegativeButton((CharSequence) "Tidak", -$$Lambda$MainActivity$mFAzNCuH58DMrp4sPLNB7acDVXo.INSTANCE);
            this.dialog = builder.create();
            if (this.dialog != null && !this.dialog.isShowing()) {
                this.dialog.show();
            }
        }
    }

    public static /* synthetic */ void lambda$postNotify$19(MainActivity mainActivity, DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        mainActivity.handleOnResume();
    }

    private void handleOnResume() {
        deleteImageDir();
        ((ActivityHomeBinding) getDataBinding()).ivImage.setImageBitmap(null);
        this.successIg = false;
        this.successFb = false;
        this.successTwitter = false;
        this.hasShareMedia = false;
        this.mCurrentPhotoPath = "";
        this.mCurrentVideoPath = "";
        this.reShareFacebook = false;
        this.reShareTwitter = false;
        ((ActivityHomeBinding) getDataBinding()).etCaption.setText("");
        ((ActivityHomeBinding) getDataBinding()).etUrlReshare.setText("");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.edit_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (itemId == R.id.logout) {
            this.mPref.clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(268468224);
            startActivity(intent);
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                ((ActivityHomeBinding) getDataBinding()).etCaption.setText(intent.getStringExtra("android.intent.extra.TEXT"));
            }
            if (type.startsWith("image/")) {
                Uri uri = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
                ((ActivityHomeBinding) getDataBinding()).etCaption.setText(intent.getStringExtra("android.intent.extra.TEXT"));
                this.imageGalleryUri = uri;
                File file = new File(getImagePathFromInputStreamUri(uri));
                this.mCurrentPhotoPath = file.getAbsolutePath();
                this.fileType = 1;
                ((MainViewModel) getViewModel()).createJob();
                Glide.with((FragmentActivity) this).asBitmap().load(file).into(((ActivityHomeBinding) getDataBinding()).ivImage);
            } else if (type.startsWith("video/")) {
                this.fileType = 2;
                this.fromGalleryVideo = true;
                ((ActivityHomeBinding) getDataBinding()).etCaption.setText(intent.getStringExtra("android.intent.extra.TEXT"));
                try {
                    this.videoGalletyUri = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
                    this.mCurrentVideoPath = new File(getImagePathFromInputStreamUri(this.videoGalletyUri)).getAbsolutePath();
                    ((MainViewModel) getViewModel()).createJob();
                    ((ActivityHomeBinding) getDataBinding()).ivImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(this.mCurrentVideoPath, 3));
                } catch (Exception e) {
                    Sentry.capture(e);
                    e.printStackTrace();
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
        super.onActivityResult(i, i2, intent);
        this.callbackManager.onActivityResult(i, i2, intent);
        if (i == 101) {
            if (i2 == -1) {
                this.successIg = false;
                this.successFb = false;
                this.successTwitter = false;
                this.fileType = 1;
                Glide.with((FragmentActivity) this).asBitmap().load(new File(this.mCurrentPhotoPath)).into(((ActivityHomeBinding) getDataBinding()).ivImage);
                ((MainViewModel) getViewModel()).createJob();
            }
        } else if (i == 102) {
            if (i2 == -1) {
                this.successIg = false;
                this.successFb = false;
                this.successTwitter = false;
                this.fileType = 2;
                this.fromGalleryVideo = false;
                ((MainViewModel) getViewModel()).createJob();
                try {
                    Uri data = intent.getData();
                    this.videoGalletyUri = data;
                    this.mCurrentVideoPath = getRealPathFromURI(this, data);
                    this.notifyFileName = this.mCurrentVideoPath;
                    ((ActivityHomeBinding) getDataBinding()).ivImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(this.mCurrentVideoPath, 3));
                } catch (Exception e) {
                    Sentry.capture(e);
                    e.printStackTrace();
                }
            }
        } else if (i == 103) {
            if (i2 == -1) {
                this.successIg = false;
                this.successFb = false;
                this.successTwitter = false;
                ((MainViewModel) getViewModel()).createJob();
                this.fileType = 1;
                try {
                    this.imageGalleryUri = intent.getData();
                    this.mCurrentPhotoPath = new File(getImagePathFromInputStreamUri(this.imageGalleryUri)).getAbsolutePath();
                    Glide.with((FragmentActivity) this).load(this.mCurrentPhotoPath).into(((ActivityHomeBinding) getDataBinding()).ivImage);
                } catch (Exception e2) {
                    Sentry.capture(e2);
                    e2.printStackTrace();
                }
            }
        } else if (i == 104) {
            if (i2 == -1) {
                this.successIg = false;
                this.successFb = false;
                this.successTwitter = false;
                ((MainViewModel) getViewModel()).createJob();
                this.fileType = 2;
                this.fromGalleryVideo = true;
                try {
                    this.videoGalletyUri = intent.getData();
                    File file = new File(getImagePathFromInputStreamUri(this.videoGalletyUri));
                    this.mCurrentVideoPath = file.getAbsolutePath();
                    ((ActivityHomeBinding) getDataBinding()).ivImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), 3));
                } catch (Exception e22) {
                    Sentry.capture(e22);
                    e22.printStackTrace();
                }
            }
        } else if (this.client != null) {
            this.client.onActivityResult(i, i2, intent);
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:22:0x0035=Splitter:B:22:0x0035, B:16:0x002c=Splitter:B:16:0x002c} */
    public java.lang.String getImagePathFromInputStreamUri(android.net.Uri r3) {
        /*
        r2 = this;
        r0 = r3.getAuthority();
        r1 = 0;
        if (r0 == 0) goto L_0x0051;
    L_0x0007:
        r0 = r2.getContentResolver();	 Catch:{ FileNotFoundException -> 0x0033, IOException -> 0x002a, all -> 0x0027 }
        r3 = r0.openInputStream(r3);	 Catch:{ FileNotFoundException -> 0x0033, IOException -> 0x002a, all -> 0x0027 }
        r0 = r2.createTemporalFileFrom(r3);	 Catch:{ FileNotFoundException -> 0x0025, IOException -> 0x0023 }
        r0 = r0.getPath();	 Catch:{ FileNotFoundException -> 0x0025, IOException -> 0x0023 }
        r3.close();	 Catch:{ IOException -> 0x001b }
        goto L_0x0052;
    L_0x001b:
        r3 = move-exception;
        io.sentry.Sentry.capture(r3);
        r3.printStackTrace();
        goto L_0x0052;
    L_0x0023:
        r0 = move-exception;
        goto L_0x002c;
    L_0x0025:
        r0 = move-exception;
        goto L_0x0035;
    L_0x0027:
        r0 = move-exception;
        r3 = r1;
        goto L_0x0045;
    L_0x002a:
        r0 = move-exception;
        r3 = r1;
    L_0x002c:
        io.sentry.Sentry.capture(r0);	 Catch:{ all -> 0x0044 }
        r3.close();	 Catch:{ IOException -> 0x003c }
        goto L_0x0051;
    L_0x0033:
        r0 = move-exception;
        r3 = r1;
    L_0x0035:
        io.sentry.Sentry.capture(r0);	 Catch:{ all -> 0x0044 }
        r3.close();	 Catch:{ IOException -> 0x003c }
        goto L_0x0051;
    L_0x003c:
        r3 = move-exception;
        io.sentry.Sentry.capture(r3);
        r3.printStackTrace();
        goto L_0x0051;
    L_0x0044:
        r0 = move-exception;
    L_0x0045:
        r3.close();	 Catch:{ IOException -> 0x0049 }
        goto L_0x0050;
    L_0x0049:
        r3 = move-exception;
        io.sentry.Sentry.capture(r3);
        r3.printStackTrace();
    L_0x0050:
        throw r0;
    L_0x0051:
        r0 = r1;
    L_0x0052:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sambhar.sambharappreport.page.main.MainActivity.getImagePathFromInputStreamUri(android.net.Uri):java.lang.String");
    }

    private File createTemporalFileFrom(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        byte[] bArr = new byte[8192];
        File createTemporalFile = createTemporalFile();
        FileOutputStream fileOutputStream = new FileOutputStream(createTemporalFile);
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                fileOutputStream.write(bArr, 0, read);
            } else {
                fileOutputStream.flush();
                try {
                    fileOutputStream.close();
                    return createTemporalFile;
                } catch (IOException e) {
                    Sentry.capture(e);
                    e.printStackTrace();
                    return createTemporalFile;
                }
            }
        }
    }

    private File createTemporalFile() {
        String format = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        StringBuilder stringBuilder;
        File externalFilesDir;
        StringBuilder stringBuilder2;
        if (this.fromGalleryVideo) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("xmbr_");
            stringBuilder.append(this.videoGalletyUri.getLastPathSegment());
            stringBuilder.append(".mp4");
            this.notifyFileName = stringBuilder.toString();
            externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(format);
            stringBuilder2.append("_tempFile.mp4");
            return new File(externalFilesDir, stringBuilder2.toString());
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("xmbr_");
        stringBuilder.append(this.imageGalleryUri.getLastPathSegment());
        stringBuilder.append(".jpg");
        this.notifyFileName = stringBuilder.toString();
        externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append(format);
        stringBuilder2.append("_tempFile.jpg");
        return new File(externalFilesDir, stringBuilder2.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002e  */
    public java.lang.String getRealPathFromURI(android.content.Context r9, android.net.Uri r10) {
        /*
        r8 = this;
        r0 = 1;
        r1 = 0;
        r4 = new java.lang.String[r0];	 Catch:{ all -> 0x002b }
        r0 = 0;
        r2 = "_data";
        r4[r0] = r2;	 Catch:{ all -> 0x002b }
        r2 = r9.getContentResolver();	 Catch:{ all -> 0x002b }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r3 = r10;
        r9 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x002b }
        r10 = "_data";
        r10 = r9.getColumnIndexOrThrow(r10);	 Catch:{ all -> 0x0028 }
        r9.moveToFirst();	 Catch:{ all -> 0x0028 }
        r10 = r9.getString(r10);	 Catch:{ all -> 0x0028 }
        if (r9 == 0) goto L_0x0027;
    L_0x0024:
        r9.close();
    L_0x0027:
        return r10;
    L_0x0028:
        r10 = move-exception;
        r1 = r9;
        goto L_0x002c;
    L_0x002b:
        r10 = move-exception;
    L_0x002c:
        if (r1 == 0) goto L_0x0031;
    L_0x002e:
        r1.close();
    L_0x0031:
        throw r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sambhar.sambharappreport.page.main.MainActivity.getRealPathFromURI(android.content.Context, android.net.Uri):java.lang.String");
    }

    private File createImageFile() throws IOException {
        String format = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(format);
        stringBuilder.append(EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR);
        File createTempFile = File.createTempFile(stringBuilder.toString(), ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        this.mCurrentPhotoPath = createTempFile.getAbsolutePath();
        this.notifyFileName = this.mCurrentPhotoPath;
        return createTempFile;
    }

    private void runPhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (intent.resolveActivity(getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                Sentry.capture(e);
                e.printStackTrace();
            }
            if (file != null) {
                this.imageGalleryUri = FileProvider.getUriForFile(this, "com.sambhar.sambharappreport.provider", file);
                intent.putExtra("output", this.imageGalleryUri);
                for (ResolveInfo resolveInfo : getPackageManager().queryIntentActivities(intent, 65536)) {
                    grantUriPermission(resolveInfo.activityInfo.packageName, this.imageGalleryUri, 3);
                }
                startActivityForResult(intent, 101);
            }
        }
    }

    private void takeVideo() {
        Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
        intent.putExtra("android.intent.extra.durationLimit", 60);
        intent.putExtra("android.intent.extra.videoQuality", 1);
        startActivityForResult(intent, 102);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 103);
    }

    private void pickVideoFromGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 104);
    }

    public void deleteImageDir() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());
        if (file.isDirectory()) {
            String[] list = file.list();
            for (String file2 : list) {
                new File(file, file2).delete();
            }
        }
    }

    private void updateStat() {
        ShambarTextView shambarTextView = ((ActivityHomeBinding) getDataBinding()).tvFacebookCount;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mPref.getFacebookCount());
        stringBuilder.append("");
        shambarTextView.setText(stringBuilder.toString());
        shambarTextView = ((ActivityHomeBinding) getDataBinding()).tvInstagramCount;
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.mPref.getInstagramCount());
        stringBuilder.append("");
        shambarTextView.setText(stringBuilder.toString());
        shambarTextView = ((ActivityHomeBinding) getDataBinding()).tvTwitterCount;
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.mPref.getTwitterCount());
        stringBuilder.append("");
        shambarTextView.setText(stringBuilder.toString());
    }

    private void checkAvailability() {
        if (this.mPref.getFacebookStatus() == 0) {
            ((ActivityHomeBinding) getDataBinding()).btShareFacebook.setVisibility(8);
            this.fbStatus = false;
            this.successFb = true;
        } else {
            ((ActivityHomeBinding) getDataBinding()).btShareFacebook.setVisibility(0);
            this.fbStatus = true;
            this.successFb = false;
        }
        if (this.mPref.getInstagramStatus() == 0) {
            ((ActivityHomeBinding) getDataBinding()).btShareIg.setVisibility(8);
            this.igStatus = false;
            this.successIg = true;
        } else {
            ((ActivityHomeBinding) getDataBinding()).btShareIg.setVisibility(0);
            this.igStatus = true;
            this.successIg = false;
        }
        if (this.mPref.getTwitterStatus() == 0) {
            ((ActivityHomeBinding) getDataBinding()).btShareTwitter.setVisibility(8);
            this.twitterStatus = false;
            this.successTwitter = true;
            return;
        }
        ((ActivityHomeBinding) getDataBinding()).btShareTwitter.setVisibility(0);
        this.twitterStatus = true;
        this.successTwitter = false;
    }

    private void shareWithTwitterSdk(int i) {
        this.mSource = "twitter";
        TwitterSession twitterSession = (TwitterSession) TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession == null) {
            authenticateUser(i);
        } else {
            mediaUpload(twitterSession, i);
        }
    }

    private void mediaUpload(final TwitterSession twitterSession, int i) {
        showLoading(getString(R.string.please_wait));
        TwitterApiClient apiClient = TwitterCore.getInstance().getApiClient(twitterSession);
        if (i == 6) {
            apiClient.getMediaService().upload(RequestBody.create(MediaType.parse("multipart/form-data"), new File(this.mCurrentPhotoPath)), null, null).enqueue(new Callback<Media>() {
                public void success(com.twitter.sdk.android.core.Result<Media> result) {
                    MainActivity.this.dismissLoading();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(((Media) result.data).mediaIdString);
                    stringBuilder.append("");
                    Log.i("Success Upload", stringBuilder.toString());
                    MainActivity.this.tweetUsingApi(twitterSession, ((Media) result.data).mediaIdString);
                }

                public void failure(TwitterException twitterException) {
                    MainActivity.this.dismissLoading();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(twitterException.getMessage());
                    stringBuilder.append("");
                    Log.i("Failed Upload", stringBuilder.toString());
                }
            });
        } else if (i == 7) {
            File file = new File(this.mCurrentVideoPath);
            final TwitterVideoApiClient twitterVideoApiClient = new TwitterVideoApiClient(twitterSession, new OkHttpClient.Builder().addInterceptor(new ChuckInterceptor(this)).build());
            final byte[] byteArray = getByteArray(file);
            final int parseInt = Integer.parseInt(String.valueOf(file.length()));
            RequestBody.create(MediaType.parse("multipart/form-data"), "video/mp4");
            RequestBody.create(MediaType.parse("multipart/form-data"), "INIT");
            RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(parseInt));
            RequestBody.create(MediaType.parse("multipart/form-data"), "tweet_video");
            final TwitterSession twitterSession2 = twitterSession;
            twitterVideoApiClient.getCustomService().uploadVideoInit("INIT", "video/mp4", String.valueOf(parseInt), "tweet_video").enqueue(new Callback<Media>() {
                public void success(com.twitter.sdk.android.core.Result<Media> result) {
                    Log.i("Success INIT", ((Media) result.data).mediaIdString);
                    MainActivity.this.appendVideo(twitterSession2, twitterVideoApiClient, ((Media) result.data).mediaIdString, byteArray, 0, parseInt);
                }

                public void failure(TwitterException twitterException) {
                    MainActivity.this.dismissLoading();
                    Log.i("Failed INIT", twitterException.getMessage());
                }
            });
        }
    }

    private void appendVideo(TwitterSession twitterSession, TwitterVideoApiClient twitterVideoApiClient, String str, byte[] bArr, int i, int i2) {
        final int i3 = i * 1000000;
        boolean z = 1000000 + i3 >= i2;
        final byte[] bArr2 = bArr;
        final boolean z2 = z;
        final int i4 = i2;
        AnonymousClass4 anonymousClass4 = new RequestBody(1000000) {
            @javax.annotation.Nullable
            public MediaType contentType() {
                return MediaType.parse("multipart/form-data");
            }

            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.write(bArr2, i3, z2 ? i4 - i3 : 1000000);
            }
        };
        final String str2 = str;
        Call uploadVideoAppend = twitterVideoApiClient.getCustomService().uploadVideoAppend(RequestBody.create(MediaType.parse("multipart/form-data"), "APPEND"), RequestBody.create(MediaType.parse("multipart/form-data"), str2), anonymousClass4, RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(i)));
        final int i5 = i;
        final boolean z3 = z;
        final TwitterVideoApiClient twitterVideoApiClient2 = twitterVideoApiClient;
        final TwitterSession twitterSession2 = twitterSession;
        final byte[] bArr3 = bArr;
        uploadVideoAppend.enqueue(new Callback<String>() {
            public void success(com.twitter.sdk.android.core.Result<String> result) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("SUCCESS ");
                stringBuilder.append(i5);
                Log.i("APPEND", stringBuilder.toString());
                if (z3) {
                    MainActivity.this.finalizeVideo(twitterVideoApiClient2, str2, twitterSession2);
                    return;
                }
                MainActivity.this.appendVideo(twitterSession2, twitterVideoApiClient2, str2, bArr3, i5 + 1, i4);
            }

            public void failure(TwitterException twitterException) {
                MainActivity.this.dismissLoading();
                Log.i("APPEND", twitterException.getMessage());
            }
        });
    }

    private void finalizeVideo(final TwitterVideoApiClient twitterVideoApiClient, final String str, final TwitterSession twitterSession) {
        RequestBody create = RequestBody.create(MediaType.parse("multipart/form-data"), "FINALIZE");
        RequestBody create2 = RequestBody.create(MediaType.parse("multipart/form-data"), str);
        RequestBody.create(MediaType.parse("multipart/form-data"), "tweet_video");
        twitterVideoApiClient.getCustomService().finalizeUpload(create, create2).enqueue(new Callback<Media>() {
            public void success(com.twitter.sdk.android.core.Result<Media> result) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("SUCCESS");
                stringBuilder.append(result.response.code());
                Log.i("FINALIZE ", stringBuilder.toString());
                MainActivity.this.checkStatus(twitterVideoApiClient, twitterSession, str);
            }

            public void failure(TwitterException twitterException) {
                MainActivity.this.dismissLoading();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("FAILED");
                stringBuilder.append(twitterException.getLocalizedMessage());
                Log.i("FINALIZE ", stringBuilder.toString());
            }
        });
    }

    private byte[] getByteArray(File file) {
        byte[] bArr = new byte[((int) file.length())];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bArr);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            Sentry.capture(e);
            e.printStackTrace();
        } catch (IOException e2) {
            Sentry.capture(e2);
            e2.printStackTrace();
        }
        return bArr;
    }

    private void authenticateUser(int i) {
        this.client = new TwitterAuthClient();
        this.client.authorize(this, new Callback<TwitterSession>() {
            public void success(com.twitter.sdk.android.core.Result<TwitterSession> result) {
                Toast.makeText(MainActivity.this, "Login successful.", 0).show();
                Log.i("Token Twitter", ((TwitterAuthToken) ((TwitterSession) result.data).getAuthToken()).token);
                Log.i("Secret Twitter", ((TwitterAuthToken) ((TwitterSession) result.data).getAuthToken()).secret);
                MainActivity.this.twitterAccessToken = ((TwitterAuthToken) ((TwitterSession) result.data).getAuthToken()).token;
                MainActivity.this.twitterSecrets = ((TwitterAuthToken) ((TwitterSession) result.data).getAuthToken()).secret;
                MainActivity.this.twitterUserId = ((TwitterSession) result.data).getUserId();
                JobCheckBodyPost jobCheckBodyPost = new JobCheckBodyPost();
                jobCheckBodyPost.setJobId(MainActivity.this.mJobId);
                jobCheckBodyPost.setTwitterAccessToken(MainActivity.this.twitterAccessToken);
                jobCheckBodyPost.setTwitterSecrets(MainActivity.this.twitterSecrets);
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                ((MainViewModel) MainActivity.this.getViewModel()).checkJob(jobCheckBodyPost);
            }

            public void failure(TwitterException twitterException) {
                Log.i("Error Log Twitter", twitterException.getLocalizedMessage());
                Log.i("Error Log Twitter", twitterException.getMessage());
                Log.i("Error Log Twitter", twitterException.getStackTrace().toString());
                Toast.makeText(MainActivity.this, "Failed to authenticate by Twitter. Please try again.", 0).show();
            }
        });
    }

    private void checkStatus(final TwitterVideoApiClient twitterVideoApiClient, final TwitterSession twitterSession, final String str) {
        twitterVideoApiClient.getCustomService().statusUpload("STATUS", str).enqueue(new Callback<MediaTwitterEntity>() {
            public void success(com.twitter.sdk.android.core.Result<MediaTwitterEntity> result) {
                Log.i("STATE STATUS ", ((MediaTwitterEntity) result.data).getInfo().getState());
                if (((MediaTwitterEntity) result.data).getInfo().getState().equalsIgnoreCase("in_progress")) {
                    MainActivity.this.checkStatus(twitterVideoApiClient, twitterSession, str);
                } else if (((MediaTwitterEntity) result.data).getInfo().getState().equalsIgnoreCase(AnalyticsEvents.PARAMETER_SHARE_OUTCOME_SUCCEEDED)) {
                    MainActivity.this.tweetUsingApi(twitterSession, str);
                } else {
                    MainActivity.this.dismissLoading();
                    MainActivity.this.showErrorSnackbar(((ActivityHomeBinding) MainActivity.this.getDataBinding()).llHomeRoot, result.response.message());
                }
            }

            public void failure(TwitterException twitterException) {
                MainActivity.this.dismissLoading();
                Log.i("Error STATUS ", twitterException.getMessage());
            }
        });
    }

    private void tweetUsingApi(TwitterSession twitterSession, String str) {
        TwitterCore.getInstance().getApiClient(twitterSession).getStatusesService().update(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString(), null, Boolean.valueOf(false), null, null, null, Boolean.valueOf(false), Boolean.valueOf(false), str).enqueue(new Callback<Tweet>() {
            public void success(com.twitter.sdk.android.core.Result<Tweet> result) {
                MainActivity.this.dismissLoading();
                Log.i("Success", ((Tweet) result.data).text.toString());
                NotifyFormBody notifyFormBody = new NotifyFormBody();
                notifyFormBody.setJobId(MainActivity.this.mJobId);
                notifyFormBody.setSource(MainActivity.this.mSource);
                notifyFormBody.setCaption(((ActivityHomeBinding) MainActivity.this.getDataBinding()).etCaption.getText().toString());
                if (MainActivity.this.fileType == 1) {
                    notifyFormBody.setFileName(MainActivity.this.notifyFileName);
                } else if (MainActivity.this.fileType == 2) {
                    notifyFormBody.setFileName(MainActivity.this.notifyFileName);
                }
                MainActivity.this.hasShareMedia = true;
                MainActivity.this.successTwitter = true;
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                ((MainViewModel) MainActivity.this.getViewModel()).notifyServer(notifyFormBody);
            }

            public void failure(TwitterException twitterException) {
                MainActivity.this.dismissLoading();
                Log.i(AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_FAILED, twitterException.getMessage());
            }
        });
    }

    private void sharePhotoFacebook() {
        if (!TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString())) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Caption", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
        }
        this.mSource = SambharConstant.SOURCE_FB;
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        this.shareDialog.show(new SharePhotoContent.Builder().addPhoto(new SharePhoto.Builder().setBitmap(BitmapFactory.decodeFile(this.mCurrentPhotoPath, options)).build()).build());
    }

    private void shareVideoFacebook() {
        if (!TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString())) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Caption", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
        }
        this.mSource = SambharConstant.SOURCE_FB;
        this.shareDialog.show(new ShareVideoContent.Builder().setVideo(new ShareVideo.Builder().setLocalUrl(this.videoGalletyUri).build()).build(), Mode.AUTOMATIC);
    }

    private void setupTwitterSdk(String str, String str2) {
        Twitter.initialize(new TwitterConfig.Builder(this).logger(new DefaultLogger(3)).twitterAuthConfig(new TwitterAuthConfig(str, str2)).debug(true).build());
    }

    private void shareLinkFacebook(String str) {
        if (!TextUtils.isEmpty(((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString())) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Caption", ((ActivityHomeBinding) getDataBinding()).etCaption.getText().toString()));
            Toast.makeText(this, "Caption copy to clipboard", 0).show();
        }
        this.shareDialog.show(((ShareLinkContent.Builder) new ShareLinkContent.Builder().setContentUrl(Uri.parse(str))).build());
    }
}
