package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.util.RequestPayload;

public class JsonParseException extends JsonProcessingException {
    private static final long serialVersionUID = 2;
    protected transient JsonParser _processor;
    protected RequestPayload _requestPayload;

    @Deprecated
    public JsonParseException(String str, JsonLocation jsonLocation) {
        super(str, jsonLocation);
    }

    @Deprecated
    public JsonParseException(String str, JsonLocation jsonLocation, Throwable th) {
        super(str, jsonLocation, th);
    }

    public JsonParseException(JsonParser jsonParser, String str) {
        super(str, jsonParser == null ? null : jsonParser.getCurrentLocation());
        this._processor = jsonParser;
    }

    public JsonParseException(JsonParser jsonParser, String str, Throwable th) {
        super(str, jsonParser == null ? null : jsonParser.getCurrentLocation(), th);
        this._processor = jsonParser;
    }

    public JsonParseException(JsonParser jsonParser, String str, JsonLocation jsonLocation) {
        super(str, jsonLocation);
        this._processor = jsonParser;
    }

    public JsonParseException(JsonParser jsonParser, String str, JsonLocation jsonLocation, Throwable th) {
        super(str, jsonLocation, th);
        this._processor = jsonParser;
    }

    public JsonParseException withParser(JsonParser jsonParser) {
        this._processor = jsonParser;
        return this;
    }

    public JsonParseException withRequestPayload(RequestPayload requestPayload) {
        this._requestPayload = requestPayload;
        return this;
    }

    public JsonParser getProcessor() {
        return this._processor;
    }

    public RequestPayload getRequestPayload() {
        return this._requestPayload;
    }

    public String getRequestPayloadAsString() {
        return this._requestPayload != null ? this._requestPayload.toString() : null;
    }

    public String getMessage() {
        String message = super.getMessage();
        if (this._requestPayload == null) {
            return message;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message);
        stringBuilder.append("\nRequest payload : ");
        stringBuilder.append(this._requestPayload.toString());
        return stringBuilder.toString();
    }
}
