package org.cyclops.integrateddynamics.core.part;

import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

import java.util.List;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S> {

    @Getter private Item item = null;

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

}
