package com.popmovies.hridley.popularmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.popmovies.hridley.popularmovies.db.MovieContract.FavoriteMovieEntry;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favoriteMoviesDb.db";
    private static final int VERSION = 1;

    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "      + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID                  + " INTEGER PRIMARY KEY, " +
                FavoriteMovieEntry.MOVIE_ID      + " INTEGER NOT NULL, " +
                FavoriteMovieEntry.BACKDROP_PATH + " TEXT NOT NULL, " +
                FavoriteMovieEntry.POSTER_PATH   + " TEXT NOT NULL, " +
                FavoriteMovieEntry.OVERVIEW      + " TEXT NOT NULL, " +
                FavoriteMovieEntry.TITLE         + " TEXT NOT NULL, " +
                FavoriteMovieEntry.RELEASE_DATE  + " TEXT NOT NULL, " +
                FavoriteMovieEntry.VOTE_AVERAGE  + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        onCreate(db);
    }

}
