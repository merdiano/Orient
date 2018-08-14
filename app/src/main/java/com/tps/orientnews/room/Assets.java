package com.tps.orientnews.room;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by merdan on 8/6/18.
 */
//@Entity()
public class Assets {
// @PrimaryKey(autoGenerate = true)
// int id;
 public String largeUrl;
 public String mediumUrl;
 public String thumbnailUrl;
 public int largeWidh;
 public int mediumWidh;
 public int thumbnailWidh;
 public int largeHeght;
 public int mediumHeght;
 public int thumbnailHeght;
 @Ignore
 public Image large;
 @Ignore
 public Image medium;
 @Ignore
 public Image thumbnail;
}
