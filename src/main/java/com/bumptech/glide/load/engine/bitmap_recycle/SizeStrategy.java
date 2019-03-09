package com.bumptech.glide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import com.bumptech.glide.util.Util;
import java.util.NavigableMap;

@RequiresApi(19)
final class SizeStrategy implements LruPoolStrategy {
    private static final int MAX_SIZE_MULTIPLE = 8;
    private final GroupedLinkedMap<Key, Bitmap> groupedMap = new GroupedLinkedMap();
    private final KeyPool keyPool = new KeyPool();
    private final NavigableMap<Integer, Integer> sortedSizes = new PrettyPrintTreeMap();

    @VisibleForTesting
    static final class Key implements Poolable {
        private final KeyPool pool;
        int size;

        Key(KeyPool keyPool) {
            this.pool = keyPool;
        }

        public void init(int i) {
            this.size = i;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Key)) {
                return false;
            }
            if (this.size == ((Key) obj).size) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.size;
        }

        public String toString() {
            return SizeStrategy.getBitmapString(this.size);
        }

        public void offer() {
            this.pool.offer(this);
        }
    }

    @VisibleForTesting
    static class KeyPool extends BaseKeyPool<Key> {
        KeyPool() {
        }

        public Key get(int i) {
            Key key = (Key) super.get();
            key.init(i);
            return key;
        }

        /* Access modifiers changed, original: protected */
        public Key create() {
            return new Key(this);
        }
    }

    SizeStrategy() {
    }

    public void put(Bitmap bitmap) {
        Key key = this.keyPool.get(Util.getBitmapByteSize(bitmap));
        this.groupedMap.put(key, bitmap);
        Integer num = (Integer) this.sortedSizes.get(Integer.valueOf(key.size));
        NavigableMap navigableMap = this.sortedSizes;
        Integer valueOf = Integer.valueOf(key.size);
        int i = 1;
        if (num != null) {
            i = 1 + num.intValue();
        }
        navigableMap.put(valueOf, Integer.valueOf(i));
    }

    @Nullable
    public Bitmap get(int i, int i2, Config config) {
        int bitmapByteSize = Util.getBitmapByteSize(i, i2, config);
        Poolable poolable = this.keyPool.get(bitmapByteSize);
        Integer num = (Integer) this.sortedSizes.ceilingKey(Integer.valueOf(bitmapByteSize));
        if (!(num == null || num.intValue() == bitmapByteSize || num.intValue() > bitmapByteSize * 8)) {
            this.keyPool.offer(poolable);
            poolable = this.keyPool.get(num.intValue());
        }
        Bitmap bitmap = (Bitmap) this.groupedMap.get(poolable);
        if (bitmap != null) {
            bitmap.reconfigure(i, i2, config);
            decrementBitmapOfSize(num);
        }
        return bitmap;
    }

    @Nullable
    public Bitmap removeLast() {
        Bitmap bitmap = (Bitmap) this.groupedMap.removeLast();
        if (bitmap != null) {
            decrementBitmapOfSize(Integer.valueOf(Util.getBitmapByteSize(bitmap)));
        }
        return bitmap;
    }

    private void decrementBitmapOfSize(Integer num) {
        Integer num2 = (Integer) this.sortedSizes.get(num);
        if (num2.intValue() == 1) {
            this.sortedSizes.remove(num);
        } else {
            this.sortedSizes.put(num, Integer.valueOf(num2.intValue() - 1));
        }
    }

    public String logBitmap(Bitmap bitmap) {
        return getBitmapString(bitmap);
    }

    public String logBitmap(int i, int i2, Config config) {
        return getBitmapString(Util.getBitmapByteSize(i, i2, config));
    }

    public int getSize(Bitmap bitmap) {
        return Util.getBitmapByteSize(bitmap);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SizeStrategy:\n  ");
        stringBuilder.append(this.groupedMap);
        stringBuilder.append("\n  SortedSizes");
        stringBuilder.append(this.sortedSizes);
        return stringBuilder.toString();
    }

    private static String getBitmapString(Bitmap bitmap) {
        return getBitmapString(Util.getBitmapByteSize(bitmap));
    }

    static String getBitmapString(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(i);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
