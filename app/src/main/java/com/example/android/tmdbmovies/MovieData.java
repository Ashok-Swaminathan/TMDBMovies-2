package com.example.android.tmdbmovies;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.android.tmdbmovies.db.MovieContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by Ashok on 5/22/2017.
 */

public class MovieData {


    // Main
    final String RET_CODE = "cod";
    final String TITLE_CODE = "title";
    final String ID_CODE = "id";
    final String SYSNOPSIS_CODE = "overview";
    final String VOTE_AVERAGE_CODE = "vote_average";
    final String RELEASE_DATE_CODE = "release_date";
    final String POSTER_PATH_CODE = "poster_path";

    // Videos
    final String NAME_CODE = "name";
    final String KEY_CODE = "key";

    public  String jsonData = null;
    public  String[] movieId = new String[0];
    public  String[] movieTitle = new String[0];
    public  String[] releaseDate = new String[0];
    public  String[] posterPath = new String[0];
    public  String[] synopsis = new String[0];
    public  String[] rating = new String[0];

    // For passing to ShowMovieDetails if from favourites
    public String[] movieTrailerNames = null;
    public String[] movieTrailerPaths = null;
    public String[] movieReviewAuthors = null;
    public String[] movieReviewPaths = null;


    public int stage = 0;
    public String errorText = "";

    Context current;
    public MovieData(Context c){
        current = c;
    }

    public boolean populateMovieDataFromDB(Cursor cursor) {
        try {
            int num = cursor.getCount();
            movieId = new String[num];
            movieTitle = new String[num];
            releaseDate = new String[num];
            synopsis = new String[num];
            rating = new String[num];
            posterPath = new String[num];

            // These are also valid now
            movieTrailerNames =  new String[num];
            movieTrailerPaths =  new String[num];
            movieReviewAuthors =  new String[num];
            movieReviewPaths =  new String[num];

            cursor.moveToPosition(-1); int i = 0;
            while (cursor.moveToNext()) {
                movieId[i] = cursor.getString(MovieContract.MovieEntry.INDEX_MOVIE_ID);
                movieTitle[i] = cursor.getString(MovieContract.MovieEntry.INDEX_TITLE);
                releaseDate[i] = cursor.getString(MovieContract.MovieEntry.INDEX_RELEASE_DATE);
                synopsis[i] = cursor.getString(MovieContract.MovieEntry.INDEX_SYNOPSIS);
                rating[i] = cursor.getString(MovieContract.MovieEntry.INDEX_RATING);
                posterPath[i] = cursor.getString(MovieContract.MovieEntry.INDEX_POSTER_PATH);
                i++;
            }
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean populateMovieData(String data) {

        try {
            JSONObject jsonData = new JSONObject(data);
            stage = 0;
            if (jsonData.has(RET_CODE)) {
                int errorCode = jsonData.getInt(RET_CODE);

                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK: {
                        Log.d("DATA","RECEIVED HTTP OK");
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                    /* Location invalid */
                        errorText = "HTTP_NOT_FOUND";
                        return false;
                    }
                    default: {
                    /* Server probably down */
                    errorText = "OTHER";
                        return false;
                    }
                }
            }

            // Populate
            // Find array
            JSONArray movieDataArray = jsonData.getJSONArray("results");
            stage = 1;
            int num = movieDataArray.length();

            Log.d("MOVIE DATA","GOT ARRAY OF LENGTH " + num);

            movieId = new String[num];
            movieTitle = new String[num];
            releaseDate = new String[num];
            synopsis = new String[num];
            rating = new String[num];
            posterPath = new String[num];

            // Null the others since it is from main api.themoviedb.org
            movieTrailerNames = null;
            movieTrailerPaths = null;
            movieReviewAuthors = null;
            movieReviewPaths = null;
            for (int i = 0; i < num; i++) {
                JSONObject movieDataSet = movieDataArray.getJSONObject(i);
                movieId[i] = movieDataSet.getString(ID_CODE);
                movieTitle[i] = movieDataSet.getString(TITLE_CODE);
                releaseDate[i] = movieDataSet.getString(RELEASE_DATE_CODE);
                posterPath[i] = movieDataSet.getString(POSTER_PATH_CODE);
                synopsis[i] = movieDataSet.getString(SYSNOPSIS_CODE);
                rating[i] = movieDataSet.getString(VOTE_AVERAGE_CODE);
            }
            Log.d("MOVIE DATA","POPULATED MOVIE DATA");
            return true;

        }catch (Exception e) {
            Log.d("MOVIE DATA","ERROR ->" + e.getMessage());
            e.printStackTrace();
            errorText = "JSON " + e.getMessage();
            return false;
        }
    }
}
