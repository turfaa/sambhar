package com.google.zxing.oned.rss.expanded;

import com.google.zxing.oned.rss.DataCharacter;
import com.google.zxing.oned.rss.FinderPattern;

final class ExpandedPair {
    private final FinderPattern finderPattern;
    private final DataCharacter leftChar;
    private final boolean mayBeLast;
    private final DataCharacter rightChar;

    ExpandedPair(DataCharacter dataCharacter, DataCharacter dataCharacter2, FinderPattern finderPattern, boolean z) {
        this.leftChar = dataCharacter;
        this.rightChar = dataCharacter2;
        this.finderPattern = finderPattern;
        this.mayBeLast = z;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean mayBeLast() {
        return this.mayBeLast;
    }

    /* Access modifiers changed, original: 0000 */
    public DataCharacter getLeftChar() {
        return this.leftChar;
    }

    /* Access modifiers changed, original: 0000 */
    public DataCharacter getRightChar() {
        return this.rightChar;
    }

    /* Access modifiers changed, original: 0000 */
    public FinderPattern getFinderPattern() {
        return this.finderPattern;
    }

    public boolean mustBeLast() {
        return this.rightChar == null;
    }

    public String toString() {
        Object obj;
        StringBuilder stringBuilder = new StringBuilder("[ ");
        stringBuilder.append(this.leftChar);
        stringBuilder.append(" , ");
        stringBuilder.append(this.rightChar);
        stringBuilder.append(" : ");
        if (this.finderPattern == null) {
            obj = "null";
        } else {
            obj = Integer.valueOf(this.finderPattern.getValue());
        }
        stringBuilder.append(obj);
        stringBuilder.append(" ]");
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ExpandedPair)) {
            return false;
        }
        ExpandedPair expandedPair = (ExpandedPair) obj;
        if (equalsOrNull(this.leftChar, expandedPair.leftChar) && equalsOrNull(this.rightChar, expandedPair.rightChar) && equalsOrNull(this.finderPattern, expandedPair.finderPattern)) {
            return true;
        }
        return false;
    }

    private static boolean equalsOrNull(Object obj, Object obj2) {
        if (obj == null) {
            return obj2 == null;
        } else {
            return obj.equals(obj2);
        }
    }

    public int hashCode() {
        return (hashNotNull(this.leftChar) ^ hashNotNull(this.rightChar)) ^ hashNotNull(this.finderPattern);
    }

    private static int hashNotNull(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }
}
