package org.cyclops.integrateddynamics.block;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockCreativeEnergyBattery extends BlockEnergyBatteryBase {

    public BlockCreativeEnergyBattery(Block.Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        ItemStack full = new ItemStack(this);
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) ((ItemBlockEnergyContainer) full.getItem()).getEnergyBattery(full).orElse(null);
        fill(energyStorage);
        list.add(full);
    }

    public boolean isCreative() {
        return true;
    }

}
