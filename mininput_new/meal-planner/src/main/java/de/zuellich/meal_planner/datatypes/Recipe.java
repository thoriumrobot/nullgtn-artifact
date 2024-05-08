package de.zuellich.meal_planner.datatypes;

import java.util.List;
import java.util.Objects;

/**
 * Class to represent a recipe.
 */
public class Recipe {

    /**
     * The name of this recipe.
     */
    private String name;

    /**
     * The list of ingredients for this recipe.
     */
    private List<Ingredient> ingredients;

    /**
     * Represents the source of this recipe. Might be a URL or a simple text.
     */
    private String source;

    public Recipe(String name, List<Ingredient> ingredients, String source) {
        this.name = name;
        this.ingredients = ingredients;
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(getName(), recipe.getName()) && Objects.equals(getIngredients(), recipe.getIngredients()) && Objects.equals(getSource(), recipe.getSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getIngredients(), getSource());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Recipe{" + "name='" + name + '\'' + ", ingredients=" + ingredients + ", source='" + source + '\'' + '}';
    }
}
