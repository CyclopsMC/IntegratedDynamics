package org.cyclops.integrateddynamics.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * A mechanical machine base block
 * @author rubensworks
 */
public abstract class BlockMechanicalMachine extends BlockWithEntityGuiCabled {

    public static final String NBT_ENERGY = "energy";

    public BlockMechanicalMachine(Properties properties, BiFunction<BlockPos, BlockState, CyclopsBlockEntity> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityMechanicalMachine.class)
                    .ifPresent(tile -> {
                        if (itemStack.hasTag() && itemStack.getTag().contains(NBT_ENERGY, Tag.TAG_INT)) {
                            tile.setEnergy(itemStack.getTag().getInt(NBT_ENERGY));
                        }
                    });
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }

}
