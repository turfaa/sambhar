package com.bumptech.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.manager.RequestManagerRetriever.RequestManagerFactory;
import com.bumptech.glide.module.AppGlideModule;
import java.util.Set;

abstract class GeneratedAppGlideModule extends AppGlideModule {
    @NonNull
    public abstract Set<Class<?>> getExcludedModuleClasses();

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public RequestManagerFactory getRequestManagerFactory() {
        return null;
    }

    GeneratedAppGlideModule() {
    }
}
