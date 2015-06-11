package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import org.cyclops.integrateddynamics.block.ReaderConfig;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A redstone reader part.
 * @author rubensworks
 */
public class PartTypeRedstoneReader extends PartTypeReadBase<PartTypeRedstoneReader, DefaultPartStateReader<PartTypeRedstoneReader>> {

    public PartTypeRedstoneReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_REDSTONE_LOW,
                Aspects.READ_BOOLEAN_REDSTONE_NONLOW,
                Aspects.READ_BOOLEAN_REDSTONE_HIGH
        ));
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
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick, int destroyStage) {
        return ReaderConfig._instance.getBlockInstance().getDefaultState();
    }

}
