package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.base.Strings;
import lombok.ToString;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
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
        super("block");
    }

    public static String getBlockDisplayNameUsSafe(IBlockState blockState) throws NoSuchMethodException {
        return blockState.getBlock().getLocalizedName();
    }

    public static String getBlockkDisplayNameSafe(IBlockState blockState) {
        // Certain mods may call client-side only methods,
        // so call a server-side-safe fallback method if that fails.
        try {
            return getBlockDisplayNameUsSafe(blockState);
        } catch (NoSuchMethodException e) {
            return L10NHelpers.localize(blockState.getBlock().getTranslationKey() + ".name");
        }
    }

    @Override
    public ValueBlock getDefault() {
        return ValueBlock.of(Blocks.AIR.getDefaultState());
    }

    @Override
    public String toCompactString(ValueBlock value) {
        if (value.getRawValue().isPresent()) {
            IBlockState blockState = value.getRawValue().get();
            ItemStack itemStack = BlockHelpers.getItemStackFromBlockState(blockState);
            if (!itemStack.isEmpty()) {
                return ValueObjectTypeItemStack.getItemStackDisplayNameSafe(itemStack);
            }
            return ValueObjectTypeBlock.getBlockkDisplayNameSafe(blockState);
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
        if(Strings.isNullOrEmpty(value)) return ValueBlock.of(Blocks.AIR.getDefaultState());
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

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeItemStackLPElement<>(this, new ValueTypeItemStackLPElement.IItemStackToValue<ValueObjectTypeBlock.ValueBlock>() {
            @Override
            public boolean isNullable() {
                return true;
            }

            @Override
            public L10NHelpers.UnlocalizedString validate(ItemStack itemStack) {
                if(!itemStack.isEmpty() && !(itemStack.getItem() instanceof ItemBlock)) {
                    return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_OBJECT_BLOCK_ERROR_NOBLOCK);
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
            IBlockState blockState = value.getRawValue().get();
            int meta = blockState.getBlock().getMetaFromState(blockState);
            return blockState.getBlock().getRegistryName() + (meta > 0 ? " " + meta : "");
        }
        return "";
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
            Block blockA = a.getBlock();
            Block blockB = b.getBlock();
            return blockA == blockB && blockA.getMetaFromState(a) == blockB.getMetaFromState(b);
        }
    }

}
