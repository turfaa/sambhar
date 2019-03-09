package com.bumptech.glide.manager;

import android.support.annotation.NonNull;
import com.bumptech.glide.util.Util;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

class ActivityFragmentLifecycle implements Lifecycle {
    private boolean isDestroyed;
    private boolean isStarted;
    private final Set<LifecycleListener> lifecycleListeners = Collections.newSetFromMap(new WeakHashMap());

    ActivityFragmentLifecycle() {
    }

    public void addListener(@NonNull LifecycleListener lifecycleListener) {
        this.lifecycleListeners.add(lifecycleListener);
        if (this.isDestroyed) {
            lifecycleListener.onDestroy();
        } else if (this.isStarted) {
            lifecycleListener.onStart();
        } else {
            lifecycleListener.onStop();
        }
    }

    public void removeListener(@NonNull LifecycleListener lifecycleListener) {
        this.lifecycleListeners.remove(lifecycleListener);
    }

    /* Access modifiers changed, original: 0000 */
    public void onStart() {
        this.isStarted = true;
        for (LifecycleListener onStart : Util.getSnapshot(this.lifecycleListeners)) {
            onStart.onStart();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onStop() {
        this.isStarted = false;
        for (LifecycleListener onStop : Util.getSnapshot(this.lifecycleListeners)) {
            onStop.onStop();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onDestroy() {
        this.isDestroyed = true;
        for (LifecycleListener onDestroy : Util.getSnapshot(this.lifecycleListeners)) {
            onDestroy.onDestroy();
        }
    }
}
