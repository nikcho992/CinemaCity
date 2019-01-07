package com.sap.cinema;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

public class MoviesOperationsServletTest {

    private static final String TEST_MOVIE_BATMAN_JSON = "{\"id\":1,\"title\":\"Batman\",\"year\":2000,\"budget\":123456}";
    private static final String TEST_MOVIE_SUPERMAN_JSON = "{\"id\":2,\"title\":\"Superman\",\"year\":2000,\"budget\":123456}";
    private static final String ALL_MOVIES_JSON = "[{id=1.0, title=Batman, year=2000.0, budget=123456.0}, {id=2.0, title=Superman, year=2000.0, budget=123456.0}]";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static final int BATMAN_ID = 1;

    private static Movie movieBatman;
    private static Movie movieSuperman;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private DataBase database;

    @BeforeClass
    public static void loadValidMovies() {
        Gson gson = new Gson();
        movieBatman = gson.fromJson(TEST_MOVIE_BATMAN_JSON, Movie.class);
        movieSuperman = gson.fromJson(TEST_MOVIE_SUPERMAN_JSON, Movie.class);
    }

    @Before
    public void setUpMockObjects() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        database = mock(DataBase.class);
    }

    @Test
    public void givenEmptyDatabaseWhenHandlingGetRequestThenReturnEmptyArrayOfMovies()
            throws SQLException, IOException, ServletException {
        Set<Movie> noMovies = new HashSet<>();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(database.getAllMovies()).thenReturn(noMovies);
        when(response.getWriter()).thenReturn(printWriter);

        new MoviesOperationsServlet(database).doGet(request, response);

        verify(database, times(1)).getAllMovies();
        verify(response, times(1)).setContentType(CONTENT_TYPE);
        verify(response, times(1)).setCharacterEncoding(CHARACTER_ENCODING);
        assertEquals(new LinkedHashSet<>(), new Gson().fromJson(stringWriter.toString(), Set.class));
        stringWriter.close();
        printWriter.close();
    }

    @Test
    public void givenNonEmptyDatabaseWhenHandlingGetRequestThenReturnTheArrayOfMovies()
            throws SQLException, ServletException, IOException {
        Set<Movie> movies = new HashSet<>();
        movies.add(movieBatman);
        movies.add(movieSuperman);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(database.getAllMovies()).thenReturn(movies);
        when(response.getWriter()).thenReturn(printWriter);

        new MoviesOperationsServlet(database).doGet(request, response);

        verify(database, times(1)).getAllMovies();
        verify(response, times(1)).setContentType(CONTENT_TYPE);
        verify(response, times(1)).setCharacterEncoding(CHARACTER_ENCODING);
        assertEquals(ALL_MOVIES_JSON, new Gson().fromJson(stringWriter.toString(), Set.class).toString());
        stringWriter.close();
        printWriter.close();
    }

    @Test
    public void givenMovieInJsonFormatWhenHandlingPostRequestThenAddThatMovieIntoDatabase()
            throws IOException, ServletException, SQLException {
        StringReader stringReader = new StringReader(TEST_MOVIE_BATMAN_JSON);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        when(request.getReader()).thenReturn(bufferedReader);
        when(database.getMovieId(movieBatman)).thenReturn(BATMAN_ID);

        new MoviesOperationsServlet(database).doPost(request, response);

        verify(database, times(1)).insertMovieToDatabase(movieBatman);
        verify(database, times(1)).getMovieId(movieBatman);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
        verify(response, times(1)).setHeader("id", Integer.toString(BATMAN_ID));
    }

    @Test
    public void givenMoviesDatabaseWhenHandlingDeleteRequestThenDeleteAllMovies()
            throws ServletException, IOException, SQLException {
        new MoviesOperationsServlet(database).doDelete(request, response);

        verify(database, times(1)).deleteAllMovies();
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    }
}
