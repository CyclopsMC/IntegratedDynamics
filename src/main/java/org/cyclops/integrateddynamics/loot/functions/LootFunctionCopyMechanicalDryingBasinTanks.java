package org.cyclops.integrateddynamics.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;

import java.util.List;

/**
 * Copies the mechanical drying basin tanks.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanks extends LootItemConditionalFunction {

    public static final MapCodec<LootFunctionCopyMechanicalDryingBasinTanks> CODEC = RecordCodecBuilder.mapCodec(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyMechanicalDryingBasinTanks::new)
    );

    protected LootFunctionCopyMechanicalDryingBasinTanks(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityMechanicalDryingBasin) {
            itemStack.set(RegistryEntries.DATACOMPONENT_FLUID_CONTENT_IN_OUT, Pair.of(
                    SimpleFluidContent.copyOf(((BlockEntityMechanicalDryingBasin) tile).getTankInput().getFluid()),
                    SimpleFluidContent.copyOf(((BlockEntityMechanicalDryingBasin) tile).getTankOutput().getFluid())
            ));
        }
        return itemStack;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

}
