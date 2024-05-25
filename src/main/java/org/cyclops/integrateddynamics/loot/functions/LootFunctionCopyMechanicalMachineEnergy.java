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
import org.cyclops.integrateddynamics.core.block.BlockMechanicalMachine;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMechanicalMachine;

import java.util.List;

/**
 * Copies mechanical machine energy.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalMachineEnergy extends LootItemConditionalFunction {

    public static final LootItemFunctionType TYPE = new LootItemFunctionType(LootFunctionCopyMechanicalMachineEnergy.CODEC);
    public static final Codec<LootFunctionCopyMechanicalMachineEnergy> CODEC = RecordCodecBuilder.create(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyMechanicalMachineEnergy::new)
    );

    protected LootFunctionCopyMechanicalMachineEnergy(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityMechanicalMachine) {
            itemStack.getOrCreateTag().putInt(BlockMechanicalMachine.NBT_ENERGY, ((BlockEntityMechanicalMachine) tile).getEnergy());
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

}
