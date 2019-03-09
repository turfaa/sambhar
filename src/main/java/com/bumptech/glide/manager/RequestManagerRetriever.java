package com.bumptech.glide.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RequestManagerRetriever implements Callback {
    private static final RequestManagerFactory DEFAULT_FACTORY = new RequestManagerFactory() {
        @NonNull
        public RequestManager build(@NonNull Glide glide, @NonNull Lifecycle lifecycle, @NonNull RequestManagerTreeNode requestManagerTreeNode, @NonNull Context context) {
            return new RequestManager(glide, lifecycle, requestManagerTreeNode, context);
        }
    };
    private static final String FRAGMENT_INDEX_KEY = "key";
    @VisibleForTesting
    static final String FRAGMENT_TAG = "com.bumptech.glide.manager";
    private static final int ID_REMOVE_FRAGMENT_MANAGER = 1;
    private static final int ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2;
    private static final String TAG = "RMRetriever";
    private volatile RequestManager applicationManager;
    private final RequestManagerFactory factory;
    private final Handler handler;
    @VisibleForTesting
    final Map<FragmentManager, RequestManagerFragment> pendingRequestManagerFragments = new HashMap();
    @VisibleForTesting
    final Map<android.support.v4.app.FragmentManager, SupportRequestManagerFragment> pendingSupportRequestManagerFragments = new HashMap();
    private final Bundle tempBundle = new Bundle();
    private final ArrayMap<View, Fragment> tempViewToFragment = new ArrayMap();
    private final ArrayMap<View, android.support.v4.app.Fragment> tempViewToSupportFragment = new ArrayMap();

    public interface RequestManagerFactory {
        @NonNull
        RequestManager build(@NonNull Glide glide, @NonNull Lifecycle lifecycle, @NonNull RequestManagerTreeNode requestManagerTreeNode, @NonNull Context context);
    }

    public RequestManagerRetriever(@Nullable RequestManagerFactory requestManagerFactory) {
        if (requestManagerFactory == null) {
            requestManagerFactory = DEFAULT_FACTORY;
        }
        this.factory = requestManagerFactory;
        this.handler = new Handler(Looper.getMainLooper(), this);
    }

    @NonNull
    private RequestManager getApplicationManager(@NonNull Context context) {
        if (this.applicationManager == null) {
            synchronized (this) {
                if (this.applicationManager == null) {
                    this.applicationManager = this.factory.build(Glide.get(context.getApplicationContext()), new ApplicationLifecycle(), new EmptyRequestManagerTreeNode(), context.getApplicationContext());
                }
            }
        }
        return this.applicationManager;
    }

    @NonNull
    public RequestManager get(@NonNull Context context) {
        if (context != null) {
            if (Util.isOnMainThread() && !(context instanceof Application)) {
                if (context instanceof FragmentActivity) {
                    return get((FragmentActivity) context);
                }
                if (context instanceof Activity) {
                    return get((Activity) context);
                }
                if (context instanceof ContextWrapper) {
                    return get(((ContextWrapper) context).getBaseContext());
                }
            }
            return getApplicationManager(context);
        }
        throw new IllegalArgumentException("You cannot start a load on a null Context");
    }

    @NonNull
    public RequestManager get(@NonNull FragmentActivity fragmentActivity) {
        if (Util.isOnBackgroundThread()) {
            return get(fragmentActivity.getApplicationContext());
        }
        assertNotDestroyed(fragmentActivity);
        return supportFragmentGet(fragmentActivity, fragmentActivity.getSupportFragmentManager(), null, isActivityVisible(fragmentActivity));
    }

    @NonNull
    public RequestManager get(@NonNull android.support.v4.app.Fragment fragment) {
        Preconditions.checkNotNull(fragment.getActivity(), "You cannot start a load on a fragment before it is attached or after it is destroyed");
        if (Util.isOnBackgroundThread()) {
            return get(fragment.getActivity().getApplicationContext());
        }
        return supportFragmentGet(fragment.getActivity(), fragment.getChildFragmentManager(), fragment, fragment.isVisible());
    }

    @NonNull
    public RequestManager get(@NonNull Activity activity) {
        if (Util.isOnBackgroundThread()) {
            return get(activity.getApplicationContext());
        }
        assertNotDestroyed(activity);
        return fragmentGet(activity, activity.getFragmentManager(), null, isActivityVisible(activity));
    }

    @NonNull
    public RequestManager get(@NonNull View view) {
        if (Util.isOnBackgroundThread()) {
            return get(view.getContext().getApplicationContext());
        }
        Preconditions.checkNotNull(view);
        Preconditions.checkNotNull(view.getContext(), "Unable to obtain a request manager for a view without a Context");
        Activity findActivity = findActivity(view.getContext());
        if (findActivity == null) {
            return get(view.getContext().getApplicationContext());
        }
        if (findActivity instanceof FragmentActivity) {
            android.support.v4.app.Fragment findSupportFragment = findSupportFragment(view, (FragmentActivity) findActivity);
            return findSupportFragment != null ? get(findSupportFragment) : get(findActivity);
        }
        Fragment findFragment = findFragment(view, findActivity);
        if (findFragment == null) {
            return get(findActivity);
        }
        return get(findFragment);
    }

    private static void findAllSupportFragmentsWithViews(@Nullable Collection<android.support.v4.app.Fragment> collection, @NonNull Map<View, android.support.v4.app.Fragment> map) {
        if (collection != null) {
            for (android.support.v4.app.Fragment fragment : collection) {
                if (fragment != null) {
                    if (fragment.getView() != null) {
                        map.put(fragment.getView(), fragment);
                        findAllSupportFragmentsWithViews(fragment.getChildFragmentManager().getFragments(), map);
                    }
                }
            }
        }
    }

    @Nullable
    private android.support.v4.app.Fragment findSupportFragment(@NonNull View view, @NonNull FragmentActivity fragmentActivity) {
        this.tempViewToSupportFragment.clear();
        findAllSupportFragmentsWithViews(fragmentActivity.getSupportFragmentManager().getFragments(), this.tempViewToSupportFragment);
        View findViewById = fragmentActivity.findViewById(16908290);
        android.support.v4.app.Fragment fragment = null;
        while (!view.equals(findViewById)) {
            fragment = (android.support.v4.app.Fragment) this.tempViewToSupportFragment.get(view);
            if (fragment != null || !(view.getParent() instanceof View)) {
                break;
            }
            view = (View) view.getParent();
        }
        this.tempViewToSupportFragment.clear();
        return fragment;
    }

    @Nullable
    @Deprecated
    private Fragment findFragment(@NonNull View view, @NonNull Activity activity) {
        this.tempViewToFragment.clear();
        findAllFragmentsWithViews(activity.getFragmentManager(), this.tempViewToFragment);
        View findViewById = activity.findViewById(16908290);
        Fragment fragment = null;
        while (!view.equals(findViewById)) {
            fragment = (Fragment) this.tempViewToFragment.get(view);
            if (fragment != null || !(view.getParent() instanceof View)) {
                break;
            }
            view = (View) view.getParent();
        }
        this.tempViewToFragment.clear();
        return fragment;
    }

    @TargetApi(26)
    @Deprecated
    private void findAllFragmentsWithViews(@NonNull FragmentManager fragmentManager, @NonNull ArrayMap<View, Fragment> arrayMap) {
        if (VERSION.SDK_INT >= 26) {
            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment.getView() != null) {
                    arrayMap.put(fragment.getView(), fragment);
                    findAllFragmentsWithViews(fragment.getChildFragmentManager(), arrayMap);
                }
            }
            return;
        }
        findAllFragmentsWithViewsPreO(fragmentManager, arrayMap);
    }

    @Deprecated
    private void findAllFragmentsWithViewsPreO(@NonNull FragmentManager fragmentManager, @NonNull ArrayMap<View, Fragment> arrayMap) {
        int i = 0;
        while (true) {
            int i2 = i + 1;
            this.tempBundle.putInt(FRAGMENT_INDEX_KEY, i);
            Fragment fragment = null;
            try {
                fragment = fragmentManager.getFragment(this.tempBundle, FRAGMENT_INDEX_KEY);
            } catch (Exception unused) {
            }
            if (fragment != null) {
                if (fragment.getView() != null) {
                    arrayMap.put(fragment.getView(), fragment);
                    if (VERSION.SDK_INT >= 17) {
                        findAllFragmentsWithViews(fragment.getChildFragmentManager(), arrayMap);
                    }
                }
                i = i2;
            } else {
                return;
            }
        }
    }

    @Nullable
    private Activity findActivity(@NonNull Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return context instanceof ContextWrapper ? findActivity(((ContextWrapper) context).getBaseContext()) : null;
    }

    @TargetApi(17)
    private static void assertNotDestroyed(@NonNull Activity activity) {
        if (VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
            throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
        }
    }

    @TargetApi(17)
    @Deprecated
    @NonNull
    public RequestManager get(@NonNull Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("You cannot start a load on a fragment before it is attached");
        } else if (Util.isOnBackgroundThread() || VERSION.SDK_INT < 17) {
            return get(fragment.getActivity().getApplicationContext());
        } else {
            return fragmentGet(fragment.getActivity(), fragment.getChildFragmentManager(), fragment, fragment.isVisible());
        }
    }

    /* Access modifiers changed, original: 0000 */
    @Deprecated
    @NonNull
    public RequestManagerFragment getRequestManagerFragment(Activity activity) {
        return getRequestManagerFragment(activity.getFragmentManager(), null, isActivityVisible(activity));
    }

    @NonNull
    private RequestManagerFragment getRequestManagerFragment(@NonNull FragmentManager fragmentManager, @Nullable Fragment fragment, boolean z) {
        RequestManagerFragment requestManagerFragment = (RequestManagerFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (requestManagerFragment == null) {
            requestManagerFragment = (RequestManagerFragment) this.pendingRequestManagerFragments.get(fragmentManager);
            if (requestManagerFragment == null) {
                requestManagerFragment = new RequestManagerFragment();
                requestManagerFragment.setParentFragmentHint(fragment);
                if (z) {
                    requestManagerFragment.getGlideLifecycle().onStart();
                }
                this.pendingRequestManagerFragments.put(fragmentManager, requestManagerFragment);
                fragmentManager.beginTransaction().add(requestManagerFragment, FRAGMENT_TAG).commitAllowingStateLoss();
                this.handler.obtainMessage(1, fragmentManager).sendToTarget();
            }
        }
        return requestManagerFragment;
    }

    @Deprecated
    @NonNull
    private RequestManager fragmentGet(@NonNull Context context, @NonNull FragmentManager fragmentManager, @Nullable Fragment fragment, boolean z) {
        RequestManagerFragment requestManagerFragment = getRequestManagerFragment(fragmentManager, fragment, z);
        RequestManager requestManager = requestManagerFragment.getRequestManager();
        if (requestManager != null) {
            return requestManager;
        }
        requestManager = this.factory.build(Glide.get(context), requestManagerFragment.getGlideLifecycle(), requestManagerFragment.getRequestManagerTreeNode(), context);
        requestManagerFragment.setRequestManager(requestManager);
        return requestManager;
    }

    /* Access modifiers changed, original: 0000 */
    @NonNull
    public SupportRequestManagerFragment getSupportRequestManagerFragment(FragmentActivity fragmentActivity) {
        return getSupportRequestManagerFragment(fragmentActivity.getSupportFragmentManager(), null, isActivityVisible(fragmentActivity));
    }

    private static boolean isActivityVisible(Activity activity) {
        return activity.isFinishing() ^ 1;
    }

    @NonNull
    private SupportRequestManagerFragment getSupportRequestManagerFragment(@NonNull android.support.v4.app.FragmentManager fragmentManager, @Nullable android.support.v4.app.Fragment fragment, boolean z) {
        SupportRequestManagerFragment supportRequestManagerFragment = (SupportRequestManagerFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (supportRequestManagerFragment == null) {
            supportRequestManagerFragment = (SupportRequestManagerFragment) this.pendingSupportRequestManagerFragments.get(fragmentManager);
            if (supportRequestManagerFragment == null) {
                supportRequestManagerFragment = new SupportRequestManagerFragment();
                supportRequestManagerFragment.setParentFragmentHint(fragment);
                if (z) {
                    supportRequestManagerFragment.getGlideLifecycle().onStart();
                }
                this.pendingSupportRequestManagerFragments.put(fragmentManager, supportRequestManagerFragment);
                fragmentManager.beginTransaction().add((android.support.v4.app.Fragment) supportRequestManagerFragment, FRAGMENT_TAG).commitAllowingStateLoss();
                this.handler.obtainMessage(2, fragmentManager).sendToTarget();
            }
        }
        return supportRequestManagerFragment;
    }

    @NonNull
    private RequestManager supportFragmentGet(@NonNull Context context, @NonNull android.support.v4.app.FragmentManager fragmentManager, @Nullable android.support.v4.app.Fragment fragment, boolean z) {
        SupportRequestManagerFragment supportRequestManagerFragment = getSupportRequestManagerFragment(fragmentManager, fragment, z);
        RequestManager requestManager = supportRequestManagerFragment.getRequestManager();
        if (requestManager != null) {
            return requestManager;
        }
        requestManager = this.factory.build(Glide.get(context), supportRequestManagerFragment.getGlideLifecycle(), supportRequestManagerFragment.getRequestManagerTreeNode(), context);
        supportRequestManagerFragment.setRequestManager(requestManager);
        return requestManager;
    }

    public boolean handleMessage(Message message) {
        Object obj = null;
        boolean z = true;
        Object remove;
        switch (message.what) {
            case 1:
                obj = (FragmentManager) message.obj;
                remove = this.pendingRequestManagerFragments.remove(obj);
                break;
            case 2:
                obj = (android.support.v4.app.FragmentManager) message.obj;
                remove = this.pendingSupportRequestManagerFragments.remove(obj);
                break;
            default:
                z = false;
                remove = null;
                break;
        }
        if (z && remove == null && Log.isLoggable(TAG, 5)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to remove expected request manager fragment, manager: ");
            stringBuilder.append(obj);
            Log.w(str, stringBuilder.toString());
        }
        return z;
    }
}
