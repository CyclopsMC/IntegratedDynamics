package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.block.FluidReaderConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An fluid reader part.
 * @author rubensworks
 */
public class PartTypeFluidReader extends PartTypeReadBase<PartTypeFluidReader, DefaultPartStateReader<PartTypeFluidReader>> {

    public PartTypeFluidReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_FLUID_FULL,
                Aspects.READ_BOOLEAN_FLUID_EMPTY,
                Aspects.READ_BOOLEAN_FLUID_NONEMPTY,
                Aspects.READ_BOOLEAN_FLUID_APPLICABLE,
                Aspects.READ_INTEGER_FLUID_AMOUNT,
                Aspects.READ_INTEGER_FLUID_CAPACITY,
                Aspects.READ_INTEGER_FLUID_TANKS,
                Aspects.READ_INTEGER_FLUID_DENSITY,
                Aspects.READ_INTEGER_FLUID_LUMINOSITY,
                Aspects.READ_INTEGER_FLUID_TEMPERATURE,
                Aspects.READ_INTEGER_FLUID_VISCOSITY,
                Aspects.READ_BOOLEAN_FLUID_GASEOUS
        ));
    }

    @Override
    public boolean isSolid(DefaultPartStateReader<PartTypeFluidReader> state) {
        return true;
    }

    @Override
    public DefaultPartStateReader<PartTypeFluidReader> constructDefaultState() {
        return new DefaultPartStateReader<PartTypeFluidReader>();
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        return FluidReaderConfig._instance.getBlockInstance().getDefaultState().withProperty(IgnoredBlock.FACING, side);
    }

}
