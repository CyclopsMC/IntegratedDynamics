package org.cyclops.integrateddynamics.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.model.DelegatingDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A base dynamic model for cables.
 * @author rubensworks
 */
public abstract class CableModelBase extends DelegatingDynamicItemAndBlockModel {

    private static final Cache<Triple<IRenderState, Direction, RenderType>, List<BakedQuad>> CACHE_QUADS = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

    private static final int RADIUS = 4;
    private static final int TEXTURE_SIZE = 16;

    private static final int LENGTH_CONNECTION = (TEXTURE_SIZE - RADIUS) / 2;
    private static final int LENGTH_CONNECTION_LIMITED = 1;
    private static final int INV_LENGTH_CONNECTION = TEXTURE_SIZE - LENGTH_CONNECTION;
    public static final float MIN = (float) LENGTH_CONNECTION / (float) TEXTURE_SIZE;
    public static final float MAX = 1.0F - MIN;
    private static final PartRenderPosition CABLE_RENDERPOSITION = new PartRenderPosition(-1,
            (((float) TEXTURE_SIZE - (float) RADIUS) / 2 / (float) TEXTURE_SIZE),
            (float) RADIUS / (float) TEXTURE_SIZE, (float) RADIUS / (float) TEXTURE_SIZE);

    private final float[][][] quadVertexes = makeQuadVertexes(MIN, MAX, 1.00F);

    protected static final ItemCameraTransforms TRANSFORMS = ModelHelpers.modifyDefaultTransforms(ImmutableMap.of(
            ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, new ItemTransformVec3f(
                    new Vector3f(0, 45, 0),
                    new Vector3f(0, 1f / 32, 0),
                    new Vector3f(0.4F, 0.4F, 0.4F)),
            ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, new ItemTransformVec3f(
                    new Vector3f(0, 225, 0),
                    new Vector3f(0, 1f / 32, 0),
                    new Vector3f(0.4F, 0.4F, 0.4F))
    ));

    public CableModelBase(BlockState blockState, Direction facing, Random rand, IModelData modelData) {
        super(blockState, facing, rand, modelData);
    }

    public CableModelBase(ItemStack itemStack, World world, LivingEntity entity) {
        super(itemStack, world, entity);
    }

