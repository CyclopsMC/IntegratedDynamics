package org.cyclops.integrateddynamics.core.part;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.List;
import java.util.Set;

/**
 * A type of part that can be inserted into a
 * {@link org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking}.
 * Only one unique instance for each part should exist, the values are stored inside an
 * {@link org.cyclops.integrateddynamics.core.part.IPartState}.
 * @author rubensworks
 */
public interface IPartType<P extends IPartType<P, S>, S extends IPartState<P>> {

    /**
     * @return The unique name for this part type.
     */
    public String getName();

    /**
     * @return The unlocalized name of this part.
     */
    public String getUnlocalizedName();

    /**
     * @return The item associated with this part type.
     */
    public Item getItem();

    /**
     * Set the item associated with this part type.
     * Can only be called once per type.
     * @param item The item associated with this part type.
     */
    public void setItem(Item item);

    /**
     * @return All possible aspects that can be used in this part type.
     */
    public Set<IAspect> getAspects();

    /**
     * Get the singleton variable for an aspect.
     * @param target The target block.
     * @param partState The state of this part.
     * @param aspect The aspect from the part of this state.
     * @return The variable that exists only once for an aspect in the given part state.
     */
    public IAspectVariable getVariable(PartTarget target, S partState, IAspect aspect);

    /**
     * Write the properties of this part to NBT.
     * An identificator for this part is not required, this is written somewhere else.
     * @param tag The tag to write to. This tag is guaranteed to be empty.
     * @param partState The state of this part.
     */
    public void toNBT(NBTTagCompound tag, S partState);

    /**
     * Read the properties of this part from nbt.
     * This tag is guaranteed to only contain data for this part.
     * @param tag The tag to read from.
     * @return The state of this part.
     */
    public S fromNBT(NBTTagCompound tag);

    /**
     * @return The default state of this part.
     */
    public S getDefaultState();

    /**
     * @param state The state
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval(S state);

    /**
     * @param state The state
     * @return If this element should be updated. This method is only called once during network initialization.
     */
    public boolean isUpdate(S state);

    /**
     * @param state The state
     * @param target The target block.
     * Update at the tick interval specified.
     */
    public void update(PartTarget target, S state);

    /**
     * @param state The state
     * Called right before the network is terminated or will be reset.
     */
    public void beforeNetworkKill(S state);

    /**
     * @param state The state
     * Called right after this network is initialized.
     */
    public void afterNetworkAlive(S state);

    /**
     * Get the itemstack from the given state.
     * @param state The state
     * @return The itemstack possibly containing the state information.
     */
    public ItemStack getItemStack(S state);

    /**
     * Get the part state from the given itemstack.
     * @param itemStack The itemstack possibly containing state information.
     * @return The state contained in the itemstack or the default part state.
     */
    public S getState(ItemStack itemStack);

    /**
     * Add the itemstacks to drop when this element is removed.
     * @param state The state
     * @param itemStacks The itemstack list to add to.
     */
    public void addDrops(S state, List<ItemStack> itemStacks);

    /**
     * Create a network element for this part type.
     * @param partContainerFacade The facade for reaching the container this part is/will be part of.
     * @param pos The position this network element is/will be placed at.
     * @param side The side this network element is/will be placed at.
     * @return A new network element instance.
     */
    public INetworkElement createNetworkElement(IPartContainerFacade partContainerFacade, DimPos pos, EnumFacing side);

    /**
     * Called when a part is right-clicked.
     * @param world The world.
     * @param pos The position of the block this part is part of.
     * @param state The block state of the parent block.
     * @param partState The state of this part.
     * @param player The player activating the part.
     * @param side The side this part is attached on.
     * @param hitX The X hit position.
     * @param hitY The Y hit position.
     * @param hitZ The Z hit position.
     * @return True if the further processing should be stopped.
     */
    public boolean onPartActivated(World world, BlockPos pos, IBlockState state, S partState, EntityPlayer player,
                                   EnumFacing side, float hitX, float hitY, float hitZ);

    /**
     * Get the base block state that will be rendered for this part.
     * An appropriate {@link org.cyclops.integrateddynamics.core.block.IgnoredBlock#FACING} property will be set.
     * @param tile The tile entity.
     * @param x X
     * @param y Y
     * @param z Z
     * @param partialTick The partial tick
     * @param destroyStage The stage of the block destruction.
     * @return The block state to render with.
     */
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                        int destroyStage);

}
