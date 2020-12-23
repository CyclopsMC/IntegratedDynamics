package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

/**
 * @author rubensworks
 */
public class Asserts {

    public static void check(final boolean expression, final String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

}
