package org.greenrobot.eventbus.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;

public class ErrorDialogManager {
    public static final String KEY_EVENT_TYPE_ON_CLOSE = "de.greenrobot.eventbus.errordialog.event_type_on_close";
    public static final String KEY_FINISH_AFTER_DIALOG = "de.greenrobot.eventbus.errordialog.finish_after_dialog";
    public static final String KEY_ICON_ID = "de.greenrobot.eventbus.errordialog.icon_id";
    public static final String KEY_MESSAGE = "de.greenrobot.eventbus.errordialog.message";
    public static final String KEY_TITLE = "de.greenrobot.eventbus.errordialog.title";
    protected static final String TAG_ERROR_DIALOG = "de.greenrobot.eventbus.error_dialog";
    protected static final String TAG_ERROR_DIALOG_MANAGER = "de.greenrobot.eventbus.error_dialog_manager";
    public static ErrorDialogFragmentFactory<?> factory;

    @TargetApi(11)
    public static class HoneycombManagerFragment extends Fragment {
        protected Bundle argumentsForErrorDialog;
        private EventBus eventBus;
        private Object executionScope;
        protected boolean finishAfterDialog;

        public void onResume() {
            super.onResume();
            this.eventBus = ErrorDialogManager.factory.config.getEventBus();
            this.eventBus.register(this);
        }

        public void onPause() {
            this.eventBus.unregister(this);
            super.onPause();
        }

        public void onEventMainThread(ThrowableFailureEvent throwableFailureEvent) {
            if (ErrorDialogManager.isInExecutionScope(this.executionScope, throwableFailureEvent)) {
                ErrorDialogManager.checkLogException(throwableFailureEvent);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.executePendingTransactions();
                DialogFragment dialogFragment = (DialogFragment) fragmentManager.findFragmentByTag(ErrorDialogManager.TAG_ERROR_DIALOG);
                if (dialogFragment != null) {
                    dialogFragment.dismiss();
                }
                DialogFragment dialogFragment2 = (DialogFragment) ErrorDialogManager.factory.prepareErrorFragment(throwableFailureEvent, this.finishAfterDialog, this.argumentsForErrorDialog);
                if (dialogFragment2 != null) {
                    dialogFragment2.show(fragmentManager, ErrorDialogManager.TAG_ERROR_DIALOG);
                }
            }
        }

