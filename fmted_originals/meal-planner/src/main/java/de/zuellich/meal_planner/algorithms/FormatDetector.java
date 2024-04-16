package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.RecipeFormat;

/**
 * Interface that describes a type to detect a recipe format.
 */
public interface FormatDetector {

    int NO_UNDERSTANDING = 0;

    int BASIC_UNDERSTANDING = 50;

    int ADVANCED_UNDERSTANDING = 75;

    /**
     * Try to detect a format.
     *
     * @return An integer indicating the level of what the detector believes the format is. 0 is no
     *     understanding, while 100 is a perfect understanding.
     */
    int isSupported(String source);

    /**
     * Return the format supported by this detector.
     *
     * @return A recipe format.
     */
    RecipeFormat getFormat();

    /**
     * @return Returns an instance of RecipeParser that can parse the provided source.
     */
    RecipeParser getParserInstance();
}
