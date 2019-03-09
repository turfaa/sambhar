package com.bumptech.glide.load.resource.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.gifdecoder.GifDecoder.BitmapProvider;
import com.bumptech.glide.gifdecoder.GifHeader;
import com.bumptech.glide.gifdecoder.GifHeaderParser;
import com.bumptech.glide.gifdecoder.StandardGifDecoder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.ImageHeaderParser.ImageType;
import com.bumptech.glide.load.ImageHeaderParserUtils;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.UnitTransformation;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.Util;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;

public class ByteBufferGifDecoder implements ResourceDecoder<ByteBuffer, GifDrawable> {
    private static final GifDecoderFactory GIF_DECODER_FACTORY = new GifDecoderFactory();
    private static final GifHeaderParserPool PARSER_POOL = new GifHeaderParserPool();
    private static final String TAG = "BufferGifDecoder";
    private final Context context;
    private final GifDecoderFactory gifDecoderFactory;
    private final GifHeaderParserPool parserPool;
    private final List<ImageHeaderParser> parsers;
    private final GifBitmapProvider provider;

    @VisibleForTesting
    static class GifDecoderFactory {
        GifDecoderFactory() {
        }

        /* Access modifiers changed, original: 0000 */
        public GifDecoder build(BitmapProvider bitmapProvider, GifHeader gifHeader, ByteBuffer byteBuffer, int i) {
            return new StandardGifDecoder(bitmapProvider, gifHeader, byteBuffer, i);
        }
    }

    @VisibleForTesting
    static class GifHeaderParserPool {
        private final Queue<GifHeaderParser> pool = Util.createQueue(0);

        GifHeaderParserPool() {
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized GifHeaderParser obtain(ByteBuffer byteBuffer) {
            GifHeaderParser gifHeaderParser;
            gifHeaderParser = (GifHeaderParser) this.pool.poll();
            if (gifHeaderParser == null) {
                gifHeaderParser = new GifHeaderParser();
            }
            return gifHeaderParser.setData(byteBuffer);
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized void release(GifHeaderParser gifHeaderParser) {
            gifHeaderParser.clear();
            this.pool.offer(gifHeaderParser);
        }
    }

    public ByteBufferGifDecoder(Context context) {
        this(context, Glide.get(context).getRegistry().getImageHeaderParsers(), Glide.get(context).getBitmapPool(), Glide.get(context).getArrayPool());
    }

    public ByteBufferGifDecoder(Context context, List<ImageHeaderParser> list, BitmapPool bitmapPool, ArrayPool arrayPool) {
        this(context, list, bitmapPool, arrayPool, PARSER_POOL, GIF_DECODER_FACTORY);
    }

    @VisibleForTesting
    ByteBufferGifDecoder(Context context, List<ImageHeaderParser> list, BitmapPool bitmapPool, ArrayPool arrayPool, GifHeaderParserPool gifHeaderParserPool, GifDecoderFactory gifDecoderFactory) {
        this.context = context.getApplicationContext();
        this.parsers = list;
        this.gifDecoderFactory = gifDecoderFactory;
        this.provider = new GifBitmapProvider(bitmapPool, arrayPool);
        this.parserPool = gifHeaderParserPool;
    }

    public boolean handles(@NonNull ByteBuffer byteBuffer, @NonNull Options options) throws IOException {
        return !((Boolean) options.get(GifOptions.DISABLE_ANIMATION)).booleanValue() && ImageHeaderParserUtils.getType(this.parsers, byteBuffer) == ImageType.GIF;
    }

    public GifDrawableResource decode(@NonNull ByteBuffer byteBuffer, int i, int i2, @NonNull Options options) {
        GifHeaderParser obtain = this.parserPool.obtain(byteBuffer);
        try {
            GifDrawableResource decode = decode(byteBuffer, i, i2, obtain, options);
            return decode;
        } finally {
            this.parserPool.release(obtain);
        }
    }

    @Nullable
    private GifDrawableResource decode(ByteBuffer byteBuffer, int i, int i2, GifHeaderParser gifHeaderParser, Options options) {
        long logTime = LogTime.getLogTime();
        StringBuilder stringBuilder;
        try {
            String str;
            GifHeader parseHeader = gifHeaderParser.parseHeader();
            if (parseHeader.getNumFrames() > 0) {
                if (parseHeader.getStatus() == 0) {
                    Config config = options.get(GifOptions.DECODE_FORMAT) == DecodeFormat.PREFER_RGB_565 ? Config.RGB_565 : Config.ARGB_8888;
                    ByteBuffer byteBuffer2 = byteBuffer;
                    GifDecoder build = this.gifDecoderFactory.build(this.provider, parseHeader, byteBuffer, getSampleSize(parseHeader, i, i2));
                    build.setDefaultBitmapConfig(config);
                    build.advance();
                    Bitmap nextFrame = build.getNextFrame();
                    if (nextFrame == null) {
                        if (Log.isLoggable(TAG, 2)) {
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Decoded GIF from stream in ");
                            stringBuilder.append(LogTime.getElapsedMillis(logTime));
                            Log.v(str, stringBuilder.toString());
                        }
                        return null;
                    }
                    GifDrawableResource gifDrawableResource = new GifDrawableResource(new GifDrawable(this.context, build, UnitTransformation.get(), i, i2, nextFrame));
                    if (Log.isLoggable(TAG, 2)) {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Decoded GIF from stream in ");
                        stringBuilder.append(LogTime.getElapsedMillis(logTime));
                        Log.v(str, stringBuilder.toString());
                    }
                    return gifDrawableResource;
                }
            }
            if (Log.isLoggable(TAG, 2)) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Decoded GIF from stream in ");
                stringBuilder.append(LogTime.getElapsedMillis(logTime));
                Log.v(str, stringBuilder.toString());
            }
            return null;
        } catch (Throwable th) {
            if (Log.isLoggable(TAG, 2)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Decoded GIF from stream in ");
                stringBuilder.append(LogTime.getElapsedMillis(logTime));
                Log.v(TAG, stringBuilder.toString());
            }
        }
    }

    private static int getSampleSize(GifHeader gifHeader, int i, int i2) {
        int min = Math.min(gifHeader.getHeight() / i2, gifHeader.getWidth() / i);
        if (min == 0) {
            min = 0;
        } else {
            min = Integer.highestOneBit(min);
        }
        min = Math.max(1, min);
        if (Log.isLoggable(TAG, 2) && min > 1) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Downsampling GIF, sampleSize: ");
            stringBuilder.append(min);
            stringBuilder.append(", target dimens: [");
            stringBuilder.append(i);
            stringBuilder.append("x");
            stringBuilder.append(i2);
            stringBuilder.append("], actual dimens: [");
            stringBuilder.append(gifHeader.getWidth());
            stringBuilder.append("x");
            stringBuilder.append(gifHeader.getHeight());
            stringBuilder.append("]");
            Log.v(str, stringBuilder.toString());
        }
        return min;
    }
}
