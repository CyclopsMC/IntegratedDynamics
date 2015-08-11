package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.block.RedstoneReaderConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.IReadRedstoneComponent;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.ReadRedstoneComponent;

/**
 * A redstone reader part.
 * @author rubensworks
 */
public class PartTypeRedstoneReader extends PartTypeReadBase<PartTypeRedstoneReader, DefaultPartStateReader<PartTypeRedstoneReader>> {

    private static final IReadRedstoneComponent READ_REDSTONE_COMPONENT = new ReadRedstoneComponent();

    public PartTypeRedstoneReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_REDSTONE_LOW,
                Aspects.READ_BOOLEAN_REDSTONE_NONLOW,
                Aspects.READ_BOOLEAN_REDSTONE_HIGH,
                Aspects.READ_INTEGER_REDSTONE
        ));
    }

    @Override
    public boolean isSolid(DefaultPartStateReader<PartTypeRedstoneReader> state) {
        return true;
    }

    @Override
    public DefaultPartStateReader<PartTypeRedstoneReader> constructDefaultState() {
        return new DefaultPartStateReader<PartTypeRedstoneReader>();
    }

    @Override
    public int getUpdateInterval(DefaultPartStateReader<PartTypeRedstoneReader> state) {
        return 10;
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        return RedstoneReaderConfig._instance.getBlockInstance().getDefaultState().withProperty(IgnoredBlock.FACING, side);
    }

    @Override
    public void onNetworkAddition(Network network, PartTarget target, DefaultPartStateReader<PartTypeRedstoneReader> state) {
        super.onNetworkAddition(network, target, state);
        READ_REDSTONE_COMPONENT.setAllowRedstoneInput(target, true);
    }

    @Override
    public void onNetworkRemoval(Network network, PartTarget target, DefaultPartStateReader<PartTypeRedstoneReader> state) {
        super.onNetworkRemoval(network, target, state);
        READ_REDSTONE_COMPONENT.setAllowRedstoneInput(target, false);
    }

}
