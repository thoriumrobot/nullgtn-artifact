package de.zuellich.meal_planner.algorithms.schema_org;

import de.zuellich.meal_planner.algorithms.AmountParser;
import de.zuellich.meal_planner.algorithms.IngredientScanner;
import de.zuellich.meal_planner.algorithms.IngredientUnitLookup;
import de.zuellich.meal_planner.datatypes.Ingredient;
import de.zuellich.meal_planner.datatypes.IngredientUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * Returns a list of ingredients based on XML.
 */
@Component
public class SchemaOrgIngredientScanner implements IngredientScanner {

    private final IngredientUnitLookup ingredientUnitLookup;

    private final AmountParser amountParser;

    /**
     * Create a new instance.
     *
     * @param amountParser Used to parse amounts for the ingredients.
     * @param ingredientUnitLookup Used to lookup the units for an ingredient.
     */
    public SchemaOrgIngredientScanner(final AmountParser amountParser, final IngredientUnitLookup ingredientUnitLookup) {
        this.amountParser = amountParser;
        this.ingredientUnitLookup = ingredientUnitLookup;
    }

    @Override
    public List<Ingredient> parse(final String source) {
        final Document document = Jsoup.parse(source);
        final Elements ingredients = getIngredientElements(document);
        final List<Ingredient> result = new ArrayList<>(ingredients.size());
        for (final Element ingredient : ingredients) {
            result.add(parseIngredient(ingredient));
        }
        return result;
    }

    /**
     * Extract a list of ingredient elements from the given document. This method should be
     * overwritten by other parsers that require a different selector to be used.
     *
     * @param document The document to extract ingredients from.
     * @return An empty Elements if none found.
     */
    protected Elements getIngredientElements(final Document document) {
        return document.getElementsByAttributeValue("itemprop", "recipeIngredient");
    }

    /**
     * Method is called to extract an Ingredient instance from the elements gathered by
     * getIngredientElements.
     *
     * @param ingredient The element representing an element.
     * @return An ingredient instance.
     */
    protected Ingredient parseIngredient(final Element ingredient) {
        final Elements typeElement = ingredient.select(".wprm-recipe-ingredient-name");
        final Elements amountElement = ingredient.select(".wprm-recipe-ingredient-amount");
        final Elements ingredientElement = ingredient.select(".wprm-recipe-ingredient-unit");
        final String type = typeElement.text();
        final float amount = amountParser.parseAmount(amountElement.text());
        final Optional<IngredientUnit> ingredientUnit = ingredientUnitLookup.lookup(ingredientElement.text());
        return new Ingredient(type, amount, ingredientUnit.orElse(IngredientUnit.NULL));
    }

    public IngredientUnitLookup getIngredientUnitLookup() {
        return ingredientUnitLookup;
    }

    public AmountParser getAmountParser() {
        return amountParser;
    }
}
