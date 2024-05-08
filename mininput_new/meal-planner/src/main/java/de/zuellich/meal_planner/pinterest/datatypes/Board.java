package de.zuellich.meal_planner.pinterest.datatypes;

import java.util.Objects;

/**
 */
public class Board {

    private String id = "";

    private String name = "";

    private String url = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Board board = (Board) o;
        return Objects.equals(getId(), board.getId()) && Objects.equals(getName(), board.getName()) && Objects.equals(getUrl(), board.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getUrl());
    }

    @Override
    public String toString() {
        return "Board{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", url='" + url + '\'' + '}';
    }
}
