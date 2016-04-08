package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.block.IEnergyBatteryFacade;
import org.cyclops.integrateddynamics.api.block.IEnergyContainerBlock;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.block.BlockContainerCabled;
import org.cyclops.integrateddynamics.network.EnergyBatteryNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import java.util.Collection;

/**
 * A block that holds energy.
 *
 * @author rubensworks
 */
public abstract class BlockEnergyBatteryBase extends BlockContainerCabled implements IEnergyBatteryFacade, IEnergyContainerBlock {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

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
    public Collection<INetworkElement<IPartNetwork>> createNetworkElements(World world, BlockPos blockPos) {
        return Sets.<INetworkElement<IPartNetwork>>newHashSet((INetworkElement) new EnergyBatteryNetworkElement(DimPos.of(world, blockPos)));
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
