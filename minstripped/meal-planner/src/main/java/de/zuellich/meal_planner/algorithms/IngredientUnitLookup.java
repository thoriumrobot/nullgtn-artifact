package de.zuellich.meal_planner.algorithms;

import de.zuellich.meal_planner.datatypes.IngredientUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class IngredientUnitLookup {

    private static IngredientUnitLookup instance = null;

    private Map<String, IngredientUnit> byShorthand;

    private Map<String, IngredientUnit> byPlural;

    private Map<String, IngredientUnit> bySingular;

    /**
     * Create a new instance and setup the lookup table.
     */
    private IngredientUnitLookup() {
        this.setupLookupTable();
    }

    /**
     * Get an instance.
     *
     * @return The new instance.
     */
    public static IngredientUnitLookup getInstance() {
        if (IngredientUnitLookup.instance == null) {
            IngredientUnitLookup.instance = new IngredientUnitLookup();
        }
        return IngredientUnitLookup.instance;
    }

    /**
     * Setup the maps with their values.
     */
    private void setupLookupTable() {
        this.byShorthand = new HashMap<>(IngredientUnit.values().length);
        this.byPlural = new HashMap<>(IngredientUnit.values().length);
        this.bySingular = new HashMap<>(IngredientUnit.values().length);
        for (final IngredientUnit unit : IngredientUnit.values()) {
            this.byShorthand.put(unit.getShorthand(), unit);
            this.byPlural.put(unit.getPlural(), unit);
            this.bySingular.put(unit.getSingular(), unit);
        }
    }

    /**
     * Try to find the unit type by its shorthand.
     *
     * @param shorthand The shorthand to lookup.
     * @return IngredientUnit.NULL if not found.
     */
    public Optional<IngredientUnit> byShorthand(final String shorthand) {
        return Optional.ofNullable(byShorthand.get(shorthand));
    }

    /**
     * Try to find the unit type by its plural form.
     *
     * @param plural The string that supposedly is plural.
     * @return IngredientUnit.NULL if not found.
     */
    public Optional<IngredientUnit> byPlural(final String plural) {
        return Optional.ofNullable(byPlural.get(plural));
    }

    /**
     * Try to find an unit by looking up search string in all search maps.
     *
     * @param search The string to search. Can be shorthand or plural.
     * @return IngredientUnit.NULL if not found.
     */
    public Optional<IngredientUnit> lookup(final String search) {
        if (byShorthand(search).isPresent()) {
            return byShorthand(search);
        }
        if (byPlural(search).isPresent()) {
            return byPlural(search);
        }
        return bySingular(search);
    }

    /**
     * Find an ingredient unit by its singular representation.
     *
     * @param search The search string that should be the singular representation of the unit you look
     *     for.
     * @return IngredientUnit.NULL if none found.
     */
    public Optional<IngredientUnit> bySingular(final String search) {
        return Optional.ofNullable(bySingular.get(search));
    }
}
