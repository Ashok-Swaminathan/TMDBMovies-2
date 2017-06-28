package com.example.android.tmdbmovies.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by Ashok on 6/22/2017.
 */

 public class MovieProvider extends ContentProvider{

    // For UriMatcher
    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper movieDbHelper;


    public static UriMatcher buildUriMatcher() {
        /** 100 for all, 101 for single entry by movieId
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_FAVOURITES, CODE_MOVIES);

        // MovieId is a numeric
        matcher.addURI(authority, MovieContract.PATH_FAVOURITES + "/#", CODE_MOVIES_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate(){
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType (@NonNull Uri uri) {
        return "TEXT"; // Only type
    }


    // Only single inserts

    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues values) {


        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES_WITH_ID : {
                throw new UnsupportedOperationException("only single inserts");
            }
            case CODE_MOVIES : {
                try {
                    long _id = movieDbHelper.getWritableDatabase().insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                    return null;
                }catch (Exception e) {
                    Uri errorUri = Uri.parse("http://error.com/" + e.getMessage());
                    return errorUri;
                }

            }
            default :
                throw new UnsupportedOperationException("Invalid URI");
        }
    } // insert

    // Query
    // Ordering is as on insertion, not sorted on ID or Release Date etc
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        /*
         * For without ID, return all entries
         * Single entry with ID
         */
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES_WITH_ID: {
                String id = uri.getLastPathSegment();
                String[] selectArgs = new String[]{id};
                cursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_ID + " = ? ",
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_MOVIES: {
                cursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public Cursor getAllEntries() {
        return query(MovieContract.MovieEntry.CONTENT_URI,MovieContract.MovieEntry.MOVIES_ALL_FIELDS,null,null,null,null);
    }
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numDeleted;

        if (selection == null)
            selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES : { // To delete all
                numDeleted = movieDbHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            }
            case CODE_MOVIES_WITH_ID : { // Delete one
                selection = MovieContract.MovieEntry.COLUMN_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                numDeleted = movieDbHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
             break;
            }
            default :
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return numDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0; // Not used

    }
 }
