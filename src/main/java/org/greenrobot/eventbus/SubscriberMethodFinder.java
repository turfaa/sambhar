package org.greenrobot.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.greenrobot.eventbus.meta.SubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

class SubscriberMethodFinder {
    private static final int BRIDGE = 64;
    private static final FindState[] FIND_STATE_POOL = new FindState[4];
    private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap();
    private static final int MODIFIERS_IGNORE = 5192;
    private static final int POOL_SIZE = 4;
    private static final int SYNTHETIC = 4096;
    private final boolean ignoreGeneratedIndex;
    private final boolean strictMethodVerification;
    private List<SubscriberInfoIndex> subscriberInfoIndexes;

    static class FindState {
        final Map<Class, Object> anyMethodByEventType = new HashMap();
        Class<?> clazz;
        final StringBuilder methodKeyBuilder = new StringBuilder(128);
        boolean skipSuperClasses;
        Class<?> subscriberClass;
        final Map<String, Class> subscriberClassByMethodKey = new HashMap();
        SubscriberInfo subscriberInfo;
        final List<SubscriberMethod> subscriberMethods = new ArrayList();

        FindState() {
        }

        /* Access modifiers changed, original: 0000 */
        public void initForSubscriber(Class<?> cls) {
            this.clazz = cls;
            this.subscriberClass = cls;
            this.skipSuperClasses = false;
            this.subscriberInfo = null;
        }

        /* Access modifiers changed, original: 0000 */
        public void recycle() {
            this.subscriberMethods.clear();
            this.anyMethodByEventType.clear();
            this.subscriberClassByMethodKey.clear();
            this.methodKeyBuilder.setLength(0);
            this.subscriberClass = null;
            this.clazz = null;
            this.skipSuperClasses = false;
            this.subscriberInfo = null;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean checkAdd(Method method, Class<?> cls) {
            Object put = this.anyMethodByEventType.put(cls, method);
            if (put == null) {
                return true;
            }
            if (put instanceof Method) {
                if (checkAddWithMethodSignature((Method) put, cls)) {
                    this.anyMethodByEventType.put(cls, this);
                } else {
                    throw new IllegalStateException();
                }
            }
            return checkAddWithMethodSignature(method, cls);
        }

        private boolean checkAddWithMethodSignature(Method method, Class<?> cls) {
            this.methodKeyBuilder.setLength(0);
            this.methodKeyBuilder.append(method.getName());
            StringBuilder stringBuilder = this.methodKeyBuilder;
            stringBuilder.append('>');
            stringBuilder.append(cls.getName());
            String stringBuilder2 = this.methodKeyBuilder.toString();
            Class declaringClass = method.getDeclaringClass();
            Class cls2 = (Class) this.subscriberClassByMethodKey.put(stringBuilder2, declaringClass);
            if (cls2 == null || cls2.isAssignableFrom(declaringClass)) {
                return true;
            }
            this.subscriberClassByMethodKey.put(stringBuilder2, cls2);
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public void moveToSuperclass() {
            if (this.skipSuperClasses) {
                this.clazz = null;
                return;
            }
            this.clazz = this.clazz.getSuperclass();
            String name = this.clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                this.clazz = null;
            }
        }
    }

    SubscriberMethodFinder(List<SubscriberInfoIndex> list, boolean z, boolean z2) {
        this.subscriberInfoIndexes = list;
        this.strictMethodVerification = z;
        this.ignoreGeneratedIndex = z2;
    }

    /* Access modifiers changed, original: 0000 */
    public List<SubscriberMethod> findSubscriberMethods(Class<?> cls) {
        List list = (List) METHOD_CACHE.get(cls);
        if (list != null) {
            return list;
        }
        if (this.ignoreGeneratedIndex) {
            list = findUsingReflection(cls);
        } else {
            list = findUsingInfo(cls);
        }
        if (list.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Subscriber ");
            stringBuilder.append(cls);
            stringBuilder.append(" and its super classes have no public methods with the @Subscribe annotation");
            throw new EventBusException(stringBuilder.toString());
        }
        METHOD_CACHE.put(cls, list);
        return list;
    }

