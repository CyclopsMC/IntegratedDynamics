package org.cyclops.integrateddynamics.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.core.item.IWrench;

/**
 * The default wrench for this mod.
 * @author rubensworks
 */
public class ItemWrench extends ConfigurableItem implements IWrench {

    private static ItemWrench _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemWrench getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemWrench(ExtendedConfig eConfig) {
        super(eConfig);
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean canUse(EntityPlayer player, BlockPos pos) {
        return true;
    }

    @Override
    public void beforeUse(EntityPlayer player, BlockPos pos) {

    }

    @Override
    public void afterUse(EntityPlayer player, BlockPos pos) {

    }
}
