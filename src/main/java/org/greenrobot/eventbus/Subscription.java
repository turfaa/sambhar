package org.greenrobot.eventbus;

final class Subscription {
    volatile boolean active = true;
    final Object subscriber;
    final SubscriberMethod subscriberMethod;

    Subscription(Object obj, SubscriberMethod subscriberMethod) {
        this.subscriber = obj;
        this.subscriberMethod = subscriberMethod;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Subscription)) {
            return false;
        }
        Subscription subscription = (Subscription) obj;
        if (this.subscriber == subscription.subscriber && this.subscriberMethod.equals(subscription.subscriberMethod)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.subscriber.hashCode() + this.subscriberMethod.methodString.hashCode();
    }
}
