package com.facebook.internal;

import android.app.Activity;
import android.util.Log;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookDialog;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import java.util.List;

public abstract class FacebookDialogBase<CONTENT, RESULT> implements FacebookDialog<CONTENT, RESULT> {
    protected static final Object BASE_AUTOMATIC_MODE = new Object();
    private static final String TAG = "FacebookDialog";
    private final Activity activity;
    private final FragmentWrapper fragmentWrapper;
    private List<ModeHandler> modeHandlers;
    private int requestCode;

    protected abstract class ModeHandler {
        public abstract boolean canShow(CONTENT content, boolean z);

        public abstract AppCall createAppCall(CONTENT content);

        protected ModeHandler() {
        }

        public Object getMode() {
            return FacebookDialogBase.BASE_AUTOMATIC_MODE;
        }
    }

    public abstract AppCall createBaseAppCall();

    public abstract List<ModeHandler> getOrderedModeHandlers();

    public abstract void registerCallbackImpl(CallbackManagerImpl callbackManagerImpl, FacebookCallback<RESULT> facebookCallback);

    protected FacebookDialogBase(Activity activity, int i) {
        Validate.notNull(activity, "activity");
        this.activity = activity;
        this.fragmentWrapper = null;
        this.requestCode = i;
    }

    protected FacebookDialogBase(FragmentWrapper fragmentWrapper, int i) {
        Validate.notNull(fragmentWrapper, "fragmentWrapper");
        this.fragmentWrapper = fragmentWrapper;
        this.activity = null;
        this.requestCode = i;
        if (fragmentWrapper.getActivity() == null) {
            throw new IllegalArgumentException("Cannot use a fragment that is not attached to an activity");
        }
    }

    public final void registerCallback(CallbackManager callbackManager, FacebookCallback<RESULT> facebookCallback) {
        if (callbackManager instanceof CallbackManagerImpl) {
            registerCallbackImpl((CallbackManagerImpl) callbackManager, facebookCallback);
            return;
        }
        throw new FacebookException("Unexpected CallbackManager, please use the provided Factory.");
    }

    public final void registerCallback(CallbackManager callbackManager, FacebookCallback<RESULT> facebookCallback, int i) {
        setRequestCode(i);
        registerCallback(callbackManager, facebookCallback);
    }

    /* Access modifiers changed, original: protected */
    public void setRequestCode(int i) {
        if (FacebookSdk.isFacebookRequestCode(i)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Request code ");
            stringBuilder.append(i);
            stringBuilder.append(" cannot be within the range reserved by the Facebook SDK.");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        this.requestCode = i;
    }

    public int getRequestCode() {
        return this.requestCode;
    }

    public boolean canShow(CONTENT content) {
        return canShowImpl(content, BASE_AUTOMATIC_MODE);
    }

    /* Access modifiers changed, original: protected */
    public boolean canShowImpl(CONTENT content, Object obj) {
        Object obj2 = obj == BASE_AUTOMATIC_MODE ? 1 : null;
        for (ModeHandler modeHandler : cachedModeHandlers()) {
            if (obj2 != null || Utility.areObjectsEqual(modeHandler.getMode(), obj)) {
                if (modeHandler.canShow(content, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void show(CONTENT content) {
        showImpl(content, BASE_AUTOMATIC_MODE);
    }

    /* Access modifiers changed, original: protected */
    public void showImpl(CONTENT content, Object obj) {
        AppCall createAppCallForMode = createAppCallForMode(content, obj);
        if (createAppCallForMode == null) {
            String str = "No code path should ever result in a null appCall";
            Log.e(TAG, str);
            if (FacebookSdk.isDebugEnabled()) {
                throw new IllegalStateException(str);
            }
        } else if (this.fragmentWrapper != null) {
            DialogPresenter.present(createAppCallForMode, this.fragmentWrapper);
        } else {
            DialogPresenter.present(createAppCallForMode, this.activity);
        }
    }

    /* Access modifiers changed, original: protected */
    public Activity getActivityContext() {
        if (this.activity != null) {
            return this.activity;
        }
        return this.fragmentWrapper != null ? this.fragmentWrapper.getActivity() : null;
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:17:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003a  */
    public void startActivityForResult(android.content.Intent r3, int r4) {
        /*
        r2 = this;
        r0 = r2.activity;
        if (r0 == 0) goto L_0x000a;
    L_0x0004:
        r0 = r2.activity;
        r0.startActivityForResult(r3, r4);
        goto L_0x0031;
    L_0x000a:
        r0 = r2.fragmentWrapper;
        if (r0 == 0) goto L_0x0036;
    L_0x000e:
        r0 = r2.fragmentWrapper;
        r0 = r0.getNativeFragment();
        if (r0 == 0) goto L_0x0020;
    L_0x0016:
        r0 = r2.fragmentWrapper;
        r0 = r0.getNativeFragment();
        r0.startActivityForResult(r3, r4);
        goto L_0x0031;
    L_0x0020:
        r0 = r2.fragmentWrapper;
        r0 = r0.getSupportFragment();
        if (r0 == 0) goto L_0x0033;
    L_0x0028:
        r0 = r2.fragmentWrapper;
        r0 = r0.getSupportFragment();
        r0.startActivityForResult(r3, r4);
    L_0x0031:
        r3 = 0;
        goto L_0x0038;
    L_0x0033:
        r3 = "Failed to find Activity or Fragment to startActivityForResult ";
        goto L_0x0038;
    L_0x0036:
        r3 = "Failed to find Activity or Fragment to startActivityForResult ";
    L_0x0038:
        if (r3 == 0) goto L_0x0048;
    L_0x003a:
        r4 = com.facebook.LoggingBehavior.DEVELOPER_ERRORS;
        r0 = 6;
        r1 = r2.getClass();
        r1 = r1.getName();
        com.facebook.internal.Logger.log(r4, r0, r1, r3);
    L_0x0048:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.internal.FacebookDialogBase.startActivityForResult(android.content.Intent, int):void");
    }

    private AppCall createAppCallForMode(CONTENT content, Object obj) {
        Object obj2 = obj == BASE_AUTOMATIC_MODE ? 1 : null;
        AppCall appCall = null;
        for (ModeHandler modeHandler : cachedModeHandlers()) {
            if (obj2 != null || Utility.areObjectsEqual(modeHandler.getMode(), obj)) {
                if (modeHandler.canShow(content, true)) {
                    try {
                        appCall = modeHandler.createAppCall(content);
                        break;
                    } catch (FacebookException e) {
                        appCall = createBaseAppCall();
                        DialogPresenter.setupAppCallForValidationError(appCall, e);
                    }
                }
            }
        }
        if (appCall != null) {
            return appCall;
        }
        appCall = createBaseAppCall();
        DialogPresenter.setupAppCallForCannotShowError(appCall);
        return appCall;
    }

    private List<ModeHandler> cachedModeHandlers() {
        if (this.modeHandlers == null) {
            this.modeHandlers = getOrderedModeHandlers();
        }
        return this.modeHandlers;
    }
}
