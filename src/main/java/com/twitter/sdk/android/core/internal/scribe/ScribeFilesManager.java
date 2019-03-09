package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import com.twitter.sdk.android.core.internal.CurrentTimeProvider;
import java.io.IOException;
import java.util.UUID;

class ScribeFilesManager extends EventsFilesManager<ScribeEvent> {
    static final String FILE_EXTENSION = ".tap";
    static final String FILE_PREFIX = "se";

    public ScribeFilesManager(Context context, EventTransform<ScribeEvent> eventTransform, CurrentTimeProvider currentTimeProvider, QueueFileEventStorage queueFileEventStorage, int i) throws IOException {
        super(context, eventTransform, currentTimeProvider, queueFileEventStorage, i);
    }

    /* Access modifiers changed, original: protected */
    public String generateUniqueRollOverFileName() {
        UUID randomUUID = UUID.randomUUID();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FILE_PREFIX);
        stringBuilder.append(EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR);
        stringBuilder.append(randomUUID.toString());
        stringBuilder.append(EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR);
        stringBuilder.append(this.currentTimeProvider.getCurrentTimeMillis());
        stringBuilder.append(FILE_EXTENSION);
        return stringBuilder.toString();
    }
}
