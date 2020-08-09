package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Container for parts.
 * @author rubensworks
 */
public abstract class ContainerMultipart<P extends IPartType<P, S>, S extends IPartState<P>>
        extends InventoryContainer implements IDirtyMarkListener {

    public static final String BUTTON_SETTINGS = "button_settings";

    private static final int PAGE_SIZE = 3;

    private final Optional<PartTarget> target;
    private final Optional<IPartContainer> partContainer;
    private final P partType;
    private final World world;

    public ContainerMultipart(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory, IInventory inventory,
                              Optional<PartTarget> target, Optional<IPartContainer> partContainer, P partType) {
        super(type, id, playerInventory, inventory);
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;
        this.world = player.getEntityWorld();

        putButtonAction(ContainerMultipart.BUTTON_SETTINGS, (s, containerExtended) -> {
            if(!world.isRemote()) {
                PartHelpers.openContainerPart((ServerPlayerEntity) player, target.get().getCenter(), partType);
            }
        });
    }

    public World getWorld() {
        return world;
    }

    public P getPartType() {
        return partType;
    }

    public Optional<PartTarget> getTarget() {
        return target;
    }

    public Optional<S> getPartState() {
        return partContainer.map(p -> (S) p.getPartState(getTarget().get().getCenter().getSide()));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return PartHelpers.canInteractWith(getTarget().get(), player, this.partContainer.get());
    }

    @Override
    public void onDirty() {

    }
}
