package org.cyclops.integrateddynamics.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockTorch;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

import java.util.Random;

/**
 * Config for the Menril Torch.
 * @author rubensworks
 *
 */
public class BlockMenrilTorchConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilTorchConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilTorchConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menrilTorch",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ConfigurableBlockTorch(this) {
            @Override
            public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
                // No particles
            }
        };
    }
    
}
