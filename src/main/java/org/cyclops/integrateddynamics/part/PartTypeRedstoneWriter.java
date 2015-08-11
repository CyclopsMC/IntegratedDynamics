package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.block.RedstoneWriterConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.DefaultPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A redstone writer part.
 * @author rubensworks
 */
public class PartTypeRedstoneWriter extends PartTypeWriteBase<PartTypeRedstoneWriter, DefaultPartStateWriter<PartTypeRedstoneWriter>> {

    public PartTypeRedstoneWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.WRITE_BOOLEAN_REDSTONE,
                Aspects.WRITE_INTEGER_REDSTONE
        ));
    }

    @Override
    public DefaultPartStateWriter<PartTypeRedstoneWriter> constructDefaultState() {
        return new DefaultPartStateWriter<PartTypeRedstoneWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

    @Override
    public int getUpdateInterval(DefaultPartStateWriter<PartTypeRedstoneWriter> state) {
        return 10;
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        IPartStateWriter state = (IPartStateWriter) tile.getPartState(side);
        IgnoredBlockStatus.Status status = IgnoredBlockStatus.Status.INACTIVE;
        IAspectWrite aspectWrite = state.getActiveAspect();
        if(aspectWrite != null) {
            if(state.getErrors(aspectWrite).isEmpty()) {
                status = IgnoredBlockStatus.Status.ACTIVE;
            } else {
                status = IgnoredBlockStatus.Status.ERROR;
            }
        }
        return RedstoneWriterConfig._instance.getBlockInstance().getDefaultState().withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
    }

}
