package com.sap.cinema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class DataBaseAccess implements DataBase {

    private final String URL = "jdbc:postgresql://127.0.0.1:5432/postgres";
    private final String USERNAME = "postgres";
    private final String PASSWORD = "postgres";
    private final String GET_ATTRIBUTES_BY_ID = "SELECT * FROM movies WHERE id = ?";
    private final String GET_ALL_ATTRIBUTES = "SELECT * FROM movies";
    private final String GET_ID_QUERY = "SELECT id FROM movies WHERE title=? AND year=? AND budget=?";
    private final String INSERT_MOVIE_QUERY = "INSERT INTO movies(title, year, budget) VALUES(?,?,?)";
    private final String UPDATE_MOVIE_QUERY = "UPDATE movies SET title=?, year=?, budget=? WHERE id = ?";
    private final String DELETE_ALL_MOVIES = "DELETE FROM movies";
    private final String DELETE_MOVIE_BY_ID = "DELETE FROM movies WHERE id = ?";
    private final String RESET_ID_COUNTER_QUERY = "ALTER SEQUENCE movies_id_seq RESTART";

    private Set<Movie> movies = new HashSet<>();
    private Connection conn;

    public DataBaseAccess() {
        try {
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            this.movies = this.convertResultSetToMovieObjects(statement.executeQuery(GET_ALL_ATTRIBUTES));
        } catch (SQLException e) {
            e.getMessage();
            return;
        }
    }

    private Set<Movie> convertResultSetToMovieObjects(ResultSet resultSet) throws SQLException {
        this.movies.clear();
        while (resultSet.next()) {
            Movie currentMovie = new Movie();
            currentMovie.setId(resultSet.getInt("id"));
            currentMovie.setTitle(resultSet.getString("title"));
            currentMovie.setYear(resultSet.getInt("year"));
            currentMovie.setBudget(resultSet.getInt("budget"));
            this.movies.add(currentMovie);
        }
        return movies;
    }

    private Movie convertResultSetToSingleMovie(ResultSet resultSet) throws SQLException {
        resultSet.next();
        Movie currentMovie = new Movie();
        currentMovie.setId(resultSet.getInt("id"));
        currentMovie.setTitle(resultSet.getString("title"));
        currentMovie.setYear(resultSet.getInt("year"));
        currentMovie.setBudget(resultSet.getInt("budget"));

        return currentMovie;
    }

    private boolean isIdPresent(int id) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(GET_ATTRIBUTES_BY_ID);
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean isPresent = false;

        while (resultSet.next()) {
            isPresent = true;
        }

        return isPresent;
    }

    public int getMovieId(Movie movie) throws SQLException {

        PreparedStatement preparedStatement = conn.prepareStatement(GET_ID_QUERY);
        preparedStatement.setString(1, movie.getTitle());
        preparedStatement.setInt(2, movie.getYear());
        preparedStatement.setInt(3, movie.getBudget());

        ResultSet resultSet = preparedStatement.executeQuery();

        resultSet.next();
        return resultSet.getInt("id");
    }

    public Movie getMovieById(int id) throws SQLException, IdNotFoundException {
        if (!isIdPresent(id)) {
            throw new IdNotFoundException();
        }

        PreparedStatement preparedStatement = conn.prepareStatement(GET_ATTRIBUTES_BY_ID);
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();

        return this.convertResultSetToSingleMovie(resultSet);
    }

    public Set<Movie> getAllMovies() throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(GET_ALL_ATTRIBUTES);

        return new HashSet<>(convertResultSetToMovieObjects(resultSet));
    }

    public void insertMovieToDatabase(Movie movieToBeAdded) throws SQLException {

        PreparedStatement preparedStatement = conn.prepareStatement(INSERT_MOVIE_QUERY);
        preparedStatement.setString(1, movieToBeAdded.getTitle());
        preparedStatement.setInt(2, movieToBeAdded.getYear());
        preparedStatement.setInt(3, movieToBeAdded.getBudget());
        preparedStatement.executeUpdate();
    }

    public void updateMovieInDatabase(Movie movie, int movieId) throws SQLException, IdNotFoundException {

        if (!isIdPresent(movieId)) {
            throw new IdNotFoundException("The given id is not present in the database");
        } else {
            PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_MOVIE_QUERY);
            preparedStatement.setString(1, movie.getTitle());
            preparedStatement.setInt(2, movie.getYear());
            preparedStatement.setInt(3, movie.getBudget());
            preparedStatement.setInt(4, movieId);

            preparedStatement.executeUpdate();

        }
    }

    public void deleteAllMovies() throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(DELETE_ALL_MOVIES);
        this.movies.clear();
        statement.executeUpdate(RESET_ID_COUNTER_QUERY);
    }

    public void deleteMovieById(int movieId) throws SQLException, IdNotFoundException {
        if (!isIdPresent(movieId)) {
            throw new IdNotFoundException("The given id is not present in the database");
        } else {
            PreparedStatement preparedStatement = conn.prepareStatement(DELETE_MOVIE_BY_ID);
            preparedStatement.setInt(1, movieId);
            preparedStatement.executeUpdate();
        }
    }
}
