package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.block.state.IBlockState;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadObjectBlockBase;

/**
 * Read a block from the world.
 * @author rubensworks
 */
public class AspectReadObjectBlockWorld extends AspectReadObjectBlockBase {

    @Override
    protected String getUnlocalizedBlockType() {
        return "world";
    }

    @Override
    protected ValueObjectTypeBlock.ValueBlock getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        IBlockState blockState = dimPos.getWorld().getBlockState(dimPos.getBlockPos());
        return ValueObjectTypeBlock.ValueBlock.of(blockState);
    }
}
