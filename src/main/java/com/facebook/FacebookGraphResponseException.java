package com.facebook;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public class FacebookGraphResponseException extends FacebookException {
    private final GraphResponse graphResponse;

    public FacebookGraphResponseException(GraphResponse graphResponse, String str) {
        super(str);
        this.graphResponse = graphResponse;
    }

    public final GraphResponse getGraphResponse() {
        return this.graphResponse;
    }

    public final String toString() {
        FacebookRequestError error = this.graphResponse != null ? this.graphResponse.getError() : null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{FacebookGraphResponseException: ");
        String message = getMessage();
        if (message != null) {
            stringBuilder.append(message);
            stringBuilder.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        }
        if (error != null) {
            stringBuilder.append("httpResponseCode: ");
            stringBuilder.append(error.getRequestStatusCode());
            stringBuilder.append(", facebookErrorCode: ");
            stringBuilder.append(error.getErrorCode());
            stringBuilder.append(", facebookErrorType: ");
            stringBuilder.append(error.getErrorType());
            stringBuilder.append(", message: ");
            stringBuilder.append(error.getErrorMessage());
            stringBuilder.append("}");
        }
        return stringBuilder.toString();
    }
}
