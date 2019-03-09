package okhttp3.internal.cache2;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.ByteString;
import okio.Source;
import okio.Timeout;

final class Relay {
    private static final long FILE_HEADER_SIZE = 32;
    static final ByteString PREFIX_CLEAN = ByteString.encodeUtf8("OkHttp cache v1\n");
    static final ByteString PREFIX_DIRTY = ByteString.encodeUtf8("OkHttp DIRTY :(\n");
    private static final int SOURCE_FILE = 2;
    private static final int SOURCE_UPSTREAM = 1;
    final Buffer buffer = new Buffer();
    final long bufferMaxSize;
    boolean complete;
    RandomAccessFile file;
    private final ByteString metadata;
    int sourceCount;
    Source upstream;
    final Buffer upstreamBuffer = new Buffer();
    long upstreamPos;
    Thread upstreamReader;

    class RelaySource implements Source {
        private FileOperator fileOperator = new FileOperator(Relay.this.file.getChannel());
        private long sourcePos;
        private final Timeout timeout = new Timeout();

        RelaySource() {
        }

        /* JADX WARNING: Missing block: B:17:0x0039, code skipped:
            r5 = r7 - r1.this$0.buffer.size();
     */
        /* JADX WARNING: Missing block: B:18:0x0048, code skipped:
            if (r1.sourcePos >= r5) goto L_0x0120;
     */
        /* JADX WARNING: Missing block: B:20:0x004b, code skipped:
            r5 = 2;
     */
        /* JADX WARNING: Missing block: B:74:?, code skipped:
            r2 = java.lang.Math.min(r2, r7 - r1.sourcePos);
            r1.this$0.buffer.copyTo(r22, r1.sourcePos - r5, r2);
            r1.sourcePos += r2;
     */
        /* JADX WARNING: Missing block: B:76:0x013e, code skipped:
            return r2;
     */
        public long read(okio.Buffer r22, long r23) throws java.io.IOException {
            /*
            r21 = this;
            r1 = r21;
            r2 = r23;
            r0 = r1.fileOperator;
            if (r0 == 0) goto L_0x0142;
        L_0x0008:
            r4 = okhttp3.internal.cache2.Relay.this;
            monitor-enter(r4);
        L_0x000b:
            r5 = r1.sourcePos;	 Catch:{ all -> 0x013f }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r7 = r0.upstreamPos;	 Catch:{ all -> 0x013f }
            r0 = 2;
            r9 = -1;
            r11 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1));
            if (r11 != 0) goto L_0x0039;
        L_0x0018:
            r5 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r5 = r5.complete;	 Catch:{ all -> 0x013f }
            if (r5 == 0) goto L_0x0020;
        L_0x001e:
            monitor-exit(r4);	 Catch:{ all -> 0x013f }
            return r9;
        L_0x0020:
            r5 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r5 = r5.upstreamReader;	 Catch:{ all -> 0x013f }
            if (r5 == 0) goto L_0x002e;
        L_0x0026:
            r0 = r1.timeout;	 Catch:{ all -> 0x013f }
            r5 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r0.waitUntilNotified(r5);	 Catch:{ all -> 0x013f }
            goto L_0x000b;
        L_0x002e:
            r5 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r6 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x013f }
            r5.upstreamReader = r6;	 Catch:{ all -> 0x013f }
            r5 = 1;
            monitor-exit(r4);	 Catch:{ all -> 0x013f }
            goto L_0x004c;
        L_0x0039:
            r5 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r5 = r5.buffer;	 Catch:{ all -> 0x013f }
            r5 = r5.size();	 Catch:{ all -> 0x013f }
            r11 = 0;
            r5 = r7 - r5;
            r11 = r1.sourcePos;	 Catch:{ all -> 0x013f }
            r13 = (r11 > r5 ? 1 : (r11 == r5 ? 0 : -1));
            if (r13 >= 0) goto L_0x0120;
        L_0x004a:
            monitor-exit(r4);	 Catch:{ all -> 0x013f }
            r5 = 2;
        L_0x004c:
            r11 = 32;
            if (r5 != r0) goto L_0x006a;
        L_0x0050:
            r4 = r1.sourcePos;
            r7 = r7 - r4;
            r2 = java.lang.Math.min(r2, r7);
            r13 = r1.fileOperator;
            r4 = r1.sourcePos;
            r14 = r4 + r11;
            r16 = r22;
            r17 = r2;
            r13.read(r14, r16, r17);
            r4 = r1.sourcePos;
            r4 = r4 + r2;
            r1.sourcePos = r4;
            return r2;
        L_0x006a:
            r4 = 0;
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            r0 = r0.upstream;	 Catch:{ all -> 0x010e }
            r5 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            r5 = r5.upstreamBuffer;	 Catch:{ all -> 0x010e }
            r6 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            r13 = r6.bufferMaxSize;	 Catch:{ all -> 0x010e }
            r5 = r0.read(r5, r13);	 Catch:{ all -> 0x010e }
            r0 = (r5 > r9 ? 1 : (r5 == r9 ? 0 : -1));
            if (r0 != 0) goto L_0x0095;
        L_0x007f:
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            r0.commit(r7);	 Catch:{ all -> 0x010e }
            r2 = okhttp3.internal.cache2.Relay.this;
            monitor-enter(r2);
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x0092 }
            r0.upstreamReader = r4;	 Catch:{ all -> 0x0092 }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x0092 }
            r0.notifyAll();	 Catch:{ all -> 0x0092 }
            monitor-exit(r2);	 Catch:{ all -> 0x0092 }
            return r9;
        L_0x0092:
            r0 = move-exception;
            monitor-exit(r2);	 Catch:{ all -> 0x0092 }
            throw r0;
        L_0x0095:
            r2 = java.lang.Math.min(r5, r2);	 Catch:{ all -> 0x010e }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            r13 = r0.upstreamBuffer;	 Catch:{ all -> 0x010e }
            r15 = 0;
            r14 = r22;
            r17 = r2;
            r13.copyTo(r14, r15, r17);	 Catch:{ all -> 0x010e }
            r9 = r1.sourcePos;	 Catch:{ all -> 0x010e }
            r0 = 0;
            r9 = r9 + r2;
            r1.sourcePos = r9;	 Catch:{ all -> 0x010e }
            r15 = r1.fileOperator;	 Catch:{ all -> 0x010e }
            r0 = 0;
            r16 = r7 + r11;
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            r0 = r0.upstreamBuffer;	 Catch:{ all -> 0x010e }
            r18 = r0.clone();	 Catch:{ all -> 0x010e }
            r19 = r5;
            r15.write(r16, r18, r19);	 Catch:{ all -> 0x010e }
            r7 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010e }
            monitor-enter(r7);	 Catch:{ all -> 0x010e }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r0 = r0.buffer;	 Catch:{ all -> 0x010b }
            r8 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r8 = r8.upstreamBuffer;	 Catch:{ all -> 0x010b }
            r0.write(r8, r5);	 Catch:{ all -> 0x010b }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r0 = r0.buffer;	 Catch:{ all -> 0x010b }
            r8 = r0.size();	 Catch:{ all -> 0x010b }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r10 = r0.bufferMaxSize;	 Catch:{ all -> 0x010b }
            r0 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
            if (r0 <= 0) goto L_0x00f1;
        L_0x00dc:
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r0 = r0.buffer;	 Catch:{ all -> 0x010b }
            r8 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r8 = r8.buffer;	 Catch:{ all -> 0x010b }
            r8 = r8.size();	 Catch:{ all -> 0x010b }
            r10 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r10 = r10.bufferMaxSize;	 Catch:{ all -> 0x010b }
            r12 = 0;
            r8 = r8 - r10;
            r0.skip(r8);	 Catch:{ all -> 0x010b }
        L_0x00f1:
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x010b }
            r8 = r0.upstreamPos;	 Catch:{ all -> 0x010b }
            r10 = 0;
            r8 = r8 + r5;
            r0.upstreamPos = r8;	 Catch:{ all -> 0x010b }
            monitor-exit(r7);	 Catch:{ all -> 0x010b }
            r5 = okhttp3.internal.cache2.Relay.this;
            monitor-enter(r5);
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x0108 }
            r0.upstreamReader = r4;	 Catch:{ all -> 0x0108 }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x0108 }
            r0.notifyAll();	 Catch:{ all -> 0x0108 }
            monitor-exit(r5);	 Catch:{ all -> 0x0108 }
            return r2;
        L_0x0108:
            r0 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x0108 }
            throw r0;
        L_0x010b:
            r0 = move-exception;
            monitor-exit(r7);	 Catch:{ all -> 0x010b }
            throw r0;	 Catch:{ all -> 0x010e }
        L_0x010e:
            r0 = move-exception;
            r2 = okhttp3.internal.cache2.Relay.this;
            monitor-enter(r2);
            r3 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x011d }
            r3.upstreamReader = r4;	 Catch:{ all -> 0x011d }
            r3 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x011d }
            r3.notifyAll();	 Catch:{ all -> 0x011d }
            monitor-exit(r2);	 Catch:{ all -> 0x011d }
            throw r0;
        L_0x011d:
            r0 = move-exception;
            monitor-exit(r2);	 Catch:{ all -> 0x011d }
            throw r0;
        L_0x0120:
            r9 = r1.sourcePos;	 Catch:{ all -> 0x013f }
            r0 = 0;
            r7 = r7 - r9;
            r2 = java.lang.Math.min(r2, r7);	 Catch:{ all -> 0x013f }
            r0 = okhttp3.internal.cache2.Relay.this;	 Catch:{ all -> 0x013f }
            r9 = r0.buffer;	 Catch:{ all -> 0x013f }
            r7 = r1.sourcePos;	 Catch:{ all -> 0x013f }
            r0 = 0;
            r11 = r7 - r5;
            r10 = r22;
            r13 = r2;
            r9.copyTo(r10, r11, r13);	 Catch:{ all -> 0x013f }
            r5 = r1.sourcePos;	 Catch:{ all -> 0x013f }
            r0 = 0;
            r5 = r5 + r2;
            r1.sourcePos = r5;	 Catch:{ all -> 0x013f }
            monitor-exit(r4);	 Catch:{ all -> 0x013f }
            return r2;
        L_0x013f:
            r0 = move-exception;
            monitor-exit(r4);	 Catch:{ all -> 0x013f }
            throw r0;
        L_0x0142:
            r0 = new java.lang.IllegalStateException;
            r2 = "closed";
            r0.<init>(r2);
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.cache2.Relay$RelaySource.read(okio.Buffer, long):long");
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void close() throws IOException {
            if (this.fileOperator != null) {
                Closeable closeable = null;
                this.fileOperator = null;
                synchronized (Relay.this) {
                    Relay relay = Relay.this;
                    relay.sourceCount--;
                    if (Relay.this.sourceCount == 0) {
                        RandomAccessFile randomAccessFile = Relay.this.file;
                        Relay.this.file = null;
                        closeable = randomAccessFile;
                    }
                }
                if (closeable != null) {
                    Util.closeQuietly(closeable);
                }
            }
        }
    }

    private Relay(RandomAccessFile randomAccessFile, Source source, long j, ByteString byteString, long j2) {
        this.file = randomAccessFile;
        this.upstream = source;
        this.complete = source == null;
        this.upstreamPos = j;
        this.metadata = byteString;
        this.bufferMaxSize = j2;
    }

    public static Relay edit(File file, Source source, ByteString byteString, long j) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        Relay relay = new Relay(randomAccessFile, source, 0, byteString, j);
        randomAccessFile.setLength(0);
        relay.writeHeader(PREFIX_DIRTY, -1, -1);
        return relay;
    }

    public static Relay read(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileOperator fileOperator = new FileOperator(randomAccessFile.getChannel());
        Buffer buffer = new Buffer();
        fileOperator.read(0, buffer, 32);
        if (buffer.readByteString((long) PREFIX_CLEAN.size()).equals(PREFIX_CLEAN)) {
            long readLong = buffer.readLong();
            long readLong2 = buffer.readLong();
            buffer = new Buffer();
            fileOperator.read(readLong + 32, buffer, readLong2);
            return new Relay(randomAccessFile, null, readLong, buffer.readByteString(), 0);
        }
        throw new IOException("unreadable cache file");
    }

    private void writeHeader(ByteString byteString, long j, long j2) throws IOException {
        Buffer buffer = new Buffer();
        buffer.write(byteString);
        buffer.writeLong(j);
        buffer.writeLong(j2);
        if (buffer.size() == 32) {
            new FileOperator(this.file.getChannel()).write(0, buffer, 32);
            return;
        }
        throw new IllegalArgumentException();
    }

    private void writeMetadata(long j) throws IOException {
        Buffer buffer = new Buffer();
        buffer.write(this.metadata);
        new FileOperator(this.file.getChannel()).write(32 + j, buffer, (long) this.metadata.size());
    }

    /* Access modifiers changed, original: 0000 */
    public void commit(long j) throws IOException {
        writeMetadata(j);
        this.file.getChannel().force(false);
        writeHeader(PREFIX_CLEAN, j, (long) this.metadata.size());
        this.file.getChannel().force(false);
        synchronized (this) {
            this.complete = true;
        }
        Util.closeQuietly(this.upstream);
        this.upstream = null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isClosed() {
        return this.file == null;
    }

    public ByteString metadata() {
        return this.metadata;
    }

    public Source newSource() {
        synchronized (this) {
            if (this.file == null) {
                return null;
            }
            this.sourceCount++;
            return new RelaySource();
        }
    }
}
