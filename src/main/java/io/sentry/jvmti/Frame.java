package io.sentry.jvmti;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Frame {
    private final LocalVariable[] locals;
    private Method method;

    public static final class LocalVariable {
        final String name;
        final Object value;

        public LocalVariable(String str, Object obj) {
            this.name = str;
            this.value = obj;
        }

        public String getName() {
            return this.name;
        }

        public Object getValue() {
            return this.value;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("LocalVariable{name='");
            stringBuilder.append(this.name);
            stringBuilder.append('\'');
            stringBuilder.append(", value=");
            stringBuilder.append(this.value);
            stringBuilder.append('}');
            return stringBuilder.toString();
        }
    }

    public Frame(Method method, LocalVariable[] localVariableArr) {
        this.method = method;
        this.locals = localVariableArr;
    }

    public Method getMethod() {
        return this.method;
    }

    public Map<String, Object> getLocals() {
        if (this.locals == null || this.locals.length == 0) {
            return Collections.emptyMap();
        }
        HashMap hashMap = new HashMap();
        for (LocalVariable localVariable : this.locals) {
            if (localVariable != null) {
                hashMap.put(localVariable.getName(), localVariable.getValue());
            }
        }
        return hashMap;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Frame{, locals=");
        stringBuilder.append(Arrays.toString(this.locals));
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
