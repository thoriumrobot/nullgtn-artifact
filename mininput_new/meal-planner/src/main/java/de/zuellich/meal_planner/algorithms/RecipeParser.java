package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.datatypes.RecipeFormat;

/**
 */
public interface RecipeParser {

    /**
     * @param source The source format to parse.
     * @return The parsed recipe instance.
     */
    Recipe parse(String source);

    RecipeFormat getFormat();
}
