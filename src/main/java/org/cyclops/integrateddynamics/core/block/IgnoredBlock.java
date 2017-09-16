package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

/**
 * A block that is not visible to the player.
 * Just used for providing models, until a better way for doing this comes around.
 * @author rubensworks
 */
public class IgnoredBlock extends ConfigurableBlock {

    @BlockProperty(excludeFromMeta = true)
    public static final PropertyEnum FACING = PropertyDirection.create("facing", EnumFacing.class);

    /**
     * Make a new blockState instance.
     *
     * @param eConfig  Config for this blockState.
     */
    public IgnoredBlock(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.GLASS);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        // Don't show block in creative tab
    }

}
