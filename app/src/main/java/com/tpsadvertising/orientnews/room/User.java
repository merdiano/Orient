package com.tpsadvertising.orientnews.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by merdan on 8/6/18.
 */
@Entity
public class User {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "uid")
    public int id;
    public String name;
    public String nickname;
    @ColumnInfo(name = "picture_url")
    public String url;
    public String first_name;
    public String last_name;
    public String description;
    public String fullName(){
        return first_name+" "+last_name;
    };
}
