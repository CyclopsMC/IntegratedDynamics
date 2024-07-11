package org.cyclops.integrateddynamics.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import java.util.Set;

/**
 * A loot condition testing if a wrench is used.
 * @author rubensworks
 */
public class LootConditionMatchWrench implements LootItemCondition {

    public static final MapCodec<LootConditionMatchWrench> CODEC = RecordCodecBuilder.mapCodec(b -> b.point(new LootConditionMatchWrench()));
    public static final LootItemConditionType TYPE = new LootItemConditionType(LootConditionMatchWrench.CODEC);

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        BlockPos blockPos = BlockPos.containing(lootContext.getParamOrNull(LootContextParams.ORIGIN));
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

}
