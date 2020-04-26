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
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalDryingBasin;

/**
 * Copies the mechanical drying basin tanks.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanks implements ILootFunction {

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileMechanicalDryingBasin) {
            itemStack.getOrCreateTag().put(BlockMechanicalDryingBasin.NBT_TANK_IN, ((TileMechanicalDryingBasin) tile).getTankInput().writeToNBT(new CompoundNBT()));
            itemStack.getOrCreateTag().put(BlockMechanicalDryingBasin.NBT_TANK_OUT, ((TileMechanicalDryingBasin) tile).getTankOutput().writeToNBT(new CompoundNBT()));
        }
        return itemStack;
    }

    public static class Serializer extends ILootFunction.Serializer<LootFunctionCopyMechanicalDryingBasinTanks> {

        public Serializer() {
            super(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_drying_basin_tanks"), LootFunctionCopyMechanicalDryingBasinTanks.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalDryingBasinTanks lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalDryingBasinTanks deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootFunctionCopyMechanicalDryingBasinTanks();
        }
    }

}
