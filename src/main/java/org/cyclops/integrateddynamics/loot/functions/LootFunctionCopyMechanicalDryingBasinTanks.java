package org.cyclops.integrateddynamics.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalDryingBasin;

import java.util.List;

/**
 * Copies the mechanical drying basin tanks.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanks extends LootItemConditionalFunction {

    public static final Codec<LootFunctionCopyMechanicalDryingBasinTanks> CODEC = RecordCodecBuilder.create(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyMechanicalDryingBasinTanks::new)
    );
    public static final LootItemFunctionType TYPE = new LootItemFunctionType(LootFunctionCopyMechanicalDryingBasinTanks.CODEC);

    protected LootFunctionCopyMechanicalDryingBasinTanks(List<LootItemCondition> conditionsIn) {
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

}
