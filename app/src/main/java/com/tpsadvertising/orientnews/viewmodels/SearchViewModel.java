package com.tpsadvertising.orientnews.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.room.PostDao;

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
