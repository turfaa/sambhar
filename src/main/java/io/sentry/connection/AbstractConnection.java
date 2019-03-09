package io.sentry.connection;

import io.sentry.environment.SentryEnvironment;
import io.sentry.event.Event;
import io.sentry.util.Util;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnection implements Connection {
    public static final String SENTRY_PROTOCOL_VERSION = "6";
    private static final Logger lockdownLogger;
    private static final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);
    private final String authHeader;
    private Set<EventSendCallback> eventSendCallbacks = new HashSet();
    private LockdownManager lockdownManager = new LockdownManager();

    public abstract void doSend(Event event) throws ConnectionException;

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AbstractConnection.class.getName());
        stringBuilder.append(".lockdown");
        lockdownLogger = LoggerFactory.getLogger(stringBuilder.toString());
    }

    protected AbstractConnection(String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sentry sentry_version=6,sentry_client=");
        stringBuilder.append(SentryEnvironment.getSentryName());
        stringBuilder.append(",");
        stringBuilder.append("sentry_key=");
        stringBuilder.append(str);
        if (Util.isNullOrEmpty(str2)) {
            str = "";
        } else {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(",sentry_secret=");
            stringBuilder2.append(str2);
            str = stringBuilder2.toString();
        }
        stringBuilder.append(str);
        this.authHeader = stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public String getAuthHeader() {
        return this.authHeader;
    }

    public final void send(Event event) throws ConnectionException {
        try {
            if (this.lockdownManager.isLockedDown()) {
                throw new LockedDownException();
            }
            doSend(event);
            this.lockdownManager.unlock();
            for (EventSendCallback eventSendCallback : this.eventSendCallbacks) {
                try {
                    eventSendCallback.onSuccess(event);
                } catch (Exception e) {
                    Logger logger = logger;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("An exception occurred while running an EventSendCallback.onSuccess: ");
                    stringBuilder.append(eventSendCallback.getClass().getName());
                    logger.warn(stringBuilder.toString(), e);
                }
            }
        } catch (ConnectionException e2) {
            for (EventSendCallback eventSendCallback2 : this.eventSendCallbacks) {
                try {
                    eventSendCallback2.onFailure(event, e2);
                } catch (Exception e3) {
                    Logger logger2 = logger;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("An exception occurred while running an EventSendCallback.onFailure: ");
                    stringBuilder2.append(eventSendCallback2.getClass().getName());
                    logger2.warn(stringBuilder2.toString(), e3);
                }
            }
            if (this.lockdownManager.lockdown(e2)) {
                Logger logger3 = lockdownLogger;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Initiated a temporary lockdown because of exception: ");
                stringBuilder3.append(e2.getMessage());
                logger3.warn(stringBuilder3.toString());
            }
            throw e2;
        }
    }

    public void addEventSendCallback(EventSendCallback eventSendCallback) {
        this.eventSendCallbacks.add(eventSendCallback);
    }
}