        public static void attachTo(Activity activity, Object obj, boolean z, Bundle bundle) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            HoneycombManagerFragment honeycombManagerFragment = (HoneycombManagerFragment) fragmentManager.findFragmentByTag(ErrorDialogManager.TAG_ERROR_DIALOG_MANAGER);
            if (honeycombManagerFragment == null) {
                honeycombManagerFragment = new HoneycombManagerFragment();
                fragmentManager.beginTransaction().add(honeycombManagerFragment, ErrorDialogManager.TAG_ERROR_DIALOG_MANAGER).commit();
                fragmentManager.executePendingTransactions();
            }
            honeycombManagerFragment.finishAfterDialog = z;
            honeycombManagerFragment.argumentsForErrorDialog = bundle;
            honeycombManagerFragment.executionScope = obj;
        }
    }

    public static class SupportManagerFragment extends android.support.v4.app.Fragment {
        protected Bundle argumentsForErrorDialog;
        private EventBus eventBus;
        private Object executionScope;
        protected boolean finishAfterDialog;
        private boolean skipRegisterOnNextResume;

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.eventBus = ErrorDialogManager.factory.config.getEventBus();
            this.eventBus.register(this);
            this.skipRegisterOnNextResume = true;
        }

        public void onResume() {
            super.onResume();
            if (this.skipRegisterOnNextResume) {
                this.skipRegisterOnNextResume = false;
                return;
            }
            this.eventBus = ErrorDialogManager.factory.config.getEventBus();
            this.eventBus.register(this);
        }

        public void onPause() {
            this.eventBus.unregister(this);
            super.onPause();
        }

        public void onEventMainThread(ThrowableFailureEvent throwableFailureEvent) {
            if (ErrorDialogManager.isInExecutionScope(this.executionScope, throwableFailureEvent)) {
                ErrorDialogManager.checkLogException(throwableFailureEvent);
                android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.executePendingTransactions();
                android.support.v4.app.DialogFragment dialogFragment = (android.support.v4.app.DialogFragment) fragmentManager.findFragmentByTag(ErrorDialogManager.TAG_ERROR_DIALOG);
                if (dialogFragment != null) {
                    dialogFragment.dismiss();
                }
                android.support.v4.app.DialogFragment dialogFragment2 = (android.support.v4.app.DialogFragment) ErrorDialogManager.factory.prepareErrorFragment(throwableFailureEvent, this.finishAfterDialog, this.argumentsForErrorDialog);
                if (dialogFragment2 != null) {
                    dialogFragment2.show(fragmentManager, ErrorDialogManager.TAG_ERROR_DIALOG);
                }
            }
        }

        public static void attachTo(Activity activity, Object obj, boolean z, Bundle bundle) {
            android.support.v4.app.FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            SupportManagerFragment supportManagerFragment = (SupportManagerFragment) supportFragmentManager.findFragmentByTag(ErrorDialogManager.TAG_ERROR_DIALOG_MANAGER);
            if (supportManagerFragment == null) {
                supportManagerFragment = new SupportManagerFragment();
                supportFragmentManager.beginTransaction().add((android.support.v4.app.Fragment) supportManagerFragment, ErrorDialogManager.TAG_ERROR_DIALOG_MANAGER).commit();
                supportFragmentManager.executePendingTransactions();
            }
            supportManagerFragment.finishAfterDialog = z;
            supportManagerFragment.argumentsForErrorDialog = bundle;
            supportManagerFragment.executionScope = obj;
        }
    }

    public static void attachTo(Activity activity) {
        attachTo(activity, false, null);
    }

    public static void attachTo(Activity activity, boolean z) {
        attachTo(activity, z, null);
    }

    public static void attachTo(Activity activity, boolean z, Bundle bundle) {
        attachTo(activity, activity.getClass(), z, bundle);
    }

    public static void attachTo(Activity activity, Object obj, boolean z, Bundle bundle) {
        if (factory == null) {
            throw new RuntimeException("You must set the static factory field to configure error dialogs for your app.");
        } else if (isSupportActivity(activity)) {
            SupportManagerFragment.attachTo(activity, obj, z, bundle);
        } else {
            HoneycombManagerFragment.attachTo(activity, obj, z, bundle);
        }
    }

    private static boolean isSupportActivity(Activity activity) {
        Class cls = activity.getClass();
        String name;
        do {
            cls = cls.getSuperclass();
            if (cls != null) {
                name = cls.getName();
                if (name.equals("android.support.v4.app.FragmentActivity")) {
                    return true;
                }
                if (name.startsWith("com.actionbarsherlock.app") && (name.endsWith(".SherlockActivity") || name.endsWith(".SherlockListActivity") || name.endsWith(".SherlockPreferenceActivity"))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Please use SherlockFragmentActivity. Illegal activity: ");
                    stringBuilder.append(name);
                    throw new RuntimeException(stringBuilder.toString());
                }
            } else {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Illegal activity type: ");
                stringBuilder2.append(activity.getClass());
                throw new RuntimeException(stringBuilder2.toString());
            }
        } while (!name.equals("android.app.Activity"));
        if (VERSION.SDK_INT >= 11) {
            return false;
        }
        throw new RuntimeException("Illegal activity without fragment support. Either use Android 3.0+ or android.support.v4.app.FragmentActivity.");
    }

    protected static void checkLogException(ThrowableFailureEvent throwableFailureEvent) {
        if (factory.config.logExceptions) {
            String str = factory.config.tagForLoggingExceptions;
            if (str == null) {
                str = EventBus.TAG;
            }
            Log.i(str, "Error dialog manager received exception", throwableFailureEvent.throwable);
        }
    }

    private static boolean isInExecutionScope(Object obj, ThrowableFailureEvent throwableFailureEvent) {
        if (throwableFailureEvent != null) {
            Object executionScope = throwableFailureEvent.getExecutionScope();
            if (!(executionScope == null || executionScope.equals(obj))) {
                return false;
            }
        }
        return true;
    }
}
