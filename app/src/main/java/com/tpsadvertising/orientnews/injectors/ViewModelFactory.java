package com.tpsadvertising.orientnews.injectors;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by merdan on 8/2/18.
 */

public class ViewModelFactory<T extends ViewModel> implements ViewModelProvider.Factory {
    private dagger.Lazy<T> viewModel;
    @Inject
    ViewModelFactory(dagger.Lazy<T> viewModel){
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T)viewModel.get();
    }
}
