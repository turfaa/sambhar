package com.facebook.appevents.codeless.internal;

import bolts.MeasurementEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventBinding {
    private final String activityName;
    private final String appVersion;
    private final String componentId;
    private final String eventName;
    private final MappingMethod method;
    private final List<ParameterComponent> parameters;
    private final List<PathComponent> path;
    private final String pathType;
    private final ActionType type;

    public enum ActionType {
        CLICK,
        SELECTED,
        TEXT_CHANGED
    }

    public enum MappingMethod {
        MANUAL,
        INFERENCE
    }

    public EventBinding(String str, MappingMethod mappingMethod, ActionType actionType, String str2, List<PathComponent> list, List<ParameterComponent> list2, String str3, String str4, String str5) {
        this.eventName = str;
        this.method = mappingMethod;
        this.type = actionType;
        this.appVersion = str2;
        this.path = list;
        this.parameters = list2;
        this.componentId = str3;
        this.pathType = str4;
        this.activityName = str5;
    }

    public static List<EventBinding> parseArray(JSONArray jSONArray) {
        int length;
        ArrayList arrayList = new ArrayList();
        int i = 0;
        if (jSONArray != null) {
            try {
                length = jSONArray.length();
            } catch (JSONException unused) {
            }
        } else {
            length = 0;
        }
        while (i < length) {
            arrayList.add(getInstanceFromJson(jSONArray.getJSONObject(i)));
            i++;
        }
        return arrayList;
    }

    public static EventBinding getInstanceFromJson(JSONObject jSONObject) throws JSONException {
        String string = jSONObject.getString(MeasurementEvent.MEASUREMENT_EVENT_NAME_KEY);
        MappingMethod valueOf = MappingMethod.valueOf(jSONObject.getString("method").toUpperCase(Locale.ENGLISH));
        ActionType valueOf2 = ActionType.valueOf(jSONObject.getString("event_type").toUpperCase(Locale.ENGLISH));
        String string2 = jSONObject.getString("app_version");
        JSONArray jSONArray = jSONObject.getJSONArray("path");
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            arrayList.add(new PathComponent(jSONArray.getJSONObject(i)));
        }
        String optString = jSONObject.optString(Constants.EVENT_MAPPING_PATH_TYPE_KEY, Constants.PATH_TYPE_ABSOLUTE);
        jSONArray = jSONObject.optJSONArray("parameters");
        ArrayList arrayList2 = new ArrayList();
        if (jSONArray != null) {
            for (int i2 = 0; i2 < jSONArray.length(); i2++) {
                arrayList2.add(new ParameterComponent(jSONArray.getJSONObject(i2)));
            }
        }
        return new EventBinding(string, valueOf, valueOf2, string2, arrayList, arrayList2, jSONObject.optString("component_id"), optString, jSONObject.optString("activity_name"));
    }

    public List<PathComponent> getViewPath() {
        return Collections.unmodifiableList(this.path);
    }

    public List<ParameterComponent> getViewParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public String getEventName() {
        return this.eventName;
    }

    public ActionType getType() {
        return this.type;
    }

    public MappingMethod getMethod() {
        return this.method;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public String getComponentId() {
        return this.componentId;
    }

    public String getPathType() {
        return this.pathType;
    }

    public String getActivityName() {
        return this.activityName;
    }
}
