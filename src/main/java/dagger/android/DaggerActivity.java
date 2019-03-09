package dagger.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import javax.inject.Inject;

public abstract class DaggerActivity extends Activity implements HasFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        AndroidInjection.inject((Activity) this);
        super.onCreate(bundle);
    }

    public AndroidInjector<Fragment> fragmentInjector() {
        return this.fragmentInjector;
    }
}
