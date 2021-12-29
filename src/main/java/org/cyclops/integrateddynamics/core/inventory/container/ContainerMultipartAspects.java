package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Container for parts.
 *
 * @author rubensworks
 */
public abstract class ContainerMultipartAspects<P extends IPartType<P, S>, S extends IPartState<P>, A extends IAspect>
        extends ScrollingInventoryContainer<A> implements IDirtyMarkListener {

    public static String BUTTON_SETTINGS = "button_settings";

    private static final int PAGE_SIZE = 3;

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;
    private final Level world;
    private final Map<IAspect, String> aspectPropertyButtons = Maps.newHashMap();

    protected final Container inputSlots;

    public ContainerMultipartAspects(@Nullable MenuType<?> type, int id, Inventory playerInventory, Container inventory,
                                     PartTarget target, Optional<IPartContainer> partContainer, P partType,
                                     List<A> items) {
        super(type, id, playerInventory, inventory, items, (item, pattern) -> {
            // We could cache this if this would prove to be a bottleneck.
            // But we have a small amount of aspects, so this shouldn't be a problem.
            return pattern.matcher(L10NHelpers.localize(item.getTranslationKey()).toLowerCase(Locale.ENGLISH)).matches();
        });
        this.target = target;
        this.partContainer = partContainer.orElseGet(() -> PartHelpers.getPartContainerChecked(target.getCenter()));
        this.partType = partType;
        this.world = player.getCommandSenderWorld();

        this.inputSlots = constructInputSlotsInventory();

        putButtonAction(ContainerMultipartAspects.BUTTON_SETTINGS, (s, containerExtended) -> {
            if (!world.isClientSide()) {
                PartHelpers.openContainerPartSettings((ServerPlayer) player, target.getCenter(), partType);
            }
        });

        for (final IAspect aspect : getUnfilteredItems()) {
            if (aspect.hasProperties()) {
                String buttonId = "button_aspect_" + aspect.getUniqueName();
                aspectPropertyButtons.put(aspect, buttonId);
                putButtonAction(buttonId, (s, containerExtended) -> {
                    if (!world.isClientSide()) {
                        PartHelpers.openContainerAspectSettings((ServerPlayer) player, target.getCenter(), aspect);
                    }
                });
            }
        }
    }

    public P getPartType() {
        return partType;
    }

    public PartTarget getTarget() {
        return target;
    }

    public IPartContainer getPartContainer() {
        return partContainer;
    }

    public S getPartState() {
        return (S) partContainer.getPartState(getTarget().getCenter().getSide());
    }

    public Map<IAspect, String> getAspectPropertyButtons() {
        return Collections.unmodifiableMap(this.aspectPropertyButtons);
    }

    public abstract int getAspectBoxHeight();

    protected Container constructInputSlotsInventory() {
        SimpleInventory inventory = new SimpleInventory(getUnfilteredItemCount(), 1);
        inventory.addDirtyMarkListener(this);
        return inventory;
    }

    @Override
    public void removed(Player player) {
        if (inputSlots instanceof SimpleInventory) {
            ((SimpleInventory) inputSlots).removeDirtyMarkListener(this);
        }
    }

    protected void disableSlot(int slotIndex) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        setSlotPosX(slot, Integer.MIN_VALUE);
        setSlotPosY(slot, Integer.MIN_VALUE);
    }

    protected abstract void enableSlot(int slotIndex, int row);

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public void onScroll(int firstRow) {
        for (int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlot(i);
        }
        super.onScroll(firstRow);
    }

    @Override
    protected void enableElementAt(int row, int elementIndex, A element) {
        super.enableElementAt(row, elementIndex, element);
        enableSlot(elementIndex, row);
    }

    @Override
    public boolean stillValid(Player player) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer);
    }

    public ItemStack writeAspectInfo(boolean generateId, ItemStack itemStack, final IAspect aspect) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(generateId, itemStack, Aspects.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IAspectVariableFacade>() {
            @Override
            public IAspectVariableFacade create(boolean generateId) {
                return new AspectVariableFacade(generateId, getPartState().getId(), aspect);
            }

            @Override
            public IAspectVariableFacade create(int id) {
                return new AspectVariableFacade(id, getPartState().getId(), aspect);
            }
        }, null, null);
    }

}
