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
import net.minecraft.world.phys.Vec3;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

/**
 * A loot condition testing if a wrench is used.
 * @author rubensworks
 */
public class LootConditionMatchWrench implements LootItemCondition {

    public static final MapCodec<LootConditionMatchWrench> CODEC = RecordCodecBuilder.mapCodec(b -> b.point(new LootConditionMatchWrench()));

    @Override
    public boolean test(LootContext lootContext) {
        ItemInstance itemInstance = lootContext.getOptional(LootContextParams.TOOL);
        Entity entity = lootContext.getOptional(LootContextParams.THIS_ENTITY);
        Vec3 origin = lootContext.getOptional(LootContextParams.ORIGIN);
        return origin != null
                && itemInstance instanceof ItemStack itemStack
                && entity instanceof Player
                && WrenchHelpers.isWrench((Player) entity, itemStack, entity.level(), BlockPos.containing(origin), null);
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

}
