package org.cyclops.integrateddynamics.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalSqueezer;

import java.util.List;

/**
 * Copies the mechanical squeezer tank.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalSqueezerTank extends LootItemConditionalFunction {

    public static final MapCodec<LootFunctionCopyMechanicalSqueezerTank> CODEC = RecordCodecBuilder.mapCodec(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyMechanicalSqueezerTank::new)
    );
    public static final LootItemFunctionType<LootFunctionCopyMechanicalSqueezerTank> TYPE = new LootItemFunctionType<>(LootFunctionCopyMechanicalSqueezerTank.CODEC);

    protected LootFunctionCopyMechanicalSqueezerTank(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityMechanicalSqueezer) {
            itemStack.set(org.cyclops.cyclopscore.RegistryEntries.COMPONENT_FLUID_CONTENT, SimpleFluidContent.copyOf(((BlockEntityMechanicalSqueezer) tile).getTank().getFluid()));
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

}
