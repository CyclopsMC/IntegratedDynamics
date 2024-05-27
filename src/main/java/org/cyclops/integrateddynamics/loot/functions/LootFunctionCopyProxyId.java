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
import org.cyclops.integrateddynamics.block.BlockProxy;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;

import java.util.List;

/**
 * Copies a proxy id to the item.
 * @author rubensworks
 */
public class LootFunctionCopyProxyId extends LootItemConditionalFunction {

    public static final Codec<LootFunctionCopyProxyId> CODEC = RecordCodecBuilder.create(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyProxyId::new)
    );
    public static final LootItemFunctionType TYPE = new LootItemFunctionType(LootFunctionCopyProxyId.CODEC);

    protected LootFunctionCopyProxyId(List<LootItemCondition> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityProxy) {
            itemStack.getOrCreateTag().putInt(BlockProxy.NBT_ID, ((BlockEntityProxy) tile).getProxyId());
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }

}
