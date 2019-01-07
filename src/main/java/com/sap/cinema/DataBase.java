package com.sap.cinema;

import java.sql.SQLException;
import java.util.Set;

public interface DataBase {
    int getMovieId(Movie movie) throws SQLException;

    Movie getMovieById(int id) throws SQLException, IdNotFoundException;

    Set<Movie> getAllMovies() throws SQLException;

    void insertMovieToDatabase(Movie movieToBeAdded) throws SQLException;

    void updateMovieInDatabase(Movie movie, int movieId) throws SQLException, IdNotFoundException;

    void deleteAllMovies() throws SQLException;

    void deleteMovieById(int movieId) throws SQLException, IdNotFoundException;
}
