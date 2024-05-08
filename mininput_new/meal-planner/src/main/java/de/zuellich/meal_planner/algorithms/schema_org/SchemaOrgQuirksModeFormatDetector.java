package de.zuellich.meal_planner.algorithms.schema_org;

import de.zuellich.meal_planner.algorithms.FormatDetector;
import de.zuellich.meal_planner.algorithms.RecipeParser;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class SchemaOrgQuirksModeFormatDetector implements FormatDetector {

    private final RecipeParser parser;

    public SchemaOrgQuirksModeFormatDetector(final SchemaOrgQuirksModeParser parser) {
        this.parser = parser;
    }

    @Override
    public int isSupported(final String source) {
        final Document document = Jsoup.parse(source);
        final Elements recipeElement = document.getElementsByAttributeValue("itemtype", "http://schema.org/Recipe");
        final Elements ingredientsElements = document.getElementsByAttributeValue("itemprop", "ingredients");
        if (!recipeElement.isEmpty() && !ingredientsElements.isEmpty()) {
            return ADVANCED_UNDERSTANDING;
        } else {
            return NO_UNDERSTANDING;
        }
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.SCHEMA_ORG_QUIRCKS_MODE;
    }

    @Override
    public RecipeParser getParserInstance() {
        return parser;
    }
}
