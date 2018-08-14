package com.tps.orientnews.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.tps.orientnews.data.PostRepository;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.room.PostDao;

import javax.inject.Inject;

/**
 * Created by merdan on 8/12/18.
 */
@PerActivity
public class SearchViewModel extends ViewModel {
    //private final PostDao repository;
    private MutableLiveData<String> query = new MutableLiveData<>();
    public LiveData<PagedList<Post>> postList;
    @Inject
    SearchViewModel(PostDao repository, PagedList.Config config){
        //this.repository = repository;
        postList = Transformations.switchMap(query, q ->{
            return new LivePagedListBuilder(repository.searchPosts(q),config)
                    .build();
        });
    }

    public boolean shearch(String key){
        String qry ="%"+key+"%";
        if(this.query.getValue() == qry)
            return false;
        this.query.setValue(qry);
        return true;
    }
}
