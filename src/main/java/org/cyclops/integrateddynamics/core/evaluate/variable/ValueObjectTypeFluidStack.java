package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Optional;
import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;

/**
 * Value type with values that are fluidstacks.
 * @author rubensworks
 */
public class ValueObjectTypeFluidStack extends ValueObjectTypeBase<ValueObjectTypeFluidStack.ValueFluidStack> implements
        IValueTypeNamed<ValueObjectTypeFluidStack.ValueFluidStack>, IValueTypeNullable<ValueObjectTypeFluidStack.ValueFluidStack> {

    public ValueObjectTypeFluidStack() {
        super("fluidstack");
    }

    @Override
    public ValueFluidStack getDefault() {
        return ValueFluidStack.of(null);
    }

    @Override
    public String toCompactString(ValueFluidStack value) {
        Optional<FluidStack> fluidStack = value.getRawValue();
        return fluidStack.isPresent() ? String.format("%s (%s mB)", fluidStack.get().getLocalizedName(), fluidStack.get().amount) : "";
    }

    @Override
    public String serialize(ValueFluidStack value) {
        NBTTagCompound tag = new NBTTagCompound();
        Optional<FluidStack> fluidStack = value.getRawValue();
        if(fluidStack.isPresent()) fluidStack.get().writeToNBT(tag);
        return tag.toString();
    }

    @Override
    public ValueFluidStack deserialize(String value) {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag);
            return ValueFluidStack.of(fluidStack);
        } catch (NBTException e) {
            return null;
        }
    }

    @Override
    public String getName(ValueFluidStack a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueFluidStack a) {
        return !a.getRawValue().isPresent();
    }

    @ToString
    public static class ValueFluidStack extends ValueOptionalBase<FluidStack> {

        private ValueFluidStack(FluidStack fluidStack) {
            super(ValueTypes.OBJECT_FLUIDSTACK, fluidStack);
        }

        public static ValueFluidStack of(FluidStack fluidStack) {
            return new ValueFluidStack(fluidStack);
        }

        @Override
        protected boolean isEqual(FluidStack a, FluidStack b) {
            return a.isFluidStackIdentical(b);
        }
    }

}
