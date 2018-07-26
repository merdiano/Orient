package com.tps.orientnews.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by merdan on 7/14/18.
 */
@Entity
public class PostsWithCategories {
    @Id
    private Long id;
    private Long postId;
    private Long categoryId;
    @Generated(hash = 76145136)
    public PostsWithCategories(Long id, Long postId, Long categoryId) {
        this.id = id;
        this.postId = postId;
        this.categoryId = categoryId;
    }
    @Generated(hash = 398618490)
    public PostsWithCategories() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getPostId() {
        return this.postId;
    }
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    public Long getCategoryId() {
        return this.categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
