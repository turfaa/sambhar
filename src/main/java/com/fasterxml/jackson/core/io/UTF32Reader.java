package com.fasterxml.jackson.core.io;

import android.support.v4.internal.view.SupportMenu;
import com.fasterxml.jackson.core.base.GeneratorBase;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class UTF32Reader extends Reader {
    protected static final int LAST_VALID_UNICODE_CHAR = 1114111;
    protected static final char NC = 0;
    protected final boolean _bigEndian;
    protected byte[] _buffer;
    protected int _byteCount;
    protected int _charCount;
    protected final IOContext _context;
    protected InputStream _in;
    protected int _length;
    protected final boolean _managedBuffers;
    protected int _ptr;
    protected char _surrogate = 0;
    protected char[] _tmpBuf;

    public UTF32Reader(IOContext iOContext, InputStream inputStream, byte[] bArr, int i, int i2, boolean z) {
        boolean z2 = false;
        this._context = iOContext;
        this._in = inputStream;
        this._buffer = bArr;
        this._ptr = i;
        this._length = i2;
        this._bigEndian = z;
        if (inputStream != null) {
            z2 = true;
        }
        this._managedBuffers = z2;
    }

    public void close() throws IOException {
        InputStream inputStream = this._in;
        if (inputStream != null) {
            this._in = null;
            freeBuffers();
            inputStream.close();
        }
    }

    public int read() throws IOException {
        if (this._tmpBuf == null) {
            this._tmpBuf = new char[1];
        }
        if (read(this._tmpBuf, 0, 1) < 1) {
            return -1;
        }
        return this._tmpBuf[0];
    }

    public int read(char[] cArr, int i, int i2) throws IOException {
        if (this._buffer == null) {
            return -1;
        }
        if (i2 < 1) {
            return i2;
        }
        int i3;
        int i4;
        if (i < 0 || i + i2 > cArr.length) {
            reportBounds(cArr, i, i2);
        }
        i2 += i;
        if (this._surrogate != 0) {
            i3 = i + 1;
            cArr[i] = this._surrogate;
            this._surrogate = 0;
        } else {
            i3 = this._length - this._ptr;
            if (i3 < 4 && !loadMore(i3)) {
                return -1;
            }
            i3 = i;
        }
        while (i3 < i2) {
            int i5 = this._ptr;
            if (this._bigEndian) {
                i5 = (this._buffer[i5 + 3] & 255) | (((this._buffer[i5] << 24) | ((this._buffer[i5 + 1] & 255) << 16)) | ((this._buffer[i5 + 2] & 255) << 8));
            } else {
                i5 = (this._buffer[i5 + 3] << 24) | (((this._buffer[i5] & 255) | ((this._buffer[i5 + 1] & 255) << 8)) | ((this._buffer[i5 + 2] & 255) << 16));
            }
            this._ptr += 4;
            if (i5 > SupportMenu.USER_MASK) {
                if (i5 > LAST_VALID_UNICODE_CHAR) {
                    int i6 = i3 - i;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("(above ");
                    stringBuilder.append(Integer.toHexString(LAST_VALID_UNICODE_CHAR));
                    stringBuilder.append(") ");
                    reportInvalid(i5, i6, stringBuilder.toString());
                }
                i5 -= 65536;
                i4 = i3 + 1;
                cArr[i3] = (char) ((i5 >> 10) + GeneratorBase.SURR1_FIRST);
                i5 = (i5 & 1023) | GeneratorBase.SURR2_FIRST;
                if (i4 >= i2) {
                    this._surrogate = (char) i5;
                    break;
                }
                i3 = i4;
            }
            i4 = i3 + 1;
            cArr[i3] = (char) i5;
            if (this._ptr >= this._length) {
                break;
            }
            i3 = i4;
        }
        i4 = i3;
        i4 -= i;
        this._charCount += i4;
        return i4;
    }

    private void reportUnexpectedEOF(int i, int i2) throws IOException {
        int i3 = this._byteCount + i;
        int i4 = this._charCount;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected EOF in the middle of a 4-byte UTF-32 char: got ");
        stringBuilder.append(i);
        stringBuilder.append(", needed ");
        stringBuilder.append(i2);
        stringBuilder.append(", at char #");
        stringBuilder.append(i4);
        stringBuilder.append(", byte #");
        stringBuilder.append(i3);
        stringBuilder.append(")");
        throw new CharConversionException(stringBuilder.toString());
    }

    private void reportInvalid(int i, int i2, String str) throws IOException {
        int i3 = (this._byteCount + this._ptr) - 1;
        int i4 = this._charCount + i2;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid UTF-32 character 0x");
        stringBuilder.append(Integer.toHexString(i));
        stringBuilder.append(str);
        stringBuilder.append(" at char #");
        stringBuilder.append(i4);
        stringBuilder.append(", byte #");
        stringBuilder.append(i3);
        stringBuilder.append(")");
        throw new CharConversionException(stringBuilder.toString());
    }

    private boolean loadMore(int i) throws IOException {
        this._byteCount += this._length - i;
        if (i > 0) {
            if (this._ptr > 0) {
                System.arraycopy(this._buffer, this._ptr, this._buffer, 0, i);
                this._ptr = 0;
            }
            this._length = i;
        } else {
            this._ptr = 0;
            i = this._in == null ? -1 : this._in.read(this._buffer);
            if (i < 1) {
                this._length = 0;
                if (i < 0) {
                    if (this._managedBuffers) {
                        freeBuffers();
                    }
                    return false;
                }
                reportStrangeStream();
            }
            this._length = i;
        }
        while (this._length < 4) {
            i = this._in == null ? -1 : this._in.read(this._buffer, this._length, this._buffer.length - this._length);
            if (i < 1) {
                if (i < 0) {
                    if (this._managedBuffers) {
                        freeBuffers();
                    }
                    reportUnexpectedEOF(this._length, 4);
                }
                reportStrangeStream();
            }
            this._length += i;
        }
        return true;
    }

    private void freeBuffers() {
        byte[] bArr = this._buffer;
        if (bArr != null) {
            this._buffer = null;
            this._context.releaseReadIOBuffer(bArr);
        }
    }

    private void reportBounds(char[] cArr, int i, int i2) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("read(buf,");
        stringBuilder.append(i);
        stringBuilder.append(",");
        stringBuilder.append(i2);
        stringBuilder.append("), cbuf[");
        stringBuilder.append(cArr.length);
        stringBuilder.append("]");
        throw new ArrayIndexOutOfBoundsException(stringBuilder.toString());
    }

    private void reportStrangeStream() throws IOException {
        throw new IOException("Strange I/O stream, returned 0 bytes on read");
    }
}
