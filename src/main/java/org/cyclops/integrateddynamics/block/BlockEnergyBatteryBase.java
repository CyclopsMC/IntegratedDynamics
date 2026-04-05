package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.core.block.BlockContainerCabled;
import org.cyclops.integrateddynamics.core.helper.Helpers;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A block that holds energy.
 *
 * @author rubensworks
 */
public abstract class BlockEnergyBatteryBase extends BlockContainerCabled {

    public BlockEnergyBatteryBase(Block.Properties properties) {
        super(properties, BlockEntityEnergyBattery::new);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_ENERGY_BATTERY.get(), new BlockEntityEnergyBattery.Ticker());
    }

    public abstract boolean isCreative();

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
                                             BlockHitResult blockRayTraceResult) {
        InteractionResult superActionResult = super.useWithoutItem(state, world, pos, player, blockRayTraceResult);
        if (superActionResult.consumesAction()) {
            return superActionResult;
        }

        if (player.getItemInHand(player.getUsedItemHand()).isEmpty()) {
            return IModHelpers.get().getBlockEntityHelpers().get(world, pos, BlockEntityEnergyBattery.class)
                    .<InteractionResult>map(tile -> {
                        player.sendOverlayMessage(Helpers.getLocalizedEnergyLevel(
                                tile.getEnergyStored(), tile.getMaxEnergyStored()));
                        return InteractionResult.SUCCESS;
                    })
                    .orElse(InteractionResult.PASS);
        }

        return InteractionResult.PASS;
    }

    /**
     * Fill an EnergyHandler with all the energy it can hold
     * @param energyStorage EnergyHandler that is to be filled
     */
    public static void fill(EnergyHandler energyStorage){
        int max = energyStorage.getCapacityAsInt();
        int stored = 1;
        while (stored > 0) {
            try (var tx = Transaction.openRoot()) {
                stored = energyStorage.insert(max, tx);
                tx.commit();
            }
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            IModHelpers.get().getBlockEntityHelpers().get(world, blockPos, BlockEntityEnergyBattery.class)
                    .ifPresent(tile -> itemStackToTile(itemStack.copy().split(1), tile));
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }

    public static void itemStackToTile(ItemStack itemStack, BlockEntityEnergyBattery tile) {
        Optional.ofNullable(itemStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(itemStack)))
                .ifPresent(energyStorage -> {
                    tile.setEnergyStored(energyStorage.getAmountAsInt());
                    tile.getEnergyHandler().setCapacity(energyStorage.getCapacityAsInt());
                });
    }

}
