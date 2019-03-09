package com.fasterxml.jackson.core.sym;

import com.fasterxml.jackson.core.JsonFactory.Feature;
import com.fasterxml.jackson.core.util.InternCache;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public final class ByteQuadsCanonicalizer {
    private static final int DEFAULT_T_SIZE = 64;
    static final int MAX_ENTRIES_FOR_REUSE = 6000;
    private static final int MAX_T_SIZE = 65536;
    private static final int MIN_HASH_SIZE = 16;
    private static final int MULT = 33;
    private static final int MULT2 = 65599;
    private static final int MULT3 = 31;
    private int _count;
    private final boolean _failOnDoS;
    private int[] _hashArea;
    private boolean _hashShared;
    private int _hashSize;
    private boolean _intern;
    private int _longNameOffset;
    private String[] _names;
    private transient boolean _needRehash;
    private final ByteQuadsCanonicalizer _parent;
    private int _secondaryStart;
    private final int _seed;
    private int _spilloverEnd;
    private final AtomicReference<TableInfo> _tableInfo;
    private int _tertiaryShift;
    private int _tertiaryStart;

    private static final class TableInfo {
        public final int count;
        public final int longNameOffset;
        public final int[] mainHash;
        public final String[] names;
        public final int size;
        public final int spilloverEnd;
        public final int tertiaryShift;

        public TableInfo(int i, int i2, int i3, int[] iArr, String[] strArr, int i4, int i5) {
            this.size = i;
            this.count = i2;
            this.tertiaryShift = i3;
            this.mainHash = iArr;
            this.names = strArr;
            this.spilloverEnd = i4;
            this.longNameOffset = i5;
        }

        public TableInfo(ByteQuadsCanonicalizer byteQuadsCanonicalizer) {
            this.size = byteQuadsCanonicalizer._hashSize;
            this.count = byteQuadsCanonicalizer._count;
            this.tertiaryShift = byteQuadsCanonicalizer._tertiaryShift;
            this.mainHash = byteQuadsCanonicalizer._hashArea;
            this.names = byteQuadsCanonicalizer._names;
            this.spilloverEnd = byteQuadsCanonicalizer._spilloverEnd;
            this.longNameOffset = byteQuadsCanonicalizer._longNameOffset;
        }

        public static TableInfo createInitial(int i) {
            int i2 = i << 3;
            return new TableInfo(i, 0, ByteQuadsCanonicalizer._calcTertiaryShift(i), new int[i2], new String[(i << 1)], i2 - i, i2);
        }
    }

    static int _calcTertiaryShift(int i) {
        i >>= 2;
        return i < 64 ? 4 : i <= 256 ? 5 : i <= 1024 ? 6 : 7;
    }

    private ByteQuadsCanonicalizer(int i, boolean z, int i2, boolean z2) {
        this._parent = null;
        this._seed = i2;
        this._intern = z;
        this._failOnDoS = z2;
        int i3 = 16;
        if (i >= 16) {
            if (((i - 1) & i) != 0) {
                while (i3 < i) {
                    i3 += i3;
                }
            } else {
                i3 = i;
            }
        }
        this._tableInfo = new AtomicReference(TableInfo.createInitial(i3));
    }

    private ByteQuadsCanonicalizer(ByteQuadsCanonicalizer byteQuadsCanonicalizer, boolean z, int i, boolean z2, TableInfo tableInfo) {
        this._parent = byteQuadsCanonicalizer;
        this._seed = i;
        this._intern = z;
        this._failOnDoS = z2;
        this._tableInfo = null;
        this._count = tableInfo.count;
        this._hashSize = tableInfo.size;
        this._secondaryStart = this._hashSize << 2;
        this._tertiaryStart = this._secondaryStart + (this._secondaryStart >> 1);
        this._tertiaryShift = tableInfo.tertiaryShift;
        this._hashArea = tableInfo.mainHash;
        this._names = tableInfo.names;
        this._spilloverEnd = tableInfo.spilloverEnd;
        this._longNameOffset = tableInfo.longNameOffset;
        this._needRehash = false;
        this._hashShared = true;
    }

    public static ByteQuadsCanonicalizer createRoot() {
        long currentTimeMillis = System.currentTimeMillis();
        return createRoot((((int) currentTimeMillis) + ((int) (currentTimeMillis >>> 32))) | 1);
    }

    protected static ByteQuadsCanonicalizer createRoot(int i) {
        return new ByteQuadsCanonicalizer(64, true, i, true);
    }

    public ByteQuadsCanonicalizer makeChild(int i) {
        return new ByteQuadsCanonicalizer(this, Feature.INTERN_FIELD_NAMES.enabledIn(i), this._seed, Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.enabledIn(i), (TableInfo) this._tableInfo.get());
    }

    public void release() {
        if (this._parent != null && maybeDirty()) {
            this._parent.mergeChild(new TableInfo(this));
            this._hashShared = true;
        }
    }

    private void mergeChild(TableInfo tableInfo) {
        int i = tableInfo.count;
        TableInfo tableInfo2 = (TableInfo) this._tableInfo.get();
        if (i != tableInfo2.count) {
            Object tableInfo3;
            if (i > MAX_ENTRIES_FOR_REUSE) {
                tableInfo3 = TableInfo.createInitial(64);
            }
            this._tableInfo.compareAndSet(tableInfo2, tableInfo3);
        }
    }

    public int size() {
        if (this._tableInfo != null) {
            return ((TableInfo) this._tableInfo.get()).count;
        }
        return this._count;
    }

    public int bucketCount() {
        return this._hashSize;
    }

    public boolean maybeDirty() {
        return this._hashShared ^ 1;
    }

    public int hashSeed() {
        return this._seed;
    }

    public int primaryCount() {
        int i = this._secondaryStart;
        int i2 = 0;
        for (int i3 = 3; i3 < i; i3 += 4) {
            if (this._hashArea[i3] != 0) {
                i2++;
            }
        }
        return i2;
    }

    public int secondaryCount() {
        int i = this._tertiaryStart;
        int i2 = 0;
        for (int i3 = this._secondaryStart + 3; i3 < i; i3 += 4) {
            if (this._hashArea[i3] != 0) {
                i2++;
            }
        }
        return i2;
    }

    public int tertiaryCount() {
        int i = this._tertiaryStart + 3;
        int i2 = this._hashSize + i;
        int i3 = 0;
        while (i < i2) {
            if (this._hashArea[i] != 0) {
                i3++;
            }
            i += 4;
        }
        return i3;
    }

    public int spilloverCount() {
        return (this._spilloverEnd - _spilloverStart()) >> 2;
    }

    public int totalCount() {
        int i = 3;
        int i2 = this._hashSize << 3;
        int i3 = 0;
        while (i < i2) {
            if (this._hashArea[i] != 0) {
                i3++;
            }
            i += 4;
        }
        return i3;
    }

    public String toString() {
        int primaryCount = primaryCount();
        int secondaryCount = secondaryCount();
        int tertiaryCount = tertiaryCount();
        int spilloverCount = spilloverCount();
        int totalCount = totalCount();
        return String.format("[%s: size=%d, hashSize=%d, %d/%d/%d/%d pri/sec/ter/spill (=%s), total:%d]", new Object[]{getClass().getName(), Integer.valueOf(this._count), Integer.valueOf(this._hashSize), Integer.valueOf(primaryCount), Integer.valueOf(secondaryCount), Integer.valueOf(tertiaryCount), Integer.valueOf(spilloverCount), Integer.valueOf(((primaryCount + secondaryCount) + tertiaryCount) + spilloverCount), Integer.valueOf(totalCount)});
    }

    public String findName(int i) {
        int _calcOffset = _calcOffset(calcHash(i));
        int[] iArr = this._hashArea;
        int i2 = iArr[_calcOffset + 3];
        if (i2 == 1) {
            if (iArr[_calcOffset] == i) {
                return this._names[_calcOffset >> 2];
            }
        } else if (i2 == 0) {
            return null;
        }
        i2 = this._secondaryStart + ((_calcOffset >> 3) << 2);
        int i3 = iArr[i2 + 3];
        if (i3 == 1) {
            if (iArr[i2] == i) {
                return this._names[i2 >> 2];
            }
        } else if (i3 == 0) {
            return null;
        }
        return _findSecondary(_calcOffset, i);
    }

    public String findName(int i, int i2) {
        int _calcOffset = _calcOffset(calcHash(i, i2));
        int[] iArr = this._hashArea;
        int i3 = iArr[_calcOffset + 3];
        if (i3 == 2) {
            if (i == iArr[_calcOffset] && i2 == iArr[_calcOffset + 1]) {
                return this._names[_calcOffset >> 2];
            }
        } else if (i3 == 0) {
            return null;
        }
        i3 = this._secondaryStart + ((_calcOffset >> 3) << 2);
        int i4 = iArr[i3 + 3];
        if (i4 == 2) {
            if (i == iArr[i3] && i2 == iArr[i3 + 1]) {
                return this._names[i3 >> 2];
            }
        } else if (i4 == 0) {
            return null;
        }
        return _findSecondary(_calcOffset, i, i2);
    }

    public String findName(int i, int i2, int i3) {
        int _calcOffset = _calcOffset(calcHash(i, i2, i3));
        int[] iArr = this._hashArea;
        int i4 = iArr[_calcOffset + 3];
        if (i4 == 3) {
            if (i == iArr[_calcOffset] && iArr[_calcOffset + 1] == i2 && iArr[_calcOffset + 2] == i3) {
                return this._names[_calcOffset >> 2];
            }
        } else if (i4 == 0) {
            return null;
        }
        i4 = this._secondaryStart + ((_calcOffset >> 3) << 2);
        int i5 = iArr[i4 + 3];
        if (i5 == 3) {
            if (i == iArr[i4] && iArr[i4 + 1] == i2 && iArr[i4 + 2] == i3) {
                return this._names[i4 >> 2];
            }
        } else if (i5 == 0) {
            return null;
        }
        return _findSecondary(_calcOffset, i, i2, i3);
    }

    public String findName(int[] iArr, int i) {
        if (i >= 4) {
            int calcHash = calcHash(iArr, i);
            int _calcOffset = _calcOffset(calcHash);
            int[] iArr2 = this._hashArea;
            int i2 = iArr2[_calcOffset + 3];
            if (calcHash == iArr2[_calcOffset] && i2 == i && _verifyLongName(iArr, i, iArr2[_calcOffset + 1])) {
                return this._names[_calcOffset >> 2];
            }
            if (i2 == 0) {
                return null;
            }
            i2 = this._secondaryStart + ((_calcOffset >> 3) << 2);
            int i3 = iArr2[i2 + 3];
            if (calcHash == iArr2[i2] && i3 == i && _verifyLongName(iArr, i, iArr2[i2 + 1])) {
                return this._names[i2 >> 2];
            }
            return _findSecondary(_calcOffset, calcHash, iArr, i);
        } else if (i == 3) {
            return findName(iArr[0], iArr[1], iArr[2]);
        } else {
            if (i == 2) {
                return findName(iArr[0], iArr[1]);
            }
            return findName(iArr[0]);
        }
    }

    private final int _calcOffset(int i) {
        return (i & (this._hashSize - 1)) << 2;
    }

    private String _findSecondary(int i, int i2) {
        int i3 = this._tertiaryStart + ((i >> (this._tertiaryShift + 2)) << this._tertiaryShift);
        int[] iArr = this._hashArea;
        int i4 = (1 << this._tertiaryShift) + i3;
        while (i3 < i4) {
            int i5 = iArr[i3 + 3];
            if (i2 == iArr[i3] && 1 == i5) {
                return this._names[i3 >> 2];
            }
            if (i5 == 0) {
                return null;
            }
            i3 += 4;
        }
        i3 = _spilloverStart();
        while (i3 < this._spilloverEnd) {
            if (i2 == iArr[i3] && 1 == iArr[i3 + 3]) {
                return this._names[i3 >> 2];
            }
            i3 += 4;
        }
        return null;
    }

    private String _findSecondary(int i, int i2, int i3) {
        int i4 = this._tertiaryStart + ((i >> (this._tertiaryShift + 2)) << this._tertiaryShift);
        int[] iArr = this._hashArea;
        int i5 = (1 << this._tertiaryShift) + i4;
        while (i4 < i5) {
            int i6 = iArr[i4 + 3];
            if (i2 == iArr[i4] && i3 == iArr[i4 + 1] && 2 == i6) {
                return this._names[i4 >> 2];
            }
            if (i6 == 0) {
                return null;
            }
            i4 += 4;
        }
        i4 = _spilloverStart();
        while (i4 < this._spilloverEnd) {
            if (i2 == iArr[i4] && i3 == iArr[i4 + 1] && 2 == iArr[i4 + 3]) {
                return this._names[i4 >> 2];
            }
            i4 += 4;
        }
        return null;
    }

    private String _findSecondary(int i, int i2, int i3, int i4) {
        int i5 = this._tertiaryStart + ((i >> (this._tertiaryShift + 2)) << this._tertiaryShift);
        int[] iArr = this._hashArea;
        int i6 = (1 << this._tertiaryShift) + i5;
        while (i5 < i6) {
            int i7 = iArr[i5 + 3];
            if (i2 == iArr[i5] && i3 == iArr[i5 + 1] && i4 == iArr[i5 + 2] && 3 == i7) {
                return this._names[i5 >> 2];
            }
            if (i7 == 0) {
                return null;
            }
            i5 += 4;
        }
        i5 = _spilloverStart();
        while (i5 < this._spilloverEnd) {
            if (i2 == iArr[i5] && i3 == iArr[i5 + 1] && i4 == iArr[i5 + 2] && 3 == iArr[i5 + 3]) {
                return this._names[i5 >> 2];
            }
            i5 += 4;
        }
        return null;
    }

    private String _findSecondary(int i, int i2, int[] iArr, int i3) {
        int i4 = this._tertiaryStart + ((i >> (this._tertiaryShift + 2)) << this._tertiaryShift);
        int[] iArr2 = this._hashArea;
        int i5 = (1 << this._tertiaryShift) + i4;
        while (i4 < i5) {
            int i6 = iArr2[i4 + 3];
            if (i2 == iArr2[i4] && i3 == i6 && _verifyLongName(iArr, i3, iArr2[i4 + 1])) {
                return this._names[i4 >> 2];
            }
            if (i6 == 0) {
                return null;
            }
            i4 += 4;
        }
        i4 = _spilloverStart();
        while (i4 < this._spilloverEnd) {
            if (i2 == iArr2[i4] && i3 == iArr2[i4 + 3] && _verifyLongName(iArr, i3, iArr2[i4 + 1])) {
                return this._names[i4 >> 2];
            }
            i4 += 4;
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:9:0x0019, code skipped:
            r3 = r7 + 1;
            r4 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:10:0x0021, code skipped:
            if (r6[r7] == r0[r8]) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:11:0x0023, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:12:0x0024, code skipped:
            r8 = r4;
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code skipped:
            r7 = r3 + 1;
            r4 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:15:0x002f, code skipped:
            if (r6[r3] == r0[r8]) goto L_0x0032;
     */
    /* JADX WARNING: Missing block: B:16:0x0031, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:17:0x0032, code skipped:
            r8 = r4;
     */
    /* JADX WARNING: Missing block: B:19:0x0035, code skipped:
            r3 = r7 + 1;
            r4 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:20:0x003d, code skipped:
            if (r6[r7] == r0[r8]) goto L_0x0040;
     */
    /* JADX WARNING: Missing block: B:21:0x003f, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:22:0x0040, code skipped:
            r8 = r4;
     */
    /* JADX WARNING: Missing block: B:24:0x0043, code skipped:
            r7 = r3 + 1;
            r4 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:25:0x004b, code skipped:
            if (r6[r3] == r0[r8]) goto L_0x004e;
     */
    /* JADX WARNING: Missing block: B:26:0x004d, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:27:0x004e, code skipped:
            r8 = r7 + 1;
            r3 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:28:0x0056, code skipped:
            if (r6[r7] == r0[r4]) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:29:0x0058, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:30:0x0059, code skipped:
            r7 = r8 + 1;
            r4 = r3 + 1;
     */
    /* JADX WARNING: Missing block: B:31:0x0061, code skipped:
            if (r6[r8] == r0[r3]) goto L_0x0064;
     */
    /* JADX WARNING: Missing block: B:32:0x0063, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:34:0x0068, code skipped:
            if (r6[r7] == r0[r4]) goto L_0x006b;
     */
    /* JADX WARNING: Missing block: B:35:0x006a, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:36:0x006b, code skipped:
            return true;
     */
    private boolean _verifyLongName(int[] r6, int r7, int r8) {
        /*
        r5 = this;
        r0 = r5._hashArea;
        r1 = 0;
        r2 = 1;
        switch(r7) {
            case 4: goto L_0x0042;
            case 5: goto L_0x0034;
            case 6: goto L_0x0026;
            case 7: goto L_0x0018;
            case 8: goto L_0x000c;
            default: goto L_0x0007;
        };
    L_0x0007:
        r6 = r5._verifyLongName2(r6, r7, r8);
        return r6;
    L_0x000c:
        r7 = r6[r1];
        r3 = r8 + 1;
        r8 = r0[r8];
        if (r7 == r8) goto L_0x0015;
    L_0x0014:
        return r1;
    L_0x0015:
        r8 = r3;
        r7 = 1;
        goto L_0x0019;
    L_0x0018:
        r7 = 0;
    L_0x0019:
        r3 = r7 + 1;
        r7 = r6[r7];
        r4 = r8 + 1;
        r8 = r0[r8];
        if (r7 == r8) goto L_0x0024;
    L_0x0023:
        return r1;
    L_0x0024:
        r8 = r4;
        goto L_0x0027;
    L_0x0026:
        r3 = 0;
    L_0x0027:
        r7 = r3 + 1;
        r3 = r6[r3];
        r4 = r8 + 1;
        r8 = r0[r8];
        if (r3 == r8) goto L_0x0032;
    L_0x0031:
        return r1;
    L_0x0032:
        r8 = r4;
        goto L_0x0035;
    L_0x0034:
        r7 = 0;
    L_0x0035:
        r3 = r7 + 1;
        r7 = r6[r7];
        r4 = r8 + 1;
        r8 = r0[r8];
        if (r7 == r8) goto L_0x0040;
    L_0x003f:
        return r1;
    L_0x0040:
        r8 = r4;
        goto L_0x0043;
    L_0x0042:
        r3 = 0;
    L_0x0043:
        r7 = r3 + 1;
        r3 = r6[r3];
        r4 = r8 + 1;
        r8 = r0[r8];
        if (r3 == r8) goto L_0x004e;
    L_0x004d:
        return r1;
    L_0x004e:
        r8 = r7 + 1;
        r7 = r6[r7];
        r3 = r4 + 1;
        r4 = r0[r4];
        if (r7 == r4) goto L_0x0059;
    L_0x0058:
        return r1;
    L_0x0059:
        r7 = r8 + 1;
        r8 = r6[r8];
        r4 = r3 + 1;
        r3 = r0[r3];
        if (r8 == r3) goto L_0x0064;
    L_0x0063:
        return r1;
    L_0x0064:
        r6 = r6[r7];
        r7 = r0[r4];
        if (r6 == r7) goto L_0x006b;
    L_0x006a:
        return r1;
    L_0x006b:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer._verifyLongName(int[], int, int):boolean");
    }

    private boolean _verifyLongName2(int[] iArr, int i, int i2) {
        int i3 = i2;
        i2 = 0;
        while (true) {
            int i4 = i2 + 1;
            int i5 = i3 + 1;
            if (iArr[i2] != this._hashArea[i3]) {
                return false;
            }
            if (i4 >= i) {
                return true;
            }
            i2 = i4;
            i3 = i5;
        }
    }

    public String addName(String str, int i) {
        _verifySharing();
        if (this._intern) {
            str = InternCache.instance.intern(str);
        }
        int _findOffsetForAdd = _findOffsetForAdd(calcHash(i));
        this._hashArea[_findOffsetForAdd] = i;
        this._hashArea[_findOffsetForAdd + 3] = 1;
        this._names[_findOffsetForAdd >> 2] = str;
        this._count++;
        _verifyNeedForRehash();
        return str;
    }

    public String addName(String str, int i, int i2) {
        _verifySharing();
        if (this._intern) {
            str = InternCache.instance.intern(str);
        }
        int _findOffsetForAdd = _findOffsetForAdd(i2 == 0 ? calcHash(i) : calcHash(i, i2));
        this._hashArea[_findOffsetForAdd] = i;
        this._hashArea[_findOffsetForAdd + 1] = i2;
        this._hashArea[_findOffsetForAdd + 3] = 2;
        this._names[_findOffsetForAdd >> 2] = str;
        this._count++;
        _verifyNeedForRehash();
        return str;
    }

    public String addName(String str, int i, int i2, int i3) {
        _verifySharing();
        if (this._intern) {
            str = InternCache.instance.intern(str);
        }
        int _findOffsetForAdd = _findOffsetForAdd(calcHash(i, i2, i3));
        this._hashArea[_findOffsetForAdd] = i;
        this._hashArea[_findOffsetForAdd + 1] = i2;
        this._hashArea[_findOffsetForAdd + 2] = i3;
        this._hashArea[_findOffsetForAdd + 3] = 3;
        this._names[_findOffsetForAdd >> 2] = str;
        this._count++;
        _verifyNeedForRehash();
        return str;
    }

    public String addName(String str, int[] iArr, int i) {
        _verifySharing();
        if (this._intern) {
            str = InternCache.instance.intern(str);
        }
        switch (i) {
            case 1:
                i = _findOffsetForAdd(calcHash(iArr[0]));
                this._hashArea[i] = iArr[0];
                this._hashArea[i + 3] = 1;
                break;
            case 2:
                i = _findOffsetForAdd(calcHash(iArr[0], iArr[1]));
                this._hashArea[i] = iArr[0];
                this._hashArea[i + 1] = iArr[1];
                this._hashArea[i + 3] = 2;
                break;
            case 3:
                i = _findOffsetForAdd(calcHash(iArr[0], iArr[1], iArr[2]));
                this._hashArea[i] = iArr[0];
                this._hashArea[i + 1] = iArr[1];
                this._hashArea[i + 2] = iArr[2];
                this._hashArea[i + 3] = 3;
                break;
            default:
                int calcHash = calcHash(iArr, i);
                int _findOffsetForAdd = _findOffsetForAdd(calcHash);
                this._hashArea[_findOffsetForAdd] = calcHash;
                this._hashArea[_findOffsetForAdd + 1] = _appendLongName(iArr, i);
                this._hashArea[_findOffsetForAdd + 3] = i;
                i = _findOffsetForAdd;
                break;
        }
        this._names[i >> 2] = str;
        this._count++;
        _verifyNeedForRehash();
        return str;
    }

    private void _verifyNeedForRehash() {
        if (this._count > (this._hashSize >> 1)) {
            if (((this._spilloverEnd - _spilloverStart()) >> 2) <= ((this._count + 1) >> 7)) {
                double d = (double) this._count;
                double d2 = (double) this._hashSize;
                Double.isNaN(d2);
                if (d <= d2 * 0.8d) {
                    return;
                }
            }
            this._needRehash = true;
        }
    }

    private void _verifySharing() {
        if (this._hashShared) {
            this._hashArea = Arrays.copyOf(this._hashArea, this._hashArea.length);
            this._names = (String[]) Arrays.copyOf(this._names, this._names.length);
            this._hashShared = false;
            _verifyNeedForRehash();
        }
        if (this._needRehash) {
            rehash();
        }
    }

    private int _findOffsetForAdd(int i) {
        i = _calcOffset(i);
        int[] iArr = this._hashArea;
        if (iArr[i + 3] == 0) {
            return i;
        }
        int i2 = this._secondaryStart + ((i >> 3) << 2);
        if (iArr[i2 + 3] == 0) {
            return i2;
        }
        i2 = this._tertiaryStart + ((i >> (this._tertiaryShift + 2)) << this._tertiaryShift);
        i = (1 << this._tertiaryShift) + i2;
        while (i2 < i) {
            if (iArr[i2 + 3] == 0) {
                return i2;
            }
            i2 += 4;
        }
        i = this._spilloverEnd;
        this._spilloverEnd += 4;
        if (this._spilloverEnd >= (this._hashSize << 3)) {
            if (this._failOnDoS) {
                _reportTooManyCollisions();
            }
            this._needRehash = true;
        }
        return i;
    }

    private int _appendLongName(int[] iArr, int i) {
        int i2 = this._longNameOffset;
        int i3 = i2 + i;
        if (i3 > this._hashArea.length) {
            this._hashArea = Arrays.copyOf(this._hashArea, this._hashArea.length + Math.max(i3 - this._hashArea.length, Math.min(4096, this._hashSize)));
        }
        System.arraycopy(iArr, 0, this._hashArea, i2, i);
        this._longNameOffset += i;
        return i2;
    }

    public int calcHash(int i) {
        i ^= this._seed;
        i += i >>> 16;
        i ^= i << 3;
        return i + (i >>> 12);
    }

    public int calcHash(int i, int i2) {
        i += i >>> 15;
        i = ((i ^ (i >>> 9)) + (i2 * 33)) ^ this._seed;
        i += i >>> 16;
        i ^= i >>> 4;
        return i + (i << 3);
    }

    public int calcHash(int i, int i2, int i3) {
        i ^= this._seed;
        i = (((i + (i >>> 9)) * 31) + i2) * 33;
        i = (i + (i >>> 15)) ^ i3;
        i += i >>> 4;
        i += i >>> 15;
        return i ^ (i << 9);
    }

    public int calcHash(int[] iArr, int i) {
        if (i >= 4) {
            int i2 = iArr[0] ^ this._seed;
            i2 = (i2 + (i2 >>> 9)) + iArr[1];
            i2 = ((i2 + (i2 >>> 15)) * 33) ^ iArr[2];
            i2 += i2 >>> 4;
            for (int i3 = 3; i3 < i; i3++) {
                int i4 = iArr[i3];
                i2 += i4 ^ (i4 >> 21);
            }
            i2 *= MULT2;
            i2 += i2 >>> 19;
            return (i2 << 5) ^ i2;
        }
        throw new IllegalArgumentException();
    }

    private void rehash() {
        this._needRehash = false;
        this._hashShared = false;
        int[] iArr = this._hashArea;
        String[] strArr = this._names;
        int i = this._hashSize;
        int i2 = this._count;
        int i3 = i + i;
        int i4 = this._spilloverEnd;
        if (i3 > 65536) {
            nukeSymbols(true);
            return;
        }
        this._hashArea = new int[(iArr.length + (i << 3))];
        this._hashSize = i3;
        this._secondaryStart = i3 << 2;
        this._tertiaryStart = this._secondaryStart + (this._secondaryStart >> 1);
        this._tertiaryShift = _calcTertiaryShift(i3);
        this._names = new String[(strArr.length << 1)];
        nukeSymbols(false);
        int[] iArr2 = new int[16];
        i3 = 0;
        for (i = 0; i < i4; i += 4) {
            int i5 = iArr[i + 3];
            if (i5 != 0) {
                i3++;
                String str = strArr[i >> 2];
                switch (i5) {
                    case 1:
                        iArr2[0] = iArr[i];
                        addName(str, iArr2, 1);
                        break;
                    case 2:
                        iArr2[0] = iArr[i];
                        iArr2[1] = iArr[i + 1];
                        addName(str, iArr2, 2);
                        break;
                    case 3:
                        iArr2[0] = iArr[i];
                        iArr2[1] = iArr[i + 1];
                        iArr2[2] = iArr[i + 2];
                        addName(str, iArr2, 3);
                        break;
                    default:
                        if (i5 > iArr2.length) {
                            iArr2 = new int[i5];
                        }
                        System.arraycopy(iArr, iArr[i + 1], iArr2, 0, i5);
                        addName(str, iArr2, i5);
                        break;
                }
            }
        }
        if (i3 != i2) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed rehash(): old count=");
            stringBuilder.append(i2);
            stringBuilder.append(", copyCount=");
            stringBuilder.append(i3);
            throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private void nukeSymbols(boolean z) {
        this._count = 0;
        this._spilloverEnd = _spilloverStart();
        this._longNameOffset = this._hashSize << 3;
        if (z) {
            Arrays.fill(this._hashArea, 0);
            Arrays.fill(this._names, null);
        }
    }

    private final int _spilloverStart() {
        int i = this._hashSize;
        return (i << 3) - i;
    }

    /* Access modifiers changed, original: protected */
    public void _reportTooManyCollisions() {
        if (this._hashSize > 1024) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Spill-over slots in symbol table with ");
            stringBuilder.append(this._count);
            stringBuilder.append(" entries, hash area of ");
            stringBuilder.append(this._hashSize);
            stringBuilder.append(" slots is now full (all ");
            stringBuilder.append(this._hashSize >> 3);
            stringBuilder.append(" slots -- suspect a DoS attack based on hash collisions.");
            stringBuilder.append(" You can disable the check via `JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW`");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }
}
