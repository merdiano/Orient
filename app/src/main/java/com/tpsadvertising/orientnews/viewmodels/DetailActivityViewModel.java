package com.tpsadvertising.orientnews.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tpsadvertising.orientnews.data.PostRepository;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Post;

import javax.inject.Inject;

/**
 * Created by merdan on 8/22/18.
 */
@PerActivity
public class DetailActivityViewModel extends ViewModel {
    private final PostRepository repository;
    public LiveData<Post> post;
    private MutableLiveData<Integer> postId = new MutableLiveData<>();
    public LiveData<Integer> nextPost;
    public LiveData<Integer> prevPost;
    @Inject
    DetailActivityViewModel(PostRepository repository){
        this.repository = repository;
        post = Transformations.switchMap(postId,it->repository.getPost(it));
        nextPost = Transformations.switchMap(postId, it->repository.getNextPostId(it));
        prevPost = Transformations.switchMap(postId, it->repository.getPrevPostId(it));
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

    public void loadNext(){
        if(postId == null || nextPost == null || nextPost.getValue() == null) return;
        postId.setValue(nextPost.getValue());
    }
    public void loadBefore(){
        if(postId == null || prevPost == null || prevPost.getValue() == null) return;

        postId.setValue(prevPost.getValue());
    }

    public LiveData<Post> checkPostFavorite(int postId){
        return repository.checkPostFavorite(postId);
    }

}

