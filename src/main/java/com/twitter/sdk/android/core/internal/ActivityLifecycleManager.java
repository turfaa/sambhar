package com.twitter.sdk.android.core.internal;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import java.util.HashSet;
import java.util.Set;

public class ActivityLifecycleManager {
    private final ActivityLifecycleCallbacksWrapper callbacksWrapper;

    private static class ActivityLifecycleCallbacksWrapper {
        private final Application application;
        private final Set<ActivityLifecycleCallbacks> registeredCallbacks = new HashSet();

        ActivityLifecycleCallbacksWrapper(Application application) {
            this.application = application;
        }

        private void clearCallbacks() {
            for (ActivityLifecycleCallbacks unregisterActivityLifecycleCallbacks : this.registeredCallbacks) {
                this.application.unregisterActivityLifecycleCallbacks(unregisterActivityLifecycleCallbacks);
            }
        }

        private boolean registerLifecycleCallbacks(final Callbacks callbacks) {
            if (this.application == null) {
                return false;
            }
            AnonymousClass1 anonymousClass1 = new ActivityLifecycleCallbacks() {
                public void onActivityCreated(Activity activity, Bundle bundle) {
                    callbacks.onActivityCreated(activity, bundle);
                }

                public void onActivityStarted(Activity activity) {
                    callbacks.onActivityStarted(activity);
                }

                public void onActivityResumed(Activity activity) {
                    callbacks.onActivityResumed(activity);
                }

                public void onActivityPaused(Activity activity) {
                    callbacks.onActivityPaused(activity);
                }

                public void onActivityStopped(Activity activity) {
                    callbacks.onActivityStopped(activity);
                }

                public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                    callbacks.onActivitySaveInstanceState(activity, bundle);
                }

                public void onActivityDestroyed(Activity activity) {
                    callbacks.onActivityDestroyed(activity);
                }
            };
            this.application.registerActivityLifecycleCallbacks(anonymousClass1);
            this.registeredCallbacks.add(anonymousClass1);
            return true;
        }
    }

    public static abstract class Callbacks {
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        public void onActivityDestroyed(Activity activity) {
        }

        public void onActivityPaused(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityStopped(Activity activity) {
        }
    }

    public ActivityLifecycleManager(Context context) {
        this.callbacksWrapper = new ActivityLifecycleCallbacksWrapper((Application) context.getApplicationContext());
    }

    public boolean registerCallbacks(Callbacks callbacks) {
        return this.callbacksWrapper != null && this.callbacksWrapper.registerLifecycleCallbacks(callbacks);
    }

    public void resetCallbacks() {
        if (this.callbacksWrapper != null) {
            this.callbacksWrapper.clearCallbacks();
        }
    }
}
