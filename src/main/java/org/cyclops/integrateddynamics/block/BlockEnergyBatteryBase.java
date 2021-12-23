package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.block.BlockContainerCabled;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * A block that holds energy.
 *
 * @author rubensworks
 */
public abstract class BlockEnergyBatteryBase extends BlockContainerCabled implements IEnergyContainerBlock {

    public BlockEnergyBatteryBase(Block.Properties properties) {
        super(properties, TileEnergyBattery::new);
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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                                             BlockRayTraceResult blockRayTraceResult) {
        ActionResultType superActionResult = super.use(state, world, pos, player, hand, blockRayTraceResult);
        if (superActionResult.consumesAction()) {
            return superActionResult;
        }

        if (player.getItemInHand(hand).isEmpty()) {
            return TileHelpers.getSafeTile(world, pos, TileEnergyBattery.class)
                    .map(tile -> {
                        player.displayClientMessage(Helpers.getLocalizedEnergyLevel(
                                tile.getEnergyStored(), tile.getMaxEnergyStored()), true);
                        return ActionResultType.SUCCESS;
                    })
                    .orElse(ActionResultType.PASS);
        }

        return ActionResultType.PASS;
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
    public void setPlacedBy(World world, BlockPos blockPos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide()) {
            TileHelpers.getSafeTile(world, blockPos, TileEnergyBattery.class)
                    .ifPresent(tile -> itemStackToTile(itemStack, tile));
        }
        super.setPlacedBy(world, blockPos, state, placer, itemStack);
    }

    public static void itemStackToTile(ItemStack itemStack, TileEnergyBattery tile) {
        itemStack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(energyStorage -> {
                    tile.setEnergyStored(energyStorage.getEnergyStored());
                    tile.setCapacity(energyStorage.getMaxEnergyStored());
                });
    }

}
