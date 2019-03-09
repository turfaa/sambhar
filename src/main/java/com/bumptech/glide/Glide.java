package com.bumptech.glide;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.data.InputStreamRewinder;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.prefill.BitmapPreFiller;
import com.bumptech.glide.load.engine.prefill.PreFillType.Builder;
import com.bumptech.glide.load.model.AssetUriLoader;
import com.bumptech.glide.load.model.ByteArrayLoader;
import com.bumptech.glide.load.model.ByteArrayLoader.ByteBufferFactory;
import com.bumptech.glide.load.model.ByteBufferEncoder;
import com.bumptech.glide.load.model.ByteBufferFileLoader;
import com.bumptech.glide.load.model.DataUrlLoader;
import com.bumptech.glide.load.model.FileLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.MediaStoreFileLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.ResourceLoader.AssetFileDescriptorFactory;
import com.bumptech.glide.load.model.ResourceLoader.FileDescriptorFactory;
import com.bumptech.glide.load.model.ResourceLoader.StreamFactory;
import com.bumptech.glide.load.model.ResourceLoader.UriFactory;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.model.StringLoader;
import com.bumptech.glide.load.model.UnitModelLoader.Factory;
import com.bumptech.glide.load.model.UriLoader;
import com.bumptech.glide.load.model.UrlUriLoader;
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader;
import com.bumptech.glide.load.model.stream.HttpUriLoader;
import com.bumptech.glide.load.model.stream.MediaStoreImageThumbLoader;
import com.bumptech.glide.load.model.stream.MediaStoreVideoThumbLoader;
import com.bumptech.glide.load.model.stream.UrlLoader;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.DefaultImageHeaderParser;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.ExifInterfaceImageHeaderParser;
import com.bumptech.glide.load.resource.bitmap.ResourceBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.UnitBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.VideoDecoder;
import com.bumptech.glide.load.resource.bytes.ByteBufferRewinder;
import com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder;
import com.bumptech.glide.load.resource.drawable.UnitDrawableDecoder;
import com.bumptech.glide.load.resource.file.FileDecoder;
import com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawableEncoder;
import com.bumptech.glide.load.resource.gif.GifFrameResourceDecoder;
import com.bumptech.glide.load.resource.gif.StreamGifDecoder;
import com.bumptech.glide.load.resource.transcode.BitmapBytesTranscoder;
import com.bumptech.glide.load.resource.transcode.BitmapDrawableTranscoder;
import com.bumptech.glide.load.resource.transcode.DrawableBytesTranscoder;
import com.bumptech.glide.load.resource.transcode.GifDrawableBytesTranscoder;
import com.bumptech.glide.manager.ConnectivityMonitorFactory;
import com.bumptech.glide.manager.RequestManagerRetriever;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.module.ManifestParser;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTargetFactory;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Glide implements ComponentCallbacks2 {
    private static final String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";
    private static final String TAG = "Glide";
    private static volatile Glide glide;
    private static volatile boolean isInitializing;
    private final ArrayPool arrayPool;
    private final BitmapPool bitmapPool;
    private final BitmapPreFiller bitmapPreFiller;
    private final ConnectivityMonitorFactory connectivityMonitorFactory;
    private final Engine engine;
    private final GlideContext glideContext;
    private final List<RequestManager> managers = new ArrayList();
    private final MemoryCache memoryCache;
    private MemoryCategory memoryCategory = MemoryCategory.NORMAL;
    private final Registry registry;
    private final RequestManagerRetriever requestManagerRetriever;

    public void onConfigurationChanged(Configuration configuration) {
    }

    @Nullable
    public static File getPhotoCacheDir(@NonNull Context context) {
        return getPhotoCacheDir(context, "image_manager_disk_cache");
    }

    @Nullable
    public static File getPhotoCacheDir(@NonNull Context context, @NonNull String str) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File file = new File(cacheDir, str);
            if (file.mkdirs() || (file.exists() && file.isDirectory())) {
                return file;
            }
            return null;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    @NonNull
    public static Glide get(@NonNull Context context) {
        if (glide == null) {
            synchronized (Glide.class) {
                if (glide == null) {
                    checkAndInitializeGlide(context);
                }
            }
        }
        return glide;
    }

    private static void checkAndInitializeGlide(@NonNull Context context) {
        if (isInitializing) {
            throw new IllegalStateException("You cannot call Glide.get() in registerComponents(), use the provided Glide instance instead");
        }
        isInitializing = true;
        initializeGlide(context);
        isInitializing = false;
    }

    @VisibleForTesting
    @Deprecated
    public static synchronized void init(Glide glide) {
        synchronized (Glide.class) {
            if (glide != null) {
                tearDown();
            }
            glide = glide;
        }
    }

    @VisibleForTesting
    public static synchronized void init(@NonNull Context context, @NonNull GlideBuilder glideBuilder) {
        synchronized (Glide.class) {
            if (glide != null) {
                tearDown();
            }
            initializeGlide(context, glideBuilder);
        }
    }

    @VisibleForTesting
    public static synchronized void tearDown() {
        synchronized (Glide.class) {
            if (glide != null) {
                glide.getContext().getApplicationContext().unregisterComponentCallbacks(glide);
                glide.engine.shutdown();
            }
            glide = null;
        }
    }

    private static void initializeGlide(@NonNull Context context) {
        initializeGlide(context, new GlideBuilder());
    }

    private static void initializeGlide(@NonNull Context context, @NonNull GlideBuilder glideBuilder) {
        context = context.getApplicationContext();
        GeneratedAppGlideModule annotationGeneratedGlideModules = getAnnotationGeneratedGlideModules();
        List emptyList = Collections.emptyList();
        if (annotationGeneratedGlideModules == null || annotationGeneratedGlideModules.isManifestParsingEnabled()) {
            emptyList = new ManifestParser(context).parse();
        }
        if (!(annotationGeneratedGlideModules == null || annotationGeneratedGlideModules.getExcludedModuleClasses().isEmpty())) {
            Set excludedModuleClasses = annotationGeneratedGlideModules.getExcludedModuleClasses();
            Iterator it = emptyList.iterator();
            while (it.hasNext()) {
                GlideModule glideModule = (GlideModule) it.next();
                if (excludedModuleClasses.contains(glideModule.getClass())) {
                    if (Log.isLoggable(TAG, 3)) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("AppGlideModule excludes manifest GlideModule: ");
                        stringBuilder.append(glideModule);
                        Log.d(str, stringBuilder.toString());
                    }
                    it.remove();
                }
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            for (GlideModule glideModule2 : emptyList) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Discovered GlideModule from manifest: ");
                stringBuilder2.append(glideModule2.getClass());
                Log.d(str2, stringBuilder2.toString());
            }
        }
        glideBuilder.setRequestManagerFactory(annotationGeneratedGlideModules != null ? annotationGeneratedGlideModules.getRequestManagerFactory() : null);
        for (GlideModule glideModule22 : emptyList) {
            glideModule22.applyOptions(context, glideBuilder);
        }
        if (annotationGeneratedGlideModules != null) {
            annotationGeneratedGlideModules.applyOptions(context, glideBuilder);
        }
        Glide build = glideBuilder.build(context);
        for (GlideModule registerComponents : emptyList) {
            registerComponents.registerComponents(context, build, build.registry);
        }
        if (annotationGeneratedGlideModules != null) {
            annotationGeneratedGlideModules.registerComponents(context, build, build.registry);
        }
        context.registerComponentCallbacks(build);
        glide = build;
    }

    @Nullable
    private static GeneratedAppGlideModule getAnnotationGeneratedGlideModules() {
        try {
            return (GeneratedAppGlideModule) Class.forName("com.bumptech.glide.GeneratedAppGlideModuleImpl").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException unused) {
            if (Log.isLoggable(TAG, 5)) {
                Log.w(TAG, "Failed to find GeneratedAppGlideModule. You should include an annotationProcessor compile dependency on com.github.bumptech.glide:compiler in your application and a @GlideModule annotated AppGlideModule implementation or LibraryGlideModules will be silently ignored");
            }
        } catch (InstantiationException e) {
            throwIncorrectGlideModule(e);
        } catch (IllegalAccessException e2) {
            throwIncorrectGlideModule(e2);
        } catch (NoSuchMethodException e3) {
            throwIncorrectGlideModule(e3);
        } catch (InvocationTargetException e4) {
            throwIncorrectGlideModule(e4);
        }
        return null;
    }

    private static void throwIncorrectGlideModule(Exception exception) {
        throw new IllegalStateException("GeneratedAppGlideModuleImpl is implemented incorrectly. If you've manually implemented this class, remove your implementation. The Annotation processor will generate a correct implementation.", exception);
    }

    Glide(@NonNull Context context, @NonNull Engine engine, @NonNull MemoryCache memoryCache, @NonNull BitmapPool bitmapPool, @NonNull ArrayPool arrayPool, @NonNull RequestManagerRetriever requestManagerRetriever, @NonNull ConnectivityMonitorFactory connectivityMonitorFactory, int i, @NonNull RequestOptions requestOptions, @NonNull Map<Class<?>, TransitionOptions<?, ?>> map) {
        Context context2 = context;
        MemoryCache memoryCache2 = memoryCache;
        BitmapPool bitmapPool2 = bitmapPool;
        ArrayPool arrayPool2 = arrayPool;
        this.engine = engine;
        this.bitmapPool = bitmapPool2;
        this.arrayPool = arrayPool2;
        this.memoryCache = memoryCache2;
        this.requestManagerRetriever = requestManagerRetriever;
        this.connectivityMonitorFactory = connectivityMonitorFactory;
        this.bitmapPreFiller = new BitmapPreFiller(memoryCache2, bitmapPool2, (DecodeFormat) requestOptions.getOptions().get(Downsampler.DECODE_FORMAT));
        Resources resources = context.getResources();
        this.registry = new Registry();
        if (VERSION.SDK_INT >= 27) {
            this.registry.register(new ExifInterfaceImageHeaderParser());
        }
        this.registry.register(new DefaultImageHeaderParser());
        Downsampler downsampler = new Downsampler(this.registry.getImageHeaderParsers(), resources.getDisplayMetrics(), bitmapPool2, arrayPool2);
        ByteBufferGifDecoder byteBufferGifDecoder = new ByteBufferGifDecoder(context2, this.registry.getImageHeaderParsers(), bitmapPool2, arrayPool2);
        ResourceDecoder parcel = VideoDecoder.parcel(bitmapPool);
        ResourceDecoder byteBufferBitmapDecoder = new ByteBufferBitmapDecoder(downsampler);
        ResourceDecoder streamBitmapDecoder = new StreamBitmapDecoder(downsampler, arrayPool2);
        ResourceDecoder resourceDrawableDecoder = new ResourceDrawableDecoder(context2);
        ModelLoaderFactory streamFactory = new StreamFactory(resources);
        UriFactory uriFactory = new UriFactory(resources);
        FileDescriptorFactory fileDescriptorFactory = new FileDescriptorFactory(resources);
        AssetFileDescriptorFactory assetFileDescriptorFactory = new AssetFileDescriptorFactory(resources);
        ResourceEncoder bitmapEncoder = new BitmapEncoder(arrayPool2);
        BitmapBytesTranscoder bitmapBytesTranscoder = new BitmapBytesTranscoder();
        GifDrawableBytesTranscoder gifDrawableBytesTranscoder = new GifDrawableBytesTranscoder();
        ModelLoaderFactory modelLoaderFactory = fileDescriptorFactory;
        modelLoaderFactory = uriFactory;
        ModelLoaderFactory modelLoaderFactory2 = assetFileDescriptorFactory;
        Context context3 = context;
        ContentResolver contentResolver = context.getContentResolver();
        BitmapBytesTranscoder bitmapBytesTranscoder2 = bitmapBytesTranscoder;
        GifDrawableBytesTranscoder gifDrawableBytesTranscoder2 = gifDrawableBytesTranscoder;
        this.registry.append(ByteBuffer.class, new ByteBufferEncoder()).append(InputStream.class, new StreamEncoder(arrayPool2)).append(Registry.BUCKET_BITMAP, ByteBuffer.class, Bitmap.class, byteBufferBitmapDecoder).append(Registry.BUCKET_BITMAP, InputStream.class, Bitmap.class, streamBitmapDecoder).append(Registry.BUCKET_BITMAP, ParcelFileDescriptor.class, Bitmap.class, parcel).append(Registry.BUCKET_BITMAP, AssetFileDescriptor.class, Bitmap.class, VideoDecoder.asset(bitmapPool)).append(Bitmap.class, Bitmap.class, Factory.getInstance()).append(Registry.BUCKET_BITMAP, Bitmap.class, Bitmap.class, new UnitBitmapDecoder()).append(Bitmap.class, bitmapEncoder).append(Registry.BUCKET_BITMAP_DRAWABLE, ByteBuffer.class, BitmapDrawable.class, new BitmapDrawableDecoder(resources, byteBufferBitmapDecoder)).append(Registry.BUCKET_BITMAP_DRAWABLE, InputStream.class, BitmapDrawable.class, new BitmapDrawableDecoder(resources, streamBitmapDecoder)).append(Registry.BUCKET_BITMAP_DRAWABLE, ParcelFileDescriptor.class, BitmapDrawable.class, new BitmapDrawableDecoder(resources, parcel)).append(BitmapDrawable.class, new BitmapDrawableEncoder(bitmapPool2, bitmapEncoder)).append(Registry.BUCKET_GIF, InputStream.class, GifDrawable.class, new StreamGifDecoder(this.registry.getImageHeaderParsers(), byteBufferGifDecoder, arrayPool2)).append(Registry.BUCKET_GIF, ByteBuffer.class, GifDrawable.class, byteBufferGifDecoder).append(GifDrawable.class, new GifDrawableEncoder()).append(GifDecoder.class, GifDecoder.class, Factory.getInstance()).append(Registry.BUCKET_BITMAP, GifDecoder.class, Bitmap.class, new GifFrameResourceDecoder(bitmapPool2)).append(Uri.class, Drawable.class, resourceDrawableDecoder).append(Uri.class, Bitmap.class, new ResourceBitmapDecoder(resourceDrawableDecoder, bitmapPool2)).register(new ByteBufferRewinder.Factory()).append(File.class, ByteBuffer.class, new ByteBufferFileLoader.Factory()).append(File.class, InputStream.class, new FileLoader.StreamFactory()).append(File.class, File.class, new FileDecoder()).append(File.class, ParcelFileDescriptor.class, new FileLoader.FileDescriptorFactory()).append(File.class, File.class, Factory.getInstance()).register(new InputStreamRewinder.Factory(arrayPool2)).append(Integer.TYPE, InputStream.class, streamFactory).append(Integer.TYPE, ParcelFileDescriptor.class, modelLoaderFactory).append(Integer.class, InputStream.class, streamFactory).append(Integer.class, ParcelFileDescriptor.class, modelLoaderFactory).append(Integer.class, Uri.class, modelLoaderFactory).append(Integer.TYPE, AssetFileDescriptor.class, modelLoaderFactory2).append(Integer.class, AssetFileDescriptor.class, modelLoaderFactory2).append(Integer.TYPE, Uri.class, modelLoaderFactory).append(String.class, InputStream.class, new DataUrlLoader.StreamFactory()).append(Uri.class, InputStream.class, new DataUrlLoader.StreamFactory()).append(String.class, InputStream.class, new StringLoader.StreamFactory()).append(String.class, ParcelFileDescriptor.class, new StringLoader.FileDescriptorFactory()).append(String.class, AssetFileDescriptor.class, new StringLoader.AssetFileDescriptorFactory()).append(Uri.class, InputStream.class, new HttpUriLoader.Factory()).append(Uri.class, InputStream.class, new AssetUriLoader.StreamFactory(context.getAssets())).append(Uri.class, ParcelFileDescriptor.class, new AssetUriLoader.FileDescriptorFactory(context.getAssets())).append(Uri.class, InputStream.class, new MediaStoreImageThumbLoader.Factory(context3)).append(Uri.class, InputStream.class, new MediaStoreVideoThumbLoader.Factory(context3)).append(Uri.class, InputStream.class, new UriLoader.StreamFactory(contentResolver)).append(Uri.class, ParcelFileDescriptor.class, new UriLoader.FileDescriptorFactory(contentResolver)).append(Uri.class, AssetFileDescriptor.class, new UriLoader.AssetFileDescriptorFactory(contentResolver)).append(Uri.class, InputStream.class, new UrlUriLoader.StreamFactory()).append(URL.class, InputStream.class, new UrlLoader.StreamFactory()).append(Uri.class, File.class, new MediaStoreFileLoader.Factory(context3)).append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory()).append(byte[].class, ByteBuffer.class, new ByteBufferFactory()).append(byte[].class, InputStream.class, new ByteArrayLoader.StreamFactory()).append(Uri.class, Uri.class, Factory.getInstance()).append(Drawable.class, Drawable.class, Factory.getInstance()).append(Drawable.class, Drawable.class, new UnitDrawableDecoder()).register(Bitmap.class, BitmapDrawable.class, new BitmapDrawableTranscoder(resources)).register(Bitmap.class, byte[].class, bitmapBytesTranscoder2).register(Drawable.class, byte[].class, new DrawableBytesTranscoder(bitmapPool2, bitmapBytesTranscoder2, gifDrawableBytesTranscoder2)).register(GifDrawable.class, byte[].class, gifDrawableBytesTranscoder2);
        this.glideContext = new GlideContext(context, arrayPool, this.registry, new ImageViewTargetFactory(), requestOptions, map, engine, i);
    }

    @NonNull
    public BitmapPool getBitmapPool() {
        return this.bitmapPool;
    }

    @NonNull
    public ArrayPool getArrayPool() {
        return this.arrayPool;
    }

    @NonNull
    public Context getContext() {
        return this.glideContext.getBaseContext();
    }

    /* Access modifiers changed, original: 0000 */
    public ConnectivityMonitorFactory getConnectivityMonitorFactory() {
        return this.connectivityMonitorFactory;
    }

    /* Access modifiers changed, original: 0000 */
    @NonNull
    public GlideContext getGlideContext() {
        return this.glideContext;
    }

    public void preFillBitmapPool(@NonNull Builder... builderArr) {
        this.bitmapPreFiller.preFill(builderArr);
    }

    public void clearMemory() {
        Util.assertMainThread();
        this.memoryCache.clearMemory();
        this.bitmapPool.clearMemory();
        this.arrayPool.clearMemory();
    }

    public void trimMemory(int i) {
        Util.assertMainThread();
        this.memoryCache.trimMemory(i);
        this.bitmapPool.trimMemory(i);
        this.arrayPool.trimMemory(i);
    }

    public void clearDiskCache() {
        Util.assertBackgroundThread();
        this.engine.clearDiskCache();
    }

    @NonNull
    public RequestManagerRetriever getRequestManagerRetriever() {
        return this.requestManagerRetriever;
    }

    @NonNull
    public MemoryCategory setMemoryCategory(@NonNull MemoryCategory memoryCategory) {
        Util.assertMainThread();
        this.memoryCache.setSizeMultiplier(memoryCategory.getMultiplier());
        this.bitmapPool.setSizeMultiplier(memoryCategory.getMultiplier());
        MemoryCategory memoryCategory2 = this.memoryCategory;
        this.memoryCategory = memoryCategory;
        return memoryCategory2;
    }

    @NonNull
    private static RequestManagerRetriever getRetriever(@Nullable Context context) {
        Preconditions.checkNotNull(context, "You cannot start a load on a not yet attached View or a Fragment where getActivity() returns null (which usually occurs when getActivity() is called before the Fragment is attached or after the Fragment is destroyed).");
        return get(context).getRequestManagerRetriever();
    }

    @NonNull
    public static RequestManager with(@NonNull Context context) {
        return getRetriever(context).get(context);
    }

    @NonNull
    public static RequestManager with(@NonNull Activity activity) {
        return getRetriever(activity).get(activity);
    }

    @NonNull
    public static RequestManager with(@NonNull FragmentActivity fragmentActivity) {
        return getRetriever(fragmentActivity).get(fragmentActivity);
    }

    @NonNull
    public static RequestManager with(@NonNull Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    @Deprecated
    @NonNull
    public static RequestManager with(@NonNull android.app.Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    @NonNull
    public static RequestManager with(@NonNull View view) {
        return getRetriever(view.getContext()).get(view);
    }

    @NonNull
    public Registry getRegistry() {
        return this.registry;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean removeFromManagers(@NonNull Target<?> target) {
        synchronized (this.managers) {
            for (RequestManager untrack : this.managers) {
                if (untrack.untrack(target)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void registerRequestManager(RequestManager requestManager) {
        synchronized (this.managers) {
            if (this.managers.contains(requestManager)) {
                throw new IllegalStateException("Cannot register already registered manager");
            }
            this.managers.add(requestManager);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void unregisterRequestManager(RequestManager requestManager) {
        synchronized (this.managers) {
            if (this.managers.contains(requestManager)) {
                this.managers.remove(requestManager);
            } else {
                throw new IllegalStateException("Cannot unregister not yet registered manager");
            }
        }
    }

    public void onTrimMemory(int i) {
        trimMemory(i);
    }

    public void onLowMemory() {
        clearMemory();
    }
}
