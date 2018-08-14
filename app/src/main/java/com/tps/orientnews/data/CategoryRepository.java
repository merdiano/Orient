package com.tps.orientnews.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.tps.orientnews.api.CategoriesWrapper;
import com.tps.orientnews.api.OrientNewsService;

import com.tps.orientnews.room.CategoryDao;
import com.tps.orientnews.room.Category;



import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Created by merdan on 8/2/18.
 */
@Singleton
public class CategoryRepository {
    private final OrientNewsService webService;
    private final CategoryDao categoryDao;
    private final Executor executor;
    @Inject
    CategoryRepository(OrientNewsService webService, CategoryDao categoryDao, Executor executor){
        this.categoryDao = categoryDao;
        this.webService = webService;
        this.executor = executor;
    }

    public LiveData<List<Category>> getCategories(){
        refreshCategories();
        return categoryDao.loadAll();
    }

    public void refreshCategories(){

        executor.execute(()->{
            if(categoryDao.count()==0)
            try {
                Response<CategoriesWrapper> response = webService.getCategories().execute();
                if(response.isSuccessful() && response.body() != null){
                    List<Category> categories = response.body().categories;
                    if(categories != null && !categories.isEmpty()){

                        categoryDao.insertMany(response.body().categories);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

