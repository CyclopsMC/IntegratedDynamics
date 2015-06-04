package org.cyclops.integrateddynamics.part;

import net.minecraft.block.state.IBlockState;
import org.cyclops.integrateddynamics.block.WriterConfig;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

/**
 * A redstone writer part.
 * @author rubensworks
 */
public class PartTypeRedstoneWriter extends PartTypeBase<PartTypeRedstoneWriter, DefaultPartState<PartTypeRedstoneWriter>>
        implements IPartTypeWriter<PartTypeRedstoneWriter, DefaultPartState<PartTypeRedstoneWriter>> {

    public PartTypeRedstoneWriter(String name) {
        super(name);
        // TODO: register input aspects
        /*AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.READ_BOOLEAN_REDSTONE_LOW,
                Aspects.READ_BOOLEAN_REDSTONE_NONLOW,
                Aspects.READ_BOOLEAN_REDSTONE_HIGH
        ));*/
    }

    @Override
    public DefaultPartState<PartTypeRedstoneWriter> constructDefaultState() {
        return new DefaultPartState<PartTypeRedstoneWriter>();
    }

    @Override
    public int getUpdateInterval(DefaultPartState<PartTypeRedstoneWriter> state) {
        return 10;
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick, int destroyStage) {
        return WriterConfig._instance.getBlockInstance().getDefaultState();
    }

}
