package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * A block that is build up from different parts.
 * This block refers to a ticking tile entity.
 * @author rubensworks
 */
public abstract class BlockMultipartTicking extends ConfigurableBlockContainer {

    /**
     * Make a new block instance.
     * @param eConfig Config for this block.
     * @param material The material for this block.
     */
    public BlockMultipartTicking(ExtendedConfig eConfig, Material material) {
        super(eConfig, material, TileMultipartTicking.class);
    }

}
