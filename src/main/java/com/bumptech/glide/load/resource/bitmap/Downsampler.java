package com.bumptech.glide.load.resource.bitmap;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.ImageHeaderParser.ImageType;
import com.bumptech.glide.load.ImageHeaderParserUtils;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.SampleSizeRounding;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class Downsampler {
    public static final Option<Boolean> ALLOW_HARDWARE_CONFIG = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.AllowHardwareDecode", Boolean.valueOf(false));
    public static final Option<DecodeFormat> DECODE_FORMAT = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeFormat", DecodeFormat.DEFAULT);
    @Deprecated
    public static final Option<DownsampleStrategy> DOWNSAMPLE_STRATEGY = DownsampleStrategy.OPTION;
    private static final DecodeCallbacks EMPTY_CALLBACKS = new DecodeCallbacks() {
        public void onDecodeComplete(BitmapPool bitmapPool, Bitmap bitmap) {
        }

        public void onObtainBounds() {
        }
    };
    public static final Option<Boolean> FIX_BITMAP_SIZE_TO_REQUESTED_DIMENSIONS = Option.memory("com.bumptech.glide.load.resource.bitmap.Downsampler.FixBitmapSize", Boolean.valueOf(false));
    private static final String ICO_MIME_TYPE = "image/x-ico";
    private static final int MARK_POSITION = 10485760;
    private static final Set<String> NO_DOWNSAMPLE_PRE_N_MIME_TYPES = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{WBMP_MIME_TYPE, ICO_MIME_TYPE})));
    private static final Queue<Options> OPTIONS_QUEUE = Util.createQueue(0);
    static final String TAG = "Downsampler";
    private static final Set<ImageType> TYPES_THAT_USE_POOL_PRE_KITKAT = Collections.unmodifiableSet(EnumSet.of(ImageType.JPEG, ImageType.PNG_A, ImageType.PNG));
    private static final String WBMP_MIME_TYPE = "image/vnd.wap.wbmp";
    private final BitmapPool bitmapPool;
    private final ArrayPool byteArrayPool;
    private final DisplayMetrics displayMetrics;
    private final HardwareConfigState hardwareConfigState = HardwareConfigState.getInstance();
    private final List<ImageHeaderParser> parsers;

    public interface DecodeCallbacks {
        void onDecodeComplete(BitmapPool bitmapPool, Bitmap bitmap) throws IOException;

        void onObtainBounds();
    }

    private static int round(double d) {
        return (int) (d + 0.5d);
    }

    public boolean handles(InputStream inputStream) {
        return true;
    }

    public boolean handles(ByteBuffer byteBuffer) {
        return true;
    }

    public Downsampler(List<ImageHeaderParser> list, DisplayMetrics displayMetrics, BitmapPool bitmapPool, ArrayPool arrayPool) {
        this.parsers = list;
        this.displayMetrics = (DisplayMetrics) Preconditions.checkNotNull(displayMetrics);
        this.bitmapPool = (BitmapPool) Preconditions.checkNotNull(bitmapPool);
        this.byteArrayPool = (ArrayPool) Preconditions.checkNotNull(arrayPool);
    }

    public Resource<Bitmap> decode(InputStream inputStream, int i, int i2, com.bumptech.glide.load.Options options) throws IOException {
        return decode(inputStream, i, i2, options, EMPTY_CALLBACKS);
    }

    public Resource<Bitmap> decode(InputStream inputStream, int i, int i2, com.bumptech.glide.load.Options options, DecodeCallbacks decodeCallbacks) throws IOException {
        com.bumptech.glide.load.Options options2 = options;
        Preconditions.checkArgument(inputStream.markSupported(), "You must provide an InputStream that supports mark()");
        byte[] bArr = (byte[]) this.byteArrayPool.get(65536, byte[].class);
        Options defaultOptions = getDefaultOptions();
        defaultOptions.inTempStorage = bArr;
        DecodeFormat decodeFormat = (DecodeFormat) options2.get(DECODE_FORMAT);
        DownsampleStrategy downsampleStrategy = (DownsampleStrategy) options2.get(DownsampleStrategy.OPTION);
        boolean booleanValue = ((Boolean) options2.get(FIX_BITMAP_SIZE_TO_REQUESTED_DIMENSIONS)).booleanValue();
        boolean z = options2.get(ALLOW_HARDWARE_CONFIG) != null && ((Boolean) options2.get(ALLOW_HARDWARE_CONFIG)).booleanValue();
        try {
            Resource<Bitmap> obtain = BitmapResource.obtain(decodeFromWrappedStreams(inputStream, defaultOptions, downsampleStrategy, decodeFormat, z, i, i2, booleanValue, decodeCallbacks), this.bitmapPool);
            return obtain;
        } finally {
            releaseOptions(defaultOptions);
            this.byteArrayPool.put(bArr);
        }
    }

    private Bitmap decodeFromWrappedStreams(InputStream inputStream, Options options, DownsampleStrategy downsampleStrategy, DecodeFormat decodeFormat, boolean z, int i, int i2, boolean z2, DecodeCallbacks decodeCallbacks) throws IOException {
        int i3;
        int i4;
        Downsampler downsampler;
        int i5;
        InputStream inputStream2 = inputStream;
        Options options2 = options;
        DecodeCallbacks decodeCallbacks2 = decodeCallbacks;
        long logTime = LogTime.getLogTime();
        int[] dimensions = getDimensions(inputStream2, options2, decodeCallbacks2, this.bitmapPool);
        int i6 = 0;
        int i7 = dimensions[0];
        int i8 = dimensions[1];
        String str = options2.outMimeType;
        boolean z3 = (i7 == -1 || i8 == -1) ? false : z;
        int orientation = ImageHeaderParserUtils.getOrientation(this.parsers, inputStream2, this.byteArrayPool);
        int exifOrientationDegrees = TransformationUtils.getExifOrientationDegrees(orientation);
        boolean isExifOrientationRequired = TransformationUtils.isExifOrientationRequired(orientation);
        int i9 = i;
        if (i9 == Integer.MIN_VALUE) {
            i3 = i2;
            i4 = i7;
        } else {
            i3 = i2;
            i4 = i9;
        }
        int i10 = i3 == Integer.MIN_VALUE ? i8 : i3;
        ImageType type = ImageHeaderParserUtils.getType(this.parsers, inputStream2, this.byteArrayPool);
        BitmapPool bitmapPool = this.bitmapPool;
        ImageType imageType = type;
        calculateScaling(type, inputStream, decodeCallbacks, bitmapPool, downsampleStrategy, exifOrientationDegrees, i7, i8, i4, i10, options);
        i9 = orientation;
        String str2 = str;
        int i11 = i8;
        int i12 = i7;
        DecodeCallbacks decodeCallbacks3 = decodeCallbacks2;
        Options options3 = options2;
        calculateConfig(inputStream, decodeFormat, z3, isExifOrientationRequired, options, i4, i10);
        if (VERSION.SDK_INT >= 19) {
            i6 = 1;
        }
        if (options3.inSampleSize == 1 || i6 != 0) {
            downsampler = this;
            if (downsampler.shouldUsePool(imageType)) {
                int ceil;
                if (i12 < 0 || i11 < 0 || !z2 || i6 == 0) {
                    float f = isScaling(options) ? ((float) options3.inTargetDensity) / ((float) options3.inDensity) : 1.0f;
                    int i13 = options3.inSampleSize;
                    float f2 = (float) i13;
                    i7 = (int) Math.ceil((double) (((float) i12) / f2));
                    ceil = (int) Math.ceil((double) (((float) i11) / f2));
                    i7 = Math.round(((float) i7) * f);
                    ceil = Math.round(((float) ceil) * f);
                    if (Log.isLoggable(TAG, 2)) {
                        String str3 = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Calculated target [");
                        stringBuilder.append(i7);
                        stringBuilder.append("x");
                        stringBuilder.append(ceil);
                        stringBuilder.append("] for source [");
                        stringBuilder.append(i12);
                        stringBuilder.append("x");
                        stringBuilder.append(i11);
                        stringBuilder.append("], sampleSize: ");
                        stringBuilder.append(i13);
                        stringBuilder.append(", targetDensity: ");
                        stringBuilder.append(options3.inTargetDensity);
                        stringBuilder.append(", density: ");
                        stringBuilder.append(options3.inDensity);
                        stringBuilder.append(", density multiplier: ");
                        stringBuilder.append(f);
                        Log.v(str3, stringBuilder.toString());
                    }
                } else {
                    i7 = i4;
                    ceil = i10;
                }
                if (i7 > 0 && ceil > 0) {
                    setInBitmap(options3, downsampler.bitmapPool, i7, ceil);
                }
            }
        } else {
            downsampler = this;
        }
        Bitmap decodeStream = decodeStream(inputStream, options3, decodeCallbacks3, downsampler.bitmapPool);
        decodeCallbacks3.onDecodeComplete(downsampler.bitmapPool, decodeStream);
        if (Log.isLoggable(TAG, 2)) {
            i5 = i9;
            logDecode(i12, i11, str2, options, decodeStream, i, i2, logTime);
        } else {
            i5 = i9;
        }
        Bitmap bitmap = null;
        if (decodeStream != null) {
            decodeStream.setDensity(downsampler.displayMetrics.densityDpi);
            bitmap = TransformationUtils.rotateImageExif(downsampler.bitmapPool, decodeStream, i5);
            if (!decodeStream.equals(bitmap)) {
                downsampler.bitmapPool.put(decodeStream);
            }
        }
        return bitmap;
    }

    private static void calculateScaling(ImageType imageType, InputStream inputStream, DecodeCallbacks decodeCallbacks, BitmapPool bitmapPool, DownsampleStrategy downsampleStrategy, int i, int i2, int i3, int i4, int i5, Options options) throws IOException {
        ImageType imageType2 = imageType;
        DownsampleStrategy downsampleStrategy2 = downsampleStrategy;
        int i6 = i;
        int i7 = i2;
        int i8 = i3;
        int i9 = i4;
        int i10 = i5;
        Options options2 = options;
        String str;
        if (i7 <= 0 || i8 <= 0) {
            if (Log.isLoggable(TAG, 3)) {
                str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to determine dimensions for: ");
                stringBuilder.append(imageType);
                stringBuilder.append(" with target [");
                stringBuilder.append(i9);
                stringBuilder.append("x");
                stringBuilder.append(i10);
                stringBuilder.append("]");
                Log.d(str, stringBuilder.toString());
            }
            return;
        }
        float scaleFactor;
        if (i6 == 90 || i6 == 270) {
            scaleFactor = downsampleStrategy2.getScaleFactor(i8, i7, i9, i10);
        } else {
            scaleFactor = downsampleStrategy2.getScaleFactor(i7, i8, i9, i10);
        }
        if (scaleFactor > 0.0f) {
            SampleSizeRounding sampleSizeRounding = downsampleStrategy2.getSampleSizeRounding(i7, i8, i9, i10);
            if (sampleSizeRounding != null) {
                int i11;
                int ceil;
                int ceil2;
                float f = (float) i7;
                float f2 = (float) i8;
                int round = i7 / round((double) (scaleFactor * f));
                int round2 = i8 / round((double) (scaleFactor * f2));
                if (sampleSizeRounding == SampleSizeRounding.MEMORY) {
                    round = Math.max(round, round2);
                } else {
                    round = Math.min(round, round2);
                }
                if (VERSION.SDK_INT > 23 || !NO_DOWNSAMPLE_PRE_N_MIME_TYPES.contains(options2.outMimeType)) {
                    round = Math.max(1, Integer.highestOneBit(round));
                    i11 = (sampleSizeRounding != SampleSizeRounding.MEMORY || ((float) round) >= 1.0f / scaleFactor) ? round : round << 1;
                } else {
                    i11 = 1;
                }
                options2.inSampleSize = i11;
                float min;
                if (imageType2 == ImageType.JPEG) {
                    min = (float) Math.min(i11, 8);
                    ceil = (int) Math.ceil((double) (f / min));
                    ceil2 = (int) Math.ceil((double) (f2 / min));
                    round = i11 / 8;
                    if (round > 0) {
                        ceil /= round;
                        ceil2 /= round;
                    }
                } else if (imageType2 == ImageType.PNG || imageType2 == ImageType.PNG_A) {
                    min = (float) i11;
                    ceil = (int) Math.floor((double) (f / min));
                    ceil2 = (int) Math.floor((double) (f2 / min));
                } else if (imageType2 == ImageType.WEBP || imageType2 == ImageType.WEBP_A) {
                    if (VERSION.SDK_INT >= 24) {
                        min = (float) i11;
                        ceil = Math.round(f / min);
                        ceil2 = Math.round(f2 / min);
                    } else {
                        min = (float) i11;
                        ceil = (int) Math.floor((double) (f / min));
                        ceil2 = (int) Math.floor((double) (f2 / min));
                    }
                } else if (i7 % i11 == 0 && i8 % i11 == 0) {
                    ceil = i7 / i11;
                    ceil2 = i8 / i11;
                } else {
                    int[] dimensions = getDimensions(inputStream, options2, decodeCallbacks, bitmapPool);
                    ceil = dimensions[0];
                    ceil2 = dimensions[1];
                }
                double scaleFactor2 = (double) downsampleStrategy2.getScaleFactor(ceil, ceil2, i9, i10);
                if (VERSION.SDK_INT >= 19) {
                    options2.inTargetDensity = adjustTargetDensityForError(scaleFactor2);
                    options2.inDensity = getDensityMultiplier(scaleFactor2);
                }
                if (isScaling(options)) {
                    options2.inScaled = true;
                } else {
                    options2.inTargetDensity = 0;
                    options2.inDensity = 0;
                }
                if (Log.isLoggable(TAG, 2)) {
                    str = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Calculate scaling, source: [");
                    stringBuilder2.append(i7);
                    stringBuilder2.append("x");
                    stringBuilder2.append(i8);
                    stringBuilder2.append("], target: [");
                    stringBuilder2.append(i9);
                    stringBuilder2.append("x");
                    stringBuilder2.append(i10);
                    stringBuilder2.append("], power of two scaled: [");
                    stringBuilder2.append(ceil);
                    stringBuilder2.append("x");
                    stringBuilder2.append(ceil2);
                    stringBuilder2.append("], exact scale factor: ");
                    stringBuilder2.append(scaleFactor);
                    stringBuilder2.append(", power of 2 sample size: ");
                    stringBuilder2.append(i11);
                    stringBuilder2.append(", adjusted scale factor: ");
                    stringBuilder2.append(scaleFactor2);
                    stringBuilder2.append(", target density: ");
                    stringBuilder2.append(options2.inTargetDensity);
                    stringBuilder2.append(", density: ");
                    stringBuilder2.append(options2.inDensity);
                    Log.v(str, stringBuilder2.toString());
                }
                return;
            }
            throw new IllegalArgumentException("Cannot round with null rounding");
        }
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("Cannot scale with factor: ");
        stringBuilder3.append(scaleFactor);
        stringBuilder3.append(" from: ");
        stringBuilder3.append(downsampleStrategy2);
        stringBuilder3.append(", source: [");
        stringBuilder3.append(i7);
        stringBuilder3.append("x");
        stringBuilder3.append(i8);
        stringBuilder3.append("], target: [");
        stringBuilder3.append(i9);
        stringBuilder3.append("x");
        stringBuilder3.append(i10);
        stringBuilder3.append("]");
        throw new IllegalArgumentException(stringBuilder3.toString());
    }

    private static int adjustTargetDensityForError(double d) {
        int densityMultiplier = getDensityMultiplier(d);
        double d2 = (double) densityMultiplier;
        Double.isNaN(d2);
        int round = round(d2 * d);
        double d3 = (double) (((float) round) / ((float) densityMultiplier));
        Double.isNaN(d3);
        d /= d3;
        double d4 = (double) round;
        Double.isNaN(d4);
        return round(d * d4);
    }

    private static int getDensityMultiplier(double d) {
        if (d > 1.0d) {
            d = 1.0d / d;
        }
        return (int) Math.round(d * 2.147483647E9d);
    }

    private boolean shouldUsePool(ImageType imageType) {
        if (VERSION.SDK_INT >= 19) {
            return true;
        }
        return TYPES_THAT_USE_POOL_PRE_KITKAT.contains(imageType);
    }

    private void calculateConfig(InputStream inputStream, DecodeFormat decodeFormat, boolean z, boolean z2, Options options, int i, int i2) {
        if (!this.hardwareConfigState.setHardwareConfigIfAllowed(i, i2, options, decodeFormat, z, z2)) {
            if (decodeFormat == DecodeFormat.PREFER_ARGB_8888 || VERSION.SDK_INT == 16) {
                options.inPreferredConfig = Config.ARGB_8888;
                return;
            }
            boolean hasAlpha;
            try {
                hasAlpha = ImageHeaderParserUtils.getType(this.parsers, inputStream, this.byteArrayPool).hasAlpha();
            } catch (IOException e) {
                if (Log.isLoggable(TAG, 3)) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Cannot determine whether the image has alpha or not from header, format ");
                    stringBuilder.append(decodeFormat);
                    Log.d(str, stringBuilder.toString(), e);
                }
                hasAlpha = false;
            }
            options.inPreferredConfig = hasAlpha ? Config.ARGB_8888 : Config.RGB_565;
            if (options.inPreferredConfig == Config.RGB_565) {
                options.inDither = true;
            }
        }
    }

    private static int[] getDimensions(InputStream inputStream, Options options, DecodeCallbacks decodeCallbacks, BitmapPool bitmapPool) throws IOException {
        options.inJustDecodeBounds = true;
        decodeStream(inputStream, options, decodeCallbacks, bitmapPool);
        options.inJustDecodeBounds = false;
        return new int[]{options.outWidth, options.outHeight};
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x005f */
    /* JADX WARNING: Can't wrap try/catch for region: R(4:18|19|21|22) */
    private static android.graphics.Bitmap decodeStream(java.io.InputStream r5, android.graphics.BitmapFactory.Options r6, com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeCallbacks r7, com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool r8) throws java.io.IOException {
        /*
        r0 = r6.inJustDecodeBounds;
        if (r0 == 0) goto L_0x000a;
    L_0x0004:
        r0 = 10485760; // 0xa00000 float:1.469368E-38 double:5.180654E-317;
        r5.mark(r0);
        goto L_0x000d;
    L_0x000a:
        r7.onObtainBounds();
    L_0x000d:
        r0 = r6.outWidth;
        r1 = r6.outHeight;
        r2 = r6.outMimeType;
        r3 = com.bumptech.glide.load.resource.bitmap.TransformationUtils.getBitmapDrawableLock();
        r3.lock();
        r3 = 0;
        r4 = android.graphics.BitmapFactory.decodeStream(r5, r3, r6);	 Catch:{ IllegalArgumentException -> 0x0030 }
        r7 = com.bumptech.glide.load.resource.bitmap.TransformationUtils.getBitmapDrawableLock();
        r7.unlock();
        r6 = r6.inJustDecodeBounds;
        if (r6 == 0) goto L_0x002d;
    L_0x002a:
        r5.reset();
    L_0x002d:
        return r4;
    L_0x002e:
        r5 = move-exception;
        goto L_0x0061;
    L_0x0030:
        r4 = move-exception;
        r0 = newIoExceptionForInBitmapAssertion(r4, r0, r1, r2, r6);	 Catch:{ all -> 0x002e }
        r1 = "Downsampler";
        r2 = 3;
        r1 = android.util.Log.isLoggable(r1, r2);	 Catch:{ all -> 0x002e }
        if (r1 == 0) goto L_0x0045;
    L_0x003e:
        r1 = "Downsampler";
        r2 = "Failed to decode with inBitmap, trying again without Bitmap re-use";
        android.util.Log.d(r1, r2, r0);	 Catch:{ all -> 0x002e }
    L_0x0045:
        r1 = r6.inBitmap;	 Catch:{ all -> 0x002e }
        if (r1 == 0) goto L_0x0060;
    L_0x0049:
        r5.reset();	 Catch:{ IOException -> 0x005f }
        r1 = r6.inBitmap;	 Catch:{ IOException -> 0x005f }
        r8.put(r1);	 Catch:{ IOException -> 0x005f }
        r6.inBitmap = r3;	 Catch:{ IOException -> 0x005f }
        r5 = decodeStream(r5, r6, r7, r8);	 Catch:{ IOException -> 0x005f }
        r6 = com.bumptech.glide.load.resource.bitmap.TransformationUtils.getBitmapDrawableLock();
        r6.unlock();
        return r5;
    L_0x005f:
        throw r0;	 Catch:{ all -> 0x002e }
    L_0x0060:
        throw r0;	 Catch:{ all -> 0x002e }
    L_0x0061:
        r6 = com.bumptech.glide.load.resource.bitmap.TransformationUtils.getBitmapDrawableLock();
        r6.unlock();
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.load.resource.bitmap.Downsampler.decodeStream(java.io.InputStream, android.graphics.BitmapFactory$Options, com.bumptech.glide.load.resource.bitmap.Downsampler$DecodeCallbacks, com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool):android.graphics.Bitmap");
    }

    private static boolean isScaling(Options options) {
        return options.inTargetDensity > 0 && options.inDensity > 0 && options.inTargetDensity != options.inDensity;
    }

    private static void logDecode(int i, int i2, String str, Options options, Bitmap bitmap, int i3, int i4, long j) {
        String str2 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Decoded ");
        stringBuilder.append(getBitmapString(bitmap));
        stringBuilder.append(" from [");
        stringBuilder.append(i);
        stringBuilder.append("x");
        stringBuilder.append(i2);
        stringBuilder.append("] ");
        stringBuilder.append(str);
        stringBuilder.append(" with inBitmap ");
        stringBuilder.append(getInBitmapString(options));
        stringBuilder.append(" for [");
        stringBuilder.append(i3);
        stringBuilder.append("x");
        stringBuilder.append(i4);
        stringBuilder.append("], sample size: ");
        stringBuilder.append(options.inSampleSize);
        stringBuilder.append(", density: ");
        stringBuilder.append(options.inDensity);
        stringBuilder.append(", target density: ");
        stringBuilder.append(options.inTargetDensity);
        stringBuilder.append(", thread: ");
        stringBuilder.append(Thread.currentThread().getName());
        stringBuilder.append(", duration: ");
        stringBuilder.append(LogTime.getElapsedMillis(j));
        Log.v(str2, stringBuilder.toString());
    }

    private static String getInBitmapString(Options options) {
        return getBitmapString(options.inBitmap);
    }

    @Nullable
    @TargetApi(19)
    private static String getBitmapString(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        String stringBuilder;
        if (VERSION.SDK_INT >= 19) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(" (");
            stringBuilder2.append(bitmap.getAllocationByteCount());
            stringBuilder2.append(")");
            stringBuilder = stringBuilder2.toString();
        } else {
            stringBuilder = "";
        }
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("[");
        stringBuilder3.append(bitmap.getWidth());
        stringBuilder3.append("x");
        stringBuilder3.append(bitmap.getHeight());
        stringBuilder3.append("] ");
        stringBuilder3.append(bitmap.getConfig());
        stringBuilder3.append(stringBuilder);
        return stringBuilder3.toString();
    }

    private static IOException newIoExceptionForInBitmapAssertion(IllegalArgumentException illegalArgumentException, int i, int i2, String str, Options options) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Exception decoding bitmap, outWidth: ");
        stringBuilder.append(i);
        stringBuilder.append(", outHeight: ");
        stringBuilder.append(i2);
        stringBuilder.append(", outMimeType: ");
        stringBuilder.append(str);
        stringBuilder.append(", inBitmap: ");
        stringBuilder.append(getInBitmapString(options));
        return new IOException(stringBuilder.toString(), illegalArgumentException);
    }

    @TargetApi(26)
    private static void setInBitmap(Options options, BitmapPool bitmapPool, int i, int i2) {
        Config config;
        if (VERSION.SDK_INT < 26) {
            config = null;
        } else if (options.inPreferredConfig != Config.HARDWARE) {
            config = options.outConfig;
        } else {
            return;
        }
        if (config == null) {
            config = options.inPreferredConfig;
        }
        options.inBitmap = bitmapPool.getDirty(i, i2, config);
    }

    private static synchronized Options getDefaultOptions() {
        Options options;
        synchronized (Downsampler.class) {
            synchronized (OPTIONS_QUEUE) {
                options = (Options) OPTIONS_QUEUE.poll();
            }
            if (options == null) {
                options = new Options();
                resetOptions(options);
            }
        }
        return options;
    }

    private static void releaseOptions(Options options) {
        resetOptions(options);
        synchronized (OPTIONS_QUEUE) {
            OPTIONS_QUEUE.offer(options);
        }
    }

    private static void resetOptions(Options options) {
        options.inTempStorage = null;
        options.inDither = false;
        options.inScaled = false;
        options.inSampleSize = 1;
        options.inPreferredConfig = null;
        options.inJustDecodeBounds = false;
        options.inDensity = 0;
        options.inTargetDensity = 0;
        options.outWidth = 0;
        options.outHeight = 0;
        options.outMimeType = null;
        options.inBitmap = null;
        options.inMutable = true;
    }
}
