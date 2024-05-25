package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeUniquelyNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
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

    public static MutableComponent getBlockkDisplayNameSafe(BlockState blockState) {
        return Component.translatable(blockState.getBlock().getDescriptionId());
    }

    @Override
    public ValueBlock getDefault() {
        return ValueBlock.of(Blocks.AIR.defaultBlockState());
    }

    @Override
    public MutableComponent toCompactString(ValueBlock value) {
        if (value.getRawValue().isPresent()) {
            BlockState blockState = value.getRawValue().get();
            ItemStack itemStack = BlockHelpers.getItemStackFromBlockState(blockState);
            if (!itemStack.isEmpty()) {
                return ValueObjectTypeItemStack.getItemStackDisplayNameSafe(itemStack);
            }
            return ValueObjectTypeBlock.getBlockkDisplayNameSafe(blockState);
        }
        return Component.literal("");
    }

    @Override
    public Tag serialize(ValueBlock value) {
        if(!value.getRawValue().isPresent()) return new CompoundTag();
        return BlockHelpers.serializeBlockState(value.getRawValue().get());
    }

    @Override
    public ValueBlock deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) {
        if (value.getId() == Tag.TAG_END || (value.getId() == Tag.TAG_COMPOUND && ((CompoundTag) value).isEmpty())) {
            return ValueBlock.of(Blocks.AIR.defaultBlockState());
        }
        return ValueBlock.of(BlockHelpers.deserializeBlockState(valueDeseralizationContext.holderGetter(), (CompoundTag) value));
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
            public Component validate(ItemStack itemStack) {
                if(!itemStack.isEmpty() && !(itemStack.getItem() instanceof BlockItem)) {
                    return Component.translatable(L10NValues.VALUETYPE_OBJECT_BLOCK_ERROR_NOBLOCK);
                }
                return null;
            }

            @Override
            public ValueObjectTypeBlock.ValueBlock getValue(ItemStack itemStack) {
                return ValueObjectTypeBlock.ValueBlock.of(
                        itemStack.isEmpty() ? Blocks.AIR.defaultBlockState() : BlockHelpers.getBlockStateFromItemStack(itemStack));
            }
        });
    }

    @Override
    public String getUniqueName(ValueBlock value) {
        if (value.getRawValue().isPresent()) {
            BlockState blockState = value.getRawValue().get();
            return BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
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
