package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Logic programmer element types for the itemstack value type.
 * @author rubensworks
 */
public class ValueObjectTypeItemStackElementType extends SingleElementType<ValueTypeItemStackElement> {
    public ValueObjectTypeItemStackElementType() {
        super(new SingleElementType.ILogicProgrammerElementConstructor<ValueTypeItemStackElement>() {
            @Override
            public ValueTypeItemStackElement construct() {
                return new ValueTypeItemStackElement<>(ValueTypes.OBJECT_ITEMSTACK, new ValueTypeItemStackElement.IItemStackToValue<ValueObjectTypeItemStack.ValueItemStack>() {
                    @Override
                    public boolean isNullable() {
                        return true;
                    }

                    @Override
                    public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                        return null;
                    }

                    @Override
                    public ValueObjectTypeItemStack.ValueItemStack getValue(ItemStack itemStack) {
                        return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
                    }
                }, LogicProgrammerElementTypes.OBJECT_ITEMSTACK_TYPE);
            }
        }, "itemstack");
    }
}
