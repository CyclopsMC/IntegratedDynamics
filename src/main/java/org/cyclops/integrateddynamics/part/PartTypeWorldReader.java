package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.block.WorldReaderConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An world reader part.
 * @author rubensworks
 */
public class PartTypeWorldReader extends PartTypeReadBase<PartTypeWorldReader, DefaultPartStateReader<PartTypeWorldReader>> {

    public PartTypeWorldReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_WORLD_BLOCK,
                Aspects.READ_BOOLEAN_WORLD_ENTITY,
                Aspects.READ_BOOLEAN_WORLD_MOB,
                Aspects.READ_BOOLEAN_WORLD_PLAYER,
                Aspects.READ_BOOLEAN_WORLD_ITEM,
                Aspects.READ_INTEGER_WORLD_ENTITY,
                Aspects.READ_INTEGER_WORLD_TIME,
                Aspects.READ_INTEGER_WORLD_TOTALTIME
        ));
    }

    @Override
    public boolean isSolid(DefaultPartStateReader<PartTypeWorldReader> state) {
        return true;
    }

    @Override
    public DefaultPartStateReader<PartTypeWorldReader> constructDefaultState() {
        return new DefaultPartStateReader<PartTypeWorldReader>();
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        return WorldReaderConfig._instance.getBlockInstance().getDefaultState().withProperty(IgnoredBlock.FACING, side);
    }

}
