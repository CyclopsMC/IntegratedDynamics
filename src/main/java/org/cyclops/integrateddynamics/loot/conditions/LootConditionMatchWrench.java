package org.cyclops.integrateddynamics.loot.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

/**
 * A loot condition testing if a wrench is used.
 * @author rubensworks
 */
public class LootConditionMatchWrench implements LootItemCondition {

    public static final MapCodec<LootConditionMatchWrench> CODEC = RecordCodecBuilder.mapCodec(b -> b.point(new LootConditionMatchWrench()));

    @Override
    public boolean test(LootContext lootContext) {
        ItemInstance itemInstance = lootContext.getOptionalParameter(LootContextParams.TOOL);
        Entity entity = lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY);
        BlockPos blockPos = BlockPos.containing(lootContext.getParameter(LootContextParams.ORIGIN));
        return itemInstance instanceof ItemStack itemStack
                && entity instanceof Player
                && WrenchHelpers.isWrench((Player) entity, itemStack, entity.level(), blockPos, null);
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

}
