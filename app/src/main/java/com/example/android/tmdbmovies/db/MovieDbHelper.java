package com.example.android.tmdbmovies.db;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import static java.nio.charset.CodingErrorAction.REPLACE;


/**
 * Created by Ashok on 6/22/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "movies.db";

    private static final int DATABASE_VERSION = 4;

    private ContentResolver myCR;

    public MovieDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myCR = context.getContentResolver();
    }

    @Override
    public void onCreate (SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVOURITES_TABLE =
                "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + "(" +
                        MovieContract.MovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        " UNIQUE (" + MovieContract.MovieEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
