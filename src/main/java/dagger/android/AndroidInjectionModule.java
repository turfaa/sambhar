package dagger.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import dagger.Module;
import dagger.android.AndroidInjector.Factory;
import dagger.multibindings.Multibinds;
import java.util.Map;

@Module
public abstract class AndroidInjectionModule {
    @Multibinds
    public abstract Map<Class<? extends Activity>, Factory<? extends Activity>> activityInjectorFactories();

    @Multibinds
    public abstract Map<Class<? extends BroadcastReceiver>, Factory<? extends BroadcastReceiver>> broadcastReceiverInjectorFactories();

    @Multibinds
    public abstract Map<Class<? extends ContentProvider>, Factory<? extends ContentProvider>> contentProviderInjectorFactories();

    @Multibinds
    public abstract Map<Class<? extends Fragment>, Factory<? extends Fragment>> fragmentInjectorFactories();

    @Multibinds
    public abstract Map<Class<? extends Service>, Factory<? extends Service>> serviceInjectorFactories();

    private AndroidInjectionModule() {
    }
}
