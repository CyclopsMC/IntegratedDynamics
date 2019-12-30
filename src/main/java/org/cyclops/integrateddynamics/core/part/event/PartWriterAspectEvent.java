package org.cyclops.integrateddynamics.core.part.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;

import javax.annotation.Nullable;

/**
 * An event that is posted in the Forge event bus when a write aspect is enabled by a player.
 * @author rubensworks
 */
public class PartWriterAspectEvent<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>, A extends IAspectWrite> extends PartAspectEvent<P, S, A> {

    private final ItemStack itemStack;

    public PartWriterAspectEvent(INetwork network, IPartNetwork partNetwork, PartTarget target, P partType, S partState,
                                 @Nullable PlayerEntity entityPlayer, A aspect, ItemStack itemStack) {
        super(network, partNetwork, target, partType, partState, entityPlayer, aspect);
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
