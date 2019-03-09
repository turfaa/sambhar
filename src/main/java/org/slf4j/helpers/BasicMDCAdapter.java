package org.slf4j.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.spi.MDCAdapter;

public class BasicMDCAdapter implements MDCAdapter {
    private InheritableThreadLocal<Map<String, String>> inheritableThreadLocal = new InheritableThreadLocal<Map<String, String>>() {
        /* Access modifiers changed, original: protected */
        public Map<String, String> childValue(Map<String, String> map) {
            return map == null ? null : new HashMap(map);
        }
    };

    public void put(String str, String str2) {
        if (str != null) {
            Map map = (Map) this.inheritableThreadLocal.get();
            if (map == null) {
                map = new HashMap();
                this.inheritableThreadLocal.set(map);
            }
            map.put(str, str2);
            return;
        }
        throw new IllegalArgumentException("key cannot be null");
    }

    public String get(String str) {
        Map map = (Map) this.inheritableThreadLocal.get();
        return (map == null || str == null) ? null : (String) map.get(str);
    }

    public void remove(String str) {
        Map map = (Map) this.inheritableThreadLocal.get();
        if (map != null) {
            map.remove(str);
        }
    }

    public void clear() {
        Map map = (Map) this.inheritableThreadLocal.get();
        if (map != null) {
            map.clear();
            this.inheritableThreadLocal.remove();
        }
    }

    public Set<String> getKeys() {
        Map map = (Map) this.inheritableThreadLocal.get();
        return map != null ? map.keySet() : null;
    }

    public Map<String, String> getCopyOfContextMap() {
        Map map = (Map) this.inheritableThreadLocal.get();
        return map != null ? new HashMap(map) : null;
    }

    public void setContextMap(Map<String, String> map) {
        this.inheritableThreadLocal.set(new HashMap(map));
    }
}
