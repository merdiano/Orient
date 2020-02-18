package com.tpsadvertising.orientnews;

import com.tpsadvertising.orientnews.room.Assets;
import com.tpsadvertising.orientnews.room.Category;
import com.tpsadvertising.orientnews.room.User;

import java.util.Date;


public class News {

    private int id;
    private Assets thumbnail_images;
    private int categryId;
    private int authorId;
    private String date;
    private String title;
    private String url;
    private String content;
    private boolean isFavorite;
    private boolean hasFadedIn;
    private int views;
    private User author;
    private Category category;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Assets getThumbnail_images() {
        return thumbnail_images;
    }

    public void setThumbnail_images(Assets thumbnail_images) {
        this.thumbnail_images = thumbnail_images;
    }

    public int getCategryId() {
        return categryId;
    }

    public void setCategryId(int categryId) {
        this.categryId = categryId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isHasFadedIn() {
        return hasFadedIn;
    }

    public void setHasFadedIn(boolean hasFadedIn) {
        this.hasFadedIn = hasFadedIn;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
