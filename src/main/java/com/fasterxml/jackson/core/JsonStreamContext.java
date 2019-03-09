package com.fasterxml.jackson.core;

import org.slf4j.Logger;

public abstract class JsonStreamContext {
    protected static final int TYPE_ARRAY = 1;
    protected static final int TYPE_OBJECT = 2;
    protected static final int TYPE_ROOT = 0;
    protected int _index;
    protected int _type;

    public abstract String getCurrentName();

    public Object getCurrentValue() {
        return null;
    }

    public abstract JsonStreamContext getParent();

    public void setCurrentValue(Object obj) {
    }

    protected JsonStreamContext() {
    }

    public final boolean inArray() {
        return this._type == 1;
    }

    public final boolean inRoot() {
        return this._type == 0;
    }

    public final boolean inObject() {
        return this._type == 2;
    }

    @Deprecated
    public final String getTypeDesc() {
        switch (this._type) {
            case 0:
                return Logger.ROOT_LOGGER_NAME;
            case 1:
                return "ARRAY";
            case 2:
                return "OBJECT";
            default:
                return "?";
        }
    }

    public String typeDesc() {
        switch (this._type) {
            case 0:
                return "root";
            case 1:
                return "Array";
            case 2:
                return "Object";
            default:
                return "?";
        }
    }

    public final int getEntryCount() {
        return this._index + 1;
    }

    public final int getCurrentIndex() {
        return this._index < 0 ? 0 : this._index;
    }
}
