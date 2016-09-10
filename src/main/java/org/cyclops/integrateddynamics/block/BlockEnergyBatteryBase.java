package org.cyclops.integrateddynamics.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.block.IEnergyBatteryFacade;
import org.cyclops.integrateddynamics.api.block.IEnergyContainerBlock;
import org.cyclops.integrateddynamics.core.block.BlockContainerCabled;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

/**
 * A block that holds energy.
 *
 * @author rubensworks
 */
public abstract class BlockEnergyBatteryBase extends BlockContainerCabled implements IEnergyBatteryFacade, IEnergyContainerBlock {

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockEnergyBatteryBase(ExtendedConfig eConfig) {
        super(eConfig, TileEnergyBattery.class);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        TileHelpers.getSafeTile(world, pos, TileEnergyBattery.class).updateBlockState();
    }

    @Override
    public IEnergyBattery getEnergyBattery(World world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, TileEnergyBattery.class);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public String getEneryContainerNBTName() {
        return "energy";
    }

    public abstract boolean isCreative();

}
