package org.cyclops.integrateddynamics.part.aspect.read.world;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that takes the target block name.
 * @author rubensworks
 */
public class AspectReadStringWorldBlockName extends AspectReadStringWorldBase {

    @Override
    protected String getUnlocalizedStringWorldType() {
        return "blockname";
    }

    @Override
    protected ValueTypeString.ValueString getValue(PartTarget target, AspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
        return ValueTypeString.ValueString.of(block.isAir(dimPos.getWorld(), dimPos.getBlockPos()) ? "" : block.getLocalizedName());
    }
}
