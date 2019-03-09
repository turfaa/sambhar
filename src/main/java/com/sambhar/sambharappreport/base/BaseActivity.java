package com.sambhar.sambharappreport.base;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider.Factory;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.sambhar.sambharappreport.base.view.LoadingDialogFragment;
import com.sambhar.sambharappreport.event.LogoutEvent;
import com.sambhar.sambharappreport.page.login.LoginActivity;
import dagger.android.AndroidInjection;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public abstract class BaseActivity<V extends BaseViewModel, T extends ViewDataBinding> extends AppCompatActivity {
    private static final String LOADING_DIALOG_TAG = "loading_dialog_tag";
    private T mViewDataBinding;
    private V mViewModel;
    @Inject
    Factory mViewModelFactory;

    public abstract int setLayoutView();

    /* Access modifiers changed, original: protected */
    public void onCreate(@Nullable Bundle bundle) {
        AndroidInjection.inject((Activity) this);
        super.onCreate(bundle);
        provideViewModel();
        provideDataBinding();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onPause();
    }

    public void setupActionBar(String str, boolean z) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle((CharSequence) str);
            supportActionBar.setDisplayHomeAsUpEnabled(z);
        }
    }

    public void showErrorSnackbar(View view, String str) {
        Snackbar.make(view, (CharSequence) str, -1).show();
    }

    @Subscribe
    public void onEvent(LogoutEvent logoutEvent) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.SHOW_LOGOUT_POPUP, true);
        intent.addFlags(268468224);
        startActivity(intent);
    }

    public final V getViewModel() {
        return this.mViewModel;
    }

    public final T getDataBinding() {
        return this.mViewDataBinding;
    }

    private void provideDataBinding() {
        this.mViewDataBinding = DataBindingUtil.setContentView(this, setLayoutView());
    }

    private void provideViewModel() {
        this.mViewModel = (BaseViewModel) ViewModelProviders.of((FragmentActivity) this, this.mViewModelFactory).get(getViewModelClass(getClass()));
    }

    private Class<V> getViewModelClass(Class<?> cls) {
        Type genericSuperclass = cls.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        }
        return getViewModelClass(cls.getSuperclass());
    }

    public synchronized void showLoading(CharSequence charSequence) {
        if (((LoadingDialogFragment) getSupportFragmentManager().findFragmentByTag(LOADING_DIALOG_TAG)) == null) {
            LoadingDialogFragment.create(charSequence).show(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }

    public void dismissLoading() {
        LoadingDialogFragment loadingDialogFragment = (LoadingDialogFragment) getSupportFragmentManager().findFragmentByTag(LOADING_DIALOG_TAG);
        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismissAllowingStateLoss();
        }
    }
}
