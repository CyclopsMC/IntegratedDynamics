package org.cyclops.integrateddynamics.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Set;

/**
 * A loot condition testing if a wrench is used.
 * @author rubensworks
 */
public class LootConditionMatchWrench implements ILootCondition {
    public static final LootConditionType TYPE = LootHelpers.registerCondition(new ResourceLocation(Reference.MOD_ID, "match_wrench"), new LootConditionMatchWrench.Serializer());

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParamOrNull(LootParameters.TOOL);
        Entity entity = lootContext.getParamOrNull(LootParameters.THIS_ENTITY);
        BlockPos blockPos = new BlockPos(lootContext.getParamOrNull(LootParameters.ORIGIN));
        return itemStack != null
                && entity instanceof PlayerEntity
                && WrenchHelpers.isWrench((PlayerEntity) entity, itemStack, entity.getCommandSenderWorld(), blockPos, null);
    }

    @Override
    public Set<LootParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootParameters.TOOL, LootParameters.THIS_ENTITY, LootParameters.ORIGIN);
    }

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer implements ILootSerializer<LootConditionMatchWrench> {

        @Override
        public void serialize(JsonObject jsonObject, LootConditionMatchWrench lootConditionMatchWrench, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootConditionMatchWrench deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootConditionMatchWrench();
        }
    }

}
