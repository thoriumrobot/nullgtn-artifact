package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Recipe;

/**
 * Retrieve basic information about a recipe. Use {@link IngredientScanner} to retrieve ingredients.
 */
public interface RecipeScanner {

    /**
     * @param source The source to parse.
     * @return A basic recipe instance with name and source information resolved. No ingredients are
     *     added.
     */
    Recipe parse(String source);
}
