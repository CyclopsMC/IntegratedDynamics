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
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;

import java.util.Optional;

/**
 * Copies a proxy id to the item.
 * @author rubensworks
 */
public class LootFunctionCopyProxyId extends LootItemConditionalFunction {

    public static final MapCodec<LootFunctionCopyProxyId> CODEC = RecordCodecBuilder.mapCodec(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyProxyId::new)
    );

    protected LootFunctionCopyProxyId(Optional<Holder<LootItemCondition>> condition) {
        super(condition);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getOptional(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityProxy) {
            itemStack.set(RegistryEntries.DATACOMPONENT_PROXY_ID, ((BlockEntityProxy) tile).getProxyId());
        }
        return itemStack;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

}
