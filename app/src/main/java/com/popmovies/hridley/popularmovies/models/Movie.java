package com.popmovies.hridley.popularmovies.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.popmovies.hridley.popularmovies.R;

/**
 * Object representing a Movie, an item from the MovieDB API
 */
@Entity
public class Movie implements Parcelable {
    @PrimaryKey
    private int id;
    private String backdropPath;
    private String posterPath;
    private String overview;
    private String originalTitle;
    private String releaseDate;
    private String voteAverage;

    private static final String MOVIEDB_POSTER_IMG_URL = "http://image.tmdb.org/t/p/";

    /**
     * Base constructor
     *
     * @param id the integer id of a movie
     * @param backdropPath the string containing the path of the image used as a backdrop
     * @param posterPath the string containing the path of the image used as a poster
     * @param overview the plot of the movie
     * @param originalTitle the original title
     * @param releaseDate a string containing the release date of the movie
     * @param voteAverage a string representing the average vote for the movie
     */
    public Movie(int id, String backdropPath, String posterPath, String overview, String originalTitle,
                 String releaseDate, String voteAverage) {
        this.id = id;
        this.backdropPath = backdropPath;
        this.posterPath = posterPath;
        this.overview = overview;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
    }

    /**
     * Constructor used by the save instance mechanism that handles a Parcel to achieve it
     *
     * @param parcel the object containing the movie data of the object we need to create
     */
    @Ignore
    private Movie(Parcel parcel) {
        id = parcel.readInt();
        backdropPath = parcel.readString();
        posterPath = parcel.readString();
        overview = parcel.readString();
        originalTitle = parcel.readString();
        releaseDate = parcel.readString();
        voteAverage = parcel.readString();
    }

    /**
     * This method returns the complete poster path based on screen size
     *
     * @param context application context
     * @return the path used by the Picasso library to display an image
     */
    @Ignore
    public String buildBackdropPath(Context context) {
        String backdropWidth = context.getResources().getString(R.string.backdrop_size);
        return MOVIEDB_POSTER_IMG_URL + backdropWidth + getBackdropPath();
    }

    /**
     * This method returns the complete poster path based on screen size
     *
     * @param context application context
     * @return the path used by the Picasso library to display an image
     */
    @Ignore
    public String buildPosterPath(Context context) {
        String posterWidth = context.getResources().getString(R.string.poster_size);
        return MOVIEDB_POSTER_IMG_URL + posterWidth + getPosterPath();
    }

    @Override
    @Ignore
    public int describeContents() {
        return 0;
    }

    @Override
    @Ignore
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(backdropPath);
        parcel.writeString(posterPath);
        parcel.writeString(overview);
        parcel.writeString(originalTitle);
        parcel.writeString(releaseDate);
        parcel.writeString(voteAverage);
    }

    @Ignore
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };

    /*
     * Following getter and setter methods for the class properties
     */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }
}
