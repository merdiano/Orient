package com.tps.orientnews.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.tps.orientnews.data.CategoryRepository;
import com.tps.orientnews.data.Listing;
import com.tps.orientnews.data.NetworkState;
import com.tps.orientnews.data.PostRepository;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Category;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.room.PostDao;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by merdan on 8/9/18.
 */
@PerActivity
public class MainActivityViewModel extends ViewModel {
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private LiveData<List<Category>> categories;
    private MutableLiveData<Integer> source = new MutableLiveData<>();
    private LiveData<Listing<Post>> repoResult;
    public LiveData<PagedList<Post>> postList;
    public LiveData<NetworkState> networkState;

    @Inject
    MainActivityViewModel(CategoryRepository categoryRepository,PostRepository postRepository){
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;

        repoResult = Transformations.map(source, input -> postRepository.postsOfOrient(input));
        postList = Transformations.switchMap(repoResult,input -> input.pagedList);
        networkState = Transformations.switchMap(repoResult,input -> input.networkState);
        //source.setValue(-1);
    }

    public boolean loadPosts(int source){
        try {
            if(source == this.source.getValue())
                return false;
        }
        catch (Exception e){}
        this.source.setValue(source);
        return true;
    }

    public void retry(){
        if(repoResult != null && repoResult.getValue() != null)
            repoResult.getValue().retry.apply(null);

    }

    public void loadCategories(){
        categoryRepository.refreshCategories();
    }

    public LiveData<List<Category>> getCategories(){
        if(this.categories == null)
        {
            categories = categoryRepository.getCategories();
        }
        return this.categories;
    }

    public int currentSource(){
        try {
            return source.getValue();
        }catch (Exception e){
            return -1;
        }
    }
}
