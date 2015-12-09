package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.BlockHelpers;

/**
 * Value type with values that are blocks (these are internally stored as blockstates).
 * @author rubensworks
 */
public class ValueObjectTypeBlock extends ValueObjectTypeBase<ValueObjectTypeBlock.ValueBlock> {

    public ValueObjectTypeBlock() {
        super("block");
    }

    @Override
    public ValueBlock getDefault() {
        return ValueBlock.of(Blocks.air.getDefaultState());
    }

    @Override
    public String toCompactString(ValueBlock value) {
        return value.getRawValue().getBlock().getLocalizedName();
    }

    @Override
    public String serialize(ValueBlock value) {
        Pair<String, Integer> serializedBlockState = BlockHelpers.serializeBlockState(value.getRawValue());
        return String.format("%s:%s", serializedBlockState.getLeft(), serializedBlockState.getRight());
    }

    @Override
    public ValueBlock deserialize(String value) {
        String[] parts = value.split(":");
        return ValueBlock.of(BlockHelpers.deserializeBlockState(
                Pair.of(parts[0], Integer.parseInt(parts[1]))
        ));
    }

    @ToString
    public static class ValueBlock extends ValueBase {

        private final IBlockState blockState;

        private ValueBlock(IBlockState blockState) {
            super(ValueTypes.OBJECT_BLOCK);
            this.blockState = blockState;
        }

        public static ValueBlock of(IBlockState blockState) {
            return new ValueBlock(blockState);
        }

        public IBlockState getRawValue() {
            return blockState;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueBlock && ((ValueBlock) o).blockState == this.blockState;
        }
    }

}
