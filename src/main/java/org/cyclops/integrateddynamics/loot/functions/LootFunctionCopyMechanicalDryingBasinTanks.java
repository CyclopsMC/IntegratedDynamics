package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.functions.ILootFunction;
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalDryingBasin;

/**
 * Copies the mechanical drying basin tanks.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanks extends LootFunction {
    public static final LootFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_drying_basin_tanks"), new LootFunctionCopyMechanicalDryingBasinTanks.Serializer());

    protected LootFunctionCopyMechanicalDryingBasinTanks(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack doApply(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileMechanicalDryingBasin) {
            itemStack.getOrCreateTag().put(BlockMechanicalDryingBasin.NBT_TANK_IN, ((TileMechanicalDryingBasin) tile).getTankInput().writeToNBT(new CompoundNBT()));
            itemStack.getOrCreateTag().put(BlockMechanicalDryingBasin.NBT_TANK_OUT, ((TileMechanicalDryingBasin) tile).getTankOutput().writeToNBT(new CompoundNBT()));
        }
        return itemStack;
    }

    @Override
    public LootFunctionType getFunctionType() {
        return TYPE;
    }

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionCopyMechanicalDryingBasinTanks> {
        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalDryingBasinTanks lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalDryingBasinTanks deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, ILootCondition[] conditionsIn) {
            return new LootFunctionCopyMechanicalDryingBasinTanks(conditionsIn);
        }
    }

}
