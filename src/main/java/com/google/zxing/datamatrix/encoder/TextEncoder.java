package com.google.zxing.datamatrix.encoder;

final class TextEncoder extends C40Encoder {
    public int getEncodingMode() {
        return 2;
    }

    TextEncoder() {
    }

    /* Access modifiers changed, original: 0000 */
    public int encodeChar(char c, StringBuilder stringBuilder) {
        if (c == ' ') {
            stringBuilder.append(3);
            return 1;
        } else if (c >= '0' && c <= '9') {
            stringBuilder.append((char) ((c - 48) + 4));
            return 1;
        } else if (c >= 'a' && c <= 'z') {
            stringBuilder.append((char) ((c - 97) + 14));
            return 1;
        } else if (c >= 0 && c <= 31) {
            stringBuilder.append(0);
            stringBuilder.append(c);
            return 2;
        } else if (c >= '!' && c <= '/') {
            stringBuilder.append(1);
            stringBuilder.append((char) (c - 33));
            return 2;
        } else if (c >= ':' && c <= '@') {
            stringBuilder.append(1);
            stringBuilder.append((char) ((c - 58) + 15));
            return 2;
        } else if (c >= '[' && c <= '_') {
            stringBuilder.append(1);
            stringBuilder.append((char) ((c - 91) + 22));
            return 2;
        } else if (c == '`') {
            stringBuilder.append(2);
            stringBuilder.append((char) (c - 96));
            return 2;
        } else if (c >= 'A' && c <= 'Z') {
            stringBuilder.append(2);
            stringBuilder.append((char) ((c - 65) + 1));
            return 2;
        } else if (c >= '{' && c <= 127) {
            stringBuilder.append(2);
            stringBuilder.append((char) ((c - 123) + 27));
            return 2;
        } else if (c >= 128) {
            stringBuilder.append("\u0001\u001e");
            return encodeChar((char) (c - 128), stringBuilder) + 2;
        } else {
            HighLevelEncoder.illegalCharacter(c);
            return -1;
        }
    }
}
