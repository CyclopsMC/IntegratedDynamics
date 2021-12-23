package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraftforge.energy.CapabilityEnergy;
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * Copies energy battery data to the item.
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryData extends LootFunction {
    public static final LootFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_energy_battery_data"), new LootFunctionCopyEnergyBatteryData.Serializer());

    protected LootFunctionCopyEnergyBatteryData(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.getParamOrNull(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileEnergyBattery) {
            itemStack.getCapability(CapabilityEnergy.ENERGY)
                    .ifPresent(energyStorage -> {
                        ((IEnergyStorageMutable) energyStorage).setEnergy(((TileEnergyBattery) tile).getEnergyStored());
                        ((IEnergyStorageCapacity) energyStorage).setCapacity(((TileEnergyBattery) tile).getMaxEnergyStored());
                    });
        }
        return itemStack;
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionCopyEnergyBatteryData> {

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyEnergyBatteryData lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyEnergyBatteryData deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, ILootCondition[] conditionsIn) {
            return new LootFunctionCopyEnergyBatteryData(conditionsIn);
        }
    }

}
