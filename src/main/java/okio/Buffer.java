package okio;

import android.support.v4.media.session.PlaybackStateCompat;
import com.fasterxml.jackson.core.base.GeneratorBase;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Buffer implements BufferedSource, BufferedSink, Cloneable, ByteChannel {
    private static final byte[] DIGITS = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102};
    static final int REPLACEMENT_CHARACTER = 65533;
    @Nullable
    Segment head;
    long size;

    public static final class UnsafeCursor implements Closeable {
        public Buffer buffer;
        public byte[] data;
        public int end = -1;
        public long offset = -1;
        public boolean readWrite;
        private Segment segment;
        public int start = -1;

        public int next() {
            if (this.offset == this.buffer.size) {
                throw new IllegalStateException();
            } else if (this.offset == -1) {
                return seek(0);
            } else {
                return seek(this.offset + ((long) (this.end - this.start)));
            }
        }

        public int seek(long j) {
            if (j < -1 || j > this.buffer.size) {
                throw new ArrayIndexOutOfBoundsException(String.format("offset=%s > size=%s", new Object[]{Long.valueOf(j), Long.valueOf(this.buffer.size)}));
            } else if (j == -1 || j == this.buffer.size) {
                this.segment = null;
                this.offset = j;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return -1;
            } else {
                long j2 = 0;
                long j3 = this.buffer.size;
                Segment segment = this.buffer.head;
                Segment segment2 = this.buffer.head;
                if (this.segment != null) {
                    long j4 = this.offset - ((long) (this.start - this.segment.pos));
                    if (j4 > j) {
                        segment2 = this.segment;
                        j3 = j4;
                    } else {
                        segment = this.segment;
                        j2 = j4;
                    }
                }
                if (j3 - j > j - j2) {
                    while (j >= ((long) (segment.limit - segment.pos)) + j2) {
                        j2 += (long) (segment.limit - segment.pos);
                        segment = segment.next;
                    }
                } else {
                    j2 = j3;
                    segment = segment2;
                    while (j2 > j) {
                        segment = segment.prev;
                        j2 -= (long) (segment.limit - segment.pos);
                    }
                }
                if (this.readWrite && segment.shared) {
                    Segment unsharedCopy = segment.unsharedCopy();
                    if (this.buffer.head == segment) {
                        this.buffer.head = unsharedCopy;
                    }
                    segment = segment.push(unsharedCopy);
                    segment.prev.pop();
                }
                this.segment = segment;
                this.offset = j;
                this.data = segment.data;
                this.start = segment.pos + ((int) (j - j2));
                this.end = segment.limit;
                return this.end - this.start;
            }
        }

        public long resizeBuffer(long j) {
            if (this.buffer == null) {
                throw new IllegalStateException("not attached to a buffer");
            } else if (this.readWrite) {
                long j2 = this.buffer.size;
                long j3;
                if (j <= j2) {
                    if (j >= 0) {
                        j3 = j2 - j;
                        while (j3 > 0) {
                            Segment segment = this.buffer.head.prev;
                            long j4 = (long) (segment.limit - segment.pos);
                            if (j4 > j3) {
                                segment.limit = (int) (((long) segment.limit) - j3);
                                break;
                            }
                            this.buffer.head = segment.pop();
                            SegmentPool.recycle(segment);
                            j3 -= j4;
                        }
                        this.segment = null;
                        this.offset = j;
                        this.data = null;
                        this.start = -1;
                        this.end = -1;
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("newSize < 0: ");
                        stringBuilder.append(j);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    }
                } else if (j > j2) {
                    j3 = j - j2;
                    Object obj = 1;
                    while (j3 > 0) {
                        Segment writableSegment = this.buffer.writableSegment(1);
                        int min = (int) Math.min(j3, (long) (8192 - writableSegment.limit));
                        writableSegment.limit += min;
                        j3 -= (long) min;
                        if (obj != null) {
                            this.segment = writableSegment;
                            this.offset = j2;
                            this.data = writableSegment.data;
                            this.start = writableSegment.limit - min;
                            this.end = writableSegment.limit;
                            obj = null;
                        }
                    }
                }
                this.buffer.size = j;
                return j2;
            } else {
                throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers");
            }
        }

        public long expandBuffer(int i) {
            StringBuilder stringBuilder;
            if (i <= 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("minByteCount <= 0: ");
                stringBuilder.append(i);
                throw new IllegalArgumentException(stringBuilder.toString());
            } else if (i > 8192) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("minByteCount > Segment.SIZE: ");
                stringBuilder.append(i);
                throw new IllegalArgumentException(stringBuilder.toString());
            } else if (this.buffer == null) {
                throw new IllegalStateException("not attached to a buffer");
            } else if (this.readWrite) {
                long j = this.buffer.size;
                Segment writableSegment = this.buffer.writableSegment(i);
                int i2 = 8192 - writableSegment.limit;
                writableSegment.limit = 8192;
                long j2 = (long) i2;
                this.buffer.size = j + j2;
                this.segment = writableSegment;
                this.offset = j;
                this.data = writableSegment.data;
                this.start = 8192 - i2;
                this.end = 8192;
                return j2;
            } else {
                throw new IllegalStateException("expandBuffer() only permitted for read/write buffers");
            }
        }

        public void close() {
            if (this.buffer != null) {
                this.buffer = null;
                this.segment = null;
                this.offset = -1;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return;
            }
            throw new IllegalStateException("not attached to a buffer");
        }
    }

    public Buffer buffer() {
        return this;
    }

    public void close() {
    }

    public BufferedSink emit() {
        return this;
    }

    public Buffer emitCompleteSegments() {
        return this;
    }

    public void flush() {
    }

    public boolean isOpen() {
        return true;
    }

    public long size() {
        return this.size;
    }

    public OutputStream outputStream() {
        return new OutputStream() {
            public void close() {
            }

            public void flush() {
            }

            public void write(int i) {
                Buffer.this.writeByte((byte) i);
            }

            public void write(byte[] bArr, int i, int i2) {
                Buffer.this.write(bArr, i, i2);
            }

            public String toString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Buffer.this);
                stringBuilder.append(".outputStream()");
                return stringBuilder.toString();
            }
        };
    }

    public boolean exhausted() {
        return this.size == 0;
    }

    public void require(long j) throws EOFException {
        if (this.size < j) {
            throw new EOFException();
        }
    }

    public boolean request(long j) {
        return this.size >= j;
    }

    public InputStream inputStream() {
        return new InputStream() {
            public void close() {
            }

            public int read() {
                return Buffer.this.size > 0 ? Buffer.this.readByte() & 255 : -1;
            }

            public int read(byte[] bArr, int i, int i2) {
                return Buffer.this.read(bArr, i, i2);
            }

            public int available() {
                return (int) Math.min(Buffer.this.size, 2147483647L);
            }

            public String toString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Buffer.this);
                stringBuilder.append(".inputStream()");
                return stringBuilder.toString();
            }
        };
    }

    public Buffer copyTo(OutputStream outputStream) throws IOException {
        return copyTo(outputStream, 0, this.size);
    }

    public Buffer copyTo(OutputStream outputStream, long j, long j2) throws IOException {
        if (outputStream != null) {
            Util.checkOffsetAndCount(this.size, j, j2);
            if (j2 == 0) {
                return this;
            }
            Segment segment = this.head;
            while (j >= ((long) (segment.limit - segment.pos))) {
                j -= (long) (segment.limit - segment.pos);
                segment = segment.next;
            }
            while (j2 > 0) {
                int i = (int) (((long) segment.pos) + j);
                int min = (int) Math.min((long) (segment.limit - i), j2);
                outputStream.write(segment.data, i, min);
                j2 -= (long) min;
                segment = segment.next;
                j = 0;
            }
            return this;
        }
        throw new IllegalArgumentException("out == null");
    }

    public Buffer copyTo(Buffer buffer, long j, long j2) {
        if (buffer != null) {
            Util.checkOffsetAndCount(this.size, j, j2);
            if (j2 == 0) {
                return this;
            }
            buffer.size += j2;
            Segment segment = this.head;
            while (j >= ((long) (segment.limit - segment.pos))) {
                j -= (long) (segment.limit - segment.pos);
                segment = segment.next;
            }
            while (j2 > 0) {
                Segment sharedCopy = segment.sharedCopy();
                sharedCopy.pos = (int) (((long) sharedCopy.pos) + j);
                sharedCopy.limit = Math.min(sharedCopy.pos + ((int) j2), sharedCopy.limit);
                if (buffer.head == null) {
                    sharedCopy.prev = sharedCopy;
                    sharedCopy.next = sharedCopy;
                    buffer.head = sharedCopy;
                } else {
                    buffer.head.prev.push(sharedCopy);
                }
                j2 -= (long) (sharedCopy.limit - sharedCopy.pos);
                segment = segment.next;
                j = 0;
            }
            return this;
        }
        throw new IllegalArgumentException("out == null");
    }

    public Buffer writeTo(OutputStream outputStream) throws IOException {
        return writeTo(outputStream, this.size);
    }

    public Buffer writeTo(OutputStream outputStream, long j) throws IOException {
        if (outputStream != null) {
            Util.checkOffsetAndCount(this.size, 0, j);
            Segment segment = this.head;
            while (j > 0) {
                int min = (int) Math.min(j, (long) (segment.limit - segment.pos));
                outputStream.write(segment.data, segment.pos, min);
                segment.pos += min;
                long j2 = (long) min;
                this.size -= j2;
                j -= j2;
                if (segment.pos == segment.limit) {
                    Segment pop = segment.pop();
                    this.head = pop;
                    SegmentPool.recycle(segment);
                    segment = pop;
                }
            }
            return this;
        }
        throw new IllegalArgumentException("out == null");
    }

    public Buffer readFrom(InputStream inputStream) throws IOException {
        readFrom(inputStream, Long.MAX_VALUE, true);
        return this;
    }

    public Buffer readFrom(InputStream inputStream, long j) throws IOException {
        if (j >= 0) {
            readFrom(inputStream, j, false);
            return this;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("byteCount < 0: ");
        stringBuilder.append(j);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    private void readFrom(InputStream inputStream, long j, boolean z) throws IOException {
        if (inputStream != null) {
            while (true) {
                if (j > 0 || z) {
                    Segment writableSegment = writableSegment(1);
                    int read = inputStream.read(writableSegment.data, writableSegment.limit, (int) Math.min(j, (long) (8192 - writableSegment.limit)));
                    if (read != -1) {
                        writableSegment.limit += read;
                        long j2 = (long) read;
                        this.size += j2;
                        j -= j2;
                    } else if (!z) {
                        throw new EOFException();
                    } else {
                        return;
                    }
                }
                return;
            }
        }
        throw new IllegalArgumentException("in == null");
    }

    public long completeSegmentByteCount() {
        long j = this.size;
        if (j == 0) {
            return 0;
        }
        Segment segment = this.head.prev;
        if (segment.limit < 8192 && segment.owner) {
            j -= (long) (segment.limit - segment.pos);
        }
        return j;
    }

    public byte readByte() {
        if (this.size != 0) {
            Segment segment = this.head;
            int i = segment.pos;
            int i2 = segment.limit;
            int i3 = i + 1;
            byte b = segment.data[i];
            this.size--;
            if (i3 == i2) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = i3;
            }
            return b;
        }
        throw new IllegalStateException("size == 0");
    }

    public byte getByte(long j) {
        Util.checkOffsetAndCount(this.size, j, 1);
        Segment segment;
        if (this.size - j > j) {
            segment = this.head;
            while (true) {
                long j2 = (long) (segment.limit - segment.pos);
                if (j < j2) {
                    return segment.data[segment.pos + ((int) j)];
                }
                j -= j2;
                segment = segment.next;
            }
        } else {
            j -= this.size;
            segment = this.head;
            do {
                segment = segment.prev;
                j += (long) (segment.limit - segment.pos);
            } while (j < 0);
            return segment.data[segment.pos + ((int) j)];
        }
    }

    public short readShort() {
        if (this.size >= 2) {
            Segment segment = this.head;
            int i = segment.pos;
            int i2 = segment.limit;
            if (i2 - i < 2) {
                return (short) (((readByte() & 255) << 8) | (readByte() & 255));
            }
            byte[] bArr = segment.data;
            int i3 = i + 1;
            int i4 = i3 + 1;
            i = ((bArr[i] & 255) << 8) | (bArr[i3] & 255);
            this.size -= 2;
            if (i4 == i2) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = i4;
            }
            return (short) i;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("size < 2: ");
        stringBuilder.append(this.size);
        throw new IllegalStateException(stringBuilder.toString());
    }

    public int readInt() {
        if (this.size >= 4) {
            Segment segment = this.head;
            int i = segment.pos;
            int i2 = segment.limit;
            if (i2 - i < 4) {
                return ((((readByte() & 255) << 24) | ((readByte() & 255) << 16)) | ((readByte() & 255) << 8)) | (readByte() & 255);
            }
            byte[] bArr = segment.data;
            int i3 = i + 1;
            int i4 = i3 + 1;
            i = ((bArr[i] & 255) << 24) | ((bArr[i3] & 255) << 16);
            i3 = i4 + 1;
            i |= (bArr[i4] & 255) << 8;
            i4 = i3 + 1;
            i |= bArr[i3] & 255;
            this.size -= 4;
            if (i4 == i2) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = i4;
            }
            return i;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("size < 4: ");
        stringBuilder.append(this.size);
        throw new IllegalStateException(stringBuilder.toString());
    }

    public long readLong() {
        if (this.size >= 8) {
            Segment segment = this.head;
            int i = segment.pos;
            int i2 = segment.limit;
            if (i2 - i < 8) {
                return ((((long) readInt()) & 4294967295L) << 32) | (4294967295L & ((long) readInt()));
            }
            byte[] bArr = segment.data;
            int i3 = i + 1;
            i = i3 + 1;
            i3 = i + 1;
            i = i3 + 1;
            int i4 = i + 1;
            i = i4 + 1;
            long j = ((((((((long) bArr[i]) & 255) << 56) | ((((long) bArr[i3]) & 255) << 48)) | ((((long) bArr[i]) & 255) << 40)) | ((((long) bArr[i3]) & 255) << 32)) | ((((long) bArr[i]) & 255) << 24)) | ((((long) bArr[i4]) & 255) << 16);
            i4 = i + 1;
            long j2 = ((((long) bArr[i]) & 255) << 8) | j;
            i = i4 + 1;
            long j3 = (((long) bArr[i4]) & 255) | j2;
            this.size -= 8;
            if (i == i2) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = i;
            }
            return j3;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("size < 8: ");
        stringBuilder.append(this.size);
        throw new IllegalStateException(stringBuilder.toString());
    }

    public short readShortLe() {
        return Util.reverseBytesShort(readShort());
    }

    public int readIntLe() {
        return Util.reverseBytesInt(readInt());
    }

    public long readLongLe() {
        return Util.reverseBytesLong(readLong());
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ae A:{SYNTHETIC, EDGE_INSN: B:41:0x00ae->B:36:0x00ae ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00aa  */
    public long readDecimalLong() {
        /*
        r17 = this;
        r0 = r17;
        r1 = r0.size;
        r3 = 0;
        r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        if (r5 == 0) goto L_0x00b9;
    L_0x000a:
        r5 = -7;
        r7 = 0;
        r8 = r5;
        r5 = 0;
        r6 = 0;
    L_0x0010:
        r10 = r0.head;
        r11 = r10.data;
        r12 = r10.pos;
        r13 = r10.limit;
    L_0x0018:
        if (r12 >= r13) goto L_0x009a;
    L_0x001a:
        r15 = r11[r12];
        r14 = 48;
        if (r15 < r14) goto L_0x006c;
    L_0x0020:
        r1 = 57;
        if (r15 > r1) goto L_0x006c;
    L_0x0024:
        r14 = r14 - r15;
        r1 = -922337203685477580; // 0xf333333333333334 float:4.1723254E-8 double:-8.390303882365713E246;
        r16 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1));
        if (r16 < 0) goto L_0x003f;
    L_0x002e:
        r16 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1));
        if (r16 != 0) goto L_0x0038;
    L_0x0032:
        r1 = (long) r14;
        r16 = (r1 > r8 ? 1 : (r1 == r8 ? 0 : -1));
        if (r16 >= 0) goto L_0x0038;
    L_0x0037:
        goto L_0x003f;
    L_0x0038:
        r1 = 10;
        r3 = r3 * r1;
        r1 = (long) r14;
        r3 = r3 + r1;
        goto L_0x0076;
    L_0x003f:
        r1 = new okio.Buffer;
        r1.<init>();
        r1 = r1.writeDecimalLong(r3);
        r1 = r1.writeByte(r15);
        if (r5 != 0) goto L_0x0051;
    L_0x004e:
        r1.readByte();
    L_0x0051:
        r2 = new java.lang.NumberFormatException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Number too large: ";
        r3.append(r4);
        r1 = r1.readUtf8();
        r3.append(r1);
        r1 = r3.toString();
        r2.<init>(r1);
        throw r2;
    L_0x006c:
        r1 = 45;
        if (r15 != r1) goto L_0x007b;
    L_0x0070:
        if (r7 != 0) goto L_0x007b;
    L_0x0072:
        r1 = 1;
        r8 = r8 - r1;
        r5 = 1;
    L_0x0076:
        r12 = r12 + 1;
        r7 = r7 + 1;
        goto L_0x0018;
    L_0x007b:
        if (r7 == 0) goto L_0x007f;
    L_0x007d:
        r6 = 1;
        goto L_0x009a;
    L_0x007f:
        r1 = new java.lang.NumberFormatException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Expected leading [0-9] or '-' character but was 0x";
        r2.append(r3);
        r3 = java.lang.Integer.toHexString(r15);
        r2.append(r3);
        r2 = r2.toString();
        r1.<init>(r2);
        throw r1;
    L_0x009a:
        if (r12 != r13) goto L_0x00a6;
    L_0x009c:
        r1 = r10.pop();
        r0.head = r1;
        okio.SegmentPool.recycle(r10);
        goto L_0x00a8;
    L_0x00a6:
        r10.pos = r12;
    L_0x00a8:
        if (r6 != 0) goto L_0x00ae;
    L_0x00aa:
        r1 = r0.head;
        if (r1 != 0) goto L_0x0010;
    L_0x00ae:
        r1 = r0.size;
        r6 = (long) r7;
        r1 = r1 - r6;
        r0.size = r1;
        if (r5 == 0) goto L_0x00b7;
    L_0x00b6:
        goto L_0x00b8;
    L_0x00b7:
        r3 = -r3;
    L_0x00b8:
        return r3;
    L_0x00b9:
        r1 = new java.lang.IllegalStateException;
        r2 = "size == 0";
        r1.<init>(r2);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.readDecimalLong():long");
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a6 A:{SYNTHETIC, EDGE_INSN: B:40:0x00a6->B:35:0x00a6 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a2  */
    public long readHexadecimalUnsignedLong() {
        /*
        r15 = this;
        r0 = r15.size;
        r2 = 0;
        r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r4 == 0) goto L_0x00ad;
    L_0x0008:
        r0 = 0;
        r4 = r2;
        r1 = 0;
    L_0x000b:
        r6 = r15.head;
        r7 = r6.data;
        r8 = r6.pos;
        r9 = r6.limit;
    L_0x0013:
        if (r8 >= r9) goto L_0x0092;
    L_0x0015:
        r10 = r7[r8];
        r11 = 48;
        if (r10 < r11) goto L_0x0022;
    L_0x001b:
        r11 = 57;
        if (r10 > r11) goto L_0x0022;
    L_0x001f:
        r11 = r10 + -48;
        goto L_0x003b;
    L_0x0022:
        r11 = 97;
        if (r10 < r11) goto L_0x002f;
    L_0x0026:
        r11 = 102; // 0x66 float:1.43E-43 double:5.04E-322;
        if (r10 > r11) goto L_0x002f;
    L_0x002a:
        r11 = r10 + -97;
        r11 = r11 + 10;
        goto L_0x003b;
    L_0x002f:
        r11 = 65;
        if (r10 < r11) goto L_0x0073;
    L_0x0033:
        r11 = 70;
        if (r10 > r11) goto L_0x0073;
    L_0x0037:
        r11 = r10 + -65;
        r11 = r11 + 10;
    L_0x003b:
        r12 = -1152921504606846976; // 0xf000000000000000 float:0.0 double:-3.105036184601418E231;
        r12 = r12 & r4;
        r14 = (r12 > r2 ? 1 : (r12 == r2 ? 0 : -1));
        if (r14 != 0) goto L_0x004b;
    L_0x0042:
        r10 = 4;
        r4 = r4 << r10;
        r10 = (long) r11;
        r4 = r4 | r10;
        r8 = r8 + 1;
        r0 = r0 + 1;
        goto L_0x0013;
    L_0x004b:
        r0 = new okio.Buffer;
        r0.<init>();
        r0 = r0.writeHexadecimalUnsignedLong(r4);
        r0 = r0.writeByte(r10);
        r1 = new java.lang.NumberFormatException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Number too large: ";
        r2.append(r3);
        r0 = r0.readUtf8();
        r2.append(r0);
        r0 = r2.toString();
        r1.<init>(r0);
        throw r1;
    L_0x0073:
        if (r0 == 0) goto L_0x0077;
    L_0x0075:
        r1 = 1;
        goto L_0x0092;
    L_0x0077:
        r0 = new java.lang.NumberFormatException;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "Expected leading [0-9a-fA-F] character but was 0x";
        r1.append(r2);
        r2 = java.lang.Integer.toHexString(r10);
        r1.append(r2);
        r1 = r1.toString();
        r0.<init>(r1);
        throw r0;
    L_0x0092:
        if (r8 != r9) goto L_0x009e;
    L_0x0094:
        r7 = r6.pop();
        r15.head = r7;
        okio.SegmentPool.recycle(r6);
        goto L_0x00a0;
    L_0x009e:
        r6.pos = r8;
    L_0x00a0:
        if (r1 != 0) goto L_0x00a6;
    L_0x00a2:
        r6 = r15.head;
        if (r6 != 0) goto L_0x000b;
    L_0x00a6:
        r1 = r15.size;
        r6 = (long) r0;
        r1 = r1 - r6;
        r15.size = r1;
        return r4;
    L_0x00ad:
        r0 = new java.lang.IllegalStateException;
        r1 = "size == 0";
        r0.<init>(r1);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.readHexadecimalUnsignedLong():long");
    }

    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    public ByteString readByteString(long j) throws EOFException {
        return new ByteString(readByteArray(j));
    }

    public int select(Options options) {
        Segment segment = this.head;
        if (segment == null) {
            return options.indexOf(ByteString.EMPTY);
        }
        ByteString[] byteStringArr = options.byteStrings;
        int length = byteStringArr.length;
        for (int i = 0; i < length; i++) {
            ByteString byteString = byteStringArr[i];
            if (this.size >= ((long) byteString.size())) {
                if (rangeEquals(segment, segment.pos, byteString, 0, byteString.size())) {
                    try {
                        skip((long) byteString.size());
                        return i;
                    } catch (EOFException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        return -1;
    }

    /* Access modifiers changed, original: 0000 */
    public int selectPrefix(Options options) {
        Segment segment = this.head;
        ByteString[] byteStringArr = options.byteStrings;
        int length = byteStringArr.length;
        int i = 0;
        while (i < length) {
            ByteString byteString = byteStringArr[i];
            int min = (int) Math.min(this.size, (long) byteString.size());
            if (min != 0) {
                if (!rangeEquals(segment, segment.pos, byteString, 0, min)) {
                    i++;
                }
            }
            return i;
        }
        return -1;
    }

    public void readFully(Buffer buffer, long j) throws EOFException {
        if (this.size >= j) {
            buffer.write(this, j);
        } else {
            buffer.write(this, this.size);
            throw new EOFException();
        }
    }

    public long readAll(Sink sink) throws IOException {
        long j = this.size;
        if (j > 0) {
            sink.write(this, j);
        }
        return j;
    }

    public String readUtf8() {
        try {
            return readString(this.size, Util.UTF_8);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    public String readUtf8(long j) throws EOFException {
        return readString(j, Util.UTF_8);
    }

    public String readString(Charset charset) {
        try {
            return readString(this.size, charset);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    public String readString(long j, Charset charset) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0, j);
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        } else if (j > 2147483647L) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("byteCount > Integer.MAX_VALUE: ");
            stringBuilder.append(j);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (j == 0) {
            return "";
        } else {
            Segment segment = this.head;
            if (((long) segment.pos) + j > ((long) segment.limit)) {
                return new String(readByteArray(j), charset);
            }
            String str = new String(segment.data, segment.pos, (int) j, charset);
            segment.pos = (int) (((long) segment.pos) + j);
            this.size -= j;
            if (segment.pos == segment.limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            }
            return str;
        }
    }

    @Nullable
    public String readUtf8Line() throws EOFException {
        long indexOf = indexOf((byte) 10);
        if (indexOf != -1) {
            return readUtf8Line(indexOf);
        }
        return this.size != 0 ? readUtf8(this.size) : null;
    }

    public String readUtf8LineStrict() throws EOFException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    public String readUtf8LineStrict(long j) throws EOFException {
        StringBuilder stringBuilder;
        if (j >= 0) {
            long j2 = Long.MAX_VALUE;
            if (j != Long.MAX_VALUE) {
                j2 = j + 1;
            }
            long indexOf = indexOf((byte) 10, 0, j2);
            if (indexOf != -1) {
                return readUtf8Line(indexOf);
            }
            if (j2 < size() && getByte(j2 - 1) == (byte) 13 && getByte(j2) == (byte) 10) {
                return readUtf8Line(j2);
            }
            Buffer buffer = new Buffer();
            copyTo(buffer, 0, Math.min(32, size()));
            stringBuilder = new StringBuilder();
            stringBuilder.append("\\n not found: limit=");
            stringBuilder.append(Math.min(size(), j));
            stringBuilder.append(" content=");
            stringBuilder.append(buffer.readByteString().hex());
            stringBuilder.append(8230);
            throw new EOFException(stringBuilder.toString());
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("limit < 0: ");
        stringBuilder.append(j);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    /* Access modifiers changed, original: 0000 */
    public String readUtf8Line(long j) throws EOFException {
        String readUtf8;
        if (j > 0) {
            long j2 = j - 1;
            if (getByte(j2) == (byte) 13) {
                readUtf8 = readUtf8(j2);
                skip(2);
                return readUtf8;
            }
        }
        readUtf8 = readUtf8(j);
        skip(1);
        return readUtf8;
    }

    public int readUtf8CodePoint() throws EOFException {
        if (this.size != 0) {
            int i;
            int i2;
            byte b = getByte(0);
            int i3 = 1;
            int i4;
            if ((b & 128) == 0) {
                i = b & 127;
                i2 = 1;
                i4 = 0;
            } else if ((b & 224) == 192) {
                i = b & 31;
                i2 = 2;
                i4 = 128;
            } else if ((b & 240) == 224) {
                i = b & 15;
                i2 = 3;
                i4 = 2048;
            } else if ((b & 248) == 240) {
                i = b & 7;
                i2 = 4;
                i4 = 65536;
            } else {
                skip(1);
                return REPLACEMENT_CHARACTER;
            }
            long j = (long) i2;
            if (this.size >= j) {
                while (i3 < i2) {
                    long j2 = (long) i3;
                    b = getByte(j2);
                    if ((b & 192) == 128) {
                        i = (i << 6) | (b & 63);
                        i3++;
                    } else {
                        skip(j2);
                        return REPLACEMENT_CHARACTER;
                    }
                }
                skip(j);
                if (i > 1114111) {
                    return REPLACEMENT_CHARACTER;
                }
                if ((i < GeneratorBase.SURR1_FIRST || i > GeneratorBase.SURR2_LAST) && i >= i4) {
                    return i;
                }
                return REPLACEMENT_CHARACTER;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("size < ");
            stringBuilder.append(i2);
            stringBuilder.append(": ");
            stringBuilder.append(this.size);
            stringBuilder.append(" (to read code point prefixed 0x");
            stringBuilder.append(Integer.toHexString(b));
            stringBuilder.append(")");
            throw new EOFException(stringBuilder.toString());
        }
        throw new EOFException();
    }

    public byte[] readByteArray() {
        try {
            return readByteArray(this.size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    public byte[] readByteArray(long j) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0, j);
        if (j <= 2147483647L) {
            byte[] bArr = new byte[((int) j)];
            readFully(bArr);
            return bArr;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("byteCount > Integer.MAX_VALUE: ");
        stringBuilder.append(j);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int read(byte[] bArr) {
        return read(bArr, 0, bArr.length);
    }

    public void readFully(byte[] bArr) throws EOFException {
        int i = 0;
        while (i < bArr.length) {
            int read = read(bArr, i, bArr.length - i);
            if (read != -1) {
                i += read;
            } else {
                throw new EOFException();
            }
        }
    }

    public int read(byte[] bArr, int i, int i2) {
        Util.checkOffsetAndCount((long) bArr.length, (long) i, (long) i2);
        Segment segment = this.head;
        if (segment == null) {
            return -1;
        }
        i2 = Math.min(i2, segment.limit - segment.pos);
        System.arraycopy(segment.data, segment.pos, bArr, i, i2);
        segment.pos += i2;
        this.size -= (long) i2;
        if (segment.pos == segment.limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return i2;
    }

    public int read(ByteBuffer byteBuffer) throws IOException {
        Segment segment = this.head;
        if (segment == null) {
            return -1;
        }
        int min = Math.min(byteBuffer.remaining(), segment.limit - segment.pos);
        byteBuffer.put(segment.data, segment.pos, min);
        segment.pos += min;
        this.size -= (long) min;
        if (segment.pos == segment.limit) {
            this.head = segment.pop();
            SegmentPool.recycle(segment);
        }
        return min;
    }

    public void clear() {
        try {
            skip(this.size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    public void skip(long j) throws EOFException {
        while (j > 0) {
            if (this.head != null) {
                int min = (int) Math.min(j, (long) (this.head.limit - this.head.pos));
                long j2 = (long) min;
                this.size -= j2;
                j -= j2;
                Segment segment = this.head;
                segment.pos += min;
                if (this.head.pos == this.head.limit) {
                    Segment segment2 = this.head;
                    this.head = segment2.pop();
                    SegmentPool.recycle(segment2);
                }
            } else {
                throw new EOFException();
            }
        }
    }

    public Buffer write(ByteString byteString) {
        if (byteString != null) {
            byteString.write(this);
            return this;
        }
        throw new IllegalArgumentException("byteString == null");
    }

    public Buffer writeUtf8(String str) {
        return writeUtf8(str, 0, str.length());
    }

    public Buffer writeUtf8(String str, int i, int i2) {
        StringBuilder stringBuilder;
        if (str == null) {
            throw new IllegalArgumentException("string == null");
        } else if (i < 0) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("beginIndex < 0: ");
            stringBuilder2.append(i);
            throw new IllegalArgumentException(stringBuilder2.toString());
        } else if (i2 < i) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("endIndex < beginIndex: ");
            stringBuilder.append(i2);
            stringBuilder.append(" < ");
            stringBuilder.append(i);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (i2 <= str.length()) {
            while (i < i2) {
                char charAt = str.charAt(i);
                int i3;
                int min;
                int i4;
                if (charAt < 128) {
                    Segment writableSegment = writableSegment(1);
                    byte[] bArr = writableSegment.data;
                    i3 = writableSegment.limit - i;
                    min = Math.min(i2, 8192 - i3);
                    int i5 = i + 1;
                    bArr[i + i3] = (byte) charAt;
                    while (i5 < min) {
                        char charAt2 = str.charAt(i5);
                        if (charAt2 >= 128) {
                            break;
                        }
                        i4 = i5 + 1;
                        bArr[i5 + i3] = (byte) charAt2;
                        i5 = i4;
                    }
                    i3 = (i3 + i5) - writableSegment.limit;
                    writableSegment.limit += i3;
                    this.size += (long) i3;
                    i = i5;
                } else if (charAt < 2048) {
                    writeByte((charAt >> 6) | 192);
                    writeByte((charAt & 63) | 128);
                    i++;
                } else if (charAt < 55296 || charAt > 57343) {
                    writeByte((charAt >> 12) | 224);
                    writeByte(((charAt >> 6) & 63) | 128);
                    writeByte((charAt & 63) | 128);
                    i++;
                } else {
                    i3 = i + 1;
                    if (i3 < i2) {
                        min = str.charAt(i3);
                    } else {
                        min = 0;
                    }
                    if (charAt > 56319 || min < GeneratorBase.SURR2_FIRST || min > GeneratorBase.SURR2_LAST) {
                        writeByte(63);
                        i = i3;
                    } else {
                        i4 = (((charAt & -55297) << 10) | (-56321 & min)) + 65536;
                        writeByte((i4 >> 18) | 240);
                        writeByte(((i4 >> 12) & 63) | 128);
                        writeByte(((i4 >> 6) & 63) | 128);
                        writeByte((i4 & 63) | 128);
                        i += 2;
                    }
                }
            }
            return this;
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("endIndex > string.length: ");
            stringBuilder.append(i2);
            stringBuilder.append(" > ");
            stringBuilder.append(str.length());
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public Buffer writeUtf8CodePoint(int i) {
        if (i < 128) {
            writeByte(i);
        } else if (i < 2048) {
            writeByte((i >> 6) | 192);
            writeByte((i & 63) | 128);
        } else if (i < 65536) {
            if (i < GeneratorBase.SURR1_FIRST || i > GeneratorBase.SURR2_LAST) {
                writeByte((i >> 12) | 224);
                writeByte(((i >> 6) & 63) | 128);
                writeByte((i & 63) | 128);
            } else {
                writeByte(63);
            }
        } else if (i <= 1114111) {
            writeByte((i >> 18) | 240);
            writeByte(((i >> 12) & 63) | 128);
            writeByte(((i >> 6) & 63) | 128);
            writeByte((i & 63) | 128);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unexpected code point: ");
            stringBuilder.append(Integer.toHexString(i));
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        return this;
    }

    public Buffer writeString(String str, Charset charset) {
        return writeString(str, 0, str.length(), charset);
    }

    public Buffer writeString(String str, int i, int i2, Charset charset) {
        StringBuilder stringBuilder;
        if (str == null) {
            throw new IllegalArgumentException("string == null");
        } else if (i < 0) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("beginIndex < 0: ");
            stringBuilder2.append(i);
            throw new IllegalAccessError(stringBuilder2.toString());
        } else if (i2 < i) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("endIndex < beginIndex: ");
            stringBuilder.append(i2);
            stringBuilder.append(" < ");
            stringBuilder.append(i);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (i2 > str.length()) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("endIndex > string.length: ");
            stringBuilder.append(i2);
            stringBuilder.append(" > ");
            stringBuilder.append(str.length());
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        } else if (charset.equals(Util.UTF_8)) {
            return writeUtf8(str, i, i2);
        } else {
            byte[] bytes = str.substring(i, i2).getBytes(charset);
            return write(bytes, 0, bytes.length);
        }
    }

    public Buffer write(byte[] bArr) {
        if (bArr != null) {
            return write(bArr, 0, bArr.length);
        }
        throw new IllegalArgumentException("source == null");
    }

    public Buffer write(byte[] bArr, int i, int i2) {
        if (bArr != null) {
            long j = (long) i2;
            Util.checkOffsetAndCount((long) bArr.length, (long) i, j);
            i2 += i;
            while (i < i2) {
                Segment writableSegment = writableSegment(1);
                int min = Math.min(i2 - i, 8192 - writableSegment.limit);
                System.arraycopy(bArr, i, writableSegment.data, writableSegment.limit, min);
                i += min;
                writableSegment.limit += min;
            }
            this.size += j;
            return this;
        }
        throw new IllegalArgumentException("source == null");
    }

    public int write(ByteBuffer byteBuffer) throws IOException {
        if (byteBuffer != null) {
            int remaining = byteBuffer.remaining();
            int i = remaining;
            while (i > 0) {
                Segment writableSegment = writableSegment(1);
                int min = Math.min(i, 8192 - writableSegment.limit);
                byteBuffer.get(writableSegment.data, writableSegment.limit, min);
                i -= min;
                writableSegment.limit += min;
            }
            this.size += (long) remaining;
            return remaining;
        }
        throw new IllegalArgumentException("source == null");
    }

    public long writeAll(Source source) throws IOException {
        if (source != null) {
            long j = 0;
            while (true) {
                long read = source.read(this, PlaybackStateCompat.ACTION_PLAY_FROM_URI);
                if (read == -1) {
                    return j;
                }
                j += read;
            }
        } else {
            throw new IllegalArgumentException("source == null");
        }
    }

    public BufferedSink write(Source source, long j) throws IOException {
        while (j > 0) {
            long read = source.read(this, j);
            if (read != -1) {
                j -= read;
            } else {
                throw new EOFException();
            }
        }
        return this;
    }

    public Buffer writeByte(int i) {
        Segment writableSegment = writableSegment(1);
        byte[] bArr = writableSegment.data;
        int i2 = writableSegment.limit;
        writableSegment.limit = i2 + 1;
        bArr[i2] = (byte) i;
        this.size++;
        return this;
    }

    public Buffer writeShort(int i) {
        Segment writableSegment = writableSegment(2);
        byte[] bArr = writableSegment.data;
        int i2 = writableSegment.limit;
        int i3 = i2 + 1;
        bArr[i2] = (byte) ((i >>> 8) & 255);
        i2 = i3 + 1;
        bArr[i3] = (byte) (i & 255);
        writableSegment.limit = i2;
        this.size += 2;
        return this;
    }

    public Buffer writeShortLe(int i) {
        return writeShort(Util.reverseBytesShort((short) i));
    }

    public Buffer writeInt(int i) {
        Segment writableSegment = writableSegment(4);
        byte[] bArr = writableSegment.data;
        int i2 = writableSegment.limit;
        int i3 = i2 + 1;
        bArr[i2] = (byte) ((i >>> 24) & 255);
        i2 = i3 + 1;
        bArr[i3] = (byte) ((i >>> 16) & 255);
        i3 = i2 + 1;
        bArr[i2] = (byte) ((i >>> 8) & 255);
        i2 = i3 + 1;
        bArr[i3] = (byte) (i & 255);
        writableSegment.limit = i2;
        this.size += 4;
        return this;
    }

    public Buffer writeIntLe(int i) {
        return writeInt(Util.reverseBytesInt(i));
    }

    public Buffer writeLong(long j) {
        Segment writableSegment = writableSegment(8);
        byte[] bArr = writableSegment.data;
        int i = writableSegment.limit;
        int i2 = i + 1;
        bArr[i] = (byte) ((int) ((j >>> 56) & 255));
        i = i2 + 1;
        bArr[i2] = (byte) ((int) ((j >>> 48) & 255));
        i2 = i + 1;
        bArr[i] = (byte) ((int) ((j >>> 40) & 255));
        i = i2 + 1;
        bArr[i2] = (byte) ((int) ((j >>> 32) & 255));
        i2 = i + 1;
        bArr[i] = (byte) ((int) ((j >>> 24) & 255));
        i = i2 + 1;
        bArr[i2] = (byte) ((int) ((j >>> 16) & 255));
        i2 = i + 1;
        bArr[i] = (byte) ((int) ((j >>> 8) & 255));
        int i3 = i2 + 1;
        bArr[i2] = (byte) ((int) (j & 255));
        writableSegment.limit = i3;
        this.size += 8;
        return this;
    }

    public Buffer writeLongLe(long j) {
        return writeLong(Util.reverseBytesLong(j));
    }

    public Buffer writeDecimalLong(long j) {
        if (j == 0) {
            return writeByte(48);
        }
        Object obj = null;
        int i = 1;
        if (j < 0) {
            j = -j;
            if (j < 0) {
                return writeUtf8("-9223372036854775808");
            }
            obj = 1;
        }
        if (j >= 100000000) {
            i = j < 1000000000000L ? j < 10000000000L ? j < 1000000000 ? 9 : 10 : j < 100000000000L ? 11 : 12 : j < 1000000000000000L ? j < 10000000000000L ? 13 : j < 100000000000000L ? 14 : 15 : j < 100000000000000000L ? j < 10000000000000000L ? 16 : 17 : j < 1000000000000000000L ? 18 : 19;
        } else if (j >= 10000) {
            i = j < 1000000 ? j < 100000 ? 5 : 6 : j < 10000000 ? 7 : 8;
        } else if (j >= 100) {
            i = j < 1000 ? 3 : 4;
        } else if (j >= 10) {
            i = 2;
        }
        if (obj != null) {
            i++;
        }
        Segment writableSegment = writableSegment(i);
        byte[] bArr = writableSegment.data;
        int i2 = writableSegment.limit + i;
        while (j != 0) {
            i2--;
            bArr[i2] = DIGITS[(int) (j % 10)];
            j /= 10;
        }
        if (obj != null) {
            bArr[i2 - 1] = (byte) 45;
        }
        writableSegment.limit += i;
        this.size += (long) i;
        return this;
    }

    public Buffer writeHexadecimalUnsignedLong(long j) {
        if (j == 0) {
            return writeByte(48);
        }
        int numberOfTrailingZeros = (Long.numberOfTrailingZeros(Long.highestOneBit(j)) / 4) + 1;
        Segment writableSegment = writableSegment(numberOfTrailingZeros);
        byte[] bArr = writableSegment.data;
        int i = writableSegment.limit;
        for (int i2 = (writableSegment.limit + numberOfTrailingZeros) - 1; i2 >= i; i2--) {
            bArr[i2] = DIGITS[(int) (15 & j)];
            j >>>= 4;
        }
        writableSegment.limit += numberOfTrailingZeros;
        this.size += (long) numberOfTrailingZeros;
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    public Segment writableSegment(int i) {
        Segment segment;
        if (i < 1 || i > 8192) {
            throw new IllegalArgumentException();
        } else if (this.head == null) {
            this.head = SegmentPool.take();
            Segment segment2 = this.head;
            Segment segment3 = this.head;
            segment = this.head;
            segment3.prev = segment;
            segment2.next = segment;
            return segment;
        } else {
            segment = this.head.prev;
            if (segment.limit + i > 8192 || !segment.owner) {
                segment = segment.push(SegmentPool.take());
            }
            return segment;
        }
    }

    public void write(Buffer buffer, long j) {
        if (buffer == null) {
            throw new IllegalArgumentException("source == null");
        } else if (buffer != this) {
            Util.checkOffsetAndCount(buffer.size, 0, j);
            while (j > 0) {
                Segment segment;
                long j2;
                if (j < ((long) (buffer.head.limit - buffer.head.pos))) {
                    segment = this.head != null ? this.head.prev : null;
                    if (segment != null && segment.owner) {
                        int i;
                        j2 = ((long) segment.limit) + j;
                        if (segment.shared) {
                            i = 0;
                        } else {
                            i = segment.pos;
                        }
                        if (j2 - ((long) i) <= PlaybackStateCompat.ACTION_PLAY_FROM_URI) {
                            buffer.head.writeTo(segment, (int) j);
                            buffer.size -= j;
                            this.size += j;
                            return;
                        }
                    }
                    buffer.head = buffer.head.split((int) j);
                }
                segment = buffer.head;
                j2 = (long) (segment.limit - segment.pos);
                buffer.head = segment.pop();
                if (this.head == null) {
                    this.head = segment;
                    segment = this.head;
                    Segment segment2 = this.head;
                    Segment segment3 = this.head;
                    segment2.prev = segment3;
                    segment.next = segment3;
                } else {
                    this.head.prev.push(segment).compact();
                }
                buffer.size -= j2;
                this.size += j2;
                j -= j2;
            }
        } else {
            throw new IllegalArgumentException("source == this");
        }
    }

    public long read(Buffer buffer, long j) {
        if (buffer == null) {
            throw new IllegalArgumentException("sink == null");
        } else if (j < 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("byteCount < 0: ");
            stringBuilder.append(j);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (this.size == 0) {
            return -1;
        } else {
            if (j > this.size) {
                j = this.size;
            }
            buffer.write(this, j);
            return j;
        }
    }

    public long indexOf(byte b) {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    public long indexOf(byte b, long j) {
        return indexOf(b, j, Long.MAX_VALUE);
    }

    public long indexOf(byte b, long j, long j2) {
        long j3 = 0;
        if (j < 0 || j2 < j) {
            throw new IllegalArgumentException(String.format("size=%s fromIndex=%s toIndex=%s", new Object[]{Long.valueOf(this.size), Long.valueOf(j), Long.valueOf(j2)}));
        }
        if (j2 > this.size) {
            j2 = this.size;
        }
        if (j == j2) {
            return -1;
        }
        Segment segment = this.head;
        if (segment == null) {
            return -1;
        }
        if (this.size - j >= j) {
            while (true) {
                long j4 = ((long) (segment.limit - segment.pos)) + j3;
                if (j4 >= j) {
                    break;
                }
                segment = segment.next;
                j3 = j4;
            }
        } else {
            j3 = this.size;
            while (j3 > j) {
                segment = segment.prev;
                j3 -= (long) (segment.limit - segment.pos);
            }
        }
        while (j3 < j2) {
            byte[] bArr = segment.data;
            int min = (int) Math.min((long) segment.limit, (((long) segment.pos) + j2) - j3);
            for (int i = (int) ((((long) segment.pos) + j) - j3); i < min; i++) {
                if (bArr[i] == b) {
                    return ((long) (i - segment.pos)) + j3;
                }
            }
            j = ((long) (segment.limit - segment.pos)) + j3;
            segment = segment.next;
            j3 = j;
        }
        return -1;
    }

    public long indexOf(ByteString byteString) throws IOException {
        return indexOf(byteString, 0);
    }

    public long indexOf(ByteString byteString, long j) throws IOException {
        if (byteString.size() != 0) {
            long j2 = 0;
            if (j >= 0) {
                Segment segment = this.head;
                long j3 = -1;
                if (segment == null) {
                    return -1;
                }
                if (this.size - j >= j) {
                    while (true) {
                        long j4 = ((long) (segment.limit - segment.pos)) + j2;
                        if (j4 >= j) {
                            break;
                        }
                        segment = segment.next;
                        j2 = j4;
                    }
                } else {
                    j2 = this.size;
                    while (j2 > j) {
                        segment = segment.prev;
                        j2 -= (long) (segment.limit - segment.pos);
                    }
                }
                byte b = byteString.getByte(0);
                int size = byteString.size();
                long j5 = (this.size - ((long) size)) + 1;
                long j6 = j;
                long j7 = j2;
                Segment segment2 = segment;
                while (j7 < j5) {
                    Segment segment3;
                    byte[] bArr = segment2.data;
                    int min = (int) Math.min((long) segment2.limit, (((long) segment2.pos) + j5) - j7);
                    int i = (int) ((((long) segment2.pos) + j6) - j7);
                    while (i < min) {
                        byte[] bArr2;
                        if (bArr[i] == b) {
                            bArr2 = bArr;
                            segment3 = segment2;
                            if (rangeEquals(segment2, i + 1, byteString, 1, size)) {
                                return ((long) (i - segment3.pos)) + j7;
                            }
                        } else {
                            bArr2 = bArr;
                            segment3 = segment2;
                        }
                        i++;
                        segment2 = segment3;
                        bArr = bArr2;
                    }
                    segment3 = segment2;
                    j6 = ((long) (segment3.limit - segment3.pos)) + j7;
                    segment2 = segment3.next;
                    j7 = j6;
                    j3 = -1;
                }
                return j3;
            }
            throw new IllegalArgumentException("fromIndex < 0");
        }
        throw new IllegalArgumentException("bytes is empty");
    }

    public long indexOfElement(ByteString byteString) {
        return indexOfElement(byteString, 0);
    }

    public long indexOfElement(ByteString byteString, long j) {
        long j2 = 0;
        if (j >= 0) {
            Segment segment = this.head;
            if (segment == null) {
                return -1;
            }
            if (this.size - j >= j) {
                while (true) {
                    long j3 = ((long) (segment.limit - segment.pos)) + j2;
                    if (j3 >= j) {
                        break;
                    }
                    segment = segment.next;
                    j2 = j3;
                }
            } else {
                j2 = this.size;
                while (j2 > j) {
                    segment = segment.prev;
                    j2 -= (long) (segment.limit - segment.pos);
                }
            }
            int i;
            int i2;
            if (byteString.size() == 2) {
                byte b = byteString.getByte(0);
                byte b2 = byteString.getByte(1);
                while (j2 < this.size) {
                    byte[] bArr = segment.data;
                    i = segment.limit;
                    for (i2 = (int) ((((long) segment.pos) + j) - j2); i2 < i; i2++) {
                        byte b3 = bArr[i2];
                        if (b3 == b || b3 == b2) {
                            return ((long) (i2 - segment.pos)) + j2;
                        }
                    }
                    j = ((long) (segment.limit - segment.pos)) + j2;
                    segment = segment.next;
                    j2 = j;
                }
            } else {
                byte[] internalArray = byteString.internalArray();
                while (j2 < this.size) {
                    byte[] bArr2 = segment.data;
                    i = segment.limit;
                    for (i2 = (int) ((((long) segment.pos) + j) - j2); i2 < i; i2++) {
                        byte b4 = bArr2[i2];
                        for (byte b5 : internalArray) {
                            if (b4 == b5) {
                                return ((long) (i2 - segment.pos)) + j2;
                            }
                        }
                    }
                    j = ((long) (segment.limit - segment.pos)) + j2;
                    segment = segment.next;
                    j2 = j;
                }
            }
            return -1;
        }
        throw new IllegalArgumentException("fromIndex < 0");
    }

    public boolean rangeEquals(long j, ByteString byteString) {
        return rangeEquals(j, byteString, 0, byteString.size());
    }

    public boolean rangeEquals(long j, ByteString byteString, int i, int i2) {
        if (j < 0 || i < 0 || i2 < 0 || this.size - j < ((long) i2) || byteString.size() - i < i2) {
            return false;
        }
        for (int i3 = 0; i3 < i2; i3++) {
            if (getByte(((long) i3) + j) != byteString.getByte(i + i3)) {
                return false;
            }
        }
        return true;
    }

    private boolean rangeEquals(Segment segment, int i, ByteString byteString, int i2, int i3) {
        int i4 = segment.limit;
        byte[] bArr = segment.data;
        while (i2 < i3) {
            if (i == i4) {
                segment = segment.next;
                byte[] bArr2 = segment.data;
                i4 = segment.pos;
                bArr = bArr2;
                i = i4;
                i4 = segment.limit;
            }
            if (bArr[i] != byteString.getByte(i2)) {
                return false;
            }
            i++;
            i2++;
        }
        return true;
    }

    public Timeout timeout() {
        return Timeout.NONE;
    }

    /* Access modifiers changed, original: 0000 */
    public List<Integer> segmentSizes() {
        if (this.head == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(this.head.limit - this.head.pos));
        Segment segment = this.head;
        while (true) {
            segment = segment.next;
            if (segment == this.head) {
                return arrayList;
            }
            arrayList.add(Integer.valueOf(segment.limit - segment.pos));
        }
    }

    public ByteString md5() {
        return digest("MD5");
    }

    public ByteString sha1() {
        return digest("SHA-1");
    }

    public ByteString sha256() {
        return digest("SHA-256");
    }

    public ByteString sha512() {
        return digest("SHA-512");
    }

    private ByteString digest(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance(str);
            if (this.head != null) {
                instance.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
                Segment segment = this.head;
                while (true) {
                    segment = segment.next;
                    if (segment == this.head) {
                        break;
                    }
                    instance.update(segment.data, segment.pos, segment.limit - segment.pos);
                }
            }
            return ByteString.of(instance.digest());
        } catch (NoSuchAlgorithmException unused) {
            throw new AssertionError();
        }
    }

    public ByteString hmacSha1(ByteString byteString) {
        return hmac("HmacSHA1", byteString);
    }

    public ByteString hmacSha256(ByteString byteString) {
        return hmac("HmacSHA256", byteString);
    }

    public ByteString hmacSha512(ByteString byteString) {
        return hmac("HmacSHA512", byteString);
    }

    private ByteString hmac(String str, ByteString byteString) {
        try {
            Mac instance = Mac.getInstance(str);
            instance.init(new SecretKeySpec(byteString.toByteArray(), str));
            if (this.head != null) {
                instance.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
                Segment segment = this.head;
                while (true) {
                    segment = segment.next;
                    if (segment == this.head) {
                        break;
                    }
                    instance.update(segment.data, segment.pos, segment.limit - segment.pos);
                }
            }
            return ByteString.of(instance.doFinal());
        } catch (NoSuchAlgorithmException unused) {
            throw new AssertionError();
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Buffer)) {
            return false;
        }
        Buffer buffer = (Buffer) obj;
        if (this.size != buffer.size) {
            return false;
        }
        long j = 0;
        if (this.size == 0) {
            return true;
        }
        Segment segment = this.head;
        Segment segment2 = buffer.head;
        int i = segment.pos;
        int i2 = segment2.pos;
        while (j < this.size) {
            long min = (long) Math.min(segment.limit - i, segment2.limit - i2);
            int i3 = i2;
            i2 = i;
            i = 0;
            while (((long) i) < min) {
                int i4 = i2 + 1;
                int i5 = i3 + 1;
                if (segment.data[i2] != segment2.data[i3]) {
                    return false;
                }
                i++;
                i2 = i4;
                i3 = i5;
            }
            if (i2 == segment.limit) {
                segment = segment.next;
                i = segment.pos;
            } else {
                i = i2;
            }
            if (i3 == segment2.limit) {
                segment2 = segment2.next;
                i2 = segment2.pos;
            } else {
                i2 = i3;
            }
            j += min;
        }
        return true;
    }

    public int hashCode() {
        Segment segment = this.head;
        if (segment == null) {
            return 0;
        }
        int i = 1;
        do {
            for (int i2 = segment.pos; i2 < segment.limit; i2++) {
                i = (i * 31) + segment.data[i2];
            }
            segment = segment.next;
        } while (segment != this.head);
        return i;
    }

    public String toString() {
        return snapshot().toString();
    }

    public Buffer clone() {
        Buffer buffer = new Buffer();
        if (this.size == 0) {
            return buffer;
        }
        buffer.head = this.head.sharedCopy();
        Segment segment = buffer.head;
        Segment segment2 = buffer.head;
        Segment segment3 = buffer.head;
        segment2.prev = segment3;
        segment.next = segment3;
        segment = this.head;
        while (true) {
            segment = segment.next;
            if (segment != this.head) {
                buffer.head.prev.push(segment.sharedCopy());
            } else {
                buffer.size = this.size;
                return buffer;
            }
        }
    }

    public ByteString snapshot() {
        if (this.size <= 2147483647L) {
            return snapshot((int) this.size);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("size > Integer.MAX_VALUE: ");
        stringBuilder.append(this.size);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public ByteString snapshot(int i) {
        if (i == 0) {
            return ByteString.EMPTY;
        }
        return new SegmentedByteString(this, i);
    }

    public UnsafeCursor readUnsafe() {
        return readUnsafe(new UnsafeCursor());
    }

    public UnsafeCursor readUnsafe(UnsafeCursor unsafeCursor) {
        if (unsafeCursor.buffer == null) {
            unsafeCursor.buffer = this;
            unsafeCursor.readWrite = false;
            return unsafeCursor;
        }
        throw new IllegalStateException("already attached to a buffer");
    }

    public UnsafeCursor readAndWriteUnsafe() {
        return readAndWriteUnsafe(new UnsafeCursor());
    }

    public UnsafeCursor readAndWriteUnsafe(UnsafeCursor unsafeCursor) {
        if (unsafeCursor.buffer == null) {
            unsafeCursor.buffer = this;
            unsafeCursor.readWrite = true;
            return unsafeCursor;
        }
        throw new IllegalStateException("already attached to a buffer");
    }
}
