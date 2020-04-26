package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockProxy;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Copies a proxy id to the item.
 * @author rubensworks
 */
public class LootFunctionCopyProxyId implements ILootFunction {

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileProxy) {
            itemStack.getOrCreateTag().putInt(BlockProxy.NBT_ID, ((TileProxy) tile).getProxyId());
        }
        return itemStack;
    }

    public static class Serializer extends ILootFunction.Serializer<LootFunctionCopyProxyId> {

        public Serializer() {
            super(new ResourceLocation(Reference.MOD_ID, "copy_proxy_id"), LootFunctionCopyProxyId.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyProxyId lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyProxyId deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootFunctionCopyProxyId();
        }
    }

}
