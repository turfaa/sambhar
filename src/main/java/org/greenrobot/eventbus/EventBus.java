package org.greenrobot.eventbus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class EventBus {
    private static final EventBusBuilder DEFAULT_BUILDER = new EventBusBuilder();
    public static String TAG = "EventBus";
    static volatile EventBus defaultInstance;
    private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap();
    private final AsyncPoster asyncPoster;
    private final BackgroundPoster backgroundPoster;
    private final ThreadLocal<PostingThreadState> currentPostingThreadState;
    private final boolean eventInheritance;
    private final ExecutorService executorService;
    private final int indexCount;
    private final boolean logNoSubscriberMessages;
    private final boolean logSubscriberExceptions;
    private final Logger logger;
    private final Poster mainThreadPoster;
    private final MainThreadSupport mainThreadSupport;
    private final boolean sendNoSubscriberEvent;
    private final boolean sendSubscriberExceptionEvent;
    private final Map<Class<?>, Object> stickyEvents;
    private final SubscriberMethodFinder subscriberMethodFinder;
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    private final boolean throwSubscriberException;
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    interface PostCallback {
        void onPostCompleted(List<SubscriberExceptionEvent> list);
    }

    static final class PostingThreadState {
        boolean canceled;
        Object event;
        final List<Object> eventQueue = new ArrayList();
        boolean isMainThread;
        boolean isPosting;
        Subscription subscription;

        PostingThreadState() {
        }
    }

    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }

    public static EventBusBuilder builder() {
        return new EventBusBuilder();
    }

    public static void clearCaches() {
        SubscriberMethodFinder.clearCaches();
        eventTypesCache.clear();
    }

    public EventBus() {
        this(DEFAULT_BUILDER);
    }

    EventBus(EventBusBuilder eventBusBuilder) {
        this.currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
            /* Access modifiers changed, original: protected */
            public PostingThreadState initialValue() {
                return new PostingThreadState();
            }
        };
        this.logger = eventBusBuilder.getLogger();
        this.subscriptionsByEventType = new HashMap();
        this.typesBySubscriber = new HashMap();
        this.stickyEvents = new ConcurrentHashMap();
        this.mainThreadSupport = eventBusBuilder.getMainThreadSupport();
        this.mainThreadPoster = this.mainThreadSupport != null ? this.mainThreadSupport.createPoster(this) : null;
        this.backgroundPoster = new BackgroundPoster(this);
        this.asyncPoster = new AsyncPoster(this);
        this.indexCount = eventBusBuilder.subscriberInfoIndexes != null ? eventBusBuilder.subscriberInfoIndexes.size() : 0;
        this.subscriberMethodFinder = new SubscriberMethodFinder(eventBusBuilder.subscriberInfoIndexes, eventBusBuilder.strictMethodVerification, eventBusBuilder.ignoreGeneratedIndex);
        this.logSubscriberExceptions = eventBusBuilder.logSubscriberExceptions;
        this.logNoSubscriberMessages = eventBusBuilder.logNoSubscriberMessages;
        this.sendSubscriberExceptionEvent = eventBusBuilder.sendSubscriberExceptionEvent;
        this.sendNoSubscriberEvent = eventBusBuilder.sendNoSubscriberEvent;
        this.throwSubscriberException = eventBusBuilder.throwSubscriberException;
        this.eventInheritance = eventBusBuilder.eventInheritance;
        this.executorService = eventBusBuilder.executorService;
    }

    public void register(Object obj) {
        List<SubscriberMethod> findSubscriberMethods = this.subscriberMethodFinder.findSubscriberMethods(obj.getClass());
        synchronized (this) {
            for (SubscriberMethod subscribe : findSubscriberMethods) {
                subscribe(obj, subscribe);
            }
        }
    }

    private void subscribe(Object obj, SubscriberMethod subscriberMethod) {
        Class cls = subscriberMethod.eventType;
        Subscription subscription = new Subscription(obj, subscriberMethod);
        CopyOnWriteArrayList copyOnWriteArrayList = (CopyOnWriteArrayList) this.subscriptionsByEventType.get(cls);
        if (copyOnWriteArrayList == null) {
            copyOnWriteArrayList = new CopyOnWriteArrayList();
            this.subscriptionsByEventType.put(cls, copyOnWriteArrayList);
        } else if (copyOnWriteArrayList.contains(subscription)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Subscriber ");
            stringBuilder.append(obj.getClass());
            stringBuilder.append(" already registered to event ");
            stringBuilder.append(cls);
            throw new EventBusException(stringBuilder.toString());
        }
        int size = copyOnWriteArrayList.size();
        int i = 0;
        while (i <= size) {
            if (i == size || subscriberMethod.priority > ((Subscription) copyOnWriteArrayList.get(i)).subscriberMethod.priority) {
                copyOnWriteArrayList.add(i, subscription);
                break;
            }
            i++;
        }
        List list = (List) this.typesBySubscriber.get(obj);
        if (list == null) {
            list = new ArrayList();
            this.typesBySubscriber.put(obj, list);
        }
        list.add(cls);
        if (!subscriberMethod.sticky) {
            return;
        }
        if (this.eventInheritance) {
            for (Entry entry : this.stickyEvents.entrySet()) {
                if (cls.isAssignableFrom((Class) entry.getKey())) {
                    checkPostStickyEventToSubscription(subscription, entry.getValue());
                }
            }
            return;
        }
        checkPostStickyEventToSubscription(subscription, this.stickyEvents.get(cls));
    }

    private void checkPostStickyEventToSubscription(Subscription subscription, Object obj) {
        if (obj != null) {
            postToSubscription(subscription, obj, isMainThread());
        }
    }

    private boolean isMainThread() {
        return this.mainThreadSupport != null ? this.mainThreadSupport.isMainThread() : true;
    }

    public synchronized boolean isRegistered(Object obj) {
        return this.typesBySubscriber.containsKey(obj);
    }

    private void unsubscribeByEventType(Object obj, Class<?> cls) {
        List list = (List) this.subscriptionsByEventType.get(cls);
        if (list != null) {
            int size = list.size();
            int i = 0;
            while (i < size) {
                Subscription subscription = (Subscription) list.get(i);
                if (subscription.subscriber == obj) {
                    subscription.active = false;
                    list.remove(i);
                    i--;
                    size--;
                }
                i++;
            }
        }
    }

    public synchronized void unregister(Object obj) {
        List<Class> list = (List) this.typesBySubscriber.get(obj);
        if (list != null) {
            for (Class unsubscribeByEventType : list) {
                unsubscribeByEventType(obj, unsubscribeByEventType);
            }
            this.typesBySubscriber.remove(obj);
        } else {
            Logger logger = this.logger;
            Level level = Level.WARNING;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Subscriber to unregister was not registered before: ");
            stringBuilder.append(obj.getClass());
            logger.log(level, stringBuilder.toString());
        }
    }

    public void post(Object obj) {
        PostingThreadState postingThreadState = (PostingThreadState) this.currentPostingThreadState.get();
        List list = postingThreadState.eventQueue;
        list.add(obj);
        if (!postingThreadState.isPosting) {
            postingThreadState.isMainThread = isMainThread();
            postingThreadState.isPosting = true;
            if (postingThreadState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            while (!list.isEmpty()) {
                try {
                    postSingleEvent(list.remove(0), postingThreadState);
                } finally {
                    postingThreadState.isPosting = false;
                    postingThreadState.isMainThread = false;
                }
            }
        }
    }

    public void cancelEventDelivery(Object obj) {
        PostingThreadState postingThreadState = (PostingThreadState) this.currentPostingThreadState.get();
        if (!postingThreadState.isPosting) {
            throw new EventBusException("This method may only be called from inside event handling methods on the posting thread");
        } else if (obj == null) {
            throw new EventBusException("Event may not be null");
        } else if (postingThreadState.event != obj) {
            throw new EventBusException("Only the currently handled event may be aborted");
        } else if (postingThreadState.subscription.subscriberMethod.threadMode == ThreadMode.POSTING) {
            postingThreadState.canceled = true;
        } else {
            throw new EventBusException(" event handlers may only abort the incoming event");
        }
    }

    public void postSticky(Object obj) {
        synchronized (this.stickyEvents) {
            this.stickyEvents.put(obj.getClass(), obj);
        }
        post(obj);
    }

    public <T> T getStickyEvent(Class<T> cls) {
        Object cast;
        synchronized (this.stickyEvents) {
            cast = cls.cast(this.stickyEvents.get(cls));
        }
        return cast;
    }

    public <T> T removeStickyEvent(Class<T> cls) {
        Object cast;
        synchronized (this.stickyEvents) {
            cast = cls.cast(this.stickyEvents.remove(cls));
        }
        return cast;
    }

    public boolean removeStickyEvent(Object obj) {
        synchronized (this.stickyEvents) {
            Class cls = obj.getClass();
            if (obj.equals(this.stickyEvents.get(cls))) {
                this.stickyEvents.remove(cls);
                return true;
            }
            return false;
        }
    }

    public void removeAllStickyEvents() {
        synchronized (this.stickyEvents) {
            this.stickyEvents.clear();
        }
    }

    public boolean hasSubscriberForEvent(Class<?> cls) {
        List lookupAllEventTypes = lookupAllEventTypes(cls);
        if (lookupAllEventTypes != null) {
            int size = lookupAllEventTypes.size();
            for (int i = 0; i < size; i++) {
                CopyOnWriteArrayList copyOnWriteArrayList;
                Class cls2 = (Class) lookupAllEventTypes.get(i);
                synchronized (this) {
                    copyOnWriteArrayList = (CopyOnWriteArrayList) this.subscriptionsByEventType.get(cls2);
                }
                if (copyOnWriteArrayList != null && !copyOnWriteArrayList.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void postSingleEvent(Object obj, PostingThreadState postingThreadState) throws Error {
        int i;
        Class cls = obj.getClass();
        if (this.eventInheritance) {
            List lookupAllEventTypes = lookupAllEventTypes(cls);
            i = 0;
            for (int i2 = 0; i2 < lookupAllEventTypes.size(); i2++) {
                i |= postSingleEventForEventType(obj, postingThreadState, (Class) lookupAllEventTypes.get(i2));
            }
        } else {
            i = postSingleEventForEventType(obj, postingThreadState, cls);
        }
        if (i == 0) {
            if (this.logNoSubscriberMessages) {
                Logger logger = this.logger;
                Level level = Level.FINE;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("No subscribers registered for event ");
                stringBuilder.append(cls);
                logger.log(level, stringBuilder.toString());
            }
            if (this.sendNoSubscriberEvent && cls != NoSubscriberEvent.class && cls != SubscriberExceptionEvent.class) {
                post(new NoSubscriberEvent(this, obj));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0040 A:{SYNTHETIC, EDGE_INSN: B:24:0x0040->B:17:0x0040 ?: BREAK  } */
    private boolean postSingleEventForEventType(java.lang.Object r5, org.greenrobot.eventbus.EventBus.PostingThreadState r6, java.lang.Class<?> r7) {
        /*
        r4 = this;
        monitor-enter(r4);
        r0 = r4.subscriptionsByEventType;	 Catch:{ all -> 0x0043 }
        r7 = r0.get(r7);	 Catch:{ all -> 0x0043 }
        r7 = (java.util.concurrent.CopyOnWriteArrayList) r7;	 Catch:{ all -> 0x0043 }
        monitor-exit(r4);	 Catch:{ all -> 0x0043 }
        r0 = 0;
        if (r7 == 0) goto L_0x0042;
    L_0x000d:
        r1 = r7.isEmpty();
        if (r1 != 0) goto L_0x0042;
    L_0x0013:
        r7 = r7.iterator();
    L_0x0017:
        r1 = r7.hasNext();
        if (r1 == 0) goto L_0x0040;
    L_0x001d:
        r1 = r7.next();
        r1 = (org.greenrobot.eventbus.Subscription) r1;
        r6.event = r5;
        r6.subscription = r1;
        r2 = 0;
        r3 = r6.isMainThread;	 Catch:{ all -> 0x0038 }
        r4.postToSubscription(r1, r5, r3);	 Catch:{ all -> 0x0038 }
        r1 = r6.canceled;	 Catch:{ all -> 0x0038 }
        r6.event = r2;
        r6.subscription = r2;
        r6.canceled = r0;
        if (r1 == 0) goto L_0x0017;
    L_0x0037:
        goto L_0x0040;
    L_0x0038:
        r5 = move-exception;
        r6.event = r2;
        r6.subscription = r2;
        r6.canceled = r0;
        throw r5;
    L_0x0040:
        r5 = 1;
        return r5;
    L_0x0042:
        return r0;
    L_0x0043:
        r5 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0043 }
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.greenrobot.eventbus.EventBus.postSingleEventForEventType(java.lang.Object, org.greenrobot.eventbus.EventBus$PostingThreadState, java.lang.Class):boolean");
    }

    private void postToSubscription(Subscription subscription, Object obj, boolean z) {
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                invokeSubscriber(subscription, obj);
                return;
            case MAIN:
                if (z) {
                    invokeSubscriber(subscription, obj);
                    return;
                } else {
                    this.mainThreadPoster.enqueue(subscription, obj);
                    return;
                }
            case MAIN_ORDERED:
                if (this.mainThreadPoster != null) {
                    this.mainThreadPoster.enqueue(subscription, obj);
                    return;
                } else {
                    invokeSubscriber(subscription, obj);
                    return;
                }
            case BACKGROUND:
                if (z) {
                    this.backgroundPoster.enqueue(subscription, obj);
                    return;
                } else {
                    invokeSubscriber(subscription, obj);
                    return;
                }
            case ASYNC:
                this.asyncPoster.enqueue(subscription, obj);
                return;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown thread mode: ");
                stringBuilder.append(subscription.subscriberMethod.threadMode);
                throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private static List<Class<?>> lookupAllEventTypes(Class<?> cls) {
        List<Class<?>> list;
        synchronized (eventTypesCache) {
            list = (List) eventTypesCache.get(cls);
            if (list == null) {
                list = new ArrayList();
                for (Object obj = cls; obj != null; obj = obj.getSuperclass()) {
                    list.add(obj);
                    addInterfaces(list, obj.getInterfaces());
                }
                eventTypesCache.put(cls, list);
            }
        }
        return list;
    }

    static void addInterfaces(List<Class<?>> list, Class<?>[] clsArr) {
        for (Object obj : clsArr) {
            if (!list.contains(obj)) {
                list.add(obj);
                addInterfaces(list, obj.getInterfaces());
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void invokeSubscriber(PendingPost pendingPost) {
        Object obj = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        if (subscription.active) {
            invokeSubscriber(subscription, obj);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void invokeSubscriber(Subscription subscription, Object obj) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, new Object[]{obj});
        } catch (InvocationTargetException e) {
            handleSubscriberException(subscription, obj, e.getCause());
        } catch (IllegalAccessException e2) {
            throw new IllegalStateException("Unexpected exception", e2);
        }
    }

    private void handleSubscriberException(Subscription subscription, Object obj, Throwable th) {
        Logger logger;
        Level level;
        StringBuilder stringBuilder;
        if (obj instanceof SubscriberExceptionEvent) {
            if (this.logSubscriberExceptions) {
                logger = this.logger;
                level = Level.SEVERE;
                stringBuilder = new StringBuilder();
                stringBuilder.append("SubscriberExceptionEvent subscriber ");
                stringBuilder.append(subscription.subscriber.getClass());
                stringBuilder.append(" threw an exception");
                logger.log(level, stringBuilder.toString(), th);
                SubscriberExceptionEvent subscriberExceptionEvent = (SubscriberExceptionEvent) obj;
                Logger logger2 = this.logger;
                Level level2 = Level.SEVERE;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Initial event ");
                stringBuilder2.append(subscriberExceptionEvent.causingEvent);
                stringBuilder2.append(" caused exception in ");
                stringBuilder2.append(subscriberExceptionEvent.causingSubscriber);
                logger2.log(level2, stringBuilder2.toString(), subscriberExceptionEvent.throwable);
            }
        } else if (this.throwSubscriberException) {
            throw new EventBusException("Invoking subscriber failed", th);
        } else {
            if (this.logSubscriberExceptions) {
                logger = this.logger;
                level = Level.SEVERE;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Could not dispatch event: ");
                stringBuilder.append(obj.getClass());
                stringBuilder.append(" to subscribing class ");
                stringBuilder.append(subscription.subscriber.getClass());
                logger.log(level, stringBuilder.toString(), th);
            }
            if (this.sendSubscriberExceptionEvent) {
                post(new SubscriberExceptionEvent(this, th, obj, subscription.subscriber));
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("EventBus[indexCount=");
        stringBuilder.append(this.indexCount);
        stringBuilder.append(", eventInheritance=");
        stringBuilder.append(this.eventInheritance);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
