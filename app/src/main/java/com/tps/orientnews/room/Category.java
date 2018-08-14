package com.tps.orientnews.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

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
