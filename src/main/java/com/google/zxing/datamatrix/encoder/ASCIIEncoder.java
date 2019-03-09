package com.google.zxing.datamatrix.encoder;

final class ASCIIEncoder implements Encoder {
    public int getEncodingMode() {
        return 0;
    }

    ASCIIEncoder() {
    }

    public void encode(EncoderContext encoderContext) {
        if (HighLevelEncoder.determineConsecutiveDigitCount(encoderContext.getMessage(), encoderContext.pos) >= 2) {
            encoderContext.writeCodeword(encodeASCIIDigits(encoderContext.getMessage().charAt(encoderContext.pos), encoderContext.getMessage().charAt(encoderContext.pos + 1)));
            encoderContext.pos += 2;
            return;
        }
        char currentChar = encoderContext.getCurrentChar();
        int lookAheadTest = HighLevelEncoder.lookAheadTest(encoderContext.getMessage(), encoderContext.pos, getEncodingMode());
        if (lookAheadTest != getEncodingMode()) {
            switch (lookAheadTest) {
                case 1:
                    encoderContext.writeCodeword(230);
                    encoderContext.signalEncoderChange(1);
                    return;
                case 2:
                    encoderContext.writeCodeword(239);
                    encoderContext.signalEncoderChange(2);
                    return;
                case 3:
                    encoderContext.writeCodeword(238);
                    encoderContext.signalEncoderChange(3);
                    return;
                case 4:
                    encoderContext.writeCodeword(240);
                    encoderContext.signalEncoderChange(4);
                    return;
                case 5:
                    encoderContext.writeCodeword(231);
                    encoderContext.signalEncoderChange(5);
                    return;
                default:
                    StringBuilder stringBuilder = new StringBuilder("Illegal mode: ");
                    stringBuilder.append(lookAheadTest);
                    throw new IllegalStateException(stringBuilder.toString());
            }
        } else if (HighLevelEncoder.isExtendedASCII(currentChar)) {
            encoderContext.writeCodeword(235);
            encoderContext.writeCodeword((char) ((currentChar - 128) + 1));
            encoderContext.pos++;
        } else {
            encoderContext.writeCodeword((char) (currentChar + 1));
            encoderContext.pos++;
        }
    }

    private static char encodeASCIIDigits(char c, char c2) {
        if (HighLevelEncoder.isDigit(c) && HighLevelEncoder.isDigit(c2)) {
            return (char) ((((c - 48) * 10) + (c2 - 48)) + 130);
        }
        StringBuilder stringBuilder = new StringBuilder("not digits: ");
        stringBuilder.append(c);
        stringBuilder.append(c2);
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
