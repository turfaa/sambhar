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
import com.facebook.share.internal.ResultProcessor;
import com.facebook.share.internal.ShareConstants;
import com.facebook.share.internal.ShareInternalUtility;
import com.facebook.share.internal.WebDialogParameters;
import com.facebook.share.model.AppGroupCreationContent;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class CreateAppGroupDialog extends FacebookDialogBase<AppGroupCreationContent, Result> {
    private static final int DEFAULT_REQUEST_CODE = RequestCodeOffset.AppGroupCreate.toRequestCode();
    private static final String GAME_GROUP_CREATION_DIALOG = "game_group_create";

    @Deprecated
    public static final class Result {
        private final String id;

        /* synthetic */ Result(String str, AnonymousClass1 anonymousClass1) {
            this(str);
        }

        private Result(String str) {
            this.id = str;
        }

        public String getId() {
            return this.id;
        }
    }

    private class WebHandler extends ModeHandler {
        public boolean canShow(AppGroupCreationContent appGroupCreationContent, boolean z) {
            return true;
        }

        private WebHandler() {
            super();
        }

        /* synthetic */ WebHandler(CreateAppGroupDialog createAppGroupDialog, AnonymousClass1 anonymousClass1) {
            this();
        }

        public AppCall createAppCall(AppGroupCreationContent appGroupCreationContent) {
            AppCall createBaseAppCall = CreateAppGroupDialog.this.createBaseAppCall();
            DialogPresenter.setupAppCallForWebDialog(createBaseAppCall, CreateAppGroupDialog.GAME_GROUP_CREATION_DIALOG, WebDialogParameters.create(appGroupCreationContent));
            return createBaseAppCall;
        }
    }

    @Deprecated
    public static boolean canShow() {
        return true;
    }

    @Deprecated
    public static void show(Activity activity, AppGroupCreationContent appGroupCreationContent) {
        new CreateAppGroupDialog(activity).show(appGroupCreationContent);
    }

    @Deprecated
    public static void show(Fragment fragment, AppGroupCreationContent appGroupCreationContent) {
        show(new FragmentWrapper(fragment), appGroupCreationContent);
    }

    @Deprecated
    public static void show(android.app.Fragment fragment, AppGroupCreationContent appGroupCreationContent) {
        show(new FragmentWrapper(fragment), appGroupCreationContent);
    }

    private static void show(FragmentWrapper fragmentWrapper, AppGroupCreationContent appGroupCreationContent) {
        new CreateAppGroupDialog(fragmentWrapper).show(appGroupCreationContent);
    }

    @Deprecated
    public CreateAppGroupDialog(Activity activity) {
        super(activity, DEFAULT_REQUEST_CODE);
    }

    @Deprecated
    public CreateAppGroupDialog(Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    @Deprecated
    public CreateAppGroupDialog(android.app.Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    private CreateAppGroupDialog(FragmentWrapper fragmentWrapper) {
        super(fragmentWrapper, DEFAULT_REQUEST_CODE);
    }

    /* Access modifiers changed, original: protected */
    public void registerCallbackImpl(CallbackManagerImpl callbackManagerImpl, final FacebookCallback<Result> facebookCallback) {
        final ResultProcessor anonymousClass1 = facebookCallback == null ? null : new ResultProcessor(facebookCallback) {
            public void onSuccess(AppCall appCall, Bundle bundle) {
                facebookCallback.onSuccess(new Result(bundle.getString(ShareConstants.WEB_DIALOG_PARAM_ID), null));
            }
        };
        callbackManagerImpl.registerCallback(getRequestCode(), new Callback() {
            public boolean onActivityResult(int i, Intent intent) {
                return ShareInternalUtility.handleActivityResult(CreateAppGroupDialog.this.getRequestCode(), i, intent, anonymousClass1);
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
