package io.sentry.event.helper;

import io.sentry.SentryClient;
import io.sentry.context.Context;
import io.sentry.event.EventBuilder;
import io.sentry.event.User;
import io.sentry.event.interfaces.UserInterface;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ContextBuilderHelper implements EventBuilderHelper {
    private SentryClient sentryClient;

    public ContextBuilderHelper(SentryClient sentryClient) {
        this.sentryClient = sentryClient;
    }

    public void helpBuildingEvent(EventBuilder eventBuilder) {
        Context context = this.sentryClient.getContext();
        List breadcrumbs = context.getBreadcrumbs();
        if (!breadcrumbs.isEmpty()) {
            eventBuilder.withBreadcrumbs(breadcrumbs);
        }
        if (context.getUser() != null) {
            eventBuilder.withSentryInterface(fromUser(context.getUser()));
        }
        Map tags = context.getTags();
        if (!tags.isEmpty()) {
            for (Entry entry : tags.entrySet()) {
                eventBuilder.withTag((String) entry.getKey(), (String) entry.getValue());
            }
        }
        Map extra = context.getExtra();
        if (!extra.isEmpty()) {
            for (Entry entry2 : extra.entrySet()) {
                eventBuilder.withExtra((String) entry2.getKey(), entry2.getValue());
            }
        }
    }

    private UserInterface fromUser(User user) {
        return new UserInterface(user.getId(), user.getUsername(), user.getIpAddress(), user.getEmail(), user.getData());
    }
}
