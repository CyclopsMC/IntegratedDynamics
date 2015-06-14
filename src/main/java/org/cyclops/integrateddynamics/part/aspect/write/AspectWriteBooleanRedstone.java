package org.cyclops.integrateddynamics.part.aspect.write;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.block.IDynamicRedstoneBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;

/**
 * Base class for boolean redstone aspects.
 * @author rubensworks
 */
public class AspectWriteBooleanRedstone extends AspectWriteBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "redstone";
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeBoolean.ValueBoolean> variable) {
        ValueTypeBoolean.ValueBoolean value = variable.getValue();
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstoneBlock block = getDynamicRedstoneBlock(dimPos);
        if(block != null) {
            int level = value.getRawValue() ? 15 : 0;
            block.setRedstoneLevel(dimPos.getWorld(), dimPos.getBlockPos(), target.getCenter().getSide(), level);
        }
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType,
                                                                                              PartTarget target, S state) {
        super.onDeactivate(partType, target, state);
        DimPos dimPos = target.getCenter().getPos();
        IDynamicRedstoneBlock block = getDynamicRedstoneBlock(dimPos);
        if(block != null) {
            block.disableRedstoneAt(dimPos.getWorld(), dimPos.getBlockPos(), target.getCenter().getSide());
        }
    }

    protected IDynamicRedstoneBlock getDynamicRedstoneBlock(DimPos dimPos) {
        Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
        if(block instanceof IDynamicRedstoneBlock) {
            return (IDynamicRedstoneBlock) block;
        }
        return null;
    }
}
