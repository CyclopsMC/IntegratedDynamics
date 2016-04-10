package org.cyclops.integrateddynamics.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.api.item.IWrench;

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
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
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

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
                                  float hitX, float hitY, float hitZ, EnumHand hand) {
        Block block = world.getBlockState(pos).getBlock();
        if(block == null || player.isSneaking()) {
            return EnumActionResult.PASS;
        } else if(block.rotateBlock(world, pos, side)) {
            player.swingArm(hand);
            return !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
        return EnumActionResult.PASS;
    }
}
