package com.twitter.sdk.android.tweetui.internal.util;

public class IntHashMap {
    private int count;
    private float loadFactor;
    private Entry[] table;
    private int threshold;

    private static class Entry {
        public final int hash;
        public int key;
        public Entry next;
        public Object value;

        protected Entry(int i, int i2, Object obj, Entry entry) {
            this.hash = i;
            this.key = i2;
            this.value = obj;
            this.next = entry;
        }
    }

    public IntHashMap() {
        this(20, 0.75f);
    }

    public IntHashMap(int i) {
        this(i, 0.75f);
    }

    public IntHashMap(int i, float f) {
        StringBuilder stringBuilder;
        if (i < 0) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal Capacity: ");
            stringBuilder.append(i);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (f > 0.0f) {
            if (i == 0) {
                i = 1;
            }
            this.loadFactor = f;
            this.table = new Entry[i];
            this.threshold = (int) (((float) i) * f);
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal Load: ");
            stringBuilder.append(f);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public int size() {
        return this.count;
    }

    public boolean isEmpty() {
        return this.count == 0;
    }

    public boolean contains(Object obj) {
        if (obj != null) {
            Entry[] entryArr = this.table;
            int length = entryArr.length;
            while (true) {
                int i = length - 1;
                if (length <= 0) {
                    return false;
                }
                for (Entry entry = entryArr[i]; entry != null; entry = entry.next) {
                    if (entry.value.equals(obj)) {
                        return true;
                    }
                }
                length = i;
            }
        } else {
            throw new NullPointerException();
        }
    }

    public boolean containsValue(Object obj) {
        return contains(obj);
    }

    public boolean containsKey(int i) {
        Entry[] entryArr = this.table;
        for (Entry entry = entryArr[(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED & i) % entryArr.length]; entry != null; entry = entry.next) {
            if (entry.hash == i) {
                return true;
            }
        }
        return false;
    }

    public Object get(int i) {
        Entry[] entryArr = this.table;
        for (Entry entry = entryArr[(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED & i) % entryArr.length]; entry != null; entry = entry.next) {
            if (entry.hash == i) {
                return entry.value;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void rehash() {
        int length = this.table.length;
        Entry[] entryArr = this.table;
        int i = (length * 2) + 1;
        Entry[] entryArr2 = new Entry[i];
        this.threshold = (int) (((float) i) * this.loadFactor);
        this.table = entryArr2;
        while (true) {
            int i2 = length - 1;
            if (length > 0) {
                Entry entry = entryArr[i2];
                while (entry != null) {
                    Entry entry2 = entry.next;
                    int i3 = (entry.hash & ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) % i;
                    entry.next = entryArr2[i3];
                    entryArr2[i3] = entry;
                    entry = entry2;
                }
                length = i2;
            } else {
                return;
            }
        }
    }

    public Object put(int i, Object obj) {
        Entry[] entryArr = this.table;
        int i2 = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED & i;
        int length = i2 % entryArr.length;
        for (Entry entry = entryArr[length]; entry != null; entry = entry.next) {
            if (entry.hash == i) {
                Object obj2 = entry.value;
                entry.value = obj;
                return obj2;
            }
        }
        if (this.count >= this.threshold) {
            rehash();
            entryArr = this.table;
            length = i2 % entryArr.length;
        }
        entryArr[length] = new Entry(i, i, obj, entryArr[length]);
        this.count++;
        return null;
    }

    public Object remove(int i) {
        Entry[] entryArr = this.table;
        int length = (ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED & i) % entryArr.length;
        Entry entry = null;
        for (Entry entry2 = entryArr[length]; entry2 != null; entry2 = entry2.next) {
            if (entry2.hash == i) {
                if (entry != null) {
                    entry.next = entry2.next;
                } else {
                    entryArr[length] = entry2.next;
                }
                this.count--;
                Object obj = entry2.value;
                entry2.value = null;
                return obj;
            }
            entry = entry2;
        }
        return null;
    }

    public synchronized void clear() {
        Entry[] entryArr = this.table;
        int length = entryArr.length;
        while (true) {
            length--;
            if (length >= 0) {
                entryArr[length] = null;
            } else {
                this.count = 0;
            }
        }
    }
}
