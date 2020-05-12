package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
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
    public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> list) {
        ItemStack full = new ItemStack(this);
        IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) ((ItemBlockEnergyContainer) full.getItem()).getEnergyBattery(full).orElse(null);
        fill(energyStorage);
        list.add(full);
    }

    public boolean isCreative() {
        return true;
    }

}
