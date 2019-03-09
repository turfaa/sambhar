package com.sambhar.sambharappreport.di;

import com.sambhar.sambharappreport.SambharApplication;

public class AppInjector {
    private AppInjector() {
    }

    public static void init(SambharApplication sambharApplication) {
        DaggerAppComponent.builder().application(sambharApplication).build().inject(sambharApplication);
    }
}
