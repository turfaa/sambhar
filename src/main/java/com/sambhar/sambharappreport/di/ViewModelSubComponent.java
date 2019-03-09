package com.sambhar.sambharappreport.di;

import dagger.Subcomponent;

@Subcomponent
public interface ViewModelSubComponent {

    @dagger.Subcomponent.Builder
    public interface Builder {
        ViewModelSubComponent build();
    }
}
