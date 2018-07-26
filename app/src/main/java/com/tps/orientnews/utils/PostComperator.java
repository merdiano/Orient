package com.tps.orientnews.utils;

import com.tps.orientnews.models.OrientPost;

import java.util.Comparator;

/**
 * Created by merdan on 7/24/18.
 */

public class PostComperator {
    public static class OrientItemComparator implements Comparator<OrientPost> {

        @Override
        public int compare(OrientPost lhs, OrientPost rhs) {
            return Float.compare(lhs.getId(), rhs.getId());
        }
    }
}
