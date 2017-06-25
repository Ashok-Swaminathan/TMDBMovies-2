package com.example.android.tmdbmovies.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * For storing data of favourite movies
 * Created by Ashok on 6/22/2017.
 */

public class MovieContract {
    // Content Authority
    public static final String CONTENT_AUTHORITY = "com.example.android.tmdbmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";

    public static final class MovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the favourites table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVOURITES)
                .build();

        /* Used internally as the name of our weather table. */
        public static final String TABLE_NAME = "favourites";

        // Indexing for Array and projection use
        public static final int INDEX_MOVIE_ID = 0;
        public static final int INDEX_TITLE = 1;
        public static final int INDEX_RELEASE_DATE = 2;
        public static final int INDEX_SYNOPSIS = 3;
        public static final int INDEX_RATING = 4;
        public static final int INDEX_POSTER_PATH = 5;


        // Column Names
        public static final String COLUMN_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_POSTER_PATH = "poster_path";


        // For projection fiels in query
        public static final String[] MOVIES_ALL_FIELDS = {
                COLUMN_ID,COLUMN_TITLE,COLUMN_RELEASE_DATE,COLUMN_SYNOPSIS,COLUMN_RATING,COLUMN_POSTER_PATH

        };

        // To build a URI based on movie_id
        public static Uri buildMovieUriWithId(String movieId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(movieId)
                    .build();
        }

    }
}
