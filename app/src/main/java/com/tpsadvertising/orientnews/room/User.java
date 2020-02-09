package com.tpsadvertising.orientnews.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

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
