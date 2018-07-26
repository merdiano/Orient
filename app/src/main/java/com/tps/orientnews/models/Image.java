package com.tps.orientnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by merdan on 7/12/18.
 */
@Entity
public class Image implements Parcelable{
    @Id
    @NotNull
    private String url;
    private int width;
    private int height;


    protected Image(Parcel in) {

        url = in.readString();
        width = in.readInt();
        height = in.readInt();
    }
    @Generated(hash = 316873959)
    public Image(@NotNull String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }
    @Generated(hash = 1590301345)
    public Image() {
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public int getWidth() {
        return this.width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return this.height;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(url);
        dest.writeInt(width);
        dest.writeInt(height);
    }
}
