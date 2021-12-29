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
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockProxy;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;

/**
 * Copies a proxy id to the item.
 * @author rubensworks
 */
public class LootFunctionCopyProxyId extends LootItemConditionalFunction {
    public static final LootItemFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_proxy_id"), new LootFunctionCopyProxyId.Serializer());

    protected LootFunctionCopyProxyId(LootItemCondition[] conditionsIn) {
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

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LootFunctionCopyProxyId> {
        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyProxyId lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyProxyId deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] conditionsIn) {
            return new LootFunctionCopyProxyId(conditionsIn);
        }
    }

}
