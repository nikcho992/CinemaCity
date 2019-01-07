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
import com.google.gson.JsonObject;

@WebServlet("/movies/*")
public class SingleMovieOperationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String BY_FORWARD_SLASH = "/";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private Gson gson = new Gson();
    private DataBase database;

    public SingleMovieOperationServlet() {
        super();
        database = new DataBaseAccess();
    }

    public SingleMovieOperationServlet(DataBase database) {
        this.database = database;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int movieId = Integer.parseInt(request.getPathInfo().split(BY_FORWARD_SLASH)[1]);

        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = gson.toJsonTree(database.getMovieById(movieId)).getAsJsonObject();
        } catch (IdNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(CHARACTER_ENCODING);

        PrintWriter writer = response.getWriter();
        writer.println(jsonObject);
        writer.flush();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int movieId = Integer.parseInt(request.getPathInfo().split(BY_FORWARD_SLASH)[1]);
        StringBuilder stringBuilder = new StringBuilder();
        String requestContent;

        while ((requestContent = request.getReader().readLine()) != null) {
            stringBuilder.append(requestContent);
        }
        Movie movieToBeAdded = gson.fromJson(stringBuilder.toString(), Movie.class);

        try {
            database.updateMovieInDatabase(movieToBeAdded, movieId);
            response.setHeader("id", Integer.toString(database.getMovieId(movieToBeAdded)));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IdNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int movieId = Integer.parseInt(request.getPathInfo().split(BY_FORWARD_SLASH)[1]);

        try {
            database.deleteMovieById(movieId);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IdNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }
}
