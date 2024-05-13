package de.zuellich.meal_planner.algorithms;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class AmountParser {

    private static final List<String> UNICODE_FRACTIONS = Arrays.asList("½", "¼", "¾");

    /**
     * Stores values for basic fractions like 1/4.
     */
    private static final ImmutableMap<String, Float> fractionLookup;

    static {
        fractionLookup = ImmutableMap.<String, Float>builderWithExpectedSize(7).put("1/8", 0.125f).put("1/4", 0.25f).put("¼", 0.25f).put("1/2", 0.5f).put("½", 0.5f).put("3/4", 0.75f).put("¾", 0.75f).build();
    }

    public static final String CONTAINS_DIGITS_PATTERN = "\\d\\s?[½¼¾]";

    public static final String IS_RANGE_PATTERN = "(\\d+).?-.?\\d+";

    /**
     * Convert the string value into a float value. Can resolve very basic fractions. If the fraction
     * is unknown it will return 0.
     *
     * @param raw The raw value. Might be "1" or "1/8".
     * @return Returns the converted value or in case of an unknown fraction, empty string or null 0.
     */
    public float parseAmount(final String raw) {
        if (raw == null || raw.isEmpty()) {
            return 0;
        }
        final boolean isRange = isRange(raw);
        if (isRange) {
            return resolveRange(raw);
        }
        final boolean containsUnicodeFractions = containsUnicodeFractions(raw);
        if (raw.contains("/") || containsUnicodeFractions) {
            return resolveFraction(raw);
        } else {
            return gracefullyParseFloat(raw);
        }
    }

    private float resolveRange(final String raw) {
        final Matcher group = Pattern.compile(IS_RANGE_PATTERN).matcher(raw);
        if (group.find()) {
            return parseAmount(group.group(1));
        }
        return 0;
    }

    private boolean isRange(final String raw) {
        return Pattern.compile(IS_RANGE_PATTERN).matcher(raw).matches();
    }

    /**
     * Parse the float without raising exceptions due to format problems.
     *
     * @param raw The string to parse as float.
     * @return In case the number format is unexpected 0 is returned.
     */
    private float gracefullyParseFloat(final String raw) {
        try {
            return Float.parseFloat(raw);
        } catch (final NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Check if the given string contains unicode fractions.
     *
     * @param raw The string to check.
     * @return True if there are unicode fractions, false if not.
     */
    private boolean containsUnicodeFractions(final String raw) {
        for (final String unicodeSymbol : UNICODE_FRACTIONS) {
            final boolean containsUnicode = raw.contains(unicodeSymbol);
            if (containsUnicode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve a fraction by looking it up in the static storage. Returns 0 if not found.
     *
     * @param raw The raw string like 1/8.
     * @return The value of the fraction or 0.
     */
    private float resolveFraction(final String raw) {
        final boolean isMixedFraction = raw.matches(CONTAINS_DIGITS_PATTERN);
        if (!isMixedFraction) {
            return parsePrimitiveFraction(raw);
        }
        return parseMixedUnicodeFraction(raw);
    }

    /**
     * Try to convert the given primitive fraction, e.g. "1/2" to a float value.
     *
     * @param raw The raw string to parse.
     * @return 0 if no fraction representation could be found.
     */
    private float parsePrimitiveFraction(final String raw) {
        final Float value = fractionLookup.get(raw);
        if (value == null) {
            return 0f;
        } else {
            return value;
        }
    }

    /**
     * Try and parse the string containing a mixed unicode fraction.
     *
     * @param raw The raw string to parse.
     * @return The float that was calculated.
     */
    private float parseMixedUnicodeFraction(final String raw) {
        String withoutUnicodeFraction = "0";
        String usedFraction = "";
        for (final String unicodeFraction : UNICODE_FRACTIONS) {
            if (raw.contains(unicodeFraction)) {
                withoutUnicodeFraction = raw.replace(unicodeFraction, "");
                usedFraction = unicodeFraction;
                break;
            }
        }
        return Float.parseFloat(withoutUnicodeFraction) + fractionLookup.getOrDefault(usedFraction, 0f);
    }
}
