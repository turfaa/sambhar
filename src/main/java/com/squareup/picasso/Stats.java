package com.squareup.picasso;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

class Stats {
    private static final int BITMAP_DECODE_FINISHED = 2;
    private static final int BITMAP_TRANSFORMED_FINISHED = 3;
    private static final int CACHE_HIT = 0;
    private static final int CACHE_MISS = 1;
    private static final int DOWNLOAD_FINISHED = 4;
    private static final String STATS_THREAD_NAME = "Picasso-Stats";
    long averageDownloadSize;
    long averageOriginalBitmapSize;
    long averageTransformedBitmapSize;
    final Cache cache;
    long cacheHits;
    long cacheMisses;
    int downloadCount;
    final Handler handler;
    int originalBitmapCount;
    final HandlerThread statsThread = new HandlerThread(STATS_THREAD_NAME, 10);
    long totalDownloadSize;
    long totalOriginalBitmapSize;
    long totalTransformedBitmapSize;
    int transformedBitmapCount;

    private static class StatsHandler extends Handler {
        private final Stats stats;

        public StatsHandler(Looper looper, Stats stats) {
            super(looper);
            this.stats = stats;
        }

        public void handleMessage(final Message message) {
            switch (message.what) {
                case 0:
                    this.stats.performCacheHit();
                    return;
                case 1:
                    this.stats.performCacheMiss();
                    return;
                case 2:
                    this.stats.performBitmapDecoded((long) message.arg1);
                    return;
                case 3:
                    this.stats.performBitmapTransformed((long) message.arg1);
                    return;
                case 4:
                    this.stats.performDownloadFinished((Long) message.obj);
                    return;
                default:
                    Picasso.HANDLER.post(new Runnable() {
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Unhandled stats message.");
                            stringBuilder.append(message.what);
                            throw new AssertionError(stringBuilder.toString());
                        }
                    });
                    return;
            }
        }
    }

    Stats(Cache cache) {
        this.cache = cache;
        this.statsThread.start();
        Utils.flushStackLocalLeaks(this.statsThread.getLooper());
        this.handler = new StatsHandler(this.statsThread.getLooper(), this);
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchBitmapDecoded(Bitmap bitmap) {
        processBitmap(bitmap, 2);
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchBitmapTransformed(Bitmap bitmap) {
        processBitmap(bitmap, 3);
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchDownloadFinished(long j) {
        this.handler.sendMessage(this.handler.obtainMessage(4, Long.valueOf(j)));
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchCacheHit() {
        this.handler.sendEmptyMessage(0);
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchCacheMiss() {
        this.handler.sendEmptyMessage(1);
    }

    /* Access modifiers changed, original: 0000 */
    public void shutdown() {
        this.statsThread.quit();
    }

    /* Access modifiers changed, original: 0000 */
    public void performCacheHit() {
        this.cacheHits++;
    }

    /* Access modifiers changed, original: 0000 */
    public void performCacheMiss() {
        this.cacheMisses++;
    }

    /* Access modifiers changed, original: 0000 */
    public void performDownloadFinished(Long l) {
        this.downloadCount++;
        this.totalDownloadSize += l.longValue();
        this.averageDownloadSize = getAverage(this.downloadCount, this.totalDownloadSize);
    }

    /* Access modifiers changed, original: 0000 */
    public void performBitmapDecoded(long j) {
        this.originalBitmapCount++;
        this.totalOriginalBitmapSize += j;
        this.averageOriginalBitmapSize = getAverage(this.originalBitmapCount, this.totalOriginalBitmapSize);
    }

    /* Access modifiers changed, original: 0000 */
    public void performBitmapTransformed(long j) {
        this.transformedBitmapCount++;
        this.totalTransformedBitmapSize += j;
        this.averageTransformedBitmapSize = getAverage(this.originalBitmapCount, this.totalTransformedBitmapSize);
    }

    /* Access modifiers changed, original: 0000 */
    public StatsSnapshot createSnapshot() {
        return new StatsSnapshot(this.cache.maxSize(), this.cache.size(), this.cacheHits, this.cacheMisses, this.totalDownloadSize, this.totalOriginalBitmapSize, this.totalTransformedBitmapSize, this.averageDownloadSize, this.averageOriginalBitmapSize, this.averageTransformedBitmapSize, this.downloadCount, this.originalBitmapCount, this.transformedBitmapCount, System.currentTimeMillis());
    }

    private void processBitmap(Bitmap bitmap, int i) {
        this.handler.sendMessage(this.handler.obtainMessage(i, Utils.getBitmapBytes(bitmap), 0));
    }

    private static long getAverage(int i, long j) {
        return j / ((long) i);
    }
}
