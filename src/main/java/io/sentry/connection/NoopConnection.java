package io.sentry.connection;

import io.sentry.event.Event;
import java.io.IOException;

public class NoopConnection extends AbstractConnection {
    public void close() throws IOException {
    }

    /* Access modifiers changed, original: protected */
    public void doSend(Event event) throws ConnectionException {
    }

    public NoopConnection() {
        super(null, null);
    }
}
