package com.facebook.internal;

import com.facebook.FacebookSdk;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

public class LockOnGetVariable<T> {
    private CountDownLatch initLatch;
    private T value;

    public LockOnGetVariable(T t) {
        this.value = t;
    }

    public LockOnGetVariable(final Callable<T> callable) {
        this.initLatch = new CountDownLatch(1);
        FacebookSdk.getExecutor().execute(new FutureTask(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    LockOnGetVariable.this.value = callable.call();
                    return null;
                } finally {
                    LockOnGetVariable.this.initLatch.countDown();
                }
            }
        }));
    }

    public T getValue() {
        waitOnInit();
        return this.value;
    }

    private void waitOnInit() {
        if (this.initLatch != null) {
            try {
                this.initLatch.await();
            } catch (InterruptedException unused) {
            }
        }
    }
}
