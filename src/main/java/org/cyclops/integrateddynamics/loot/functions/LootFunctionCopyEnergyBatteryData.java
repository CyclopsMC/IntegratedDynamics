package org.cyclops.integrateddynamics.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageMutable;

import java.util.Optional;

/**
 * Copies energy battery data to the item.
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryData extends LootItemConditionalFunction {

    public static final MapCodec<LootFunctionCopyEnergyBatteryData> CODEC = RecordCodecBuilder.mapCodec(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyEnergyBatteryData::new)
    );

    protected LootFunctionCopyEnergyBatteryData(Optional<Holder<LootItemCondition>> condition) {
        super(condition);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getOptional(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityEnergyBattery) {
            EnergyHandler energyStorage = itemStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(itemStack));
            if (energyStorage != null) {
                ((IEnergyStorageMutable) energyStorage).setEnergy(((BlockEntityEnergyBattery) tile).getEnergyStored());
                ((IEnergyStorageCapacity) energyStorage).setCapacity(((BlockEntityEnergyBattery) tile).getMaxEnergyStored());
            }
        }
        return itemStack;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

}
