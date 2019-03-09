package org.greenrobot.eventbus.util;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ExceptionToResourceMapping {
    public final Map<Class<? extends Throwable>, Integer> throwableToMsgIdMap = new HashMap();

    public Integer mapThrowable(Throwable th) {
        StringBuilder stringBuilder;
        Throwable th2 = th;
        int i = 20;
        do {
            Integer mapThrowableFlat = mapThrowableFlat(th2);
            if (mapThrowableFlat != null) {
                return mapThrowableFlat;
            }
            th2 = th2.getCause();
            i--;
            if (i <= 0 || th2 == th) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("No specific message resource ID found for ");
                stringBuilder.append(th);
                Log.d("EventBus", stringBuilder.toString());
            }
        } while (th2 != null);
        stringBuilder = new StringBuilder();
        stringBuilder.append("No specific message resource ID found for ");
        stringBuilder.append(th);
        Log.d("EventBus", stringBuilder.toString());
        return null;
    }

    /* Access modifiers changed, original: protected */
    public Integer mapThrowableFlat(Throwable th) {
        Class cls = th.getClass();
        Integer num = (Integer) this.throwableToMsgIdMap.get(cls);
        if (num == null) {
            Class cls2 = null;
            for (Entry entry : this.throwableToMsgIdMap.entrySet()) {
                Class cls3 = (Class) entry.getKey();
                if (cls3.isAssignableFrom(cls) && (cls2 == null || cls2.isAssignableFrom(cls3))) {
                    num = (Integer) entry.getValue();
                    cls2 = cls3;
                }
            }
        }
        return num;
    }

    public ExceptionToResourceMapping addMapping(Class<? extends Throwable> cls, int i) {
        this.throwableToMsgIdMap.put(cls, Integer.valueOf(i));
        return this;
    }
}
