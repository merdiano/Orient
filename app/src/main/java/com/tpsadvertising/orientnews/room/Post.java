package com.tpsadvertising.orientnews.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.RoomWarnings;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created by merdan on 8/6/18.
 */
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(foreignKeys = {@ForeignKey(entity = Category.class,parentColumns = "id",childColumns = "category_id"),
        @ForeignKey(entity = User.class,parentColumns = "uid",childColumns = "author_id")})
public class Post {
    @NotNull
    @PrimaryKey
    public int id;
    @Embedded
    public Assets thumbnail_images;
    @ColumnInfo(name = "category_id")

    public int categryId;
    @ColumnInfo(name = "author_id")
    public int authorId;
    public Date date;
    public String title;
    public String url;
    public String content;
//    public String excerpt;
    public boolean isFavorite;
    public boolean hasFadedIn;
    public int views;
    @Embedded
    public User author;
    @Ignore
    public Category category;
}
