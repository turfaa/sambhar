package com.squareup.picasso;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video.Thumbnails;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.RequestHandler.Result;
import java.io.IOException;

class MediaStoreRequestHandler extends ContentStreamRequestHandler {
    private static final String[] CONTENT_ORIENTATION = new String[]{"orientation"};

    enum PicassoKind {
        MICRO(3, 96, 96),
        MINI(1, 512, 384),
        FULL(2, -1, -1);
        
        final int androidKind;
        final int height;
        final int width;

        private PicassoKind(int i, int i2, int i3) {
            this.androidKind = i;
            this.width = i2;
            this.height = i3;
        }
    }

    MediaStoreRequestHandler(Context context) {
        super(context);
    }

    public boolean canHandleRequest(Request request) {
        Uri uri = request.uri;
        return "content".equals(uri.getScheme()) && "media".equals(uri.getAuthority());
    }

    public Result load(Request request, int i) throws IOException {
        Request request2 = request;
        ContentResolver contentResolver = this.context.getContentResolver();
        int exifOrientation = getExifOrientation(contentResolver, request2.uri);
        String type = contentResolver.getType(request2.uri);
        Object obj = (type == null || !type.startsWith("video/")) ? null : 1;
        if (request.hasSize()) {
            PicassoKind picassoKind = getPicassoKind(request2.targetWidth, request2.targetHeight);
            if (obj == null && picassoKind == PicassoKind.FULL) {
                return new Result(null, getInputStream(request), LoadedFrom.DISK, exifOrientation);
            }
            Bitmap thumbnail;
            long parseId = ContentUris.parseId(request2.uri);
            Options createBitmapOptions = RequestHandler.createBitmapOptions(request);
            createBitmapOptions.inJustDecodeBounds = true;
            Options options = createBitmapOptions;
            RequestHandler.calculateInSampleSize(request2.targetWidth, request2.targetHeight, picassoKind.width, picassoKind.height, createBitmapOptions, request);
            if (obj != null) {
                thumbnail = Thumbnails.getThumbnail(contentResolver, parseId, picassoKind == PicassoKind.FULL ? 1 : picassoKind.androidKind, options);
            } else {
                thumbnail = Images.Thumbnails.getThumbnail(contentResolver, parseId, picassoKind.androidKind, options);
            }
            if (thumbnail != null) {
                return new Result(thumbnail, null, LoadedFrom.DISK, exifOrientation);
            }
        }
        return new Result(null, getInputStream(request), LoadedFrom.DISK, exifOrientation);
    }

    static PicassoKind getPicassoKind(int i, int i2) {
        if (i <= PicassoKind.MICRO.width && i2 <= PicassoKind.MICRO.height) {
            return PicassoKind.MICRO;
        }
        if (i > PicassoKind.MINI.width || i2 > PicassoKind.MINI.height) {
            return PicassoKind.FULL;
        }
        return PicassoKind.MINI;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0035  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002e  */
    static int getExifOrientation(android.content.ContentResolver r8, android.net.Uri r9) {
        /*
        r0 = 0;
        r1 = 0;
        r4 = CONTENT_ORIENTATION;	 Catch:{ RuntimeException -> 0x0032, all -> 0x002a }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r2 = r8;
        r3 = r9;
        r8 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ RuntimeException -> 0x0032, all -> 0x002a }
        if (r8 == 0) goto L_0x0024;
    L_0x000f:
        r9 = r8.moveToFirst();	 Catch:{ RuntimeException -> 0x0022, all -> 0x0020 }
        if (r9 != 0) goto L_0x0016;
    L_0x0015:
        goto L_0x0024;
    L_0x0016:
        r9 = r8.getInt(r0);	 Catch:{ RuntimeException -> 0x0022, all -> 0x0020 }
        if (r8 == 0) goto L_0x001f;
    L_0x001c:
        r8.close();
    L_0x001f:
        return r9;
    L_0x0020:
        r9 = move-exception;
        goto L_0x002c;
        goto L_0x0033;
    L_0x0024:
        if (r8 == 0) goto L_0x0029;
    L_0x0026:
        r8.close();
    L_0x0029:
        return r0;
    L_0x002a:
        r9 = move-exception;
        r8 = r1;
    L_0x002c:
        if (r8 == 0) goto L_0x0031;
    L_0x002e:
        r8.close();
    L_0x0031:
        throw r9;
    L_0x0032:
        r8 = r1;
    L_0x0033:
        if (r8 == 0) goto L_0x0038;
    L_0x0035:
        r8.close();
    L_0x0038:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.squareup.picasso.MediaStoreRequestHandler.getExifOrientation(android.content.ContentResolver, android.net.Uri):int");
    }
}
