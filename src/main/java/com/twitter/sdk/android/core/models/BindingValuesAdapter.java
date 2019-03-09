package com.twitter.sdk.android.core.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class BindingValuesAdapter implements JsonSerializer<BindingValues>, JsonDeserializer<BindingValues> {
    private static final String BOOLEAN_MEMBER = "boolean_value";
    private static final String BOOLEAN_TYPE = "BOOLEAN";
    private static final String IMAGE_TYPE = "IMAGE";
    private static final String IMAGE_VALUE_MEMBER = "image_value";
    private static final String STRING_TYPE = "STRING";
    private static final String TYPE_MEMBER = "type";
    private static final String TYPE_VALUE_MEMBER = "string_value";
    private static final String USER_TYPE = "USER";
    private static final String USER_VALUE_MEMBER = "user_value";

    public JsonElement serialize(BindingValues bindingValues, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;
    }

    public BindingValues deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonElement.isJsonObject()) {
            return new BindingValues();
        }
        Set<Entry> entrySet = jsonElement.getAsJsonObject().entrySet();
        HashMap hashMap = new HashMap(32);
        for (Entry entry : entrySet) {
            hashMap.put((String) entry.getKey(), getValue(((JsonElement) entry.getValue()).getAsJsonObject(), jsonDeserializationContext));
        }
        return new BindingValues(hashMap);
    }

    /* Access modifiers changed, original: 0000 */
    public Object getValue(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        JsonElement jsonElement = jsonObject.get("type");
        if (jsonElement == null || !jsonElement.isJsonPrimitive()) {
            return null;
        }
        String asString = jsonElement.getAsString();
        Object obj = -1;
        int hashCode = asString.hashCode();
        if (hashCode != -1838656495) {
            if (hashCode != 2614219) {
                if (hashCode != 69775675) {
                    if (hashCode == 782694408 && asString.equals(BOOLEAN_TYPE)) {
                        obj = 3;
                    }
                } else if (asString.equals("IMAGE")) {
                    obj = 1;
                }
            } else if (asString.equals(USER_TYPE)) {
                obj = 2;
            }
        } else if (asString.equals(STRING_TYPE)) {
            obj = null;
        }
        switch (obj) {
            case null:
                return jsonDeserializationContext.deserialize(jsonObject.get(TYPE_VALUE_MEMBER), String.class);
            case 1:
                return jsonDeserializationContext.deserialize(jsonObject.get(IMAGE_VALUE_MEMBER), ImageValue.class);
            case 2:
                return jsonDeserializationContext.deserialize(jsonObject.get(USER_VALUE_MEMBER), UserValue.class);
            case 3:
                return jsonDeserializationContext.deserialize(jsonObject.get(BOOLEAN_MEMBER), Boolean.class);
            default:
                return null;
        }
    }
}
