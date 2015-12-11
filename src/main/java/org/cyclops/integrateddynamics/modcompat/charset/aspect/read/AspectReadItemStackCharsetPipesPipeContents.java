package org.cyclops.integrateddynamics.modcompat.charset.aspect.read;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadObjectItemStackBase;
import pl.asie.charset.api.pipes.IPipe;

/**
 * Read the itemstack inside a pipe.
 * @author rubensworks
 */
public class AspectReadItemStackCharsetPipesPipeContents extends AspectReadObjectItemStackBase {

    @Override
    protected String getUnlocalizedItemStackType() {
        return "charsetpipe.contents";
    }

    @Override
    protected ValueObjectTypeItemStack.ValueItemStack getValue(PartTarget target, IAspectProperties properties) {
        DimPos pos = target.getTarget().getPos();
        IPipe pipe = TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IPipe.class);
        ItemStack itemStack = null;
        if(pipe != null) {
            itemStack = pipe.getTravellingStack(null);
        }
        return ValueObjectTypeItemStack.ValueItemStack.of(itemStack);
    }
}
