package org.cyclops.integrateddynamics.core.helper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import org.cyclops.integrateddynamics.api.item.IWrench;

/**
 * Helper methods related to items.
 * @author rubensworks
 */
public final class WrenchHelpers {

    /**
     * Checks if the given player can wrench something.
     * @param player The player.
     * @param pos The position that is being wrenched.
     * @return If the wrenching can continue with the held item.
     */
    public static boolean isWrench(EntityPlayer player, BlockPos pos) {
        if(player.getCurrentEquippedItem() == null) {
            return false;
        }
        Item item = player.getCurrentEquippedItem().getItem();
        // TODO: add support for other mod wrenches, like the one from BC.
        return item instanceof IWrench && ((IWrench) item).canUse(player, pos);
    }

    /**
     * Wrench a given position.
     * Requires the {@link WrenchHelpers#isWrench(net.minecraft.entity.player.EntityPlayer, net.minecraft.util.BlockPos)}
     * to be passed.
     * Takes an extra parameter of any type that is forwarded to the wrench action.
     * @param player The player.
     * @param pos The position that is being wrenched.
     * @param action The actual wrench action.
     * @param parameter An extra parameter that is forwarded to the action.
     * @param <P> The type of parameter to pass.
     */
    public static <P> void wrench(EntityPlayer player, BlockPos pos, IWrenchAction<P> action, P parameter) {
        Item item = player.getCurrentEquippedItem().getItem();
        // TODO: add support for other mod wrenches, like the one from BC.
        if(item instanceof IWrench) {
            ((IWrench) item).beforeUse(player, pos);
            action.onWrench(player, pos, parameter);
            ((IWrench) item).afterUse(player, pos);
        }
    }

    /**
     * Wrench a given position.
     * Requires the {@link WrenchHelpers#isWrench(net.minecraft.entity.player.EntityPlayer, net.minecraft.util.BlockPos)}
     * to be passed.
     * @param player The player.
     * @param pos The position that is being wrenched.
     * @param action The actual wrench action.
     */
    public static void wrench(EntityPlayer player, BlockPos pos, IWrenchAction<Void> action) {
        wrench(player, pos, action, null);
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
        public void onWrench(EntityPlayer player, BlockPos pos, P parameter);

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
        public void onWrench(EntityPlayer player, BlockPos pos, Void parameter) {
            onWrench(player, pos);
        }

        /**
         * Called for the actual wrenching action.
         * @param player The player.
         * @param pos The position that is being wrenched.
         */
        public abstract void onWrench(EntityPlayer player, BlockPos pos);

    }

}
