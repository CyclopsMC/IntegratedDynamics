package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import lombok.ToString;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;

/**
 * Value type with values that are blocks (these are internally stored as blockstates).
 * @author rubensworks
 */
public class ValueObjectTypeBlock extends ValueObjectTypeBase<ValueObjectTypeBlock.ValueBlock> implements
        IValueTypeNamed<ValueObjectTypeBlock.ValueBlock>, IValueTypeNullable<ValueObjectTypeBlock.ValueBlock> {

    public ValueObjectTypeBlock() {
        super("block");
    }

    @Override
    public ValueBlock getDefault() {
        return ValueBlock.of(null);
    }

    @Override
    public String toCompactString(ValueBlock value) {
        if (value.getRawValue().isPresent()) {
            IBlockState blockState = value.getRawValue().get();
            ItemStack itemStack = BlockHelpers.getItemStackFromBlockState(blockState);
            if (itemStack != null) {
                return itemStack.getDisplayName();
            }
            return blockState.getBlock().getLocalizedName();
        }
        return "";
    }

    @Override
    public String serialize(ValueBlock value) {
        if(!value.getRawValue().isPresent()) return "";
        Pair<String, Integer> serializedBlockState = BlockHelpers.serializeBlockState(value.getRawValue().get());
        return String.format("%s$%s", serializedBlockState.getLeft(), serializedBlockState.getRight());
    }

    @Override
    public ValueBlock deserialize(String value) {
        if(Strings.isNullOrEmpty(value)) return ValueBlock.of(null);
        String[] parts = value.split("\\$");
        try {
            return ValueBlock.of(BlockHelpers.deserializeBlockState(
                    Pair.of(parts[0], Integer.parseInt(parts[1]))
            ));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Something went wrong while deserializing '%s'.", value));
        }
    }

    @Override
    public String getName(ValueBlock a) {
        return toCompactString(a);
    }

    @Override
    public boolean isNull(ValueBlock a) {
        return !a.getRawValue().isPresent();
    }

    @ToString
    public static class ValueBlock extends ValueOptionalBase<IBlockState> {

        private ValueBlock(IBlockState blockState) {
            super(ValueTypes.OBJECT_BLOCK, blockState);
        }

        public static ValueBlock of(IBlockState blockState) {
            return new ValueBlock(blockState);
        }

        @Override
        protected boolean isEqual(IBlockState a, IBlockState b) {
            return a.getBlock().getMetaFromState(a) == b.getBlock().getMetaFromState(b);
        }
    }

}
