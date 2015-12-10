package org.cyclops.integrateddynamics.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.tileentity.ITileCableFacadeable;
import org.cyclops.integrateddynamics.client.render.model.FacadeModel;

/**
 * An item that represents a facade of a certain type.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemFacade extends ConfigurableItem {

    private static ItemFacade _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemFacade getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemFacade(ExtendedConfig eConfig) {
        super(eConfig);
    }

    public IBlockState getFacadeBlock(ItemStack itemStack) {
        if(itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getTagCompound();
            String blockName = tag.getString("blockName");
            int meta = tag.getInteger("meta");
            return BlockHelpers.deserializeBlockState(Pair.of(blockName, meta));
        }
        return null;
    }

    public ItemStack getFacadeBlockItem(ItemStack itemStack) {
        IBlockState blockState = getFacadeBlock(itemStack);
        if(blockState != null) {
            return BlockHelpers.getItemStackFromBlockState(blockState);
        }
        return null;
    }

    public void writeFacadeBlock(ItemStack itemStack, IBlockState blockState) {
        NBTTagCompound tag = ItemStackHelpers.getSafeTagCompound(itemStack);
        Pair<String, Integer> serializedBlockState = BlockHelpers.serializeBlockState(blockState);
        tag.setString("blockName", serializedBlockState.getLeft());
        tag.setInteger("meta", serializedBlockState.getRight());
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        String suffix = EnumChatFormatting.ITALIC + L10NHelpers.localize("general.integrateddynamics.info.none");
        ItemStack itemStackInner = getFacadeBlockItem(itemStack);
        if(itemStackInner != null) {
            suffix = getFacadeBlockItem(itemStack).getDisplayName();
        }
        return super.getItemStackDisplayName(itemStack) + " - " + suffix;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        if(!world.isRemote) {
            ITileCableFacadeable facadeContainer = (ITileCableFacadeable) world.getTileEntity(pos);
            IBlockState blockState = getFacadeBlock(itemStack);
            if(facadeContainer != null && blockState != null) {
                // Add facade to existing cable
                if (!facadeContainer.hasFacade()) {
                    facadeContainer.setFacade(blockState);
                    ItemBlockCable.playPlaceSound(world, pos);
                    itemStack.stackSize--;
                }
            }
            return true;
        }
        return super.onItemUse(itemStack, playerIn, world, pos, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean hasDynamicModel() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IBakedModel createDynamicModel() {
        return new FacadeModel();
    }

    @SubscribeEvent
    @Override
    public void onModelBakeEvent(ModelBakeEvent event){
        // Don't throw away the original model, but use if for displaying an unbound facade item.
        IBakedModel oldModel = (IBakedModel) event.modelRegistry.getObject(eConfig.dynamicItemVariantLocation);
        FacadeModel.emptyModel = oldModel;
        super.onModelBakeEvent(event);
    }

}
