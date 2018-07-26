package com.tps.orientnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by merdan on 7/13/18.
 */
@Entity
public class Category implements Parcelable{
    @Id
    private Long id;
    private String title;
    public boolean active = true;
    private int post_count;
    private int lastDownloadedPage;
    private int pages;

    @Generated(hash = 947788557)
    public Category(Long id, String title, boolean active, int post_count,
            int lastDownloadedPage, int pages) {
        this.id = id;
        this.title = title;
        this.active = active;
        this.post_count = post_count;
        this.lastDownloadedPage = lastDownloadedPage;
        this.pages = pages;
    }

    @Generated(hash = 1150634039)
    public Category() {
    }


    protected Category(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        active = in.readByte() != 0;
        post_count = in.readInt();
        lastDownloadedPage = in.readInt();
        pages = in.readInt();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        dest.writeString(title);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeInt(post_count);
        dest.writeInt(lastDownloadedPage);
        dest.writeInt(pages);
    }

    public int getPost_count() {
        return this.post_count;
    }

    public void setPost_count(int post_count) {
        this.post_count = post_count;
    }

    public int getLastDownloadedPage() {
        return this.lastDownloadedPage;
    }

    public void setLastDownloadedPage(int lastDownloadedPage) {
        this.lastDownloadedPage = lastDownloadedPage;
    }

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
