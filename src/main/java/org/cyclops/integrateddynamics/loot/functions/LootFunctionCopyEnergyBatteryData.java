package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockProxy;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Copies energy battery data to the item.
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryData implements ILootFunction {

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileEnergyBattery) {
            itemStack.getCapability(CapabilityEnergy.ENERGY)
                    .ifPresent(energyStorage -> {
                        ((IEnergyStorageMutable) energyStorage).setEnergy(((TileEnergyBattery) tile).getEnergyStored());
                        ((IEnergyStorageCapacity) energyStorage).setCapacity(((TileEnergyBattery) tile).getMaxEnergyStored());
                    });
        }
        return itemStack;
    }

    public static class Serializer extends ILootFunction.Serializer<LootFunctionCopyEnergyBatteryData> {

        public Serializer() {
            super(new ResourceLocation(Reference.MOD_ID, "copy_energy_battery_data"), LootFunctionCopyEnergyBatteryData.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyEnergyBatteryData lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyEnergyBatteryData deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootFunctionCopyEnergyBatteryData();
        }
    }

}
