package com.facebook.share.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.facebook.FacebookCallback;
import com.facebook.internal.AppCall;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.internal.CallbackManagerImpl.Callback;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;
import com.facebook.internal.DialogFeature;
import com.facebook.internal.DialogPresenter;
import com.facebook.internal.DialogPresenter.ParameterProvider;
import com.facebook.internal.FacebookDialogBase;
import com.facebook.internal.FragmentWrapper;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class LikeDialog extends FacebookDialogBase<LikeContent, Result> {
    private static final int DEFAULT_REQUEST_CODE = RequestCodeOffset.Like.toRequestCode();
    private static final String TAG = "LikeDialog";

    @Deprecated
    public static final class Result {
        private final Bundle bundle;

        public Result(Bundle bundle) {
            this.bundle = bundle;
        }

        public Bundle getData() {
            return this.bundle;
        }
    }

    private class NativeHandler extends ModeHandler {
        public boolean canShow(LikeContent likeContent, boolean z) {
            return false;
        }

        private NativeHandler() {
            super();
        }

        /* synthetic */ NativeHandler(LikeDialog likeDialog, AnonymousClass1 anonymousClass1) {
            this();
        }

        public AppCall createAppCall(final LikeContent likeContent) {
            AppCall createBaseAppCall = LikeDialog.this.createBaseAppCall();
            DialogPresenter.setupAppCallForNativeDialog(createBaseAppCall, new ParameterProvider() {
                public Bundle getParameters() {
                    return LikeDialog.createParameters(likeContent);
                }

                public Bundle getLegacyParameters() {
                    Log.e(LikeDialog.TAG, "Attempting to present the Like Dialog with an outdated Facebook app on the device");
                    return new Bundle();
                }
            }, LikeDialog.getFeature());
            return createBaseAppCall;
        }
    }

    private class WebFallbackHandler extends ModeHandler {
        public boolean canShow(LikeContent likeContent, boolean z) {
            return false;
        }

        private WebFallbackHandler() {
            super();
        }

        /* synthetic */ WebFallbackHandler(LikeDialog likeDialog, AnonymousClass1 anonymousClass1) {
            this();
        }

        public AppCall createAppCall(LikeContent likeContent) {
            AppCall createBaseAppCall = LikeDialog.this.createBaseAppCall();
            DialogPresenter.setupAppCallForWebFallbackDialog(createBaseAppCall, LikeDialog.createParameters(likeContent), LikeDialog.getFeature());
            return createBaseAppCall;
        }
    }

    @Deprecated
    public static boolean canShowNativeDialog() {
        return false;
    }

    @Deprecated
    public static boolean canShowWebFallback() {
        return false;
    }

    @Deprecated
    public void show(LikeContent likeContent) {
    }

    @Deprecated
    public LikeDialog(Activity activity) {
        super(activity, DEFAULT_REQUEST_CODE);
    }

    @Deprecated
    public LikeDialog(Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    @Deprecated
    public LikeDialog(android.app.Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    @Deprecated
    public LikeDialog(FragmentWrapper fragmentWrapper) {
        super(fragmentWrapper, DEFAULT_REQUEST_CODE);
    }

    /* Access modifiers changed, original: protected */
    public AppCall createBaseAppCall() {
        return new AppCall(getRequestCode());
    }

    /* Access modifiers changed, original: protected */
    public List<ModeHandler> getOrderedModeHandlers() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new NativeHandler(this, null));
        arrayList.add(new WebFallbackHandler(this, null));
        return arrayList;
    }

    /* Access modifiers changed, original: protected */
    public void registerCallbackImpl(CallbackManagerImpl callbackManagerImpl, final FacebookCallback<Result> facebookCallback) {
        final ResultProcessor anonymousClass1 = facebookCallback == null ? null : new ResultProcessor(facebookCallback) {
            public void onSuccess(AppCall appCall, Bundle bundle) {
                facebookCallback.onSuccess(new Result(bundle));
            }
        };
        callbackManagerImpl.registerCallback(getRequestCode(), new Callback() {
            public boolean onActivityResult(int i, Intent intent) {
                return ShareInternalUtility.handleActivityResult(LikeDialog.this.getRequestCode(), i, intent, anonymousClass1);
            }
        });
    }

    private static DialogFeature getFeature() {
        return LikeDialogFeature.LIKE_DIALOG;
    }

    private static Bundle createParameters(LikeContent likeContent) {
        Bundle bundle = new Bundle();
        bundle.putString("object_id", likeContent.getObjectId());
        bundle.putString("object_type", likeContent.getObjectType());
        return bundle;
    }
}
