package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

import java.util.List;

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
    public IgnoredBlock(ExtendedConfig eConfig) {
        super(eConfig, Material.GLASS);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        // Don't show block in creative tab
    }

}
