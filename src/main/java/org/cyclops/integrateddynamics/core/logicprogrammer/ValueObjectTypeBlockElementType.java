package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Logic programmer element types for the block value type.
 * @author rubensworks
 */
public class ValueObjectTypeBlockElementType extends SingleElementType<ValueTypeItemStackElement> {
    public ValueObjectTypeBlockElementType() {
        super(new SingleElementType.ILogicProgrammerElementConstructor<ValueTypeItemStackElement>() {
            @Override
            public ValueTypeItemStackElement construct() {
                return new ValueTypeItemStackElement<>(ValueTypes.OBJECT_BLOCK, new ValueTypeItemStackElement.IItemStackToValue<ValueObjectTypeBlock.ValueBlock>() {
                    @Override
                    public boolean isNullable() {
                        return true;
                    }

                    @Override
                    public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                        if(itemStack != null && !(itemStack.getItem() instanceof ItemBlock)) {
                            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_BLOCK_ERROR_NOBLOCK);
                        }
                        return null;
                    }

                    @Override
                    public ValueObjectTypeBlock.ValueBlock getValue(ItemStack itemStack) {
                        return ValueObjectTypeBlock.ValueBlock.of(BlockHelpers.getBlockStateFromItemStack(itemStack));
                    }
                }, LogicProgrammerElementTypes.OBJECT_BLOCK_TYPE);
            }
        }, "block");
    }
}
