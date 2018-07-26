package com.tps.orientnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by merdan on 7/12/18.
 */
@Entity
public class OrientPost implements Parcelable{

    @Id private Long id;
    private Date date;
    private String title;
    private String url;
    private String content;
    private String excerpt;
    private long assets_id;
    @Transient
    public boolean hasFadedIn = false;
    @ToOne(joinProperty = "assets_id")
    public Assets thumbnail_images;
    private long auther_id;
    @ToOne(joinProperty = "auther_id")
    public User author;
    private int views;
    private long categoryId;
    @ToOne(joinProperty = "categoryId")
    public Category category;


    protected OrientPost(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        long tmpCreated_at = in.readLong();
        date = tmpCreated_at != -1 ? new Date(tmpCreated_at) : null;
        title = in.readString();
        url = in.readString();
        content = in.readString();
        excerpt = in.readString();
        assets_id = in.readLong();
        thumbnail_images = (Assets) in.readValue(Assets.class.getClassLoader());
        auther_id = in.readLong();
        author =  (User)in.readValue(User.class.getClassLoader());
        views = in.readInt();
        categoryId = in.readLong();
        category=(Category) in.readValue(Category.class.getClassLoader());
    }

    @Generated(hash = 1786417787)
    public OrientPost(Long id, Date date, String title, String url, String content, String excerpt,
            long assets_id, long auther_id, int views, long categoryId) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.url = url;
        this.content = content;
        this.excerpt = excerpt;
        this.assets_id = assets_id;
        this.auther_id = auther_id;
        this.views = views;
        this.categoryId = categoryId;
    }

    @Generated(hash = 540323559)
    public OrientPost() {
    }

    public static final Creator<OrientPost> CREATOR = new Creator<OrientPost>() {
        @Override
        public OrientPost createFromParcel(Parcel in) {
            return new OrientPost(in);
        }

        @Override
        public OrientPost[] newArray(int size) {
            return new OrientPost[size];
        }
    };
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 2028743736)
    private transient OrientPostDao myDao;
    @Generated(hash = 2061882855)
    private transient Long thumbnail_images__resolvedKey;
    @Generated(hash = 1107320010)
    private transient Long author__resolvedKey;
    @Generated(hash = 1372501278)
    private transient Long category__resolvedKey;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(date != null ? date.getTime() : -1L);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(content);
        dest.writeString(excerpt);
        dest.writeLong(assets_id);
        dest.writeValue(thumbnail_images);
        dest.writeLong(auther_id);
        dest.writeValue(author);
        dest.writeInt(views);
        dest.writeLong(categoryId);
        dest.writeValue(category);
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getDate() {
        return this.date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getExcerpt() {
        return this.excerpt;
    }
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
    public long getAssets_id() {
        return this.assets_id;
    }
    public void setAssets_id(long assets_id) {
        this.assets_id = assets_id;
    }
    public long getAuther_id() {
        return this.auther_id;
    }
    public void setAuther_id(long auther_id) {
        this.auther_id = auther_id;
    }
    public String getThumbUrl(){
        if(thumbnail_images!=null){
            return thumbnail_images.getThumbUrl();
        }
        return "http://orient.tm/wp-content/themes/newscore/assets/images/placeholder-plain.gif";
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getViews() {
        return this.views;
    }

    public void setViews(int views) {
        this.views = views;
    }



    @Override
    public boolean equals(Object o) {
        return (o.getClass() == getClass() && ((OrientPost) o).id == id);
    }

    public long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1620828150)
    public Assets getThumbnail_images() {
        long __key = this.assets_id;
        if (thumbnail_images__resolvedKey == null || !thumbnail_images__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AssetsDao targetDao = daoSession.getAssetsDao();
            Assets thumbnail_imagesNew = targetDao.load(__key);
            synchronized (this) {
                thumbnail_images = thumbnail_imagesNew;
                thumbnail_images__resolvedKey = __key;
            }
        }
        return thumbnail_images;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1914357809)
    public void setThumbnail_images(@NotNull Assets thumbnail_images) {
        if (thumbnail_images == null) {
            throw new DaoException(
                    "To-one property 'assets_id' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.thumbnail_images = thumbnail_images;
            assets_id = thumbnail_images.getId();
            thumbnail_images__resolvedKey = assets_id;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 689808633)
    public User getAuthor() {
        long __key = this.auther_id;
        if (author__resolvedKey == null || !author__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            User authorNew = targetDao.load(__key);
            synchronized (this) {
                author = authorNew;
                author__resolvedKey = __key;
            }
        }
        return author;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1031268283)
    public void setAuthor(@NotNull User author) {
        if (author == null) {
            throw new DaoException(
                    "To-one property 'auther_id' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.author = author;
            auther_id = author.getId();
            author__resolvedKey = auther_id;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 234631651)
    public Category getCategory() {
        long __key = this.categoryId;
        if (category__resolvedKey == null || !category__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CategoryDao targetDao = daoSession.getCategoryDao();
            Category categoryNew = targetDao.load(__key);
            synchronized (this) {
                category = categoryNew;
                category__resolvedKey = __key;
            }
        }
        return category;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1927364589)
    public void setCategory(@NotNull Category category) {
        if (category == null) {
            throw new DaoException(
                    "To-one property 'categoryId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.category = category;
            categoryId = category.getId();
            category__resolvedKey = categoryId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1242347044)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOrientPostDao() : null;
    }

}
