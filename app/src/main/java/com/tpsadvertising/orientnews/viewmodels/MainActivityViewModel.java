package com.tpsadvertising.orientnews.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.util.Log;

import com.tpsadvertising.orientnews.api.ReklamaService;
import com.tpsadvertising.orientnews.data.CategoryRepository;
import com.tpsadvertising.orientnews.data.Listing;
import com.tpsadvertising.orientnews.data.NetworkState;
import com.tpsadvertising.orientnews.data.PostRepository;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Category;
import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.room.PostDao;
import com.tpsadvertising.orientnews.room.Reklama;

import java.util.List;

import javax.inject.Inject;

import kotlin.NotImplementedError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by merdan on 8/9/18.
 */
@PerActivity
public class MainActivityViewModel extends ViewModel {
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final ReklamaService adService;
    private LiveData<List<Category>> categories;
    private MutableLiveData<Integer> source = new MutableLiveData<>();
    private LiveData<Listing<Post>> repoResult;
    public LiveData<PagedList<Post>> postList;
    public LiveData<NetworkState> networkState;
    public MutableLiveData<List<Reklama>> adverts = new MutableLiveData<>();
//    public LiveData<PagedList<Post>> favoritePosts;
    @Inject
    MainActivityViewModel(CategoryRepository categoryRepository,
                          PostRepository postRepository,
                          ReklamaService adService){
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
        this.adService = adService;
        repoResult = Transformations.map(source, input ->{
            if(input == 0)
                return postRepository.favoritePosts();
            else
                return postRepository.postsOfOrient(input);
        }) ;
        //todo map favorites
        postList = Transformations.switchMap(repoResult,input -> input.pagedList);

        networkState = Transformations.switchMap(repoResult,input -> input.networkState);
//
//        favoritePosts = Transformations.map(source,s->{
//            if(s==0)
//                return postRepository.favoritePosts();
//                });
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

    public void loadAdverts(){
        adService.getAds().enqueue(new Callback<List<Reklama>>() {
            @Override
            public void onResponse(Call<List<Reklama>> call, Response<List<Reklama>> response) {
                if(response.isSuccessful() && response.body() != null){
                    adverts.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Reklama>> call, Throwable t) {
                Log.d("Adverts","Failed loading adverts");
            }
        });
    }
//    public void loadFavorites(){
//        postList = postRepository.favoritePosts();
//    }

    public void retry(){
        if(repoResult != null && repoResult.getValue() != null)
            repoResult
                    .getValue()
                    .retry
                    .apply(null);

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
