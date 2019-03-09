package com.bumptech.glide.load.resource.bitmap;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RecyclableBufferedInputStream extends FilterInputStream {
    private volatile byte[] buf;
    private final ArrayPool byteArrayPool;
    private int count;
    private int marklimit;
    private int markpos;
    private int pos;

    static class InvalidMarkException extends IOException {
        private static final long serialVersionUID = -4338378848813561757L;

        InvalidMarkException(String str) {
            super(str);
        }
    }

    public boolean markSupported() {
        return true;
    }

    public RecyclableBufferedInputStream(@NonNull InputStream inputStream, @NonNull ArrayPool arrayPool) {
        this(inputStream, arrayPool, 65536);
    }

    @VisibleForTesting
    RecyclableBufferedInputStream(@NonNull InputStream inputStream, @NonNull ArrayPool arrayPool, int i) {
        super(inputStream);
        this.markpos = -1;
        this.byteArrayPool = arrayPool;
        this.buf = (byte[]) arrayPool.get(i, byte[].class);
    }

    public synchronized int available() throws IOException {
        InputStream inputStream;
        inputStream = this.in;
        if (this.buf == null || inputStream == null) {
            throw streamClosed();
        }
        return (this.count - this.pos) + inputStream.available();
    }

    private static IOException streamClosed() throws IOException {
        throw new IOException("BufferedInputStream is closed");
    }

    public synchronized void fixMarkLimit() {
        this.marklimit = this.buf.length;
    }

    public synchronized void release() {
        if (this.buf != null) {
            this.byteArrayPool.put(this.buf);
            this.buf = null;
        }
    }

    public void close() throws IOException {
        if (this.buf != null) {
            this.byteArrayPool.put(this.buf);
            this.buf = null;
        }
        InputStream inputStream = this.in;
        this.in = null;
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private int fillbuf(InputStream inputStream, byte[] bArr) throws IOException {
        int read;
        if (this.markpos == -1 || this.pos - this.markpos >= this.marklimit) {
            read = inputStream.read(bArr);
            if (read > 0) {
                this.markpos = -1;
                this.pos = 0;
                this.count = read;
            }
            return read;
        }
        int i;
        if (this.markpos == 0 && this.marklimit > bArr.length && this.count == bArr.length) {
            int length = bArr.length * 2;
            if (length > this.marklimit) {
                length = this.marklimit;
            }
            byte[] bArr2 = (byte[]) this.byteArrayPool.get(length, byte[].class);
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            this.buf = bArr2;
            this.byteArrayPool.put(bArr);
            bArr = bArr2;
        } else if (this.markpos > 0) {
            System.arraycopy(bArr, this.markpos, bArr, 0, bArr.length - this.markpos);
        }
        this.pos -= this.markpos;
        this.markpos = 0;
        this.count = 0;
        read = inputStream.read(bArr, this.pos, bArr.length - this.pos);
        if (read <= 0) {
            i = this.pos;
        } else {
            i = this.pos + read;
        }
        this.count = i;
        return read;
    }

    public synchronized void mark(int i) {
        this.marklimit = Math.max(this.marklimit, i);
        this.markpos = this.pos;
    }

    public synchronized int read() throws IOException {
        byte[] bArr = this.buf;
        InputStream inputStream = this.in;
        if (bArr == null || inputStream == null) {
            throw streamClosed();
        } else if (this.pos >= this.count && fillbuf(inputStream, bArr) == -1) {
            return -1;
        } else {
            if (bArr != this.buf) {
                bArr = this.buf;
                if (bArr == null) {
                    throw streamClosed();
                }
            }
            if (this.count - this.pos <= 0) {
                return -1;
            }
            int i = this.pos;
            this.pos = i + 1;
            return bArr[i] & 255;
        }
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code skipped:
            return r2;
     */
    /* JADX WARNING: Missing block: B:35:0x0050, code skipped:
            return r4;
     */
    /* JADX WARNING: Missing block: B:42:0x005d, code skipped:
            return r4;
     */
    public synchronized int read(@android.support.annotation.NonNull byte[] r6, int r7, int r8) throws java.io.IOException {
        /*
        r5 = this;
        monitor-enter(r5);
        r0 = r5.buf;	 Catch:{ all -> 0x009e }
        if (r0 == 0) goto L_0x0099;
    L_0x0005:
        if (r8 != 0) goto L_0x000a;
    L_0x0007:
        r6 = 0;
        monitor-exit(r5);
        return r6;
    L_0x000a:
        r1 = r5.in;	 Catch:{ all -> 0x009e }
        if (r1 == 0) goto L_0x0094;
    L_0x000e:
        r2 = r5.pos;	 Catch:{ all -> 0x009e }
        r3 = r5.count;	 Catch:{ all -> 0x009e }
        if (r2 >= r3) goto L_0x003b;
    L_0x0014:
        r2 = r5.count;	 Catch:{ all -> 0x009e }
        r3 = r5.pos;	 Catch:{ all -> 0x009e }
        r2 = r2 - r3;
        if (r2 < r8) goto L_0x001d;
    L_0x001b:
        r2 = r8;
        goto L_0x0022;
    L_0x001d:
        r2 = r5.count;	 Catch:{ all -> 0x009e }
        r3 = r5.pos;	 Catch:{ all -> 0x009e }
        r2 = r2 - r3;
    L_0x0022:
        r3 = r5.pos;	 Catch:{ all -> 0x009e }
        java.lang.System.arraycopy(r0, r3, r6, r7, r2);	 Catch:{ all -> 0x009e }
        r3 = r5.pos;	 Catch:{ all -> 0x009e }
        r3 = r3 + r2;
        r5.pos = r3;	 Catch:{ all -> 0x009e }
        if (r2 == r8) goto L_0x0039;
    L_0x002e:
        r3 = r1.available();	 Catch:{ all -> 0x009e }
        if (r3 != 0) goto L_0x0035;
    L_0x0034:
        goto L_0x0039;
    L_0x0035:
        r7 = r7 + r2;
        r2 = r8 - r2;
        goto L_0x003c;
    L_0x0039:
        monitor-exit(r5);
        return r2;
    L_0x003b:
        r2 = r8;
    L_0x003c:
        r3 = r5.markpos;	 Catch:{ all -> 0x009e }
        r4 = -1;
        if (r3 != r4) goto L_0x0051;
    L_0x0041:
        r3 = r0.length;	 Catch:{ all -> 0x009e }
        if (r2 < r3) goto L_0x0051;
    L_0x0044:
        r3 = r1.read(r6, r7, r2);	 Catch:{ all -> 0x009e }
        if (r3 != r4) goto L_0x0084;
    L_0x004a:
        if (r2 != r8) goto L_0x004d;
    L_0x004c:
        goto L_0x004f;
    L_0x004d:
        r4 = r8 - r2;
    L_0x004f:
        monitor-exit(r5);
        return r4;
    L_0x0051:
        r3 = r5.fillbuf(r1, r0);	 Catch:{ all -> 0x009e }
        if (r3 != r4) goto L_0x005e;
    L_0x0057:
        if (r2 != r8) goto L_0x005a;
    L_0x0059:
        goto L_0x005c;
    L_0x005a:
        r4 = r8 - r2;
    L_0x005c:
        monitor-exit(r5);
        return r4;
    L_0x005e:
        r3 = r5.buf;	 Catch:{ all -> 0x009e }
        if (r0 == r3) goto L_0x006c;
    L_0x0062:
        r0 = r5.buf;	 Catch:{ all -> 0x009e }
        if (r0 == 0) goto L_0x0067;
    L_0x0066:
        goto L_0x006c;
    L_0x0067:
        r6 = streamClosed();	 Catch:{ all -> 0x009e }
        throw r6;	 Catch:{ all -> 0x009e }
    L_0x006c:
        r3 = r5.count;	 Catch:{ all -> 0x009e }
        r4 = r5.pos;	 Catch:{ all -> 0x009e }
        r3 = r3 - r4;
        if (r3 < r2) goto L_0x0075;
    L_0x0073:
        r3 = r2;
        goto L_0x007a;
    L_0x0075:
        r3 = r5.count;	 Catch:{ all -> 0x009e }
        r4 = r5.pos;	 Catch:{ all -> 0x009e }
        r3 = r3 - r4;
    L_0x007a:
        r4 = r5.pos;	 Catch:{ all -> 0x009e }
        java.lang.System.arraycopy(r0, r4, r6, r7, r3);	 Catch:{ all -> 0x009e }
        r4 = r5.pos;	 Catch:{ all -> 0x009e }
        r4 = r4 + r3;
        r5.pos = r4;	 Catch:{ all -> 0x009e }
    L_0x0084:
        r2 = r2 - r3;
        if (r2 != 0) goto L_0x0089;
    L_0x0087:
        monitor-exit(r5);
        return r8;
    L_0x0089:
        r4 = r1.available();	 Catch:{ all -> 0x009e }
        if (r4 != 0) goto L_0x0092;
    L_0x008f:
        r8 = r8 - r2;
        monitor-exit(r5);
        return r8;
    L_0x0092:
        r7 = r7 + r3;
        goto L_0x003c;
    L_0x0094:
        r6 = streamClosed();	 Catch:{ all -> 0x009e }
        throw r6;	 Catch:{ all -> 0x009e }
    L_0x0099:
        r6 = streamClosed();	 Catch:{ all -> 0x009e }
        throw r6;	 Catch:{ all -> 0x009e }
    L_0x009e:
        r6 = move-exception;
        monitor-exit(r5);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.load.resource.bitmap.RecyclableBufferedInputStream.read(byte[], int, int):int");
    }

    public synchronized void reset() throws IOException {
        if (this.buf == null) {
            throw new IOException("Stream is closed");
        } else if (-1 != this.markpos) {
            this.pos = this.markpos;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Mark has been invalidated, pos: ");
            stringBuilder.append(this.pos);
            stringBuilder.append(" markLimit: ");
            stringBuilder.append(this.marklimit);
            throw new InvalidMarkException(stringBuilder.toString());
        }
    }

    public synchronized long skip(long j) throws IOException {
        if (j < 1) {
            return 0;
        }
        byte[] bArr = this.buf;
        if (bArr != null) {
            InputStream inputStream = this.in;
            if (inputStream == null) {
                throw streamClosed();
            } else if (((long) (this.count - this.pos)) >= j) {
                this.pos = (int) (((long) this.pos) + j);
                return j;
            } else {
                long j2 = ((long) this.count) - ((long) this.pos);
                this.pos = this.count;
                if (this.markpos == -1 || j > ((long) this.marklimit)) {
                    return j2 + inputStream.skip(j - j2);
                } else if (fillbuf(inputStream, bArr) == -1) {
                    return j2;
                } else {
                    if (((long) (this.count - this.pos)) >= j - j2) {
                        this.pos = (int) ((((long) this.pos) + j) - j2);
                        return j;
                    }
                    j2 = (j2 + ((long) this.count)) - ((long) this.pos);
                    this.pos = this.count;
                    return j2;
                }
            }
        }
        throw streamClosed();
    }
}
