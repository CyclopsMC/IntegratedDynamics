package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockTorch;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

import java.util.Random;

/**
 * Config for the Menril Stone Torch.
 * @author rubensworks
 *
 */
public class BlockMenrilTorchStoneConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilTorchStoneConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilTorchStoneConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menril_torch_stone",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ConfigurableBlockTorch(this) {
            @Override
            public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
                // No particles
            }

            @Override
            public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
                return SoundType.STONE;
            }
        };
    }

    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_TORCH;
    }
}
