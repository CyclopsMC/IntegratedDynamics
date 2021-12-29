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
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalSqueezer;

/**
 * Copies the mechanical squeezer tank.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalSqueezerTank extends LootItemConditionalFunction {
    public static final LootItemFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_squeezer_tank"), new LootFunctionCopyMechanicalSqueezerTank.Serializer());

    protected LootFunctionCopyMechanicalSqueezerTank(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        BlockEntity tile = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityMechanicalSqueezer) {
            itemStack.getOrCreateTag().put(BlockMechanicalSqueezer.NBT_TANK, ((BlockEntityMechanicalSqueezer) tile).getTank().writeToNBT(new CompoundTag()));
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

    public static class Serializer extends LootItemConditionalFunction.Serializer<LootFunctionCopyMechanicalSqueezerTank> {
        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalSqueezerTank lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalSqueezerTank deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] conditionsIn) {
            return new LootFunctionCopyMechanicalSqueezerTank(conditionsIn);
        }
    }

}
