package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public final class FuelHelpers {

    private FuelHelpers() {
    }

    /**
     * @param itemStack An item.
     * @return If the item can be burned as fuel in a furnace-like block.
     */
    public static boolean isFuel(ItemStack itemStack) {
        return itemStack.has(DataComponents.COOKING_FUEL);
    }

    /**
     * Resolve the burn time of an item inside a concrete furnace-like block.
     * @param level The server level.
     * @param blockEntity The block entity burning the fuel.
     * @param itemStack The fuel.
     * @return The burn time in ticks, or 0 if the item is not fuel.
     */
    public static int getBurnTime(ServerLevel level, BlockEntity blockEntity, SlotProvider container, ItemStack itemStack) {
        return ResolvableInt.getFromItem(itemStack, DataComponents.COOKING_FUEL, CookingFuel::burnTime,
                createContext(level, blockEntity.getBlockState(), blockEntity, container, blockEntity.getBlockPos()), 0);
    }

    /**
     * Resolve the burn time of an item as a plain vanilla furnace would.
     * Used where no burning block is known, such as for the burn time operator.
     * @param level The server level.
     * @param itemStack The fuel.
     * @return The burn time in ticks, or 0 if the item is not fuel.
     */
    public static int getFurnaceBurnTime(@Nullable ServerLevel level, ItemStack itemStack) {
        if (level == null || !isFuel(itemStack)) {
            return 0;
        }
        BlockState blockState = Blocks.FURNACE.defaultBlockState();
        FurnaceBlockEntity furnace = new FurnaceBlockEntity(BlockPos.ZERO, blockState);
        return ResolvableInt.getFromItem(itemStack, DataComponents.COOKING_FUEL, CookingFuel::burnTime,
                createContext(level, blockState, furnace, furnace, BlockPos.ZERO), 0);
    }

    private static LootContext createContext(ServerLevel level, BlockState blockState, BlockEntity blockEntity,
                                             SlotProvider container, BlockPos pos) {
        return new LootContext.Builder(
                new LootParams.Builder(level)
                        .withParameter(LootContextParams.BLOCK_STATE, blockState)
                        .withParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.CONTAINER, container)
                        .create(LootContextParamSets.CONTAINER_PROCESS)
        ).create(Optional.empty());
    }

}
