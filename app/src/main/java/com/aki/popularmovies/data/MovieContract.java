package com.aki.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.aki.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movies";

    public static final class MovieEntry implements BaseColumns{

        //content uri is main, its the location of where the uri is in the system.
        //content_uri = content://com.aki.popularmovies/movies
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        // content_type = vnd.android.cursor.dir/com.aki.popularmoviesmovies
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MOVIE ;
        //item_type = vnd.android.cursor.item/com.aki.popularmoviesmovies
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MOVIE;


        //Table columns names
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_NAME = "movie_name";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_POSTER_BLOB = "poster_blob";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static Uri buildMovieUriWithID(long id){
            //fn returns = content://com.aki.popularmovies/movies/1024 <- some id
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }

}
