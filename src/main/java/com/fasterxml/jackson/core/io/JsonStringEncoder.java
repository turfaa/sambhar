package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import java.lang.ref.SoftReference;

public final class JsonStringEncoder {
    private static final byte[] HB = CharTypes.copyHexBytes();
    private static final char[] HC = CharTypes.copyHexChars();
    private static final int SURR1_FIRST = 55296;
    private static final int SURR1_LAST = 56319;
    private static final int SURR2_FIRST = 56320;
    private static final int SURR2_LAST = 57343;
    protected static final ThreadLocal<SoftReference<JsonStringEncoder>> _threadEncoder = new ThreadLocal();
    protected ByteArrayBuilder _bytes;
    protected final char[] _qbuf = new char[6];
    protected TextBuffer _text;

    public JsonStringEncoder() {
        this._qbuf[0] = '\\';
        this._qbuf[2] = '0';
        this._qbuf[3] = '0';
    }

    public static JsonStringEncoder getInstance() {
        JsonStringEncoder jsonStringEncoder;
        SoftReference softReference = (SoftReference) _threadEncoder.get();
        if (softReference == null) {
            jsonStringEncoder = null;
        } else {
            jsonStringEncoder = (JsonStringEncoder) softReference.get();
        }
        if (jsonStringEncoder != null) {
            return jsonStringEncoder;
        }
        jsonStringEncoder = new JsonStringEncoder();
        _threadEncoder.set(new SoftReference(jsonStringEncoder));
        return jsonStringEncoder;
    }

