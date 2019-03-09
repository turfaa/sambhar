package com.sambhar.sambharappreport.di;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import com.google.gson.Gson;
import com.readystatesoftware.chuck.ChuckInterceptor;
import com.sambhar.sambharappreport.SambharApplication;
import com.sambhar.sambharappreport.SambharApplication_MembersInjector;
import com.sambhar.sambharappreport.base.ProjectViewModelFactory;
import com.sambhar.sambharappreport.base.ProjectViewModelFactory_Factory;
import com.sambhar.sambharappreport.data.UserSharedPref;
import com.sambhar.sambharappreport.di.ActivityModule_BindChangePasswordActivity.ChangePasswordActivitySubcomponent;
import com.sambhar.sambharappreport.di.ActivityModule_BindEditProfileActivity.EditProfileActivitySubcomponent;
import com.sambhar.sambharappreport.di.ActivityModule_BindLoginActivity.LoginActivitySubcomponent;
import com.sambhar.sambharappreport.di.ActivityModule_BindMainActivity.MainActivitySubcomponent;
import com.sambhar.sambharappreport.di.ActivityModule_BindRegisterActivity.RegisterActivitySubcomponent;
import com.sambhar.sambharappreport.di.ActivityModule_BindRegisterDetailDataActivity.RegisterDetailDataActivitySubcomponent;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordActivity;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordActivity_MembersInjector;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordViewModel;
import com.sambhar.sambharappreport.page.changepassword.ChangePasswordViewModel_Factory;
import com.sambhar.sambharappreport.page.editprofile.EditProfileActivity;
import com.sambhar.sambharappreport.page.editprofile.EditProfileActivity_MembersInjector;
import com.sambhar.sambharappreport.page.editprofile.EditProfileViewModel;
import com.sambhar.sambharappreport.page.editprofile.EditProfileViewModel_Factory;
import com.sambhar.sambharappreport.page.login.LoginActivity;
import com.sambhar.sambharappreport.page.login.LoginActivity_MembersInjector;
import com.sambhar.sambharappreport.page.login.LoginViewModel;
import com.sambhar.sambharappreport.page.login.LoginViewModel_Factory;
import com.sambhar.sambharappreport.page.main.MainActivity;
import com.sambhar.sambharappreport.page.main.MainActivity_MembersInjector;
import com.sambhar.sambharappreport.page.main.MainViewModel;
import com.sambhar.sambharappreport.page.main.MainViewModel_Factory;
import com.sambhar.sambharappreport.page.register.RegisterActivity;
import com.sambhar.sambharappreport.page.register.RegisterActivity_MembersInjector;
import com.sambhar.sambharappreport.page.register.RegisterDetailDataActivity;
import com.sambhar.sambharappreport.page.register.RegisterDetailDataActivity_MembersInjector;
import com.sambhar.sambharappreport.page.register.RegisterViewModel;
import com.sambhar.sambharappreport.page.register.RegisterViewModel_Factory;
import com.sambhar.sambharappreport.rest.AppRemoteDataSource;
import com.sambhar.sambharappreport.rest.AppRemoteDataSource_Factory;
import com.sambhar.sambharappreport.rest.AppRepository;
import com.sambhar.sambharappreport.rest.AppRepository_Factory;
import com.sambhar.sambharappreport.rest.AppRest;
import com.sambhar.sambharappreport.rest.LiveDataCallAdapterFactory;
import com.sambhar.sambharappreport.rest.LiveDataCallAdapterFactory_Factory;
import com.sambhar.sambharappreport.rest.RestBuilderModule;
import com.sambhar.sambharappreport.rest.RestBuilderModule_ProvideAppRestFactory;
import com.sambhar.sambharappreport.rest.interceptor.AuthenticationInterceptor;
import com.sambhar.sambharappreport.rest.interceptor.HeaderInterceptor;
import dagger.MembersInjector;
import dagger.android.AndroidInjector.Factory;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.DispatchingAndroidInjector_Factory;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.MapProviderFactory;
import dagger.internal.MembersInjectors;
import dagger.internal.Preconditions;
import java.util.Map;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

