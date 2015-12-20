package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItem;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Logic programmer element types for the item value type.
 * @author rubensworks
 */
public class ValueObjectTypeItemElementType extends SingleElementType<ValueTypeItemStackElement> {
    public ValueObjectTypeItemElementType() {
        super(new ILogicProgrammerElementConstructor<ValueTypeItemStackElement>() {
            @Override
            public ValueTypeItemStackElement construct() {
                return new ValueTypeItemStackElement<>(ValueTypes.OBJECT_ITEM, new ValueTypeItemStackElement.IItemStackToValue<ValueObjectTypeItem.ValueItem>() {
                    @Override
                    public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                        return null;
                    }

                    @Override
                    public ValueObjectTypeItem.ValueItem getValue(ItemStack itemStack) {
                        return ValueObjectTypeItem.ValueItem.of(itemStack != null ? itemStack.getItem() : null);
                    }
                }, LogicProgrammerElementTypes.OBJECT_ITEM_TYPE);
            }
        }, "item");
    }
}
