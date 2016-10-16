package com.aki.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Movie implements Parcelable{
    String title;
    String poster;
    String synopsis;
    String user_rating;
    String release_date;
    String id;
    ArrayList<String> reviewerName;
    ArrayList<String> reviewContent;
    ArrayList<String> trailers;
    byte [] blobData;
    boolean isFav;

    public Movie(){
        title = null;
        poster = "http://image.tmdb.org/t/p/w500";
        synopsis = null;
        user_rating = null;
        release_date = null;
        id = null;
        reviewerName = new ArrayList<>();
        reviewContent = new ArrayList<>();
        trailers = new ArrayList<>();
        isFav = false;
        blobData = null;
    }

    protected Movie(Parcel in) {
        title = in.readString();
        poster = in.readString();
        synopsis = in.readString();
        user_rating = in.readString();
        release_date = in.readString();
        id = in.readString();
        reviewerName = in.createStringArrayList();
        reviewContent = in.createStringArrayList();
        trailers = in.createStringArrayList();
       // blobData = in.createByteArray();
        isFav = in.readByte() != 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(synopsis);
        dest.writeString(user_rating);
        dest.writeString(release_date);
        dest.writeString(id);
        dest.writeStringList(reviewerName);
        dest.writeStringList(reviewContent);
        dest.writeStringList(trailers);
       // dest.writeByteArray(blobData);
        dest.writeByte((byte) (isFav ? 1 : 0));
    }
}
