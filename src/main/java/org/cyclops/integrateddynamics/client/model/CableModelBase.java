package org.cyclops.integrateddynamics.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.math.Quadrant;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.model.data.ModelData;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.model.DelegatingDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCableClientConfig;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A base dynamic facadeModel for cables.
 * @author rubensworks
 */
public abstract class CableModelBase extends DelegatingDynamicItemAndBlockModel {

    public static ModelBaker MODEL_BAKER;
    private static final Cache<Triple<IRenderState, Direction, ChunkSectionLayer>, List<BakedQuad>> CACHE_QUADS = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

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

    protected static final ItemTransforms TRANSFORMS = ModelHelpers.modifyDefaultTransforms(ImmutableMap.of(
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new ItemTransform(
                    new Vector3f(0, 45, 0),
                    new Vector3f(0, 1f / 32, 0),
                    new Vector3f(0.4F, 0.4F, 0.4F)),
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new ItemTransform(
                    new Vector3f(0, 225, 0),
                    new Vector3f(0, 1f / 32, 0),
                    new Vector3f(0.4F, 0.4F, 0.4F))
    ));

    public CableModelBase(BlockAndTintGetter level, BlockState blockState, Direction facing, RandomSource rand, ModelData modelData, ChunkSectionLayer renderType) {
        super(level, blockState, facing, rand, modelData, renderType);
    }

