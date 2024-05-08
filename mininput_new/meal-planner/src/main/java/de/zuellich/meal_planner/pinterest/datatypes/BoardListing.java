package de.zuellich.meal_planner.pinterest.datatypes;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Represents the listing of a board. Includes the board with its information and the list of pins
 * included.
 */
public class BoardListing {

    private Board board = new Board();

    private List<Pin> pins = ImmutableList.of();

    public Board getBoard() {
        return this.board;
    }

    public void setBoard(final Board board) {
        this.board = board;
    }

    public List<Pin> getPins() {
        return this.pins;
    }

    public void setPins(final List<Pin> pins) {
        this.pins = pins;
    }
}
