package com.tpsadvertising.orientnews.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tpsadvertising.orientnews.api.CategoriesWrapper;
import com.tpsadvertising.orientnews.api.OrientNewsService;

import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.CategoryDao;
import com.tpsadvertising.orientnews.room.Category;



import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import retrofit2.Response;

/**
 * Created by merdan on 8/2/18.
 */
@PerActivity
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

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                if(categoryDao.count()==0) {
                    Response<CategoriesWrapper> response = webService.getCategories().execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<Category> categories = response.body().categories;
                        if (categories != null && !categories.isEmpty()) {
                            //categoryDao.deleteAll();
                            categoryDao.insertMany(response.body().categories);
                        }
                    }
//                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

