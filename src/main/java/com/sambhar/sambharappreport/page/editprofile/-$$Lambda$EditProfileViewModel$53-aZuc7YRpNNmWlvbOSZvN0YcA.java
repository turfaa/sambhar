package com.sambhar.sambharappreport.page.editprofile;

import android.arch.core.util.Function;
import com.sambhar.sambharappreport.entity.bodypost.EditProfileBodyPost;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EditProfileViewModel$53-aZuc7YRpNNmWlvbOSZvN0YcA implements Function {
    private final /* synthetic */ EditProfileViewModel f$0;

    public /* synthetic */ -$$Lambda$EditProfileViewModel$53-aZuc7YRpNNmWlvbOSZvN0YcA(EditProfileViewModel editProfileViewModel) {
        this.f$0 = editProfileViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.updateProfile((EditProfileBodyPost) obj);
    }
}
