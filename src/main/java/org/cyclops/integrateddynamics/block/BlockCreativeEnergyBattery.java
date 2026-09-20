package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Block;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockCreativeEnergyBattery extends BlockEnergyBatteryBase {

    public BlockCreativeEnergyBattery(Block.Properties properties) {
        super(properties);
    }

    public boolean isCreative() {
        return true;
    }

}
