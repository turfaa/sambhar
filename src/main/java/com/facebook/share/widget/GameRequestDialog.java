package com.facebook.share.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.facebook.FacebookCallback;
import com.facebook.internal.AppCall;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.internal.CallbackManagerImpl.Callback;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;
import com.facebook.internal.DialogPresenter;
import com.facebook.internal.FacebookDialogBase;
import com.facebook.internal.FragmentWrapper;
import com.facebook.share.internal.GameRequestValidation;
import com.facebook.share.internal.ResultProcessor;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.internal.ShareInternalUtility;
import com.facebook.share.internal.WebDialogParameters;
import com.facebook.share.model.GameRequestContent;
import java.util.ArrayList;
import java.util.List;

public class GameRequestDialog extends FacebookDialogBase<GameRequestContent, Result> {
    private static final int DEFAULT_REQUEST_CODE = RequestCodeOffset.GameRequest.toRequestCode();
    private static final String GAME_REQUEST_DIALOG = "apprequests";

    public static final class Result {
        String requestId;
        List<String> to;

        /* synthetic */ Result(Bundle bundle, AnonymousClass1 anonymousClass1) {
            this(bundle);
        }

        private Result(Bundle bundle) {
            this.requestId = bundle.getString(ShareConstants.WEB_DIALOG_RESULT_PARAM_REQUEST_ID);
            this.to = new ArrayList();
            while (true) {
                if (bundle.containsKey(String.format(ShareConstants.WEB_DIALOG_RESULT_PARAM_TO_ARRAY_MEMBER, new Object[]{Integer.valueOf(this.to.size())}))) {
                    this.to.add(bundle.getString(String.format(ShareConstants.WEB_DIALOG_RESULT_PARAM_TO_ARRAY_MEMBER, new Object[]{Integer.valueOf(this.to.size())})));
                } else {
                    return;
                }
            }
        }

        public String getRequestId() {
            return this.requestId;
        }

        public List<String> getRequestRecipients() {
            return this.to;
        }
    }

    private class WebHandler extends ModeHandler {
        public boolean canShow(GameRequestContent gameRequestContent, boolean z) {
            return true;
        }

        private WebHandler() {
            super();
        }

        /* synthetic */ WebHandler(GameRequestDialog gameRequestDialog, AnonymousClass1 anonymousClass1) {
            this();
        }

        public AppCall createAppCall(GameRequestContent gameRequestContent) {
            GameRequestValidation.validate(gameRequestContent);
            AppCall createBaseAppCall = GameRequestDialog.this.createBaseAppCall();
            DialogPresenter.setupAppCallForWebDialog(createBaseAppCall, GameRequestDialog.GAME_REQUEST_DIALOG, WebDialogParameters.create(gameRequestContent));
            return createBaseAppCall;
        }
    }

    public static boolean canShow() {
        return true;
    }

    public static void show(Activity activity, GameRequestContent gameRequestContent) {
        new GameRequestDialog(activity).show(gameRequestContent);
    }

    public static void show(Fragment fragment, GameRequestContent gameRequestContent) {
        show(new FragmentWrapper(fragment), gameRequestContent);
    }

    public static void show(android.app.Fragment fragment, GameRequestContent gameRequestContent) {
        show(new FragmentWrapper(fragment), gameRequestContent);
    }

    private static void show(FragmentWrapper fragmentWrapper, GameRequestContent gameRequestContent) {
        new GameRequestDialog(fragmentWrapper).show(gameRequestContent);
    }

    public GameRequestDialog(Activity activity) {
        super(activity, DEFAULT_REQUEST_CODE);
    }

    public GameRequestDialog(Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    public GameRequestDialog(android.app.Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    private GameRequestDialog(FragmentWrapper fragmentWrapper) {
        super(fragmentWrapper, DEFAULT_REQUEST_CODE);
    }

    /* Access modifiers changed, original: protected */
    public void registerCallbackImpl(CallbackManagerImpl callbackManagerImpl, final FacebookCallback<Result> facebookCallback) {
        ResultProcessor resultProcessor;
        if (facebookCallback == null) {
            resultProcessor = null;
        } else {
            resultProcessor = new ResultProcessor(facebookCallback) {
                public void onSuccess(AppCall appCall, Bundle bundle) {
                    if (bundle != null) {
                        facebookCallback.onSuccess(new Result(bundle, null));
                    } else {
                        onCancel(appCall);
                    }
                }
            };
        }
        callbackManagerImpl.registerCallback(getRequestCode(), new Callback() {
            public boolean onActivityResult(int i, Intent intent) {
                return ShareInternalUtility.handleActivityResult(GameRequestDialog.this.getRequestCode(), i, intent, resultProcessor);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public AppCall createBaseAppCall() {
        return new AppCall(getRequestCode());
    }

    /* Access modifiers changed, original: protected */
    public List<ModeHandler> getOrderedModeHandlers() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new WebHandler(this, null));
        return arrayList;
    }
}
