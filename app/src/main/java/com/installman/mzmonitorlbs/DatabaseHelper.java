package com.installman.mzmonitorlbs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhong on 16-3-23.
 */

class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "moniterLbs.db";

    public DatabaseHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(final SQLiteDatabase db) {
        String sql = "create table mzMonitor(" +
                "_id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "latitude DOUBLE NOT NULL," +
                "longitude DOUBLE NOT NULL," +
                "monitor_type INT NOT NULL," +
                "monitor_angle INT NOT NULL," +
                "station TEXT" +
                ")";
        db.execSQL(sql);
    }

    public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
        String sql = "drop table mzMonitor";
        db.execSQL(sql);
    }
}
