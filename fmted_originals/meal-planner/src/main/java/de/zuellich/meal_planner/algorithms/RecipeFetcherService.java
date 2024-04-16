package de.zuellich.meal_planner.algorithms;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

/**
 * Implements methods to fetch a recipe.
 */
@Service
public class RecipeFetcherService {

    public String fetchByURL(String url) throws IOException {
        return Jsoup.connect(url).get().html();
    }
}
