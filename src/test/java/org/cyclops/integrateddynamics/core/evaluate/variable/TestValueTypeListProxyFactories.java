package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the factory types of value list proxies.
 * Entity and inventory types should be checked manually.
 * @author rubensworks
 */
public class TestValueTypeListProxyFactories {

    @BeforeClass
    public static void before() {
        ValueTypeListProxyFactories.load();
    }

    @Test
    public void testAppend() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyAppend(
                ValueTypeList.ValueList.ofAll(ValueTypeBoolean.ValueBoolean.of(true)).getRawValue(),
                ValueTypeBoolean.ValueBoolean.of(true)
        ));
    }

    @Test
    public void testConcat() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyConcat<>(
                ValueTypeList.ValueList.ofAll(ValueTypeBoolean.ValueBoolean.of(true)).getRawValue(),
                ValueTypeList.ValueList.ofAll(ValueTypeBoolean.ValueBoolean.of(true)).getRawValue()
        ));
    }

    @Test
    public void testLazyBuilt() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyLazyBuilt<>(
                ValueTypeInteger.ValueInteger.of(0),
                Operators.ARITHMETIC_ADDITION
        ));
    }

    @Test
    public void testMaterialized() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyMaterialized<>(
                ValueTypes.BOOLEAN,
                Lists.newArrayList(ValueTypeBoolean.ValueBoolean.of(true))
        ));
    }

    @Test
    public void testNbtKeys() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyNbtKeys(
                Optional.of(new CompoundTag())
        ));
    }

    @Test
    public void testNbtValueListByte() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyNbtValueListByte(
                "a",
                Optional.of(new CompoundTag())
        ));
    }

    @Test
    public void testNbtValueListInt() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyNbtValueListInt(
                "a",
                Optional.of(new CompoundTag())
        ));
    }

    @Test
    public void testNbtValueListTag() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyNbtValueListTag(
                "a",
                Optional.of(new CompoundTag())
        ));
    }

    @Test
    public void testNbtValueOperatorMapped() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        testFactoryType(new ValueTypeListProxyOperatorMapped(
                Operators.ARITHMETIC_ADDITION,
                ValueTypeList.ValueList.ofAll(ValueTypeInteger.ValueInteger.of(10)).getRawValue()
        ));
    }

    protected void testFactoryType(IValueTypeListProxy<?, ?> proxy) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        Tag serialized = ValueTypeListProxyFactories.REGISTRY.serialize(proxy);
        IValueTypeListProxy<?, ?> proxyNew = ValueTypeListProxyFactories.REGISTRY.deserialize(serialized);
        if (!(proxy.isInfinite() && proxy.isInfinite())) {
            assertThat(proxyNew, equalTo(proxy));
        }
    }

}
