package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.Recipe;
import de.zuellich.meal_planner.exception.RecipeParseException;
import java.io.IOException;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class RecipeService {

    private final RecipeParserFactory parserFactory;

    private final RecipeFetcherService fetcherService;

    public RecipeService(RecipeParserFactory parserFactory, RecipeFetcherService fetcherService) {
        this.parserFactory = parserFactory;
        this.fetcherService = fetcherService;
    }

    /**
     * Try to fetch the recipe from the URL and create a response.
     *
     * @param url The URL to fetch the recipe from.
     * @return A response that either is the recipe or an error.
     */
    public Recipe fromURL(String url) {
        String recipeSource = null;
        try {
            recipeSource = fetcherService.fetchByURL(url);
        } catch (IOException e) {
            throw new RecipeParseException("Error fetching recipe.", e);
        }
        if (recipeSource.isEmpty()) {
            throw new RecipeParseException("Recipe source is empty. Error downloading?");
        }
        RecipeParser parser = parserFactory.getParser(recipeSource);
        return parser.parse(recipeSource);
    }
}
