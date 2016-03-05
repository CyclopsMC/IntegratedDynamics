package org.cyclops.integrateddynamics.modcompat.thaumcraft.logicprogrammer;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.SingleElementType;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeItemStackElement;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.ThaumcraftModCompat;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueObjectTypeAspect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;

/**
 * Logic programmer element types for the fluidstack value type.
 * @author rubensworks
 */
public class ValueObjectTypeAspectElementType extends SingleElementType<ValueTypeItemStackElement> {
    public ValueObjectTypeAspectElementType() {
        super(new ILogicProgrammerElementConstructor<ValueTypeItemStackElement>() {
            @Override
            public ValueTypeItemStackElement construct() {
                return new ValueTypeItemStackElement<>(ThaumcraftModCompat.OBJECT_ASPECT, new ValueTypeItemStackElement.IItemStackToValue<ValueObjectTypeAspect.ValueAspect>() {
                    @Override
                    public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                        AspectList aspectList = AspectHelper.getObjectAspects(itemStack);
                        return aspectList.size() != 0 ? null : new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_THAUMCRAFTASPECT_ERROR_NOASPECT);
                    }

                    @Override
                    public ValueObjectTypeAspect.ValueAspect getValue(ItemStack itemStack) {
                        AspectList aspectList = AspectHelper.getObjectAspects(itemStack);
                        Aspect[] aspectArray = aspectList.getAspectsSortedByAmount();
                        return ValueObjectTypeAspect.ValueAspect.of(aspectArray[0], aspectList.getAmount(aspectArray[0]));
                    }
                }, ThaumcraftModCompat.OBJECT_ASPECT_ELEMENTTYPE);
            }
        }, "thaumcraftaspect");
    }
}
