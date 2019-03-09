package com.bumptech.glide.request;

import android.support.annotation.Nullable;

public final class ErrorRequestCoordinator implements RequestCoordinator, Request {
    private Request error;
    @Nullable
    private final RequestCoordinator parent;
    private Request primary;

    public ErrorRequestCoordinator(@Nullable RequestCoordinator requestCoordinator) {
        this.parent = requestCoordinator;
    }

    public void setRequests(Request request, Request request2) {
        this.primary = request;
        this.error = request2;
    }

    public void begin() {
        if (!this.primary.isRunning()) {
            this.primary.begin();
        }
    }

    public void clear() {
        this.primary.clear();
        if (this.error.isRunning()) {
            this.error.clear();
        }
    }

    public boolean isRunning() {
        return (this.primary.isFailed() ? this.error : this.primary).isRunning();
    }

    public boolean isComplete() {
        return (this.primary.isFailed() ? this.error : this.primary).isComplete();
    }

    public boolean isResourceSet() {
        return (this.primary.isFailed() ? this.error : this.primary).isResourceSet();
    }

    public boolean isCleared() {
        return (this.primary.isFailed() ? this.error : this.primary).isCleared();
    }

    public boolean isFailed() {
        return this.primary.isFailed() && this.error.isFailed();
    }

    public void recycle() {
        this.primary.recycle();
        this.error.recycle();
    }

    public boolean isEquivalentTo(Request request) {
        boolean z = false;
        if (!(request instanceof ErrorRequestCoordinator)) {
            return false;
        }
        ErrorRequestCoordinator errorRequestCoordinator = (ErrorRequestCoordinator) request;
        if (this.primary.isEquivalentTo(errorRequestCoordinator.primary) && this.error.isEquivalentTo(errorRequestCoordinator.error)) {
            z = true;
        }
        return z;
    }

    public boolean canSetImage(Request request) {
        return parentCanSetImage() && isValidRequest(request);
    }

    private boolean parentCanSetImage() {
        return this.parent == null || this.parent.canSetImage(this);
    }

    public boolean canNotifyStatusChanged(Request request) {
        return parentCanNotifyStatusChanged() && isValidRequest(request);
    }

    public boolean canNotifyCleared(Request request) {
        return parentCanNotifyCleared() && isValidRequest(request);
    }

    private boolean parentCanNotifyCleared() {
        return this.parent == null || this.parent.canNotifyCleared(this);
    }

    private boolean parentCanNotifyStatusChanged() {
        return this.parent == null || this.parent.canNotifyStatusChanged(this);
    }

    private boolean isValidRequest(Request request) {
        return request.equals(this.primary) || (this.primary.isFailed() && request.equals(this.error));
    }

    public boolean isAnyResourceSet() {
        return parentIsAnyResourceSet() || isResourceSet();
    }

    private boolean parentIsAnyResourceSet() {
        return this.parent != null && this.parent.isAnyResourceSet();
    }

    public void onRequestSuccess(Request request) {
        if (this.parent != null) {
            this.parent.onRequestSuccess(this);
        }
    }

    public void onRequestFailed(Request request) {
        if (request.equals(this.error)) {
            if (this.parent != null) {
                this.parent.onRequestFailed(this);
            }
            return;
        }
        if (!this.error.isRunning()) {
            this.error.begin();
        }
    }
}
