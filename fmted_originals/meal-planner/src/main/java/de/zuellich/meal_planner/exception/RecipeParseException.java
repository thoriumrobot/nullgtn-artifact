package de.zuellich.meal_planner.exception;

/**
 */
public class RecipeParseException extends RuntimeException {

    public RecipeParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipeParseException(String message) {
        super(message);
    }
}
