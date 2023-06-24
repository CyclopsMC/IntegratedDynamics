package org.cyclops.integrateddynamics.block;


import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Collection;
import java.util.Collections;

/**
 * Config for {@link BlockFluidLiquidChorus}.
 * @author rubensworks
 *
 */
public class BlockFluidLiquidChorusConfig extends BlockConfig {

    public BlockFluidLiquidChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "block_liquid_chorus",
                eConfig -> new BlockFluidLiquidChorus(Block.Properties.of()
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
