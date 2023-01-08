package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;

/**
 * Copies energy battery data to the item.
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryData extends LootItemConditionalFunction {
    public static final LootItemFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_energy_battery_data"), new LootFunctionCopyEnergyBatteryData.Serializer());

    protected LootFunctionCopyEnergyBatteryData(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityEnergyBattery) {
            itemStack.getCapability(ForgeCapabilities.ENERGY)
                    .ifPresent(energyStorage -> {
                        ((IEnergyStorageMutable) energyStorage).setEnergy(((BlockEntityEnergyBattery) tile).getEnergyStored());
                        ((IEnergyStorageCapacity) energyStorage).setCapacity(((BlockEntityEnergyBattery) tile).getMaxEnergyStored());
                    });
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LootFunctionCopyEnergyBatteryData> {

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyEnergyBatteryData lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyEnergyBatteryData deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] conditionsIn) {
            return new LootFunctionCopyEnergyBatteryData(conditionsIn);
        }
    }

}
