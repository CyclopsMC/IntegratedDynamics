package net.minecraft.util.math.shapes;

/**
 * A {@link VoxelShape} with a public constructor.
 * @author rubensworks
 */
public abstract class VoxelShapeExtendable extends VoxelShape {

    public VoxelShapeExtendable(VoxelShapePart voxelShapePart) {
        super(voxelShapePart);
    }
}
