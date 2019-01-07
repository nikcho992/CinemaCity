package com.sap.cinema;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

public class SingleMovieOperationServletTest {

    private static final String TEST_MOVIE_JSON = "{\"id\":1,\"title\":\"Random\",\"year\":2000,\"budget\":123456}";
    private static final String TEST_INVALID_MOVIE_JSON = "{\"id\":-12345,\"title\":\"Random\",\"year\":2000,\"budget\":123456}";
    private static final int VALID_TEST_MOVIE_ID = 1;
    private static final int INVALID_TEST_MOVIE_ID = -12345;

    private static Movie validMovie;
    private static Movie invalidMovie;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private DataBase database;

    @BeforeClass
    public static void loadValidMovie() {
        Gson gson = new Gson();
        validMovie = gson.fromJson(TEST_MOVIE_JSON, Movie.class);
        invalidMovie = gson.fromJson(TEST_INVALID_MOVIE_JSON, Movie.class);
    }

    @Before
    public void setUpMockObjects() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        database = mock(DataBase.class);
    }

    @Test
    public void givenValidMovieIdWhenHandlingGetRequestThenReturnThatMovie()
            throws SQLException, IdNotFoundException, IOException, ServletException {
        when(database.getMovieById(VALID_TEST_MOVIE_ID)).thenReturn(validMovie);
        when(request.getPathInfo()).thenReturn("/" + VALID_TEST_MOVIE_ID);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        new SingleMovieOperationServlet(database).doGet(request, response);

        verify(database, times(1)).getMovieById(VALID_TEST_MOVIE_ID);
        verify(response, times(1)).getWriter();
        assertEquals(validMovie, new Gson().fromJson(stringWriter.toString(), Movie.class));
        stringWriter.close();
        printWriter.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenInvalidMovieIdWhenHandlingGetRequestThenSetResponseStatusCodeToNotFound()
            throws SQLException, IdNotFoundException, ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/" + INVALID_TEST_MOVIE_ID);
        when(database.getMovieById(INVALID_TEST_MOVIE_ID)).thenThrow(IdNotFoundException.class);

        new SingleMovieOperationServlet(database).doGet(request, response);

        verify(database, times(1)).getMovieById(INVALID_TEST_MOVIE_ID);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void givenValidMovieIdWhenHandlingDeleteRequestThenDeleteThatMovie()
            throws ServletException, IOException, SQLException, IdNotFoundException {
        when(request.getPathInfo()).thenReturn("/" + VALID_TEST_MOVIE_ID);

        new SingleMovieOperationServlet(database).doDelete(request, response);

        verify(database, times(1)).deleteMovieById(VALID_TEST_MOVIE_ID);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void givenInvalidMovieIdWhenHandlingDeleteRequestThenSetResponseStatusCodeToNotFound()
            throws IOException, SQLException, IdNotFoundException, ServletException {
        when(request.getPathInfo()).thenReturn("/" + INVALID_TEST_MOVIE_ID);
        doThrow(IdNotFoundException.class).when(database).deleteMovieById(INVALID_TEST_MOVIE_ID);

        new SingleMovieOperationServlet(database).doDelete(request, response);

        verify(database, times(1)).deleteMovieById(INVALID_TEST_MOVIE_ID);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void givenValidMovieIdWhenHandlingPutRequestThenUpdateThatMovie()
            throws ServletException, IOException, SQLException, IdNotFoundException {
        StringReader stringReader = new StringReader(TEST_MOVIE_JSON);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        when(request.getPathInfo()).thenReturn("/" + VALID_TEST_MOVIE_ID);
        when(request.getReader()).thenReturn(bufferedReader);
        when(database.getMovieId(validMovie)).thenReturn(VALID_TEST_MOVIE_ID);

        new SingleMovieOperationServlet(database).doPut(request, response);

        verify(database, times(1)).getMovieId(validMovie);
        verify(database, times(1)).updateMovieInDatabase(validMovie, VALID_TEST_MOVIE_ID);
        verify(response, times(1)).setHeader("id", Integer.toString(VALID_TEST_MOVIE_ID));
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
        stringReader.close();
        bufferedReader.close();
    }

    @Test
    public void givenInvalidMovieIdWhenHandlingPutRequestThenSetResponseStatusCodeToNotFound()
            throws SQLException, IOException, IdNotFoundException, ServletException {
        StringReader stringReader = new StringReader(TEST_INVALID_MOVIE_JSON);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        when(request.getPathInfo()).thenReturn("/" + INVALID_TEST_MOVIE_ID);
        when(request.getReader()).thenReturn(bufferedReader);
        doThrow(IdNotFoundException.class).when(database).updateMovieInDatabase(invalidMovie, INVALID_TEST_MOVIE_ID);

        new SingleMovieOperationServlet(database).doPut(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
        stringReader.close();
        bufferedReader.close();
    }
}
