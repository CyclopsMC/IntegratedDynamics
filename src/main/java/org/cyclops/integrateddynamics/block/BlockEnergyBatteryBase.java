package org.cyclops.integrateddynamics.block;

import net.minecraft.util.BlockRenderLayer;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.core.block.BlockContainerCabled;
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
    public BlockRenderLayer getBlockLayer() {
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

}
