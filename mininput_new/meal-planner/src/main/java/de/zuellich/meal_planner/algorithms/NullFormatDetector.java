package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.RecipeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class NullFormatDetector implements FormatDetector {

    private final NullParser parser;

    @Autowired
    public NullFormatDetector(final NullParser parser) {
        this.parser = parser;
    }

    @Override
    public int isSupported(final String source) {
        return 1;
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.NULL;
    }

    @Override
    public RecipeParser getParserInstance() {
        return parser;
    }
}
