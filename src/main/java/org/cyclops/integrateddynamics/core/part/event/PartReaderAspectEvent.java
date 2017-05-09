package org.cyclops.integrateddynamics.core.part.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;

import javax.annotation.Nullable;

/**
 * An event that is posted in the Forge event bus when a reader aspect was written by a player.
 * The resulting itemstack can be modified.
 * @author rubensworks
 */
public class PartReaderAspectEvent<P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>, A extends IAspectRead> extends PartAspectEvent<P, S, A> {

    private ItemStack itemStack;

    public PartReaderAspectEvent(INetwork network, IPartNetwork partNetwork, PartTarget target, P partType, S partState,
                                 @Nullable EntityPlayer entityPlayer, A aspect, ItemStack itemStack) {
        super(network, partNetwork, target, partType, partState, entityPlayer, aspect);
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
