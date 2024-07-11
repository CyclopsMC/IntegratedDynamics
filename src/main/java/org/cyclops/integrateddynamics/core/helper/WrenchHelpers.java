package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Helper methods related to items.
 * @author rubensworks
 */
public final class WrenchHelpers {

    public static final TagKey<Item> TAG_WRENCH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));

    /**
     * Checks if the given player can wrench something.
     * @param player The player.
     * @param heldItem The item the player is holding.
     * @param world The world in which the wrenching is happening.
     * @param pos The position that is being wrenched.
     * @param side The side that is being wrenched.
     * @return If the wrenching can continue with the held item.
     */
    public static boolean isWrench(Player player, ItemStack heldItem, Level world, BlockPos pos, @Nullable Direction side) {
        return heldItem.getItem().builtInRegistryHolder().tags().anyMatch(tag -> tag.equals(WrenchHelpers.TAG_WRENCH));
    }

    /**
     * Wrench a given position.
     * Requires the {@link WrenchHelpers#isWrench(Player, ItemStack, Level, BlockPos, Direction)}
     * to be passed.
     * Takes an extra parameter of any type that is forwarded to the wrench action.
     * @param player The player.
     * @param heldItem The item the player is holding.
     * @param world The world in which the wrenching is happening.
     * @param pos The position that is being wrenched.
     * @param side The side that is being wrenched.
     * @param action The actual wrench action.
     * @param parameter An extra parameter that is forwarded to the action.
     * @param <P> The type of parameter to pass.
     */
    public static <P> void wrench(Player player, ItemStack heldItem, Level world, BlockPos pos, Direction side, IWrenchAction<P> action, P parameter) {
        if (isWrench(player, heldItem, world, pos, side)) {
            action.onWrench(player, pos, parameter);
        }
    }

    /**
     * Wrench a given position.
     * Requires the {@link WrenchHelpers#isWrench(Player, ItemStack, Level, BlockPos, Direction)}
     * to be passed.
     * @param player The player.
     * @param heldItem The item the player is holding.
     * @param world The world in which the wrenching is happening.
     * @param pos The position that is being wrenched.
     * @param side The side that is being wrenched.
     * @param action The actual wrench action.
     */
    public static void wrench(Player player, ItemStack heldItem, Level world, BlockPos pos, Direction side, IWrenchAction<Void> action) {
        wrench(player, heldItem, world, pos, side, action, null);
    }

    /**
     * An action that can serve as wrenching.
     * @param <P> The type of parameter that is passed.
     */
    public static interface IWrenchAction<P> {

        /**
         * Called for the actual wrenching action.
         * @param player The player.
         * @param pos The position that is being wrenched.
         * @param parameter An extra parameter that is used to call this action.
         */
        public void onWrench(Player player, BlockPos pos, P parameter);

    }

    /**
     * An action that can serve as wrenching.
     */
    public static abstract class SimpleWrenchAction implements IWrenchAction<Void> {

        /**
         * Called for the actual wrenching action.
         * @param player The player.
         * @param pos The position that is being wrenched.
         * @param parameter An extra parameter that is used to call this action.
         */
        public void onWrench(Player player, BlockPos pos, Void parameter) {
            onWrench(player, pos);
        }

        /**
         * Called for the actual wrenching action.
         * @param player The player.
         * @param pos The position that is being wrenched.
         */
        public abstract void onWrench(Player player, BlockPos pos);

    }

}
