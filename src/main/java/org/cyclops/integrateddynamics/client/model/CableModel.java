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

    public CableModel(IExtendedBlockState state, boolean isItemStack) {
        super(state, isItemStack);
    }

    public CableModel() {
        super();
    }

    private float[][][] quadVertexes = new float[][][]{
            {
                    {0.25F, 1.00F, 0.25F},
                    {0.75F, 1.00F, 0.25F},
                    {0.75F, 0.75F, 0.25F},
                    {0.25F, 0.75F, 0.25F},
            },
            {
                    {0.25F, 0.75F, 0.25F},
                    {0.25F, 0.75F, 0.75F},
                    {0.25F, 1.00F, 0.75F},
                    {0.25F, 1.00F, 0.25F},
            },
            {
                    {0.25F, 0.75F, 0.75F},
                    {0.75F, 0.75F, 0.75F},
                    {0.75F, 1.00F, 0.75F},
                    {0.25F, 1.00F, 0.75F},
            },
            {
                    {0.75F, 1.00F, 0.25F},
                    {0.75F, 1.00F, 0.75F},
                    {0.75F, 0.75F, 0.75F},
                    {0.75F, 0.75F, 0.25F},
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
    public List<BakedQuad> func_177550_a() {
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
                    int[] data = Ints.concat(
                            vertexToInts((float) v1.xCoord, (float) v1.yCoord, (float) v1.zCoord, -1, texture, 4, i == 2 ? 4 : 0),
                            vertexToInts((float) v2.xCoord, (float) v2.yCoord, (float) v2.zCoord, -1, texture, 12, i == 2 ? 4 : 0),
                            vertexToInts((float) v3.xCoord, (float) v3.yCoord, (float) v3.zCoord, -1, texture, 12, i == 2 ? 0 : 4),
                            vertexToInts((float) v4.xCoord, (float) v4.yCoord, (float) v4.zCoord, -1, texture, 4, i == 2 ? 0 : 4)
                    );
                    i++;
                    ret.add(new BakedQuad(data, -1, realSide));
                }
            } else {
                addBakedQuad(ret, 0.25F, 0.75F, 0.25F, 0.75F, 0.75F, texture, side);
            }
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
