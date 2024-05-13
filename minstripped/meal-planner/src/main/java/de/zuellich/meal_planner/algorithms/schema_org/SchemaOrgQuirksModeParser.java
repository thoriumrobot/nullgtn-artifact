package de.zuellich.meal_planner.algorithms.schema_org;

import de.zuellich.meal_planner.datatypes.RecipeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class SchemaOrgQuirksModeParser extends SchemaOrgParser {

    @Autowired
    public SchemaOrgQuirksModeParser(SchemaOrgRecipeScanner recipeScanner, SchemaOrgQuirksModeIngredientScanner ingredientScanner) {
        super(recipeScanner, ingredientScanner);
    }

    @Override
    public RecipeFormat getFormat() {
        return RecipeFormat.SCHEMA_ORG_QUIRCKS_MODE;
    }
}
