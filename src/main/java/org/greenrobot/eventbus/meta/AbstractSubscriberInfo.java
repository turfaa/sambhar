package org.greenrobot.eventbus.meta;

import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.SubscriberMethod;
import org.greenrobot.eventbus.ThreadMode;

public abstract class AbstractSubscriberInfo implements SubscriberInfo {
    private final boolean shouldCheckSuperclass;
    private final Class subscriberClass;
    private final Class<? extends SubscriberInfo> superSubscriberInfoClass;

    protected AbstractSubscriberInfo(Class cls, Class<? extends SubscriberInfo> cls2, boolean z) {
        this.subscriberClass = cls;
        this.superSubscriberInfoClass = cls2;
        this.shouldCheckSuperclass = z;
    }

    public Class getSubscriberClass() {
        return this.subscriberClass;
    }

    public SubscriberInfo getSuperSubscriberInfo() {
        if (this.superSubscriberInfoClass == null) {
            return null;
        }
        try {
            return (SubscriberInfo) this.superSubscriberInfoClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e2) {
            throw new RuntimeException(e2);
        }
    }

    public boolean shouldCheckSuperclass() {
        return this.shouldCheckSuperclass;
    }

    /* Access modifiers changed, original: protected */
    public SubscriberMethod createSubscriberMethod(String str, Class<?> cls) {
        return createSubscriberMethod(str, cls, ThreadMode.POSTING, 0, false);
    }

    /* Access modifiers changed, original: protected */
    public SubscriberMethod createSubscriberMethod(String str, Class<?> cls, ThreadMode threadMode) {
        return createSubscriberMethod(str, cls, threadMode, 0, false);
    }

    /* Access modifiers changed, original: protected */
    public SubscriberMethod createSubscriberMethod(String str, Class<?> cls, ThreadMode threadMode, int i, boolean z) {
        try {
            return new SubscriberMethod(this.subscriberClass.getDeclaredMethod(str, new Class[]{cls}), cls, threadMode, i, z);
        } catch (NoSuchMethodException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not find subscriber method in ");
            stringBuilder.append(this.subscriberClass);
            stringBuilder.append(". Maybe a missing ProGuard rule?");
            throw new EventBusException(stringBuilder.toString(), e);
        }
    }
}
