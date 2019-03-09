package com.google.zxing.datamatrix.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;

final class BitMatrixParser {
    private final BitMatrix mappingBitMatrix;
    private final BitMatrix readMappingMatrix;
    private final Version version;

    BitMatrixParser(BitMatrix bitMatrix) throws FormatException {
        int height = bitMatrix.getHeight();
        if (height < 8 || height > Errors.ALREADY_UNFAVORITED || (height & 1) != 0) {
            throw FormatException.getFormatInstance();
        }
        this.version = readVersion(bitMatrix);
        this.mappingBitMatrix = extractDataRegion(bitMatrix);
        this.readMappingMatrix = new BitMatrix(this.mappingBitMatrix.getWidth(), this.mappingBitMatrix.getHeight());
    }

    /* Access modifiers changed, original: 0000 */
    public Version getVersion() {
        return this.version;
    }

    private static Version readVersion(BitMatrix bitMatrix) throws FormatException {
        return Version.getVersionForDimensions(bitMatrix.getHeight(), bitMatrix.getWidth());
    }

    /* Access modifiers changed, original: 0000 */
    public byte[] readCodewords() throws FormatException {
        byte[] bArr = new byte[this.version.getTotalCodewords()];
        int height = this.mappingBitMatrix.getHeight();
        int width = this.mappingBitMatrix.getWidth();
        int i = 4;
        int i2 = 0;
        Object obj = null;
        int i3 = 0;
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        while (true) {
            if (i == height && i2 == 0 && obj == null) {
                int i4 = i3 + 1;
                bArr[i3] = (byte) readCorner1(height, width);
                i -= 2;
                i2 += 2;
                i3 = i4;
                obj = 1;
            } else {
                int i5 = height - 2;
                if (i == i5 && i2 == 0 && (width & 3) != 0 && obj2 == null) {
                    int i6 = i3 + 1;
                    bArr[i3] = (byte) readCorner2(height, width);
                    i -= 2;
                    i2 += 2;
                    i3 = i6;
                    obj2 = 1;
                } else if (i == height + 4 && i2 == 2 && (width & 7) == 0 && obj3 == null) {
                    int i7 = i3 + 1;
                    bArr[i3] = (byte) readCorner3(height, width);
                    i -= 2;
                    i2 += 2;
                    i3 = i7;
                    obj3 = 1;
                } else if (i == i5 && i2 == 0 && (width & 7) == 4 && obj4 == null) {
                    int i8 = i3 + 1;
                    bArr[i3] = (byte) readCorner4(height, width);
                    i -= 2;
                    i2 += 2;
                    i3 = i8;
                    obj4 = 1;
                } else {
                    int i9;
                    do {
                        if (i < height && i2 >= 0 && !this.readMappingMatrix.get(i2, i)) {
                            i9 = i3 + 1;
                            bArr[i3] = (byte) readUtah(i, i2, height, width);
                            i3 = i9;
                        }
                        i -= 2;
                        i2 += 2;
                        if (i < 0) {
                            break;
                        }
                    } while (i2 < width);
                    i++;
                    i2 += 3;
                    do {
                        if (i >= 0 && i2 < width && !this.readMappingMatrix.get(i2, i)) {
                            i9 = i3 + 1;
                            bArr[i3] = (byte) readUtah(i, i2, height, width);
                            i3 = i9;
                        }
                        i += 2;
                        i2 -= 2;
                        if (i >= height) {
                            break;
                        }
                    } while (i2 >= 0);
                    i += 3;
                    i2++;
                }
            }
            if (i >= height && i2 >= width) {
                break;
            }
        }
        if (i3 == this.version.getTotalCodewords()) {
            return bArr;
        }
        throw FormatException.getFormatInstance();
    }

    private boolean readModule(int i, int i2, int i3, int i4) {
        if (i < 0) {
            i += i3;
            i2 += 4 - ((i3 + 4) & 7);
        }
        if (i2 < 0) {
            i2 += i4;
            i += 4 - ((i4 + 4) & 7);
        }
        this.readMappingMatrix.set(i2, i);
        return this.mappingBitMatrix.get(i2, i);
    }

