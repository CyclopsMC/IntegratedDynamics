package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import org.cyclops.integrateddynamics.block.WriterConfig;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.write.DefaultPartStateWriter;
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
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick, int destroyStage) {
        return WriterConfig._instance.getBlockInstance().getDefaultState();
    }

}
