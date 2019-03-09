package com.sambhar.sambharappreport.page.register;

import android.view.View;
import android.view.View.OnClickListener;
import com.sambhar.sambharappreport.entity.GroupEntity;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RegisterDetailAdapter$uDVU70P19aK9X8e-EoVVma473CA implements OnClickListener {
    private final /* synthetic */ RegisterDetailAdapter f$0;
    private final /* synthetic */ int f$1;

    public /* synthetic */ -$$Lambda$RegisterDetailAdapter$uDVU70P19aK9X8e-EoVVma473CA(RegisterDetailAdapter registerDetailAdapter, int i) {
        this.f$0 = registerDetailAdapter;
        this.f$1 = i;
    }

    public final void onClick(View view) {
        this.f$0.listener.itemClick((GroupEntity) this.f$0.mList.get(this.f$1));
    }
}
