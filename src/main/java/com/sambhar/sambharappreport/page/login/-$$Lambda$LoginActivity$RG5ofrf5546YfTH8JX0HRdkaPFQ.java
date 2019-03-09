package com.sambhar.sambharappreport.page.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.sambhar.sambharappreport.page.register.RegisterActivity;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$LoginActivity$RG5ofrf5546YfTH8JX0HRdkaPFQ implements OnClickListener {
    private final /* synthetic */ LoginActivity f$0;

    public /* synthetic */ -$$Lambda$LoginActivity$RG5ofrf5546YfTH8JX0HRdkaPFQ(LoginActivity loginActivity) {
        this.f$0 = loginActivity;
    }

    public final void onClick(View view) {
        this.f$0.startActivity(new Intent(this.f$0, RegisterActivity.class));
    }
}
