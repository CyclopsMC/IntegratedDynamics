package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Maps;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.AxeItem;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Stripped Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilWoodStrippedConfig extends BlockConfig {

    public BlockMenrilWoodStrippedConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_wood_stripped",
                eConfig -> Blocks.createLogBlock(MaterialColor.CYAN, MaterialColor.CYAN),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }
    
    @Override
    public void onRegistered() {

    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        BlockHelpers.setFireInfo(getInstance(), 5, 20);
        AxeItem.BLOCK_STRIPPING_MAP = Maps.newHashMap(AxeItem.BLOCK_STRIPPING_MAP);
        AxeItem.BLOCK_STRIPPING_MAP.put(RegistryEntries.BLOCK_MENRIL_WOOD, RegistryEntries.BLOCK_MENRIL_WOOD_STRIPPED);
    }

}
