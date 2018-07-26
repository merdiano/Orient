package com.tps.orientnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by merdan on 7/12/18.
 */
@Entity
public class Assets implements Parcelable {

    @Id(autoincrement = true)
    Long id;
    private String mediumImageId;
    private String largeImageId;
    private String thumbnailId;
    @ToOne(joinProperty = "mediumImageId")
    public Image medium;
    @ToOne (joinProperty = "largeImageId")
    public Image large;
    @ToOne (joinProperty = "thumbnailId")
    public Image thumbnail;

    protected Assets(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        mediumImageId = in.readString();
        largeImageId = in.readString();
        thumbnailId = in.readString();
        medium = (Image)in.readValue(Image.class.getClassLoader());
        large = (Image) in.readValue(Image.class.getClassLoader());
        thumbnail = (Image) in.readValue(Image.class.getClassLoader());
    }
    public String getThumbUrl(){
        if(thumbnail != null)
            return thumbnail.getUrl();
        return "http://orient.tm/wp-content/themes/newscore/assets/images/placeholder-plain.gif";
    }
    @Generated(hash = 2036579909)
    public Assets(Long id, String mediumImageId, String largeImageId, String thumbnailId) {
        this.id = id;
        this.mediumImageId = mediumImageId;
        this.largeImageId = largeImageId;
        this.thumbnailId = thumbnailId;
    }
    @Generated(hash = 1373698660)
    public Assets() {
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(mediumImageId);
        dest.writeString(largeImageId);
        dest.writeString(thumbnailId);
        dest.writeValue(medium);
        dest.writeValue(large);
        dest.writeValue(thumbnail);
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getMediumImageId() {
        return this.mediumImageId;
    }
    public void setMediumImageId(String mediumImageId) {
        this.mediumImageId = mediumImageId;
    }
    public String getLargeImageId() {
        return this.largeImageId;
    }
    public void setLargeImageId(String largeImageId) {
        this.largeImageId = largeImageId;
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1439146063)
    public Image getMedium() {
        String __key = this.mediumImageId;
        if (medium__resolvedKey == null || medium__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ImageDao targetDao = daoSession.getImageDao();
            Image mediumNew = targetDao.load(__key);
            synchronized (this) {
                medium = mediumNew;
                medium__resolvedKey = __key;
            }
        }
        return medium;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1397620742)
    public void setMedium(Image medium) {
        synchronized (this) {
            this.medium = medium;
            mediumImageId = medium == null ? null : medium.getUrl();
            medium__resolvedKey = mediumImageId;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1056222427)
    public Image getLarge() {
        String __key = this.largeImageId;
        if (large__resolvedKey == null || large__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ImageDao targetDao = daoSession.getImageDao();
            Image largeNew = targetDao.load(__key);
            synchronized (this) {
                large = largeNew;
                large__resolvedKey = __key;
            }
        }
        return large;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 440655601)
    public void setLarge(Image large) {
        synchronized (this) {
            this.large = large;
            largeImageId = large == null ? null : large.getUrl();
            large__resolvedKey = largeImageId;
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
    public String getThumbnailId() {
        return this.thumbnailId;
    }
    public void setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1738043243)
    public Image getThumbnail() {
        String __key = this.thumbnailId;
        if (thumbnail__resolvedKey == null || thumbnail__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ImageDao targetDao = daoSession.getImageDao();
            Image thumbnailNew = targetDao.load(__key);
            synchronized (this) {
                thumbnail = thumbnailNew;
                thumbnail__resolvedKey = __key;
            }
        }
        return thumbnail;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1358470438)
    public void setThumbnail(Image thumbnail) {
        synchronized (this) {
            this.thumbnail = thumbnail;
            thumbnailId = thumbnail == null ? null : thumbnail.getUrl();
            thumbnail__resolvedKey = thumbnailId;
        }
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1655196095)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAssetsDao() : null;
    }
    public static final Creator<Assets> CREATOR = new Creator<Assets>() {
        @Override
        public Assets createFromParcel(Parcel in) {
            return new Assets(in);
        }

        @Override
        public Assets[] newArray(int size) {
            return new Assets[size];
        }
    };
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 549332066)
    private transient AssetsDao myDao;
    @Generated(hash = 784349418)
    private transient String medium__resolvedKey;
    @Generated(hash = 684853957)
    private transient String large__resolvedKey;
    @Generated(hash = 598656630)
    private transient String thumbnail__resolvedKey;


}
