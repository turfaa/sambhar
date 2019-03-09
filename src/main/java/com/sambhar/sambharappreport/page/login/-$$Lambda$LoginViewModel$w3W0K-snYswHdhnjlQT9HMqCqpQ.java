package com.sambhar.sambharappreport.page.login;

import android.arch.core.util.Function;
import com.sambhar.sambharappreport.entity.bodypost.LoginFormBody;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$LoginViewModel$w3W0K-snYswHdhnjlQT9HMqCqpQ implements Function {
    private final /* synthetic */ LoginViewModel f$0;

    public /* synthetic */ -$$Lambda$LoginViewModel$w3W0K-snYswHdhnjlQT9HMqCqpQ(LoginViewModel loginViewModel) {
        this.f$0 = loginViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.login((LoginFormBody) obj);
    }
}
