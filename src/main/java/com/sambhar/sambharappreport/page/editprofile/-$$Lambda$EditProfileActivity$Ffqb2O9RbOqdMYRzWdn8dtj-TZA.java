package com.sambhar.sambharappreport.page.editprofile;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordActivity;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EditProfileActivity$Ffqb2O9RbOqdMYRzWdn8dtj-TZA implements OnClickListener {
    private final /* synthetic */ EditProfileActivity f$0;

    public /* synthetic */ -$$Lambda$EditProfileActivity$Ffqb2O9RbOqdMYRzWdn8dtj-TZA(EditProfileActivity editProfileActivity) {
        this.f$0 = editProfileActivity;
    }

    public final void onClick(View view) {
        this.f$0.startActivity(new Intent(this.f$0, ChangePasswordActivity.class));
    }
}
