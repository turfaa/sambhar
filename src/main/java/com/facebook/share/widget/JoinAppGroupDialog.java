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
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class JoinAppGroupDialog extends FacebookDialogBase<String, Result> {
    private static final int DEFAULT_REQUEST_CODE = RequestCodeOffset.AppGroupJoin.toRequestCode();
    private static final String JOIN_GAME_GROUP_DIALOG = "game_group_join";

    @Deprecated
    public static final class Result {
        private final Bundle data;

        /* synthetic */ Result(Bundle bundle, AnonymousClass1 anonymousClass1) {
            this(bundle);
        }

        private Result(Bundle bundle) {
            this.data = bundle;
        }

        public Bundle getData() {
            return this.data;
        }
    }

    private class WebHandler extends ModeHandler {
        public boolean canShow(String str, boolean z) {
            return true;
        }

        private WebHandler() {
            super();
        }

        /* synthetic */ WebHandler(JoinAppGroupDialog joinAppGroupDialog, AnonymousClass1 anonymousClass1) {
            this();
        }

        public AppCall createAppCall(String str) {
            AppCall createBaseAppCall = JoinAppGroupDialog.this.createBaseAppCall();
            Bundle bundle = new Bundle();
            bundle.putString(ShareConstants.WEB_DIALOG_PARAM_ID, str);
            DialogPresenter.setupAppCallForWebDialog(createBaseAppCall, JoinAppGroupDialog.JOIN_GAME_GROUP_DIALOG, bundle);
            return createBaseAppCall;
        }
    }

    @Deprecated
    public static boolean canShow() {
        return true;
    }

    @Deprecated
    public static void show(Activity activity, String str) {
        new JoinAppGroupDialog(activity).show(str);
    }

    @Deprecated
    public static void show(Fragment fragment, String str) {
        show(new FragmentWrapper(fragment), str);
    }

    @Deprecated
    public static void show(android.app.Fragment fragment, String str) {
        show(new FragmentWrapper(fragment), str);
    }

    private static void show(FragmentWrapper fragmentWrapper, String str) {
        new JoinAppGroupDialog(fragmentWrapper).show(str);
    }

    @Deprecated
    public JoinAppGroupDialog(Activity activity) {
        super(activity, DEFAULT_REQUEST_CODE);
    }

    @Deprecated
    public JoinAppGroupDialog(Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    @Deprecated
    public JoinAppGroupDialog(android.app.Fragment fragment) {
        this(new FragmentWrapper(fragment));
    }

    private JoinAppGroupDialog(FragmentWrapper fragmentWrapper) {
        super(fragmentWrapper, DEFAULT_REQUEST_CODE);
    }

    /* Access modifiers changed, original: protected */
    public void registerCallbackImpl(CallbackManagerImpl callbackManagerImpl, final FacebookCallback<Result> facebookCallback) {
        final ResultProcessor anonymousClass1 = facebookCallback == null ? null : new ResultProcessor(facebookCallback) {
            public void onSuccess(AppCall appCall, Bundle bundle) {
                facebookCallback.onSuccess(new Result(bundle, null));
            }
        };
        callbackManagerImpl.registerCallback(getRequestCode(), new Callback() {
            public boolean onActivityResult(int i, Intent intent) {
                return ShareInternalUtility.handleActivityResult(JoinAppGroupDialog.this.getRequestCode(), i, intent, anonymousClass1);
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
