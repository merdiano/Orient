package com.tpsadvertising.orientnews.injectors;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import javax.inject.Inject;

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
