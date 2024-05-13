package de.zuellich.meal_planner.algorithms.schema_org;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zuellich.meal_planner.algorithms.*;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class SchemaOrgJSONLDFormatDetector implements FormatDetector {

    private final SchemaOrgJSONLDParser parser;

    @Autowired
    public SchemaOrgJSONLDFormatDetector(final SchemaOrgJSONLDParser parser) {
        this.parser = parser;
    }

    @Override
    public int isSupported(final String source) {
        final Document parse = Jsoup.parse(source);
        final Elements elementsByAttribute = parse.getElementsByAttributeValue("type", "application/ld+json");
        final boolean containsRecipeData = elementsByAttribute.size() >= 1 && lookForRecipeInformation(elementsByAttribute);
        if (containsRecipeData) {
            return ADVANCED_UNDERSTANDING;
        }
        return NO_UNDERSTANDING;
    }

    private boolean lookForRecipeInformation(final Elements elements) {
        final ObjectMapper mapper = new ObjectMapper();
        for (final Element possibleRecipe : elements) {
            try {
                final String html = possibleRecipe.html();
                final JsonNode jsonNode = mapper.readTree(html);
                final JsonNode typeAnnotation = jsonNode.get("@type");
                if (typeAnnotation != null && typeAnnotation.textValue().equals("Recipe")) {
                    return true;
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.SCHEMA_ORG_JSON_LD;
    }

    @Override
    public RecipeParser getParserInstance() {
        return parser;
    }
}
