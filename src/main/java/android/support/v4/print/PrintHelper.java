package android.support.v4.print;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintAttributes.Margins;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;
import android.print.PrintDocumentInfo.Builder;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.FileNotFoundException;

public final class PrintHelper {
    @SuppressLint({"InlinedApi"})
    public static final int COLOR_MODE_COLOR = 2;
    @SuppressLint({"InlinedApi"})
    public static final int COLOR_MODE_MONOCHROME = 1;
    static final boolean IS_MIN_MARGINS_HANDLING_CORRECT;
    private static final String LOG_TAG = "PrintHelper";
    private static final int MAX_PRINT_SIZE = 3500;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    static final boolean PRINT_ACTIVITY_RESPECTS_ORIENTATION;
    public static final int SCALE_MODE_FILL = 2;
    public static final int SCALE_MODE_FIT = 1;
    int mColorMode = 2;
    final Context mContext;
    Options mDecodeOptions = null;
    final Object mLock = new Object();
    int mOrientation = 1;
    int mScaleMode = 2;

    public interface OnPrintFinishCallback {
        void onFinish();
    }

    @RequiresApi(19)
    private class PrintBitmapAdapter extends PrintDocumentAdapter {
        private PrintAttributes mAttributes;
        private final Bitmap mBitmap;
        private final OnPrintFinishCallback mCallback;
        private final int mFittingMode;
        private final String mJobName;

        PrintBitmapAdapter(String str, int i, Bitmap bitmap, OnPrintFinishCallback onPrintFinishCallback) {
            this.mJobName = str;
            this.mFittingMode = i;
            this.mBitmap = bitmap;
            this.mCallback = onPrintFinishCallback;
        }

        public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes2, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
            this.mAttributes = printAttributes2;
            layoutResultCallback.onLayoutFinished(new Builder(this.mJobName).setContentType(1).setPageCount(1).build(), printAttributes2.equals(printAttributes) ^ 1);
        }

        public void onWrite(PageRange[] pageRangeArr, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            PrintHelper.this.writeBitmap(this.mAttributes, this.mFittingMode, this.mBitmap, parcelFileDescriptor, cancellationSignal, writeResultCallback);
        }

