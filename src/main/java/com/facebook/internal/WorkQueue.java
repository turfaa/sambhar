package com.facebook.internal;

import com.facebook.FacebookSdk;
import java.util.concurrent.Executor;

public class WorkQueue {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int DEFAULT_MAX_CONCURRENT = 8;
    private final Executor executor;
    private final int maxConcurrent;
    private WorkNode pendingJobs;
    private int runningCount;
    private WorkNode runningJobs;
    private final Object workLock;

    public interface WorkItem {
        boolean cancel();

        boolean isRunning();

        void moveToFront();
    }

    private class WorkNode implements WorkItem {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final Runnable callback;
        private boolean isRunning;
        private WorkNode next;
        private WorkNode prev;

        /* Access modifiers changed, original: 0000 */
        public void verify(boolean z) {
        }

        static {
            Class cls = WorkQueue.class;
        }

        WorkNode(Runnable runnable) {
            this.callback = runnable;
        }

        public boolean cancel() {
            synchronized (WorkQueue.this.workLock) {
                if (isRunning()) {
                    return false;
                }
                WorkQueue.this.pendingJobs = removeFromList(WorkQueue.this.pendingJobs);
                return true;
            }
        }

        public void moveToFront() {
            synchronized (WorkQueue.this.workLock) {
                if (!isRunning()) {
                    WorkQueue.this.pendingJobs = removeFromList(WorkQueue.this.pendingJobs);
                    WorkQueue.this.pendingJobs = addToList(WorkQueue.this.pendingJobs, true);
                }
            }
        }

        public boolean isRunning() {
            return this.isRunning;
        }

        /* Access modifiers changed, original: 0000 */
        public Runnable getCallback() {
            return this.callback;
        }

        /* Access modifiers changed, original: 0000 */
        public WorkNode getNext() {
            return this.next;
        }

        /* Access modifiers changed, original: 0000 */
        public void setIsRunning(boolean z) {
            this.isRunning = z;
        }

        /* Access modifiers changed, original: 0000 */
        public WorkNode addToList(WorkNode this_, boolean z) {
            if (this_ == null) {
                this.prev = this;
                this.next = this;
                this_ = this;
            } else {
                this.next = this_;
                this.prev = this_.prev;
                WorkNode workNode = this.next;
                this.prev.next = this;
                workNode.prev = this;
            }
            return z ? this : this_;
        }

        /* Access modifiers changed, original: 0000 */
        public WorkNode removeFromList(WorkNode workNode) {
            if (workNode == this) {
                if (this.next == this) {
                    workNode = null;
                } else {
                    workNode = this.next;
                }
            }
            this.next.prev = this.prev;
            this.prev.next = this.next;
            this.prev = null;
            this.next = null;
            return workNode;
        }
    }

    public WorkQueue() {
        this(8);
    }

    public WorkQueue(int i) {
        this(i, FacebookSdk.getExecutor());
    }

    public WorkQueue(int i, Executor executor) {
        this.workLock = new Object();
        this.runningJobs = null;
        this.runningCount = 0;
        this.maxConcurrent = i;
        this.executor = executor;
    }

    public WorkItem addActiveWorkItem(Runnable runnable) {
        return addActiveWorkItem(runnable, true);
    }

    public WorkItem addActiveWorkItem(Runnable runnable, boolean z) {
        WorkNode workNode = new WorkNode(runnable);
        synchronized (this.workLock) {
            this.pendingJobs = workNode.addToList(this.pendingJobs, z);
        }
        startItem();
        return workNode;
    }

    public void validate() {
        synchronized (this.workLock) {
            if (this.runningJobs != null) {
                WorkNode workNode = this.runningJobs;
                do {
                    workNode.verify(true);
                    workNode = workNode.getNext();
                } while (workNode != this.runningJobs);
            }
        }
    }

    private void startItem() {
        finishItemAndStartNew(null);
    }

    private void finishItemAndStartNew(WorkNode workNode) {
        synchronized (this.workLock) {
            if (workNode != null) {
                try {
                    this.runningJobs = workNode.removeFromList(this.runningJobs);
                    this.runningCount--;
                } catch (Throwable th) {
                    while (true) {
                    }
                }
            }
            if (this.runningCount < this.maxConcurrent) {
                workNode = this.pendingJobs;
                if (workNode != null) {
                    this.pendingJobs = workNode.removeFromList(this.pendingJobs);
                    this.runningJobs = workNode.addToList(this.runningJobs, false);
                    this.runningCount++;
                    workNode.setIsRunning(true);
                }
            } else {
                workNode = null;
            }
        }
        if (workNode != null) {
            execute(workNode);
        }
    }

    private void execute(final WorkNode workNode) {
        this.executor.execute(new Runnable() {
            public void run() {
                try {
                    workNode.getCallback().run();
                } finally {
                    WorkQueue.this.finishItemAndStartNew(workNode);
                }
            }
        });
    }
}
