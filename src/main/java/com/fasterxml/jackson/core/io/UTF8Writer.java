package com.fasterxml.jackson.core.io;

import android.support.v4.internal.view.SupportMenu;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class UTF8Writer extends Writer {
    static final int SURR1_FIRST = 55296;
    static final int SURR1_LAST = 56319;
    static final int SURR2_FIRST = 56320;
    static final int SURR2_LAST = 57343;
    private final IOContext _context;
    private OutputStream _out;
    private byte[] _outBuffer;
    private final int _outBufferEnd = (this._outBuffer.length - 4);
    private int _outPtr = 0;
    private int _surrogate;

    public UTF8Writer(IOContext iOContext, OutputStream outputStream) {
        this._context = iOContext;
        this._out = outputStream;
        this._outBuffer = iOContext.allocWriteEncodingBuffer();
    }

    public Writer append(char c) throws IOException {
        write((int) c);
        return this;
    }

    public void close() throws IOException {
        if (this._out != null) {
            if (this._outPtr > 0) {
                this._out.write(this._outBuffer, 0, this._outPtr);
                this._outPtr = 0;
            }
            OutputStream outputStream = this._out;
            this._out = null;
            byte[] bArr = this._outBuffer;
            if (bArr != null) {
                this._outBuffer = null;
                this._context.releaseWriteEncodingBuffer(bArr);
            }
            outputStream.close();
            int i = this._surrogate;
            this._surrogate = 0;
            if (i > 0) {
                illegalSurrogate(i);
            }
        }
    }

    public void flush() throws IOException {
        if (this._out != null) {
            if (this._outPtr > 0) {
                this._out.write(this._outBuffer, 0, this._outPtr);
                this._outPtr = 0;
            }
            this._out.flush();
        }
    }

    public void write(char[] cArr) throws IOException {
        write(cArr, 0, cArr.length);
    }

    public void write(char[] cArr, int i, int i2) throws IOException {
        if (i2 < 2) {
            if (i2 == 1) {
                write(cArr[i]);
            }
            return;
        }
        int i3;
        if (this._surrogate > 0) {
            i3 = i + 1;
            i2--;
            write(convertSurrogate(cArr[i]));
            i = i3;
        }
        i3 = this._outPtr;
        byte[] bArr = this._outBuffer;
        int i4 = this._outBufferEnd;
        i2 += i;
        while (i < i2) {
            int i5;
            if (i3 >= i4) {
                this._out.write(bArr, 0, i3);
                i3 = 0;
            }
            int i6 = i + 1;
            i = cArr[i];
            if (i < 128) {
                i5 = i3 + 1;
                bArr[i3] = (byte) i;
                i = i2 - i6;
                i3 = i4 - i5;
                if (i > i3) {
                    i = i3;
                }
                int i7 = i + i6;
                while (true) {
                    i = i6;
                    i3 = i5;
                    if (i >= i7) {
                        break;
                    }
                    i6 = i + 1;
                    i = cArr[i];
                    if (i >= 128) {
                        break;
                    }
                    i5 = i3 + 1;
                    bArr[i3] = (byte) i;
                }
            }
            if (i < 2048) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i >> 6) | 192);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((i & 63) | 128);
                i = i6;
            } else if (i < 55296 || i > 57343) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i >> 12) | 224);
                i3 = i5 + 1;
                bArr[i5] = (byte) (((i >> 6) & 63) | 128);
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i & 63) | 128);
                i = i6;
                i3 = i5;
            } else {
                if (i > 56319) {
                    this._outPtr = i3;
                    illegalSurrogate(i);
                }
                this._surrogate = i;
                if (i6 >= i2) {
                    break;
                }
                i = i6 + 1;
                i6 = convertSurrogate(cArr[i6]);
                if (i6 > 1114111) {
                    this._outPtr = i3;
                    illegalSurrogate(i6);
                }
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i6 >> 18) | 240);
                i3 = i5 + 1;
                bArr[i5] = (byte) (((i6 >> 12) & 63) | 128);
                i5 = i3 + 1;
                bArr[i3] = (byte) (((i6 >> 6) & 63) | 128);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((i6 & 63) | 128);
            }
        }
        this._outPtr = i3;
    }

    public void write(int i) throws IOException {
        if (this._surrogate > 0) {
            i = convertSurrogate(i);
        } else if (i >= 55296 && i <= 57343) {
            if (i > 56319) {
                illegalSurrogate(i);
            }
            this._surrogate = i;
            return;
        }
        if (this._outPtr >= this._outBufferEnd) {
            this._out.write(this._outBuffer, 0, this._outPtr);
            this._outPtr = 0;
        }
        int i2;
        if (i < 128) {
            byte[] bArr = this._outBuffer;
            i2 = this._outPtr;
            this._outPtr = i2 + 1;
            bArr[i2] = (byte) i;
        } else {
            int i3;
            i2 = this._outPtr;
            int i4;
            if (i < 2048) {
                i4 = i2 + 1;
                this._outBuffer[i2] = (byte) ((i >> 6) | 192);
                i3 = i4 + 1;
                this._outBuffer[i4] = (byte) ((i & 63) | 128);
            } else if (i <= SupportMenu.USER_MASK) {
                i4 = i2 + 1;
                this._outBuffer[i2] = (byte) ((i >> 12) | 224);
                i3 = i4 + 1;
                this._outBuffer[i4] = (byte) (((i >> 6) & 63) | 128);
                i4 = i3 + 1;
                this._outBuffer[i3] = (byte) ((i & 63) | 128);
                i3 = i4;
            } else {
                if (i > 1114111) {
                    illegalSurrogate(i);
                }
                i4 = i2 + 1;
                this._outBuffer[i2] = (byte) ((i >> 18) | 240);
                i3 = i4 + 1;
                this._outBuffer[i4] = (byte) (((i >> 12) & 63) | 128);
                i4 = i3 + 1;
                this._outBuffer[i3] = (byte) (((i >> 6) & 63) | 128);
                i3 = i4 + 1;
                this._outBuffer[i4] = (byte) ((i & 63) | 128);
            }
            this._outPtr = i3;
        }
    }

    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    public void write(String str, int i, int i2) throws IOException {
        if (i2 < 2) {
            if (i2 == 1) {
                write(str.charAt(i));
            }
            return;
        }
        int i3;
        if (this._surrogate > 0) {
            i3 = i + 1;
            i2--;
            write(convertSurrogate(str.charAt(i)));
            i = i3;
        }
        i3 = this._outPtr;
        byte[] bArr = this._outBuffer;
        int i4 = this._outBufferEnd;
        i2 += i;
        while (i < i2) {
            int i5;
            if (i3 >= i4) {
                this._out.write(bArr, 0, i3);
                i3 = 0;
            }
            int i6 = i + 1;
            i = str.charAt(i);
            if (i < 128) {
                i5 = i3 + 1;
                bArr[i3] = (byte) i;
                i = i2 - i6;
                i3 = i4 - i5;
                if (i > i3) {
                    i = i3;
                }
                int i7 = i + i6;
                while (true) {
                    i = i6;
                    i3 = i5;
                    if (i >= i7) {
                        break;
                    }
                    i6 = i + 1;
                    i = str.charAt(i);
                    if (i >= 128) {
                        break;
                    }
                    i5 = i3 + 1;
                    bArr[i3] = (byte) i;
                }
            }
            if (i < 2048) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i >> 6) | 192);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((i & 63) | 128);
                i = i6;
            } else if (i < 55296 || i > 57343) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i >> 12) | 224);
                i3 = i5 + 1;
                bArr[i5] = (byte) (((i >> 6) & 63) | 128);
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i & 63) | 128);
                i = i6;
                i3 = i5;
            } else {
                if (i > 56319) {
                    this._outPtr = i3;
                    illegalSurrogate(i);
                }
                this._surrogate = i;
                if (i6 >= i2) {
                    break;
                }
                i = i6 + 1;
                i6 = convertSurrogate(str.charAt(i6));
                if (i6 > 1114111) {
                    this._outPtr = i3;
                    illegalSurrogate(i6);
                }
                i5 = i3 + 1;
                bArr[i3] = (byte) ((i6 >> 18) | 240);
                i3 = i5 + 1;
                bArr[i5] = (byte) (((i6 >> 12) & 63) | 128);
                i5 = i3 + 1;
                bArr[i3] = (byte) (((i6 >> 6) & 63) | 128);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((i6 & 63) | 128);
            }
        }
        this._outPtr = i3;
    }

    /* Access modifiers changed, original: protected */
    public int convertSurrogate(int i) throws IOException {
        int i2 = this._surrogate;
        this._surrogate = 0;
        if (i >= 56320 && i <= 57343) {
            return (((i2 - 55296) << 10) + 65536) + (i - 56320);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Broken surrogate pair: first char 0x");
        stringBuilder.append(Integer.toHexString(i2));
        stringBuilder.append(", second 0x");
        stringBuilder.append(Integer.toHexString(i));
        stringBuilder.append("; illegal combination");
        throw new IOException(stringBuilder.toString());
    }

    protected static void illegalSurrogate(int i) throws IOException {
        throw new IOException(illegalSurrogateDesc(i));
    }

    protected static String illegalSurrogateDesc(int i) {
        StringBuilder stringBuilder;
        if (i > 1114111) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal character point (0x");
            stringBuilder.append(Integer.toHexString(i));
            stringBuilder.append(") to output; max is 0x10FFFF as per RFC 4627");
            return stringBuilder.toString();
        } else if (i < 55296) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal character point (0x");
            stringBuilder.append(Integer.toHexString(i));
            stringBuilder.append(") to output");
            return stringBuilder.toString();
        } else if (i <= 56319) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unmatched first part of surrogate pair (0x");
            stringBuilder.append(Integer.toHexString(i));
            stringBuilder.append(")");
            return stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unmatched second part of surrogate pair (0x");
            stringBuilder.append(Integer.toHexString(i));
            stringBuilder.append(")");
            return stringBuilder.toString();
        }
    }
}
