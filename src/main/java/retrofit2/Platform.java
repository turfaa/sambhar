package retrofit2;

import android.os.Handler;
import android.os.Looper;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import retrofit2.CallAdapter.Factory;

class Platform {
    private static final Platform PLATFORM = findPlatform();

    static class Android extends Platform {

        static class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());

            MainThreadExecutor() {
            }

            public void execute(Runnable runnable) {
                this.handler.post(runnable);
            }
        }

        Android() {
        }

        public Executor defaultCallbackExecutor() {
            return new MainThreadExecutor();
        }

        /* Access modifiers changed, original: 0000 */
        public Factory defaultCallAdapterFactory(@Nullable Executor executor) {
            if (executor != null) {
                return new ExecutorCallAdapterFactory(executor);
            }
            throw new AssertionError();
        }
    }

    @IgnoreJRERequirement
    static class Java8 extends Platform {
        Java8() {
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isDefaultMethod(Method method) {
            return method.isDefault();
        }

        /* Access modifiers changed, original: varargs */
        public Object invokeDefaultMethod(Method method, Class<?> cls, Object obj, @Nullable Object... objArr) throws Throwable {
            Constructor declaredConstructor = Lookup.class.getDeclaredConstructor(new Class[]{Class.class, Integer.TYPE});
            declaredConstructor.setAccessible(true);
            return ((Lookup) declaredConstructor.newInstance(new Object[]{cls, Integer.valueOf(-1)})).unreflectSpecial(method, cls).bindTo(obj).invokeWithArguments(objArr);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public Executor defaultCallbackExecutor() {
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isDefaultMethod(Method method) {
        return false;
    }

    Platform() {
    }

    static Platform get() {
        return PLATFORM;
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x000f */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|(2:3|4)|5|6|7) */
    /* JADX WARNING: Missing block: B:10:0x001f, code skipped:
            return new retrofit2.Platform();
     */
    private static retrofit2.Platform findPlatform() {
        /*
        r0 = "android.os.Build";
        java.lang.Class.forName(r0);	 Catch:{ ClassNotFoundException -> 0x000f }
        r0 = android.os.Build.VERSION.SDK_INT;	 Catch:{ ClassNotFoundException -> 0x000f }
        if (r0 == 0) goto L_0x000f;
    L_0x0009:
        r0 = new retrofit2.Platform$Android;	 Catch:{ ClassNotFoundException -> 0x000f }
        r0.<init>();	 Catch:{ ClassNotFoundException -> 0x000f }
        return r0;
    L_0x000f:
        r0 = "java.util.Optional";
        java.lang.Class.forName(r0);	 Catch:{ ClassNotFoundException -> 0x001a }
        r0 = new retrofit2.Platform$Java8;	 Catch:{ ClassNotFoundException -> 0x001a }
        r0.<init>();	 Catch:{ ClassNotFoundException -> 0x001a }
        return r0;
    L_0x001a:
        r0 = new retrofit2.Platform;
        r0.<init>();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: retrofit2.Platform.findPlatform():retrofit2.Platform");
    }

    /* Access modifiers changed, original: 0000 */
    public Factory defaultCallAdapterFactory(@Nullable Executor executor) {
        if (executor != null) {
            return new ExecutorCallAdapterFactory(executor);
        }
        return DefaultCallAdapterFactory.INSTANCE;
    }

    /* Access modifiers changed, original: varargs */
    @Nullable
    public Object invokeDefaultMethod(Method method, Class<?> cls, Object obj, @Nullable Object... objArr) throws Throwable {
        throw new UnsupportedOperationException();
    }
}
