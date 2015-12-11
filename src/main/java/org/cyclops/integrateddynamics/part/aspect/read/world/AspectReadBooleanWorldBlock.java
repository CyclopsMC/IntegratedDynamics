package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;

/**
 * Aspect that checks if the target has a block.
 * @author rubensworks
 */
public class AspectReadBooleanWorldBlock extends AspectReadBooleanWorldBase {

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "block";
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
        return ValueTypeBoolean.ValueBoolean.of(block != Blocks.air);
    }
}
