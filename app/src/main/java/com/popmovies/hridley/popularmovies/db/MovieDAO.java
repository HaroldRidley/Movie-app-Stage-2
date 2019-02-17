package com.popmovies.hridley.popularmovies.db;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.popmovies.hridley.popularmovies.models.Movie;

import java.util.List;

@Dao
public interface MovieDAO {
    @Query("SELECT * FROM Movie")
   List<Movie> loadAllMovies();

    @Insert
    void insertMovie(Movie movie);
    @Delete
    void deleteMovie(Movie movie);

    @Query("SELECT * FROM Movie WHERE id = :id")
    Movie loadMovieById(int id);
}
