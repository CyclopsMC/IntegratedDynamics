package org.cyclops.integrateddynamics.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockTorch;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

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
                "menril_torch",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockTorch initSubInstance() {
        return new ConfigurableBlockTorch(this) {
            @Override
            public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
                // No particles
            }
        };
    }

    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_TORCH;
    }

}
