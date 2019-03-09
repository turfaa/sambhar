package com.sambhar.sambharappreport.base;

import android.arch.lifecycle.ViewModelProvider.Factory;
import android.databinding.ViewDataBinding;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class BaseActivity_MembersInjector<V extends BaseViewModel, T extends ViewDataBinding> implements MembersInjector<BaseActivity<V, T>> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final Provider<Factory> mViewModelFactoryProvider;

    public BaseActivity_MembersInjector(Provider<Factory> provider) {
        this.mViewModelFactoryProvider = provider;
    }

    public static <V extends BaseViewModel, T extends ViewDataBinding> MembersInjector<BaseActivity<V, T>> create(Provider<Factory> provider) {
        return new BaseActivity_MembersInjector(provider);
    }

    public void injectMembers(BaseActivity<V, T> baseActivity) {
        if (baseActivity != null) {
            baseActivity.mViewModelFactory = (Factory) this.mViewModelFactoryProvider.get();
            return;
        }
        throw new NullPointerException("Cannot inject members into a null reference");
    }

    public static <V extends BaseViewModel, T extends ViewDataBinding> void injectMViewModelFactory(BaseActivity<V, T> baseActivity, Provider<Factory> provider) {
        baseActivity.mViewModelFactory = (Factory) provider.get();
    }
}