public final class DaggerAppComponent implements AppComponent {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private Provider<AppRemoteDataSource> appRemoteDataSourceProvider;
    private Provider<AppRepository> appRepositoryProvider;
    private Provider<Application> applicationProvider;
    private Provider<Factory<? extends Activity>> bindAndroidInjectorFactoryProvider;
    private Provider<Factory<? extends Activity>> bindAndroidInjectorFactoryProvider2;
    private Provider<Factory<? extends Activity>> bindAndroidInjectorFactoryProvider3;
    private Provider<Factory<? extends Activity>> bindAndroidInjectorFactoryProvider4;
    private Provider<Factory<? extends Activity>> bindAndroidInjectorFactoryProvider5;
    private Provider<Factory<? extends Activity>> bindAndroidInjectorFactoryProvider6;
    private Provider<ViewModel> bindChangePasswordViewModelProvider;
    private Provider<ViewModel> bindEditProfileViewModelProvider;
    private Provider<ViewModel> bindLoginViewModelProvider;
    private Provider<ViewModel> bindMainViewModelProvider;
    private Provider<ViewModel> bindRegisterViewModelProvider;
    private Provider<ViewModelProvider.Factory> bindViewModelFactoryProvider;
    private Provider<com.sambhar.sambharappreport.di.ActivityModule_BindChangePasswordActivity.ChangePasswordActivitySubcomponent.Builder> changePasswordActivitySubcomponentBuilderProvider;
    private Provider<ChangePasswordViewModel> changePasswordViewModelProvider;
    private Provider<DispatchingAndroidInjector<Activity>> dispatchingAndroidInjectorProvider;
    private Provider<com.sambhar.sambharappreport.di.ActivityModule_BindEditProfileActivity.EditProfileActivitySubcomponent.Builder> editProfileActivitySubcomponentBuilderProvider;
    private Provider<EditProfileViewModel> editProfileViewModelProvider;
    private Provider<LiveDataCallAdapterFactory> liveDataCallAdapterFactoryProvider;
    private Provider<com.sambhar.sambharappreport.di.ActivityModule_BindLoginActivity.LoginActivitySubcomponent.Builder> loginActivitySubcomponentBuilderProvider;
    private Provider<LoginViewModel> loginViewModelProvider;
    private Provider<com.sambhar.sambharappreport.di.ActivityModule_BindMainActivity.MainActivitySubcomponent.Builder> mainActivitySubcomponentBuilderProvider;
    private Provider<MainViewModel> mainViewModelProvider;
    private Provider<Map<Class<? extends Activity>, Provider<Factory<? extends Activity>>>> mapOfClassOfAndProviderOfFactoryOfProvider;
    private Provider<Map<Class<? extends ViewModel>, Provider<ViewModel>>> mapOfClassOfAndProviderOfViewModelProvider;
    private Provider<ProjectViewModelFactory> projectViewModelFactoryProvider;
    private Provider<AppRest> provideAppRestProvider;
    private Provider<AuthenticationInterceptor> provideAuthenticationInterceptorProvider;
    private Provider<ChuckInterceptor> provideChuckInterceptorProvider;
    private Provider<Context> provideContextProvider;
    private Provider<Gson> provideGsonProvider;
    private Provider<HeaderInterceptor> provideHeaderInterceptorProvider;
    private Provider<OkHttpClient> provideOkHttpClientProvider;
    private Provider<UserSharedPref> provideUserSharedPrefProvider;
    private Provider<com.sambhar.sambharappreport.di.ActivityModule_BindRegisterActivity.RegisterActivitySubcomponent.Builder> registerActivitySubcomponentBuilderProvider;
    private Provider<com.sambhar.sambharappreport.di.ActivityModule_BindRegisterDetailDataActivity.RegisterDetailDataActivitySubcomponent.Builder> registerDetailDataActivitySubcomponentBuilderProvider;
    private Provider<RegisterViewModel> registerViewModelProvider;
    private MembersInjector<SambharApplication> sambharApplicationMembersInjector;

    private static final class Builder implements com.sambhar.sambharappreport.di.AppComponent.Builder {
        private AppModule appModule;
        private Application application;
        private RestBuilderModule restBuilderModule;

        private Builder() {
        }

        /* synthetic */ Builder(AnonymousClass1 anonymousClass1) {
            this();
        }

