package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Recipe;
import java.util.Collections;
import javax.annotation.Nullable;

/**
 */
public class NullRecipe extends Recipe {

    public NullRecipe() {
        super("", Collections.emptyList(), "");
    }
}
