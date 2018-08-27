package com.tps.orientnews.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.tps.orientnews.data.PostRepository;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.room.PostDao;

import javax.inject.Inject;

/**
 * Created by merdan on 8/22/18.
 */
@PerActivity
public class DetailActivityViewModel extends ViewModel {
    private final PostRepository repository;
    public LiveData<Post> post;
    MutableLiveData<Integer> postId = new MutableLiveData<>();
    @Inject
    DetailActivityViewModel(PostRepository repository){
        this.repository = repository;
        post = Transformations.switchMap(postId,it->repository.getPost(it));
    }

    public void loadPost(int id){
        this.postId.setValue(id);
    }
//    public void reload(int id)
    public boolean addToFavorite(){
        if(this.post != null){
            Post p = this.post.getValue();
            p.isFavorite = !p.isFavorite;
            repository.updatePost(p);
            return p.isFavorite;
        }
        return false;
    }
}

