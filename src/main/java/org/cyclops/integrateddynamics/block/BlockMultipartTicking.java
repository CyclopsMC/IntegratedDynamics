package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * A block that is build up from different parts.
 * This block refers to a ticking tile entity.
 * @author rubensworks
 */
public class BlockMultipartTicking extends ConfigurableBlockContainer {

    private static BlockMultipartTicking _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockMultipartTicking getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     * @param eConfig Config for this block.
     */
    public BlockMultipartTicking(ExtendedConfig eConfig) {
        super(eConfig, Material.glass, TileMultipartTicking.class);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
    }

}