        public AppComponent build() {
            if (this.appModule == null) {
                this.appModule = new AppModule();
            }
            if (this.restBuilderModule == null) {
                this.restBuilderModule = new RestBuilderModule();
            }
            if (this.application != null) {
                return new DaggerAppComponent(this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Application.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public Builder application(Application application) {
            this.application = (Application) Preconditions.checkNotNull(application);
            return this;
        }
    }

    private final class ChangePasswordActivitySubcomponentImpl implements ChangePasswordActivitySubcomponent {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MembersInjector<ChangePasswordActivity> changePasswordActivityMembersInjector;

        static {
            Class cls = DaggerAppComponent.class;
        }

        /* synthetic */ ChangePasswordActivitySubcomponentImpl(DaggerAppComponent daggerAppComponent, ChangePasswordActivitySubcomponentBuilder changePasswordActivitySubcomponentBuilder, AnonymousClass1 anonymousClass1) {
            this(changePasswordActivitySubcomponentBuilder);
        }

        private ChangePasswordActivitySubcomponentImpl(ChangePasswordActivitySubcomponentBuilder changePasswordActivitySubcomponentBuilder) {
            initialize(changePasswordActivitySubcomponentBuilder);
        }

        private void initialize(ChangePasswordActivitySubcomponentBuilder changePasswordActivitySubcomponentBuilder) {
            this.changePasswordActivityMembersInjector = ChangePasswordActivity_MembersInjector.create(DaggerAppComponent.this.bindViewModelFactoryProvider);
        }

        public void inject(ChangePasswordActivity changePasswordActivity) {
            this.changePasswordActivityMembersInjector.injectMembers(changePasswordActivity);
        }
    }

    private final class EditProfileActivitySubcomponentImpl implements EditProfileActivitySubcomponent {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MembersInjector<EditProfileActivity> editProfileActivityMembersInjector;

        static {
            Class cls = DaggerAppComponent.class;
        }

        /* synthetic */ EditProfileActivitySubcomponentImpl(DaggerAppComponent daggerAppComponent, EditProfileActivitySubcomponentBuilder editProfileActivitySubcomponentBuilder, AnonymousClass1 anonymousClass1) {
            this(editProfileActivitySubcomponentBuilder);
        }

        private EditProfileActivitySubcomponentImpl(EditProfileActivitySubcomponentBuilder editProfileActivitySubcomponentBuilder) {
            initialize(editProfileActivitySubcomponentBuilder);
        }

        private void initialize(EditProfileActivitySubcomponentBuilder editProfileActivitySubcomponentBuilder) {
            this.editProfileActivityMembersInjector = EditProfileActivity_MembersInjector.create(DaggerAppComponent.this.bindViewModelFactoryProvider);
        }

        public void inject(EditProfileActivity editProfileActivity) {
            this.editProfileActivityMembersInjector.injectMembers(editProfileActivity);
        }
    }

    private final class LoginActivitySubcomponentImpl implements LoginActivitySubcomponent {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MembersInjector<LoginActivity> loginActivityMembersInjector;

        static {
            Class cls = DaggerAppComponent.class;
        }

        /* synthetic */ LoginActivitySubcomponentImpl(DaggerAppComponent daggerAppComponent, LoginActivitySubcomponentBuilder loginActivitySubcomponentBuilder, AnonymousClass1 anonymousClass1) {
            this(loginActivitySubcomponentBuilder);
        }

        private LoginActivitySubcomponentImpl(LoginActivitySubcomponentBuilder loginActivitySubcomponentBuilder) {
            initialize(loginActivitySubcomponentBuilder);
        }

        private void initialize(LoginActivitySubcomponentBuilder loginActivitySubcomponentBuilder) {
            this.loginActivityMembersInjector = LoginActivity_MembersInjector.create(DaggerAppComponent.this.bindViewModelFactoryProvider, DaggerAppComponent.this.provideUserSharedPrefProvider);
        }

        public void inject(LoginActivity loginActivity) {
            this.loginActivityMembersInjector.injectMembers(loginActivity);
        }
    }

    private final class MainActivitySubcomponentImpl implements MainActivitySubcomponent {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MembersInjector<MainActivity> mainActivityMembersInjector;

        static {
            Class cls = DaggerAppComponent.class;
        }

        /* synthetic */ MainActivitySubcomponentImpl(DaggerAppComponent daggerAppComponent, MainActivitySubcomponentBuilder mainActivitySubcomponentBuilder, AnonymousClass1 anonymousClass1) {
            this(mainActivitySubcomponentBuilder);
        }

        private MainActivitySubcomponentImpl(MainActivitySubcomponentBuilder mainActivitySubcomponentBuilder) {
            initialize(mainActivitySubcomponentBuilder);
        }

        private void initialize(MainActivitySubcomponentBuilder mainActivitySubcomponentBuilder) {
            this.mainActivityMembersInjector = MainActivity_MembersInjector.create(DaggerAppComponent.this.bindViewModelFactoryProvider, DaggerAppComponent.this.provideUserSharedPrefProvider);
        }

        public void inject(MainActivity mainActivity) {
            this.mainActivityMembersInjector.injectMembers(mainActivity);
        }
    }

    private final class RegisterActivitySubcomponentImpl implements RegisterActivitySubcomponent {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MembersInjector<RegisterActivity> registerActivityMembersInjector;

        static {
            Class cls = DaggerAppComponent.class;
        }

        /* synthetic */ RegisterActivitySubcomponentImpl(DaggerAppComponent daggerAppComponent, RegisterActivitySubcomponentBuilder registerActivitySubcomponentBuilder, AnonymousClass1 anonymousClass1) {
            this(registerActivitySubcomponentBuilder);
        }

        private RegisterActivitySubcomponentImpl(RegisterActivitySubcomponentBuilder registerActivitySubcomponentBuilder) {
            initialize(registerActivitySubcomponentBuilder);
        }

        private void initialize(RegisterActivitySubcomponentBuilder registerActivitySubcomponentBuilder) {
            this.registerActivityMembersInjector = RegisterActivity_MembersInjector.create(DaggerAppComponent.this.bindViewModelFactoryProvider);
        }

        public void inject(RegisterActivity registerActivity) {
            this.registerActivityMembersInjector.injectMembers(registerActivity);
        }
    }

    private final class RegisterDetailDataActivitySubcomponentImpl implements RegisterDetailDataActivitySubcomponent {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private MembersInjector<RegisterDetailDataActivity> registerDetailDataActivityMembersInjector;

        static {
            Class cls = DaggerAppComponent.class;
        }

        /* synthetic */ RegisterDetailDataActivitySubcomponentImpl(DaggerAppComponent daggerAppComponent, RegisterDetailDataActivitySubcomponentBuilder registerDetailDataActivitySubcomponentBuilder, AnonymousClass1 anonymousClass1) {
            this(registerDetailDataActivitySubcomponentBuilder);
        }

        private RegisterDetailDataActivitySubcomponentImpl(RegisterDetailDataActivitySubcomponentBuilder registerDetailDataActivitySubcomponentBuilder) {
            initialize(registerDetailDataActivitySubcomponentBuilder);
        }

        private void initialize(RegisterDetailDataActivitySubcomponentBuilder registerDetailDataActivitySubcomponentBuilder) {
            this.registerDetailDataActivityMembersInjector = RegisterDetailDataActivity_MembersInjector.create(DaggerAppComponent.this.bindViewModelFactoryProvider);
        }

        public void inject(RegisterDetailDataActivity registerDetailDataActivity) {
            this.registerDetailDataActivityMembersInjector.injectMembers(registerDetailDataActivity);
        }
    }

    private final class ChangePasswordActivitySubcomponentBuilder extends com.sambhar.sambharappreport.di.ActivityModule_BindChangePasswordActivity.ChangePasswordActivitySubcomponent.Builder {
        private ChangePasswordActivity seedInstance;

        private ChangePasswordActivitySubcomponentBuilder() {
        }

        /* synthetic */ ChangePasswordActivitySubcomponentBuilder(DaggerAppComponent daggerAppComponent, AnonymousClass1 anonymousClass1) {
            this();
        }

        public ChangePasswordActivitySubcomponent build() {
            if (this.seedInstance != null) {
                return new ChangePasswordActivitySubcomponentImpl(DaggerAppComponent.this, this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ChangePasswordActivity.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void seedInstance(ChangePasswordActivity changePasswordActivity) {
            this.seedInstance = (ChangePasswordActivity) Preconditions.checkNotNull(changePasswordActivity);
        }
    }

    private final class EditProfileActivitySubcomponentBuilder extends com.sambhar.sambharappreport.di.ActivityModule_BindEditProfileActivity.EditProfileActivitySubcomponent.Builder {
        private EditProfileActivity seedInstance;

        private EditProfileActivitySubcomponentBuilder() {
        }

        /* synthetic */ EditProfileActivitySubcomponentBuilder(DaggerAppComponent daggerAppComponent, AnonymousClass1 anonymousClass1) {
            this();
        }

        public EditProfileActivitySubcomponent build() {
            if (this.seedInstance != null) {
                return new EditProfileActivitySubcomponentImpl(DaggerAppComponent.this, this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(EditProfileActivity.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void seedInstance(EditProfileActivity editProfileActivity) {
            this.seedInstance = (EditProfileActivity) Preconditions.checkNotNull(editProfileActivity);
        }
    }

    private final class LoginActivitySubcomponentBuilder extends com.sambhar.sambharappreport.di.ActivityModule_BindLoginActivity.LoginActivitySubcomponent.Builder {
        private LoginActivity seedInstance;

        private LoginActivitySubcomponentBuilder() {
        }

        /* synthetic */ LoginActivitySubcomponentBuilder(DaggerAppComponent daggerAppComponent, AnonymousClass1 anonymousClass1) {
            this();
        }

        public LoginActivitySubcomponent build() {
            if (this.seedInstance != null) {
                return new LoginActivitySubcomponentImpl(DaggerAppComponent.this, this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(LoginActivity.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void seedInstance(LoginActivity loginActivity) {
            this.seedInstance = (LoginActivity) Preconditions.checkNotNull(loginActivity);
        }
    }

    private final class MainActivitySubcomponentBuilder extends com.sambhar.sambharappreport.di.ActivityModule_BindMainActivity.MainActivitySubcomponent.Builder {
        private MainActivity seedInstance;

        private MainActivitySubcomponentBuilder() {
        }

        /* synthetic */ MainActivitySubcomponentBuilder(DaggerAppComponent daggerAppComponent, AnonymousClass1 anonymousClass1) {
            this();
        }

        public MainActivitySubcomponent build() {
            if (this.seedInstance != null) {
                return new MainActivitySubcomponentImpl(DaggerAppComponent.this, this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(MainActivity.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void seedInstance(MainActivity mainActivity) {
            this.seedInstance = (MainActivity) Preconditions.checkNotNull(mainActivity);
        }
    }

    private final class RegisterActivitySubcomponentBuilder extends com.sambhar.sambharappreport.di.ActivityModule_BindRegisterActivity.RegisterActivitySubcomponent.Builder {
        private RegisterActivity seedInstance;

        private RegisterActivitySubcomponentBuilder() {
        }

        /* synthetic */ RegisterActivitySubcomponentBuilder(DaggerAppComponent daggerAppComponent, AnonymousClass1 anonymousClass1) {
            this();
        }

        public RegisterActivitySubcomponent build() {
            if (this.seedInstance != null) {
                return new RegisterActivitySubcomponentImpl(DaggerAppComponent.this, this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(RegisterActivity.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void seedInstance(RegisterActivity registerActivity) {
            this.seedInstance = (RegisterActivity) Preconditions.checkNotNull(registerActivity);
        }
    }

    private final class RegisterDetailDataActivitySubcomponentBuilder extends com.sambhar.sambharappreport.di.ActivityModule_BindRegisterDetailDataActivity.RegisterDetailDataActivitySubcomponent.Builder {
        private RegisterDetailDataActivity seedInstance;

        private RegisterDetailDataActivitySubcomponentBuilder() {
        }

        /* synthetic */ RegisterDetailDataActivitySubcomponentBuilder(DaggerAppComponent daggerAppComponent, AnonymousClass1 anonymousClass1) {
            this();
        }

        public RegisterDetailDataActivitySubcomponent build() {
            if (this.seedInstance != null) {
                return new RegisterDetailDataActivitySubcomponentImpl(DaggerAppComponent.this, this, null);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(RegisterDetailDataActivity.class.getCanonicalName());
            stringBuilder.append(" must be set");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void seedInstance(RegisterDetailDataActivity registerDetailDataActivity) {
            this.seedInstance = (RegisterDetailDataActivity) Preconditions.checkNotNull(registerDetailDataActivity);
        }
    }

    /* synthetic */ DaggerAppComponent(Builder builder, AnonymousClass1 anonymousClass1) {
        this(builder);
    }

    private DaggerAppComponent(Builder builder) {
        initialize(builder);
    }

    public static com.sambhar.sambharappreport.di.AppComponent.Builder builder() {
        return new Builder();
    }

    private void initialize(Builder builder) {
        this.mainActivitySubcomponentBuilderProvider = new dagger.internal.Factory<com.sambhar.sambharappreport.di.ActivityModule_BindMainActivity.MainActivitySubcomponent.Builder>() {
            public com.sambhar.sambharappreport.di.ActivityModule_BindMainActivity.MainActivitySubcomponent.Builder get() {
                return new MainActivitySubcomponentBuilder(DaggerAppComponent.this, null);
            }
        };
        this.bindAndroidInjectorFactoryProvider = this.mainActivitySubcomponentBuilderProvider;
        this.loginActivitySubcomponentBuilderProvider = new dagger.internal.Factory<com.sambhar.sambharappreport.di.ActivityModule_BindLoginActivity.LoginActivitySubcomponent.Builder>() {
            public com.sambhar.sambharappreport.di.ActivityModule_BindLoginActivity.LoginActivitySubcomponent.Builder get() {
                return new LoginActivitySubcomponentBuilder(DaggerAppComponent.this, null);
            }
        };
        this.bindAndroidInjectorFactoryProvider2 = this.loginActivitySubcomponentBuilderProvider;
        this.registerActivitySubcomponentBuilderProvider = new dagger.internal.Factory<com.sambhar.sambharappreport.di.ActivityModule_BindRegisterActivity.RegisterActivitySubcomponent.Builder>() {
            public com.sambhar.sambharappreport.di.ActivityModule_BindRegisterActivity.RegisterActivitySubcomponent.Builder get() {
                return new RegisterActivitySubcomponentBuilder(DaggerAppComponent.this, null);
            }
        };
        this.bindAndroidInjectorFactoryProvider3 = this.registerActivitySubcomponentBuilderProvider;
        this.registerDetailDataActivitySubcomponentBuilderProvider = new dagger.internal.Factory<com.sambhar.sambharappreport.di.ActivityModule_BindRegisterDetailDataActivity.RegisterDetailDataActivitySubcomponent.Builder>() {
            public com.sambhar.sambharappreport.di.ActivityModule_BindRegisterDetailDataActivity.RegisterDetailDataActivitySubcomponent.Builder get() {
                return new RegisterDetailDataActivitySubcomponentBuilder(DaggerAppComponent.this, null);
            }
        };
        this.bindAndroidInjectorFactoryProvider4 = this.registerDetailDataActivitySubcomponentBuilderProvider;
        this.editProfileActivitySubcomponentBuilderProvider = new dagger.internal.Factory<com.sambhar.sambharappreport.di.ActivityModule_BindEditProfileActivity.EditProfileActivitySubcomponent.Builder>() {
            public com.sambhar.sambharappreport.di.ActivityModule_BindEditProfileActivity.EditProfileActivitySubcomponent.Builder get() {
                return new EditProfileActivitySubcomponentBuilder(DaggerAppComponent.this, null);
            }
        };
        this.bindAndroidInjectorFactoryProvider5 = this.editProfileActivitySubcomponentBuilderProvider;
        this.changePasswordActivitySubcomponentBuilderProvider = new dagger.internal.Factory<com.sambhar.sambharappreport.di.ActivityModule_BindChangePasswordActivity.ChangePasswordActivitySubcomponent.Builder>() {
            public com.sambhar.sambharappreport.di.ActivityModule_BindChangePasswordActivity.ChangePasswordActivitySubcomponent.Builder get() {
                return new ChangePasswordActivitySubcomponentBuilder(DaggerAppComponent.this, null);
            }
        };
        this.bindAndroidInjectorFactoryProvider6 = this.changePasswordActivitySubcomponentBuilderProvider;
        this.mapOfClassOfAndProviderOfFactoryOfProvider = MapProviderFactory.builder(6).put(MainActivity.class, this.bindAndroidInjectorFactoryProvider).put(LoginActivity.class, this.bindAndroidInjectorFactoryProvider2).put(RegisterActivity.class, this.bindAndroidInjectorFactoryProvider3).put(RegisterDetailDataActivity.class, this.bindAndroidInjectorFactoryProvider4).put(EditProfileActivity.class, this.bindAndroidInjectorFactoryProvider5).put(ChangePasswordActivity.class, this.bindAndroidInjectorFactoryProvider6).build();
        this.dispatchingAndroidInjectorProvider = DispatchingAndroidInjector_Factory.create(this.mapOfClassOfAndProviderOfFactoryOfProvider);
        this.sambharApplicationMembersInjector = SambharApplication_MembersInjector.create(this.dispatchingAndroidInjectorProvider);
        this.applicationProvider = InstanceFactory.create(builder.application);
        this.provideContextProvider = DoubleCheck.provider(AppModule_ProvideContextFactory.create(builder.appModule, this.applicationProvider));
        this.provideUserSharedPrefProvider = AppModule_ProvideUserSharedPrefFactory.create(builder.appModule, this.provideContextProvider);
        this.provideHeaderInterceptorProvider = DoubleCheck.provider(AppModule_ProvideHeaderInterceptorFactory.create(builder.appModule, this.provideUserSharedPrefProvider));
        this.provideAuthenticationInterceptorProvider = DoubleCheck.provider(AppModule_ProvideAuthenticationInterceptorFactory.create(builder.appModule, this.provideUserSharedPrefProvider));
        this.provideChuckInterceptorProvider = DoubleCheck.provider(AppModule_ProvideChuckInterceptorFactory.create(builder.appModule, this.provideContextProvider));
        this.provideOkHttpClientProvider = DoubleCheck.provider(AppModule_ProvideOkHttpClientFactory.create(builder.appModule, this.provideHeaderInterceptorProvider, this.provideAuthenticationInterceptorProvider, this.provideChuckInterceptorProvider));
        this.provideGsonProvider = DoubleCheck.provider(AppModule_ProvideGsonFactory.create(builder.appModule));
        this.liveDataCallAdapterFactoryProvider = DoubleCheck.provider(LiveDataCallAdapterFactory_Factory.create(MembersInjectors.noOp(), this.provideGsonProvider));
        this.provideAppRestProvider = DoubleCheck.provider(RestBuilderModule_ProvideAppRestFactory.create(builder.restBuilderModule, this.provideOkHttpClientProvider, this.provideGsonProvider, this.liveDataCallAdapterFactoryProvider));
        this.appRemoteDataSourceProvider = AppRemoteDataSource_Factory.create(this.provideAppRestProvider);
        this.appRepositoryProvider = DoubleCheck.provider(AppRepository_Factory.create(this.appRemoteDataSourceProvider));
        this.mainViewModelProvider = MainViewModel_Factory.create(MembersInjectors.noOp(), this.appRepositoryProvider);
        this.bindMainViewModelProvider = this.mainViewModelProvider;
        this.loginViewModelProvider = LoginViewModel_Factory.create(MembersInjectors.noOp(), this.appRepositoryProvider);
        this.bindLoginViewModelProvider = this.loginViewModelProvider;
        this.registerViewModelProvider = RegisterViewModel_Factory.create(MembersInjectors.noOp(), this.appRepositoryProvider);
        this.bindRegisterViewModelProvider = this.registerViewModelProvider;
        this.editProfileViewModelProvider = EditProfileViewModel_Factory.create(MembersInjectors.noOp(), this.appRepositoryProvider);
        this.bindEditProfileViewModelProvider = this.editProfileViewModelProvider;
        this.changePasswordViewModelProvider = ChangePasswordViewModel_Factory.create(MembersInjectors.noOp(), this.appRepositoryProvider);
        this.bindChangePasswordViewModelProvider = this.changePasswordViewModelProvider;
        this.mapOfClassOfAndProviderOfViewModelProvider = MapProviderFactory.builder(5).put(MainViewModel.class, this.bindMainViewModelProvider).put(LoginViewModel.class, this.bindLoginViewModelProvider).put(RegisterViewModel.class, this.bindRegisterViewModelProvider).put(EditProfileViewModel.class, this.bindEditProfileViewModelProvider).put(ChangePasswordViewModel.class, this.bindChangePasswordViewModelProvider).build();
        this.projectViewModelFactoryProvider = ProjectViewModelFactory_Factory.create(this.mapOfClassOfAndProviderOfViewModelProvider);
        this.bindViewModelFactoryProvider = DoubleCheck.provider(this.projectViewModelFactoryProvider);
    }

    public void inject(SambharApplication sambharApplication) {
        this.sambharApplicationMembersInjector.injectMembers(sambharApplication);
    }
}
