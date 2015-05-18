package org.cyclops.integrateddynamics.client.model;

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
    private static final int INV_LENGTH_CONNECTION = TEXTURE_SIZE - LENGTH_CONNECTION;
    public static final float MIN = (float) LENGTH_CONNECTION / (float) TEXTURE_SIZE;
    public static final float MAX = 1.0F - MIN;

    public CableModel(IExtendedBlockState state, boolean isItemStack) {
        super(state, isItemStack);
    }

    public CableModel() {
        super();
    }

    private float[][][] quadVertexes = new float[][][]{
            {
                    {MIN, 1.00F, MIN},
                    {MAX, 1.00F, MIN},
                    {MAX, MAX  , MIN},
                    {MIN, MAX  , MIN},
            },
            {
                    {MIN, MAX  , MIN},
                    {MIN, MAX  , MAX},
                    {MIN, 1.00F, MAX},
                    {MIN, 1.00F, MIN},
            },
            {
                    {MIN, MAX  , MAX},
                    {MAX, MAX  , MAX},
                    {MAX, 1.00F,  MAX},
                    {MIN, 1.00F, MAX},
            },
            {
                    {MAX, 1.00F, MIN},
                    {MAX, 1.00F, MAX},
                    {MAX, MAX  , MAX},
                    {MAX, MAX  , MIN},
            }
    };

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

    @Override
    public List<BakedQuad> getGeneralQuads() {
        List<BakedQuad> ret = Lists.newLinkedList();
        TextureAtlasSprite texture = getTexture();

        for(EnumFacing side : EnumFacing.values()) {
            boolean isConnected = false;
            if(isItemStack()) {
                isConnected = side == EnumFacing.EAST || side == EnumFacing.WEST;
            } else if(getState() != null && getState().getValue(BlockCable.CONNECTED[side.ordinal()]) != null) {
                isConnected = getState().getValue(BlockCable.CONNECTED[side.ordinal()]);
            }
            if(isConnected) {
                int i = 0;
                for (float[][] v : quadVertexes) {
                    Vec3 v1 = rotate(new Vec3(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), side).addVector(.5, .5, .5);
                    Vec3 v2 = rotate(new Vec3(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), side).addVector(.5, .5, .5);
                    Vec3 v3 = rotate(new Vec3(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), side).addVector(.5, .5, .5);
                    Vec3 v4 = rotate(new Vec3(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), side).addVector(.5, .5, .5);
                    EnumFacing realSide = getSideFromVecs(v1, v2, v3);
                    boolean invert = i == 2 || i == 1;
                    int[] data = Ints.concat(
                            vertexToInts((float) v1.xCoord, (float) v1.yCoord, (float) v1.zCoord, -1, texture,
                                    LENGTH_CONNECTION    , invert ? LENGTH_CONNECTION : 0),
                            vertexToInts((float) v2.xCoord, (float) v2.yCoord, (float) v2.zCoord, -1, texture,
                                    INV_LENGTH_CONNECTION, invert ? LENGTH_CONNECTION : 0),
                            vertexToInts((float) v3.xCoord, (float) v3.yCoord, (float) v3.zCoord, -1, texture,
                                    INV_LENGTH_CONNECTION, invert ? 0 : LENGTH_CONNECTION),
                            vertexToInts((float) v4.xCoord, (float) v4.yCoord, (float) v4.zCoord, -1, texture,
                                    LENGTH_CONNECTION    , invert ? 0 : LENGTH_CONNECTION)
                    );
                    i++;
                    ret.add(new BakedQuad(data, -1, realSide));
                }
            } else {
                addBakedQuad(ret, MIN, MAX, MIN, MAX, MAX, texture, side);
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
    public TextureAtlasSprite getTexture() {
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
