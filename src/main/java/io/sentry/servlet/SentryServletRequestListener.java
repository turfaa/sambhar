package io.sentry.servlet;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentryServletRequestListener implements ServletRequestListener {
    private static final ThreadLocal<HttpServletRequest> THREAD_REQUEST = new ThreadLocal();
    private static final Logger logger = LoggerFactory.getLogger(SentryServletRequestListener.class);

    public static HttpServletRequest getServletRequest() {
        return (HttpServletRequest) THREAD_REQUEST.get();
    }

    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        THREAD_REQUEST.remove();
        try {
            SentryClient storedClient = Sentry.getStoredClient();
            if (storedClient != null) {
                storedClient.clearContext();
            }
        } catch (Exception e) {
            logger.error("Error clearing Context state.", e);
        }
    }

    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        ServletRequest servletRequest = servletRequestEvent.getServletRequest();
        if (servletRequest instanceof HttpServletRequest) {
            THREAD_REQUEST.set((HttpServletRequest) servletRequest);
        }
    }
}
