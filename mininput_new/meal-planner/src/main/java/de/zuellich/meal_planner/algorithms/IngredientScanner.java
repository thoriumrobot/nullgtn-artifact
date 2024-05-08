package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Ingredient;
import java.util.List;

/**
 * Scans a list of ingredients.
 */
public interface IngredientScanner {

    /**
     * Get a list of ingredients from the specified source.
     *
     * @return The list of ingredients parsed or an empty list if none found.
     */
    List<Ingredient> parse(String source);
}
