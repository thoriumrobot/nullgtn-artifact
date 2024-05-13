package de.zuellich.meal_planner.algorithms.schema_org;

import de.zuellich.meal_planner.algorithms.FormatDetector;
import de.zuellich.meal_planner.algorithms.RecipeParser;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class SchemaOrgFormatDetector implements FormatDetector {

    private final RecipeParser parser;

    @Autowired
    public SchemaOrgFormatDetector(@Qualifier("schemaOrgParser") final SchemaOrgParser parser) {
        this.parser = parser;
    }

    @Override
    public int isSupported(final String htmlSource) {
        final Document document = Jsoup.parse(htmlSource);
        if (canFindSchemaOrgAnnotation(document)) {
            return BASIC_UNDERSTANDING;
        } else {
            return NO_UNDERSTANDING;
        }
    }

    /**
     * Try to find the schema.org recipe annotation.
     *
     * @param document The recipes as jsoup document.
     * @return True if we found the schema.org recipe annotation.
     */
    private boolean canFindSchemaOrgAnnotation(final Document document) {
        final Elements recipeElement = document.getElementsByAttributeValue("itemtype", "http://schema.org/Recipe");
        final Elements ingredientsElement = document.getElementsByAttributeValue("itemprop", "recipeIngredient");
        return !recipeElement.isEmpty() && !ingredientsElement.isEmpty();
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.SCHEMA_ORG;
    }

    @Override
    public RecipeParser getParserInstance() {
        return parser;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SchemaOrgFormatDetector that = (SchemaOrgFormatDetector) o;
        return Objects.equals(parser, that.parser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parser);
    }
}
