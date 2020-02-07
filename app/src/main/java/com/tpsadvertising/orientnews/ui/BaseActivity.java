package com.tpsadvertising.orientnews.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tpsadvertising.orientnews.injectors.ViewModelFactory;

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjection;

/**
 * Created by merdan on 7/13/18.
 */

public abstract class BaseActivity<T extends ViewModel> extends AppCompatActivity{

    @Inject @Named("defaultPrefs")
    SharedPreferences shPrefs;
    @Inject
    ViewModelFactory<T> viewModelFactory;
    T viewModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this,viewModelFactory)
                .get((Class<T>) ((ParameterizedType) this.getClass()
                        .getGenericSuperclass())
                        .getActualTypeArguments()[0]);
    }
}