    public CableModelBase() {
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

    private Direction getSideFromVecs(Vec3d a, Vec3d b, Vec3d c) {
        int dir = a.y == b.y && b.y == c.y ? 0 : (a.x == b.x && b.x == c.x ? 2 : 4);
        if (dir == 0) {
            dir += (c.y >= 0.5) ? 1 : 0;
        } else if (dir == 2) {
            dir += (c.x >= 0.5) ? 1 : 0;
        } else if (dir == 4) {
            dir += (c.z >= 0.5) ? 1 : 0;
        }
        return Direction.byIndex(dir);
    }

    public List<BakedQuad> getFacadeQuads(BlockState blockState, Direction side, PartRenderPosition partRenderPosition) {
        List<BakedQuad> ret = Lists.newLinkedList();
        IBakedModel model = RenderHelpers.getBakedModel(blockState);
        TextureAtlasSprite texture = model.getParticleTexture();
        if(partRenderPosition == PartRenderPosition.NONE) {
            addBakedQuad(ret, 0, 1, 0, 1, 1, texture, side);
        } else {
            float w = partRenderPosition.getWidthFactorSide();
            float h = partRenderPosition.getHeightFactorSide();

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

    protected abstract boolean isRealCable(IModelData modelData);
    protected abstract Optional<BlockState> getFacade(IModelData modelData);
    protected abstract boolean isConnected(IModelData modelData, Direction side);
    protected abstract boolean hasPart(IModelData modelData, Direction side);
    protected abstract PartRenderPosition getPartRenderPosition(IModelData modelData, Direction side);
    protected abstract boolean shouldRenderParts(IModelData modelData);
    protected abstract IBakedModel getPartModel(IModelData modelData, Direction side);
    protected abstract IRenderState getRenderState(IModelData modelData);

    @Override
    public List<BakedQuad> getGeneralQuads() {
        Triple<IRenderState, Direction, RenderType> cacheKey = null;
        List<BakedQuad> cachedQuads = null;
        if (GeneralConfig.cacheCableModels) {
            IRenderState renderState = getRenderState(modelData);
            if (renderState != null) {
                cacheKey = Triple.of(renderState, this.facing, MinecraftForgeClient.getRenderLayer());
                cachedQuads = CACHE_QUADS.getIfPresent(cacheKey);
            }
        }
        if (cachedQuads == null) {
            List<BakedQuad> ret = Lists.newLinkedList();
            TextureAtlasSprite texture = getParticleTexture();
            boolean renderCable = isItemStack() || (isRealCable(modelData) && MinecraftForgeClient.getRenderLayer() == RenderType.getSolid());
            Optional<BlockState> blockStateHolder = getFacade(modelData);
            for (Direction side : Direction.values()) {
                boolean isConnected = isItemStack() ? side == Direction.EAST || side == Direction.WEST : isConnected(modelData, side);
                boolean hasPart = !isItemStack() && hasPart(modelData, side);
                if (hasPart && shouldRenderParts(modelData)) {
                    try {
                        ret.addAll(getPartModel(modelData, side).getQuads(this.blockState, this.facing, this.rand, this.modelData));
                    } catch (Exception e) {
                        // Skip rendering this part, could occur when the player is still logging in.
                    }
                }
                if (renderCable) {
                    if (isConnected || hasPart) {
                        int i = 0;
                        float[][][] quadVertexes = this.quadVertexes;
                        if (hasPart) {
                            PartRenderPosition partRenderPosition = getPartRenderPosition(modelData, side);
                            float depthFactor = partRenderPosition == PartRenderPosition.NONE ? 0F : partRenderPosition.getDepthFactor();
                            quadVertexes = makeQuadVertexes(MIN, MAX, 1F - depthFactor);
                        }
                        for (float[][] v : quadVertexes) {
                            Vec3d v1 = rotate(new Vec3d(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), side).add(.5, .5, .5);
                            Vec3d v2 = rotate(new Vec3d(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), side).add(.5, .5, .5);
                            Vec3d v3 = rotate(new Vec3d(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), side).add(.5, .5, .5);
                            Vec3d v4 = rotate(new Vec3d(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), side).add(.5, .5, .5);
                            Direction realSide = getSideFromVecs(v1, v2, v3);

                            boolean invert = i == 2 || i == 1;
                            int length = hasPart ? LENGTH_CONNECTION_LIMITED : LENGTH_CONNECTION;

                            int[] data = Ints.concat(
                                    vertexToInts((float) v1.x, (float) v1.y, (float) v1.z, -1, texture,
                                            LENGTH_CONNECTION, invert ? length : 0),
                                    vertexToInts((float) v2.x, (float) v2.y, (float) v2.z, -1, texture,
                                            INV_LENGTH_CONNECTION, invert ? length : 0),
                                    vertexToInts((float) v3.x, (float) v3.y, (float) v3.z, -1, texture,
                                            INV_LENGTH_CONNECTION, invert ? 0 : length),
                                    vertexToInts((float) v4.x, (float) v4.y, (float) v4.z, -1, texture,
                                            LENGTH_CONNECTION, invert ? 0 : length)
                            );
                            i++;
                            ForgeHooksClient.fillNormal(data, realSide); // This fixes lighting issues when item is rendered in hand/inventory
                            ret.add(new BakedQuad(data, -1, realSide, texture, true));
                        }
                    } else {
                        addBakedQuad(ret, MIN, MAX, MIN, MAX, MAX, texture, side);
                    }
                }
            }

            if (blockStateHolder.isPresent() && shouldRenderParts(modelData)) {
                for (Direction side : Direction.values()) {
                    boolean isConnected = isItemStack() ? side == Direction.EAST || side == Direction.WEST : isConnected(modelData, side);
                    PartRenderPosition partRenderPosition = PartRenderPosition.NONE;
                    boolean hasPart = !isItemStack() && hasPart(modelData, side);
                    if (hasPart)          partRenderPosition = getPartRenderPosition(modelData, side);
                    else if (isConnected) partRenderPosition = CABLE_RENDERPOSITION;
                    ret.addAll(getFacadeQuads(blockStateHolder.get(), side, partRenderPosition));
                }
            }

            // Close the cable connections for items
            if (isItemStack()) {
                addBakedQuad(ret, MIN, MAX, MIN, MAX, 1, texture, Direction.EAST);
                addBakedQuad(ret, MIN, MAX, MIN, MAX, 1, texture, Direction.WEST);
            }
            cachedQuads = ret;
            if (cacheKey != null) {
                CACHE_QUADS.put(cacheKey, cachedQuads);
            }
        }
        return cachedQuads;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return RegistryEntries.BLOCK_CABLE.texture;
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull ILightReader world, @Nonnull BlockPos pos,
                                   @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class)
                .map(TileMultipartTicking::getConnectionState)
                .orElse(EmptyModelData.INSTANCE);
    }

    @Override
    public boolean func_230044_c_() {
        return false; // If false, RenderHelper.setupGuiFlatDiffuseLighting() is called
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return TRANSFORMS;
    }

}
