package com.tpsadvertising.orientnews.utils;

import com.tpsadvertising.orientnews.room.Post;

import java.util.Comparator;

/**
 * Created by merdan on 7/24/18.
 */

public class PostComperator {
    public static class OrientItemComparator implements Comparator<Post> {

        @Override
        public int compare(Post lhs, Post rhs) {
            return Float.compare(rhs.id,lhs.id);
        }
    }
}