    private int readUtah(int i, int i2, int i3, int i4) {
        int i5 = i - 2;
        int i6 = i2 - 2;
        int readModule = readModule(i5, i6, i3, i4) << 1;
        int i7 = i2 - 1;
        if (readModule(i5, i7, i3, i4)) {
            readModule |= 1;
        }
        i5 = readModule << 1;
        readModule = i - 1;
        if (readModule(readModule, i6, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(readModule, i7, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(readModule, i2, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i, i6, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        if (readModule(i, i7, i3, i4)) {
            i5 |= 1;
        }
        i5 <<= 1;
        return readModule(i, i2, i3, i4) ? i5 | 1 : i5;
    }

    private int readCorner1(int i, int i2) {
        int i3 = i - 1;
        int readModule = readModule(i3, 0, i, i2) << 1;
        if (readModule(i3, 1, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(i3, 2, i, i2)) {
            readModule |= 1;
        }
        i3 = readModule << 1;
        if (readModule(0, i2 - 2, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        readModule = i2 - 1;
        if (readModule(0, readModule, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, readModule, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(2, readModule, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        return readModule(3, readModule, i, i2) ? i3 | 1 : i3;
    }

    private int readCorner2(int i, int i2) {
        int readModule = readModule(i - 3, 0, i, i2) << 1;
        if (readModule(i - 2, 0, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(i - 1, 0, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(0, i2 - 4, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(0, i2 - 3, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(0, i2 - 2, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        int i3 = i2 - 1;
        if (readModule(0, i3, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        return readModule(1, i3, i, i2) ? readModule | 1 : readModule;
    }

    private int readCorner3(int i, int i2) {
        int i3 = i - 1;
        int readModule = readModule(i3, 0, i, i2) << 1;
        int i4 = i2 - 1;
        if (readModule(i3, i4, i, i2)) {
            readModule |= 1;
        }
        i3 = readModule << 1;
        readModule = i2 - 3;
        if (readModule(0, readModule, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        int i5 = i2 - 2;
        if (readModule(0, i5, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(0, i4, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, readModule, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        if (readModule(1, i5, i, i2)) {
            i3 |= 1;
        }
        i3 <<= 1;
        return readModule(1, i4, i, i2) ? i3 | 1 : i3;
    }

    private int readCorner4(int i, int i2) {
        int readModule = readModule(i - 3, 0, i, i2) << 1;
        if (readModule(i - 2, 0, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(i - 1, 0, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(0, i2 - 2, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        int i3 = i2 - 1;
        if (readModule(0, i3, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(1, i3, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        if (readModule(2, i3, i, i2)) {
            readModule |= 1;
        }
        readModule <<= 1;
        return readModule(3, i3, i, i2) ? readModule | 1 : readModule;
    }

    private BitMatrix extractDataRegion(BitMatrix bitMatrix) {
        int symbolSizeRows = this.version.getSymbolSizeRows();
        int symbolSizeColumns = this.version.getSymbolSizeColumns();
        if (bitMatrix.getHeight() == symbolSizeRows) {
            int dataRegionSizeRows = this.version.getDataRegionSizeRows();
            int dataRegionSizeColumns = this.version.getDataRegionSizeColumns();
            symbolSizeRows /= dataRegionSizeRows;
            symbolSizeColumns /= dataRegionSizeColumns;
            BitMatrix bitMatrix2 = new BitMatrix(symbolSizeColumns * dataRegionSizeColumns, symbolSizeRows * dataRegionSizeRows);
            for (int i = 0; i < symbolSizeRows; i++) {
                BitMatrix bitMatrix3;
                int i2 = i * dataRegionSizeRows;
                for (int i3 = 0; i3 < symbolSizeColumns; i3++) {
                    int i4 = i3 * dataRegionSizeColumns;
                    for (int i5 = 0; i5 < dataRegionSizeRows; i5++) {
                        int i6 = (((dataRegionSizeRows + 2) * i) + 1) + i5;
                        int i7 = i2 + i5;
                        for (int i8 = 0; i8 < dataRegionSizeColumns; i8++) {
                            if (bitMatrix.get((((dataRegionSizeColumns + 2) * i3) + 1) + i8, i6)) {
                                bitMatrix2.set(i4 + i8, i7);
                            }
                        }
                        bitMatrix3 = bitMatrix;
                    }
                    bitMatrix3 = bitMatrix;
                }
                bitMatrix3 = bitMatrix;
            }
            return bitMatrix2;
        }
        throw new IllegalArgumentException("Dimension of bitMarix must match the version size");
    }
}
