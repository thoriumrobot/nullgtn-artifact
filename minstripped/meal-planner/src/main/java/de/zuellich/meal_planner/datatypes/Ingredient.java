package de.zuellich.meal_planner.datatypes;

import java.util.Objects;

/**
 * Represents an ingredient.
 */
public class Ingredient {

    private IngredientUnit unit;

    private String type;

    private float amount;

    public Ingredient(String type, float amount, IngredientUnit unit) {
        this.type = type;
        this.amount = amount;
        this.unit = unit;
    }

    public IngredientUnit getUnit() {
        return unit;
    }

    public String getType() {
        return type;
    }

    public float getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Ingredient that = (Ingredient) o;
        return Float.compare(that.getAmount(), getAmount()) == 0 && getUnit() == that.getUnit() && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUnit(), getType(), getAmount());
    }

    @Override
    public String toString() {
        return "Ingredient{" + "unit=" + unit + ", type='" + type + '\'' + ", amount=" + amount + '}';
    }
}
