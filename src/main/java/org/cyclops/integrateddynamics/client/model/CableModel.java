package org.cyclops.integrateddynamics.client.model;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.cyclops.cyclopscore.client.model.DynamicModel;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.util.List;

/**
 * A dynamic model for cables.
 * @author rubensworks
 */
public class CableModel extends DynamicModel {

    private static final int RADIUS = 4;
    private static final int TEXTURE_SIZE = 16;

    private static final int LENGTH_CONNECTION = (TEXTURE_SIZE - RADIUS) / 2;
    private static final int LENGTH_CONNECTION_LIMITED = 1;
    private static final int INV_LENGTH_CONNECTION = TEXTURE_SIZE - LENGTH_CONNECTION;
    public static final float MIN = (float) LENGTH_CONNECTION / (float) TEXTURE_SIZE;
    public static final float MAX = 1.0F - MIN;
    private static final IPartType.RenderPosition CABLE_RENDERPOSITION = new IPartType.RenderPosition(
            (((float) TEXTURE_SIZE - (float) RADIUS) / 2 / (float) TEXTURE_SIZE),
            (float) RADIUS / (float) TEXTURE_SIZE, (float) RADIUS / (float) TEXTURE_SIZE);

    private final float[][][] quadVertexes = makeQuadVertexes(MIN, MAX, 1.00F);

    public CableModel(IExtendedBlockState state, boolean isItemStack) {
        super(state, isItemStack);
    }

    public CableModel() {
        super();
    }
    
    protected static float[][][] makeQuadVertexes(float min, float max, float length) {
        return new float[][][]{
                {
                        {min, length, min},
                        {max, length, min},
                        {max, max   , min},
                        {min, max   , min},
                },
                {
                        {min, max   , min},
                        {min, max   , max},
                        {min, length, max},
                        {min, length, min},
                },
                {
                        {min, max   , max},
                        {max, max   , max},
                        {max, length,  max},
                        {min, length, max},
                },
                {
                        {max, length, min},
                        {max, length, max},
                        {max, max   , max},
                        {max, max   , min},
                }
        };
    }

    private EnumFacing getSideFromVecs(Vec3 a, Vec3 b, Vec3 c) {
        int dir = a.yCoord == b.yCoord && b.yCoord == c.yCoord ? 0 : (a.xCoord == b.xCoord && b.xCoord == c.xCoord ? 2 : 4);
        if (dir == 0) {
            dir += (c.yCoord >= 0.5) ? 1 : 0;
        } else if (dir == 2) {
            dir += (c.xCoord >= 0.5) ? 1 : 0;
        } else if (dir == 4) {
            dir += (c.zCoord >= 0.5) ? 1 : 0;
        }
        return EnumFacing.getFront(dir);
    }

