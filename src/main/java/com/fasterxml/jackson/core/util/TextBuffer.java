package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.io.NumberInput;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public final class TextBuffer {
    static final int MAX_SEGMENT_LEN = 262144;
    static final int MIN_SEGMENT_LEN = 1000;
    static final char[] NO_CHARS = new char[0];
    private final BufferRecycler _allocator;
    private char[] _currentSegment;
    private int _currentSize;
    private boolean _hasSegments;
    private char[] _inputBuffer;
    private int _inputLen;
    private int _inputStart;
    private char[] _resultArray;
    private String _resultString;
    private int _segmentSize;
    private ArrayList<char[]> _segments;

    public TextBuffer(BufferRecycler bufferRecycler) {
        this._allocator = bufferRecycler;
    }

    public void releaseBuffers() {
        if (this._allocator == null) {
            resetWithEmpty();
        } else if (this._currentSegment != null) {
            resetWithEmpty();
            char[] cArr = this._currentSegment;
            this._currentSegment = null;
            this._allocator.releaseCharBuffer(2, cArr);
        }
    }

    public void resetWithEmpty() {
        this._inputStart = -1;
        this._currentSize = 0;
        this._inputLen = 0;
        this._inputBuffer = null;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        }
    }

    public void resetWithShared(char[] cArr, int i, int i2) {
        this._resultString = null;
        this._resultArray = null;
        this._inputBuffer = cArr;
        this._inputStart = i;
        this._inputLen = i2;
        if (this._hasSegments) {
            clearSegments();
        }
    }

    public void resetWithCopy(char[] cArr, int i, int i2) {
        this._inputBuffer = null;
        this._inputStart = -1;
        this._inputLen = 0;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        } else if (this._currentSegment == null) {
            this._currentSegment = buf(i2);
        }
        this._segmentSize = 0;
        this._currentSize = 0;
        append(cArr, i, i2);
    }

    public void resetWithString(String str) {
        this._inputBuffer = null;
        this._inputStart = -1;
        this._inputLen = 0;
        this._resultString = str;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        }
        this._currentSize = 0;
    }

    private char[] buf(int i) {
        if (this._allocator != null) {
            return this._allocator.allocCharBuffer(2, i);
        }
        return new char[Math.max(i, 1000)];
    }

    private void clearSegments() {
        this._hasSegments = false;
        this._segments.clear();
        this._segmentSize = 0;
        this._currentSize = 0;
    }

    public int size() {
        if (this._inputStart >= 0) {
            return this._inputLen;
        }
        if (this._resultArray != null) {
            return this._resultArray.length;
        }
        if (this._resultString != null) {
            return this._resultString.length();
        }
        return this._segmentSize + this._currentSize;
    }

    public int getTextOffset() {
        return this._inputStart >= 0 ? this._inputStart : 0;
    }

    /* JADX WARNING: Missing block: B:9:0x0011, code skipped:
            return true;
     */
    public boolean hasTextAsCharacters() {
        /*
        r2 = this;
        r0 = r2._inputStart;
        r1 = 1;
        if (r0 >= 0) goto L_0x0011;
    L_0x0005:
        r0 = r2._resultArray;
        if (r0 == 0) goto L_0x000a;
    L_0x0009:
        goto L_0x0011;
    L_0x000a:
        r0 = r2._resultString;
        if (r0 == 0) goto L_0x0010;
    L_0x000e:
        r0 = 0;
        return r0;
    L_0x0010:
        return r1;
    L_0x0011:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.util.TextBuffer.hasTextAsCharacters():boolean");
    }

    public char[] getTextBuffer() {
        if (this._inputStart >= 0) {
            return this._inputBuffer;
        }
        if (this._resultArray != null) {
            return this._resultArray;
        }
        if (this._resultString != null) {
            char[] toCharArray = this._resultString.toCharArray();
            this._resultArray = toCharArray;
            return toCharArray;
        } else if (this._hasSegments) {
            return contentsAsArray();
        } else {
            return this._currentSegment == null ? NO_CHARS : this._currentSegment;
        }
    }

    public String contentsAsString() {
        if (this._resultString == null) {
            String str;
            if (this._resultArray != null) {
                this._resultString = new String(this._resultArray);
            } else if (this._inputStart < 0) {
                int i = this._segmentSize;
                int i2 = this._currentSize;
                if (i == 0) {
                    if (i2 == 0) {
                        str = "";
                    } else {
                        str = new String(this._currentSegment, 0, i2);
                    }
                    this._resultString = str;
                } else {
                    StringBuilder stringBuilder = new StringBuilder(i + i2);
                    if (this._segments != null) {
                        i = this._segments.size();
                        for (i2 = 0; i2 < i; i2++) {
                            char[] cArr = (char[]) this._segments.get(i2);
                            stringBuilder.append(cArr, 0, cArr.length);
                        }
                    }
                    stringBuilder.append(this._currentSegment, 0, this._currentSize);
                    this._resultString = stringBuilder.toString();
                }
            } else if (this._inputLen < 1) {
                str = "";
                this._resultString = str;
                return str;
            } else {
                this._resultString = new String(this._inputBuffer, this._inputStart, this._inputLen);
            }
        }
        return this._resultString;
    }

    public char[] contentsAsArray() {
        char[] cArr = this._resultArray;
        if (cArr != null) {
            return cArr;
        }
        cArr = resultArray();
        this._resultArray = cArr;
        return cArr;
    }

    public BigDecimal contentsAsDecimal() throws NumberFormatException {
        if (this._resultArray != null) {
            return NumberInput.parseBigDecimal(this._resultArray);
        }
        if (this._inputStart >= 0 && this._inputBuffer != null) {
            return NumberInput.parseBigDecimal(this._inputBuffer, this._inputStart, this._inputLen);
        }
        if (this._segmentSize != 0 || this._currentSegment == null) {
            return NumberInput.parseBigDecimal(contentsAsArray());
        }
        return NumberInput.parseBigDecimal(this._currentSegment, 0, this._currentSize);
    }

    public double contentsAsDouble() throws NumberFormatException {
        return NumberInput.parseDouble(contentsAsString());
    }

    public int contentsToWriter(Writer writer) throws IOException {
        int i;
        if (this._resultArray != null) {
            writer.write(this._resultArray);
            return this._resultArray.length;
        } else if (this._resultString != null) {
            writer.write(this._resultString);
            return this._resultString.length();
        } else if (this._inputStart >= 0) {
            i = this._inputLen;
            if (i > 0) {
                writer.write(this._inputBuffer, this._inputStart, i);
            }
            return i;
        } else {
            int i2;
            if (this._segments != null) {
                i = this._segments.size();
                i2 = 0;
                for (int i3 = 0; i3 < i; i3++) {
                    char[] cArr = (char[]) this._segments.get(i3);
                    int length = cArr.length;
                    writer.write(cArr, 0, length);
                    i2 += length;
                }
            } else {
                i2 = 0;
            }
            i = this._currentSize;
            if (i > 0) {
                writer.write(this._currentSegment, 0, i);
                i2 += i;
            }
            return i2;
        }
    }

    public void ensureNotShared() {
        if (this._inputStart >= 0) {
            unshare(16);
        }
    }

    public void append(char c) {
        if (this._inputStart >= 0) {
            unshare(16);
        }
        this._resultString = null;
        this._resultArray = null;
        char[] cArr = this._currentSegment;
        if (this._currentSize >= cArr.length) {
            expand(1);
            cArr = this._currentSegment;
        }
        int i = this._currentSize;
        this._currentSize = i + 1;
        cArr[i] = c;
    }

    public void append(char[] cArr, int i, int i2) {
        if (this._inputStart >= 0) {
            unshare(i2);
        }
        this._resultString = null;
        this._resultArray = null;
        char[] cArr2 = this._currentSegment;
        int length = cArr2.length - this._currentSize;
        if (length >= i2) {
            System.arraycopy(cArr, i, cArr2, this._currentSize, i2);
            this._currentSize += i2;
            return;
        }
        if (length > 0) {
            System.arraycopy(cArr, i, cArr2, this._currentSize, length);
            i += length;
            i2 -= length;
        }
        do {
            expand(i2);
            int min = Math.min(this._currentSegment.length, i2);
            System.arraycopy(cArr, i, this._currentSegment, 0, min);
            this._currentSize += min;
            i += min;
            i2 -= min;
        } while (i2 > 0);
    }

    public void append(String str, int i, int i2) {
        if (this._inputStart >= 0) {
            unshare(i2);
        }
        this._resultString = null;
        this._resultArray = null;
        char[] cArr = this._currentSegment;
        int length = cArr.length - this._currentSize;
        if (length >= i2) {
            str.getChars(i, i + i2, cArr, this._currentSize);
            this._currentSize += i2;
            return;
        }
        if (length > 0) {
            int i3 = i + length;
            str.getChars(i, i3, cArr, this._currentSize);
            i2 -= length;
            i = i3;
        }
        while (true) {
            expand(i2);
            int min = Math.min(this._currentSegment.length, i2);
            length = i + min;
            str.getChars(i, length, this._currentSegment, 0);
            this._currentSize += min;
            i2 -= min;
            if (i2 > 0) {
                i = length;
            } else {
                return;
            }
        }
    }

    public char[] getCurrentSegment() {
        if (this._inputStart >= 0) {
            unshare(1);
        } else {
            char[] cArr = this._currentSegment;
            if (cArr == null) {
                this._currentSegment = buf(0);
            } else if (this._currentSize >= cArr.length) {
                expand(1);
            }
        }
        return this._currentSegment;
    }

    public char[] emptyAndGetCurrentSegment() {
        this._inputStart = -1;
        this._currentSize = 0;
        this._inputLen = 0;
        this._inputBuffer = null;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        }
        char[] cArr = this._currentSegment;
        if (cArr != null) {
            return cArr;
        }
        cArr = buf(0);
        this._currentSegment = cArr;
        return cArr;
    }

    public int getCurrentSegmentSize() {
        return this._currentSize;
    }

    public void setCurrentLength(int i) {
        this._currentSize = i;
    }

    public String setCurrentAndReturn(int i) {
        this._currentSize = i;
        if (this._segmentSize > 0) {
            return contentsAsString();
        }
        String str;
        i = this._currentSize;
        if (i == 0) {
            str = "";
        } else {
            str = new String(this._currentSegment, 0, i);
        }
        this._resultString = str;
        return str;
    }

    public char[] finishCurrentSegment() {
        if (this._segments == null) {
            this._segments = new ArrayList();
        }
        this._hasSegments = true;
        this._segments.add(this._currentSegment);
        int length = this._currentSegment.length;
        this._segmentSize += length;
        this._currentSize = 0;
        length += length >> 1;
        if (length < 1000) {
            length = 1000;
        } else if (length > 262144) {
            length = 262144;
        }
        char[] carr = carr(length);
        this._currentSegment = carr;
        return carr;
    }

    public char[] expandCurrentSegment() {
        char[] cArr = this._currentSegment;
        int length = cArr.length;
        int i = (length >> 1) + length;
        if (i > 262144) {
            i = (length >> 2) + length;
        }
        cArr = Arrays.copyOf(cArr, i);
        this._currentSegment = cArr;
        return cArr;
    }

    public char[] expandCurrentSegment(int i) {
        char[] cArr = this._currentSegment;
        if (cArr.length >= i) {
            return cArr;
        }
        char[] copyOf = Arrays.copyOf(cArr, i);
        this._currentSegment = copyOf;
        return copyOf;
    }

    public String toString() {
        return contentsAsString();
    }

    private void unshare(int i) {
        int i2 = this._inputLen;
        this._inputLen = 0;
        char[] cArr = this._inputBuffer;
        this._inputBuffer = null;
        int i3 = this._inputStart;
        this._inputStart = -1;
        i += i2;
        if (this._currentSegment == null || i > this._currentSegment.length) {
            this._currentSegment = buf(i);
        }
        if (i2 > 0) {
            System.arraycopy(cArr, i3, this._currentSegment, 0, i2);
        }
        this._segmentSize = 0;
        this._currentSize = i2;
    }

    private void expand(int i) {
        if (this._segments == null) {
            this._segments = new ArrayList();
        }
        char[] cArr = this._currentSegment;
        this._hasSegments = true;
        this._segments.add(cArr);
        this._segmentSize += cArr.length;
        this._currentSize = 0;
        i = cArr.length;
        i += i >> 1;
        if (i < 1000) {
            i = 1000;
        } else if (i > 262144) {
            i = 262144;
        }
        this._currentSegment = carr(i);
    }

    private char[] resultArray() {
        if (this._resultString != null) {
            return this._resultString.toCharArray();
        }
        int i;
        int i2;
        if (this._inputStart >= 0) {
            i = this._inputLen;
            if (i < 1) {
                return NO_CHARS;
            }
            i2 = this._inputStart;
            if (i2 == 0) {
                return Arrays.copyOf(this._inputBuffer, i);
            }
            return Arrays.copyOfRange(this._inputBuffer, i2, i + i2);
        }
        i = size();
        if (i < 1) {
            return NO_CHARS;
        }
        int i3;
        char[] carr = carr(i);
        if (this._segments != null) {
            i2 = this._segments.size();
            i3 = 0;
            for (int i4 = 0; i4 < i2; i4++) {
                char[] cArr = (char[]) this._segments.get(i4);
                int length = cArr.length;
                System.arraycopy(cArr, 0, carr, i3, length);
                i3 += length;
            }
        } else {
            i3 = 0;
        }
        System.arraycopy(this._currentSegment, 0, carr, i3, this._currentSize);
        return carr;
    }

    private char[] carr(int i) {
        return new char[i];
    }
}
