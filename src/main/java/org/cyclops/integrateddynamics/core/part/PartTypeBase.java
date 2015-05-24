package org.cyclops.integrateddynamics.core.part;

import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

import java.util.List;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S>,
        IGuiContainerProvider {

    @Getter
    private Item item = null;
    @Getter
    private final int guiID;

    public PartTypeBase() {
        if(hasGui()) {
            this.guiID = Helpers.getNewId(getMod(), Helpers.IDType.GUI);
            getMod().getGuiHandler().registerGUI(this, ExtendedGuiHandler.PART);
        } else {
            this.guiID = -1;
        }
    }

    @Override
    public INetworkElement createNetworkElement(IPartContainerFacade partContainerFacade, DimPos pos, EnumFacing side) {
        return new PartNetworkElement(this, partContainerFacade, pos, side);
    }

    @Override
    public ItemStack getItemStack(S state) {
        NBTTagCompound tag = new NBTTagCompound();
        toNBT(tag, state);
        ItemStack itemStack = new ItemStack(getItem());
        itemStack.setTagCompound(tag);
        return itemStack;
    }

    @Override
    public S getState(ItemStack itemStack) {
        S partState = null;
        if(itemStack != null && itemStack.getTagCompound() != null) {
            partState = fromNBT(itemStack.getTagCompound());
        }
        if(partState == null) {
            partState = getDefaultState();
        }
        return partState;
    }

    @Override
    public void addDrops(S state, List<ItemStack> itemStacks) {
        itemStacks.add(getItemStack(state));
    }

    public void setItem(Item item) {
        if(this.item != null) {
            throw new IllegalStateException(String.format("Could not set the new item %s in %s with the already set " +
                    "item %s.", this.item, this, item));
        }
        this.item = item;
    }

    /**
     * @return Constructor call for a new default state for this part type.
     */
    protected abstract S constructDefaultState();

    @Override
    public final S getDefaultState() {
        S defaultState = constructDefaultState();
        defaultState.generateId();
        return defaultState;
    }

    protected abstract boolean hasGui();

    @Override
    public boolean onPartActivated(World world, BlockPos pos, IBlockState state, S partState, EntityPlayer player,
                                   EnumFacing side, float hitX, float hitY, float hitZ) {
        // Drop through if the player is sneaking
        if(player.isSneaking()) {
            return false;
        }

        getMod().getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, side); // Pass the side as extra data to the gui
        if(!world.isRemote && hasGui()) {
            player.openGui(getMod().getModId(), getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

}
