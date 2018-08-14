package com.tps.orientnews.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.tps.orientnews.data.CategoryRepository;
import com.tps.orientnews.data.PostRepository;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Category;
import com.tps.orientnews.room.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by merdan on 8/2/18.
 */
@PerActivity
public class MainViewModel extends ViewModel {
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private LiveData<List<Category>> categories;
    private MediatorLiveData<List<Post>> postsLivedata = new MediatorLiveData<>();
    private MutableLiveData<Integer> page;
    private MutableLiveData<Integer> source;//mabe can use map;
    private int pageIndex = 1;
    @Inject
    public MainViewModel(CategoryRepository categoryRepository, PostRepository postRepository){
        this.categoryRepository = categoryRepository;
        this.postRepository = postRepository;
//        this.postRepository.buildQuery();

//        postsLivedata.addSource(getSource(), source -> {
//            postsLivedata.setValue(postRepository.getPosts(1,source).getValue());
//        });
//        postsLivedata.addSource(getPage(), page->{
//            int source = getSource().getValue();
//            List<Post> repoPosts = postRepository.getPosts(page,source).getValue();
//            if(repoPosts == null || repoPosts.isEmpty())return;
//            if(page == 1){
//                postsLivedata.setValue(repoPosts);
//            }
//            else{
//                List<Post> oldPosts = postsLivedata.getValue();
//
//                if(oldPosts == null)
//                    oldPosts = new ArrayList<>(repoPosts);
//
//                postsLivedata.setValue(oldPosts);
//            }
//
//        });
    }

    public MutableLiveData<Integer> getPage(){
        if(page == null)
        {
            page = new MutableLiveData<>();
            page.setValue(1);
        }
        return page;

    }

    public MutableLiveData<Integer> getSource(){
        if(source == null){
            source = new MutableLiveData<>();
            source.setValue(-1);
        }
        return source;
    }

    public LiveData<List<Category>> getCategories(){
        if(this.categories == null)
        {
            categories = categoryRepository.getCategories();
        }
        return this.categories;
    }

//    public LiveData<List<Post>> getPostsLivedata()
//    {
//        return Transformations.switchMap(getPage(),page->
//             postRepository.getPosts(page,getSource().getValue())
//       );
//    }


    public void loadPosts(){
        pageIndex = 0;
//        postRepository.buildQuery();
        loadMore();

    }
    public void loadPosts(long categoryId){
        pageIndex =0;
//        postRepository.changeQuery(categoryId);
        loadMore();
    }

    public void loadMore(){
        pageIndex++;
//        this.postsLivedata.setValue(postRepository.getPostsLivedata(pageIndex).getValue());
    }
}
