package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
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
    public IFormattableTextComponent toCompactString(ValueFluidStack value) {
        FluidStack fluidStack = value.getRawValue();
        return !fluidStack.isEmpty() ? ((IFormattableTextComponent) fluidStack.getDisplayName()).appendString(String.format(" (%s mB)", fluidStack.getAmount())) : new StringTextComponent("");
    }

    @Override
    public INBT serialize(ValueFluidStack value) {
        CompoundNBT tag = new CompoundNBT();
        value.getRawValue().writeToNBT(tag);
        return tag;
    }

    @Override
    public ValueFluidStack deserialize(INBT value) {
        if (value instanceof CompoundNBT) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT((CompoundNBT) value);
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
            public ITextComponent validate(ItemStack itemStack) {
                return itemStack.isEmpty() || Helpers.getFluidStack(itemStack) != null ? null : new TranslationTextComponent(L10NValues.VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID);
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
                String.format("%s %s", fluidStack.getFluid().getRegistryName(), fluidStack.getAmount()) : "";
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