        public void onFinish() {
            if (this.mCallback != null) {
                this.mCallback.onFinish();
            }
        }
    }

    @RequiresApi(19)
    private class PrintUriAdapter extends PrintDocumentAdapter {
        PrintAttributes mAttributes;
        Bitmap mBitmap = null;
        final OnPrintFinishCallback mCallback;
        final int mFittingMode;
        final Uri mImageFile;
        final String mJobName;
        AsyncTask<Uri, Boolean, Bitmap> mLoadBitmap;

        PrintUriAdapter(String str, Uri uri, OnPrintFinishCallback onPrintFinishCallback, int i) {
            this.mJobName = str;
            this.mImageFile = uri;
            this.mCallback = onPrintFinishCallback;
            this.mFittingMode = i;
        }

        public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes2, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
            synchronized (this) {
                this.mAttributes = printAttributes2;
            }
            if (cancellationSignal.isCanceled()) {
                layoutResultCallback.onLayoutCancelled();
            } else if (this.mBitmap != null) {
                layoutResultCallback.onLayoutFinished(new Builder(this.mJobName).setContentType(1).setPageCount(1).build(), printAttributes2.equals(printAttributes) ^ 1);
            } else {
                final CancellationSignal cancellationSignal2 = cancellationSignal;
                final PrintAttributes printAttributes3 = printAttributes2;
                final PrintAttributes printAttributes4 = printAttributes;
                final LayoutResultCallback layoutResultCallback2 = layoutResultCallback;
                this.mLoadBitmap = new AsyncTask<Uri, Boolean, Bitmap>() {
                    /* Access modifiers changed, original: protected */
                    public void onPreExecute() {
                        cancellationSignal2.setOnCancelListener(new OnCancelListener() {
                            public void onCancel() {
                                PrintUriAdapter.this.cancelLoad();
                                AnonymousClass1.this.cancel(false);
                            }
                        });
                    }

                    /* Access modifiers changed, original: protected|varargs */
                    public Bitmap doInBackground(Uri... uriArr) {
                        try {
                            return PrintHelper.this.loadConstrainedBitmap(PrintUriAdapter.this.mImageFile);
                        } catch (FileNotFoundException unused) {
                            return null;
                        }
                    }

                    /* Access modifiers changed, original: protected */
                    public void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        if (bitmap != null && (!PrintHelper.PRINT_ACTIVITY_RESPECTS_ORIENTATION || PrintHelper.this.mOrientation == 0)) {
                            MediaSize mediaSize;
                            synchronized (this) {
                                mediaSize = PrintUriAdapter.this.mAttributes.getMediaSize();
                            }
                            if (!(mediaSize == null || mediaSize.isPortrait() == PrintHelper.isPortrait(bitmap))) {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90.0f);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            }
                        }
                        PrintUriAdapter.this.mBitmap = bitmap;
                        if (bitmap != null) {
                            layoutResultCallback2.onLayoutFinished(new Builder(PrintUriAdapter.this.mJobName).setContentType(1).setPageCount(1).build(), 1 ^ printAttributes3.equals(printAttributes4));
                        } else {
                            layoutResultCallback2.onLayoutFailed(null);
                        }
                        PrintUriAdapter.this.mLoadBitmap = null;
                    }

                    /* Access modifiers changed, original: protected */
                    public void onCancelled(Bitmap bitmap) {
                        layoutResultCallback2.onLayoutCancelled();
                        PrintUriAdapter.this.mLoadBitmap = null;
                    }
                }.execute(new Uri[0]);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void cancelLoad() {
            synchronized (PrintHelper.this.mLock) {
                if (PrintHelper.this.mDecodeOptions != null) {
                    if (VERSION.SDK_INT < 24) {
                        PrintHelper.this.mDecodeOptions.requestCancelDecode();
                    }
                    PrintHelper.this.mDecodeOptions = null;
                }
            }
        }

        public void onFinish() {
            super.onFinish();
            cancelLoad();
            if (this.mLoadBitmap != null) {
                this.mLoadBitmap.cancel(true);
            }
            if (this.mCallback != null) {
                this.mCallback.onFinish();
            }
            if (this.mBitmap != null) {
                this.mBitmap.recycle();
                this.mBitmap = null;
            }
        }

        public void onWrite(PageRange[] pageRangeArr, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            PrintHelper.this.writeBitmap(this.mAttributes, this.mFittingMode, this.mBitmap, parcelFileDescriptor, cancellationSignal, writeResultCallback);
        }
    }

    static {
        boolean z = false;
        boolean z2 = VERSION.SDK_INT < 20 || VERSION.SDK_INT > 23;
        PRINT_ACTIVITY_RESPECTS_ORIENTATION = z2;
        if (VERSION.SDK_INT != 23) {
            z = true;
        }
        IS_MIN_MARGINS_HANDLING_CORRECT = z;
    }

    public static boolean systemSupportsPrint() {
        return VERSION.SDK_INT >= 19;
    }

    public PrintHelper(@NonNull Context context) {
        this.mContext = context;
    }

    public void setScaleMode(int i) {
        this.mScaleMode = i;
    }

    public int getScaleMode() {
        return this.mScaleMode;
    }

    public void setColorMode(int i) {
        this.mColorMode = i;
    }

    public int getColorMode() {
        return this.mColorMode;
    }

    public void setOrientation(int i) {
        this.mOrientation = i;
    }

    public int getOrientation() {
        if (VERSION.SDK_INT < 19 || this.mOrientation != 0) {
            return this.mOrientation;
        }
        return 1;
    }

    public void printBitmap(@NonNull String str, @NonNull Bitmap bitmap) {
        printBitmap(str, bitmap, null);
    }

    public void printBitmap(@NonNull String str, @NonNull Bitmap bitmap, @Nullable OnPrintFinishCallback onPrintFinishCallback) {
        if (VERSION.SDK_INT >= 19 && bitmap != null) {
            MediaSize mediaSize;
            PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
            if (isPortrait(bitmap)) {
                mediaSize = MediaSize.UNKNOWN_PORTRAIT;
            } else {
                mediaSize = MediaSize.UNKNOWN_LANDSCAPE;
            }
            printManager.print(str, new PrintBitmapAdapter(str, this.mScaleMode, bitmap, onPrintFinishCallback), new PrintAttributes.Builder().setMediaSize(mediaSize).setColorMode(this.mColorMode).build());
        }
    }

    public void printBitmap(@NonNull String str, @NonNull Uri uri) throws FileNotFoundException {
        printBitmap(str, uri, null);
    }

    public void printBitmap(@NonNull String str, @NonNull Uri uri, @Nullable OnPrintFinishCallback onPrintFinishCallback) throws FileNotFoundException {
        if (VERSION.SDK_INT >= 19) {
            PrintUriAdapter printUriAdapter = new PrintUriAdapter(str, uri, onPrintFinishCallback, this.mScaleMode);
            PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setColorMode(this.mColorMode);
            if (this.mOrientation == 1 || this.mOrientation == 0) {
                builder.setMediaSize(MediaSize.UNKNOWN_LANDSCAPE);
            } else if (this.mOrientation == 2) {
                builder.setMediaSize(MediaSize.UNKNOWN_PORTRAIT);
            }
            printManager.print(str, printUriAdapter, builder.build());
        }
    }

    static boolean isPortrait(Bitmap bitmap) {
        return bitmap.getWidth() <= bitmap.getHeight();
    }

    @RequiresApi(19)
    private static PrintAttributes.Builder copyAttributes(PrintAttributes printAttributes) {
        PrintAttributes.Builder minMargins = new PrintAttributes.Builder().setMediaSize(printAttributes.getMediaSize()).setResolution(printAttributes.getResolution()).setMinMargins(printAttributes.getMinMargins());
        if (printAttributes.getColorMode() != 0) {
            minMargins.setColorMode(printAttributes.getColorMode());
        }
        if (VERSION.SDK_INT >= 23 && printAttributes.getDuplexMode() != 0) {
            minMargins.setDuplexMode(printAttributes.getDuplexMode());
        }
        return minMargins;
    }

    static Matrix getMatrix(int i, int i2, RectF rectF, int i3) {
        float max;
        Matrix matrix = new Matrix();
        float f = (float) i;
        float width = rectF.width() / f;
        if (i3 == 2) {
            max = Math.max(width, rectF.height() / ((float) i2));
        } else {
            max = Math.min(width, rectF.height() / ((float) i2));
        }
        matrix.postScale(max, max);
        matrix.postTranslate((rectF.width() - (f * max)) / 2.0f, (rectF.height() - (((float) i2) * max)) / 2.0f);
        return matrix;
    }

    /* Access modifiers changed, original: 0000 */
    @RequiresApi(19)
    public void writeBitmap(PrintAttributes printAttributes, int i, Bitmap bitmap, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
        PrintAttributes printAttributes2;
        if (IS_MIN_MARGINS_HANDLING_CORRECT) {
            printAttributes2 = printAttributes;
        } else {
            printAttributes2 = copyAttributes(printAttributes).setMinMargins(new Margins(0, 0, 0, 0)).build();
        }
        final CancellationSignal cancellationSignal2 = cancellationSignal;
        final Bitmap bitmap2 = bitmap;
        final PrintAttributes printAttributes3 = printAttributes;
        final int i2 = i;
        final ParcelFileDescriptor parcelFileDescriptor2 = parcelFileDescriptor;
        final WriteResultCallback writeResultCallback2 = writeResultCallback;
        new AsyncTask<Void, Void, Throwable>() {
            /* Access modifiers changed, original: protected|varargs */
            /* JADX WARNING: Missing exception handler attribute for start block: B:46:0x00da */
            /* JADX WARNING: Removed duplicated region for block: B:39:0x00c9 A:{Catch:{ all -> 0x00cd, Throwable -> 0x00e2 }} */
            /* JADX WARNING: Missing exception handler attribute for start block: B:36:0x00c5 */
            /* JADX WARNING: Removed duplicated region for block: B:27:0x00a7 A:{Catch:{ all -> 0x00cd, Throwable -> 0x00e2 }} */
            /* JADX WARNING: Missing exception handler attribute for start block: B:24:0x00a3 */
            /* JADX WARNING: Removed duplicated region for block: B:49:0x00de A:{Catch:{ all -> 0x00cd, Throwable -> 0x00e2 }} */
            /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
            /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
            /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
            /* JADX WARNING: Missing block: B:49:0x00de, code skipped:
            r1.recycle();
     */
            public java.lang.Throwable doInBackground(java.lang.Void... r8) {
                /*
                r7 = this;
                r8 = r4;	 Catch:{ Throwable -> 0x00e2 }
                r8 = r8.isCanceled();	 Catch:{ Throwable -> 0x00e2 }
                r0 = 0;
                if (r8 == 0) goto L_0x000a;
            L_0x0009:
                return r0;
            L_0x000a:
                r8 = new android.print.pdf.PrintedPdfDocument;	 Catch:{ Throwable -> 0x00e2 }
                r1 = android.support.v4.print.PrintHelper.this;	 Catch:{ Throwable -> 0x00e2 }
                r1 = r1.mContext;	 Catch:{ Throwable -> 0x00e2 }
                r2 = r5;	 Catch:{ Throwable -> 0x00e2 }
                r8.<init>(r1, r2);	 Catch:{ Throwable -> 0x00e2 }
                r1 = r6;	 Catch:{ Throwable -> 0x00e2 }
                r2 = r5;	 Catch:{ Throwable -> 0x00e2 }
                r2 = r2.getColorMode();	 Catch:{ Throwable -> 0x00e2 }
                r1 = android.support.v4.print.PrintHelper.convertBitmapForColorMode(r1, r2);	 Catch:{ Throwable -> 0x00e2 }
                r2 = r4;	 Catch:{ Throwable -> 0x00e2 }
                r2 = r2.isCanceled();	 Catch:{ Throwable -> 0x00e2 }
                if (r2 == 0) goto L_0x002a;
            L_0x0029:
                return r0;
            L_0x002a:
                r2 = 1;
                r3 = r8.startPage(r2);	 Catch:{ all -> 0x00cd }
                r4 = android.support.v4.print.PrintHelper.IS_MIN_MARGINS_HANDLING_CORRECT;	 Catch:{ all -> 0x00cd }
                if (r4 == 0) goto L_0x0041;
            L_0x0033:
                r2 = new android.graphics.RectF;	 Catch:{ all -> 0x00cd }
                r4 = r3.getInfo();	 Catch:{ all -> 0x00cd }
                r4 = r4.getContentRect();	 Catch:{ all -> 0x00cd }
                r2.<init>(r4);	 Catch:{ all -> 0x00cd }
                goto L_0x0064;
            L_0x0041:
                r4 = new android.print.pdf.PrintedPdfDocument;	 Catch:{ all -> 0x00cd }
                r5 = android.support.v4.print.PrintHelper.this;	 Catch:{ all -> 0x00cd }
                r5 = r5.mContext;	 Catch:{ all -> 0x00cd }
                r6 = r7;	 Catch:{ all -> 0x00cd }
                r4.<init>(r5, r6);	 Catch:{ all -> 0x00cd }
                r2 = r4.startPage(r2);	 Catch:{ all -> 0x00cd }
                r5 = new android.graphics.RectF;	 Catch:{ all -> 0x00cd }
                r6 = r2.getInfo();	 Catch:{ all -> 0x00cd }
                r6 = r6.getContentRect();	 Catch:{ all -> 0x00cd }
                r5.<init>(r6);	 Catch:{ all -> 0x00cd }
                r4.finishPage(r2);	 Catch:{ all -> 0x00cd }
                r4.close();	 Catch:{ all -> 0x00cd }
                r2 = r5;
            L_0x0064:
                r4 = r1.getWidth();	 Catch:{ all -> 0x00cd }
                r5 = r1.getHeight();	 Catch:{ all -> 0x00cd }
                r6 = r8;	 Catch:{ all -> 0x00cd }
                r4 = android.support.v4.print.PrintHelper.getMatrix(r4, r5, r2, r6);	 Catch:{ all -> 0x00cd }
                r5 = android.support.v4.print.PrintHelper.IS_MIN_MARGINS_HANDLING_CORRECT;	 Catch:{ all -> 0x00cd }
                if (r5 == 0) goto L_0x0077;
            L_0x0076:
                goto L_0x0085;
            L_0x0077:
                r5 = r2.left;	 Catch:{ all -> 0x00cd }
                r6 = r2.top;	 Catch:{ all -> 0x00cd }
                r4.postTranslate(r5, r6);	 Catch:{ all -> 0x00cd }
                r5 = r3.getCanvas();	 Catch:{ all -> 0x00cd }
                r5.clipRect(r2);	 Catch:{ all -> 0x00cd }
            L_0x0085:
                r2 = r3.getCanvas();	 Catch:{ all -> 0x00cd }
                r2.drawBitmap(r1, r4, r0);	 Catch:{ all -> 0x00cd }
                r8.finishPage(r3);	 Catch:{ all -> 0x00cd }
                r2 = r4;	 Catch:{ all -> 0x00cd }
                r2 = r2.isCanceled();	 Catch:{ all -> 0x00cd }
                if (r2 == 0) goto L_0x00ab;
            L_0x0097:
                r8.close();	 Catch:{ Throwable -> 0x00e2 }
                r8 = r9;	 Catch:{ Throwable -> 0x00e2 }
                if (r8 == 0) goto L_0x00a3;
            L_0x009e:
                r8 = r9;	 Catch:{ IOException -> 0x00a3 }
                r8.close();	 Catch:{ IOException -> 0x00a3 }
            L_0x00a3:
                r8 = r6;	 Catch:{ Throwable -> 0x00e2 }
                if (r1 == r8) goto L_0x00aa;
            L_0x00a7:
                r1.recycle();	 Catch:{ Throwable -> 0x00e2 }
            L_0x00aa:
                return r0;
            L_0x00ab:
                r2 = new java.io.FileOutputStream;	 Catch:{ all -> 0x00cd }
                r3 = r9;	 Catch:{ all -> 0x00cd }
                r3 = r3.getFileDescriptor();	 Catch:{ all -> 0x00cd }
                r2.<init>(r3);	 Catch:{ all -> 0x00cd }
                r8.writeTo(r2);	 Catch:{ all -> 0x00cd }
                r8.close();	 Catch:{ Throwable -> 0x00e2 }
                r8 = r9;	 Catch:{ Throwable -> 0x00e2 }
                if (r8 == 0) goto L_0x00c5;
            L_0x00c0:
                r8 = r9;	 Catch:{ IOException -> 0x00c5 }
                r8.close();	 Catch:{ IOException -> 0x00c5 }
            L_0x00c5:
                r8 = r6;	 Catch:{ Throwable -> 0x00e2 }
                if (r1 == r8) goto L_0x00cc;
            L_0x00c9:
                r1.recycle();	 Catch:{ Throwable -> 0x00e2 }
            L_0x00cc:
                return r0;
            L_0x00cd:
                r0 = move-exception;
                r8.close();	 Catch:{ Throwable -> 0x00e2 }
                r8 = r9;	 Catch:{ Throwable -> 0x00e2 }
                if (r8 == 0) goto L_0x00da;
            L_0x00d5:
                r8 = r9;	 Catch:{ IOException -> 0x00da }
                r8.close();	 Catch:{ IOException -> 0x00da }
            L_0x00da:
                r8 = r6;	 Catch:{ Throwable -> 0x00e2 }
                if (r1 == r8) goto L_0x00e1;
            L_0x00de:
                r1.recycle();	 Catch:{ Throwable -> 0x00e2 }
            L_0x00e1:
                throw r0;	 Catch:{ Throwable -> 0x00e2 }
            L_0x00e2:
                r8 = move-exception;
                return r8;
                */
                throw new UnsupportedOperationException("Method not decompiled: android.support.v4.print.PrintHelper$AnonymousClass1.doInBackground(java.lang.Void[]):java.lang.Throwable");
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(Throwable th) {
                if (cancellationSignal2.isCanceled()) {
                    writeResultCallback2.onWriteCancelled();
                } else if (th == null) {
                    writeResultCallback2.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } else {
                    Log.e(PrintHelper.LOG_TAG, "Error writing printed content", th);
                    writeResultCallback2.onWriteFailed(null);
                }
            }
        }.execute(new Void[0]);
    }

    /* Access modifiers changed, original: 0000 */
    public Bitmap loadConstrainedBitmap(Uri uri) throws FileNotFoundException {
        if (uri == null || this.mContext == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        loadBitmap(uri, options);
        int i = options.outWidth;
        int i2 = options.outHeight;
        if (i <= 0 || i2 <= 0) {
            return null;
        }
        int max = Math.max(i, i2);
        int i3 = 1;
        while (max > MAX_PRINT_SIZE) {
            max >>>= 1;
            i3 <<= 1;
        }
        if (i3 <= 0 || Math.min(i, i2) / i3 <= 0) {
            return null;
        }
        Options options2;
        synchronized (this.mLock) {
            this.mDecodeOptions = new Options();
            this.mDecodeOptions.inMutable = true;
            this.mDecodeOptions.inSampleSize = i3;
            options2 = this.mDecodeOptions;
        }
        try {
            Bitmap loadBitmap = loadBitmap(uri, options2);
            synchronized (this.mLock) {
                this.mDecodeOptions = null;
            }
            return loadBitmap;
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mDecodeOptions = null;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x002a A:{SYNTHETIC, Splitter:B:18:0x002a} */
    private android.graphics.Bitmap loadBitmap(android.net.Uri r3, android.graphics.BitmapFactory.Options r4) throws java.io.FileNotFoundException {
        /*
        r2 = this;
        if (r3 == 0) goto L_0x0037;
    L_0x0002:
        r0 = r2.mContext;
        if (r0 == 0) goto L_0x0037;
    L_0x0006:
        r0 = 0;
        r1 = r2.mContext;	 Catch:{ all -> 0x0027 }
        r1 = r1.getContentResolver();	 Catch:{ all -> 0x0027 }
        r3 = r1.openInputStream(r3);	 Catch:{ all -> 0x0027 }
        r4 = android.graphics.BitmapFactory.decodeStream(r3, r0, r4);	 Catch:{ all -> 0x0024 }
        if (r3 == 0) goto L_0x0023;
    L_0x0017:
        r3.close();	 Catch:{ IOException -> 0x001b }
        goto L_0x0023;
    L_0x001b:
        r3 = move-exception;
        r0 = "PrintHelper";
        r1 = "close fail ";
        android.util.Log.w(r0, r1, r3);
    L_0x0023:
        return r4;
    L_0x0024:
        r4 = move-exception;
        r0 = r3;
        goto L_0x0028;
    L_0x0027:
        r4 = move-exception;
    L_0x0028:
        if (r0 == 0) goto L_0x0036;
    L_0x002a:
        r0.close();	 Catch:{ IOException -> 0x002e }
        goto L_0x0036;
    L_0x002e:
        r3 = move-exception;
        r0 = "PrintHelper";
        r1 = "close fail ";
        android.util.Log.w(r0, r1, r3);
    L_0x0036:
        throw r4;
    L_0x0037:
        r3 = new java.lang.IllegalArgumentException;
        r4 = "bad argument to loadBitmap";
        r3.<init>(r4);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.print.PrintHelper.loadBitmap(android.net.Uri, android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
    }

    static Bitmap convertBitmapForColorMode(Bitmap bitmap, int i) {
        if (i != 1) {
            return bitmap;
        }
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        canvas.setBitmap(null);
        return createBitmap;
    }
}