    public List<BakedQuad> getFacadeQuads(IBlockState blockState, EnumFacing side, IPartType.RenderPosition renderPosition) {
        List<BakedQuad> ret = Lists.newLinkedList();
        IBakedModel model = RenderHelpers.getBakedModel(blockState);
        TextureAtlasSprite texture = model.getParticleTexture();
        if(renderPosition == IPartType.RenderPosition.NONE) {
            addBakedQuad(ret, 0, 1, 0, 1, 1, texture, side);
        } else {
            float w = renderPosition.getWidthFactor();
            float h = renderPosition.getHeightFactor();

            float x0 = 0F;
            float x1 = (1F - w) / 2;
            float x2 = x1 + w;
            float x3 = 1F;
            float z0 = 0F;
            float z1 = (1F - h) / 2;
            float z2 = z1 + h;
            float z3 = 1F;

            /*
             * We render the following eight boxes, excluding the part box in the middle.
             * -------
             * |1|2|3|
             * -------
             * |4|P|5|
             * -------
             * |6|7|8|
             * -------
             */

            addBakedQuad(ret, x0, x1, z0, z1, 1, texture, side); // 1
            addBakedQuad(ret, x1, x2, z0, z1, 1, texture, side); // 2
            addBakedQuad(ret, x2, x3, z0, z1, 1, texture, side); // 3

            addBakedQuad(ret, x0, x1, z1, z2, 1, texture, side); // 4
            // P
            addBakedQuad(ret, x2, x3, z1, z2, 1, texture, side); // 5

            addBakedQuad(ret, x0, x1, z2, z3, 1, texture, side); // 6
            addBakedQuad(ret, x1, x2, z2, z3, 1, texture, side); // 7
            addBakedQuad(ret, x2, x3, z2, z3, 1, texture, side); // 8
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BakedQuad> getGeneralQuads() {
        List<BakedQuad> ret = Lists.newLinkedList();
        TextureAtlasSprite texture = getParticleTexture();
        boolean realCable = isItemStack() || BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.REALCABLE, true);

        if(realCable) {
            Optional<IBlockState> blockStateHolder = BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.FACADE, Optional.absent());
            for (EnumFacing side : EnumFacing.values()) {
                boolean isConnected = isItemStack()
                        ? side == EnumFacing.EAST || side == EnumFacing.WEST
                        : BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.CONNECTED[side.ordinal()], false);
                boolean hasPart = !isItemStack() &&
                        BlockHelpers.getSafeBlockStateProperty(getState(), BlockCable.PART[side.ordinal()], false);
                IPartType.RenderPosition renderPosition = IPartType.RenderPosition.NONE;
                if(isConnected) {
                    renderPosition = CABLE_RENDERPOSITION;
                }
                if (isConnected || hasPart) {
                    int i = 0;
                    float[][][] quadVertexes = this.quadVertexes;
                    if(hasPart) {
                        renderPosition = BlockHelpers.getSafeBlockStateProperty(getState(),
                                BlockCable.PART_RENDERPOSITIONS[side.ordinal()], IPartType.RenderPosition.NONE);
                        float depthFactor = renderPosition == IPartType.RenderPosition.NONE ? 0F : renderPosition.getDepthFactor();
                        quadVertexes = makeQuadVertexes(MIN, MAX, 1F - depthFactor);
                    }
                    for (float[][] v : quadVertexes) {
                        Vec3 v1 = rotate(new Vec3(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), side).addVector(.5, .5, .5);
                        Vec3 v2 = rotate(new Vec3(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), side).addVector(.5, .5, .5);
                        Vec3 v3 = rotate(new Vec3(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), side).addVector(.5, .5, .5);
                        Vec3 v4 = rotate(new Vec3(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), side).addVector(.5, .5, .5);
                        EnumFacing realSide = getSideFromVecs(v1, v2, v3);

                        boolean invert = i == 2 || i == 1;
                        int length = hasPart ? LENGTH_CONNECTION_LIMITED : LENGTH_CONNECTION;

                        int[] data = Ints.concat(
                                vertexToInts((float) v1.xCoord, (float) v1.yCoord, (float) v1.zCoord, -1, texture,
                                        LENGTH_CONNECTION, invert ? length : 0),
                                vertexToInts((float) v2.xCoord, (float) v2.yCoord, (float) v2.zCoord, -1, texture,
                                        INV_LENGTH_CONNECTION, invert ? length : 0),
                                vertexToInts((float) v3.xCoord, (float) v3.yCoord, (float) v3.zCoord, -1, texture,
                                        INV_LENGTH_CONNECTION, invert ? 0 : length),
                                vertexToInts((float) v4.xCoord, (float) v4.yCoord, (float) v4.zCoord, -1, texture,
                                        LENGTH_CONNECTION, invert ? 0 : length)
                        );
                        i++;
                        ret.add(new BakedQuad(data, -1, realSide));
                    }
                } else {
                    addBakedQuad(ret, MIN, MAX, MIN, MAX, MAX, texture, side);
                }

                // Render facade if present
                if(blockStateHolder.isPresent()) {
                    ret.addAll(getFacadeQuads(blockStateHolder.get(), side, renderPosition));
                }
            }
        }

        // Close the cable connections for items
        if(isItemStack()) {
            addBakedQuad(ret, MIN, MAX, MIN, MAX, 1, texture, EnumFacing.EAST);
            addBakedQuad(ret, MIN, MAX, MIN, MAX, 1, texture, EnumFacing.WEST);
        }

        return ret;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return BlockCable.getInstance().texture;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        return new CableModel((IExtendedBlockState) state, false);
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack) {
        return new CableModel((IExtendedBlockState) BlockCable.getInstance().getDefaultState(), true);
    }
}