    /* JADX WARNING: Missing block: B:9:0x0029, code skipped:
            r8 = r1 + 1;
            r1 = r12.charAt(r1);
            r9 = r2[r1];
     */
    /* JADX WARNING: Missing block: B:10:0x0031, code skipped:
            if (r9 >= 0) goto L_0x003a;
     */
    /* JADX WARNING: Missing block: B:11:0x0033, code skipped:
            r1 = _appendNumeric(r1, r11._qbuf);
     */
    /* JADX WARNING: Missing block: B:12:0x003a, code skipped:
            r1 = _appendNamed(r9, r11._qbuf);
     */
    /* JADX WARNING: Missing block: B:13:0x0040, code skipped:
            r9 = r6 + r1;
     */
    /* JADX WARNING: Missing block: B:14:0x0043, code skipped:
            if (r9 <= r7.length) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:15:0x0045, code skipped:
            r9 = r7.length - r6;
     */
    /* JADX WARNING: Missing block: B:16:0x0047, code skipped:
            if (r9 <= 0) goto L_0x004e;
     */
    /* JADX WARNING: Missing block: B:17:0x0049, code skipped:
            java.lang.System.arraycopy(r11._qbuf, 0, r7, r6, r9);
     */
    /* JADX WARNING: Missing block: B:18:0x004e, code skipped:
            r6 = r0.finishCurrentSegment();
            r1 = r1 - r9;
            java.lang.System.arraycopy(r11._qbuf, r9, r6, 0, r1);
            r7 = r6;
            r6 = r1;
     */
    /* JADX WARNING: Missing block: B:19:0x005b, code skipped:
            java.lang.System.arraycopy(r11._qbuf, 0, r7, r6, r1);
            r6 = r9;
     */
    /* JADX WARNING: Missing block: B:20:0x0061, code skipped:
            r1 = r8;
     */
    public char[] quoteAsString(java.lang.String r12) {
        /*
        r11 = this;
        r0 = r11._text;
        if (r0 != 0) goto L_0x000c;
    L_0x0004:
        r0 = new com.fasterxml.jackson.core.util.TextBuffer;
        r1 = 0;
        r0.<init>(r1);
        r11._text = r0;
    L_0x000c:
        r1 = r0.emptyAndGetCurrentSegment();
        r2 = com.fasterxml.jackson.core.io.CharTypes.get7BitOutputEscapes();
        r3 = r2.length;
        r4 = r12.length();
        r5 = 0;
        r7 = r1;
        r1 = 0;
        r6 = 0;
    L_0x001d:
        if (r1 >= r4) goto L_0x0078;
    L_0x001f:
        r8 = r12.charAt(r1);
        if (r8 >= r3) goto L_0x0063;
    L_0x0025:
        r9 = r2[r8];
        if (r9 == 0) goto L_0x0063;
    L_0x0029:
        r8 = r1 + 1;
        r1 = r12.charAt(r1);
        r9 = r2[r1];
        if (r9 >= 0) goto L_0x003a;
    L_0x0033:
        r9 = r11._qbuf;
        r1 = r11._appendNumeric(r1, r9);
        goto L_0x0040;
    L_0x003a:
        r1 = r11._qbuf;
        r1 = r11._appendNamed(r9, r1);
    L_0x0040:
        r9 = r6 + r1;
        r10 = r7.length;
        if (r9 <= r10) goto L_0x005b;
    L_0x0045:
        r9 = r7.length;
        r9 = r9 - r6;
        if (r9 <= 0) goto L_0x004e;
    L_0x0049:
        r10 = r11._qbuf;
        java.lang.System.arraycopy(r10, r5, r7, r6, r9);
    L_0x004e:
        r6 = r0.finishCurrentSegment();
        r1 = r1 - r9;
        r7 = r11._qbuf;
        java.lang.System.arraycopy(r7, r9, r6, r5, r1);
        r7 = r6;
        r6 = r1;
        goto L_0x0061;
    L_0x005b:
        r10 = r11._qbuf;
        java.lang.System.arraycopy(r10, r5, r7, r6, r1);
        r6 = r9;
    L_0x0061:
        r1 = r8;
        goto L_0x001d;
    L_0x0063:
        r9 = r7.length;
        if (r6 < r9) goto L_0x006c;
    L_0x0066:
        r6 = r0.finishCurrentSegment();
        r7 = r6;
        r6 = 0;
    L_0x006c:
        r9 = r6 + 1;
        r7[r6] = r8;
        r1 = r1 + 1;
        if (r1 < r4) goto L_0x0076;
    L_0x0074:
        r6 = r9;
        goto L_0x0078;
    L_0x0076:
        r6 = r9;
        goto L_0x001f;
    L_0x0078:
        r0.setCurrentLength(r6);
        r12 = r0.contentsAsArray();
        return r12;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.io.JsonStringEncoder.quoteAsString(java.lang.String):char[]");
    }

    /* JADX WARNING: Missing block: B:6:0x0017, code skipped:
            r5 = r4 + 1;
            r4 = r8.charAt(r4);
            r6 = r0[r4];
     */
    /* JADX WARNING: Missing block: B:7:0x001f, code skipped:
            if (r6 >= 0) goto L_0x0028;
     */
    /* JADX WARNING: Missing block: B:8:0x0021, code skipped:
            r4 = _appendNumeric(r4, r7._qbuf);
     */
    /* JADX WARNING: Missing block: B:9:0x0028, code skipped:
            r4 = _appendNamed(r6, r7._qbuf);
     */
    /* JADX WARNING: Missing block: B:10:0x002e, code skipped:
            r9.append(r7._qbuf, 0, r4);
            r4 = r5;
     */
    public void quoteAsString(java.lang.CharSequence r8, java.lang.StringBuilder r9) {
        /*
        r7 = this;
        r0 = com.fasterxml.jackson.core.io.CharTypes.get7BitOutputEscapes();
        r1 = r0.length;
        r2 = r8.length();
        r3 = 0;
        r4 = 0;
    L_0x000b:
        if (r4 >= r2) goto L_0x003c;
    L_0x000d:
        r5 = r8.charAt(r4);
        if (r5 >= r1) goto L_0x0035;
    L_0x0013:
        r6 = r0[r5];
        if (r6 == 0) goto L_0x0035;
    L_0x0017:
        r5 = r4 + 1;
        r4 = r8.charAt(r4);
        r6 = r0[r4];
        if (r6 >= 0) goto L_0x0028;
    L_0x0021:
        r6 = r7._qbuf;
        r4 = r7._appendNumeric(r4, r6);
        goto L_0x002e;
    L_0x0028:
        r4 = r7._qbuf;
        r4 = r7._appendNamed(r6, r4);
    L_0x002e:
        r6 = r7._qbuf;
        r9.append(r6, r3, r4);
        r4 = r5;
        goto L_0x000b;
    L_0x0035:
        r9.append(r5);
        r4 = r4 + 1;
        if (r4 < r2) goto L_0x000d;
    L_0x003c:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.io.JsonStringEncoder.quoteAsString(java.lang.CharSequence, java.lang.StringBuilder):void");
    }

    /* JADX WARNING: Missing block: B:18:0x0043, code skipped:
            if (r4 < r5.length) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:19:0x0045, code skipped:
            r5 = r0.finishCurrentSegment();
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:20:0x004a, code skipped:
            r7 = r2 + 1;
            r2 = r11.charAt(r2);
     */
    /* JADX WARNING: Missing block: B:21:0x0050, code skipped:
            if (r2 > 127) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:22:0x0052, code skipped:
            r4 = _appendByte(r2, r6[r2], r0, r4);
            r5 = r0.getCurrentSegment();
     */
    /* JADX WARNING: Missing block: B:23:0x005c, code skipped:
            r2 = r7;
     */
    /* JADX WARNING: Missing block: B:25:0x0060, code skipped:
            if (r2 > 2047) goto L_0x0072;
     */
    /* JADX WARNING: Missing block: B:26:0x0062, code skipped:
            r6 = r4 + 1;
            r5[r4] = (byte) ((r2 >> 6) | 192);
            r2 = (r2 & 63) | 128;
            r4 = r6;
     */
    /* JADX WARNING: Missing block: B:28:0x0075, code skipped:
            if (r2 < 55296) goto L_0x00d3;
     */
    /* JADX WARNING: Missing block: B:30:0x007a, code skipped:
            if (r2 <= 57343) goto L_0x007d;
     */
    /* JADX WARNING: Missing block: B:32:0x0080, code skipped:
            if (r2 <= 56319) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:33:0x0082, code skipped:
            _illegal(r2);
     */
    /* JADX WARNING: Missing block: B:34:0x0085, code skipped:
            if (r7 < r1) goto L_0x008a;
     */
    /* JADX WARNING: Missing block: B:35:0x0087, code skipped:
            _illegal(r2);
     */
    /* JADX WARNING: Missing block: B:36:0x008a, code skipped:
            r6 = r7 + 1;
            r2 = _convert(r2, r11.charAt(r7));
     */
    /* JADX WARNING: Missing block: B:37:0x0097, code skipped:
            if (r2 <= 1114111) goto L_0x009c;
     */
    /* JADX WARNING: Missing block: B:38:0x0099, code skipped:
            _illegal(r2);
     */
    /* JADX WARNING: Missing block: B:39:0x009c, code skipped:
            r7 = r4 + 1;
            r5[r4] = (byte) ((r2 >> 18) | 240);
     */
    /* JADX WARNING: Missing block: B:40:0x00a6, code skipped:
            if (r7 < r5.length) goto L_0x00ad;
     */
    /* JADX WARNING: Missing block: B:41:0x00a8, code skipped:
            r5 = r0.finishCurrentSegment();
            r7 = 0;
     */
    /* JADX WARNING: Missing block: B:42:0x00ad, code skipped:
            r4 = r7 + 1;
            r5[r7] = (byte) (((r2 >> 12) & 63) | 128);
     */
    /* JADX WARNING: Missing block: B:43:0x00b9, code skipped:
            if (r4 < r5.length) goto L_0x00c1;
     */
    /* JADX WARNING: Missing block: B:44:0x00bb, code skipped:
            r5 = r0.finishCurrentSegment();
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:45:0x00c1, code skipped:
            r7 = r4 + 1;
            r5[r4] = (byte) (((r2 >> 6) & 63) | 128);
            r2 = (r2 & 63) | 128;
            r4 = r7;
            r7 = r6;
     */
    /* JADX WARNING: Missing block: B:46:0x00d3, code skipped:
            r6 = r4 + 1;
            r5[r4] = (byte) ((r2 >> 12) | 224);
     */
    /* JADX WARNING: Missing block: B:47:0x00dd, code skipped:
            if (r6 < r5.length) goto L_0x00e4;
     */
    /* JADX WARNING: Missing block: B:48:0x00df, code skipped:
            r5 = r0.finishCurrentSegment();
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:49:0x00e4, code skipped:
            r4 = r6 + 1;
            r5[r6] = (byte) (((r2 >> 6) & 63) | 128);
            r2 = (r2 & 63) | 128;
     */
    /* JADX WARNING: Missing block: B:51:0x00f4, code skipped:
            if (r4 < r5.length) goto L_0x00fc;
     */
    /* JADX WARNING: Missing block: B:52:0x00f6, code skipped:
            r5 = r0.finishCurrentSegment();
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:53:0x00fc, code skipped:
            r6 = r4 + 1;
            r5[r4] = (byte) r2;
            r4 = r6;
     */
    public byte[] quoteAsUTF8(java.lang.String r11) {
        /*
        r10 = this;
        r0 = r10._bytes;
        if (r0 != 0) goto L_0x000c;
    L_0x0004:
        r0 = new com.fasterxml.jackson.core.util.ByteArrayBuilder;
        r1 = 0;
        r0.<init>(r1);
        r10._bytes = r0;
    L_0x000c:
        r1 = r11.length();
        r2 = r0.resetAndGetFirstSegment();
        r3 = 0;
        r5 = r2;
        r2 = 0;
        r4 = 0;
    L_0x0018:
        if (r2 >= r1) goto L_0x0104;
    L_0x001a:
        r6 = com.fasterxml.jackson.core.io.CharTypes.get7BitOutputEscapes();
    L_0x001e:
        r7 = r11.charAt(r2);
        r8 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        if (r7 > r8) goto L_0x0042;
    L_0x0026:
        r9 = r6[r7];
        if (r9 == 0) goto L_0x002b;
    L_0x002a:
        goto L_0x0042;
    L_0x002b:
        r8 = r5.length;
        if (r4 < r8) goto L_0x0034;
    L_0x002e:
        r4 = r0.finishCurrentSegment();
        r5 = r4;
        r4 = 0;
    L_0x0034:
        r8 = r4 + 1;
        r7 = (byte) r7;
        r5[r4] = r7;
        r2 = r2 + 1;
        if (r2 < r1) goto L_0x0040;
    L_0x003d:
        r4 = r8;
        goto L_0x0104;
    L_0x0040:
        r4 = r8;
        goto L_0x001e;
    L_0x0042:
        r7 = r5.length;
        if (r4 < r7) goto L_0x004a;
    L_0x0045:
        r5 = r0.finishCurrentSegment();
        r4 = 0;
    L_0x004a:
        r7 = r2 + 1;
        r2 = r11.charAt(r2);
        if (r2 > r8) goto L_0x005e;
    L_0x0052:
        r5 = r6[r2];
        r4 = r10._appendByte(r2, r5, r0, r4);
        r5 = r0.getCurrentSegment();
    L_0x005c:
        r2 = r7;
        goto L_0x0018;
    L_0x005e:
        r6 = 2047; // 0x7ff float:2.868E-42 double:1.0114E-320;
        if (r2 > r6) goto L_0x0072;
    L_0x0062:
        r6 = r4 + 1;
        r8 = r2 >> 6;
        r8 = r8 | 192;
        r8 = (byte) r8;
        r5[r4] = r8;
        r2 = r2 & 63;
        r2 = r2 | 128;
        r4 = r6;
        goto L_0x00f3;
    L_0x0072:
        r6 = 55296; // 0xd800 float:7.7486E-41 double:2.732E-319;
        if (r2 < r6) goto L_0x00d3;
    L_0x0077:
        r6 = 57343; // 0xdfff float:8.0355E-41 double:2.8331E-319;
        if (r2 <= r6) goto L_0x007d;
    L_0x007c:
        goto L_0x00d3;
    L_0x007d:
        r6 = 56319; // 0xdbff float:7.892E-41 double:2.78253E-319;
        if (r2 <= r6) goto L_0x0085;
    L_0x0082:
        _illegal(r2);
    L_0x0085:
        if (r7 < r1) goto L_0x008a;
    L_0x0087:
        _illegal(r2);
    L_0x008a:
        r6 = r7 + 1;
        r7 = r11.charAt(r7);
        r2 = _convert(r2, r7);
        r7 = 1114111; // 0x10ffff float:1.561202E-39 double:5.50444E-318;
        if (r2 <= r7) goto L_0x009c;
    L_0x0099:
        _illegal(r2);
    L_0x009c:
        r7 = r4 + 1;
        r8 = r2 >> 18;
        r8 = r8 | 240;
        r8 = (byte) r8;
        r5[r4] = r8;
        r4 = r5.length;
        if (r7 < r4) goto L_0x00ad;
    L_0x00a8:
        r5 = r0.finishCurrentSegment();
        r7 = 0;
    L_0x00ad:
        r4 = r7 + 1;
        r8 = r2 >> 12;
        r8 = r8 & 63;
        r8 = r8 | 128;
        r8 = (byte) r8;
        r5[r7] = r8;
        r7 = r5.length;
        if (r4 < r7) goto L_0x00c1;
    L_0x00bb:
        r4 = r0.finishCurrentSegment();
        r5 = r4;
        r4 = 0;
    L_0x00c1:
        r7 = r4 + 1;
        r8 = r2 >> 6;
        r8 = r8 & 63;
        r8 = r8 | 128;
        r8 = (byte) r8;
        r5[r4] = r8;
        r2 = r2 & 63;
        r2 = r2 | 128;
        r4 = r7;
        r7 = r6;
        goto L_0x00f3;
    L_0x00d3:
        r6 = r4 + 1;
        r8 = r2 >> 12;
        r8 = r8 | 224;
        r8 = (byte) r8;
        r5[r4] = r8;
        r4 = r5.length;
        if (r6 < r4) goto L_0x00e4;
    L_0x00df:
        r5 = r0.finishCurrentSegment();
        r6 = 0;
    L_0x00e4:
        r4 = r6 + 1;
        r8 = r2 >> 6;
        r8 = r8 & 63;
        r8 = r8 | 128;
        r8 = (byte) r8;
        r5[r6] = r8;
        r2 = r2 & 63;
        r2 = r2 | 128;
    L_0x00f3:
        r6 = r5.length;
        if (r4 < r6) goto L_0x00fc;
    L_0x00f6:
        r4 = r0.finishCurrentSegment();
        r5 = r4;
        r4 = 0;
    L_0x00fc:
        r6 = r4 + 1;
        r2 = (byte) r2;
        r5[r4] = r2;
        r4 = r6;
        goto L_0x005c;
    L_0x0104:
        r11 = r10._bytes;
        r11 = r11.completeAndCoalesce(r4);
        return r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.io.JsonStringEncoder.quoteAsUTF8(java.lang.String):byte[]");
    }

    public byte[] encodeAsUTF8(String str) {
        ByteArrayBuilder byteArrayBuilder = this._bytes;
        if (byteArrayBuilder == null) {
            byteArrayBuilder = new ByteArrayBuilder(null);
            this._bytes = byteArrayBuilder;
        }
        int length = str.length();
        byte[] resetAndGetFirstSegment = byteArrayBuilder.resetAndGetFirstSegment();
        byte[] bArr = resetAndGetFirstSegment;
        int length2 = resetAndGetFirstSegment.length;
        int i = 0;
        int i2 = 0;
        loop0:
        while (i < length) {
            byte[] finishCurrentSegment;
            int i3;
            int i4 = i + 1;
            i = str.charAt(i);
            while (i <= 127) {
                if (i2 >= length2) {
                    finishCurrentSegment = byteArrayBuilder.finishCurrentSegment();
                    length2 = finishCurrentSegment.length;
                    bArr = finishCurrentSegment;
                    i2 = 0;
                }
                i3 = i2 + 1;
                bArr[i2] = (byte) i;
                if (i4 >= length) {
                    i2 = i3;
                    break loop0;
                }
                i = i4 + 1;
                char charAt = str.charAt(i4);
                i4 = i;
                char c = charAt;
                i2 = i3;
            }
            if (i2 >= length2) {
                bArr = byteArrayBuilder.finishCurrentSegment();
                length2 = bArr.length;
                i2 = 0;
            }
            if (i < 2048) {
                i3 = i2 + 1;
                bArr[i2] = (byte) ((i >> 6) | 192);
                i2 = i3;
            } else if (i < 55296 || i > 57343) {
                i3 = i2 + 1;
                bArr[i2] = (byte) ((i >> 12) | 224);
                if (i3 >= length2) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    length2 = bArr.length;
                    i3 = 0;
                }
                i2 = i3 + 1;
                bArr[i3] = (byte) (((i >> 6) & 63) | 128);
            } else {
                if (i > 56319) {
                    _illegal(i);
                }
                if (i4 >= length) {
                    _illegal(i);
                }
                i3 = i4 + 1;
                i = _convert(i, str.charAt(i4));
                if (i > 1114111) {
                    _illegal(i);
                }
                i4 = i2 + 1;
                bArr[i2] = (byte) ((i >> 18) | 240);
                if (i4 >= length2) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    length2 = bArr.length;
                    i4 = 0;
                }
                i2 = i4 + 1;
                bArr[i4] = (byte) (((i >> 12) & 63) | 128);
                if (i2 >= length2) {
                    finishCurrentSegment = byteArrayBuilder.finishCurrentSegment();
                    length2 = finishCurrentSegment.length;
                    bArr = finishCurrentSegment;
                    i2 = 0;
                }
                i4 = i2 + 1;
                bArr[i2] = (byte) (((i >> 6) & 63) | 128);
                i2 = i4;
                i4 = i3;
            }
            if (i2 >= length2) {
                finishCurrentSegment = byteArrayBuilder.finishCurrentSegment();
                length2 = finishCurrentSegment.length;
                bArr = finishCurrentSegment;
                i2 = 0;
            }
            i3 = i2 + 1;
            bArr[i2] = (byte) ((i & 63) | 128);
            i = i4;
            i2 = i3;
        }
        return this._bytes.completeAndCoalesce(i2);
    }

    private int _appendNumeric(int i, char[] cArr) {
        cArr[1] = 'u';
        cArr[4] = HC[i >> 4];
        cArr[5] = HC[i & 15];
        return 6;
    }

    private int _appendNamed(int i, char[] cArr) {
        cArr[1] = (char) i;
        return 2;
    }

    private int _appendByte(int i, int i2, ByteArrayBuilder byteArrayBuilder, int i3) {
        byteArrayBuilder.setCurrentSegmentLength(i3);
        byteArrayBuilder.append(92);
        if (i2 < 0) {
            byteArrayBuilder.append(117);
            if (i > 255) {
                i2 = i >> 8;
                byteArrayBuilder.append(HB[i2 >> 4]);
                byteArrayBuilder.append(HB[i2 & 15]);
                i &= 255;
            } else {
                byteArrayBuilder.append(48);
                byteArrayBuilder.append(48);
            }
            byteArrayBuilder.append(HB[i >> 4]);
            byteArrayBuilder.append(HB[i & 15]);
        } else {
            byteArrayBuilder.append((byte) i2);
        }
        return byteArrayBuilder.getCurrentSegmentLength();
    }

    private static int _convert(int i, int i2) {
        if (i2 >= 56320 && i2 <= 57343) {
            return (((i - 55296) << 10) + 65536) + (i2 - 56320);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Broken surrogate pair: first char 0x");
        stringBuilder.append(Integer.toHexString(i));
        stringBuilder.append(", second 0x");
        stringBuilder.append(Integer.toHexString(i2));
        stringBuilder.append("; illegal combination");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    private static void _illegal(int i) {
        throw new IllegalArgumentException(UTF8Writer.illegalSurrogateDesc(i));
    }
}
