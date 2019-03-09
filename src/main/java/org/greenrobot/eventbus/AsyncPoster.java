package org.greenrobot.eventbus;

class AsyncPoster implements Runnable, Poster {
    private final EventBus eventBus;
    private final PendingPostQueue queue = new PendingPostQueue();

    AsyncPoster(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void enqueue(Subscription subscription, Object obj) {
        this.queue.enqueue(PendingPost.obtainPendingPost(subscription, obj));
        this.eventBus.getExecutorService().execute(this);
    }

    public void run() {
        PendingPost poll = this.queue.poll();
        if (poll != null) {
            this.eventBus.invokeSubscriber(poll);
            return;
        }
        throw new IllegalStateException("No pending post available");
    }
}
