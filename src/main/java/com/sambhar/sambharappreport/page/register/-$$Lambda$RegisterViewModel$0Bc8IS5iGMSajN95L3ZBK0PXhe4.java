package com.sambhar.sambharappreport.page.register;

import android.arch.core.util.Function;
import com.sambhar.sambharappreport.entity.bodypost.RegisterFormBody;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RegisterViewModel$0Bc8IS5iGMSajN95L3ZBK0PXhe4 implements Function {
    private final /* synthetic */ RegisterViewModel f$0;

    public /* synthetic */ -$$Lambda$RegisterViewModel$0Bc8IS5iGMSajN95L3ZBK0PXhe4(RegisterViewModel registerViewModel) {
        this.f$0 = registerViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.registerUser((RegisterFormBody) obj);
    }
}
