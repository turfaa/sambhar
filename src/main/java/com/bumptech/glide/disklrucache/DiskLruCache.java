package com.bumptech.glide.disklrucache;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class DiskLruCache implements Closeable {
    static final long ANY_SEQUENCE_NUMBER = -1;
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    static final String JOURNAL_FILE_TEMP = "journal.tmp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    private static final String READ = "READ";
    private static final String REMOVE = "REMOVE";
    static final String VERSION_1 = "1";
    private final int appVersion;
    private final Callable<Void> cleanupCallable = new Callable<Void>() {
        /* JADX WARNING: Missing block: B:11:0x0027, code skipped:
            return null;
     */
        public java.lang.Void call() throws java.lang.Exception {
            /*
            r4 = this;
            r0 = com.bumptech.glide.disklrucache.DiskLruCache.this;
            monitor-enter(r0);
            r1 = com.bumptech.glide.disklrucache.DiskLruCache.this;	 Catch:{ all -> 0x0028 }
            r1 = r1.journalWriter;	 Catch:{ all -> 0x0028 }
            r2 = 0;
            if (r1 != 0) goto L_0x000e;
        L_0x000c:
            monitor-exit(r0);	 Catch:{ all -> 0x0028 }
            return r2;
        L_0x000e:
            r1 = com.bumptech.glide.disklrucache.DiskLruCache.this;	 Catch:{ all -> 0x0028 }
            r1.trimToSize();	 Catch:{ all -> 0x0028 }
            r1 = com.bumptech.glide.disklrucache.DiskLruCache.this;	 Catch:{ all -> 0x0028 }
            r1 = r1.journalRebuildRequired();	 Catch:{ all -> 0x0028 }
            if (r1 == 0) goto L_0x0026;
        L_0x001b:
            r1 = com.bumptech.glide.disklrucache.DiskLruCache.this;	 Catch:{ all -> 0x0028 }
            r1.rebuildJournal();	 Catch:{ all -> 0x0028 }
            r1 = com.bumptech.glide.disklrucache.DiskLruCache.this;	 Catch:{ all -> 0x0028 }
            r3 = 0;
            r1.redundantOpCount = r3;	 Catch:{ all -> 0x0028 }
        L_0x0026:
            monitor-exit(r0);	 Catch:{ all -> 0x0028 }
            return r2;
        L_0x0028:
            r1 = move-exception;
            monitor-exit(r0);	 Catch:{ all -> 0x0028 }
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.disklrucache.DiskLruCache$AnonymousClass1.call():java.lang.Void");
        }
    };
    private final File directory;
    final ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new DiskLruCacheThreadFactory());
    private final File journalFile;
    private final File journalFileBackup;
    private final File journalFileTmp;
    private Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap(0, 0.75f, true);
    private long maxSize;
    private long nextSequenceNumber = 0;
    private int redundantOpCount;
    private long size = 0;
    private final int valueCount;

    private static final class DiskLruCacheThreadFactory implements ThreadFactory {
        private DiskLruCacheThreadFactory() {
        }

        /* synthetic */ DiskLruCacheThreadFactory(AnonymousClass1 anonymousClass1) {
            this();
        }

        public synchronized Thread newThread(Runnable runnable) {
            Thread thread;
            thread = new Thread(runnable, "glide-disk-lru-cache-thread");
            thread.setPriority(1);
            return thread;
        }
    }

    public final class Editor {
        private boolean committed;
        private final Entry entry;
        private final boolean[] written;

        /* synthetic */ Editor(DiskLruCache diskLruCache, Entry entry, AnonymousClass1 anonymousClass1) {
            this(entry);
        }

        private Editor(Entry entry) {
            this.entry = entry;
            this.written = entry.readable ? null : new boolean[DiskLruCache.this.valueCount];
        }

        private InputStream newInputStream(int i) throws IOException {
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor != this) {
                    throw new IllegalStateException();
                } else if (this.entry.readable) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(this.entry.getCleanFile(i));
                        return fileInputStream;
                    } catch (FileNotFoundException unused) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        public String getString(int i) throws IOException {
            InputStream newInputStream = newInputStream(i);
            return newInputStream != null ? DiskLruCache.inputStreamToString(newInputStream) : null;
        }

        public File getFile(int i) throws IOException {
            File dirtyFile;
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor == this) {
                    if (!this.entry.readable) {
                        this.written[i] = true;
                    }
                    dirtyFile = this.entry.getDirtyFile(i);
                    if (!DiskLruCache.this.directory.exists()) {
                        DiskLruCache.this.directory.mkdirs();
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
            return dirtyFile;
        }

        public void set(int i, String str) throws IOException {
            Throwable th;
            Closeable closeable = null;
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(getFile(i)), Util.UTF_8);
                try {
                    outputStreamWriter.write(str);
                    Util.closeQuietly(outputStreamWriter);
                } catch (Throwable th2) {
                    th = th2;
                    closeable = outputStreamWriter;
                    Util.closeQuietly(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Util.closeQuietly(closeable);
                throw th;
            }
        }

        public void commit() throws IOException {
            DiskLruCache.this.completeEdit(this, true);
            this.committed = true;
        }

        public void abort() throws IOException {
            DiskLruCache.this.completeEdit(this, false);
        }

        public void abortUnlessCommitted() {
            if (!this.committed) {
                try {
                    abort();
                } catch (IOException unused) {
                }
            }
        }
    }

    private final class Entry {
        File[] cleanFiles;
        private Editor currentEditor;
        File[] dirtyFiles;
        private final String key;
        private final long[] lengths;
        private boolean readable;
        private long sequenceNumber;

        /* synthetic */ Entry(DiskLruCache diskLruCache, String str, AnonymousClass1 anonymousClass1) {
            this(str);
        }

        private Entry(String str) {
            this.key = str;
            this.lengths = new long[DiskLruCache.this.valueCount];
            this.cleanFiles = new File[DiskLruCache.this.valueCount];
            this.dirtyFiles = new File[DiskLruCache.this.valueCount];
            StringBuilder stringBuilder = new StringBuilder(str);
            stringBuilder.append('.');
            int length = stringBuilder.length();
            for (int i = 0; i < DiskLruCache.this.valueCount; i++) {
                stringBuilder.append(i);
                this.cleanFiles[i] = new File(DiskLruCache.this.directory, stringBuilder.toString());
                stringBuilder.append(".tmp");
                this.dirtyFiles[i] = new File(DiskLruCache.this.directory, stringBuilder.toString());
                stringBuilder.setLength(length);
            }
        }

        public String getLengths() throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            for (long j : this.lengths) {
                stringBuilder.append(' ');
                stringBuilder.append(j);
            }
            return stringBuilder.toString();
        }

        private void setLengths(String[] strArr) throws IOException {
            if (strArr.length == DiskLruCache.this.valueCount) {
                int i = 0;
                while (i < strArr.length) {
                    try {
                        this.lengths[i] = Long.parseLong(strArr[i]);
                        i++;
                    } catch (NumberFormatException unused) {
                        throw invalidLengths(strArr);
                    }
                }
                return;
            }
            throw invalidLengths(strArr);
        }

        private IOException invalidLengths(String[] strArr) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unexpected journal line: ");
            stringBuilder.append(Arrays.toString(strArr));
            throw new IOException(stringBuilder.toString());
        }

        public File getCleanFile(int i) {
            return this.cleanFiles[i];
        }

        public File getDirtyFile(int i) {
            return this.dirtyFiles[i];
        }
    }

    public final class Value {
        private final File[] files;
        private final String key;
        private final long[] lengths;
        private final long sequenceNumber;

        /* synthetic */ Value(DiskLruCache diskLruCache, String str, long j, File[] fileArr, long[] jArr, AnonymousClass1 anonymousClass1) {
            this(str, j, fileArr, jArr);
        }

        private Value(String str, long j, File[] fileArr, long[] jArr) {
            this.key = str;
            this.sequenceNumber = j;
            this.files = fileArr;
            this.lengths = jArr;
        }

        public Editor edit() throws IOException {
            return DiskLruCache.this.edit(this.key, this.sequenceNumber);
        }

        public File getFile(int i) {
            return this.files[i];
        }

        public String getString(int i) throws IOException {
            return DiskLruCache.inputStreamToString(new FileInputStream(this.files[i]));
        }

        public long getLength(int i) {
            return this.lengths[i];
        }
    }

    private DiskLruCache(File file, int i, int i2, long j) {
        File file2 = file;
        this.directory = file2;
        this.appVersion = i;
        this.journalFile = new File(file2, JOURNAL_FILE);
        this.journalFileTmp = new File(file2, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(file2, JOURNAL_FILE_BACKUP);
        this.valueCount = i2;
        this.maxSize = j;
    }

    public static DiskLruCache open(File file, int i, int i2, long j) throws IOException {
        if (j <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else if (i2 > 0) {
            File file2 = new File(file, JOURNAL_FILE_BACKUP);
            if (file2.exists()) {
                File file3 = new File(file, JOURNAL_FILE);
                if (file3.exists()) {
                    file2.delete();
                } else {
                    renameTo(file2, file3, false);
                }
            }
            DiskLruCache diskLruCache = new DiskLruCache(file, i, i2, j);
            if (diskLruCache.journalFile.exists()) {
                try {
                    diskLruCache.readJournal();
                    diskLruCache.processJournal();
                    return diskLruCache;
                } catch (IOException e) {
                    PrintStream printStream = System.out;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("DiskLruCache ");
                    stringBuilder.append(file);
                    stringBuilder.append(" is corrupt: ");
                    stringBuilder.append(e.getMessage());
                    stringBuilder.append(", removing");
                    printStream.println(stringBuilder.toString());
                    diskLruCache.delete();
                }
            }
            file.mkdirs();
            diskLruCache = new DiskLruCache(file, i, i2, j);
            diskLruCache.rebuildJournal();
            return diskLruCache;
        } else {
            throw new IllegalArgumentException("valueCount <= 0");
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:23:0x008a=Splitter:B:23:0x008a, B:16:0x005d=Splitter:B:16:0x005d} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:16:0x005d */
    /* JADX WARNING: Can't wrap try/catch for region: R(5:16|17|(1:19)(1:20)|21|22) */
    /* JADX WARNING: Missing block: B:17:?, code skipped:
            r8.redundantOpCount = r1 - r8.lruEntries.size();
     */
    /* JADX WARNING: Missing block: B:18:0x006a, code skipped:
            if (r0.hasUnterminatedLine() != false) goto L_0x006c;
     */
    /* JADX WARNING: Missing block: B:19:0x006c, code skipped:
            rebuildJournal();
     */
    /* JADX WARNING: Missing block: B:20:0x0070, code skipped:
            r8.journalWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(r8.journalFile, true), com.bumptech.glide.disklrucache.Util.US_ASCII));
     */
    /* JADX WARNING: Missing block: B:21:0x0086, code skipped:
            com.bumptech.glide.disklrucache.Util.closeQuietly(r0);
     */
    /* JADX WARNING: Missing block: B:22:0x0089, code skipped:
            return;
     */
    private void readJournal() throws java.io.IOException {
        /*
        r8 = this;
        r0 = new com.bumptech.glide.disklrucache.StrictLineReader;
        r1 = new java.io.FileInputStream;
        r2 = r8.journalFile;
        r1.<init>(r2);
        r2 = com.bumptech.glide.disklrucache.Util.US_ASCII;
        r0.<init>(r1, r2);
        r1 = r0.readLine();	 Catch:{ all -> 0x00be }
        r2 = r0.readLine();	 Catch:{ all -> 0x00be }
        r3 = r0.readLine();	 Catch:{ all -> 0x00be }
        r4 = r0.readLine();	 Catch:{ all -> 0x00be }
        r5 = r0.readLine();	 Catch:{ all -> 0x00be }
        r6 = "libcore.io.DiskLruCache";
        r6 = r6.equals(r1);	 Catch:{ all -> 0x00be }
        if (r6 == 0) goto L_0x008a;
    L_0x002a:
        r6 = "1";
        r6 = r6.equals(r2);	 Catch:{ all -> 0x00be }
        if (r6 == 0) goto L_0x008a;
    L_0x0032:
        r6 = r8.appVersion;	 Catch:{ all -> 0x00be }
        r6 = java.lang.Integer.toString(r6);	 Catch:{ all -> 0x00be }
        r3 = r6.equals(r3);	 Catch:{ all -> 0x00be }
        if (r3 == 0) goto L_0x008a;
    L_0x003e:
        r3 = r8.valueCount;	 Catch:{ all -> 0x00be }
        r3 = java.lang.Integer.toString(r3);	 Catch:{ all -> 0x00be }
        r3 = r3.equals(r4);	 Catch:{ all -> 0x00be }
        if (r3 == 0) goto L_0x008a;
    L_0x004a:
        r3 = "";
        r3 = r3.equals(r5);	 Catch:{ all -> 0x00be }
        if (r3 == 0) goto L_0x008a;
    L_0x0052:
        r1 = 0;
    L_0x0053:
        r2 = r0.readLine();	 Catch:{ EOFException -> 0x005d }
        r8.readJournalLine(r2);	 Catch:{ EOFException -> 0x005d }
        r1 = r1 + 1;
        goto L_0x0053;
    L_0x005d:
        r2 = r8.lruEntries;	 Catch:{ all -> 0x00be }
        r2 = r2.size();	 Catch:{ all -> 0x00be }
        r1 = r1 - r2;
        r8.redundantOpCount = r1;	 Catch:{ all -> 0x00be }
        r1 = r0.hasUnterminatedLine();	 Catch:{ all -> 0x00be }
        if (r1 == 0) goto L_0x0070;
    L_0x006c:
        r8.rebuildJournal();	 Catch:{ all -> 0x00be }
        goto L_0x0086;
    L_0x0070:
        r1 = new java.io.BufferedWriter;	 Catch:{ all -> 0x00be }
        r2 = new java.io.OutputStreamWriter;	 Catch:{ all -> 0x00be }
        r3 = new java.io.FileOutputStream;	 Catch:{ all -> 0x00be }
        r4 = r8.journalFile;	 Catch:{ all -> 0x00be }
        r5 = 1;
        r3.<init>(r4, r5);	 Catch:{ all -> 0x00be }
        r4 = com.bumptech.glide.disklrucache.Util.US_ASCII;	 Catch:{ all -> 0x00be }
        r2.<init>(r3, r4);	 Catch:{ all -> 0x00be }
        r1.<init>(r2);	 Catch:{ all -> 0x00be }
        r8.journalWriter = r1;	 Catch:{ all -> 0x00be }
    L_0x0086:
        com.bumptech.glide.disklrucache.Util.closeQuietly(r0);
        return;
    L_0x008a:
        r3 = new java.io.IOException;	 Catch:{ all -> 0x00be }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00be }
        r6.<init>();	 Catch:{ all -> 0x00be }
        r7 = "unexpected journal header: [";
        r6.append(r7);	 Catch:{ all -> 0x00be }
        r6.append(r1);	 Catch:{ all -> 0x00be }
        r1 = ", ";
        r6.append(r1);	 Catch:{ all -> 0x00be }
        r6.append(r2);	 Catch:{ all -> 0x00be }
        r1 = ", ";
        r6.append(r1);	 Catch:{ all -> 0x00be }
        r6.append(r4);	 Catch:{ all -> 0x00be }
        r1 = ", ";
        r6.append(r1);	 Catch:{ all -> 0x00be }
        r6.append(r5);	 Catch:{ all -> 0x00be }
        r1 = "]";
        r6.append(r1);	 Catch:{ all -> 0x00be }
        r1 = r6.toString();	 Catch:{ all -> 0x00be }
        r3.<init>(r1);	 Catch:{ all -> 0x00be }
        throw r3;	 Catch:{ all -> 0x00be }
    L_0x00be:
        r1 = move-exception;
        com.bumptech.glide.disklrucache.Util.closeQuietly(r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.disklrucache.DiskLruCache.readJournal():void");
    }

    private void readJournalLine(String str) throws IOException {
        int indexOf = str.indexOf(32);
        StringBuilder stringBuilder;
        if (indexOf != -1) {
            Object substring;
            int i = indexOf + 1;
            int indexOf2 = str.indexOf(32, i);
            if (indexOf2 == -1) {
                substring = str.substring(i);
                if (indexOf == REMOVE.length() && str.startsWith(REMOVE)) {
                    this.lruEntries.remove(substring);
                    return;
                }
            }
            substring = str.substring(i, indexOf2);
            Entry entry = (Entry) this.lruEntries.get(substring);
            if (entry == null) {
                entry = new Entry(this, substring, null);
                this.lruEntries.put(substring, entry);
            }
            if (indexOf2 != -1 && indexOf == CLEAN.length() && str.startsWith(CLEAN)) {
                String[] split = str.substring(indexOf2 + 1).split(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
                entry.readable = true;
                entry.currentEditor = null;
                entry.setLengths(split);
            } else if (indexOf2 == -1 && indexOf == DIRTY.length() && str.startsWith(DIRTY)) {
                entry.currentEditor = new Editor(this, entry, null);
            } else if (!(indexOf2 == -1 && indexOf == READ.length() && str.startsWith(READ))) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("unexpected journal line: ");
                stringBuilder.append(str);
                throw new IOException(stringBuilder.toString());
            }
            return;
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("unexpected journal line: ");
        stringBuilder.append(str);
        throw new IOException(stringBuilder.toString());
    }

    private void processJournal() throws IOException {
        deleteIfExists(this.journalFileTmp);
        Iterator it = this.lruEntries.values().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            int i = 0;
            if (entry.currentEditor == null) {
                while (i < this.valueCount) {
                    this.size += entry.lengths[i];
                    i++;
                }
            } else {
                entry.currentEditor = null;
                while (i < this.valueCount) {
                    deleteIfExists(entry.getCleanFile(i));
                    deleteIfExists(entry.getDirtyFile(i));
                    i++;
                }
                it.remove();
            }
        }
    }

    private synchronized void rebuildJournal() throws IOException {
        if (this.journalWriter != null) {
            this.journalWriter.close();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFileTmp), Util.US_ASCII));
        try {
            bufferedWriter.write(MAGIC);
            bufferedWriter.write("\n");
            bufferedWriter.write("1");
            bufferedWriter.write("\n");
            bufferedWriter.write(Integer.toString(this.appVersion));
            bufferedWriter.write("\n");
            bufferedWriter.write(Integer.toString(this.valueCount));
            bufferedWriter.write("\n");
            bufferedWriter.write("\n");
            for (Entry entry : this.lruEntries.values()) {
                StringBuilder stringBuilder;
                if (entry.currentEditor != null) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("DIRTY ");
                    stringBuilder.append(entry.key);
                    stringBuilder.append(10);
                    bufferedWriter.write(stringBuilder.toString());
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("CLEAN ");
                    stringBuilder.append(entry.key);
                    stringBuilder.append(entry.getLengths());
                    stringBuilder.append(10);
                    bufferedWriter.write(stringBuilder.toString());
                }
            }
            if (this.journalFile.exists()) {
                renameTo(this.journalFile, this.journalFileBackup, true);
            }
            renameTo(this.journalFileTmp, this.journalFile, false);
            this.journalFileBackup.delete();
            this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), Util.US_ASCII));
        } finally {
            bufferedWriter.close();
        }
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    private static void renameTo(File file, File file2, boolean z) throws IOException {
        if (z) {
            deleteIfExists(file2);
        }
        if (!file.renameTo(file2)) {
            throw new IOException();
        }
    }

    public synchronized Value get(String str) throws IOException {
        checkNotClosed();
        Entry entry = (Entry) this.lruEntries.get(str);
        if (entry == null) {
            return null;
        }
        if (!entry.readable) {
            return null;
        }
        for (File exists : entry.cleanFiles) {
            if (!exists.exists()) {
                return null;
            }
        }
        this.redundantOpCount++;
        this.journalWriter.append(READ);
        this.journalWriter.append(' ');
        this.journalWriter.append(str);
        this.journalWriter.append(10);
        if (journalRebuildRequired()) {
            this.executorService.submit(this.cleanupCallable);
        }
        return new Value(this, str, entry.sequenceNumber, entry.cleanFiles, entry.lengths, null);
    }

    public Editor edit(String str) throws IOException {
        return edit(str, -1);
    }

    /* JADX WARNING: Missing block: B:9:0x001e, code skipped:
            return null;
     */
    private synchronized com.bumptech.glide.disklrucache.DiskLruCache.Editor edit(java.lang.String r6, long r7) throws java.io.IOException {
        /*
        r5 = this;
        monitor-enter(r5);
        r5.checkNotClosed();	 Catch:{ all -> 0x005d }
        r0 = r5.lruEntries;	 Catch:{ all -> 0x005d }
        r0 = r0.get(r6);	 Catch:{ all -> 0x005d }
        r0 = (com.bumptech.glide.disklrucache.DiskLruCache.Entry) r0;	 Catch:{ all -> 0x005d }
        r1 = -1;
        r3 = 0;
        r4 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1));
        if (r4 == 0) goto L_0x001f;
    L_0x0013:
        if (r0 == 0) goto L_0x001d;
    L_0x0015:
        r1 = r0.sequenceNumber;	 Catch:{ all -> 0x005d }
        r4 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1));
        if (r4 == 0) goto L_0x001f;
    L_0x001d:
        monitor-exit(r5);
        return r3;
    L_0x001f:
        if (r0 != 0) goto L_0x002c;
    L_0x0021:
        r0 = new com.bumptech.glide.disklrucache.DiskLruCache$Entry;	 Catch:{ all -> 0x005d }
        r0.<init>(r5, r6, r3);	 Catch:{ all -> 0x005d }
        r7 = r5.lruEntries;	 Catch:{ all -> 0x005d }
        r7.put(r6, r0);	 Catch:{ all -> 0x005d }
        goto L_0x0034;
    L_0x002c:
        r7 = r0.currentEditor;	 Catch:{ all -> 0x005d }
        if (r7 == 0) goto L_0x0034;
    L_0x0032:
        monitor-exit(r5);
        return r3;
    L_0x0034:
        r7 = new com.bumptech.glide.disklrucache.DiskLruCache$Editor;	 Catch:{ all -> 0x005d }
        r7.<init>(r5, r0, r3);	 Catch:{ all -> 0x005d }
        r0.currentEditor = r7;	 Catch:{ all -> 0x005d }
        r8 = r5.journalWriter;	 Catch:{ all -> 0x005d }
        r0 = "DIRTY";
        r8.append(r0);	 Catch:{ all -> 0x005d }
        r8 = r5.journalWriter;	 Catch:{ all -> 0x005d }
        r0 = 32;
        r8.append(r0);	 Catch:{ all -> 0x005d }
        r8 = r5.journalWriter;	 Catch:{ all -> 0x005d }
        r8.append(r6);	 Catch:{ all -> 0x005d }
        r6 = r5.journalWriter;	 Catch:{ all -> 0x005d }
        r8 = 10;
        r6.append(r8);	 Catch:{ all -> 0x005d }
        r6 = r5.journalWriter;	 Catch:{ all -> 0x005d }
        r6.flush();	 Catch:{ all -> 0x005d }
        monitor-exit(r5);
        return r7;
    L_0x005d:
        r6 = move-exception;
        monitor-exit(r5);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.disklrucache.DiskLruCache.edit(java.lang.String, long):com.bumptech.glide.disklrucache.DiskLruCache$Editor");
    }

    public File getDirectory() {
        return this.directory;
    }

    public synchronized long getMaxSize() {
        return this.maxSize;
    }

    public synchronized void setMaxSize(long j) {
        this.maxSize = j;
        this.executorService.submit(this.cleanupCallable);
    }

    public synchronized long size() {
        return this.size;
    }

    /* JADX WARNING: Missing block: B:43:0x0108, code skipped:
            return;
     */
    private synchronized void completeEdit(com.bumptech.glide.disklrucache.DiskLruCache.Editor r10, boolean r11) throws java.io.IOException {
        /*
        r9 = this;
        monitor-enter(r9);
        r0 = r10.entry;	 Catch:{ all -> 0x010f }
        r1 = r0.currentEditor;	 Catch:{ all -> 0x010f }
        if (r1 != r10) goto L_0x0109;
    L_0x000b:
        r1 = 0;
        if (r11 == 0) goto L_0x004d;
    L_0x000e:
        r2 = r0.readable;	 Catch:{ all -> 0x010f }
        if (r2 != 0) goto L_0x004d;
    L_0x0014:
        r2 = 0;
    L_0x0015:
        r3 = r9.valueCount;	 Catch:{ all -> 0x010f }
        if (r2 >= r3) goto L_0x004d;
    L_0x0019:
        r3 = r10.written;	 Catch:{ all -> 0x010f }
        r3 = r3[r2];	 Catch:{ all -> 0x010f }
        if (r3 == 0) goto L_0x0033;
    L_0x0021:
        r3 = r0.getDirtyFile(r2);	 Catch:{ all -> 0x010f }
        r3 = r3.exists();	 Catch:{ all -> 0x010f }
        if (r3 != 0) goto L_0x0030;
    L_0x002b:
        r10.abort();	 Catch:{ all -> 0x010f }
        monitor-exit(r9);
        return;
    L_0x0030:
        r2 = r2 + 1;
        goto L_0x0015;
    L_0x0033:
        r10.abort();	 Catch:{ all -> 0x010f }
        r10 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x010f }
        r11 = new java.lang.StringBuilder;	 Catch:{ all -> 0x010f }
        r11.<init>();	 Catch:{ all -> 0x010f }
        r0 = "Newly created entry didn't create value for index ";
        r11.append(r0);	 Catch:{ all -> 0x010f }
        r11.append(r2);	 Catch:{ all -> 0x010f }
        r11 = r11.toString();	 Catch:{ all -> 0x010f }
        r10.<init>(r11);	 Catch:{ all -> 0x010f }
        throw r10;	 Catch:{ all -> 0x010f }
    L_0x004d:
        r10 = r9.valueCount;	 Catch:{ all -> 0x010f }
        if (r1 >= r10) goto L_0x0082;
    L_0x0051:
        r10 = r0.getDirtyFile(r1);	 Catch:{ all -> 0x010f }
        if (r11 == 0) goto L_0x007c;
    L_0x0057:
        r2 = r10.exists();	 Catch:{ all -> 0x010f }
        if (r2 == 0) goto L_0x007f;
    L_0x005d:
        r2 = r0.getCleanFile(r1);	 Catch:{ all -> 0x010f }
        r10.renameTo(r2);	 Catch:{ all -> 0x010f }
        r10 = r0.lengths;	 Catch:{ all -> 0x010f }
        r3 = r10[r1];	 Catch:{ all -> 0x010f }
        r5 = r2.length();	 Catch:{ all -> 0x010f }
        r10 = r0.lengths;	 Catch:{ all -> 0x010f }
        r10[r1] = r5;	 Catch:{ all -> 0x010f }
        r7 = r9.size;	 Catch:{ all -> 0x010f }
        r10 = 0;
        r7 = r7 - r3;
        r7 = r7 + r5;
        r9.size = r7;	 Catch:{ all -> 0x010f }
        goto L_0x007f;
    L_0x007c:
        deleteIfExists(r10);	 Catch:{ all -> 0x010f }
    L_0x007f:
        r1 = r1 + 1;
        goto L_0x004d;
    L_0x0082:
        r10 = r9.redundantOpCount;	 Catch:{ all -> 0x010f }
        r1 = 1;
        r10 = r10 + r1;
        r9.redundantOpCount = r10;	 Catch:{ all -> 0x010f }
        r10 = 0;
        r0.currentEditor = r10;	 Catch:{ all -> 0x010f }
        r10 = r0.readable;	 Catch:{ all -> 0x010f }
        r10 = r10 | r11;
        r2 = 10;
        r3 = 32;
        if (r10 == 0) goto L_0x00ca;
    L_0x0097:
        r0.readable = r1;	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r1 = "CLEAN";
        r10.append(r1);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r10.append(r3);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r1 = r0.key;	 Catch:{ all -> 0x010f }
        r10.append(r1);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r1 = r0.getLengths();	 Catch:{ all -> 0x010f }
        r10.append(r1);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r10.append(r2);	 Catch:{ all -> 0x010f }
        if (r11 == 0) goto L_0x00ed;
    L_0x00bf:
        r10 = r9.nextSequenceNumber;	 Catch:{ all -> 0x010f }
        r1 = 1;
        r1 = r1 + r10;
        r9.nextSequenceNumber = r1;	 Catch:{ all -> 0x010f }
        r0.sequenceNumber = r10;	 Catch:{ all -> 0x010f }
        goto L_0x00ed;
    L_0x00ca:
        r10 = r9.lruEntries;	 Catch:{ all -> 0x010f }
        r11 = r0.key;	 Catch:{ all -> 0x010f }
        r10.remove(r11);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r11 = "REMOVE";
        r10.append(r11);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r10.append(r3);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r11 = r0.key;	 Catch:{ all -> 0x010f }
        r10.append(r11);	 Catch:{ all -> 0x010f }
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r10.append(r2);	 Catch:{ all -> 0x010f }
    L_0x00ed:
        r10 = r9.journalWriter;	 Catch:{ all -> 0x010f }
        r10.flush();	 Catch:{ all -> 0x010f }
        r10 = r9.size;	 Catch:{ all -> 0x010f }
        r0 = r9.maxSize;	 Catch:{ all -> 0x010f }
        r2 = (r10 > r0 ? 1 : (r10 == r0 ? 0 : -1));
        if (r2 > 0) goto L_0x0100;
    L_0x00fa:
        r10 = r9.journalRebuildRequired();	 Catch:{ all -> 0x010f }
        if (r10 == 0) goto L_0x0107;
    L_0x0100:
        r10 = r9.executorService;	 Catch:{ all -> 0x010f }
        r11 = r9.cleanupCallable;	 Catch:{ all -> 0x010f }
        r10.submit(r11);	 Catch:{ all -> 0x010f }
    L_0x0107:
        monitor-exit(r9);
        return;
    L_0x0109:
        r10 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x010f }
        r10.<init>();	 Catch:{ all -> 0x010f }
        throw r10;	 Catch:{ all -> 0x010f }
    L_0x010f:
        r10 = move-exception;
        monitor-exit(r9);
        throw r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.disklrucache.DiskLruCache.completeEdit(com.bumptech.glide.disklrucache.DiskLruCache$Editor, boolean):void");
    }

    private boolean journalRebuildRequired() {
        return this.redundantOpCount >= 2000 && this.redundantOpCount >= this.lruEntries.size();
    }

    /* JADX WARNING: Missing block: B:21:0x008d, code skipped:
            return true;
     */
    /* JADX WARNING: Missing block: B:23:0x008f, code skipped:
            return false;
     */
    public synchronized boolean remove(java.lang.String r8) throws java.io.IOException {
        /*
        r7 = this;
        monitor-enter(r7);
        r7.checkNotClosed();	 Catch:{ all -> 0x0090 }
        r0 = r7.lruEntries;	 Catch:{ all -> 0x0090 }
        r0 = r0.get(r8);	 Catch:{ all -> 0x0090 }
        r0 = (com.bumptech.glide.disklrucache.DiskLruCache.Entry) r0;	 Catch:{ all -> 0x0090 }
        r1 = 0;
        if (r0 == 0) goto L_0x008e;
    L_0x000f:
        r2 = r0.currentEditor;	 Catch:{ all -> 0x0090 }
        if (r2 == 0) goto L_0x0017;
    L_0x0015:
        goto L_0x008e;
    L_0x0017:
        r2 = r7.valueCount;	 Catch:{ all -> 0x0090 }
        if (r1 >= r2) goto L_0x005a;
    L_0x001b:
        r2 = r0.getCleanFile(r1);	 Catch:{ all -> 0x0090 }
        r3 = r2.exists();	 Catch:{ all -> 0x0090 }
        if (r3 == 0) goto L_0x0043;
    L_0x0025:
        r3 = r2.delete();	 Catch:{ all -> 0x0090 }
        if (r3 == 0) goto L_0x002c;
    L_0x002b:
        goto L_0x0043;
    L_0x002c:
        r8 = new java.io.IOException;	 Catch:{ all -> 0x0090 }
        r0 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0090 }
        r0.<init>();	 Catch:{ all -> 0x0090 }
        r1 = "failed to delete ";
        r0.append(r1);	 Catch:{ all -> 0x0090 }
        r0.append(r2);	 Catch:{ all -> 0x0090 }
        r0 = r0.toString();	 Catch:{ all -> 0x0090 }
        r8.<init>(r0);	 Catch:{ all -> 0x0090 }
        throw r8;	 Catch:{ all -> 0x0090 }
    L_0x0043:
        r2 = r7.size;	 Catch:{ all -> 0x0090 }
        r4 = r0.lengths;	 Catch:{ all -> 0x0090 }
        r5 = r4[r1];	 Catch:{ all -> 0x0090 }
        r4 = 0;
        r2 = r2 - r5;
        r7.size = r2;	 Catch:{ all -> 0x0090 }
        r2 = r0.lengths;	 Catch:{ all -> 0x0090 }
        r3 = 0;
        r2[r1] = r3;	 Catch:{ all -> 0x0090 }
        r1 = r1 + 1;
        goto L_0x0017;
    L_0x005a:
        r0 = r7.redundantOpCount;	 Catch:{ all -> 0x0090 }
        r1 = 1;
        r0 = r0 + r1;
        r7.redundantOpCount = r0;	 Catch:{ all -> 0x0090 }
        r0 = r7.journalWriter;	 Catch:{ all -> 0x0090 }
        r2 = "REMOVE";
        r0.append(r2);	 Catch:{ all -> 0x0090 }
        r0 = r7.journalWriter;	 Catch:{ all -> 0x0090 }
        r2 = 32;
        r0.append(r2);	 Catch:{ all -> 0x0090 }
        r0 = r7.journalWriter;	 Catch:{ all -> 0x0090 }
        r0.append(r8);	 Catch:{ all -> 0x0090 }
        r0 = r7.journalWriter;	 Catch:{ all -> 0x0090 }
        r2 = 10;
        r0.append(r2);	 Catch:{ all -> 0x0090 }
        r0 = r7.lruEntries;	 Catch:{ all -> 0x0090 }
        r0.remove(r8);	 Catch:{ all -> 0x0090 }
        r8 = r7.journalRebuildRequired();	 Catch:{ all -> 0x0090 }
        if (r8 == 0) goto L_0x008c;
    L_0x0085:
        r8 = r7.executorService;	 Catch:{ all -> 0x0090 }
        r0 = r7.cleanupCallable;	 Catch:{ all -> 0x0090 }
        r8.submit(r0);	 Catch:{ all -> 0x0090 }
    L_0x008c:
        monitor-exit(r7);
        return r1;
    L_0x008e:
        monitor-exit(r7);
        return r1;
    L_0x0090:
        r8 = move-exception;
        monitor-exit(r7);
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.disklrucache.DiskLruCache.remove(java.lang.String):boolean");
    }

    public synchronized boolean isClosed() {
        return this.journalWriter == null;
    }

    private void checkNotClosed() {
        if (this.journalWriter == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    public synchronized void flush() throws IOException {
        checkNotClosed();
        trimToSize();
        this.journalWriter.flush();
    }

    public synchronized void close() throws IOException {
        if (this.journalWriter != null) {
            Iterator it = new ArrayList(this.lruEntries.values()).iterator();
            while (it.hasNext()) {
                Entry entry = (Entry) it.next();
                if (entry.currentEditor != null) {
                    entry.currentEditor.abort();
                }
            }
            trimToSize();
            this.journalWriter.close();
            this.journalWriter = null;
        }
    }

    private void trimToSize() throws IOException {
        while (this.size > this.maxSize) {
            remove((String) ((java.util.Map.Entry) this.lruEntries.entrySet().iterator().next()).getKey());
        }
    }

    public void delete() throws IOException {
        close();
        Util.deleteContents(this.directory);
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        return Util.readFully(new InputStreamReader(inputStream, Util.UTF_8));
    }
}
