package com.sap.cinema;

public class Movie {

    private int id;
    private String title;
    private int year;
    private int budget;

    Movie() {
    }

    Movie(int id, String title, int year, int budget) {
        this.setTitle(title);
        this.setYear(year);
        this.setBudget(budget);
    }

    void setId(int id) {
        this.id = id;
    }

    void setTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Can not pass null objects.");
        }

        this.title = title;
    }

    void setYear(int year) {
        if (year < 0) {
            throw new IllegalArgumentException("There were no movies B.C");
        }

        this.year = year;
    }

    void setBudget(int budget) {
        if (budget <= 0) {
            throw new IllegalArgumentException("The budget must be a positive number");
        }

        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public int getBudget() {
        return budget;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Movie other = (Movie) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
