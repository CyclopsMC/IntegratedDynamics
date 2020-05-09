package org.cyclops.integrateddynamics.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
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

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockEnergyBatteryBase(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileEnergyBattery.class);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        if (player.getHeldItem(hand).isEmpty()) {
            TileEnergyBattery tile = TileHelpers.getSafeTile(world, pos, TileEnergyBattery.class);
            if (tile != null) {
                player.sendStatusMessage(new TextComponentString(Helpers.getLocalizedEnergyLevel(
                        tile.getEnergyStored(), tile.getMaxEnergyStored())), true);
                return true;
            }
        }
        return false;
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
}
