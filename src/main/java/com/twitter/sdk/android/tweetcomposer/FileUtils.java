package com.twitter.sdk.android.tweetcomposer;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.facebook.share.internal.MessengerShareContentUtility;
import java.io.File;

class FileUtils {
    private static final String MEDIA_SCHEME = "com.android.providers.media.documents";

    FileUtils() {
    }

    @TargetApi(19)
    static String getPath(Context context, Uri uri) {
        if ((VERSION.SDK_INT >= 19 ? 1 : null) != null && isMediaDocumentAuthority(uri)) {
            if (!MessengerShareContentUtility.MEDIA_IMAGE.equals(DocumentsContract.getDocumentId(uri).split(":")[0])) {
                return null;
            }
            return resolveFilePath(context, Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{DocumentsContract.getDocumentId(uri).split(":")[1]});
        } else if (isContentScheme(uri)) {
            return resolveFilePath(context, uri, null, null);
        } else {
            if (isFileScheme(uri)) {
                return uri.getPath();
            }
            return null;
        }
    }

    static boolean isMediaDocumentAuthority(Uri uri) {
        return MEDIA_SCHEME.equalsIgnoreCase(uri.getAuthority());
    }

    static boolean isContentScheme(Uri uri) {
        return "content".equalsIgnoreCase(uri.getScheme());
    }

    static boolean isFileScheme(Uri uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0039  */
    static java.lang.String resolveFilePath(android.content.Context r7, android.net.Uri r8, java.lang.String r9, java.lang.String[] r10) {
        /*
        r0 = 1;
        r3 = new java.lang.String[r0];
        r0 = "_data";
        r1 = 0;
        r3[r1] = r0;
        r0 = 0;
        r1 = r7.getContentResolver();	 Catch:{ all -> 0x0035 }
        r6 = 0;
        r2 = r8;
        r4 = r9;
        r5 = r10;
        r7 = r1.query(r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0035 }
        if (r7 == 0) goto L_0x002f;
    L_0x0017:
        r8 = r7.moveToFirst();	 Catch:{ all -> 0x002d }
        if (r8 == 0) goto L_0x002f;
    L_0x001d:
        r8 = "_data";
        r8 = r7.getColumnIndexOrThrow(r8);	 Catch:{ all -> 0x002d }
        r8 = r7.getString(r8);	 Catch:{ all -> 0x002d }
        if (r7 == 0) goto L_0x002c;
    L_0x0029:
        r7.close();
    L_0x002c:
        return r8;
    L_0x002d:
        r8 = move-exception;
        goto L_0x0037;
    L_0x002f:
        if (r7 == 0) goto L_0x0034;
    L_0x0031:
        r7.close();
    L_0x0034:
        return r0;
    L_0x0035:
        r8 = move-exception;
        r7 = r0;
    L_0x0037:
        if (r7 == 0) goto L_0x003c;
    L_0x0039:
        r7.close();
    L_0x003c:
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.tweetcomposer.FileUtils.resolveFilePath(android.content.Context, android.net.Uri, java.lang.String, java.lang.String[]):java.lang.String");
    }

    static String getMimeType(File file) {
        String extension = getExtension(file.getName());
        return !TextUtils.isEmpty(extension) ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : "application/octet-stream";
    }

    static String getExtension(String str) {
        if (str == null) {
            return null;
        }
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf < 0) {
            str = "";
        } else {
            str = str.substring(lastIndexOf + 1);
        }
        return str;
    }
}
