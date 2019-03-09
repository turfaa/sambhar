package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.NetworkInfo;
import com.squareup.picasso.Picasso.LoadedFrom;
import java.io.IOException;
import java.io.InputStream;

public abstract class RequestHandler {

    public static final class Result {
        private final Bitmap bitmap;
        private final int exifOrientation;
        private final LoadedFrom loadedFrom;
        private final InputStream stream;

        public Result(Bitmap bitmap, LoadedFrom loadedFrom) {
            this((Bitmap) Utils.checkNotNull(bitmap, "bitmap == null"), null, loadedFrom, 0);
        }

        public Result(InputStream inputStream, LoadedFrom loadedFrom) {
            this(null, (InputStream) Utils.checkNotNull(inputStream, "stream == null"), loadedFrom, 0);
        }

        Result(Bitmap bitmap, InputStream inputStream, LoadedFrom loadedFrom, int i) {
            int i2 = 0;
            int i3 = bitmap != null ? 1 : 0;
            if (inputStream != null) {
                i2 = 1;
            }
            if ((i2 ^ i3) != 0) {
                this.bitmap = bitmap;
                this.stream = inputStream;
                this.loadedFrom = (LoadedFrom) Utils.checkNotNull(loadedFrom, "loadedFrom == null");
                this.exifOrientation = i;
                return;
            }
            throw new AssertionError();
        }

        public Bitmap getBitmap() {
            return this.bitmap;
        }

        public InputStream getStream() {
            return this.stream;
        }

        public LoadedFrom getLoadedFrom() {
            return this.loadedFrom;
        }

        /* Access modifiers changed, original: 0000 */
        public int getExifOrientation() {
            return this.exifOrientation;
        }
    }

    public abstract boolean canHandleRequest(Request request);

    /* Access modifiers changed, original: 0000 */
    public int getRetryCount() {
        return 0;
    }

    public abstract Result load(Request request, int i) throws IOException;

    /* Access modifiers changed, original: 0000 */
    public boolean shouldRetry(boolean z, NetworkInfo networkInfo) {
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean supportsReplay() {
        return false;
    }

    static Options createBitmapOptions(Request request) {
        boolean hasSize = request.hasSize();
        Object obj = request.config != null ? 1 : null;
        Options options = null;
        if (hasSize || obj != null) {
            options = new Options();
            options.inJustDecodeBounds = hasSize;
            if (obj != null) {
                options.inPreferredConfig = request.config;
            }
        }
        return options;
    }

    static boolean requiresInSampleSize(Options options) {
        return options != null && options.inJustDecodeBounds;
    }

    static void calculateInSampleSize(int i, int i2, Options options, Request request) {
        calculateInSampleSize(i, i2, options.outWidth, options.outHeight, options, request);
    }

    static void calculateInSampleSize(int i, int i2, int i3, int i4, Options options, Request request) {
        if (i4 <= i2 && i3 <= i) {
            i = 1;
        } else if (i2 == 0) {
            i = (int) Math.floor((double) (((float) i3) / ((float) i)));
        } else if (i == 0) {
            i = (int) Math.floor((double) (((float) i4) / ((float) i2)));
        } else {
            i2 = (int) Math.floor((double) (((float) i4) / ((float) i2)));
            i = (int) Math.floor((double) (((float) i3) / ((float) i)));
            i = request.centerInside ? Math.max(i2, i) : Math.min(i2, i);
        }
        options.inSampleSize = i;
        options.inJustDecodeBounds = false;
    }
}
