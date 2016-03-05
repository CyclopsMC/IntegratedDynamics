package org.cyclops.integrateddynamics.modcompat.charset.aspect.write;

import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.modcompat.charset.CharsetPipesModCompat;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteListBase;

/**
 * Set the target shifter filter.
 * @author rubensworks
 */
public class AspectWriteListCharsetPipesShifter extends AspectWriteListBase {

    @Override
    protected String getUnlocalizedListType() {
        return "charsetpipe.shifter";
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
        super.onActivate(partType, target, state);
        state.addVolatileCapability(CharsetPipesModCompat.SHIFTER, new ShifterPart(target.getCenter().getSide()));
        AspectWriteBooleanCharsetPipesShifter.notifyNeighbours(target);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType, PartTarget target, S state) {
        super.onDeactivate(partType, target, state);
        state.removeVolatileCapability(CharsetPipesModCompat.SHIFTER);
        AspectWriteBooleanCharsetPipesShifter.notifyNeighbours(target);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeList.ValueList> variable) {
        try {
            ValueTypeList.ValueList value = variable.getValue();
            if(value.getRawValue().getValueType() == ValueTypes.OBJECT_ITEMSTACK) {
                IValueTypeListProxy<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack> listProxy = value.getRawValue();
                ShifterPart shifter = (ShifterPart) state.getCapability(CharsetPipesModCompat.SHIFTER);
                shifter.setFilter(listProxy);
                shifter.setShifting(true);
            } else {
                state.addError(this, new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                        new L10NHelpers.UnlocalizedString(ValueTypes.OBJECT_ITEMSTACK.getUnlocalizedName()),
                        new L10NHelpers.UnlocalizedString(value.getRawValue().getValueType().getUnlocalizedName())));
                state.setDeactivated(true);
            }
        } catch (EvaluationException e) {
            state.addError(this, new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
            state.setDeactivated(true);
        }
    }
}
