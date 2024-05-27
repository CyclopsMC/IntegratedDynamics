package org.cyclops.integrateddynamics.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;

import java.util.List;

/**
 * Copies energy battery data to the item.
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryData extends LootItemConditionalFunction {

    public static final Codec<LootFunctionCopyEnergyBatteryData> CODEC = RecordCodecBuilder.create(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyEnergyBatteryData::new)
    );
    public static final LootItemFunctionType TYPE = new LootItemFunctionType(LootFunctionCopyEnergyBatteryData.CODEC);

    protected LootFunctionCopyEnergyBatteryData(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityEnergyBattery) {
            IEnergyStorage energyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energyStorage != null) {
                ((IEnergyStorageMutable) energyStorage).setEnergy(((BlockEntityEnergyBattery) tile).getEnergyStored());
                ((IEnergyStorageCapacity) energyStorage).setCapacity(((BlockEntityEnergyBattery) tile).getMaxEnergyStored());
            }
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

}
