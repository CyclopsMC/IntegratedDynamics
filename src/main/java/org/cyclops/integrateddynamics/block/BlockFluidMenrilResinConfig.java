package org.cyclops.integrateddynamics.block;


import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for {@link BlockFluidMenrilResin}.
 * @author rubensworks
 *
 */
public class BlockFluidMenrilResinConfig extends BlockConfig {

    public BlockFluidMenrilResinConfig() {
        super(
                IntegratedDynamics._instance,
                "block_menril_resin",
                eConfig -> new BlockFluidMenrilResin(Block.Properties.of(Material.WATER)
                        .noCollission()
                        .strength(100.0F)),
                (eConfig, block) -> new BlockItem(block, new Item.Properties())
        );
    }

    @Override
    protected Collection<ItemStack> defaultCreativeTabEntries() {
        return Collections.emptyList();
    }

}
