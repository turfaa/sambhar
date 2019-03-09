package com.sambhar.sambharappreport.page.changepassword;

import android.arch.core.util.Function;
import com.sambhar.sambharappreport.entity.bodypost.UpdatePasswordFormBody;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChangePasswordViewModel$o3W_64EpmuaVlyNkf-wY94STfKQ implements Function {
    private final /* synthetic */ ChangePasswordViewModel f$0;

    public /* synthetic */ -$$Lambda$ChangePasswordViewModel$o3W_64EpmuaVlyNkf-wY94STfKQ(ChangePasswordViewModel changePasswordViewModel) {
        this.f$0 = changePasswordViewModel;
    }

    public final Object apply(Object obj) {
        return this.f$0.mAppRepository.updatePassword((UpdatePasswordFormBody) obj);
    }
}
