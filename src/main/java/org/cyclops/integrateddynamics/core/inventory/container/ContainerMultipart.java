package org.cyclops.integrateddynamics.core.inventory.container;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
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
    public static final String BUTTON_OFFSETS = "button_offsets";

    private static final int PAGE_SIZE = 3;

    private final Optional<PartTarget> target;
    private final Optional<IPartContainer> partContainer;
    private final P partType;
    private final Level world;

    public ContainerMultipart(@Nullable MenuType<?> type, int id, Inventory playerInventory, Container inventory,
                              Optional<PartTarget> target, Optional<IPartContainer> partContainer, P partType) {
        super(type, id, playerInventory, inventory);
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;
        this.world = player.getCommandSenderWorld();

        putButtonAction(ContainerMultipart.BUTTON_SETTINGS, (s, containerExtended) -> {
            if(!world.isClientSide()) {
                PartHelpers.openContainerPart((ServerPlayer) player, target.get().getCenter(), partType);
            }
        });
        putButtonAction(ContainerMultipart.BUTTON_OFFSETS, (s, containerExtended) -> {
            if(!world.isClientSide()) {
                PartHelpers.openContainerPartOffsets((ServerPlayer) player, target.get().getCenter(), partType);
            }
        });
    }

    public Level getLevel() {
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

    public Optional<IPartContainer> getPartContainer() {
        return partContainer;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return PartHelpers.canInteractWith(getTarget().get(), player, this.partContainer.get());
    }

    @Override
    public void onDirty() {

    }
}
