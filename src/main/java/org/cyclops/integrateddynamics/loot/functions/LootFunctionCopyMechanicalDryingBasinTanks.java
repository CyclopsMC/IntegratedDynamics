package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
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
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;

/**
 * Copies the mechanical drying basin tanks.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanks extends LootItemConditionalFunction {
    public static final LootItemFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_drying_basin_tanks"), new LootFunctionCopyMechanicalDryingBasinTanks.Serializer());

    protected LootFunctionCopyMechanicalDryingBasinTanks(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityMechanicalDryingBasin) {
            itemStack.getOrCreateTag().put(BlockMechanicalDryingBasin.NBT_TANK_IN, ((BlockEntityMechanicalDryingBasin) tile).getTankInput().writeToNBT(new CompoundTag()));
            itemStack.getOrCreateTag().put(BlockMechanicalDryingBasin.NBT_TANK_OUT, ((BlockEntityMechanicalDryingBasin) tile).getTankOutput().writeToNBT(new CompoundTag()));
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

    public static class Serializer extends LootItemConditionalFunction.Serializer<LootFunctionCopyMechanicalDryingBasinTanks> {
        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalDryingBasinTanks lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalDryingBasinTanks deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] conditionsIn) {
            return new LootFunctionCopyMechanicalDryingBasinTanks(conditionsIn);
        }
    }

}
