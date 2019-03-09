package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;

public class BitmapEncoder implements ResourceEncoder<Bitmap> {
    public static final Option<CompressFormat> COMPRESSION_FORMAT = Option.memory("com.bumptech.glide.load.resource.bitmap.BitmapEncoder.CompressionFormat");
    public static final Option<Integer> COMPRESSION_QUALITY = Option.memory("com.bumptech.glide.load.resource.bitmap.BitmapEncoder.CompressionQuality", Integer.valueOf(90));
    private static final String TAG = "BitmapEncoder";
    @Nullable
    private final ArrayPool arrayPool;

    public BitmapEncoder(@NonNull ArrayPool arrayPool) {
        this.arrayPool = arrayPool;
    }

    @Deprecated
    public BitmapEncoder() {
        this.arrayPool = null;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:37:0x00c5=Splitter:B:37:0x00c5, B:28:0x006b=Splitter:B:28:0x006b} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0074 A:{Catch:{ all -> 0x00c6 }} */
    /* JADX WARNING: Missing exception handler attribute for start block: B:28:0x006b */
    /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00c5 */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0061 A:{Catch:{ all -> 0x0055 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c2 A:{SYNTHETIC, Splitter:B:35:0x00c2} */
    /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|(8:4|5|6|7|(2:9|10)(1:11)|12|13|14)|15|16|28|29|(1:31)|32|33) */
    /* JADX WARNING: Can't wrap try/catch for region: R(4:21|(2:35|36)|37|38) */
    /* JADX WARNING: Missing block: B:27:0x0068, code skipped:
            if (r5 == null) goto L_0x006b;
     */
    public boolean encode(@android.support.annotation.NonNull com.bumptech.glide.load.engine.Resource<android.graphics.Bitmap> r8, @android.support.annotation.NonNull java.io.File r9, @android.support.annotation.NonNull com.bumptech.glide.load.Options r10) {
        /*
        r7 = this;
        r8 = r8.get();
        r8 = (android.graphics.Bitmap) r8;
        r0 = r7.getFormat(r8, r10);
        r1 = "encode: [%dx%d] %s";
        r2 = r8.getWidth();
        r2 = java.lang.Integer.valueOf(r2);
        r3 = r8.getHeight();
        r3 = java.lang.Integer.valueOf(r3);
        com.bumptech.glide.util.pool.GlideTrace.beginSectionFormat(r1, r2, r3, r0);
        r1 = com.bumptech.glide.util.LogTime.getLogTime();	 Catch:{ all -> 0x00c6 }
        r3 = COMPRESSION_QUALITY;	 Catch:{ all -> 0x00c6 }
        r3 = r10.get(r3);	 Catch:{ all -> 0x00c6 }
        r3 = (java.lang.Integer) r3;	 Catch:{ all -> 0x00c6 }
        r3 = r3.intValue();	 Catch:{ all -> 0x00c6 }
        r4 = 0;
        r5 = 0;
        r6 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x0057 }
        r6.<init>(r9);	 Catch:{ IOException -> 0x0057 }
        r9 = r7.arrayPool;	 Catch:{ IOException -> 0x0052, all -> 0x004f }
        if (r9 == 0) goto L_0x0043;
    L_0x003a:
        r9 = new com.bumptech.glide.load.data.BufferedOutputStream;	 Catch:{ IOException -> 0x0052, all -> 0x004f }
        r5 = r7.arrayPool;	 Catch:{ IOException -> 0x0052, all -> 0x004f }
        r9.<init>(r6, r5);	 Catch:{ IOException -> 0x0052, all -> 0x004f }
        r5 = r9;
        goto L_0x0044;
    L_0x0043:
        r5 = r6;
    L_0x0044:
        r8.compress(r0, r3, r5);	 Catch:{ IOException -> 0x0057 }
        r5.close();	 Catch:{ IOException -> 0x0057 }
        r4 = 1;
    L_0x004b:
        r5.close();	 Catch:{ IOException -> 0x006b }
        goto L_0x006b;
    L_0x004f:
        r8 = move-exception;
        r5 = r6;
        goto L_0x00c0;
    L_0x0052:
        r9 = move-exception;
        r5 = r6;
        goto L_0x0058;
    L_0x0055:
        r8 = move-exception;
        goto L_0x00c0;
    L_0x0057:
        r9 = move-exception;
    L_0x0058:
        r3 = "BitmapEncoder";
        r6 = 3;
        r3 = android.util.Log.isLoggable(r3, r6);	 Catch:{ all -> 0x0055 }
        if (r3 == 0) goto L_0x0068;
    L_0x0061:
        r3 = "BitmapEncoder";
        r6 = "Failed to encode Bitmap";
        android.util.Log.d(r3, r6, r9);	 Catch:{ all -> 0x0055 }
    L_0x0068:
        if (r5 == 0) goto L_0x006b;
    L_0x006a:
        goto L_0x004b;
    L_0x006b:
        r9 = "BitmapEncoder";
        r3 = 2;
        r9 = android.util.Log.isLoggable(r9, r3);	 Catch:{ all -> 0x00c6 }
        if (r9 == 0) goto L_0x00bc;
    L_0x0074:
        r9 = "BitmapEncoder";
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00c6 }
        r3.<init>();	 Catch:{ all -> 0x00c6 }
        r5 = "Compressed with type: ";
        r3.append(r5);	 Catch:{ all -> 0x00c6 }
        r3.append(r0);	 Catch:{ all -> 0x00c6 }
        r0 = " of size ";
        r3.append(r0);	 Catch:{ all -> 0x00c6 }
        r0 = com.bumptech.glide.util.Util.getBitmapByteSize(r8);	 Catch:{ all -> 0x00c6 }
        r3.append(r0);	 Catch:{ all -> 0x00c6 }
        r0 = " in ";
        r3.append(r0);	 Catch:{ all -> 0x00c6 }
        r0 = com.bumptech.glide.util.LogTime.getElapsedMillis(r1);	 Catch:{ all -> 0x00c6 }
        r3.append(r0);	 Catch:{ all -> 0x00c6 }
        r0 = ", options format: ";
        r3.append(r0);	 Catch:{ all -> 0x00c6 }
        r0 = COMPRESSION_FORMAT;	 Catch:{ all -> 0x00c6 }
        r10 = r10.get(r0);	 Catch:{ all -> 0x00c6 }
        r3.append(r10);	 Catch:{ all -> 0x00c6 }
        r10 = ", hasAlpha: ";
        r3.append(r10);	 Catch:{ all -> 0x00c6 }
        r8 = r8.hasAlpha();	 Catch:{ all -> 0x00c6 }
        r3.append(r8);	 Catch:{ all -> 0x00c6 }
        r8 = r3.toString();	 Catch:{ all -> 0x00c6 }
        android.util.Log.v(r9, r8);	 Catch:{ all -> 0x00c6 }
    L_0x00bc:
        com.bumptech.glide.util.pool.GlideTrace.endSection();
        return r4;
    L_0x00c0:
        if (r5 == 0) goto L_0x00c5;
    L_0x00c2:
        r5.close();	 Catch:{ IOException -> 0x00c5 }
    L_0x00c5:
        throw r8;	 Catch:{ all -> 0x00c6 }
    L_0x00c6:
        r8 = move-exception;
        com.bumptech.glide.util.pool.GlideTrace.endSection();
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.load.resource.bitmap.BitmapEncoder.encode(com.bumptech.glide.load.engine.Resource, java.io.File, com.bumptech.glide.load.Options):boolean");
    }

    private CompressFormat getFormat(Bitmap bitmap, Options options) {
        CompressFormat compressFormat = (CompressFormat) options.get(COMPRESSION_FORMAT);
        if (compressFormat != null) {
            return compressFormat;
        }
        if (bitmap.hasAlpha()) {
            return CompressFormat.PNG;
        }
        return CompressFormat.JPEG;
    }

    @NonNull
    public EncodeStrategy getEncodeStrategy(@NonNull Options options) {
        return EncodeStrategy.TRANSFORMED;
    }
}
