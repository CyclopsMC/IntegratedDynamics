package org.cyclops.integrateddynamics.core.test;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.command.CommandTest;
import org.cyclops.integrateddynamics.gametest.integration.Asserts;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helpers for tests
 * @author rubensworks
 */
public class TestHelpers {

    public static boolean canRunIntegrationTests() {
        try {
            Class.forName(CommandTest.CLASSES.get(0));
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

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

    public static <T> CompoundTag serialize(Consumer<ValueOutput> deserializer) {
        return serialize(deserializer, ValueDeseralizationContext.ofAllEnabled().holderLookupProvider());
    }

    public static <T> CompoundTag serialize(Consumer<ValueOutput> deserializer, HolderLookup.Provider holderLookup) {
        TagValueOutput valueOutput = TagValueOutput.createWithContext(new ProblemReporter() {
            @Override
            public ProblemReporter forChild(PathElement p_421613_) {
                return this;
            }

            @Override
            public void report(Problem p_422137_) {

            }
        }, holderLookup);
        deserializer.accept(valueOutput);
        return valueOutput.buildResult();
    }

    public static <T> T deserialize(CompoundTag tag, Function<ValueInput, T> serializer) {
        return deserialize(tag, serializer, ValueDeseralizationContext.ofAllEnabled().holderLookupProvider());
    }

    public static <T> T deserialize(CompoundTag tag, Function<ValueInput, T> serializer, HolderLookup.Provider holderLookup) {
        ValueInput valueInput = TagValueInput.create(new ProblemReporter() {
            @Override
            public ProblemReporter forChild(PathElement p_421613_) {
                return this;
            }

            @Override
            public void report(Problem p_422137_) {

            }
        }, holderLookup, tag);
        return serializer.apply(valueInput);
    }

}
