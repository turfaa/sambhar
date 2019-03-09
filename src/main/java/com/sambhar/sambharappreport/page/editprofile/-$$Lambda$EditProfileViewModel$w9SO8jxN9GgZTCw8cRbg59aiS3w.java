package com.sambhar.sambharappreport.page.editprofile;

import android.arch.core.util.Function;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EditProfileViewModel$w9SO8jxN9GgZTCw8cRbg59aiS3w implements Function {
    private final /* synthetic */ EditProfileViewModel f$0;

    public /* synthetic */ -$$Lambda$EditProfileViewModel$w9SO8jxN9GgZTCw8cRbg59aiS3w(EditProfileViewModel editProfileViewModel) {
        this.f$0 = editProfileViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.getProfileUser();
    }
}
