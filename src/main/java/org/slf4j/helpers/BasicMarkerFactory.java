package org.slf4j.helpers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

public class BasicMarkerFactory implements IMarkerFactory {
    private final ConcurrentMap<String, Marker> markerMap = new ConcurrentHashMap();

    public Marker getMarker(String str) {
        if (str != null) {
            Marker marker = (Marker) this.markerMap.get(str);
            if (marker != null) {
                return marker;
            }
            marker = new BasicMarker(str);
            Marker marker2 = (Marker) this.markerMap.putIfAbsent(str, marker);
            return marker2 != null ? marker2 : marker;
        } else {
            throw new IllegalArgumentException("Marker name cannot be null");
        }
    }

    public boolean exists(String str) {
        return str == null ? false : this.markerMap.containsKey(str);
    }

    public boolean detachMarker(String str) {
        boolean z = false;
        if (str == null) {
            return false;
        }
        if (this.markerMap.remove(str) != null) {
            z = true;
        }
        return z;
    }

    public Marker getDetachedMarker(String str) {
        return new BasicMarker(str);
    }
}
