package com.bumptech.glide.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public final class ByteBufferUtil {
    private static final AtomicReference<byte[]> BUFFER_REF = new AtomicReference();
    private static final int BUFFER_SIZE = 16384;

    private static class ByteBufferStream extends InputStream {
        private static final int UNSET = -1;
        @NonNull
        private final ByteBuffer byteBuffer;
        private int markPos = -1;

        public boolean markSupported() {
            return true;
        }

        ByteBufferStream(@NonNull ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        public int available() {
            return this.byteBuffer.remaining();
        }

        public int read() {
            if (this.byteBuffer.hasRemaining()) {
                return this.byteBuffer.get();
            }
            return -1;
        }

        public synchronized void mark(int i) {
            this.markPos = this.byteBuffer.position();
        }

        public int read(@NonNull byte[] bArr, int i, int i2) throws IOException {
            if (!this.byteBuffer.hasRemaining()) {
                return -1;
            }
            i2 = Math.min(i2, available());
            this.byteBuffer.get(bArr, i, i2);
            return i2;
        }

        public synchronized void reset() throws IOException {
            if (this.markPos != -1) {
                this.byteBuffer.position(this.markPos);
            } else {
                throw new IOException("Cannot reset to unset mark position");
            }
        }

        public long skip(long j) throws IOException {
            if (!this.byteBuffer.hasRemaining()) {
                return -1;
            }
            j = Math.min(j, (long) available());
            this.byteBuffer.position((int) (((long) this.byteBuffer.position()) + j));
            return j;
        }
    }

    static final class SafeArray {
        final byte[] data;
        final int limit;
        final int offset;

        SafeArray(@NonNull byte[] bArr, int i, int i2) {
            this.data = bArr;
            this.offset = i;
            this.limit = i2;
        }
    }

    private ByteBufferUtil() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x004e A:{SYNTHETIC, Splitter:B:29:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0055 A:{SYNTHETIC, Splitter:B:33:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004e A:{SYNTHETIC, Splitter:B:29:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0055 A:{SYNTHETIC, Splitter:B:33:0x0055} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:14:0x002f */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(6:9|10|(2:12|13)|14|15|16) */
    @android.support.annotation.NonNull
    public static java.nio.ByteBuffer fromFile(@android.support.annotation.NonNull java.io.File r9) throws java.io.IOException {
        /*
        r0 = 0;
        r5 = r9.length();	 Catch:{ all -> 0x004a }
        r1 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        r3 = (r5 > r1 ? 1 : (r5 == r1 ? 0 : -1));
        if (r3 > 0) goto L_0x0042;
    L_0x000c:
        r1 = 0;
        r3 = (r5 > r1 ? 1 : (r5 == r1 ? 0 : -1));
        if (r3 == 0) goto L_0x003a;
    L_0x0012:
        r7 = new java.io.RandomAccessFile;	 Catch:{ all -> 0x004a }
        r1 = "r";
        r7.<init>(r9, r1);	 Catch:{ all -> 0x004a }
        r9 = r7.getChannel();	 Catch:{ all -> 0x0038 }
        r2 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ all -> 0x0033 }
        r3 = 0;
        r1 = r9;
        r0 = r1.map(r2, r3, r5);	 Catch:{ all -> 0x0033 }
        r0 = r0.load();	 Catch:{ all -> 0x0033 }
        if (r9 == 0) goto L_0x002f;
    L_0x002c:
        r9.close();	 Catch:{ IOException -> 0x002f }
    L_0x002f:
        r7.close();	 Catch:{ IOException -> 0x0032 }
    L_0x0032:
        return r0;
    L_0x0033:
        r0 = move-exception;
        r8 = r0;
        r0 = r9;
        r9 = r8;
        goto L_0x004c;
    L_0x0038:
        r9 = move-exception;
        goto L_0x004c;
    L_0x003a:
        r9 = new java.io.IOException;	 Catch:{ all -> 0x004a }
        r1 = "File unsuitable for memory mapping";
        r9.<init>(r1);	 Catch:{ all -> 0x004a }
        throw r9;	 Catch:{ all -> 0x004a }
    L_0x0042:
        r9 = new java.io.IOException;	 Catch:{ all -> 0x004a }
        r1 = "File too large to map into memory";
        r9.<init>(r1);	 Catch:{ all -> 0x004a }
        throw r9;	 Catch:{ all -> 0x004a }
    L_0x004a:
        r9 = move-exception;
        r7 = r0;
    L_0x004c:
        if (r0 == 0) goto L_0x0053;
    L_0x004e:
        r0.close();	 Catch:{ IOException -> 0x0052 }
        goto L_0x0053;
    L_0x0053:
        if (r7 == 0) goto L_0x0058;
    L_0x0055:
        r7.close();	 Catch:{ IOException -> 0x0058 }
    L_0x0058:
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.util.ByteBufferUtil.fromFile(java.io.File):java.nio.ByteBuffer");
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x002f A:{SYNTHETIC, Splitter:B:19:0x002f} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0036 A:{SYNTHETIC, Splitter:B:23:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x002f A:{SYNTHETIC, Splitter:B:19:0x002f} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0036 A:{SYNTHETIC, Splitter:B:23:0x0036} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:10:0x0021 */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Can't wrap try/catch for region: R(6:5|6|(2:8|9)|10|11|27) */
    /* JADX WARNING: Missing block: B:28:?, code skipped:
            return;
     */
    public static void toFile(@android.support.annotation.NonNull java.nio.ByteBuffer r4, @android.support.annotation.NonNull java.io.File r5) throws java.io.IOException {
        /*
        r0 = 0;
        r4.position(r0);
        r1 = 0;
        r2 = new java.io.RandomAccessFile;	 Catch:{ all -> 0x002a }
        r3 = "rw";
        r2.<init>(r5, r3);	 Catch:{ all -> 0x002a }
        r5 = r2.getChannel();	 Catch:{ all -> 0x0027 }
        r5.write(r4);	 Catch:{ all -> 0x0025 }
        r5.force(r0);	 Catch:{ all -> 0x0025 }
        r5.close();	 Catch:{ all -> 0x0025 }
        r2.close();	 Catch:{ all -> 0x0025 }
        if (r5 == 0) goto L_0x0021;
    L_0x001e:
        r5.close();	 Catch:{ IOException -> 0x0021 }
    L_0x0021:
        r2.close();	 Catch:{ IOException -> 0x0024 }
    L_0x0024:
        return;
    L_0x0025:
        r4 = move-exception;
        goto L_0x002d;
    L_0x0027:
        r4 = move-exception;
        r5 = r1;
        goto L_0x002d;
    L_0x002a:
        r4 = move-exception;
        r5 = r1;
        r2 = r5;
    L_0x002d:
        if (r5 == 0) goto L_0x0034;
    L_0x002f:
        r5.close();	 Catch:{ IOException -> 0x0033 }
        goto L_0x0034;
    L_0x0034:
        if (r2 == 0) goto L_0x0039;
    L_0x0036:
        r2.close();	 Catch:{ IOException -> 0x0039 }
    L_0x0039:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.util.ByteBufferUtil.toFile(java.nio.ByteBuffer, java.io.File):void");
    }

    public static void toStream(@NonNull ByteBuffer byteBuffer, @NonNull OutputStream outputStream) throws IOException {
        SafeArray safeArray = getSafeArray(byteBuffer);
        if (safeArray != null) {
            outputStream.write(safeArray.data, safeArray.offset, safeArray.offset + safeArray.limit);
            return;
        }
        byte[] bArr = (byte[]) BUFFER_REF.getAndSet(null);
        if (bArr == null) {
            bArr = new byte[16384];
        }
        while (byteBuffer.remaining() > 0) {
            int min = Math.min(byteBuffer.remaining(), bArr.length);
            byteBuffer.get(bArr, 0, min);
            outputStream.write(bArr, 0, min);
        }
        BUFFER_REF.set(bArr);
    }

    @NonNull
    public static byte[] toBytes(@NonNull ByteBuffer byteBuffer) {
        SafeArray safeArray = getSafeArray(byteBuffer);
        if (safeArray != null && safeArray.offset == 0 && safeArray.limit == safeArray.data.length) {
            return byteBuffer.array();
        }
        byteBuffer = byteBuffer.asReadOnlyBuffer();
        byte[] bArr = new byte[byteBuffer.limit()];
        byteBuffer.position(0);
        byteBuffer.get(bArr);
        return bArr;
    }

    @NonNull
    public static InputStream toStream(@NonNull ByteBuffer byteBuffer) {
        return new ByteBufferStream(byteBuffer);
    }

    @NonNull
    public static ByteBuffer fromStream(@NonNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(16384);
        byte[] bArr = (byte[]) BUFFER_REF.getAndSet(null);
        if (bArr == null) {
            bArr = new byte[16384];
        }
        while (true) {
            int read = inputStream.read(bArr);
            if (read >= 0) {
                byteArrayOutputStream.write(bArr, 0, read);
            } else {
                BUFFER_REF.set(bArr);
                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                return (ByteBuffer) ByteBuffer.allocateDirect(toByteArray.length).put(toByteArray).position(0);
            }
        }
    }

    @Nullable
    private static SafeArray getSafeArray(@NonNull ByteBuffer byteBuffer) {
        return (byteBuffer.isReadOnly() || !byteBuffer.hasArray()) ? null : new SafeArray(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit());
    }
}
