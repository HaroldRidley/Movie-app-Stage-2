package com.popmovies.hridley.popularmovies.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String AUTHORITY = "com.popmovies.hridley.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public static final class FavoriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String MOVIE_ID = "movieId";
        public static final String BACKDROP_PATH = "backdropPath";
        public static final String POSTER_PATH = "posterPath";
        public static final String OVERVIEW = "overview";
        public static final String TITLE = "title";
        public static final String RELEASE_DATE = "releaseDate";
        public static final String VOTE_AVERAGE = "voteAverage";

        public static Uri buildFavoriteUriWithMovieId(int movieId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .build();
        }
    }

}
