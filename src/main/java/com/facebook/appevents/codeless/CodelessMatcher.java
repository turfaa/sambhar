package com.facebook.appevents.codeless;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.codeless.CodelessLoggingEventListener.AutoLoggingAccessibilityDelegate;
import com.facebook.appevents.codeless.RCTCodelessLoggingEventListener.AutoLoggingOnTouchListener;
import com.facebook.appevents.codeless.internal.Constants;
import com.facebook.appevents.codeless.internal.EventBinding;
import com.facebook.appevents.codeless.internal.ParameterComponent;
import com.facebook.appevents.codeless.internal.PathComponent;
import com.facebook.appevents.codeless.internal.PathComponent.MatchBitmaskType;
import com.facebook.appevents.codeless.internal.ViewHierarchy;
import com.facebook.internal.FetchedAppSettings;
import com.facebook.internal.FetchedAppSettingsManager;
import com.facebook.internal.InternalSettings;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodelessMatcher {
    private static final String CURRENT_CLASS_NAME = ".";
    private static final String PARENT_CLASS_NAME = "..";
    private static final String TAG = CodelessMatcher.class.getCanonicalName();
    private Set<Activity> activitiesSet = new HashSet();
    private HashMap<String, String> delegateMap = new HashMap();
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private Set<ViewMatcher> viewMatchers = new HashSet();

    public static class MatchedView {
        private WeakReference<View> view;
        private String viewMapKey;

        public MatchedView(View view, String str) {
            this.view = new WeakReference(view);
            this.viewMapKey = str;
        }

        @Nullable
        public View getView() {
            return this.view == null ? null : (View) this.view.get();
        }

        public String getViewMapKey() {
            return this.viewMapKey;
        }
    }

    protected static class ViewMatcher implements OnGlobalLayoutListener, OnScrollChangedListener, Runnable {
        private final String activityName;
        private HashMap<String, String> delegateMap;
        @Nullable
        private List<EventBinding> eventBindings;
        private final Handler handler;
        private WeakReference<View> rootView;

        public ViewMatcher(View view, Handler handler, HashMap<String, String> hashMap, String str) {
            this.rootView = new WeakReference(view);
            this.handler = handler;
            this.delegateMap = hashMap;
            this.activityName = str;
            this.handler.postDelayed(this, 200);
        }

        public void run() {
            FetchedAppSettings appSettingsWithoutQuery = FetchedAppSettingsManager.getAppSettingsWithoutQuery(FacebookSdk.getApplicationId());
            if (appSettingsWithoutQuery != null && appSettingsWithoutQuery.getCodelessEventsEnabled()) {
                this.eventBindings = EventBinding.parseArray(appSettingsWithoutQuery.getEventBindings());
                if (this.eventBindings != null) {
                    View view = (View) this.rootView.get();
                    if (view != null) {
                        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.addOnGlobalLayoutListener(this);
                            viewTreeObserver.addOnScrollChangedListener(this);
                        }
                        startMatch();
                    }
                }
            }
        }

        public void onGlobalLayout() {
            startMatch();
        }

        public void onScrollChanged() {
            startMatch();
        }

        private void startMatch() {
            if (this.eventBindings != null && this.rootView.get() != null) {
                for (int i = 0; i < this.eventBindings.size(); i++) {
                    findView((EventBinding) this.eventBindings.get(i), (View) this.rootView.get());
                }
            }
        }

        public void findView(EventBinding eventBinding, View view) {
            if (eventBinding != null && view != null) {
                if (TextUtils.isEmpty(eventBinding.getActivityName()) || eventBinding.getActivityName().equals(this.activityName)) {
                    List viewPath = eventBinding.getViewPath();
                    if (viewPath.size() <= 25) {
                        for (MatchedView attachListener : findViewByPath(eventBinding, view, viewPath, 0, -1, this.activityName)) {
                            attachListener(attachListener, view, eventBinding);
                        }
                    }
                }
            }
        }

        public static List<MatchedView> findViewByPath(EventBinding eventBinding, View view, List<PathComponent> list, int i, int i2, String str) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append(CodelessMatcher.CURRENT_CLASS_NAME);
            stringBuilder.append(String.valueOf(i2));
            str = stringBuilder.toString();
            ArrayList arrayList = new ArrayList();
            if (view == null) {
                return arrayList;
            }
            List findVisibleChildren;
            int i3;
            if (i >= list.size()) {
                arrayList.add(new MatchedView(view, str));
            } else {
                PathComponent pathComponent = (PathComponent) list.get(i);
                if (pathComponent.className.equals(CodelessMatcher.PARENT_CLASS_NAME)) {
                    ViewParent parent = view.getParent();
                    if (parent instanceof ViewGroup) {
                        findVisibleChildren = findVisibleChildren((ViewGroup) parent);
                        i2 = findVisibleChildren.size();
                        for (i3 = 0; i3 < i2; i3++) {
                            arrayList.addAll(findViewByPath(eventBinding, (View) findVisibleChildren.get(i3), list, i + 1, i3, str));
                        }
                    }
                    return arrayList;
                } else if (pathComponent.className.equals(CodelessMatcher.CURRENT_CLASS_NAME)) {
                    arrayList.add(new MatchedView(view, str));
                    return arrayList;
                } else if (!isTheSameView(view, pathComponent, i2)) {
                    return arrayList;
                } else {
                    if (i == list.size() - 1) {
                        arrayList.add(new MatchedView(view, str));
                    }
                }
            }
            if (view instanceof ViewGroup) {
                findVisibleChildren = findVisibleChildren((ViewGroup) view);
                i2 = findVisibleChildren.size();
                for (i3 = 0; i3 < i2; i3++) {
                    arrayList.addAll(findViewByPath(eventBinding, (View) findVisibleChildren.get(i3), list, i + 1, i3, str));
                }
            }
            return arrayList;
        }

        private static boolean isTheSameView(View view, PathComponent pathComponent, int i) {
            if (pathComponent.index != -1 && i != pathComponent.index) {
                return false;
            }
            if (!view.getClass().getCanonicalName().equals(pathComponent.className)) {
                if (!pathComponent.className.matches(".*android\\..*")) {
                    return false;
                }
                String[] split = pathComponent.className.split("\\.");
                if (split.length <= 0) {
                    return false;
                }
                if (!view.getClass().getSimpleName().equals(split[split.length - 1])) {
                    return false;
                }
            }
            if ((pathComponent.matchBitmask & MatchBitmaskType.ID.getValue()) > 0 && pathComponent.id != view.getId()) {
                return false;
            }
            if ((pathComponent.matchBitmask & MatchBitmaskType.TEXT.getValue()) > 0 && !pathComponent.text.equals(ViewHierarchy.getTextOfView(view))) {
                return false;
            }
            if ((pathComponent.matchBitmask & MatchBitmaskType.DESCRIPTION.getValue()) > 0) {
                Object obj;
                String str = pathComponent.description;
                if (view.getContentDescription() == null) {
                    obj = "";
                } else {
                    obj = String.valueOf(view.getContentDescription());
                }
                if (!str.equals(obj)) {
                    return false;
                }
            }
            if ((pathComponent.matchBitmask & MatchBitmaskType.HINT.getValue()) > 0 && !pathComponent.hint.equals(ViewHierarchy.getHintOfView(view))) {
                return false;
            }
            if ((pathComponent.matchBitmask & MatchBitmaskType.TAG.getValue()) > 0) {
                Object obj2;
                String str2 = pathComponent.tag;
                if (view.getTag() == null) {
                    obj2 = "";
                } else {
                    obj2 = String.valueOf(view.getTag());
                }
                if (!str2.equals(obj2)) {
                    return false;
                }
            }
            return true;
        }

        private static List<View> findVisibleChildren(ViewGroup viewGroup) {
            ArrayList arrayList = new ArrayList();
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    arrayList.add(childAt);
                }
            }
            return arrayList;
        }

        private void attachListener(MatchedView matchedView, View view, EventBinding eventBinding) {
            if (eventBinding != null) {
                try {
                    View view2 = matchedView.getView();
                    if (view2 != null) {
                        View findRCTRootView = ViewHierarchy.findRCTRootView(view2);
                        if (findRCTRootView != null && ViewHierarchy.isRCTButton(view2, findRCTRootView)) {
                            attachRCTListener(matchedView, view, findRCTRootView, eventBinding);
                        } else if (!view2.getClass().getName().startsWith("com.facebook.react")) {
                            String viewMapKey = matchedView.getViewMapKey();
                            AccessibilityDelegate existingDelegate = ViewHierarchy.getExistingDelegate(view2);
                            Object obj = null;
                            Object obj2 = existingDelegate != null ? 1 : null;
                            Object obj3 = (obj2 == null || !(existingDelegate instanceof AutoLoggingAccessibilityDelegate)) ? null : 1;
                            if (obj3 != null && ((AutoLoggingAccessibilityDelegate) existingDelegate).getSupportCodelessLogging()) {
                                obj = 1;
                            }
                            if (!this.delegateMap.containsKey(viewMapKey) && (obj2 == null || obj3 == null || obj == null)) {
                                view2.setAccessibilityDelegate(CodelessLoggingEventListener.getAccessibilityDelegate(eventBinding, view, view2));
                                this.delegateMap.put(viewMapKey, eventBinding.getEventName());
                            }
                        }
                    }
                } catch (FacebookException e) {
                    Log.e(CodelessMatcher.TAG, "Failed to attach auto logging event listener.", e);
                }
            }
        }

        private void attachRCTListener(MatchedView matchedView, View view, View view2, EventBinding eventBinding) {
            if (eventBinding != null) {
                View view3 = matchedView.getView();
                if (view3 != null && ViewHierarchy.isRCTButton(view3, view2)) {
                    String viewMapKey = matchedView.getViewMapKey();
                    OnTouchListener existingOnTouchListener = ViewHierarchy.getExistingOnTouchListener(view3);
                    Object obj = null;
                    Object obj2 = existingOnTouchListener != null ? 1 : null;
                    Object obj3 = (obj2 == null || !(existingOnTouchListener instanceof AutoLoggingOnTouchListener)) ? null : 1;
                    if (obj3 != null && ((AutoLoggingOnTouchListener) existingOnTouchListener).getSupportCodelessLogging()) {
                        obj = 1;
                    }
                    if (!this.delegateMap.containsKey(viewMapKey) && (obj2 == null || obj3 == null || obj == null)) {
                        view3.setOnTouchListener(RCTCodelessLoggingEventListener.getOnTouchListener(eventBinding, view, view3));
                        this.delegateMap.put(viewMapKey, eventBinding.getEventName());
                    }
                }
            }
        }
    }

    public void add(Activity activity) {
        if (!InternalSettings.isUnityApp()) {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                this.activitiesSet.add(activity);
                this.delegateMap.clear();
                startTracking();
                return;
            }
            throw new FacebookException("Can't add activity to CodelessMatcher on non-UI thread");
        }
    }

    public void remove(Activity activity) {
        if (!InternalSettings.isUnityApp()) {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                this.activitiesSet.remove(activity);
                this.viewMatchers.clear();
                this.delegateMap.clear();
                return;
            }
            throw new FacebookException("Can't remove activity from CodelessMatcher on non-UI thread");
        }
    }

    public static Bundle getParameters(EventBinding eventBinding, View view, View view2) {
        Bundle bundle = new Bundle();
        if (eventBinding == null) {
            return bundle;
        }
        List<ParameterComponent> viewParameters = eventBinding.getViewParameters();
        if (viewParameters != null) {
            for (ParameterComponent parameterComponent : viewParameters) {
                if (parameterComponent.value != null && parameterComponent.value.length() > 0) {
                    bundle.putString(parameterComponent.name, parameterComponent.value);
                } else if (parameterComponent.path.size() > 0) {
                    List findViewByPath;
                    if (parameterComponent.pathType.equals(Constants.PATH_TYPE_RELATIVE)) {
                        findViewByPath = ViewMatcher.findViewByPath(eventBinding, view2, parameterComponent.path, 0, -1, view2.getClass().getSimpleName());
                    } else {
                        findViewByPath = ViewMatcher.findViewByPath(eventBinding, view, parameterComponent.path, 0, -1, view.getClass().getSimpleName());
                    }
                    for (MatchedView matchedView : findViewByPath) {
                        if (matchedView.getView() != null) {
                            String textOfView = ViewHierarchy.getTextOfView(matchedView.getView());
                            if (textOfView.length() > 0) {
                                bundle.putString(parameterComponent.name, textOfView);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return bundle;
    }

    private void startTracking() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            matchViews();
        } else {
            this.uiThreadHandler.post(new Runnable() {
                public void run() {
                    CodelessMatcher.this.matchViews();
                }
            });
        }
    }

    private void matchViews() {
        for (Activity activity : this.activitiesSet) {
            this.viewMatchers.add(new ViewMatcher(activity.getWindow().getDecorView().getRootView(), this.uiThreadHandler, this.delegateMap, activity.getClass().getSimpleName()));
        }
    }
}
