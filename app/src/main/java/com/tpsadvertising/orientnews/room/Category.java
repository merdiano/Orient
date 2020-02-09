package com.tpsadvertising.orientnews.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Created by merdan on 8/6/18.
 */
@Entity
public class Category {
    @NonNull
    @PrimaryKey
//    @ColumnInfo(name = "cid")
    public int id;
//    @ColumnInfo(name = "ctitle")
    public String title;
}
