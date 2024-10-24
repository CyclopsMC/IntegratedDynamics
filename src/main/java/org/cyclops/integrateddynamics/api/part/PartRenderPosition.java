package org.cyclops.integrateddynamics.api.part;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MatrixHelpers;
import org.cyclops.integrateddynamics.block.shapes.CollisionContextBlockSupport;
import org.cyclops.integrateddynamics.client.model.CableModel;

import java.util.Arrays;

/**
 * Render position definition of a part.
 * @author rubensworks
 */
public class PartRenderPosition {

    public static final PartRenderPosition NONE = new PartRenderPosition(-1, -1, -1, -1);

    private final float depthFactor;
    private final float widthFactor;
    private final float heightFactor;
    private final float widthFactorSide;
    private final float heightFactorSide;
    private final EnumFacingMap<VoxelShape> sidedCableCollisionBoxes;
    private final EnumFacingMap<VoxelShape> collisionBoxes;
    private final EnumFacingMap<VoxelShape> collisionBoxesBlockSupport;

    public PartRenderPosition(float selectionDepthFactor, float depthFactor, float widthFactor, float heightFactor) {
        this(selectionDepthFactor, depthFactor, widthFactor, heightFactor, widthFactor, heightFactor);
    }

    public PartRenderPosition(float selectionDepthFactor, float depthFactor, float widthFactor, float heightFactor,
                              float widthFactorSide, float heightFactorSide) {
        this.depthFactor = depthFactor;
        this.widthFactor = widthFactor;
        this.heightFactor = heightFactor;
        this.widthFactorSide = widthFactorSide;
        this.heightFactorSide = heightFactorSide;
        float[][] sidedCableCollisionBoxesRaw = new float[][]{
                {CableModel.MIN, selectionDepthFactor, CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX}, // DOWN
                {CableModel.MIN, CableModel.MAX, CableModel.MIN, CableModel.MAX, 1 - selectionDepthFactor, CableModel.MAX}, // UP
                {CableModel.MIN, CableModel.MIN, selectionDepthFactor, CableModel.MAX, CableModel.MAX, CableModel.MIN}, // NORTH
                {CableModel.MIN, CableModel.MAX, CableModel.MAX, CableModel.MAX, CableModel.MIN, 1 - selectionDepthFactor}, // SOUTH
                {selectionDepthFactor, CableModel.MIN, CableModel.MIN, CableModel.MIN, CableModel.MAX, CableModel.MAX}, // WEST
                {CableModel.MAX, CableModel.MIN, CableModel.MIN, 1 - selectionDepthFactor, CableModel.MAX, CableModel.MAX}, // EAST
        };

        sidedCableCollisionBoxes = EnumFacingMap.newMap();
        for (Direction side : Direction.values()) {
            float[] b = sidedCableCollisionBoxesRaw[side.ordinal()];
            sidedCableCollisionBoxes.put(side, Shapes.create(new AABB(b[0], b[1], b[2], b[3], b[4], b[5])));
        }

        float min = (1 - widthFactor) / 2 + 0.0025F;
        float max = (1 - widthFactor) / 2 + widthFactor - 0.0025F;
        float[][] collisionBoxesRaw = new float[][]{
                {min, max}, {0.005F, selectionDepthFactor}, {min, max}
        };
        float[][] collisionBoxesBlockSupportRaw = new float[][]{
                {0, 1}, {0, selectionDepthFactor}, {0, 1}
        };
        collisionBoxes = EnumFacingMap.newMap();
        collisionBoxesBlockSupport = EnumFacingMap.newMap();
        for (Direction side : Direction.values()) {
            // Copy bounds
            float[][] bounds = new float[collisionBoxesRaw.length][collisionBoxesRaw[0].length];
            for (int i = 0; i < bounds.length; i++)
                bounds[i] = Arrays.copyOf(collisionBoxesRaw[i], collisionBoxesRaw[i].length);

            // Transform bounds
            MatrixHelpers.transform(bounds, side);
            collisionBoxes.put(side, Shapes.create(new AABB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1])));

            // Copy bounds block support
            float[][] boundsBS = new float[collisionBoxesBlockSupportRaw.length][collisionBoxesBlockSupportRaw[0].length];
            for (int i = 0; i < boundsBS.length; i++)
                boundsBS[i] = Arrays.copyOf(collisionBoxesBlockSupportRaw[i], collisionBoxesBlockSupportRaw[i].length);

            // Transform bounds block support
            MatrixHelpers.transform(boundsBS, side);
            collisionBoxesBlockSupport.put(side, Shapes.create(new AABB(boundsBS[0][0], boundsBS[1][0], boundsBS[2][0], boundsBS[0][1], boundsBS[1][1], boundsBS[2][1])));
        }
    }

    public float getDepthFactor() {
        return depthFactor;
    }

    public float getWidthFactor() {
        return widthFactor;
    }

    public float getHeightFactor() {
        return heightFactor;
    }

    public VoxelShape getSidedCableBoundingBox(Direction side) {
        return sidedCableCollisionBoxes.get(side);
    }

    public VoxelShape getBoundingBox(Direction side, CollisionContext context) {
        if (context instanceof CollisionContextBlockSupport) {
            return collisionBoxesBlockSupport.get(side);
        }
        return collisionBoxes.get(side);
    }

    @Deprecated // TODO: rm in next major
    public VoxelShape getBoundingBox(Direction side) {
        return this.getBoundingBox(side, CollisionContext.empty());
    }

    public float getWidthFactorSide() {
        return widthFactorSide;
    }

    public float getHeightFactorSide() {
        return heightFactorSide;
    }

    @Override
    public String toString() {
        return "PartRenderPosition{" +
                "depthFactor=" + depthFactor +
                ", widthFactor=" + widthFactor +
                ", heightFactor=" + heightFactor +
                ", widthFactorSide=" + widthFactorSide +
                ", heightFactorSide=" + heightFactorSide +
                '}';
    }

    public String toCompactString() {
        return "df=" + depthFactor +
                ",wf=" + widthFactor +
                ",hf=" + heightFactor +
                ",wfs=" + widthFactorSide +
                ",hfs=" + heightFactorSide;
    }
}
