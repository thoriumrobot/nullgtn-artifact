package de.zuellich.meal_planner.algorithms;

import com.google.common.collect.ImmutableList;
import de.zuellich.meal_planner.datatypes.Ingredient;
import de.zuellich.meal_planner.datatypes.IngredientUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of @{@link IngredientScanner} for plain text without any further markup.
 */
@Service
public class PlainTextIngredientScanner implements IngredientScanner {

    private final AmountParser amountParser;

    private final IngredientUnitLookup ingredientUnitLookup;

    @Autowired
    public PlainTextIngredientScanner(final AmountParser amountParser, final IngredientUnitLookup ingredientUnitLookup) {
        this.amountParser = amountParser;
        this.ingredientUnitLookup = ingredientUnitLookup;
    }

    /**
     * @param source Expects only one ingredient "description" to be supplied.
     * @return Contains the parsed ingredient or an empty list of none found.
     */
    @Override
    public List<Ingredient> parse(final String source) {
        // https://stackoverflow.com/a/8910767
        final int numberOfWhitespaces = source.length() - source.replace(" ", "").length();
        if (numberOfWhitespaces < 2) {
            return ImmutableList.of(new Ingredient(source, 0, IngredientUnit.NULL));
        }
        int start = 0;
        int end = 0;
        for (; end < source.length(); end++) {
            final Optional<Integer> optionalInteger = parseInt(source.charAt(end));
            if (source.charAt(end) == ' ' && !optionalInteger.isPresent()) {
                break;
            }
        }
        final String amount = source.substring(start, end).trim();
        final float parseAmount = amountParser.parseAmount(amount);
        start = end;
        do {
            end++;
        } while (source.charAt(end) != ' ');
        final String unit = source.substring(start, end).trim();
        final Optional<IngredientUnit> ingredientUnit = ingredientUnitLookup.lookup(unit);
        final int finalStart = start;
        final int finalEnd = end;
        final String ingredientName = ingredientUnit.map((notused) -> source.substring(finalEnd)).orElse(source.substring(finalStart)).trim();
        final Ingredient result = new Ingredient(ingredientName, parseAmount, ingredientUnit.orElse(IngredientUnit.NULL));
        return ImmutableList.of(result);
    }

    /**
     * Try to parse an integer from the given character.
     */
    private Optional<Integer> parseInt(final char c) {
        if (c >= 48 && c <= 57) {
            return Optional.of(Integer.parseInt(String.valueOf(c)));
        } else {
            return Optional.empty();
        }
    }
}
