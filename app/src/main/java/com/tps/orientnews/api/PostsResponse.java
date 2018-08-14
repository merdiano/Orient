package com.tps.orientnews.api;


import com.tps.orientnews.room.Post;

import java.util.List;

/**
 * Created by merdan on 7/12/18.
 */

public class PostsResponse {
    public int count;
    public int total_count;
    public int pages;
    public List<Post> posts;
}
