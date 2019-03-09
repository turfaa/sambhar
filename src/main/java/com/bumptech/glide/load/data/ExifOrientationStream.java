package com.bumptech.glide.load.data;

import android.support.annotation.NonNull;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ExifOrientationStream extends FilterInputStream {
    private static final byte[] EXIF_SEGMENT = new byte[]{(byte) -1, (byte) -31, (byte) 0, (byte) 28, (byte) 69, (byte) 120, (byte) 105, (byte) 102, (byte) 0, (byte) 0, (byte) 77, (byte) 77, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 8, (byte) 0, (byte) 1, (byte) 1, (byte) 18, (byte) 0, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0};
    private static final int ORIENTATION_POSITION = (SEGMENT_LENGTH + 2);
    private static final int SEGMENT_LENGTH = EXIF_SEGMENT.length;
    private static final int SEGMENT_START_POSITION = 2;
    private final byte orientation;
    private int position;

    public boolean markSupported() {
        return false;
    }

    public ExifOrientationStream(InputStream inputStream, int i) {
        super(inputStream);
        if (i < -1 || i > 8) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot add invalid orientation: ");
            stringBuilder.append(i);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        this.orientation = (byte) i;
    }

    public void mark(int i) {
        throw new UnsupportedOperationException();
    }

    public int read() throws IOException {
        int read;
        if (this.position < 2 || this.position > ORIENTATION_POSITION) {
            read = super.read();
        } else if (this.position == ORIENTATION_POSITION) {
            read = this.orientation;
        } else {
            read = EXIF_SEGMENT[this.position - 2] & 255;
        }
        if (read != -1) {
            this.position++;
        }
        return read;
    }

    public int read(@NonNull byte[] bArr, int i, int i2) throws IOException {
        int read;
        if (this.position > ORIENTATION_POSITION) {
            read = super.read(bArr, i, i2);
        } else if (this.position == ORIENTATION_POSITION) {
            bArr[i] = this.orientation;
            read = 1;
        } else if (this.position < 2) {
            read = super.read(bArr, i, 2 - this.position);
        } else {
            i2 = Math.min(ORIENTATION_POSITION - this.position, i2);
            System.arraycopy(EXIF_SEGMENT, this.position - 2, bArr, i, i2);
            read = i2;
        }
        if (read > 0) {
            this.position += read;
        }
        return read;
    }

    public long skip(long j) throws IOException {
        j = super.skip(j);
        if (j > 0) {
            this.position = (int) (((long) this.position) + j);
        }
        return j;
    }

    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }
}
