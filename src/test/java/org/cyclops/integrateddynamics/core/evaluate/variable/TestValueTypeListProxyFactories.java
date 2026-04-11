package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import org.cyclops.cyclopscore.helper.CyclopsCoreInstance;
import org.cyclops.integrateddynamics.ModBaseMocked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.cyclops.integrateddynamics.core.test.TestHelpers.deserialize;
import static org.cyclops.integrateddynamics.core.test.TestHelpers.serialize;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the factory types of value list proxies.
 * Entity and inventory types should be checked manually.
 * @author rubensworks
 */
public class TestValueTypeListProxyFactories {

    static { CyclopsCoreInstance.MOD = new ModBaseMocked(); }

    @BeforeAll
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
    public void testMaterializedIntegerListFromIntArrayTag() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        // Simulate codec round-trip: ListTag<IntTag> -> IntArrayTag (as done by Minecraft's NbtOps)
        ValueTypeListProxyMaterialized<?, ?> proxy = new ValueTypeListProxyMaterialized<>(
                ValueTypes.INTEGER,
                Lists.newArrayList(ValueTypeInteger.ValueInteger.of(42))
        );
        CompoundTag tag = serialize(o -> {
            try {
                ValueTypeListProxyFactories.REGISTRY.serialize(o, proxy);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        tag.put("values", new IntArrayTag(new int[]{42}));
        IValueTypeListProxy<?, ?> proxyNew = deserialize(tag, valueInput -> {
            try {
                return ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        assertThat(proxyNew, equalTo(proxy));
    }

    @Test
    public void testMaterializedLongListFromLongArrayTag() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        // Simulate codec round-trip: ListTag<LongTag> -> LongArrayTag (as done by Minecraft's NbtOps)
        ValueTypeListProxyMaterialized<?, ?> proxy = new ValueTypeListProxyMaterialized<>(
                ValueTypes.LONG,
                Lists.newArrayList(ValueTypeLong.ValueLong.of(123L))
        );
        CompoundTag tag = serialize(o -> {
            try {
                ValueTypeListProxyFactories.REGISTRY.serialize(o, proxy);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        tag.put("values", new LongArrayTag(new long[]{123L}));
        IValueTypeListProxy<?, ?> proxyNew = deserialize(tag, valueInput -> {
            try {
                return ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        assertThat(proxyNew, equalTo(proxy));
    }

    @Test
    public void testMaterializedBooleanListFromByteArrayTag() throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        // Simulate codec round-trip: ListTag<ByteTag> -> ByteArrayTag (as done by Minecraft's NbtOps)
        ValueTypeListProxyMaterialized<?, ?> proxy = new ValueTypeListProxyMaterialized<>(
                ValueTypes.BOOLEAN,
                Lists.newArrayList(ValueTypeBoolean.ValueBoolean.of(true))
        );
        CompoundTag tag = serialize(o -> {
            try {
                ValueTypeListProxyFactories.REGISTRY.serialize(o, proxy);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        tag.put("values", new ByteArrayTag(new byte[]{(byte) 1}));
        IValueTypeListProxy<?, ?> proxyNew = deserialize(tag, valueInput -> {
            try {
                return ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        assertThat(proxyNew, equalTo(proxy));
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

    protected void testFactoryType(IValueTypeListProxy<?, ?> proxy) {
        CompoundTag serialized = serialize(o -> {
            try {
                ValueTypeListProxyFactories.REGISTRY.serialize(o, proxy);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        IValueTypeListProxy<?, ?> proxyNew = deserialize(serialized, valueInput -> {
            try {
                return ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput);
            } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
                throw new RuntimeException(e);
            }
        }, ValueDeseralizationContextMocked.get().holderLookupProvider());
        if (!(proxy.isInfinite() && proxy.isInfinite())) {
            assertThat(proxyNew, equalTo(proxy));
        }
    }

}
