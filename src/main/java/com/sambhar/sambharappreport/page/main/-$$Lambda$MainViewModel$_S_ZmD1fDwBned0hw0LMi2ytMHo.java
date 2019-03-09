package com.sambhar.sambharappreport.page.main;

import android.arch.core.util.Function;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MainViewModel$_S_ZmD1fDwBned0hw0LMi2ytMHo implements Function {
    private final /* synthetic */ MainViewModel f$0;

    public /* synthetic */ -$$Lambda$MainViewModel$_S_ZmD1fDwBned0hw0LMi2ytMHo(MainViewModel mainViewModel) {
        this.f$0 = mainViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.createJob();
    }
}
