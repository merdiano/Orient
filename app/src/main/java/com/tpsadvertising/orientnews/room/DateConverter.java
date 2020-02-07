package com.tpsadvertising.orientnews.room;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by merdan on 8/6/18.
 */

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
