package io.sentry.context;

public class ThreadLocalContextManager implements ContextManager {
    private final ThreadLocal<Context> context = new ThreadLocal<Context>() {
        /* Access modifiers changed, original: protected */
        public Context initialValue() {
            return new Context();
        }
    };

    public Context getContext() {
        return (Context) this.context.get();
    }

    public void clear() {
        this.context.remove();
    }
}
