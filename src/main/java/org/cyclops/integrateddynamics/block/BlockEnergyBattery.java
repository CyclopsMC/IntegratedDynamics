package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockEnergyBattery extends BlockEnergyBatteryBase {

    @Icon(location = "block/energy_battery_overlay_side_2")
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite iconOverlay;

    public BlockEnergyBattery(Block.Properties properties) {
        super(properties);
        if(MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getIconProvider().registerIconHolderObject(this);
        }
    }

    @Override
    public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> list) {
        if (MinecraftHelpers.isMinecraftInitialized()) {
            ItemStack itemStack = new ItemStack(this);

            int capacityOriginal = BlockEnergyBatteryConfig.capacity;
            int capacity = capacityOriginal;
            int lastCapacity;
            do {
                ItemStack currentStack = itemStack.copy();
                IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) currentStack.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                energyStorage.setCapacity(capacity);
                list.add(currentStack.copy());
                fill(energyStorage);
                list.add(currentStack.copy());
                lastCapacity = capacity;
                capacity = capacity << 2;
            } while (capacity < Math.min(BlockEnergyBatteryConfig.maxCreativeCapacity, BlockEnergyBatteryConfig.maxCapacity) && capacity > lastCapacity);
        }
    }

    public boolean isCreative() {
        return false;
    }

}
