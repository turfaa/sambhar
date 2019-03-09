package org.greenrobot.eventbus;

final class PendingPostQueue {
    private PendingPost head;
    private PendingPost tail;

    PendingPostQueue() {
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized void enqueue(PendingPost pendingPost) {
        if (pendingPost != null) {
            if (this.tail != null) {
                this.tail.next = pendingPost;
                this.tail = pendingPost;
            } else if (this.head == null) {
                this.tail = pendingPost;
                this.head = pendingPost;
            } else {
                throw new IllegalStateException("Head present, but no tail");
            }
            notifyAll();
        } else {
            throw new NullPointerException("null cannot be enqueued");
        }
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized PendingPost poll() {
        PendingPost pendingPost;
        pendingPost = this.head;
        if (this.head != null) {
            this.head = this.head.next;
            if (this.head == null) {
                this.tail = null;
            }
        }
        return pendingPost;
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized PendingPost poll(int i) throws InterruptedException {
        if (this.head == null) {
            wait((long) i);
        }
        return poll();
    }
}
