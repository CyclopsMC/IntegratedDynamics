package org.cyclops.integrateddynamics.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0; // TODO: abstract
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState(); // TODO: abstract
    }

}
