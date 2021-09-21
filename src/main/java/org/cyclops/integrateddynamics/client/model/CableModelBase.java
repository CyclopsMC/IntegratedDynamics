package org.cyclops.integrateddynamics.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.SimpleModelTransform;
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
import java.util.stream.Collectors;

/**
 * A base dynamic model for cables.
 * @author rubensworks
 */
public abstract class CableModelBase extends DelegatingDynamicItemAndBlockModel {

    private static final FaceBakery FACE_BAKERY = new FaceBakery();
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

    private Direction getSideFromVecs(Vector3d a, Vector3d b, Vector3d c) {
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
        Random rand = new Random();
        IBakedModel model = RenderHelpers.getBakedModel(blockState);
        List<BakedQuad> originalQuads = model.getQuads(blockState, side, rand);
        return originalQuads.stream()
                .flatMap(originalQuad -> {
                    List<BakedQuad> ret = Lists.newLinkedList();
                    if(partRenderPosition == PartRenderPosition.NONE) {
                        addFacadeQuad(ret, originalQuad, 0, 0, 1f, 1f, side);
                    } else {
                        float w = partRenderPosition.getWidthFactorSide();
                        float h = partRenderPosition.getHeightFactorSide();
                        float u0 = 0f;
                        float v0 = 0f;
                        float u1 = (1f - w) / 2;
                        float v1 = (1f - h) / 2;
                        float u2 = u1 + w;
                        float v2 = v1 + h;
                        float u3 = 1f;
                        float v3 = 1f;
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
                        addFacadeQuad(ret, originalQuad, u0, v0, u1, v1, side); // 1
                        addFacadeQuad(ret, originalQuad, u1, v0, u2, v1, side); // 2
                        addFacadeQuad(ret, originalQuad, u2, v0, u3, v1, side); // 3
                        addFacadeQuad(ret, originalQuad, u0, v1, u1, v2, side); // 4
                        addFacadeQuad(ret, originalQuad, u2, v1, u3, v2, side); // 5
                        addFacadeQuad(ret, originalQuad, u0, v2, u1, v3, side); // 6
                        addFacadeQuad(ret, originalQuad, u1, v2, u2, v3, side); // 7
                        addFacadeQuad(ret, originalQuad, u2, v2, u3, v3, side); // 8
                    }
                    return ret.stream();
                })
                .collect(Collectors.toList());
    }

    private void addFacadeQuad(List<BakedQuad> quads, BakedQuad originalQuad, float u0, float v0, float u1, float v1, Direction side) {
        Vector3f from = new Vector3f(u0 * 16f, v0 * 16f, 0f);
        Vector3f to = new Vector3f(u1 * 16f, v1 * 16f, 0f);
        TextureAtlasSprite texture = originalQuad.getSprite();
        float[] uvArray = { 16f - u1 * 16f, 16f - v1 * 16f, 16f - u0 * 16f, 16f - v0 * 16f };
        int ROTATION_NONE = 0;
        BlockFaceUV blockFaceUV = new BlockFaceUV(uvArray, ROTATION_NONE);
        Direction NO_FACE_CULLING = null;
        String DUMMY_TEXTURE_NAME = "";
        BlockPartFace blockPartFace = new BlockPartFace(NO_FACE_CULLING, originalQuad.getTintIndex(), DUMMY_TEXTURE_NAME, blockFaceUV);
        IModelTransform transformation = new SimpleModelTransform(getMatrix(getRotation(side)));
        BlockPartRotation DEFAULT_ROTATION = null;
        boolean APPLY_SHADING = true;
        quads.add(FACE_BAKERY.bakeQuad(from, to, blockPartFace, texture, Direction.NORTH, transformation, DEFAULT_ROTATION, APPLY_SHADING, null));
    }

    public static TransformationMatrix getMatrix(ModelRotation modelRotation) {
        return modelRotation.getRotation();
    }

    public static ModelRotation getRotation(Direction facing) {
        switch (facing) {
            case DOWN:  return ModelRotation.X90_Y180;
            case UP:    return ModelRotation.X270_Y180;
            case NORTH: return ModelRotation.X0_Y0;
            case SOUTH: return ModelRotation.X0_Y180;
            case WEST:  return ModelRotation.X0_Y270;
            case EAST:  return ModelRotation.X0_Y90;
        }
        throw new IllegalArgumentException(String.valueOf(facing));
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
            Optional<BlockState> blockStateHolder = getFacade(modelData);
            boolean renderCable = isItemStack() || (isRealCable(modelData) && (
                    (!blockStateHolder.isPresent() && MinecraftForgeClient.getRenderLayer() == RenderType.getSolid())
                            || (blockStateHolder.isPresent() && MinecraftForgeClient.getRenderLayer() == RenderType.getTranslucent())));
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
                            Vector3d v1 = rotate(new Vector3d(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), side).add(.5, .5, .5);
                            Vector3d v2 = rotate(new Vector3d(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), side).add(.5, .5, .5);
                            Vector3d v3 = rotate(new Vector3d(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), side).add(.5, .5, .5);
                            Vector3d v4 = rotate(new Vector3d(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), side).add(.5, .5, .5);
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

            if (blockStateHolder.isPresent() && shouldRenderParts(modelData)
                    && RenderTypeLookup.canRenderInLayer(blockStateHolder.get(), MinecraftForgeClient.getRenderLayer())) {
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
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos,
                                   @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return TileHelpers.getSafeTile(world, pos, TileMultipartTicking.class)
                .map(TileMultipartTicking::getConnectionState)
                .orElse(EmptyModelData.INSTANCE);
    }

    @Override
    public boolean isSideLit() {
        return false; // If false, RenderHelper.setupGuiFlatDiffuseLighting() is called
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return TRANSFORMS;
    }

}
