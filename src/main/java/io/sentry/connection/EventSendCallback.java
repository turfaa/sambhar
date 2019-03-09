package io.sentry.connection;

import io.sentry.event.Event;

public interface EventSendCallback {
    void onFailure(Event event, Exception exception);

    void onSuccess(Event event);
}