    public CableModelBase(ItemStack itemStack, ClientLevel world, ItemOwner entity) {
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

    private Direction getSideFromVecs(Vector3f a, Vector3f b, Vector3f c) {
        int dir = a.y == b.y && b.y == c.y ? 0 : (a.x == b.x && b.x == c.x ? 2 : 4);
        if (dir == 0) {
            dir += (c.y >= 0.5) ? 1 : 0;
        } else if (dir == 2) {
            dir += (c.x >= 0.5) ? 1 : 0;
        } else if (dir == 4) {
            dir += (c.z >= 0.5) ? 1 : 0;
        }
        return Direction.from3DDataValue(dir);
    }

    public List<BakedQuad> getFacadeQuads(BlockStateModel facadeModel, BlockState blockState, Direction side, PartRenderPosition partRenderPosition, ChunkSectionLayer renderType) {
        List<BakedQuad> originalQuads = Lists.newArrayList();
        List<BlockStateModelPart> parts = new ArrayList<>();
        facadeModel.collectParts(rand, parts);
        for (BlockStateModelPart collectPart : parts) {
            originalQuads.addAll(collectPart.getQuads(null));
            originalQuads.addAll(collectPart.getQuads(side));
        }

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
        // MODEL_BAKER is populated lazily when the cable *item* model is baked (see ItemModelCable.Unbaked#bake).
        // Under deferred/lazy model baking (e.g. ModernFix's dynamic_resources) that may not have happened yet when a
        // chunk section containing a facade is first meshed, leaving MODEL_BAKER null and crashing FaceBakery#bakeQuad.
        // Skip the facade quad in that case instead of crashing; it will render once the item model has been baked.
        if (MODEL_BAKER == null) {
            return;
        }
        Vector3f from = new Vector3f(u0 * 16f, v0 * 16f, 0f);
        Vector3f to = new Vector3f(u1 * 16f, v1 * 16f, 0f);
        TextureAtlasSprite texture = originalQuad.materialInfo().sprite();
        CuboidFace.UVs blockFaceUV = new CuboidFace.UVs(16f - u1 * 16f, 16f - v1 * 16f, 16f - u0 * 16f, 16f - v0 * 16f);
        Direction NO_FACE_CULLING = null;
        String DUMMY_TEXTURE_NAME = "";
        CuboidFace blockPartFace = new CuboidFace(NO_FACE_CULLING, originalQuad.materialInfo().tintIndex(), DUMMY_TEXTURE_NAME, blockFaceUV, Quadrant.R0);
        ModelState transformation = getRotation(side);
        boolean APPLY_SHADING = true;
        quads.add(FaceBakery.bakeQuad(MODEL_BAKER, from, to, blockPartFace, new Material.Baked(texture, false), Direction.NORTH, transformation, null, APPLY_SHADING, 0));
    }

    public static BlockModelRotation getRotation(Direction facing) {
        switch (facing) {
            case DOWN:  return BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R90, Quadrant.R180));
            case UP:    return BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R270, Quadrant.R180));
            case NORTH: return BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R0));
            case SOUTH: return BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R180));
            case WEST:  return BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R270));
            case EAST:  return BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R90));
        }
        throw new IllegalArgumentException(String.valueOf(facing));
    }

    protected abstract boolean isRealCable(ModelData modelData);
    protected abstract Optional<BlockState> getFacade(ModelData modelData);
    protected abstract boolean isConnected(ModelData modelData, Direction side);
    protected abstract boolean hasPart(ModelData modelData, Direction side);
    protected abstract PartRenderPosition getPartRenderPosition(ModelData modelData, Direction side);
    protected abstract boolean shouldRenderParts(ModelData modelData);
    protected abstract BlockStateModel getPartModel(ModelData modelData, Direction side);
    protected abstract IRenderState getRenderState(ModelData modelData);

    @Override
    public int materialFlags() {
        return 0;
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        Triple<IRenderState, Direction, ChunkSectionLayer> cacheKey = null;
        List<BakedQuad> cachedQuads = null;
        if (GeneralConfig.cacheCableModels) {
            IRenderState renderState = getRenderState(modelData);
            if (renderState != null) {
                cacheKey = Triple.of(renderState, this.facing, this.renderType);
                cachedQuads = CACHE_QUADS.getIfPresent(cacheKey);
            }
        }
        if (cachedQuads == null) {
            List<BakedQuad> ret = Lists.newLinkedList();
            TextureAtlasSprite texture = particleIcon();
            Optional<BlockState> blockStateHolder = getFacade(modelData);
            boolean renderCable = isItemStack() || (isRealCable(modelData) && (
                    (!blockStateHolder.isPresent() && this.renderType == ChunkSectionLayer.SOLID)
                            || (blockStateHolder.isPresent() && this.renderType == ChunkSectionLayer.TRANSLUCENT)));
            for (Direction side : Direction.values()) {
                boolean isConnected = isItemStack() ? side == Direction.EAST || side == Direction.WEST : isConnected(modelData, side);
                boolean hasPart = !isItemStack() && hasPart(modelData, side);
                if (hasPart && shouldRenderParts(modelData)) {
                    try {
                        List<BlockStateModelPart> parts = new ArrayList<>();
                        getPartModel(modelData, side).collectParts(rand, parts);
                        for (BlockStateModelPart collectPart : parts) {
                            ret.addAll(collectPart.getQuads(null));
                        }
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
                            Vector3f v1 = rotate(new Vector3f(v[0][0] - .5f, v[0][1] - .5f, v[0][2] - .5f), side).add(.5f, .5f, .5f);
                            Vector3f v2 = rotate(new Vector3f(v[1][0] - .5f, v[1][1] - .5f, v[1][2] - .5f), side).add(.5f, .5f, .5f);
                            Vector3f v3 = rotate(new Vector3f(v[2][0] - .5f, v[2][1] - .5f, v[2][2] - .5f), side).add(.5f, .5f, .5f);
                            Vector3f v4 = rotate(new Vector3f(v[3][0] - .5f, v[3][1] - .5f, v[3][2] - .5f), side).add(.5f, .5f, .5f);
                            Direction realSide = getSideFromVecs(v1, v2, v3);

                            boolean invert = i == 2 || i == 1;
                            int length = hasPart ? LENGTH_CONNECTION_LIMITED : LENGTH_CONNECTION;

                            i++;
                            ret.add(new BakedQuad(
                                    v1,
                                    v2,
                                    v3,
                                    v4,
                                    UVPair.pack(texture.getU(LENGTH_CONNECTION / 16f), texture.getV(invert ? length / 16f : 0)),
                                    UVPair.pack(texture.getU(INV_LENGTH_CONNECTION / 16f), texture.getV(invert ? length / 16f : 0)),
                                    UVPair.pack(texture.getU(INV_LENGTH_CONNECTION / 16f), texture.getV(invert ? 0 : length / 16f)),
                                    UVPair.pack(texture.getU(LENGTH_CONNECTION / 16f), texture.getV(invert ? 0 : length / 16f)),
                                    realSide,
                                    new BakedQuad.MaterialInfo(texture, ChunkSectionLayer.SOLID, RenderTypes.entityCutout(texture.atlasLocation()), -1, true, 0),
                                    BakedNormals.UNSPECIFIED,
                                    BakedColors.DEFAULT
                            ));
                        }
                    } else {
                        addBakedQuad(ret, MIN, MAX, MIN, MAX, MAX, texture, side);
                    }
                }
            }

            if (blockStateHolder.isPresent() && shouldRenderParts(modelData) && this.renderType != null && this.facing != null) {
                BlockStateModel facadeModel = IModHelpers.get().getRenderHelpers().getBakedModel(blockStateHolder.get());
                boolean isConnected = isItemStack() ? this.facing == Direction.EAST || this.facing == Direction.WEST : isConnected(modelData, this.facing);
                PartRenderPosition partRenderPosition = PartRenderPosition.NONE;
                boolean hasPart = !isItemStack() && hasPart(modelData, this.facing);
                if (hasPart) partRenderPosition = getPartRenderPosition(modelData, this.facing);
                else if (isConnected) partRenderPosition = CABLE_RENDERPOSITION;
                ret.addAll(getFacadeQuads(facadeModel, blockStateHolder.get(), this.facing, partRenderPosition, this.renderType));
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

    public TextureAtlasSprite particleIcon() {
        return BlockCableClientConfig.BLOCK_TEXTURE;
    }

    @Override
    public Material.Baked particleMaterial() {
        return new Material.Baked(particleIcon(), false);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData) {
        return IModHelpers.get().getBlockEntityHelpers().get(world, pos, BlockEntityMultipartTicking.class)
                .map(BlockEntityMultipartTicking::getConnectionState)
                .orElse(ModelData.EMPTY);
    }

    @Override
    public boolean usesBlockLight() {
        return false; // If false, RenderHelper.setupGuiFlatDiffuseLighting() is called
    }

    @Override
    public ItemTransforms getTopTransforms() {
        return TRANSFORMS;
    }

    @Override
    public List<ChunkSectionLayer> getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return List.of(ChunkSectionLayer.values());
    }
}
