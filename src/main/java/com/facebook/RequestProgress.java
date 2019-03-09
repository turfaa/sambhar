package com.facebook;

import android.os.Handler;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphRequest.OnProgressCallback;

class RequestProgress {
    private final Handler callbackHandler;
    private long lastReportedProgress;
    private long maxProgress;
    private long progress;
    private final GraphRequest request;
    private final long threshold = FacebookSdk.getOnProgressThreshold();

    RequestProgress(Handler handler, GraphRequest graphRequest) {
        this.request = graphRequest;
        this.callbackHandler = handler;
    }

    /* Access modifiers changed, original: 0000 */
    public long getProgress() {
        return this.progress;
    }

    /* Access modifiers changed, original: 0000 */
    public long getMaxProgress() {
        return this.maxProgress;
    }

    /* Access modifiers changed, original: 0000 */
    public void addProgress(long j) {
        this.progress += j;
        if (this.progress >= this.lastReportedProgress + this.threshold || this.progress >= this.maxProgress) {
            reportProgress();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void addToMax(long j) {
        this.maxProgress += j;
    }

    /* Access modifiers changed, original: 0000 */
    public void reportProgress() {
        if (this.progress > this.lastReportedProgress) {
            Callback callback = this.request.getCallback();
            if (this.maxProgress > 0 && (callback instanceof OnProgressCallback)) {
                final long j = this.progress;
                final long j2 = this.maxProgress;
                final OnProgressCallback onProgressCallback = (OnProgressCallback) callback;
                if (this.callbackHandler == null) {
                    onProgressCallback.onProgress(j, j2);
                } else {
                    this.callbackHandler.post(new Runnable() {
                        public void run() {
                            onProgressCallback.onProgress(j, j2);
                        }
                    });
                }
                this.lastReportedProgress = this.progress;
            }
        }
    }
}
