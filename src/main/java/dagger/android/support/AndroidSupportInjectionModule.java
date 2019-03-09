package dagger.android.support;

import android.support.v4.app.Fragment;
import dagger.Module;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.Multibinds;
import java.util.Map;

@Module(includes = {AndroidInjectionModule.class})
public abstract class AndroidSupportInjectionModule {
    @Multibinds
    public abstract Map<Class<? extends Fragment>, Factory<? extends Fragment>> supportFragmentInjectorFactories();

    private AndroidSupportInjectionModule() {
    }
}
