package com.google.zxing.oned.rss.expanded;

import java.util.ArrayList;
import java.util.List;

final class ExpandedRow {
    private final List<ExpandedPair> pairs;
    private final int rowNumber;
    private final boolean wasReversed;

    ExpandedRow(List<ExpandedPair> list, int i, boolean z) {
        this.pairs = new ArrayList(list);
        this.rowNumber = i;
        this.wasReversed = z;
    }

    /* Access modifiers changed, original: 0000 */
    public List<ExpandedPair> getPairs() {
        return this.pairs;
    }

    /* Access modifiers changed, original: 0000 */
    public int getRowNumber() {
        return this.rowNumber;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isReversed() {
        return this.wasReversed;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isEquivalent(List<ExpandedPair> list) {
        return this.pairs.equals(list);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{ ");
        stringBuilder.append(this.pairs);
        stringBuilder.append(" }");
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ExpandedRow)) {
            return false;
        }
        ExpandedRow expandedRow = (ExpandedRow) obj;
        if (this.pairs.equals(expandedRow.getPairs()) && this.wasReversed == expandedRow.wasReversed) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.pairs.hashCode() ^ Boolean.valueOf(this.wasReversed).hashCode();
    }
}
