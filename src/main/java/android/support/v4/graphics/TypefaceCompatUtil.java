package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

@RestrictTo({Scope.LIBRARY_GROUP})
public class TypefaceCompatUtil {
    private static final String CACHE_FILE_PREFIX = ".font";
    private static final String TAG = "TypefaceCompatUtil";

    private TypefaceCompatUtil() {
    }

    @Nullable
    public static File getTempFile(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CACHE_FILE_PREFIX);
        stringBuilder.append(Process.myPid());
        stringBuilder.append("-");
        stringBuilder.append(Process.myTid());
        stringBuilder.append("-");
        String stringBuilder2 = stringBuilder.toString();
        int i = 0;
        while (i < 100) {
            File cacheDir = context.getCacheDir();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(stringBuilder2);
            stringBuilder3.append(i);
            File file = new File(cacheDir, stringBuilder3.toString());
            try {
                if (file.createNewFile()) {
                    return file;
                }
                i++;
            } catch (IOException unused) {
            }
        }
        return null;
    }

    @Nullable
    @RequiresApi(19)
    private static ByteBuffer mmap(File file) {
        Throwable th;
        try {
            ByteBuffer byteBuffer;
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                FileChannel channel = fileInputStream.getChannel();
                MappedByteBuffer map = channel.map(MapMode.READ_ONLY, 0, channel.size());
                fileInputStream.close();
                return map;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                byteBuffer = th;
                th = th3;
            }
            if (byteBuffer != null) {
                try {
                    fileInputStream.close();
                } catch (Throwable unused) {
                }
            } else {
                fileInputStream.close();
            }
            throw th;
            throw th;
        } catch (IOException unused2) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0048 A:{Splitter:B:7:0x0013, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0053  */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:31:0x0048, code skipped:
            r9 = th;
     */
    /* JADX WARNING: Missing block: B:32:0x0049, code skipped:
            r10 = null;
     */
    /* JADX WARNING: Missing block: B:39:0x0053, code skipped:
            if (r10 != null) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:41:?, code skipped:
            r8.close();
     */
    /* JADX WARNING: Missing block: B:43:?, code skipped:
            r8.close();
     */
    @android.support.annotation.Nullable
    @android.support.annotation.RequiresApi(19)
    public static java.nio.ByteBuffer mmap(android.content.Context r8, android.os.CancellationSignal r9, android.net.Uri r10) {
        /*
        r8 = r8.getContentResolver();
        r0 = 0;
        r1 = "r";
        r8 = r8.openFileDescriptor(r10, r1, r9);	 Catch:{ IOException -> 0x005d }
        if (r8 != 0) goto L_0x0013;
    L_0x000d:
        if (r8 == 0) goto L_0x0012;
    L_0x000f:
        r8.close();	 Catch:{ IOException -> 0x005d }
    L_0x0012:
        return r0;
    L_0x0013:
        r9 = new java.io.FileInputStream;	 Catch:{ Throwable -> 0x004b, all -> 0x0048 }
        r10 = r8.getFileDescriptor();	 Catch:{ Throwable -> 0x004b, all -> 0x0048 }
        r9.<init>(r10);	 Catch:{ Throwable -> 0x004b, all -> 0x0048 }
        r1 = r9.getChannel();	 Catch:{ Throwable -> 0x0038, all -> 0x0035 }
        r5 = r1.size();	 Catch:{ Throwable -> 0x0038, all -> 0x0035 }
        r2 = java.nio.channels.FileChannel.MapMode.READ_ONLY;	 Catch:{ Throwable -> 0x0038, all -> 0x0035 }
        r3 = 0;
        r10 = r1.map(r2, r3, r5);	 Catch:{ Throwable -> 0x0038, all -> 0x0035 }
        r9.close();	 Catch:{ Throwable -> 0x004b, all -> 0x0048 }
        if (r8 == 0) goto L_0x0034;
    L_0x0031:
        r8.close();	 Catch:{ IOException -> 0x005d }
    L_0x0034:
        return r10;
    L_0x0035:
        r10 = move-exception;
        r1 = r0;
        goto L_0x003e;
    L_0x0038:
        r10 = move-exception;
        throw r10;	 Catch:{ all -> 0x003a }
    L_0x003a:
        r1 = move-exception;
        r7 = r1;
        r1 = r10;
        r10 = r7;
    L_0x003e:
        if (r1 == 0) goto L_0x0044;
    L_0x0040:
        r9.close();	 Catch:{ Throwable -> 0x0047, all -> 0x0048 }
        goto L_0x0047;
    L_0x0044:
        r9.close();	 Catch:{ Throwable -> 0x004b, all -> 0x0048 }
    L_0x0047:
        throw r10;	 Catch:{ Throwable -> 0x004b, all -> 0x0048 }
    L_0x0048:
        r9 = move-exception;
        r10 = r0;
        goto L_0x0051;
    L_0x004b:
        r9 = move-exception;
        throw r9;	 Catch:{ all -> 0x004d }
    L_0x004d:
        r10 = move-exception;
        r7 = r10;
        r10 = r9;
        r9 = r7;
    L_0x0051:
        if (r8 == 0) goto L_0x005c;
    L_0x0053:
        if (r10 == 0) goto L_0x0059;
    L_0x0055:
        r8.close();	 Catch:{ Throwable -> 0x005c }
        goto L_0x005c;
    L_0x0059:
        r8.close();	 Catch:{ IOException -> 0x005d }
    L_0x005c:
        throw r9;	 Catch:{ IOException -> 0x005d }
    L_0x005d:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(android.content.Context, android.os.CancellationSignal, android.net.Uri):java.nio.ByteBuffer");
    }

    @Nullable
    @RequiresApi(19)
    public static ByteBuffer copyToDirectBuffer(Context context, Resources resources, int i) {
        File tempFile = getTempFile(context);
        if (tempFile == null) {
            return null;
        }
        try {
            if (!copyToFile(tempFile, resources, i)) {
                return null;
            }
            ByteBuffer mmap = mmap(tempFile);
            tempFile.delete();
            return mmap;
        } finally {
            tempFile.delete();
        }
    }

    public static boolean copyToFile(File file, InputStream inputStream) {
        IOException e;
        String str;
        StringBuilder stringBuilder;
        Throwable th;
        ThreadPolicy allowThreadDiskWrites = StrictMode.allowThreadDiskWrites();
        Closeable closeable = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            try {
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        closeQuietly(fileOutputStream);
                        StrictMode.setThreadPolicy(allowThreadDiskWrites);
                        return true;
                    }
                }
            } catch (IOException e2) {
                e = e2;
                closeable = fileOutputStream;
                try {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Error copying resource contents to temp file: ");
                    stringBuilder.append(e.getMessage());
                    Log.e(str, stringBuilder.toString());
                    closeQuietly(closeable);
                    StrictMode.setThreadPolicy(allowThreadDiskWrites);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    closeQuietly(closeable);
                    StrictMode.setThreadPolicy(allowThreadDiskWrites);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = fileOutputStream;
                closeQuietly(closeable);
                StrictMode.setThreadPolicy(allowThreadDiskWrites);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Error copying resource contents to temp file: ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            closeQuietly(closeable);
            StrictMode.setThreadPolicy(allowThreadDiskWrites);
            return false;
        }
    }

    public static boolean copyToFile(File file, Resources resources, int i) {
        Throwable th;
        Closeable openRawResource;
        try {
            openRawResource = resources.openRawResource(i);
            try {
                boolean copyToFile = copyToFile(file, openRawResource);
                closeQuietly(openRawResource);
                return copyToFile;
            } catch (Throwable th2) {
                th = th2;
                closeQuietly(openRawResource);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            openRawResource = null;
            closeQuietly(openRawResource);
            throw th;
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }
}
