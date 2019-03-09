package com.google.zxing.oned.rss.expanded.decoders;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;

final class GeneralAppIdDecoder {
    private final StringBuilder buffer = new StringBuilder();
    private final CurrentParsingState current = new CurrentParsingState();
    private final BitArray information;

    GeneralAppIdDecoder(BitArray bitArray) {
        this.information = bitArray;
    }

    /* Access modifiers changed, original: 0000 */
    public String decodeAllCodes(StringBuilder stringBuilder, int i) throws NotFoundException, FormatException {
        String str = null;
        while (true) {
            DecodedInformation decodeGeneralPurposeField = decodeGeneralPurposeField(i, str);
            String parseFieldsInGeneralPurpose = FieldParser.parseFieldsInGeneralPurpose(decodeGeneralPurposeField.getNewString());
            if (parseFieldsInGeneralPurpose != null) {
                stringBuilder.append(parseFieldsInGeneralPurpose);
            }
            parseFieldsInGeneralPurpose = decodeGeneralPurposeField.isRemaining() ? String.valueOf(decodeGeneralPurposeField.getRemainingValue()) : null;
            if (i == decodeGeneralPurposeField.getNewPosition()) {
                return stringBuilder.toString();
            }
            i = decodeGeneralPurposeField.getNewPosition();
            str = parseFieldsInGeneralPurpose;
        }
    }

    private boolean isStillNumeric(int i) {
        if (i + 7 <= this.information.getSize()) {
            int i2 = i;
            while (true) {
                int i3 = i + 3;
                if (i2 >= i3) {
                    return this.information.get(i3);
                }
                if (this.information.get(i2)) {
                    return true;
                }
                i2++;
            }
        } else if (i + 4 <= this.information.getSize()) {
            return true;
        } else {
            return false;
        }
    }

    private DecodedNumeric decodeNumeric(int i) throws FormatException {
        int i2 = i + 7;
        if (i2 > this.information.getSize()) {
            i = extractNumericValueFromBitArray(i, 4);
            if (i == 0) {
                return new DecodedNumeric(this.information.getSize(), 10, 10);
            }
            return new DecodedNumeric(this.information.getSize(), i - 1, 10);
        }
        i = extractNumericValueFromBitArray(i, 7) - 8;
        return new DecodedNumeric(i2, i / 11, i % 11);
    }

    /* Access modifiers changed, original: 0000 */
    public int extractNumericValueFromBitArray(int i, int i2) {
        return extractNumericValueFromBitArray(this.information, i, i2);
    }

