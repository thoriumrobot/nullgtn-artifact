package de.zuellich.meal_planner.algorithms.schema_org;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.collect.ImmutableList;
import de.zuellich.meal_planner.algorithms.PlainTextIngredientScanner;
import de.zuellich.meal_planner.algorithms.RecipeParser;
import de.zuellich.meal_planner.datatypes.Ingredient;
import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.datatypes.RecipeFormat;
import java.io.IOException;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class SchemaOrgJSONLDParser implements RecipeParser {

    private final PlainTextIngredientScanner ingredientScanner;

    @Autowired
    public SchemaOrgJSONLDParser(final PlainTextIngredientScanner ingredientScanner) {
        this.ingredientScanner = ingredientScanner;
    }

    @Override
    public Recipe parse(final String source) {
        try {
            final JsonNode flatten = getRecipeElementAndExtractData(source);
            final String name = this.getRecipeName(flatten);
            final ImmutableList<Ingredient> ingredients = getIngredients(flatten);
            return new Recipe(name, ingredients, "");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ImmutableList<Ingredient> getIngredients(final JsonNode data) {
        if (!data.has("recipeIngredient")) {
            return ImmutableList.of();
        }
        final JsonNode recipeIngredients = data.get("recipeIngredient");
        final ImmutableList.Builder<Ingredient> builder = ImmutableList.builder();
        final Iterator<JsonNode> elements = recipeIngredients.elements();
        for (; elements.hasNext(); ) {
            final JsonNode element = elements.next();
            builder.add(ingredientScanner.parse(element.textValue().trim()).get(0));
        }
        return builder.build();
    }

    private JsonNode getRecipeElementAndExtractData(final String source) throws IOException {
        final Elements type = Jsoup.parse(source).getElementsByAttributeValue("type", "application/ld+json");
        final ObjectMapper mapper = new ObjectMapper();
        for (final Element possibleRecipe : type) {
            final JsonNode jsonNode = mapper.readTree(possibleRecipe.html());
            final JsonNode typeAnnotation = jsonNode.get("@type");
            if (typeAnnotation != null && "Recipe".equals(typeAnnotation.textValue())) {
                return jsonNode;
            }
        }
        return NullNode.getInstance();
    }

    private String getRecipeName(final JsonNode jsonData) {
        if (!jsonData.has("name")) {
            return "";
        }
        final String recipeName = jsonData.get("name").textValue();
        if (!recipeName.isEmpty()) {
            return recipeName;
        }
        return "";
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.SCHEMA_ORG_JSON_LD;
    }
}
