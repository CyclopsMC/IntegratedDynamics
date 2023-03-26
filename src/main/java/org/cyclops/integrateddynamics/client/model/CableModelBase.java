package org.cyclops.integrateddynamics.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.model.DelegatingDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
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

    public CableModelBase(BlockState blockState, Direction facing, RandomSource rand, ModelData modelData, RenderType renderType) {
        super(blockState, facing, rand, modelData, renderType);
    }

    public CableModelBase(ItemStack itemStack, Level world, LivingEntity entity) {
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

    private Direction getSideFromVecs(Vec3 a, Vec3 b, Vec3 c) {
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

    public List<BakedQuad> getFacadeQuads(BakedModel facadeModel, BlockState blockState, Direction side, PartRenderPosition partRenderPosition) {
        RandomSource rand = RandomSource.create();
        List<BakedQuad> originalQuads = facadeModel.getQuads(blockState, side, rand);
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
        BlockElementFace blockPartFace = new BlockElementFace(NO_FACE_CULLING, originalQuad.getTintIndex(), DUMMY_TEXTURE_NAME, blockFaceUV);
        ModelState transformation = new SimpleModelState(getMatrix(getRotation(side)));
        BlockElementRotation DEFAULT_ROTATION = null;
        boolean APPLY_SHADING = true;
        quads.add(FACE_BAKERY.bakeQuad(from, to, blockPartFace, texture, Direction.NORTH, transformation, DEFAULT_ROTATION, APPLY_SHADING, null));
    }

    public static Transformation getMatrix(BlockModelRotation modelRotation) {
        return modelRotation.getRotation();
    }

    public static BlockModelRotation getRotation(Direction facing) {
        switch (facing) {
            case DOWN:  return BlockModelRotation.X90_Y180;
            case UP:    return BlockModelRotation.X270_Y180;
            case NORTH: return BlockModelRotation.X0_Y0;
            case SOUTH: return BlockModelRotation.X0_Y180;
            case WEST:  return BlockModelRotation.X0_Y270;
            case EAST:  return BlockModelRotation.X0_Y90;
        }
        throw new IllegalArgumentException(String.valueOf(facing));
    }

    protected abstract boolean isRealCable(ModelData modelData);
    protected abstract Optional<BlockState> getFacade(ModelData modelData);
    protected abstract boolean isConnected(ModelData modelData, Direction side);
    protected abstract boolean hasPart(ModelData modelData, Direction side);
    protected abstract PartRenderPosition getPartRenderPosition(ModelData modelData, Direction side);
    protected abstract boolean shouldRenderParts(ModelData modelData);
    protected abstract BakedModel getPartModel(ModelData modelData, Direction side);
    protected abstract IRenderState getRenderState(ModelData modelData);

    @Override
    public List<BakedQuad> getGeneralQuads() {
        Triple<IRenderState, Direction, RenderType> cacheKey = null;
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
            TextureAtlasSprite texture = getParticleIcon();
            Optional<BlockState> blockStateHolder = getFacade(modelData);
            boolean renderCable = isItemStack() || (isRealCable(modelData) && (
                    (!blockStateHolder.isPresent() && this.renderType == RenderType.solid())
                            || (blockStateHolder.isPresent() && this.renderType == RenderType.translucent())));
            for (Direction side : Direction.values()) {
                boolean isConnected = isItemStack() ? side == Direction.EAST || side == Direction.WEST : isConnected(modelData, side);
                boolean hasPart = !isItemStack() && hasPart(modelData, side);
                if (hasPart && shouldRenderParts(modelData)) {
                    try {
                        ret.addAll(getPartModel(modelData, side).getQuads(this.blockState, this.facing, this.rand, this.modelData, this.renderType));
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
                            Vec3 v1 = rotate(new Vec3(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), side).add(.5, .5, .5);
                            Vec3 v2 = rotate(new Vec3(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), side).add(.5, .5, .5);
                            Vec3 v3 = rotate(new Vec3(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), side).add(.5, .5, .5);
                            Vec3 v4 = rotate(new Vec3(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), side).add(.5, .5, .5);
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
                    && this.renderType != null) {
                BakedModel facadeModel = RenderHelpers.getBakedModel(blockStateHolder.get());
                if (facadeModel.getRenderTypes(blockStateHolder.get(), rand, ModelData.EMPTY)
                        .contains(this.renderType)) {
                    for (Direction side : Direction.values()) {
                        boolean isConnected = isItemStack() ? side == Direction.EAST || side == Direction.WEST : isConnected(modelData, side);
                        PartRenderPosition partRenderPosition = PartRenderPosition.NONE;
                        boolean hasPart = !isItemStack() && hasPart(modelData, side);
                        if (hasPart) partRenderPosition = getPartRenderPosition(modelData, side);
                        else if (isConnected) partRenderPosition = CABLE_RENDERPOSITION;
                        ret.addAll(getFacadeQuads(facadeModel, blockStateHolder.get(), side, partRenderPosition));
                    }
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
    public TextureAtlasSprite getParticleIcon() {
        return RegistryEntries.BLOCK_CABLE.texture;
    }

    @Nonnull
    @Override
    public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos,
                                   @Nonnull BlockState state, @Nonnull ModelData tileData) {
        return BlockEntityHelpers.get(world, pos, BlockEntityMultipartTicking.class)
                .map(BlockEntityMultipartTicking::getConnectionState)
                .orElse(ModelData.EMPTY);
    }

    @Override
    public boolean usesBlockLight() {
        return false; // If false, RenderHelper.setupGuiFlatDiffuseLighting() is called
    }

    @Override
    public ItemTransforms getTransforms() {
        return TRANSFORMS;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.all();
    }
}
