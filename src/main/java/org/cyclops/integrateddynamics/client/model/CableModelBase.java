package org.cyclops.integrateddynamics.client.model;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.model.DelegatingDynamicItemAndBlockModel;
import org.cyclops.cyclopscore.helper.ModelHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Random;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A base dynamic model for cables.
 * @author rubensworks
 */
public abstract class CableModelBase extends DelegatingDynamicItemAndBlockModel {

    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Cache<Triple<IRenderState, EnumFacing, BlockRenderLayer>, List<BakedQuad>> CACHE_QUADS = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

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

    protected static final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> TRANSFORMS =
            ModelHelpers.modifyDefaultTransforms(ImmutableMap.of(
                    ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                            new Vector3f(0, 1f / 32, 0),
                            TRSRTransformation.quatFromXYZDegrees(new Vector3f(0, 45, 0)),
                            new Vector3f(0.4F, 0.4F, 0.4F),
                            null)),
                    ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                            new Vector3f(0, 1f / 32, 0),
                            TRSRTransformation.quatFromXYZDegrees(new Vector3f(0, 225, 0)),
                            new Vector3f(0.4F, 0.4F, 0.4F),
                            null))
            ));

    public CableModelBase(IBlockState blockState, EnumFacing facing, long rand) {
        super(blockState, facing, rand);
    }

    public CableModelBase(ItemStack itemStack, World world, EntityLivingBase entity) {
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

    private EnumFacing getSideFromVecs(Vec3d a, Vec3d b, Vec3d c) {
        int dir = a.y == b.y && b.y == c.y ? 0 : (a.x == b.x && b.x == c.x ? 2 : 4);
        if (dir == 0) {
            dir += (c.y >= 0.5) ? 1 : 0;
        } else if (dir == 2) {
            dir += (c.x >= 0.5) ? 1 : 0;
        } else if (dir == 4) {
            dir += (c.z >= 0.5) ? 1 : 0;
        }
        return EnumFacing.byIndex(dir);
    }

    public List<BakedQuad> getFacadeQuads(IBlockState blockState, EnumFacing side, PartRenderPosition partRenderPosition) {
        Random rand = new Random();
        List<BakedQuad> ret = Lists.newLinkedList();
        IBakedModel model = RenderHelpers.getBakedModel(blockState);
        BakedQuad originalQuad = model.getQuads(blockState, side, rand.nextLong()).get(0);
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
        return ret;
    }

    private void addFacadeQuad(List<BakedQuad> quads, BakedQuad originalQuad, float u0, float v0, float u1, float v1, EnumFacing side) {
        org.lwjgl.util.vector.Vector3f from = new org.lwjgl.util.vector.Vector3f(u0 * 16f, v0 * 16f, 0f);
        org.lwjgl.util.vector.Vector3f to = new org.lwjgl.util.vector.Vector3f(u1 * 16f, v1 * 16f, 0f);
        TextureAtlasSprite texture = originalQuad.getSprite();
        float[] uvArray = { 16f - u1 * 16f, 16f - v1 * 16f, 16f - u0 * 16f, 16f - v0 * 16f };
        int ROTATION_NONE = 0;
        BlockFaceUV blockFaceUV = new BlockFaceUV(uvArray, ROTATION_NONE);
        EnumFacing NO_FACE_CULLING = null;
        int TINT_INDEX_NONE = -1;
        String DUMMY_TEXTURE_NAME = "";
        BlockPartFace blockPartFace = new BlockPartFace(NO_FACE_CULLING, TINT_INDEX_NONE, DUMMY_TEXTURE_NAME, blockFaceUV);
        ITransformation transformation = TRSRTransformation.from(side);
        BlockPartRotation DEFAULT_ROTATION = null;
        boolean UV_LOCKED = true;
        boolean APPLY_SHADING = true;
        quads.add(FACE_BAKERY.makeBakedQuad(from, to, blockPartFace, texture, EnumFacing.NORTH, transformation, DEFAULT_ROTATION, UV_LOCKED, APPLY_SHADING));
    }

    protected abstract boolean isRealCable();
    protected abstract Optional<IBlockState> getFacade();
    protected abstract boolean isConnected(EnumFacing side);
    protected abstract boolean hasPart(EnumFacing side);
    protected abstract PartRenderPosition getPartRenderPosition(EnumFacing side);
    protected abstract boolean shouldRenderParts();
    protected abstract IBakedModel getPartModel(EnumFacing side);
    protected abstract IRenderState getRenderState();

    @Override
    public List<BakedQuad> getGeneralQuads() {
        Triple<IRenderState, EnumFacing, BlockRenderLayer> cacheKey = null;
        List<BakedQuad> cachedQuads = null;
        if (GeneralConfig.cacheCableModels) {
            IRenderState renderState = getRenderState();
            if (renderState != null) {
                cacheKey = Triple.of(renderState, this.facing, MinecraftForgeClient.getRenderLayer());
                cachedQuads = CACHE_QUADS.getIfPresent(cacheKey);
            }
        }
        if (cachedQuads == null) {
            List<BakedQuad> ret = Lists.newLinkedList();
            TextureAtlasSprite texture = getParticleTexture();
            Optional<IBlockState> blockStateHolder = getFacade();
            boolean renderCable = isItemStack() || (isRealCable() && MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID && !blockStateHolder.isPresent());
            for (EnumFacing side : EnumFacing.values()) {
                boolean isConnected = isItemStack() ? side == EnumFacing.EAST || side == EnumFacing.WEST : isConnected(side);
                boolean hasPart = !isItemStack() && hasPart(side);
                if (hasPart && shouldRenderParts()) {
                    try {
                        ret.addAll(getPartModel(side).getQuads(this.blockState, this.facing, this.rand));
                    } catch (Exception e) {
                        // Skip rendering this part, could occur when the player is still logging in.
                    }
                }
                if (renderCable) {
                    if (isConnected || hasPart) {
                        int i = 0;
                        float[][][] quadVertexes = this.quadVertexes;
                        if (hasPart) {
                            PartRenderPosition partRenderPosition = getPartRenderPosition(side);
                            float depthFactor = partRenderPosition == PartRenderPosition.NONE ? 0F : partRenderPosition.getDepthFactor();
                            quadVertexes = makeQuadVertexes(MIN, MAX, 1F - depthFactor);
                        }
                        for (float[][] v : quadVertexes) {
                            Vec3d v1 = rotate(new Vec3d(v[0][0] - .5, v[0][1] - .5, v[0][2] - .5), side).add(.5, .5, .5);
                            Vec3d v2 = rotate(new Vec3d(v[1][0] - .5, v[1][1] - .5, v[1][2] - .5), side).add(.5, .5, .5);
                            Vec3d v3 = rotate(new Vec3d(v[2][0] - .5, v[2][1] - .5, v[2][2] - .5), side).add(.5, .5, .5);
                            Vec3d v4 = rotate(new Vec3d(v[3][0] - .5, v[3][1] - .5, v[3][2] - .5), side).add(.5, .5, .5);
                            EnumFacing realSide = getSideFromVecs(v1, v2, v3);

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
                            ret.add(new BakedQuad(data, -1, realSide, texture, true, DefaultVertexFormats.ITEM));
                        }
                    } else {
                        addBakedQuad(ret, MIN, MAX, MIN, MAX, MAX, texture, side);
                    }
                }
            }

            if (blockStateHolder.isPresent() && shouldRenderParts()) {
                for (EnumFacing side : EnumFacing.values()) {
                    boolean isConnected = isItemStack() ? side == EnumFacing.EAST || side == EnumFacing.WEST : isConnected(side);
                    PartRenderPosition partRenderPosition = PartRenderPosition.NONE;
                    boolean hasPart = !isItemStack() && hasPart(side);
                    if (hasPart)          partRenderPosition = getPartRenderPosition(side);
                    else if (isConnected) partRenderPosition = CABLE_RENDERPOSITION;
                    ret.addAll(getFacadeQuads(blockStateHolder.get(), side, partRenderPosition));
                }
            }

            // Close the cable connections for items
            if (isItemStack()) {
                addBakedQuad(ret, MIN, MAX, MIN, MAX, 1, texture, EnumFacing.EAST);
                addBakedQuad(ret, MIN, MAX, MIN, MAX, 1, texture, EnumFacing.WEST);
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
        return BlockCable.getInstance().texture;
    }

    public IExtendedBlockState getState() {
        return (IExtendedBlockState) this.blockState;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        TRSRTransformation tr = ModelHelpers.DEFAULT_PERSPECTIVE_TRANSFORMS.get(cameraTransformType);
        Matrix4f mat = null;
        if(tr != null && !tr.equals(TRSRTransformation.identity())) mat = TRSRTransformation.blockCornerToCenter(tr).getMatrix();
        return Pair.of(this, mat);
    }
}
