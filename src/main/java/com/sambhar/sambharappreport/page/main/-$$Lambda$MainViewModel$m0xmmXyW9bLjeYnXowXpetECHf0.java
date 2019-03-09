package com.sambhar.sambharappreport.page.main;

import android.arch.core.util.Function;
import com.sambhar.sambharappreport.entity.bodypost.JobCheckBodyPost;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MainViewModel$m0xmmXyW9bLjeYnXowXpetECHf0 implements Function {
    private final /* synthetic */ MainViewModel f$0;

    public /* synthetic */ -$$Lambda$MainViewModel$m0xmmXyW9bLjeYnXowXpetECHf0(MainViewModel mainViewModel) {
        this.f$0 = mainViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.checkJobValidation((JobCheckBodyPost) obj);
    }
}
