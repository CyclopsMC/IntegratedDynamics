package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeItemStackLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

/**
 * Value type with values that are blocks (these are internally stored as blockstates).
 * @author rubensworks
 */
public class ValueObjectTypeBlock extends ValueObjectTypeBase<ValueObjectTypeBlock.ValueBlock> implements
        IValueTypeNamed<ValueObjectTypeBlock.ValueBlock>, IValueTypeUniquelyNamed<ValueObjectTypeBlock.ValueBlock>,
        IValueTypeNullable<ValueObjectTypeBlock.ValueBlock> {

    public ValueObjectTypeBlock() {
        super("block", ValueObjectTypeBlock.ValueBlock.class);
    }

    public static IFormattableTextComponent getBlockkDisplayNameSafe(BlockState blockState) {
        return new TranslationTextComponent(blockState.getBlock().getTranslationKey());
    }

    @Override
    public ValueBlock getDefault() {
        return ValueBlock.of(Blocks.AIR.getDefaultState());
    }

    @Override
    public IFormattableTextComponent toCompactString(ValueBlock value) {
        if (value.getRawValue().isPresent()) {
            BlockState blockState = value.getRawValue().get();
            ItemStack itemStack = BlockHelpers.getItemStackFromBlockState(blockState);
            if (!itemStack.isEmpty()) {
                return ValueObjectTypeItemStack.getItemStackDisplayNameSafe(itemStack);
            }
            return ValueObjectTypeBlock.getBlockkDisplayNameSafe(blockState);
        }
        return new StringTextComponent("");
    }

    @Override
    public INBT serialize(ValueBlock value) {
        if(!value.getRawValue().isPresent()) return new CompoundNBT();
        return BlockHelpers.serializeBlockState(value.getRawValue().get());
    }

    @Override
    public ValueBlock deserialize(INBT value) {
        if (value.getId() == Constants.NBT.TAG_END || (value.getId() == Constants.NBT.TAG_COMPOUND && ((CompoundNBT) value).isEmpty())) {
            return ValueBlock.of(Blocks.AIR.getDefaultState());
        }
        return ValueBlock.of(BlockHelpers.deserializeBlockState((CompoundNBT) value));
    }

    @Override
    public String getName(ValueBlock a) {
        return toCompactString(a).getString();
    }

    @Override
    public boolean isNull(ValueBlock a) {
        return !a.getRawValue().isPresent();
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeItemStackLPElement<>(this, new ValueTypeItemStackLPElement.IItemStackToValue<ValueObjectTypeBlock.ValueBlock>() {
            @Override
            public boolean isNullable() {
                return true;
            }

            @Override
            public ITextComponent validate(ItemStack itemStack) {
                if(!itemStack.isEmpty() && !(itemStack.getItem() instanceof BlockItem)) {
                    return new TranslationTextComponent(L10NValues.VALUETYPE_OBJECT_BLOCK_ERROR_NOBLOCK);
                }
                return null;
            }

            @Override
            public ValueObjectTypeBlock.ValueBlock getValue(ItemStack itemStack) {
                return ValueObjectTypeBlock.ValueBlock.of(
                        itemStack.isEmpty() ? Blocks.AIR.getDefaultState() : BlockHelpers.getBlockStateFromItemStack(itemStack));
            }
        });
    }

    @Override
    public String getUniqueName(ValueBlock value) {
        if (value.getRawValue().isPresent()) {
            BlockState blockState = value.getRawValue().get();
            return blockState.getBlock().getRegistryName().toString();
        }
        return "";
    }

    @ToString
    public static class ValueBlock extends ValueOptionalBase<BlockState> {

        private ValueBlock(BlockState blockState) {
            super(ValueTypes.OBJECT_BLOCK, blockState);
        }

        public static ValueBlock of(BlockState blockState) {
            return new ValueBlock(blockState);
        }

        @Override
        protected boolean isEqual(BlockState a, BlockState b) {
            return a.equals(b);
        }
    }

}
