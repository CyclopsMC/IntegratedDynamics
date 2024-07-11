package org.cyclops.integrateddynamics.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.RegistryEntries;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;

import java.util.function.BiFunction;

/**
 * A mechanical machine base block
 * @author rubensworks
 */
public abstract class BlockMechanicalMachine extends BlockWithEntityGuiCabled {

    public BlockMechanicalMachine(Properties properties, BiFunction<BlockPos, BlockState, CyclopsBlockEntity> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityMechanicalMachine.class)
                    .ifPresent(tile -> {
                        if (itemStack.has(RegistryEntries.COMPONENT_ENERGY_STORAGE)) {
                            tile.setEnergy(itemStack.get(RegistryEntries.COMPONENT_ENERGY_STORAGE));
                        }
                    });
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }

}
