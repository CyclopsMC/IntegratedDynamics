package org.cyclops.integrateddynamics.api.network;

import java.util.function.Predicate;

/**
 * A data object for filter predicates for ingredient networks with their settings.
 * @author rubensworks
 */
public class PositionedAddonsNetworkIngredientsFilter<T> {

    private final Predicate<T> filter;
    private final boolean applyToInsertions;
    private final boolean applyToExtractions;
    private final boolean allowAllIfFilterNotApplied;

    public PositionedAddonsNetworkIngredientsFilter(Predicate<T> filter, boolean applyToInsertions,
                                                    boolean applyToExtractions, boolean allowAllIfFilterNotApplied) {
        this.filter = filter;
        this.applyToInsertions = applyToInsertions;
        this.applyToExtractions = applyToExtractions;
        this.allowAllIfFilterNotApplied = allowAllIfFilterNotApplied;
    }

    /**
     * Test the given ingredient for an insertion operation.
     * @param ingredient An ingredient to test.
     * @return If the operation is allowed.
     */
    public boolean testInsertion(T ingredient) {
        if (this.isApplyToInsertions()) {
            return getFilter().test(ingredient);
        } else {
            return this.isAllowAllIfFilterNotApplied();
        }
    }

    /**
     * Test the given ingredient for an insertion operation.
     * @param ingredient An ingredient to test.
     * @return If the operation is allowed.
     */
    public boolean testExtraction(T ingredient) {
        if (this.isApplyToExtractions()) {
            return getFilter().test(ingredient);
        } else {
            return this.isAllowAllIfFilterNotApplied();
        }
    }

    /**
     * Test the given ingredient for read-only and iteration operations.
     * @param ingredient An ingredient to test.
     * @return If the operation is allowed.
     */
    public boolean testView(T ingredient) {
        return this.testExtraction(ingredient);
    }

    /**
     * @return The ingredient filter
     */
    public Predicate<T> getFilter() {
        return filter;
    }

    /**
     * @return If the ingredient filter should be used for insertions.
     */
    public boolean isApplyToInsertions() {
        return applyToInsertions;
    }

    /**
     * @return If the ingredient filter should be used for extractions.
     */
    public boolean isApplyToExtractions() {
        return applyToExtractions;
    }

    /**
     * @return Determines what to do if insertion or extraction application is disabled.
     *         If this method returns true, then all insertions and/or extractions will be allowed when their respective application is disabled.
     *         If this method returns false, then no insertions and/or extractions will be allowed when their respective application is disabled.
     */
    public boolean isAllowAllIfFilterNotApplied() {
        return allowAllIfFilterNotApplied;
    }
}
