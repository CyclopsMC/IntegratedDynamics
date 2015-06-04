package org.cyclops.integrateddynamics.core.part;

import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;

/**
 * Object holder to refer to a block side and position.
 * @author rubensworks
 */
@Data(staticConstructor = "of")
public class PartPos {

    private final DimPos pos;
    private final EnumFacing side;

    public static PartPos of(World world, BlockPos pos, EnumFacing side) {
        return of(DimPos.of(world, pos), side);
    }

}
