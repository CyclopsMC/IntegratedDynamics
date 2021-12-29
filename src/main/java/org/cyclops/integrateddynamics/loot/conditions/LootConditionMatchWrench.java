package org.cyclops.integrateddynamics.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Set;

/**
 * A loot condition testing if a wrench is used.
 * @author rubensworks
 */
public class LootConditionMatchWrench implements LootItemCondition {
    public static final LootItemConditionType TYPE = LootHelpers.registerCondition(new ResourceLocation(Reference.MOD_ID, "match_wrench"), new LootConditionMatchWrench.Serializer());

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockPos blockPos = new BlockPos(lootContext.getParamOrNull(LootContextParams.ORIGIN));
        return itemStack != null
                && entity instanceof Player
                && WrenchHelpers.isWrench((Player) entity, itemStack, entity.getCommandSenderWorld(), blockPos, null);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL, LootContextParams.THIS_ENTITY, LootContextParams.ORIGIN);
    }

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootConditionMatchWrench> {

        @Override
        public void serialize(JsonObject jsonObject, LootConditionMatchWrench lootConditionMatchWrench, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootConditionMatchWrench deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootConditionMatchWrench();
        }
    }

}
