package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockLog;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.Achievements;
import org.cyclops.integrateddynamics.GeneralConfig;

/**
 * Menril log block.
 * @author rubensworks
 */
public class BlockMenrilLog extends ConfigurableBlockLog {

    private static BlockMenrilLog _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockMenrilLog getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockMenrilLog(ExtendedConfig eConfig) {
        super(eConfig);
        this.setHardness(2.0F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SoundType getSoundType() {
        return SoundType.WOOD;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // TODO: remove this hack in the next major version
        // This was needed because of my derp: https://github.com/CyclopsMC/IntegratedDynamics/issues/65
        if ("0.5.0".equals(GeneralConfig.version)) {
            return super.getStateFromMeta(meta / 3);
        }
        return super.getStateFromMeta(meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        // TODO: remove this hack in the next major version
        if ("0.5.0".equals(GeneralConfig.version)) {
            return super.getMetaFromState(state) * 3;
        }
        return super.getMetaFromState(state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (super.removedByPlayer(state, world, pos, player, willHarvest)) {
            player.addStat(Achievements.MENEGLIN_DISCOVERY);
            return true;
        }
        return false;
    }
}
