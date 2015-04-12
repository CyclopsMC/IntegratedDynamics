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
public class BlockCable extends ConfigurableBlockContainer {

    private static BlockCable _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BlockCable getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     * @param eConfig Config for this block.
     */
    public BlockCable(ExtendedConfig eConfig) {
        super(eConfig, Material.glass, TileMultipartTicking.class);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public int getRenderType() {
        return 3;
    }

}
