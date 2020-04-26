package org.cyclops.integrateddynamics.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameter;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Set;

/**
 * A loot condition testing if a wrench is used.
 * @author rubensworks
 */
public class LootConditionMatchWrench implements ILootCondition {
    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.get(LootParameters.TOOL);
        Entity entity = lootContext.get(LootParameters.THIS_ENTITY);
        BlockPos blockPos = lootContext.get(LootParameters.POSITION);
        return itemStack != null
                && entity instanceof PlayerEntity
                && WrenchHelpers.isWrench((PlayerEntity) entity, itemStack, entity.getEntityWorld(), blockPos, null);
    }

    @Override
    public Set<LootParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootParameters.TOOL, LootParameters.THIS_ENTITY, LootParameters.POSITION);
    }

    public static class Serializer extends ILootCondition.AbstractSerializer<LootConditionMatchWrench> {

        protected Serializer() {
            super(new ResourceLocation(Reference.MOD_ID, "match_wrench"), LootConditionMatchWrench.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootConditionMatchWrench lootConditionMatchWrench, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootConditionMatchWrench deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootConditionMatchWrench();
        }
    }

}
