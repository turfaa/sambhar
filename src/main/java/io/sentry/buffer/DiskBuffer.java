package io.sentry.buffer;

import io.sentry.event.Event;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskBuffer implements Buffer {
    public static final String FILE_SUFFIX = ".sentry-event";
    private static final Logger logger = LoggerFactory.getLogger(DiskBuffer.class);
    private final File bufferDir;
    private int maxEvents;

    public DiskBuffer(File file, int i) {
        this.bufferDir = file;
        this.maxEvents = i;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Could not create or write to disk buffer dir: ");
        stringBuilder.append(file.getAbsolutePath());
        String stringBuilder2 = stringBuilder.toString();
        try {
            file.mkdirs();
            if (file.isDirectory() && file.canWrite()) {
                Logger logger = logger;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append(Integer.toString(getNumStoredEvents()));
                stringBuilder3.append(" stored events found in dir: ");
                stringBuilder3.append(file.getAbsolutePath());
                logger.debug(stringBuilder3.toString());
                return;
            }
            throw new RuntimeException(stringBuilder2);
        } catch (Exception e) {
            throw new RuntimeException(stringBuilder2, e);
        }
    }

    public void add(Event event) {
        ObjectOutputStream objectOutputStream;
        Throwable th;
        Throwable th2;
        StringBuilder stringBuilder;
        if (getNumStoredEvents() >= this.maxEvents) {
            Logger logger = logger;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Not adding Event because at least ");
            stringBuilder.append(Integer.toString(this.maxEvents));
            stringBuilder.append(" events are already stored: ");
            stringBuilder.append(event.getId());
            logger.warn(stringBuilder.toString());
            return;
        }
        String absolutePath = this.bufferDir.getAbsolutePath();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(event.getId().toString());
        stringBuilder2.append(FILE_SUFFIX);
        File file = new File(absolutePath, stringBuilder2.toString());
        Logger logger2;
        if (file.exists()) {
            logger2 = logger;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Not adding Event to offline storage because it already exists: ");
            stringBuilder.append(file.getAbsolutePath());
            logger2.trace(stringBuilder.toString());
            return;
        }
        Logger logger3 = logger;
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Adding Event to offline storage: ");
        stringBuilder2.append(file.getAbsolutePath());
        logger3.debug(stringBuilder2.toString());
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            try {
                objectOutputStream.writeObject(event);
                objectOutputStream.close();
                fileOutputStream.close();
            } catch (Throwable th22) {
                Throwable th3 = th22;
                th22 = th;
                th = th3;
            }
        } catch (Exception th4) {
            logger3 = logger;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Error writing Event to offline storage: ");
            stringBuilder2.append(event.getId());
            logger3.error(stringBuilder2.toString(), th4);
        } catch (Throwable unused) {
        }
        logger2 = logger;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(Integer.toString(getNumStoredEvents()));
        stringBuilder3.append(" stored events are now in dir: ");
        stringBuilder3.append(this.bufferDir.getAbsolutePath());
        logger2.debug(stringBuilder3.toString());
        return;
        throw th;
        if (th22 != null) {
            try {
                objectOutputStream.close();
            } catch (Throwable unused2) {
            }
        } else {
            objectOutputStream.close();
        }
        throw th;
    }

    public void discard(Event event) {
        File file = this.bufferDir;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(event.getId().toString());
        stringBuilder.append(FILE_SUFFIX);
        File file2 = new File(file, stringBuilder.toString());
        if (file2.exists()) {
            Logger logger = logger;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Discarding Event from offline storage: ");
            stringBuilder2.append(file2.getAbsolutePath());
            logger.debug(stringBuilder2.toString());
            if (!file2.delete()) {
                logger = logger;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Failed to delete Event: ");
                stringBuilder2.append(file2.getAbsolutePath());
                logger.warn(stringBuilder2.toString());
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0070 A:{Splitter:B:3:0x000f, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x007f A:{SYNTHETIC, Splitter:B:43:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x007b A:{SYNTHETIC, Splitter:B:41:0x007b} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:33:0x0070, code skipped:
            r2 = th;
     */
    /* JADX WARNING: Missing block: B:34:0x0071, code skipped:
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:42:?, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:44:?, code skipped:
            r1.close();
     */
    private io.sentry.event.Event fileToEvent(java.io.File r7) {
        /*
        r6 = this;
        r0 = 0;
        r1 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
        r2 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
        r3 = r7.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
        r2.<init>(r3);	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
        r1.<init>(r2);	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
        r2 = new java.io.ObjectInputStream;	 Catch:{ Throwable -> 0x0073, all -> 0x0070 }
        r2.<init>(r1);	 Catch:{ Throwable -> 0x0073, all -> 0x0070 }
        r3 = r2.readObject();	 Catch:{ Throwable -> 0x0060, all -> 0x005d }
        r2.close();	 Catch:{ Throwable -> 0x0073, all -> 0x0070 }
        r1.close();	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
        r3 = (io.sentry.event.Event) r3;	 Catch:{ Exception -> 0x0021 }
        return r3;
    L_0x0021:
        r1 = move-exception;
        r2 = logger;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Error casting Object to Event: ";
        r3.append(r4);
        r4 = r7.getAbsolutePath();
        r3.append(r4);
        r3 = r3.toString();
        r2.error(r3, r1);
        r1 = r7.delete();
        if (r1 != 0) goto L_0x005c;
    L_0x0042:
        r1 = logger;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Failed to delete Event: ";
        r2.append(r3);
        r7 = r7.getAbsolutePath();
        r2.append(r7);
        r7 = r2.toString();
        r1.warn(r7);
    L_0x005c:
        return r0;
    L_0x005d:
        r3 = move-exception;
        r4 = r0;
        goto L_0x0066;
    L_0x0060:
        r3 = move-exception;
        throw r3;	 Catch:{ all -> 0x0062 }
    L_0x0062:
        r4 = move-exception;
        r5 = r4;
        r4 = r3;
        r3 = r5;
    L_0x0066:
        if (r4 == 0) goto L_0x006c;
    L_0x0068:
        r2.close();	 Catch:{ Throwable -> 0x006f, all -> 0x0070 }
        goto L_0x006f;
    L_0x006c:
        r2.close();	 Catch:{ Throwable -> 0x0073, all -> 0x0070 }
    L_0x006f:
        throw r3;	 Catch:{ Throwable -> 0x0073, all -> 0x0070 }
    L_0x0070:
        r2 = move-exception;
        r3 = r0;
        goto L_0x0079;
    L_0x0073:
        r2 = move-exception;
        throw r2;	 Catch:{ all -> 0x0075 }
    L_0x0075:
        r3 = move-exception;
        r5 = r3;
        r3 = r2;
        r2 = r5;
    L_0x0079:
        if (r3 == 0) goto L_0x007f;
    L_0x007b:
        r1.close();	 Catch:{ Throwable -> 0x0082 }
        goto L_0x0082;
    L_0x007f:
        r1.close();	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
    L_0x0082:
        throw r2;	 Catch:{ FileNotFoundException -> 0x00bf, Exception -> 0x0083 }
    L_0x0083:
        r1 = move-exception;
        r2 = logger;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Error reading Event file: ";
        r3.append(r4);
        r4 = r7.getAbsolutePath();
        r3.append(r4);
        r3 = r3.toString();
        r2.error(r3, r1);
        r1 = r7.delete();
        if (r1 != 0) goto L_0x00be;
    L_0x00a4:
        r1 = logger;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Failed to delete Event: ";
        r2.append(r3);
        r7 = r7.getAbsolutePath();
        r2.append(r7);
        r7 = r2.toString();
        r1.warn(r7);
    L_0x00be:
        return r0;
    L_0x00bf:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: io.sentry.buffer.DiskBuffer.fileToEvent(java.io.File):io.sentry.event.Event");
    }

    private Event getNextEvent(Iterator<File> it) {
        while (it.hasNext()) {
            File file = (File) it.next();
            if (file.getAbsolutePath().endsWith(FILE_SUFFIX)) {
                Event fileToEvent = fileToEvent(file);
                if (fileToEvent != null) {
                    return fileToEvent;
                }
            }
        }
        return null;
    }

    public Iterator<Event> getEvents() {
        final Iterator it = Arrays.asList(this.bufferDir.listFiles()).iterator();
        return new Iterator<Event>() {
            private Event next = DiskBuffer.this.getNextEvent(it);

            public boolean hasNext() {
                return this.next != null;
            }

            public Event next() {
                Event event = this.next;
                this.next = DiskBuffer.this.getNextEvent(it);
                return event;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private int getNumStoredEvents() {
        int i = 0;
        for (File absolutePath : this.bufferDir.listFiles()) {
            if (absolutePath.getAbsolutePath().endsWith(FILE_SUFFIX)) {
                i++;
            }
        }
        return i;
    }
}
