package com.tps.orientnews;

import com.tps.orientnews.models.Category;
import com.tps.orientnews.models.CategoryDao;
import com.tps.orientnews.models.DaoSession;
import com.tps.orientnews.models.OrientPost;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by merdan on 7/12/18.
 */
@Singleton
public class Repository {
    @Inject
    DaoSession daoSession;
    private static final int POSTS_LIMIT = 10;
    @Inject
    public Repository() {
    }

    public List<OrientPost>getPosts(int page){
        return daoSession.getOrientPostDao()
                .queryBuilder()
                .offset(page*POSTS_LIMIT)
                .limit(POSTS_LIMIT)
                .list();
    }

    public void insertPosts(List<OrientPost> posts){
        daoSession.getOrientPostDao()
                .insertInTx(posts);
    }

    public List<Category> getCategories(){
        List cats = daoSession.getCategoryDao().loadAll();
        if(cats == null || cats.size()==0){
            cats = getDefaultCats();
            daoSession.getCategoryDao().insertInTx(cats);
        }
        return cats;
    }
    public List<Category> getActiveCategories(){
        return daoSession.getCategoryDao()
                .queryBuilder()
                .where(CategoryDao.Properties.Active.eq(true))
                .list();
    }
    public void updateCategory(Category cat){
        daoSession.update(cat);

    }
    private List<Category> getDefaultCats() {
        ArrayList<Category> categories=new ArrayList<Category>();
//        categories.add(new Category(1l,"Recent News",true));
//        categories.add(new Category(2l,"Events",false));
//        categories.add(new Category(3l,"Economy",false));
//        categories.add(new Category(5l,"Culture",false));
//        categories.add(new Category(6l,"Sport",false));
//        categories.add(new Category(10l,"World",false));
//        categories.add(new Category(41l,"Society",false));
//        categories.add(new Category(101l,"Tender",false));
        return categories;
    }
}
