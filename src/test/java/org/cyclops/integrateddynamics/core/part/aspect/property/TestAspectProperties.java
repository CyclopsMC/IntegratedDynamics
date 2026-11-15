package org.cyclops.integrateddynamics.core.part.aspect.property;

import com.google.common.collect.ImmutableList;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rubensworks
 */
public class TestAspectProperties {

    private static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_POSITIVE =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "test.positive", input -> input.getRawValue() >= 0);
    private static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_ANY =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "test.any");

    private AspectProperties defaultProperties;

    @Before
    public void beforeEach() {
        defaultProperties = new AspectProperties(ImmutableList.of(PROP_POSITIVE, PROP_ANY));
        defaultProperties.setValue(PROP_POSITIVE, ValueTypeInteger.ValueInteger.of(5));
    }

    // Loaded properties have no validators, as in AspectProperties#fromNBT
    private static AspectProperties loaded(int positive, int any) {
        AspectProperties properties = new AspectProperties();
        properties.setValue(new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "test.positive"), ValueTypeInteger.ValueInteger.of(positive));
        properties.setValue(new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "test.any"), ValueTypeInteger.ValueInteger.of(any));
        return properties;
    }

    @Test
    public void testResetInvalidValuesKeepsValidValues() {
        AspectProperties properties = loaded(10, -10);
        AspectProperties.resetInvalidValues(properties, defaultProperties);
        assertThat(properties.getValue(PROP_POSITIVE).getRawValue(), is(10));
        assertThat(properties.getValue(PROP_ANY).getRawValue(), is(-10));
    }

    @Test
    public void testResetInvalidValuesResetsInvalidValues() {
        AspectProperties properties = loaded(-1, -10);
        AspectProperties.resetInvalidValues(properties, defaultProperties);
        assertThat(properties.getValue(PROP_POSITIVE).getRawValue(), is(5));
        assertThat(properties.getValue(PROP_ANY).getRawValue(), is(-10));
    }

}
