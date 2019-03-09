package com.bumptech.glide.load.data.mediastore;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

class ThumbnailStreamOpener {
    private static final FileService DEFAULT_SERVICE = new FileService();
    private static final String TAG = "ThumbStreamOpener";
    private final ArrayPool byteArrayPool;
    private final ContentResolver contentResolver;
    private final List<ImageHeaderParser> parsers;
    private final ThumbnailQuery query;
    private final FileService service;

    ThumbnailStreamOpener(List<ImageHeaderParser> list, ThumbnailQuery thumbnailQuery, ArrayPool arrayPool, ContentResolver contentResolver) {
        this(list, DEFAULT_SERVICE, thumbnailQuery, arrayPool, contentResolver);
    }

    ThumbnailStreamOpener(List<ImageHeaderParser> list, FileService fileService, ThumbnailQuery thumbnailQuery, ArrayPool arrayPool, ContentResolver contentResolver) {
        this.service = fileService;
        this.query = thumbnailQuery;
        this.byteArrayPool = arrayPool;
        this.contentResolver = contentResolver;
        this.parsers = list;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0047 A:{SYNTHETIC, Splitter:B:27:0x0047} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0027 A:{Catch:{ all -> 0x0044 }} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003f A:{SYNTHETIC, Splitter:B:20:0x003f} */
    public int getOrientation(android.net.Uri r7) {
        /*
        r6 = this;
        r0 = 0;
        r1 = r6.contentResolver;	 Catch:{ IOException | NullPointerException -> 0x001a, IOException | NullPointerException -> 0x001a, all -> 0x0017 }
        r1 = r1.openInputStream(r7);	 Catch:{ IOException | NullPointerException -> 0x001a, IOException | NullPointerException -> 0x001a, all -> 0x0017 }
        r0 = r6.parsers;	 Catch:{ IOException | NullPointerException -> 0x0015, IOException | NullPointerException -> 0x0015 }
        r2 = r6.byteArrayPool;	 Catch:{ IOException | NullPointerException -> 0x0015, IOException | NullPointerException -> 0x0015 }
        r0 = com.bumptech.glide.load.ImageHeaderParserUtils.getOrientation(r0, r1, r2);	 Catch:{ IOException | NullPointerException -> 0x0015, IOException | NullPointerException -> 0x0015 }
        if (r1 == 0) goto L_0x0014;
    L_0x0011:
        r1.close();	 Catch:{ IOException -> 0x0014 }
    L_0x0014:
        return r0;
    L_0x0015:
        r0 = move-exception;
        goto L_0x001e;
    L_0x0017:
        r7 = move-exception;
        r1 = r0;
        goto L_0x0045;
    L_0x001a:
        r1 = move-exception;
        r5 = r1;
        r1 = r0;
        r0 = r5;
    L_0x001e:
        r2 = "ThumbStreamOpener";
        r3 = 3;
        r2 = android.util.Log.isLoggable(r2, r3);	 Catch:{ all -> 0x0044 }
        if (r2 == 0) goto L_0x003d;
    L_0x0027:
        r2 = "ThumbStreamOpener";
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0044 }
        r3.<init>();	 Catch:{ all -> 0x0044 }
        r4 = "Failed to open uri: ";
        r3.append(r4);	 Catch:{ all -> 0x0044 }
        r3.append(r7);	 Catch:{ all -> 0x0044 }
        r7 = r3.toString();	 Catch:{ all -> 0x0044 }
        android.util.Log.d(r2, r7, r0);	 Catch:{ all -> 0x0044 }
    L_0x003d:
        if (r1 == 0) goto L_0x0042;
    L_0x003f:
        r1.close();	 Catch:{ IOException -> 0x0042 }
    L_0x0042:
        r7 = -1;
        return r7;
    L_0x0044:
        r7 = move-exception;
    L_0x0045:
        if (r1 == 0) goto L_0x004a;
    L_0x0047:
        r1.close();	 Catch:{ IOException -> 0x004a }
    L_0x004a:
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.load.data.mediastore.ThumbnailStreamOpener.getOrientation(android.net.Uri):int");
    }

    public InputStream open(Uri uri) throws FileNotFoundException {
        String path = getPath(uri);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File file = this.service.get(path);
        if (!isValid(file)) {
            return null;
        }
        Uri fromFile = Uri.fromFile(file);
        try {
            return this.contentResolver.openInputStream(fromFile);
        } catch (NullPointerException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("NPE opening uri: ");
            stringBuilder.append(uri);
            stringBuilder.append(" -> ");
            stringBuilder.append(fromFile);
            throw ((FileNotFoundException) new FileNotFoundException(stringBuilder.toString()).initCause(e));
        }
    }

    @Nullable
    private String getPath(@NonNull Uri uri) {
        Cursor query = this.query.query(uri);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    String string = query.getString(0);
                    return string;
                }
            } finally {
                if (query != null) {
                    query.close();
                }
            }
        }
        if (query != null) {
            query.close();
        }
        return null;
    }

    private boolean isValid(File file) {
        return this.service.exists(file) && 0 < this.service.length(file);
    }
}
