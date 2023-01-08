package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeItemStackLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

import java.util.Objects;

/**
 * Value type with values that are fluidstacks.
 * @author rubensworks
 */
public class ValueObjectTypeFluidStack extends ValueObjectTypeBase<ValueObjectTypeFluidStack.ValueFluidStack> implements
        IValueTypeNamed<ValueObjectTypeFluidStack.ValueFluidStack>,
        IValueTypeUniquelyNamed<ValueObjectTypeFluidStack.ValueFluidStack>,
        IValueTypeNullable<ValueObjectTypeFluidStack.ValueFluidStack> {

    public ValueObjectTypeFluidStack() {
        super("fluidstack", ValueObjectTypeFluidStack.ValueFluidStack.class);
    }

    @Override
    public ValueFluidStack getDefault() {
        return ValueFluidStack.of(FluidStack.EMPTY);
    }

    @Override
    public MutableComponent toCompactString(ValueFluidStack value) {
        FluidStack fluidStack = value.getRawValue();
        return !fluidStack.isEmpty() ? ((MutableComponent) fluidStack.getDisplayName()).append(String.format(" (%s mB)", fluidStack.getAmount())) : Component.literal("");
    }

    @Override
    public Tag serialize(ValueFluidStack value) {
        CompoundTag tag = new CompoundTag();
        value.getRawValue().writeToNBT(tag);
        return tag;
    }

    @Override
    public ValueFluidStack deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        if (value instanceof CompoundTag) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT((CompoundTag) value);
            return ValueFluidStack.of(fluidStack);
        } else {
            return null;
        }
    }

    @Override
    public String getName(ValueFluidStack a) {
        return toCompactString(a).getString();
    }

    @Override
    public boolean isNull(ValueFluidStack a) {
        return a.getRawValue().isEmpty();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeItemStackLPElement<>(this, new ValueTypeItemStackLPElement.IItemStackToValue<ValueObjectTypeFluidStack.ValueFluidStack>() {
            @Override
            public boolean isNullable() {
                return true;
            }

            @Override
            public Component validate(ItemStack itemStack) {
                return itemStack.isEmpty() || Helpers.getFluidStack(itemStack) != null ? null : Component.translatable(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
            }

            @Override
            public ValueObjectTypeFluidStack.ValueFluidStack getValue(ItemStack itemStack) {
                return ValueObjectTypeFluidStack.ValueFluidStack.of(Helpers.getFluidStack(itemStack));
            }
        });
    }

    @Override
    public String getUniqueName(ValueFluidStack value) {
        FluidStack fluidStack = value.getRawValue();
        return !fluidStack.isEmpty() ?
                String.format("%s %s", ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()), fluidStack.getAmount()) : "";
    }

    @ToString
    public static class ValueFluidStack extends ValueBase {

        private final FluidStack fluidStack;

        private ValueFluidStack(FluidStack itemStack) {
            super(ValueTypes.OBJECT_FLUIDSTACK);
            this.fluidStack = Objects.requireNonNull(itemStack, "Attempted to create a ValueFluidStack for a null FluidStack.");
        }

        public static ValueFluidStack of(FluidStack itemStack) {
            return new ValueFluidStack(itemStack);
        }

        public FluidStack getRawValue() {
            return fluidStack;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueFluidStack && this.getRawValue().isFluidStackIdentical(((ValueFluidStack) o).getRawValue());
        }

        @Override
        public int hashCode() {
            return fluidStack.hashCode();
        }

    }

}
