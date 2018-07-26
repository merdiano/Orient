package com.tps.orientnews.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by merdan on 7/12/18.
 */
@Entity
public class User implements Parcelable {

    @Id
    private Long id;
    private String name;
    private String nickname;
    private String url;
    private String first_name;
    private String last_name;
    private String description;
    @Generated(hash = 1489505123)
    public User(Long id, String name, String nickname, String url,
            String first_name, String last_name, String description) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.url = url;
        this.first_name = first_name;
        this.last_name = last_name;
        this.description = description;
    }
    protected User(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        name = in.readString();
        nickname = in.readString();
        url = in.readString();
        first_name = in.readString();
        last_name = in.readString();
        description = in.readString();
    }
    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public String getNickname() {
        return this.nickname;
    }
    public String getUrl() {
        return this.url;
    }
    public String getFirst_name() {
        return this.first_name;
    }
    public String getLast_name() {
        return this.last_name;
    }
    public String getDescription() {
        return this.description;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(nickname);
        dest.writeString(url);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(description);
    }
    public String fullname(){
        return first_name+" "+last_name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }
    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
