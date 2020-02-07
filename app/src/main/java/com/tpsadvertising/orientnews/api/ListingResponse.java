package com.tpsadvertising.orientnews.api;


import com.tpsadvertising.orientnews.room.Post;

import java.util.List;

/**
 * Created by merdan on 7/12/18.
 */

public class ListingResponse {
    public int count;
    public int total_count;
    public int pages;
    public List<Post> posts;
}
