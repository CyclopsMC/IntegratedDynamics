package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.client.multipart.MultipartSpecialRenderer;
import mcmultipart.multipart.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRenderer;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

/**
 * Helpers for McMultiPart
 * @author rubensworks
 */
public class McMultiPartHelpers {

    @SideOnly(Side.CLIENT)
    public static ModelResourceLocation CABLE_MODEL_LOCATION;

    /**
     * Load common.
     */
    public static void load() {
        MultipartRegistry.registerPart(PartCable.class, Reference.MOD_ID + ":cable");
        for(final IPartType partType : PartTypes.REGISTRY.getPartTypes()) {
            // Force dummy initialization for all parts so that default blockstate doesn't fail.
            MultipartRegistry.registerPartFactory(new IPartFactory() {
                @Override
                public IMultipart createPart(String s, boolean b) {
                    PartPartType partPart = new PartPartType();
                    partPart.init(EnumFacing.NORTH, partType);
                    return partPart;
                }
            }, PartPartType.getType(partType));
        }
        CableHelpers.addInterfaceRetriever(new CableHelpers.IInterfaceRetriever() {
            @Override
            public <C> C getInterface(IBlockAccess world, BlockPos pos, Class<C> clazz) {
                    IMultipartContainer multipartContainer = TileHelpers.getSafeTile(world, pos, IMultipartContainer.class);
                    if(multipartContainer != null) {
                        for(IMultipart part : multipartContainer.getParts()) {
                            if(clazz.isInstance(part)) {
                                return clazz.cast(part);
                            }
                        }
                    }
                return null;
            }
        });
        ItemBlockCable.addUseAction(new ItemBlockCable.IUseAction() {
            @Override
            public boolean attempItemUseTarget(ItemStack itemStack, World world, BlockPos pos, BlockCable blockCable) {
                PartCable partCable = new PartCable();
                if(MultipartHelper.canAddPart(world, pos, partCable)) {
                    if(!world.isRemote) {
                        MultipartHelper.addPart(world, pos, partCable);
                    }
                    return true;
                }
                return false;
            }
        });
        ItemPart.addUseAction(new ItemPart.IUseAction() {
            @Override
            public boolean attempItemUseTarget(ItemPart itemPart, ItemStack itemStack, World world, BlockPos pos, EnumFacing sideHit) {
                PartPartType partPart = new PartPartType(sideHit.getOpposite(), itemPart.getPart());
                pos = pos.offset(sideHit);
                if(MultipartHelper.getPartContainer(world, pos) != null && MultipartHelper.canAddPart(world, pos, partPart)) {
                    if(!world.isRemote) {
                        MultipartHelper.addPart(world, pos, partPart);
                    }
                    return true;
                }
                return false;
            }
        });
        MultipartRegistry.registerPartConverter(new BlockCableConverter());
        MultipartRegistry.registerReversePartConverter(new BlockCableReverseConverter());
    }

    protected static String getModelPathCable() {
        return Reference.MOD_ID + ":" + BlockCableConfig._instance.getNamedId();
    }

    /**
     * Load client
     */
    @SideOnly(Side.CLIENT)
    public static void loadClient() {
        MinecraftForge.EVENT_BUS.register(new McMultiPartEventListener());
        final ModelResourceLocation blockLocation = new ModelResourceLocation(getModelPathCable(), "multipart");
        ModelLoader.setCustomStateMapper(BlockCable.getInstance(), new StateMapperBase() {
            protected ModelResourceLocation getModelResourceLocation(IBlockState blockState) {
                return blockLocation;
            }
        });
        CABLE_MODEL_LOCATION = blockLocation;
        MultipartRegistryClient.bindMultipartSpecialRenderer(PartPartType.class, new MultipartSpecialRenderer<PartPartType>() {
            @Override
            public void renderMultipartAt(PartPartType partPartType, double x, double y, double z, float partialTick, int destroyStage) {
                IPartContainer partContainer = partPartType.getPartContainer();
                if(partContainer != null) {
                    for (IPartOverlayRenderer renderer : PartOverlayRenderers.REGISTRY.getRenderers((IPartType<?, ?>) partPartType.getPartType())) {
                        renderer.renderPartOverlay(partContainer, x, y, z, partialTick, destroyStage, partPartType.getFacing(), partPartType.getPartType(), rendererDispatcher);
                    }
                }
            }
        });
    }

}
