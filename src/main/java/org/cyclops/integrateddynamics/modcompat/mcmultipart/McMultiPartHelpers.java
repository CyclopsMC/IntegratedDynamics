package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

/**
 * Helpers for McMultiPart
 * @author rubensworks
 */
public class McMultiPartHelpers {

    @SideOnly(Side.CLIENT)
    public static ModelResourceLocation CABLE_MODEL_LOCATION;

    public static void load() {
        MultipartRegistry.registerPart(PartCable.class, Reference.MOD_ID + ":cable");
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
        MultipartRegistry.registerPartConverter(new BlockCableConverter());
        MultipartRegistry.registerReversePartConverter(new BlockCableReverseConverter());
    }

    public static String getModelPathCable() {
        return Reference.MOD_ID + ":" + BlockCableConfig._instance.getNamedId();
    }

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
    }

}
