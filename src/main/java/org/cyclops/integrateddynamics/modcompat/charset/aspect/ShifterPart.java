package org.cyclops.integrateddynamics.modcompat.charset.aspect;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import pl.asie.charset.api.pipes.IShifter;

/**
 * Dynamic Shifter implementation.
 * @author rubensworks
 */
public class ShifterPart<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> implements IShifter {

    private final EnumFacing direction;
    private final P partType;
    private final S partState;
    private boolean shifting;
    private Iterable<ValueObjectTypeItemStack.ValueItemStack> filterItem;
    private Iterable<ValueObjectTypeFluidStack.ValueFluidStack> filterFluid;
    private IOperator filterItemPredicate;
    private IOperator filterFluidPredicate;

    public ShifterPart(EnumFacing direction, P partType, S partState) {
        this.direction = direction;
        this.partType = partType;
        this.partState = partState;
        this.shifting = false;
    }

    @Override
    public Mode getMode() {
        return Mode.Shift;
    }

    @Override
    public EnumFacing getDirection() {
        return direction;
    }

    @Override
    public int getShiftDistance() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isShifting() {
        return shifting;
    }

    public void setShifting(boolean shifting) {
        this.shifting = shifting;
    }

    @Override
    public boolean hasFilter() {
        return filterItem != null || filterFluid != null
                || filterItemPredicate != null || filterFluidPredicate != null;
    }

    public void setFilterItem(Iterable<ValueObjectTypeItemStack.ValueItemStack> filterItem) {
        this.filterItem = filterItem;
    }

    public void setFilterFluid(Iterable<ValueObjectTypeFluidStack.ValueFluidStack> filterFluid) {
        this.filterFluid = filterFluid;
    }

    public void setFilterItemPredicate(IOperator filterItemPredicate) {
        this.filterItemPredicate = filterItemPredicate;
    }

    public void setFilterFluidPredicate(IOperator filterFluidPredicate) {
        this.filterFluidPredicate = filterFluidPredicate;
    }

    @Override
    public boolean matches(ItemStack source) {
        if(filterItem != null) {
            for(ValueObjectTypeItemStack.ValueItemStack itemStack : filterItem) {
                if(itemStack.getRawValue().isPresent()
                        && ItemStackHelpers.areItemStacksIdentical(itemStack.getRawValue().get(), source)) {
                    return true;
                }
            }
            return false;
        }
        if(filterItemPredicate != null) {
            ValueObjectTypeItemStack.ValueItemStack valueItemStack = ValueObjectTypeItemStack.ValueItemStack.of(source);
            try {
                IValue result = ValueHelpers.evaluateOperator(filterItemPredicate, valueItemStack);
                return ((ValueTypeBoolean.ValueBoolean) result).getRawValue();
            } catch (EvaluationException e) {
                partState.addError(partState.getActiveAspect(), new L10NHelpers.UnlocalizedString(e.getMessage()));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(FluidStack source) {
        if(filterFluid != null) {
            for(ValueObjectTypeFluidStack.ValueFluidStack fluidStack : filterFluid) {
                if (fluidStack.getRawValue().isPresent()) {
                    FluidStack self = fluidStack.getRawValue().get();
                    if ((self == null && source == null)
                            || (self != null && source != null && self.getFluid() == source.getFluid())) {
                        return true;
                    }
                }
            }
            return false;
        }
        if(filterFluidPredicate != null) {
            ValueObjectTypeFluidStack.ValueFluidStack valueFluidStack = ValueObjectTypeFluidStack.ValueFluidStack.of(source);
            try {
                IValue result = ValueHelpers.evaluateOperator(filterFluidPredicate, valueFluidStack);
                return ((ValueTypeBoolean.ValueBoolean) result).getRawValue();
            } catch (EvaluationException e) {
                partState.addError(partState.getActiveAspect(), new L10NHelpers.UnlocalizedString(e.getMessage()));
                return false;
            }
        }
        return true;
    }
}
