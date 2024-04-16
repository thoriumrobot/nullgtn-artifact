package de.zuellich.meal_planner.controller;

import de.zuellich.meal_planner.algorithms.RecipeService;
import de.zuellich.meal_planner.datatypes.Recipe;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handles the endpoint for parsing raw recipes.
 */
@RestController
public class Parse {

    private final RecipeService recipeService;

    @Autowired
    public Parse(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @RequestMapping("/parse")
    public ResponseEntity<Object> parse(@RequestParam(value = "url") String url) {
        UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
        if (!urlValidator.isValid(url)) {
            return ResponseEntity.badRequest().build();
        }
        Recipe recipe = recipeService.fromURL(url);
        return ResponseEntity.ok(recipe);
    }
}
