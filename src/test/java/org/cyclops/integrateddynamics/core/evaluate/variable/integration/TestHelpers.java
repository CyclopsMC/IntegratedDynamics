package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import org.apache.http.util.Asserts;

import java.util.Objects;

/**
 * Helpers for tests
 * @author rubensworks
 */
public class TestHelpers {

    /**
     * Assertion for equal objects.
     * @param actual Actual value.
     * @param expected Expected value.
     * @param ifEqual Message identifying the assertion.
     * @param <T> The type.
     */
    public static <T> void assertEqual(T actual, T expected, String ifEqual) {
        try {
            if(actual instanceof Double) {
                Asserts.check(((Double) actual - (Double) expected) < 0.0001D, ifEqual);
            } else if(actual instanceof Float) {
                Asserts.check(((Float) actual - (Float) expected) < 0.0001F, ifEqual);
            } else {
                Asserts.check(Objects.equals(actual, expected), ifEqual);
            }
        } catch (IllegalStateException e) {
            throw new AssertionError(String.format("Failure: %s. Expected %s, but got %s.", ifEqual, expected, actual));
        }
    }

    /**
     * Assertion for non equal objects.
     * @param actual Actual value.
     * @param expected Expected value.
     * @param ifNonEqual Message identifying the assertion.
     * @param <T> The type.
     */
    public static <T> void assertNonEqual(T actual, T expected, String ifNonEqual) {
        try {
            if(actual instanceof Double) {
                Asserts.check(((Double) actual - (Double) expected) >= 0.0001D, ifNonEqual);
            } else if(actual instanceof Float) {
                Asserts.check(((Float) actual - (Float) expected) >= 0.0001F, ifNonEqual);
            } else {
                Asserts.check(!Objects.equals(actual, expected), ifNonEqual);
            }
        } catch (IllegalStateException e) {
            throw new AssertionError(String.format("Failure: %s. Expected not %s, but got %s.", ifNonEqual, expected, actual));
        }
    }

    /**
     * Assertion for null objects.
     * @param actual Actual value.
     * @param ifNull Message identifying the assertion.
     * @param <T> The type.
     */
    public static <T> void assertNull(T actual, String ifNull) {
        try {
            Asserts.check(actual == null, ifNull);
        } catch (IllegalStateException e) {
            throw new AssertionError(String.format("Failure: %s. Expected to be null, but got %s.", ifNull, actual));
        }
    }

    /**
     * Assertion for non null objects.
     * @param actual Actual value.
     * @param ifNonNull Message identifying the assertion.
     * @param <T> The type.
     */
    public static <T> void assertNonNull(T actual, String ifNonNull) {
        try {
            Asserts.check(actual != null, ifNonNull);
        } catch (IllegalStateException e) {
            throw new AssertionError(String.format("Failure: %s. Expected to be non null, but got %s.", ifNonNull, actual));
        }
    }

}
