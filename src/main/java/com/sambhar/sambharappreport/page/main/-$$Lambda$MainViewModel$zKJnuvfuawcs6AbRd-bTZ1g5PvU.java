package com.sambhar.sambharappreport.page.main;

import android.arch.core.util.Function;
import com.sambhar.sambharappreport.entity.bodypost.NotifyFormBody;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MainViewModel$zKJnuvfuawcs6AbRd-bTZ1g5PvU implements Function {
    private final /* synthetic */ MainViewModel f$0;

    public /* synthetic */ -$$Lambda$MainViewModel$zKJnuvfuawcs6AbRd-bTZ1g5PvU(MainViewModel mainViewModel) {
        this.f$0 = mainViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.notify((NotifyFormBody) obj);
    }
}