    static int extractNumericValueFromBitArray(BitArray bitArray, int i, int i2) {
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            if (bitArray.get(i + i4)) {
                i3 |= 1 << ((i2 - i4) - 1);
            }
        }
        return i3;
    }

    /* Access modifiers changed, original: 0000 */
    public DecodedInformation decodeGeneralPurposeField(int i, String str) throws FormatException {
        this.buffer.setLength(0);
        if (str != null) {
            this.buffer.append(str);
        }
        this.current.setPosition(i);
        DecodedInformation parseBlocks = parseBlocks();
        if (parseBlocks == null || !parseBlocks.isRemaining()) {
            return new DecodedInformation(this.current.getPosition(), this.buffer.toString());
        }
        return new DecodedInformation(this.current.getPosition(), this.buffer.toString(), parseBlocks.getRemainingValue());
    }

    private DecodedInformation parseBlocks() throws FormatException {
        BlockParsedResult parseAlphaBlock;
        boolean isFinished;
        do {
            int position = this.current.getPosition();
            if (this.current.isAlpha()) {
                parseAlphaBlock = parseAlphaBlock();
                isFinished = parseAlphaBlock.isFinished();
            } else if (this.current.isIsoIec646()) {
                parseAlphaBlock = parseIsoIec646Block();
                isFinished = parseAlphaBlock.isFinished();
            } else {
                parseAlphaBlock = parseNumericBlock();
                isFinished = parseAlphaBlock.isFinished();
            }
            if ((position != this.current.getPosition() ? 1 : null) == null && !isFinished) {
                break;
            }
        } while (!isFinished);
        return parseAlphaBlock.getDecodedInformation();
    }

    private BlockParsedResult parseNumericBlock() throws FormatException {
        while (isStillNumeric(this.current.getPosition())) {
            DecodedNumeric decodeNumeric = decodeNumeric(this.current.getPosition());
            this.current.setPosition(decodeNumeric.getNewPosition());
            if (decodeNumeric.isFirstDigitFNC1()) {
                DecodedInformation decodedInformation;
                if (decodeNumeric.isSecondDigitFNC1()) {
                    decodedInformation = new DecodedInformation(this.current.getPosition(), this.buffer.toString());
                } else {
                    decodedInformation = new DecodedInformation(this.current.getPosition(), this.buffer.toString(), decodeNumeric.getSecondDigit());
                }
                return new BlockParsedResult(decodedInformation, true);
            }
            this.buffer.append(decodeNumeric.getFirstDigit());
            if (decodeNumeric.isSecondDigitFNC1()) {
                return new BlockParsedResult(new DecodedInformation(this.current.getPosition(), this.buffer.toString()), true);
            }
            this.buffer.append(decodeNumeric.getSecondDigit());
        }
        if (isNumericToAlphaNumericLatch(this.current.getPosition())) {
            this.current.setAlpha();
            this.current.incrementPosition(4);
        }
        return new BlockParsedResult(false);
    }

    private BlockParsedResult parseIsoIec646Block() throws FormatException {
        while (isStillIsoIec646(this.current.getPosition())) {
            DecodedChar decodeIsoIec646 = decodeIsoIec646(this.current.getPosition());
            this.current.setPosition(decodeIsoIec646.getNewPosition());
            if (decodeIsoIec646.isFNC1()) {
                return new BlockParsedResult(new DecodedInformation(this.current.getPosition(), this.buffer.toString()), true);
            }
            this.buffer.append(decodeIsoIec646.getValue());
        }
        if (isAlphaOr646ToNumericLatch(this.current.getPosition())) {
            this.current.incrementPosition(3);
            this.current.setNumeric();
        } else if (isAlphaTo646ToAlphaLatch(this.current.getPosition())) {
            if (this.current.getPosition() + 5 < this.information.getSize()) {
                this.current.incrementPosition(5);
            } else {
                this.current.setPosition(this.information.getSize());
            }
            this.current.setAlpha();
        }
        return new BlockParsedResult(false);
    }

    private BlockParsedResult parseAlphaBlock() {
        while (isStillAlpha(this.current.getPosition())) {
            DecodedChar decodeAlphanumeric = decodeAlphanumeric(this.current.getPosition());
            this.current.setPosition(decodeAlphanumeric.getNewPosition());
            if (decodeAlphanumeric.isFNC1()) {
                return new BlockParsedResult(new DecodedInformation(this.current.getPosition(), this.buffer.toString()), true);
            }
            this.buffer.append(decodeAlphanumeric.getValue());
        }
        if (isAlphaOr646ToNumericLatch(this.current.getPosition())) {
            this.current.incrementPosition(3);
            this.current.setNumeric();
        } else if (isAlphaTo646ToAlphaLatch(this.current.getPosition())) {
            if (this.current.getPosition() + 5 < this.information.getSize()) {
                this.current.incrementPosition(5);
            } else {
                this.current.setPosition(this.information.getSize());
            }
            this.current.setIsoIec646();
        }
        return new BlockParsedResult(false);
    }

    private boolean isStillIsoIec646(int i) {
        if (i + 5 > this.information.getSize()) {
            return false;
        }
        int extractNumericValueFromBitArray = extractNumericValueFromBitArray(i, 5);
        if (extractNumericValueFromBitArray >= 5 && extractNumericValueFromBitArray < 16) {
            return true;
        }
        if (i + 7 > this.information.getSize()) {
            return false;
        }
        int extractNumericValueFromBitArray2 = extractNumericValueFromBitArray(i, 7);
        if (extractNumericValueFromBitArray2 >= 64 && extractNumericValueFromBitArray2 < 116) {
            return true;
        }
        if (i + 8 > this.information.getSize()) {
            return false;
        }
        i = extractNumericValueFromBitArray(i, 8);
        if (i < 232 || i >= 253) {
            return false;
        }
        return true;
    }

    private DecodedChar decodeIsoIec646(int i) throws FormatException {
        int extractNumericValueFromBitArray = extractNumericValueFromBitArray(i, 5);
        if (extractNumericValueFromBitArray == 15) {
            return new DecodedChar(i + 5, '$');
        }
        if (extractNumericValueFromBitArray >= 5 && extractNumericValueFromBitArray < 15) {
            return new DecodedChar(i + 5, (char) ((extractNumericValueFromBitArray + 48) - 5));
        }
        extractNumericValueFromBitArray = extractNumericValueFromBitArray(i, 7);
        if (extractNumericValueFromBitArray >= 64 && extractNumericValueFromBitArray < 90) {
            return new DecodedChar(i + 7, (char) (extractNumericValueFromBitArray + 1));
        }
        if (extractNumericValueFromBitArray >= 90 && extractNumericValueFromBitArray < 116) {
            return new DecodedChar(i + 7, (char) (extractNumericValueFromBitArray + 7));
        }
        char c;
        switch (extractNumericValueFromBitArray(i, 8)) {
            case 232:
                c = '!';
                break;
            case 233:
                c = '\"';
                break;
            case 234:
                c = '%';
                break;
            case 235:
                c = '&';
                break;
            case 236:
                c = '\'';
                break;
            case 237:
                c = '(';
                break;
            case 238:
                c = ')';
                break;
            case Errors.GUEST_AUTH_ERROR_CODE /*239*/:
                c = '*';
                break;
            case 240:
                c = '+';
                break;
            case 241:
                c = ',';
                break;
            case 242:
                c = '-';
                break;
            case 243:
                c = '.';
                break;
            case 244:
                c = '/';
                break;
            case 245:
                c = ':';
                break;
            case 246:
                c = ';';
                break;
            case 247:
                c = '<';
                break;
            case 248:
                c = '=';
                break;
            case 249:
                c = '>';
                break;
            case Callback.DEFAULT_SWIPE_ANIMATION_DURATION /*250*/:
                c = '?';
                break;
            case 251:
                c = '_';
                break;
            case 252:
                c = ' ';
                break;
            default:
                throw FormatException.getFormatInstance();
        }
        return new DecodedChar(i + 8, c);
    }

    private boolean isStillAlpha(int i) {
        if (i + 5 > this.information.getSize()) {
            return false;
        }
        int extractNumericValueFromBitArray = extractNumericValueFromBitArray(i, 5);
        if (extractNumericValueFromBitArray >= 5 && extractNumericValueFromBitArray < 16) {
            return true;
        }
        if (i + 6 > this.information.getSize()) {
            return false;
        }
        i = extractNumericValueFromBitArray(i, 6);
        if (i < 16 || i >= 63) {
            return false;
        }
        return true;
    }

    private DecodedChar decodeAlphanumeric(int i) {
        int extractNumericValueFromBitArray = extractNumericValueFromBitArray(i, 5);
        if (extractNumericValueFromBitArray == 15) {
            return new DecodedChar(i + 5, '$');
        }
        if (extractNumericValueFromBitArray >= 5 && extractNumericValueFromBitArray < 15) {
            return new DecodedChar(i + 5, (char) ((extractNumericValueFromBitArray + 48) - 5));
        }
        extractNumericValueFromBitArray = extractNumericValueFromBitArray(i, 6);
        if (extractNumericValueFromBitArray >= 32 && extractNumericValueFromBitArray < 58) {
            return new DecodedChar(i + 6, (char) (extractNumericValueFromBitArray + 33));
        }
        char c;
        switch (extractNumericValueFromBitArray) {
            case 58:
                c = '*';
                break;
            case 59:
                c = ',';
                break;
            case 60:
                c = '-';
                break;
            case 61:
                c = '.';
                break;
            case 62:
                c = '/';
                break;
            default:
                StringBuilder stringBuilder = new StringBuilder("Decoding invalid alphanumeric value: ");
                stringBuilder.append(extractNumericValueFromBitArray);
                throw new IllegalStateException(stringBuilder.toString());
        }
        return new DecodedChar(i + 6, c);
    }

    private boolean isAlphaTo646ToAlphaLatch(int i) {
        if (i + 1 > this.information.getSize()) {
            return false;
        }
        for (int i2 = 0; i2 < 5; i2++) {
            int i3 = i2 + i;
            if (i3 >= this.information.getSize()) {
                break;
            }
            if (i2 == 2) {
                if (!this.information.get(i + 2)) {
                    return false;
                }
            } else if (this.information.get(i3)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAlphaOr646ToNumericLatch(int i) {
        int i2 = i + 3;
        if (i2 > this.information.getSize()) {
            return false;
        }
        while (i < i2) {
            if (this.information.get(i)) {
                return false;
            }
            i++;
        }
        return true;
    }

    private boolean isNumericToAlphaNumericLatch(int i) {
        if (i + 1 > this.information.getSize()) {
            return false;
        }
        int i2 = 0;
        while (i2 < 4) {
            int i3 = i2 + i;
            if (i3 >= this.information.getSize()) {
                break;
            } else if (this.information.get(i3)) {
                return false;
            } else {
                i2++;
            }
        }
        return true;
    }
}
