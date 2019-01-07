package com.sap.cinema;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/movies")
public class MoviesOperationsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String MOVIE_ATTRIBUTE = "movies";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private DataBase database = new DataBaseAccess();
    private Gson gson = new Gson();

    public MoviesOperationsServlet() {
        super();
        database = new DataBaseAccess();
    }

    public MoviesOperationsServlet(DataBase database) {
        this.database = database;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonArray jsonArray;
        try {
            jsonArray = gson.toJsonTree(database.getAllMovies()).getAsJsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(MOVIE_ATTRIBUTE, jsonArray);

            response.setContentType(CONTENT_TYPE);
            response.setCharacterEncoding(CHARACTER_ENCODING);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        PrintWriter writer = response.getWriter();
        writer.println(jsonArray);
        writer.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType(CONTENT_TYPE);

        StringBuilder stringBuilder = new StringBuilder();
        String requestContent;

        while ((requestContent = request.getReader().readLine()) != null) {
            stringBuilder.append(requestContent);
        }

        Movie movieToBeAdded = gson.fromJson(stringBuilder.toString(), Movie.class);

        try {
            database.insertMovieToDatabase(movieToBeAdded);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setHeader("id", Integer.toString(database.getMovieId(movieToBeAdded)));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            database.deleteAllMovies();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
