package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.core.block.BlockContainerCabled;
import org.cyclops.integrateddynamics.core.helper.Helpers;

import javax.annotation.Nullable;

/**
 * A block that holds energy.
 *
 * @author rubensworks
 */
public abstract class BlockEnergyBatteryBase extends BlockContainerCabled implements IEnergyContainerBlock {

    public BlockEnergyBatteryBase(Block.Properties properties) {
        super(properties, BlockEntityEnergyBattery::new);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_ENERGY_BATTERY, new BlockEntityEnergyBattery.Ticker());
    }

    @Override
    public String getEneryContainerNBTName() {
        return "energy";
    }

    @Override
    public String getEneryContainerCapacityNBTName() {
        return "capacity";
    }

    public abstract boolean isCreative();

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                             BlockHitResult blockRayTraceResult) {
        InteractionResult superActionResult = super.use(state, world, pos, player, hand, blockRayTraceResult);
        if (superActionResult.consumesAction()) {
            return superActionResult;
        }

        if (player.getItemInHand(hand).isEmpty()) {
            return BlockEntityHelpers.get(world, pos, BlockEntityEnergyBattery.class)
                    .map(tile -> {
                        player.displayClientMessage(Helpers.getLocalizedEnergyLevel(
                                tile.getEnergyStored(), tile.getMaxEnergyStored()), true);
                        return InteractionResult.SUCCESS;
                    })
                    .orElse(InteractionResult.PASS);
        }

        return InteractionResult.PASS;
    }

    /**
     * Fill an IEnergyStorage with all the energy it can hold
     * @param energyStorage IEnergyStorage that is to be filled
     */
    public static void fill(IEnergyStorage energyStorage){
        int max = energyStorage.getMaxEnergyStored();
        int stored = 1;
        while (stored > 0) {
            stored = energyStorage.receiveEnergy(max, false);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityEnergyBattery.class)
                    .ifPresent(tile -> itemStackToTile(itemStack, tile));
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }

    public static void itemStackToTile(ItemStack itemStack, BlockEntityEnergyBattery tile) {
        itemStack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(energyStorage -> {
                    tile.setEnergyStored(energyStorage.getEnergyStored());
                    tile.setCapacity(energyStorage.getMaxEnergyStored());
                });
    }

}
