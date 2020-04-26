package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * Copies the mechanical squeezer tank.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalSqueezerTank implements ILootFunction {

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileMechanicalSqueezer) {
            itemStack.getOrCreateTag().put(BlockMechanicalSqueezer.NBT_TANK, ((TileMechanicalSqueezer) tile).getTank().writeToNBT(new CompoundNBT()));
        }
        return itemStack;
    }

    public static class Serializer extends ILootFunction.Serializer<LootFunctionCopyMechanicalSqueezerTank> {

        public Serializer() {
            super(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_squeezer_tank"), LootFunctionCopyMechanicalSqueezerTank.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalSqueezerTank lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalSqueezerTank deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootFunctionCopyMechanicalSqueezerTank();
        }
    }

}
