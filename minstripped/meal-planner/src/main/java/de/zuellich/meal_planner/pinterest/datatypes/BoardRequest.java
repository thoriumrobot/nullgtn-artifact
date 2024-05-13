package de.zuellich.meal_planner.pinterest.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A helper POJO to handle the board result wrapped in data. Jackson configuration doesn't seem so
 * simple...
 */
public class BoardRequest {

    @JsonProperty(value = "data")
    private Board board = new Board();

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}
