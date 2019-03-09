package com.bumptech.glide.gifdecoder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class GifHeaderParser {
    static final int DEFAULT_FRAME_DELAY = 10;
    private static final int DESCRIPTOR_MASK_INTERLACE_FLAG = 64;
    private static final int DESCRIPTOR_MASK_LCT_FLAG = 128;
    private static final int DESCRIPTOR_MASK_LCT_SIZE = 7;
    private static final int EXTENSION_INTRODUCER = 33;
    private static final int GCE_DISPOSAL_METHOD_SHIFT = 2;
    private static final int GCE_MASK_DISPOSAL_METHOD = 28;
    private static final int GCE_MASK_TRANSPARENT_COLOR_FLAG = 1;
    private static final int IMAGE_SEPARATOR = 44;
    private static final int LABEL_APPLICATION_EXTENSION = 255;
    private static final int LABEL_COMMENT_EXTENSION = 254;
    private static final int LABEL_GRAPHIC_CONTROL_EXTENSION = 249;
    private static final int LABEL_PLAIN_TEXT_EXTENSION = 1;
    private static final int LSD_MASK_GCT_FLAG = 128;
    private static final int LSD_MASK_GCT_SIZE = 7;
    private static final int MASK_INT_LOWEST_BYTE = 255;
    private static final int MAX_BLOCK_SIZE = 256;
    static final int MIN_FRAME_DELAY = 2;
    private static final String TAG = "GifHeaderParser";
    private static final int TRAILER = 59;
    private final byte[] block = new byte[256];
    private int blockSize = 0;
    private GifHeader header;
    private ByteBuffer rawData;

    public GifHeaderParser setData(@NonNull ByteBuffer byteBuffer) {
        reset();
        this.rawData = byteBuffer.asReadOnlyBuffer();
        this.rawData.position(0);
        this.rawData.order(ByteOrder.LITTLE_ENDIAN);
        return this;
    }

    public GifHeaderParser setData(@Nullable byte[] bArr) {
        if (bArr != null) {
            setData(ByteBuffer.wrap(bArr));
        } else {
            this.rawData = null;
            this.header.status = 2;
        }
        return this;
    }

    public void clear() {
        this.rawData = null;
        this.header = null;
    }

    private void reset() {
        this.rawData = null;
        Arrays.fill(this.block, (byte) 0);
        this.header = new GifHeader();
        this.blockSize = 0;
    }

    @NonNull
    public GifHeader parseHeader() {
        if (this.rawData == null) {
            throw new IllegalStateException("You must call setData() before parseHeader()");
        } else if (err()) {
            return this.header;
        } else {
            readHeader();
            if (!err()) {
                readContents();
                if (this.header.frameCount < 0) {
                    this.header.status = 1;
                }
            }
            return this.header;
        }
    }

    public boolean isAnimated() {
        readHeader();
        if (!err()) {
            readContents(2);
        }
        return this.header.frameCount > 1;
    }

    private void readContents() {
        readContents(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    private void readContents(int i) {
        Object obj = null;
        while (obj == null && !err() && this.header.frameCount <= i) {
            int read = read();
            if (read == 33) {
                read = read();
                if (read == 1) {
                    skip();
                } else if (read != LABEL_GRAPHIC_CONTROL_EXTENSION) {
                    switch (read) {
                        case LABEL_COMMENT_EXTENSION /*254*/:
                            skip();
                            break;
                        case 255:
                            readBlock();
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i2 = 0; i2 < 11; i2++) {
                                stringBuilder.append((char) this.block[i2]);
                            }
                            if (!stringBuilder.toString().equals("NETSCAPE2.0")) {
                                skip();
                                break;
                            } else {
                                readNetscapeExt();
                                break;
                            }
                        default:
                            skip();
                            break;
                    }
                } else {
                    this.header.currentFrame = new GifFrame();
                    readGraphicControlExt();
                }
            } else if (read == 44) {
                if (this.header.currentFrame == null) {
                    this.header.currentFrame = new GifFrame();
                }
                readBitmap();
            } else if (read != 59) {
                this.header.status = 1;
            } else {
                obj = 1;
            }
        }
    }

    private void readGraphicControlExt() {
        read();
        int read = read();
        this.header.currentFrame.dispose = (read & 28) >> 2;
        boolean z = true;
        if (this.header.currentFrame.dispose == 0) {
            this.header.currentFrame.dispose = 1;
        }
        GifFrame gifFrame = this.header.currentFrame;
        if ((read & 1) == 0) {
            z = false;
        }
        gifFrame.transparency = z;
        read = readShort();
        if (read < 2) {
            read = 10;
        }
        this.header.currentFrame.delay = read * 10;
        this.header.currentFrame.transIndex = read();
        read();
    }

    private void readBitmap() {
        this.header.currentFrame.ix = readShort();
        this.header.currentFrame.iy = readShort();
        this.header.currentFrame.iw = readShort();
        this.header.currentFrame.ih = readShort();
        int read = read();
        boolean z = false;
        Object obj = (read & 128) != 0 ? 1 : null;
        int pow = (int) Math.pow(2.0d, (double) ((read & 7) + 1));
        GifFrame gifFrame = this.header.currentFrame;
        if ((read & 64) != 0) {
            z = true;
        }
        gifFrame.interlace = z;
        if (obj != null) {
            this.header.currentFrame.lct = readColorTable(pow);
        } else {
            this.header.currentFrame.lct = null;
        }
        this.header.currentFrame.bufferFrameStart = this.rawData.position();
        skipImageData();
        if (!err()) {
            GifHeader gifHeader = this.header;
            gifHeader.frameCount++;
            this.header.frames.add(this.header.currentFrame);
        }
    }

    private void readNetscapeExt() {
        do {
            readBlock();
            if (this.block[0] == (byte) 1) {
                this.header.loopCount = (this.block[1] & 255) | ((this.block[2] & 255) << 8);
            }
            if (this.blockSize <= 0) {
                return;
            }
        } while (!err());
    }

    private void readHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append((char) read());
        }
        if (stringBuilder.toString().startsWith("GIF")) {
            readLSD();
            if (this.header.gctFlag && !err()) {
                this.header.gct = readColorTable(this.header.gctSize);
                this.header.bgColor = this.header.gct[this.header.bgIndex];
            }
            return;
        }
        this.header.status = 1;
    }

    private void readLSD() {
        this.header.width = readShort();
        this.header.height = readShort();
        int read = read();
        this.header.gctFlag = (read & 128) != 0;
        this.header.gctSize = (int) Math.pow(2.0d, (double) ((read & 7) + 1));
        this.header.bgIndex = read();
        this.header.pixelAspect = read();
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0041  */
    @android.support.annotation.Nullable
    private int[] readColorTable(int r10) {
        /*
        r9 = this;
        r0 = r10 * 3;
        r0 = new byte[r0];
        r1 = 0;
        r2 = r9.rawData;	 Catch:{ BufferUnderflowException -> 0x0036 }
        r2.get(r0);	 Catch:{ BufferUnderflowException -> 0x0036 }
        r2 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
        r2 = new int[r2];	 Catch:{ BufferUnderflowException -> 0x0036 }
        r1 = 0;
        r3 = 0;
    L_0x0010:
        if (r1 >= r10) goto L_0x004d;
    L_0x0012:
        r4 = r3 + 1;
        r3 = r0[r3];	 Catch:{ BufferUnderflowException -> 0x0034 }
        r3 = r3 & 255;
        r5 = r4 + 1;
        r4 = r0[r4];	 Catch:{ BufferUnderflowException -> 0x0034 }
        r4 = r4 & 255;
        r6 = r5 + 1;
        r5 = r0[r5];	 Catch:{ BufferUnderflowException -> 0x0034 }
        r5 = r5 & 255;
        r7 = r1 + 1;
        r8 = -16777216; // 0xffffffffff000000 float:-1.7014118E38 double:NaN;
        r3 = r3 << 16;
        r3 = r3 | r8;
        r4 = r4 << 8;
        r3 = r3 | r4;
        r3 = r3 | r5;
        r2[r1] = r3;	 Catch:{ BufferUnderflowException -> 0x0034 }
        r3 = r6;
        r1 = r7;
        goto L_0x0010;
    L_0x0034:
        r10 = move-exception;
        goto L_0x0038;
    L_0x0036:
        r10 = move-exception;
        r2 = r1;
    L_0x0038:
        r0 = "GifHeaderParser";
        r1 = 3;
        r0 = android.util.Log.isLoggable(r0, r1);
        if (r0 == 0) goto L_0x0048;
    L_0x0041:
        r0 = "GifHeaderParser";
        r1 = "Format Error Reading Color Table";
        android.util.Log.d(r0, r1, r10);
    L_0x0048:
        r10 = r9.header;
        r0 = 1;
        r10.status = r0;
    L_0x004d:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.gifdecoder.GifHeaderParser.readColorTable(int):int[]");
    }

    private void skipImageData() {
        read();
        skip();
    }

    private void skip() {
        int read;
        do {
            read = read();
            this.rawData.position(Math.min(this.rawData.position() + read, this.rawData.limit()));
        } while (read > 0);
    }

    private void readBlock() {
        this.blockSize = read();
        if (this.blockSize > 0) {
            int i = 0;
            int i2 = 0;
            while (i < this.blockSize) {
                try {
                    i2 = this.blockSize - i;
                    this.rawData.get(this.block, i, i2);
                    i += i2;
                } catch (Exception e) {
                    if (Log.isLoggable(TAG, 3)) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Error Reading Block n: ");
                        stringBuilder.append(i);
                        stringBuilder.append(" count: ");
                        stringBuilder.append(i2);
                        stringBuilder.append(" blockSize: ");
                        stringBuilder.append(this.blockSize);
                        Log.d(str, stringBuilder.toString(), e);
                    }
                    this.header.status = 1;
                    return;
                }
            }
        }
    }

    private int read() {
        try {
            return this.rawData.get() & 255;
        } catch (Exception unused) {
            this.header.status = 1;
            return 0;
        }
    }

    private int readShort() {
        return this.rawData.getShort();
    }

    private boolean err() {
        return this.header.status != 0;
    }
}
