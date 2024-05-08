package de.zuellich.meal_planner.algorithms;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Entry point to parsing a source.
 */
@Service
public class RecipeParserFactory {

    private final Set<FormatDetector> formatDetectors;

    private final NullParser defaultParser;

    @Autowired
    public RecipeParserFactory(final Set<FormatDetector> formatDetectors, NullParser defaultParser) {
        this.formatDetectors = formatDetectors;
        this.defaultParser = defaultParser;
    }

    /**
     * Get a parser instance for the provided source.
     *
     * @param source The source that is supplied to the format detector.
     * @return A parser instance that can parse the sauce.
     */
    public RecipeParser getParser(final String source) {
        FormatDetector mostCompatibleFormat = null;
        int highestCompatibility = 0;
        for (final FormatDetector formatDetector : formatDetectors) {
            final int compatibility = formatDetector.isSupported(source);
            if (compatibility > highestCompatibility) {
                mostCompatibleFormat = formatDetector;
                highestCompatibility = compatibility;
            }
        }
        if (mostCompatibleFormat != null) {
            return mostCompatibleFormat.getParserInstance();
        } else {
            return defaultParser;
        }
    }
}
