package org.cyclops.integrateddynamics.api.part;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.IInitListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Default implementation of {@link IPartType}.
 * @author rubensworks
 */
public abstract class PartTypeAdapter<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S> {

    private String unlocalizedName = null;

    @Override
    public String getUnlocalizedName() {
        return unlocalizedName != null ? unlocalizedName : (unlocalizedName = getUnlocalizedNameBase() + ".name");
    }

    @Override
    public boolean isSolid(S state) {
        return false;
    }

    @Override
    public void onInit(IInitListener.Step initStep) {

    }

    @Override
    public void toNBT(NBTTagCompound tag, S partState) {
        partState.writeToNBT(tag);
    }

    @Override
    public S fromNBT(NBTTagCompound tag) {
        S partState = constructDefaultState();
        partState.readFromNBT(tag);
        partState.gatherCapabilities((P) this);
        return partState;
    }

    @Override
    public void setUpdateInterval(S state, int updateInterval) {
        state.setUpdateInterval(updateInterval);
    }

    @Override
    public int getUpdateInterval(S state) {
        return state.getUpdateInterval();
    }

    @Override
    public void setPriority(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, int priority) {
        //noinspection deprecation
        state.setPriority(priority);
    }

    @Override
    public int getPriority(S state) {
        return state.getPriority();
    }

    @Override
    public void setChannel(S state, int channel) {
        state.setChannel(channel);
    }

    @Override
    public int getChannel(S state) {
        return state.getChannel();
    }

    @Override
    public boolean isUpdate(S state) {
        return false;
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void beforeNetworkKill(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void afterNetworkAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void afterNetworkReAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

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
    public ItemStack getPickBlock(World world, BlockPos pos, S state) {
        return getItemStack(state);
    }

    @Override
    public S getState(ItemStack itemStack) {
        S partState = null;
        if(!itemStack.isEmpty() && itemStack.getTagCompound() != null
                && itemStack.getTagCompound().hasKey("id", MinecraftHelpers.NBTTag_Types.NBTTagInt.ordinal())) {
            partState = fromNBT(itemStack.getTagCompound());
        }
        if(partState == null) {
            partState = getDefaultState();
        }
        return partState;
    }

    /**
     * @return Constructor call for a new default state for this part type.
     */
    protected abstract S constructDefaultState();

    @Override
    public S getDefaultState() {
        S defaultState = constructDefaultState();
        defaultState.generateId();
        defaultState.gatherCapabilities((P) this);
        return defaultState;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement) {
        if(dropMainElement) {
            itemStacks.add(getItemStack(state));
        }
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, S partState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, S partState, Random random) {

    }

    @Override
    public void onPreRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {

    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, IBlockAccess world, Block neighborBlock) {

    }

    @Override
    public int getConsumptionRate(S state) {
        return 0;
    }

    @Override
    public void postUpdate(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, boolean updated) {
        setEnabled(state, updated);
    }

    @Override
    public boolean isEnabled(S state) {
        return state.isEnabled();
    }

    @Override
    public void setEnabled(S state, boolean enabled) {
        state.setEnabled(enabled);
    }

    @Override
    public void loadTooltip(S state, List<String> lines) {

    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<String> lines) {

    }

    @Override
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState) {
        return oldPartState == null || newPartState == null;
    }

    @Override
    public boolean hasEventSubscriptions() {
        return false;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return Collections.emptySet();
    }

    @Override
    public void onEvent(INetworkEvent event, IPartNetworkElement<P, S> networkElement) {

    }
}
