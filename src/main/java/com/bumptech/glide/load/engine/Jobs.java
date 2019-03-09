package com.bumptech.glide.load.engine;

import android.support.annotation.VisibleForTesting;
import com.bumptech.glide.load.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class Jobs {
    private final Map<Key, EngineJob<?>> jobs = new HashMap();
    private final Map<Key, EngineJob<?>> onlyCacheJobs = new HashMap();

    Jobs() {
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Map<Key, EngineJob<?>> getAll() {
        return Collections.unmodifiableMap(this.jobs);
    }

    /* Access modifiers changed, original: 0000 */
    public EngineJob<?> get(Key key, boolean z) {
        return (EngineJob) getJobMap(z).get(key);
    }

    /* Access modifiers changed, original: 0000 */
    public void put(Key key, EngineJob<?> engineJob) {
        getJobMap(engineJob.onlyRetrieveFromCache()).put(key, engineJob);
    }

    /* Access modifiers changed, original: 0000 */
    public void removeIfCurrent(Key key, EngineJob<?> engineJob) {
        Map jobMap = getJobMap(engineJob.onlyRetrieveFromCache());
        if (engineJob.equals(jobMap.get(key))) {
            jobMap.remove(key);
        }
    }

    private Map<Key, EngineJob<?>> getJobMap(boolean z) {
        return z ? this.onlyCacheJobs : this.jobs;
    }
}
