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
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMechanicalSqueezer;

import java.util.List;

/**
 * Copies the mechanical squeezer tank.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalSqueezerTank extends LootItemConditionalFunction {

    public static final Codec<LootFunctionCopyMechanicalSqueezerTank> CODEC = RecordCodecBuilder.create(
            builder -> commonFields(builder).apply(builder, LootFunctionCopyMechanicalSqueezerTank::new)
    );
    public static final LootItemFunctionType TYPE = new LootItemFunctionType(LootFunctionCopyMechanicalSqueezerTank.CODEC);

    protected LootFunctionCopyMechanicalSqueezerTank(List<LootItemCondition> conditionsIn) {
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

}