    private List<SubscriberMethod> findUsingInfo(Class<?> cls) {
        FindState prepareFindState = prepareFindState();
        prepareFindState.initForSubscriber(cls);
        while (prepareFindState.clazz != null) {
            prepareFindState.subscriberInfo = getSubscriberInfo(prepareFindState);
            if (prepareFindState.subscriberInfo != null) {
                for (SubscriberMethod subscriberMethod : prepareFindState.subscriberInfo.getSubscriberMethods()) {
                    if (prepareFindState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                        prepareFindState.subscriberMethods.add(subscriberMethod);
                    }
                }
            } else {
                findUsingReflectionInSingleClass(prepareFindState);
            }
            prepareFindState.moveToSuperclass();
        }
        return getMethodsAndRelease(prepareFindState);
    }

    private List<SubscriberMethod> getMethodsAndRelease(FindState findState) {
        ArrayList arrayList = new ArrayList(findState.subscriberMethods);
        findState.recycle();
        synchronized (FIND_STATE_POOL) {
            int i = 0;
            while (i < 4) {
                try {
                    if (FIND_STATE_POOL[i] == null) {
                        FIND_STATE_POOL[i] = findState;
                        break;
                    }
                    i++;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        return arrayList;
    }

    private FindState prepareFindState() {
        synchronized (FIND_STATE_POOL) {
            int i = 0;
            while (i < 4) {
                try {
                    FindState findState = FIND_STATE_POOL[i];
                    if (findState != null) {
                        FIND_STATE_POOL[i] = null;
                        return findState;
                    }
                    i++;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            return new FindState();
        }
    }

    private SubscriberInfo getSubscriberInfo(FindState findState) {
        if (!(findState.subscriberInfo == null || findState.subscriberInfo.getSuperSubscriberInfo() == null)) {
            SubscriberInfo superSubscriberInfo = findState.subscriberInfo.getSuperSubscriberInfo();
            if (findState.clazz == superSubscriberInfo.getSubscriberClass()) {
                return superSubscriberInfo;
            }
        }
        if (this.subscriberInfoIndexes != null) {
            for (SubscriberInfoIndex subscriberInfo : this.subscriberInfoIndexes) {
                SubscriberInfo subscriberInfo2 = subscriberInfo.getSubscriberInfo(findState.clazz);
                if (subscriberInfo2 != null) {
                    return subscriberInfo2;
                }
            }
        }
        return null;
    }

    private List<SubscriberMethod> findUsingReflection(Class<?> cls) {
        FindState prepareFindState = prepareFindState();
        prepareFindState.initForSubscriber(cls);
        while (prepareFindState.clazz != null) {
            findUsingReflectionInSingleClass(prepareFindState);
            prepareFindState.moveToSuperclass();
        }
        return getMethodsAndRelease(prepareFindState);
    }

    private void findUsingReflectionInSingleClass(FindState findState) {
        Method[] declaredMethods;
        try {
            declaredMethods = findState.clazz.getDeclaredMethods();
        } catch (Throwable unused) {
            declaredMethods = findState.clazz.getMethods();
            findState.skipSuperClasses = true;
        }
        for (Method method : declaredMethods) {
            int modifiers = method.getModifiers();
            StringBuilder stringBuilder;
            String stringBuilder2;
            StringBuilder stringBuilder3;
            if ((modifiers & 1) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                Class[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Subscribe subscribe = (Subscribe) method.getAnnotation(Subscribe.class);
                    if (subscribe != null) {
                        Class cls = parameterTypes[0];
                        if (findState.checkAdd(method, cls)) {
                            findState.subscriberMethods.add(new SubscriberMethod(method, cls, subscribe.threadMode(), subscribe.priority(), subscribe.sticky()));
                        }
                    }
                } else if (this.strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(method.getDeclaringClass().getName());
                    stringBuilder.append(".");
                    stringBuilder.append(method.getName());
                    stringBuilder2 = stringBuilder.toString();
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("@Subscribe method ");
                    stringBuilder3.append(stringBuilder2);
                    stringBuilder3.append("must have exactly 1 parameter but has ");
                    stringBuilder3.append(parameterTypes.length);
                    throw new EventBusException(stringBuilder3.toString());
                }
            } else if (this.strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(method.getDeclaringClass().getName());
                stringBuilder.append(".");
                stringBuilder.append(method.getName());
                stringBuilder2 = stringBuilder.toString();
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append(stringBuilder2);
                stringBuilder3.append(" is a illegal @Subscribe method: must be public, non-static, and non-abstract");
                throw new EventBusException(stringBuilder3.toString());
            }
        }
    }

    static void clearCaches() {
        METHOD_CACHE.clear();
    }
}
